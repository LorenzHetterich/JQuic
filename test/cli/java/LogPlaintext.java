import jquic.example.http.SimpleHttpMessage;
import jquic.example.http.annotated.*;

import java.io.PrintStream;

public class LogPlaintext {
	
	/**
	 * print stream that will actually write to a file
	 */
	public static PrintStream out;
	
	public static void main(String[] args) {
		
		// create proxy
		SimpleAnnotatedProxy proxy = new SimpleAnnotatedProxy();
		
		// add rules for requests (client -> proxy)
		proxy.requestModifier.addObject(new RequestRules());
		
		// add rules for responses (proxy -> client)
		proxy.responseModifier.addObject(new ResponseRules());
		
		System.out.println("starting proxy");
		
		// set output stream for logging
		try {
			out = new PrintStream("log.txt");
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		// start proxy on port 4000 with example certificate
		proxy.start(4000, "../testcert/cert.crt", "../testcert/key.key");
	}
	
	
	public static class RequestRules {
		
		/**
		 * This rule matches every request and has a priority of 0.
		 * @param request Request the client made to the proxy
		 * @return Request that will be sent to destination
		 */
		@Request
		public SimpleHttpMessage logeroni(SimpleHttpMessage request) {
			// print request to file
			LogPlaintext.out.println("[CLIENT -> PROXY]");
			LogPlaintext.out.println(request);
			
			// return request (--> will be sent to server without modification)
			return request;
		}
		
	}
	
	public static class ResponseRules {
		
		/**
		 * This rule matches every response and has a priority of 0.
		 * @param response Response the server sent to the proxy
		 * @return Response that will be sent to client
		 */
		@Response
		public SimpleHttpMessage logeroni(SimpleHttpMessage response) {
			// print response to file
			LogPlaintext.out.println("[PROXY -> CLIENT]");
			LogPlaintext.out.println(response);
			
			// return response (--> will be sent to client without modification)
			return response;
		}
	}
	
}