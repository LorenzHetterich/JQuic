package jquic.example.http.annotated;

import static lsquicbindings.Constants.LSENG_HTTP;
import static lsquicbindings.Constants.LSENG_SERVER;

import com.sun.jna.Pointer;

import boringsslbindings.BoringSslHelper;
import jquic.base.DatagramServer;
import jquic.base.PacketHandler;
import jquic.base.connection.QuicConnection;
import jquic.base.engine.QuicEngine;
import jquic.base.stream.QuicStream;
import jquic.example.http.HttpHeaders;
import jquic.example.http.SimpleHttpMessage;
import lsquicbindings.struct.lsquic_http_headers;

/**
 * Http server that calls methods to handle requests based on annotations
 */
public class AnnotatedHttpServer {

	/**
	 * handles UDP stuff
	 */
	private DatagramServer server;
	
	/**
	 * handles UDP stuff
	 */
	private PacketHandler handler;
	
	/**
	 * Quic engine used
	 */
	public QuicEngine engine;
	
	/**
	 * pointer to native ssl context struct
	 */
	private Pointer sslContext;
	
	/**
	 * true while the server is running
	 */
	private boolean running;
	
	/**
	 * used for easy ssl context construction etc.
	 */
	private BoringSslHelper ssl;
	
	/**
	 * used to handle http requests
	 */
	public RequestHandler requestHandler;
	
	/**
	 * sent to client if {@link #requestHandler} cannot handle a request
	 */
	public SimpleHttpMessage serverError;

	/**
	 * Constructor. <br>
	 * Initializes {@link #serverError}
	 */
	public AnnotatedHttpServer() {
		requestHandler = new RequestHandler();
		HttpHeaders headers = new HttpHeaders();
		headers.put(":status", "500");
		headers.put("content-type", "text/plain");
		headers.put("content-length", "15");
		serverError = new SimpleHttpMessage("HTTP 1.1/500 Internal Server Error", headers, "no route found!".getBytes());
	}

	/**
	 * Handle Request by parsing it
	 * @param stream be read from
	 */
	public void handleRequest(QuicStream stream) {
		try {
			onRequest(stream, SimpleHttpMessage.parse(stream));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Getter for engine
	 * @return used engine
	 */
	public QuicEngine getEngine() {
		return engine;
	}

	/**
	 * Send response with server error message
	 * @param stream used for sending
	 * @param request to be answered
	 */
	public void onRequest(QuicStream stream, SimpleHttpMessage request) {
		SimpleHttpMessage response = requestHandler.handle(request, serverError);
		
		sendResponse(stream, response);
	}

	/**
	 * Respond to request
	 * @param stream for sending
	 * @param response HttpMessage
	 */
	public void sendResponse(QuicStream stream, SimpleHttpMessage response) {
		
		lsquic_http_headers.ByReference hdrs = response.headers.toNative();

		engine.stream_send_headers(stream.getNative(), hdrs.getPointer(), 0);
		if(response.hasData()) {
			stream.out.write(response.data);
		}
		engine.stream_shutdown(stream.getNative(), 1);
	}

	/**
	 * Start server <br>
	 * TODO: Don't hardcode the version :/
	 * @param port used
	 * @param certPath used
	 * @param keyPath used
	 */
	public void start(int port, String certPath, String keyPath) {
		start("h3-34", port, certPath, keyPath);
	}
	
	/**
	 * Start server
	 * @param alpn alpn to use
	 * @param port used
	 * @param certPath used
	 * @param keyPath used
	 */
	public void start(String alpn, int port, String certPath, String keyPath) {
		if (running) {
			throw new RuntimeException("Server is already running!");
		}
		// Start Datagram server
		running = true;
		this.server = new DatagramServer(port);

		// Do SSL stuff
		ssl = new BoringSslHelper();
		ssl.addAlpn(alpn);
		this.sslContext = ssl.createContext();
		ssl.setupCertivicatePEM(this.sslContext, certPath, keyPath);

		// Set handler
		this.handler = new PacketHandler(server);

		engine = new QuicEngine(LSENG_SERVER | LSENG_HTTP, null, 
		a -> {
			a.ea_lookup_cert = this::lookup_cert;
		},
		handler::packets_out, this::getSslContext);

		// Connect engine
		engine.setConnectionSupplier(HttpServerConnection::new);

		handler.setEngine(engine);
		new Thread(handler).start();

	}

	/**
	 * Check cert
	 * @return SSL context
	 */
	public Pointer lookup_cert(Pointer a, Pointer b, Pointer c) {
		return sslContext;
	}

	/**
	 * Getter for SSL context
	 * @return Context
	 */
	public Pointer getSslContext(Pointer a, Pointer b) {
		return sslContext;
	}

	/**
	 * Quic connection used by {@link AnnotatedHttpServer}
	 */
	public class HttpServerConnection extends QuicConnection {
		
		/**
		 * Build server connection
		 * @param id of connection
		 * @param connection provided
		 * @param engine for QUIC
		 */
		public HttpServerConnection(long id, Pointer connection, QuicEngine engine) {
			super(id, connection, engine);
			this.setStreamSupplier(HttpServerStream::new);
		}

	}

	/**
	 * Quic stream used by {@link AnnotatedHttpServer}
	 */
	public class HttpServerStream extends QuicStream {
		
		/**
		 * Open Streams on thread base
		 * @param id of conn
		 * @param context pointer to native context
		 * @param stream pointer
		 * @param connection used
		 */
		public HttpServerStream(long id, Pointer context, Pointer stream, QuicConnection connection) {
			super(id, context, stream, connection);
			new Thread(() -> handleRequest(this)).start();
		}

	}

	/**
	 * Stop server
	 */
	public void stop() {
		handler.stop();
		try {
			server.close();
		} catch (Exception e) {
		}
	}

	/**
	 * Getter for {@link #sslContext}
	 * @return {@link #sslContext}
	 */
	public Pointer getSSLCtx() {
		return sslContext;
	}
	
}
