package jquic.example;

import jquic.example.http.HttpServer;

/**
 * Example using {@link HttpServer}
 */
public class SimpleHttpServerExample {

	/**
	 * instance of HttpServer
	 * (so nothing native gets destroyed)
	 */
	public static HttpServer server;


	/**
	 * Start HttpServer on port 1234 using certificate and key from directory testcert
	 */
	public static void start() {
		server = new HttpServer();
		server.start(1234, "testcert/cert.crt", "testcert/key.key");
	}
	
}
