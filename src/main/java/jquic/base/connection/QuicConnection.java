package jquic.base.connection;

import static usercodebindings.Constants.usercode;

import java.util.HashMap;
import java.util.Map;

import com.sun.jna.Pointer;

import jquic.base.engine.QuicEngine;
import jquic.base.stream.QuicStream;

/**
 * Wrapper class for lsquic_conn_t
 */
public class QuicConnection {

	/**
	 * associated {@link jquic.base.engine.QuicEngine engine}
	 */
	public final QuicEngine engine;
	
	/**
	 * id of next stream that will be opened
	 */
	private long cur_stream;
	
	/**
	 * mapping from stream id to stream
	 */
	private Map<Long, QuicStream> streams;
	
	/**
	 * native lsquic_conn_t
	 */
	private Pointer connection;
	
	/**
	 * set to true once the stream is closed
	 */
	private boolean closed = false;
	
	/**
	 * connection id (used in engine to differentiate connections)
	 */
	private long id;
	
	/**
	 * supplier for {@link jquic.base.stream.QuicStream streams}
	 */
	private StreamSupplier stream_supplier = QuicStream::new;
	
	/**
	 * Server name indication associated with this stream. <br>
	 * Only really used for http
	 */
	public String sni;
	
	/**
	 * Constructor
	 * @param id {@link #id}
	 * @param connection {@link #connection}
	 * @param engine {@link #engine}
	 */
	public QuicConnection(long id, Pointer connection, QuicEngine engine) {
		this.streams = new HashMap<>();
		this.id = id;
		this.connection = connection;
		this.engine = engine;
	}
	
	/**
	 * Setter for {@link #stream_supplier}
	 * @param stream_supplier {@link #stream_supplier}
	 */
	public void setStreamSupplier(StreamSupplier stream_supplier) {
		this.stream_supplier = stream_supplier;
	}
	
	/**
	 * Getter for {@link #id}
	 * @return {@link #id}
	 */
	public long getId() {
		return id;
	}
	
	/**
	 * Getter for {@link #connection}
	 * @return {@link #connection}
	 */
	public Pointer getNative() {
		return connection;
	}
	
	/**
	 * convenience method for {@link QuicEngine#make_stream(Pointer)}
	 */
	public void makeStream() {
		engine.make_stream(getNative());
	}
	
	/**
	 * wraps a new stream using {@link #stream_supplier} and adds it to {@link #streams}
	 * @param stream native lsquic_stream_t
	 * @return the wrapped stream
	 */
	public QuicStream addStream(Pointer stream) {
		long id = cur_stream ++;
		Pointer ctx = usercode.create_stream_ctx(id);
		QuicStream qstream = stream_supplier.apply(id, ctx, stream, this);
		this.streams.put(id, qstream);
		return qstream;
	}
	
	/**
	 * removes a stream by id
	 * @param id of the stream
	 * @return the removed stream (or null if no stream with given id exists)
	 */
	public QuicStream removeStream(long id) {
		return this.streams.remove(id);
	}
	
	/**
	 * finds a stream by id
	 * @param id of the stream
	 * @return the stream with matching id
	 * @throws RuntimeException if no such stream can be found
	 */
	public QuicStream findStream(long id) {
		QuicStream stream = streams.get(id);
		
		if(stream == null)
			throw new RuntimeException("No stream with Id " + id);
		
		return stream;
	}
	
	/*
	 * Callbacks
	 */
	
	/**
	 * implementation of {@link lsquicbindings.struct.lsquic_stream_if#on_new_stream lsquic_stream_if#on_new_stream}
	 * @param context user supplied context (connection id)
	 * @param stream native lsquic_stream_t
	 * @return stream context
	 */
	public final Pointer new_stream(Pointer context, Pointer stream) {
		QuicStream qstream = addStream(stream);
		qstream.onOpened();
		return qstream.context;
	}
	
	/**
	 * mplementation of {@link lsquicbindings.struct.lsquic_stream_if#on_close lsquic_stream_if#on_close}
	 * @param stream native lsquic_stream_t
	 * @param context user supplied context (stream id)
	 */
	public void stream_close(Pointer stream, Pointer context) {
		removeStream(context.getLong(0)).onClosed();
	}
	
	/**
	 * called when the connection is closed
	 */
	public void onClosed() {
		
	}

	/**
	 * convenience method for {@link jquic.base.engine.QuicEngine#conn_close(Pointer)}
	 */
	public void close() {
		if(closed)
			return;
		closed = true;
		
		engine.conn_close(getNative());
	}
	
}
