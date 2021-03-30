package jquic.example;

import jquic.example.http.HttpClient;
import jquic.example.http.HttpHeaders;
import jquic.example.http.SimpleHttpMessage;

/**
 * Example using {@link HttpClient}
 */
public class HttpClientExample {

	/**
	 * instance of the HttpClient
	 * (make sure we have a reference so no native structs are freed :O)
	 */
	public static HttpClient client;
	
	/**
	 * starts the client, connects to localhost:12345 and sends a get request to /. Prints the response
	 */
	public static void start() {
		// Build and start client on default setting
		client = new HttpClient();
		client.start();
		client.connect("localhost", "localhost", 12345);

		// Build headers
		HttpHeaders headers = new HttpHeaders();

		// Fill headers with right values for request
		headers.put(":method", "GET");
		headers.put(":path", "/");
		headers.put(":scheme", "https");
		headers.put("user-agent", "jquic-http-test");

		// Make response and catch response from server
		SimpleHttpMessage request = new SimpleHttpMessage("GET / HTTP/1.1", headers, null);
		SimpleHttpMessage response = client.sendRequest(request);
		
		System.out.println(response.toString());
	}
}
