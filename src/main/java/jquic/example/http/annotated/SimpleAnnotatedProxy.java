package jquic.example.http.annotated;

import jquic.example.http.HttpClient;
import jquic.example.http.HttpHeaders;
import jquic.example.http.SimpleHttpMessage;
import jquic.example.http.SimpleProxy;

/**
 * proxy that invokes (possibly) modifying methods for requests and responses based on annotations
 */
public class SimpleAnnotatedProxy extends SimpleProxy{

	// Handler for request and response
	/**
	 * handles annotation magic for requests
	 */
	public RequestHandler requestModifier = new RequestHandler();
	
	/**
	 * handles annotation magic for responses
	 */
	public ResponseHandler responseModifier = new ResponseHandler();

	/**
	 * Get response for request. <br>
	 * TODO: use logger
	 * @param request send
	 * @return
	 */
	@Override
	public SimpleHttpMessage getResponse(SimpleHttpMessage request) {
		request = requestModifier.handle(request);

		// Start HTTP client
		HttpClient client = new HttpClient();
		client.start();
		client.connect(request.sni, request.sni, request.port);

		// Send request over client
		SimpleHttpMessage response = client.sendRequest(request);
		client.close();

		// Check if server answers
		if (response == null) {
			System.err.println("[Proxy]: Did not get a response from server!");
			
			HttpHeaders headers = new HttpHeaders();
			
			headers.put(":status", "444");
			
			response = new SimpleHttpMessage("HTTP/1.1 444 No Response", headers, null);
		}

		// Handle response from server
		response = responseModifier.handle(response);

		// Build header with correct status code
		response.headers.putFirst(":status", Integer.toString(response.getResponseLine().status_code));
		return response;
	}
	
}
