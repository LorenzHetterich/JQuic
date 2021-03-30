package jquic.main.providers.json;

import jquic.example.http.HttpClient;
import jquic.example.http.HttpHeaders;
import jquic.example.http.SimpleHttpMessage;
import jquic.example.http.SimpleProxy;

public class SimpleJsonProxy extends SimpleProxy{

	// Modifier for request and response
	public JsonRequestModifier requestModifier;
	public JsonResponseModifier responseModifier;
	
	public int defaultServerPort = 443;

	/**
	 * Constructor
	 */
	public SimpleJsonProxy() {
		requestModifier = new JsonRequestModifier();
		responseModifier = new JsonResponseModifier();
	}

	/**
	 * Get response to a request, can be modified at will with Modifiers
	 * @param request to be send
	 * @return response
	 */
	@Override
	public SimpleHttpMessage getResponse(SimpleHttpMessage request) {
		request.port = defaultServerPort;
		// Modifier call for eventual changes as specified in json
		request = requestModifier.handle(request);

		// Start client
		HttpClient client = new HttpClient();
		client.start();
		client.connect(request.sni, request.sni, request.port);

		// Get response
		SimpleHttpMessage response = client.sendRequest(request);
		client.close();

		// Handle empty response
		if (response == null) {
			System.err.println("[Proxy]: Did not get a response from server!");
			
			HttpHeaders headers = new HttpHeaders();
			
			headers.put(":status", "444");
			
			response = new SimpleHttpMessage("HTTP/1.1 444 No Response", headers, null);
		}

		// Modifier call for eventual changes specified in json
		response = responseModifier.handle(response);

		// Complete headers
		response.headers.putFirst(":status", Integer.toString(response.getResponseLine().status_code));
		return response;
	}
	
}
