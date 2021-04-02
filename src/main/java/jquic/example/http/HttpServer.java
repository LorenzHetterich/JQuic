package jquic.example.http;

import static lsquicbindings.Constants.LSENG_HTTP;
import static lsquicbindings.Constants.LSENG_SERVER;

import com.sun.jna.Pointer;

import boringsslbindings.BoringSslHelper;
import jquic.base.DatagramServer;
import jquic.base.PacketHandler;
import jquic.base.connection.QuicConnection;
import jquic.base.engine.QuicEngine;
import jquic.base.stream.QuicStream;

/**
 * Simple http server. <br>
 * Overwrite {@link #onRequest(QuicStream, SimpleHttpMessage)} to handle incoming requests
 */
public class HttpServer {
	
	/**
	 * Used Datagram Server
	 */
	private DatagramServer server;
	
	/**
	 * Package handler
	 */
	private PacketHandler handler;
	
	/**
	 * QUIC engine
	 */
	private QuicEngine engine;
	
	/**
	 * Pointer to SSL context
	 */
	private Pointer sslContext;
	
	/**
	 * Server status
	 */
	private boolean running;
	
	/**
	 * Boring SSLHelper for request handling
	 */
	private BoringSslHelper ssl;

	/**
	 * Handles and parses HTTP message
	 * @param stream to listen on
	 */
	public void handleRequest(QuicStream stream) {
		try {
			onRequest(stream, SimpleHttpMessage.parse(stream));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Getter
	 * @return engine
	 */
	public QuicEngine getEngine() {
		return engine;
	}

	/**
	 * Prepare HTTP header for response
	 * @param stream to write to
	 * @param request to be send
	 */
	public void onRequest(QuicStream stream, SimpleHttpMessage request) {
		HttpHeaders headers = new HttpHeaders();
		// Put HHTP infos to header
		headers.put(":status", "200");
		headers.put("content-type", "text/plain");
		headers.put("content-length", "6");
		// Send response
		sendResponse(stream, new SimpleHttpMessage("HTTP 1.1/200 OK", headers, "Hello\n".getBytes()));
	}

	/**
	 * Send response
	 * @param stream to send trough
	 * @param response to be send
	 */
	public void sendResponse(QuicStream stream, SimpleHttpMessage response) {
		// Send headers
		stream.sendHeaders(response.headers);
		// Check if response is not empty and write to stream
		if(response.hasData()) {
			stream.out.write(response.data);
		}
		// Shutdown Stream
		engine.stream_shutdown(stream.getNative(), 1);
	}

	/**
	 * Start HTTP Server
	 * @param alpn alpn to use
	 * @param port to be used
	 * @param certPath path to certificate for SSL
	 * @param keyPath path to key for SSL
	 */
	public void start(String alpn, int port, String certPath, String keyPath) {
		// Check status
		if (running) {
			throw new RuntimeException("Server is already running!");
		}

		running = true;
		// Create new Datagram Server
		this.server = new DatagramServer(port);
		// Create BoringSSLHelper, SSL and Certificate
		ssl = new BoringSslHelper();
		ssl.addAlpn(alpn);
		this.sslContext = ssl.createContext();
		ssl.setupCertivicatePEM(this.sslContext, certPath, keyPath);

		// Handler for package
		this.handler = new PacketHandler(server);

		// Create QUIC engine with given SSL context and certificate
		engine = new QuicEngine(LSENG_SERVER | LSENG_HTTP, null, 
		a -> {
			a.ea_lookup_cert = this::lookup_cert;
		},
		handler::packets_out, this::getSslContext);

		// Create Connection
		engine.setConnectionSupplier(HttpServerConnection::new);

		// Handler for engine
		handler.setEngine(engine);
		new Thread(handler).start();
	
	}
	
	/**
	 * Start HTTP Server
	 * @param port to be used
	 * @param certPath path to certificate for SSL
	 * @param keyPath path to key for SSL
	 */
	public void start(int port, String certPath, String keyPath) {
		// TODO: don't hardcode the version here :D
		start("h3-34", port, certPath, keyPath);
	}

	/**
	 * verify certificate in Lib
	 * @param a
	 * @param b
	 * @param c
	 * @return
	 */
	public Pointer lookup_cert(Pointer a, Pointer b, Pointer c) {
		return sslContext;
	}

	/**
	 * Getter for SSL context
	 * @param a
	 * @param b
	 * @return SSL context
	 */
	public Pointer getSslContext(Pointer a, Pointer b) {
		return sslContext;
	}

	/**
	 * Quic connection used by the {@link HttpServer}
	 */
	public class HttpServerConnection extends QuicConnection {
		
		/**
		 * Constructor for HTTP Server connection
		 * @param id of connection
		 * @param connection used
		 * @param engine used
		 */
		public HttpServerConnection(long id, Pointer connection, QuicEngine engine) {
			super(id, connection, engine);
			this.setStreamSupplier(HttpServerStream::new);
		}

	}

	/**
	 * Quic stream used by the {@link HttpProxy}. <br>
	 * Starts a thread to read incoming request.
	 */
	public class HttpServerStream extends QuicStream {
		
		/**
		 * Constructot for Stream
		 * @param id of connection
		 * @param context pointer to native context
		 * @param stream used
		 * @param connection used
		 */
		public HttpServerStream(long id, Pointer context, Pointer stream, QuicConnection connection) {
			super(id, context, stream, connection);
			new Thread(() -> handleRequest(this)).start();
		}

	}

	/**
	 * Getter for SSL Context
	 * @return SSL Context
	 */
	public Pointer getSSLCtx() {
		return sslContext;
	}

	/**
	 * Stop handler
	 */
	public void stop() {
		handler.stop();
		try {
			server.close();
		} catch (Exception e) {
		}
	}

}
