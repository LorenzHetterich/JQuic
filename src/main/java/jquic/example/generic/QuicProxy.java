package jquic.example.generic;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.sun.jna.Pointer;

import jquic.base.connection.QuicConnection;
import jquic.base.engine.QuicEngine;
import jquic.base.stream.QuicStream;
import jquic.example.generic.QuicProxy.ClientConnection.ClientStream;
import jquic.example.generic.QuicProxy.ServerConnection.ServerStream;
import logging.Logger;

/**
 * Generic quic proxy. <br>
 * For every client - proxy connection or stream, a proxy - server connection (or stream) is opened and associated with it. <br>
 * Once either of them dies, their associated connection / stream is closed. <br>
 * To modify data flowing through the proxy, overwrite {@link #onData(ClientStream, ServerStream, byte[], int)} and {@link #onData(ServerStream, ClientStream, byte[], int)}
 */
public class QuicProxy {
	
	/**
	 * quic client
	 */
	protected QuicClient client;
	
	/**
	 * quic server
	 */
	protected QuicServer server;

	/**
	 * default port to forward requests to
	 */
	public int destPort = 443;
	
	/**
	 * logger used for more pretty output
	 */
	protected Logger logger = new Logger(this);

	/**
	 * Constructor
	 */
	public QuicProxy() {
		client = new QuicClient();
		server = new QuicServer();
	}


	/**
	 * Start proxy
	 * @param port server
	 * @param alpn
	 * @param cert SSL certificate
	 * @param key SSL key
	 */
	public void start(int port, String alpn, String cert, String key) {
		client.start(-1, alpn, ServerConnection::new);
		server.start(port, cert, key, alpn, ClientConnection::new);
	}

	/**
	 * @param connection new conn
	 */
	public void onNewConnection(ClientConnection connection) {
		logger.debug("new connection!");
	}

	/**
	 * Handle new stream
	 * @param stream of client
	 */
	public void onNewStream(ClientStream stream) {
		logger.debug("new stream pair established!");
		new Thread(stream::readThread).start();
		new Thread(stream.proxy2server::readThread).start();
	}

	/**
	 * No support for Seerver push yet
	 * @param stream to be pushed
	 */
	public void onPushedStream(ServerStream stream) {
		
	}

	/**
	 * Write to stream from server to proxy
	 * @param a in stream
	 * @param b out stream
	 * @param data to be written
	 * @param amount of data to be written
	 */
	public void onData(ServerStream a, ClientStream b, byte[] data, int amount) {
		logger.debug("server -> proxy");
		if(amount > 0) {
			b.out.write(data, 0, amount);
		}
		if(a.closed) {
			b.close();
			if(((ServerConnection)a.connection).streams.isEmpty()) {
				a.connection.close();
			}
		}
	}
	/**
	 * Write to stream from client to proxy
	 * @param a in stream
	 * @param b out stream
	 * @param data to be written
	 * @param amount of data to be written
	 */
	public void onData(ClientStream a, ServerStream b, byte[] data, int amount) {
		logger.debug("client -> proxy");
		if(amount > 0) {
			b.out.write(data, 0, amount);
		}
		if(a.closed) {
			b.close();
			if(((ClientConnection)a.connection).streams.isEmpty()) {
				a.connection.close();
			}
		}
	}

	/**
	 * Client connection of proxy
	 */
	public class ClientConnection extends QuicConnection {

		/**
		 * associated connection 
		 */
		public final ServerConnection proxy2server;
		
		/**
		 * streams
		 */
		protected Set<ClientStream> streams = Collections.synchronizedSet(new HashSet<>());

		/**
		 * Constructor - set up connections and streams
		 * @param id of conn
		 * @param connection pointer
		 * @param engine used
		 */
		public ClientConnection(long id, Pointer connection, QuicEngine engine) {
			super(id, connection, engine);
			proxy2server = (ServerConnection) client.connect(engine.get_sni(connection), destPort);
			proxy2server.proxy2client = this;
			onNewConnection(this);
			this.setStreamSupplier(ClientStream::new);
		}

		/**
		 * Synchronized list of client streams used in ClientStream
		 */
		public List<ClientStream> waitingForPartner = Collections.synchronizedList(new LinkedList<>());

		/**
		 * Client streams
		 */
		public class ClientStream extends QuicStream {

			/**
			 * associated stream
			 */
			public ServerStream proxy2server;
			
			/**
			 * true once the stream is closed
			 */
			public boolean closed;
			
			/**
			 * Constructor
			 * @param id of conn
			 * @param context pointer to native context
			 * @param stream pointer
			 * @param connection used
			 */
			public ClientStream(long id, Pointer context, Pointer stream, QuicConnection connection) {
				super(id, context, stream, connection);
				streams.add(this);
				if(engine.lsquic_stream_is_pushed(stream)) {
					throw new RuntimeException("Server push not yet implemented!");
				} else {
					waitingForPartner.add(this);
					ClientConnection.this.proxy2server.makeStream();
				}
			}

			/**
			 * On thread based read
			 */
			public void readThread() {
				byte[] buffer = new byte[1024];
				while(!closed) {
					int amount = in.read(buffer);
					if(amount == -1) {
						// stream closed
						closed = true;
					}
					onData(this, proxy2server, buffer, amount);
				}
			}

			/**
			 * Close stream
			 */
			@Override
			public void onClosed() {
				super.onClosed();
				streams.remove(this);
				closed = true;
			}
		}
		
	}

	/**
	 * Server Connection of proxy
	 */
	public class ServerConnection extends QuicConnection {

		/**
		 * associated connection
		 */
		public ClientConnection proxy2client;
		
		/**
		 * set of streams
		 */
		protected Set<ServerStream> streams = Collections.synchronizedSet(new HashSet<>());

		/**
		 * Constructor - sets up stream supplier
		 * @param id of conn
		 * @param connection pointer
		 * @param engine used
		 */
		public ServerConnection(long id, Pointer connection, QuicEngine engine) {
			super(id, connection, engine);	
			this.setStreamSupplier(ServerStream::new);
		}

		/**
		 * Server streams
		 */
		public class ServerStream extends QuicStream {

			/**
			 * true once the stream is closed
			 */
			public boolean closed = false;
			
			/**
			 * associated stream
			 */
			public ClientStream proxy2client;

			/**
			 * Constructor - sets up connections and streams
			 * @param id of conn
		     * @param context pointer to native context
			 * @param stream pointer
			 * @param connection used
			 */
			public ServerStream(long id, Pointer context, Pointer stream, QuicConnection connection) {
				super(id, context, stream, connection);
				streams.add(this);
				if(engine.lsquic_stream_is_pushed(stream)) {					
					throw new RuntimeException("Server push is not yet supported!");
				} else {

					// stream was made because server pushed a stream, so we link a pushed server stream to this one
					ClientStream str = ServerConnection.this.proxy2client.waitingForPartner.remove(0);
					
					this.proxy2client = str;
					str.proxy2server = this;
					
					onNewStream(str);
				}
			}

			/**
			 * On thread based read
			 */
			public void readThread() {
				byte[] buffer = new byte[1024];
				while(!closed) {
					int amount = in.read(buffer);
					if(amount == -1) {
						// stream closed
						closed = true;
					}
					onData(this, proxy2client, buffer, amount);
				}
			}

			/**
			 * Close stream
			 */
			@Override
			public void onClosed() {
				super.onClosed();
				closed = true;
				streams.remove(this);
			}
			
		}
		
	}

	/**
	 * Stop proxy
	 */
	public void stop() {
		client.stop();
		server.stop();
	}
	
}
