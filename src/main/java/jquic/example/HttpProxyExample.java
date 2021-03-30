package jquic.example;

import jquic.example.http.HttpClient;
import jquic.example.http.HttpProxy;
import jquic.example.http.SimpleHttpMessage;

/**
 * Example using {@link HttpProxy} <br>
 * TODO: The HttpProxy and this class don't make good use of the logger and are kind of weird in some parts 
 * @deprecated TODO: update to {@link jquic.example.http.SimpleProxy SimpleProxy}
 */
@Deprecated
public class HttpProxyExample {

	/**
	 * instance of the HttpProxy.
	 * (make sure we hold a reference for no UAF)
	 */
	public static HttpProxy proxy;

	/**
	 * starts a proxy on port 1234 redirecting requests to sni:12345. <br>
	 * Logs all requests + responses
	 */
	public static void start() {
		proxy = new HttpProxy() {
			
			@Override
			public HttpClient createClient(SimpleHttpMessage request, String sni) {
				HttpClient client = new HttpClient() {
					@Override
					public SimpleHttpMessage sendRequest(SimpleHttpMessage request) {
						System.out.println("PROXY -> SERVER");
						return super.sendRequest(request);
					}
				};
				client.start();
				client.connect("localhost", sni, 12345);
				return client;
			}
			
		};

		// Start on default setting
		proxy.start(1234, "testcert/cert.crt", "testcert/key.key");
	}
	
}
