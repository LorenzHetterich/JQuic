package jquic.example;

import jquic.example.echo.EchoClient;

/**
 * example using the {@link EchoClient}
 */
public class EchoClientExample {

	/**
	 * instance of the echo client
	 * (make sure we have a reference so no native structs are cleared :O)
	 */
	public static EchoClient client;

	/**
	 * starts echo client and connects to localhost:12345 
	 */
	public static void start() {
		client = new EchoClient();
		client.start(-1, "localhost", 12345);
	}
	
}
