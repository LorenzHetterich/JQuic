package jquic.example.http;

import jquic.base.stream.QuicStream;

/**
 * Simple Http proxy (or rather server actually). <br>
 * Use together with {@link HttpClient} for a "real" proxy. <br>
 * Overwrite {@link #getResponse(SimpleHttpMessage)} with whatever you need :)
 */
public abstract class SimpleProxy {
	
	/**
	 * Constructor for HTTP server
	 * Proxy is mirror if methods called on request doesn't change something
	 */
	protected HttpServer server = new HttpServer() {
		
		@Override
		public void onRequest(QuicStream stream, SimpleHttpMessage request) {
			request.sni = stream.connection.sni;
			RequestLine req = request.getRequestLine();
			request.headers.putFirst(":scheme", "https");
			request.headers.putFirst(":path", req.path + (req.query.isEmpty() ? "" : "?" + req.query));
			request.headers.putFirst(":method", req.method);
			this.sendResponse(stream, SimpleProxy.this.getResponse(request));
		}
		
	};

	/**
	 * Start server
	 * @param port used
	 * @param cert used
	 * @param key used
	 */
	public void start(int port, String cert, String key) {
		server.start(port, cert, key);
	}
	
	/**
	 * Start server
	 * @param version quic version to use
	 * @param port used
	 * @param cert used
	 * @param key used
	 */
	public void start(String version, int port, String cert, String key) {
		server.start(version, port, cert, key);
	}

	/**
	 * Stop server
	 */
	public void stop() {
		server.stop();
	}
	
	/**
	 * Called whenever a client sends a request
	 * @param request The client send
	 * @return response to send to client
	 */
	public abstract SimpleHttpMessage getResponse(SimpleHttpMessage request);
	
}
