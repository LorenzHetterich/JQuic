package jquic.example;

import jquic.example.echo.EchoServer;

/**
 * Example using {@link EchoServer}
 */
public class EchoServerExample {

	/**
	 * instance of the echo server
	 * (make sure we have a reference so no native structs are cleared :O)
	 */
	public static EchoServer server;

	/**
	 * starts the echo server on port 12345 using certificate and key from the testcert folder
	 */
	public static void start() {
		server = new EchoServer();
		server.start(12345, "testcert/cert.crt", "testcert/key.key");
	}
	
}
