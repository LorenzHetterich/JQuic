package jquic.base.engine;

import static lsquicbindings.Constants.lsquic;
import static usercodebindings.Constants.usercode;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;

import jquic.base.connection.QuicConnection;
import jquic.base.stream.QuicStream;
import lsquicbindings.struct.lsquic_engine_api;
import lsquicbindings.struct.lsquic_engine_api.ea_get_ssl_ctx;
import lsquicbindings.struct.lsquic_engine_api.lsquic_packets_out_f;
import lsquicbindings.struct.lsquic_engine_settings;
import lsquicbindings.struct.lsquic_http_headers;
import lsquicbindings.struct.lsquic_stream_if;
import lsquicbindings.struct.sockaddr_in;

/**
 * Wrapper class for lsquic_engine_t
 */
public class QuicEngine {

	/**
	 * id of next connection
	 */
	private long cur_conn;

	/**
	 * mapping from connection id to connection
	 */
	private Map<Long, QuicConnection> connections;

	/**
	 * native lsquic_engine_t
	 */
	private Pointer engine;

	/**
	 * stream callback interface
	 */
	public lsquic_stream_if.ByReference stream_if;
	
	/**
	 * settings
	 */
	public lsquic_engine_settings.ByReference es_settings;
	
	/**
	 * supplier for {@link jquic.base.connection.QuicConnection connections}
	 */
	private ConnectionSupplier connection_supplier = QuicConnection::new;

	/**
	 * this reference is required so api does not get garbage collected (and thus the struct freed)
	 */
	public lsquic_engine_api.ByReference api;
	
	/**
	 * Constructor
	 * 
	 * @param flags         engine flags
	 * @param set_callbacks consumer to set optional callbacks in {@link #stream_if}
	 * @param set_api       consumer to set additional fields in engine api
	 * @param packets_out   callback for sending packets
	 * @param get_ssl_ctx   callback for ssl context (may be null for client)
	 */
	public QuicEngine(int flags, Consumer<lsquic_stream_if.ByReference> set_callbacks,
			Consumer<lsquic_engine_api.ByReference> set_api, lsquic_packets_out_f packets_out,
			ea_get_ssl_ctx get_ssl_ctx) {
		this.connections = new HashMap<>();

		api = new lsquic_engine_api.ByReference();
		es_settings = new lsquic_engine_settings.ByReference();
		es_settings.es_cc_algo = 2;
		lsquic_engine_init_settings(es_settings, flags);
		es_settings.read();

		api.ea_packets_out = packets_out;
		api.ea_get_ssl_ctx = get_ssl_ctx;
		api.ea_settings = es_settings;

		stream_if = new lsquic_stream_if.ByReference();
		stream_if.on_new_conn = this::on_new_conn;
		stream_if.on_conn_closed = this::connection_closed;

		stream_if.on_new_stream = this::new_stream;

		stream_if.on_read = usercode::stream_read_cb;
		stream_if.on_write = usercode::stream_write_cb;
		stream_if.on_close = this::stream_close;

		if (set_callbacks != null) {
			set_callbacks.accept(stream_if);
		}

		stream_if.write();

		api.ea_stream_if = stream_if;

		if (set_api != null) {
			set_api.accept(api);
		}

		api.write();

		engine = lsquic.lsquic_engine_new(flags, api);
		
	}

	/**
	 * Setter for {@link #connection_supplier}
	 * 
	 * @param connection_supplier {@link #connection_supplier}
	 */
	public void setConnectionSupplier(ConnectionSupplier connection_supplier) {
		this.connection_supplier = connection_supplier;
	}

	/**
	 * Getter for {@link #engine}
	 * 
	 * @return {@link #engine}
	 */
	public Pointer getNative() {
		return engine;
	}

	/**
	 * finds a connection by id
	 * 
	 * @param id of the connection
	 * @return the connection with matching id
	 * @throws RuntimeException if no such connection can be found
	 */
	public QuicConnection findConnection(long id) {
		QuicConnection connection = connections.get(id);

		if (connection == null) {
			throw new RuntimeException("No connection with id " + id);
		}

		return connection;
	}

	/**
	 * adds a connection
	 * 
	 * @param connection native lsquic_connection_t
	 * @param id         of connection
	 */
	public void addConnection(Pointer connection, long id) {
		QuicConnection conn = connection_supplier.apply(id, connection, this);
		conn.sni = this.get_sni(connection);
		connections.put(id, conn);
	}

	/**
	 * removes a connection by id
	 * 
	 * @param id of connection
	 * @return removed connection (or null)
	 */
	public QuicConnection removeConnection(long id) {
		QuicConnection conn = connections.remove(id);
		
		if(conn != null) {
			conn.onClosed();
		}
		
		return conn;
	}
	
	/**
	 * Method that is called when the engine is destroyed. May be overwritten.
	 */
	public void onDestroy() {

	}

	/**
	 * destroys the wrapper as well as the native lsquic_engine_t
	 */
	public synchronized void destroy() {
		onDestroy();
		lsquic.lsquic_engine_destroy(engine);
		connections.clear();
		stream_if = null;
		connections = null;
		engine = null;
	}
	
	/*
	 * Methods that call the usercode library for improved speed (supplying java methods as callbacks requires a transition 
	 * from native code to java code. This is slow, thus writing them in C improves our speed
	 * (Even though some operations are optimized like this, we were only going for the low-hanging fruits for now, as speed is not a goal of this implementation)
	 * The next low-hanging fruit would be the UDP packet sending / receiving callbacks.
	 */
	
	/**
	 * Synchronized wrapper for (usercode) stream_write
	 * basically uses the usercode library for a seemingly synchronous write operation
	 * @param stream stream to write to
	 * @param data array containing data to write
	 * @param offset start offset within data array
	 * @param length amount of bytes to write
	 */
	public int stream_write(QuicStream stream, Pointer context, byte[] data, int offset, int length) {
		// allocate native memory block and copy data we want to write to it
		Memory mem = new Memory(length);
		mem.write(0, data, offset, length);
		
		// this needs to be synchronized, as lsquic library calls are made
		synchronized(this) {
			// if the output was closed whilst waiting for the lock, we cancel the write operation
			if(stream.out.isClosed()) {
				return -1;
			}
			
			// initiate write operation
			usercode.stream_write(stream.getNative(), context, mem, 0, length);
			// tell the lsquic library it should call our write callback 
			// (TODO: this can be moved elsewhere for speed improvements. 
			// Also, callback is not always needed as we could call lsquic_stream_write directly 
			// if we just write a few bytes.)
			stream_wantwrite(stream.getNative(), 1);
		}
		
		// wait until write operation is done. Note that this MUST NOT be synchronized, else we block the whole engine and deadlock.
		// locking is handled by the native code :)
		return usercode.stream_write_wait(context);
	}
	
	/**
	 * Synchronized wrapper for (usercode) stream_read
	 * basically uses the usercode library for a seemingly synchronous read operation
	 * @param stream stream to read from
	 * @param data array to read to
	 * @param offset start offset within data array
	 * @param min minimum amount of bytes to read (if you know you need to read alot of bytes, setting this to a higher value will increase overall speed)
	 * @param max maximum amount of bytes to read
	 */
	public int stream_read(QuicStream stream, byte[] data, int offset, int min, int max) {
		// allocate native memory to read to
		Memory mem = new Memory(max);
		

		// this needs to be synchronized, as lsquic library calls are made
		synchronized(this) {
			
			// if input was close whilst waiting for the lock, the read operation is cancelled 
			if(stream.in.isClosed()) {
				return -1;
			}
			
			// initialize read operation
			usercode.stream_read(stream.getNative(), stream.context, mem, min, max);
			
			// tell the lsquic library it should call our read callback 
			// (TODO: this can be moved elsewhere for speed improvements. 
			// Also, callback is not always needed as we could call lsquic_stream_read directly 
			// if we just read a few bytes.)
			stream_wantread(stream.getNative(), 1);
		}
		
		// wait until read operation is done. Note that this MUST NOT be synchronized, else we block the whole engine and deadlock.
		// locking is handled by the native code :)
		int amount = usercode.stream_read_wait(stream.context, max);
		
		// if read operation failed, return -1
		if(amount <= 0) {
			return -1;
		}
		
		// copy the data we read to the supplied java byte array
		mem.read(0, data, offset, amount);	
		
		// return amount of bytes read
		return amount;
	}

	/*
	 * Operations that require (partial) locking. Synchronizing (parts of) these operations per engine
	 * makes sure no data-races occur within the lsquic library
	 */

	/**
	 * wrapper for native lsquic_engine_connect
	 * 
	 * @param version  quic version to use
	 * @param local_sa local address
	 * @param peer_sa  remote address
	 * @param peer_ctx context for peer (may be null)
	 * @param conn_ctx initial connection context (may be null)
	 * @param sni      server name indication (may be null)
	 * 
	 * @return newly created connection
	 * @see #connect(int, Pointer, Pointer, Pointer, Pointer, String)
	 * @see #connect(int, Pointer, Pointer, Pointer, Pointer, String, short,
	 *      Pointer, long, Pointer, long)
	 */
	public QuicConnection connect(int version, InetSocketAddress local_sa, InetSocketAddress peer_sa, Pointer peer_ctx,
			Pointer conn_ctx, String sni) {

		sockaddr_in.ByReference local = sockaddr_in.ByReference.fromInetAddress(local_sa);
		sockaddr_in.ByReference peer = sockaddr_in.ByReference.fromInetAddress(peer_sa);

		local.write();
		peer.write();

		QuicConnection conn = connect(version, local.getPointer(), peer.getPointer(), peer_ctx, conn_ctx, sni);

		local.read();
		peer.read();

		return conn;
	}

	/**
	 * wrapper for native lsquic_engine_connect
	 * 
	 * @param version  quic version to use
	 * @param local_sa local address
	 * @param peer_sa  remove address
	 * @param peer_ctx context for peer (may be null)
	 * @param conn_ctx connection context (may be null)
	 * @param sni      server name indication (may be null)
	 * 
	 * @return newly created connection
	 * 
	 * @see #connect(int, InetSocketAddress, InetSocketAddress, Pointer, Pointer,
	 *      String)
	 * @see #connect(int, Pointer, Pointer, Pointer, Pointer, String, short,
	 *      Pointer, long, Pointer, long)
	 */
	public QuicConnection connect(int version, Pointer local_sa, Pointer peer_sa, Pointer peer_ctx, Pointer conn_ctx,
			String sni) {
		
		Pointer p = this.connect(version, local_sa, peer_sa, peer_ctx, conn_ctx, sni, (short) 0, null, 0, null, 0);
		Pointer context = this.get_connection_ctx(p);
		
		return this.findConnection(Pointer.nativeValue(context));
	}

	/**
	 * synchronized wrapper for lsquic_engine_process_conns
	 */
	public synchronized void process_conns() {
		lsquic.lsquic_engine_process_conns(engine);
	}

	/**
	 * synchronized wrapper for lsquic_engine_packet_in
	 */
	public synchronized int packet_in(byte[] udp_payload, long size, Pointer sa_local, Pointer sa_peer,
			Pointer peer_ctx, int ecn) {
		return lsquic.lsquic_engine_packet_in(engine, udp_payload, size, sa_local, sa_peer, peer_ctx, ecn);
	}

	/**
	 * synchronized wrapper for lsquic_conn_get_ctx
	 */
	public synchronized Pointer get_connection_ctx(Pointer connection) {
		return lsquic.lsquic_conn_get_ctx(connection);
	}

	/**
	 * synchronized wrapper for lsquic_stream_close
	 */
	public synchronized int close_stream(Pointer stream) {
		return lsquic.lsquic_stream_close(stream);
	}

	/**
	 * synchronized wrapper for lsquic_stream_conn
	 */
	public synchronized Pointer get_stream_conn(Pointer stream) {
		return lsquic.lsquic_stream_conn(stream);
	}

	/**
	 * synchronized wrapper for lsquic_stream_flush
	 */
	public synchronized int stream_flush(QuicStream stream) {
		if(stream.out.isClosed())
			return 1;
		return lsquic.lsquic_stream_flush(stream.getNative());
	}

	/**
	 * synchronized wrapper for lsquic_stream_read
	 */
	public synchronized long stream_read(Pointer stream, Pointer buf, long buf_size) {
		return lsquic.lsquic_stream_read(stream, buf, buf_size);
	}

	/**
	 * synchronized wrapper for lsquic_stream_write
	 */
	public synchronized long lsquic_stream_write(Pointer stream, Pointer buf, long buf_size) {
		return lsquic.lsquic_stream_write(stream, buf, buf_size);
	}
	
	/**
	 * synchronized wrapper for lsquic_stream_wantwrite
	 */
	public  synchronized int stream_wantwrite(Pointer stream, int value) {
		return lsquic.lsquic_stream_wantwrite(stream, value);
	}

	/**
	 * synchronized wrapper for lsquic_stream_wantread
	 */
	public synchronized int stream_wantread(Pointer stream, int value) {
		return lsquic.lsquic_stream_wantread(stream, value);
	}

	/**
	 * synchronized wrapper for lsquic_conn_make_stream
	 */
	public synchronized void make_stream(Pointer connection) {
		lsquic.lsquic_conn_make_stream(connection);
	}
	
	/**
	 * synchronized wrapper for lsquic_engine_has_unsent_packets
	 */
	public synchronized int lsquic_engine_has_unsent_packets() {
		if(engine == null)
			return 1;
		return lsquic.lsquic_engine_has_unsent_packets(engine);
	}
	
	/**
	 * synchronized wrapper for lsquic_engine_send_unsent_packets
	 */
	public synchronized void lsquic_engine_send_unsent_packets() {
		if(engine == null)
			return;
		lsquic.lsquic_engine_send_unsent_packets(engine);
	}

	/**
	 * synchronized wrapper for lsquic_engine_connect
	 */
	public synchronized Pointer connect(int version, Pointer local_sa, Pointer peer_sa, Pointer peer_ctx,
			Pointer conn_ctx, String sni, short base_plpmtu, Pointer sess_resume, long sess_resume_len, Pointer token,
			long token_sz) {
		return lsquic.lsquic_engine_connect(engine, version, local_sa, peer_sa, peer_ctx, conn_ctx, sni, base_plpmtu,
				sess_resume, sess_resume_len, token, token_sz);
	}
	
	/**
	 * synchronized wrapper for lsquic_stream_is_pushed
	 */
	public synchronized boolean lsquic_stream_is_pushed(Pointer stream) {
		return lsquic.lsquic_stream_is_pushed(stream) != 0;
	}
	
	/**
	 * synchronized wrapper for lsquic_conn_push_stream
	 */
	public synchronized int lsquic_conn_push_stream(Pointer conn, Pointer hdr_set, Pointer stream, lsquic_http_headers.ByReference headers) {
		return lsquic.lsquic_conn_push_stream(conn, hdr_set, stream, headers);
	}
	
	/**
	 * synchronized wrapper for lsquic_stream_send_headers
	 */
	public synchronized int stream_send_headers(Pointer stream, Pointer headers, int no_data) {
		return lsquic.lsquic_stream_send_headers(stream, headers, no_data);
	}
	
	/**
	 * synchronized wrapper for lsquic_stream_shutdown
	 */
	public synchronized int stream_shutdown(Pointer stream, int what) {
		return lsquic.lsquic_stream_shutdown(stream, what);
	}

	/**
	 * synchronized wrapper for lsquic_conn_get_sni
	 */
	public synchronized String get_sni(Pointer conn) {
		return lsquic.lsquic_conn_get_sni(conn);
	}
	
	/**
	 * synchronized wrapper for lsquic_conn_close
	 */
	public synchronized void conn_close(Pointer conn) {
		if(engine == null)
			return;
		lsquic.lsquic_conn_close(conn);
	}
	
	/**
	 * Synchronized wrapper for lsquic_engine_init_settings
	 */
	public synchronized void lsquic_engine_init_settings(lsquic_engine_settings.ByReference settings, int flags) {
		lsquic.lsquic_engine_init_settings(settings, flags);
	}

	/*
	 * Callbacks that have to be provided in the stream interface
	 */

	/**
	 * Opens new connection by setting conn_id and adds native connection
	 * @param context SSL
	 * @param connection used
	 * @return poinrter to user supplied connection context (id of connection)
	 */
	public synchronized Pointer on_new_conn(Pointer context, Pointer connection) {
		long connection_id = ++cur_conn;
		addConnection(connection, connection_id);
		return Pointer.createConstant(connection_id);
	}

	/**
	 * Closes connection by removing it
	 * @param connection to be closed
	 */
	public synchronized void connection_closed(Pointer connection) {
		long id = Pointer.nativeValue(lsquic.lsquic_conn_get_ctx(connection));
		removeConnection(id);
	}
	

	/**
	 * Opens new stream on connection
	 * @param context SSL
	 * @param stream pointer
	 * @return pointer to user supplied stream context (a c struct that can be found in the usercode library)
	 */
	public synchronized Pointer new_stream(Pointer context, Pointer stream) {
		Pointer conn = lsquic.lsquic_stream_conn(stream);
		long conn_id = Pointer.nativeValue(lsquic.lsquic_conn_get_ctx(conn));
		return findConnection(conn_id).new_stream(context, stream);
	}

	/**
	 * Closes stream on connection
	 * @param stream to be closed
	 * @param context SSL
	 */
	public synchronized void stream_close(Pointer stream, Pointer context) {
		Pointer conn = lsquic.lsquic_stream_conn(stream);
		long conn_id = Pointer.nativeValue(lsquic.lsquic_conn_get_ctx(conn));
		findConnection(conn_id).stream_close(stream, context);
		usercode.delete_stream_ctx(context);
	}

}
