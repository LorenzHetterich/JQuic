package jquic.example;

import jquic.example.http.SimpleHttpMessage;
import jquic.example.http.annotated.Request;
import jquic.example.http.annotated.Response;
import jquic.example.http.annotated.SimpleAnnotatedProxy;

/**
 * Example using {@link SimpleAnnotatedProxy}
 */
public class SimpleAnnotatedProxyExample {

	/**
	 * Instance of SimpleAnnotatedProxy
	 * (make sure we have a reference so no native stuff is freed)
	 */
	public static SimpleAnnotatedProxy proxy = new SimpleAnnotatedProxy();

	/**
	 * start proxy on port 4000 using certificate and key from directory testcert. <br>
	 * uses {@link RequestModifier} to modify proxy --> client requests before sending them to the server and <br>
	 * {@link ResponseModifier} to modify server --> proxy requests before sending them to the client.
	 */
	public static void start() {
		proxy.requestModifier.addObject(new RequestModifier());
		proxy.responseModifier.addObject(new ResponseModifier());
		proxy.start(4000, "testcert/cert.crt", "testcert/key.key");
	}
	
	/**
	 * Class to modify requests using annotated methods
	 */
	public static class RequestModifier {
		
		/**
		 * Default: let through
		 * @param request client --> proxy request
		 * @return proxy --> server request
		 */
		@Request
		public static SimpleHttpMessage letThrough(SimpleHttpMessage request) {
			return request;
		}

		/**
		 * Replace /admin in path with /nope
		 * @param request client --> proxy request
		 * @return proxy --> server request
		 */
		@Request(method = "GET", path = "/admin", priority = 1)
		public static SimpleHttpMessage modifyAdmin(SimpleHttpMessage request) {
			// TODO: does this really still work like this with the new requestLine implementation?
			request.firstLine = request.firstLine.replace("/admin", "/nope");
			return request;
		}
		
	}
	
	/**
	 * Class to modify responses using annotated methods
	 */
	public static class ResponseModifier {
		
		/**
		 * Default: let through
		 * @param response server --> proxy response
		 * @return proxy --> client response
		 */
		@Response
		public static SimpleHttpMessage letThrough(SimpleHttpMessage response) {
			return response;
		}

		/**
		 * Change status code 404 to 418
		 * @param response server --> proxy response
		 * @return proxy --> client response
		 */
		@Response(status_code="404", priority = 1)
		public static SimpleHttpMessage modifyAdmin(SimpleHttpMessage response) {
			// TODO: does this really still work like this with the new responseLine implementation?
			response.getResponseLine().setStatus(418);
			response.firstLine = response.getResponseLine().toString();
			return response;
		}
		
	}
	
}
