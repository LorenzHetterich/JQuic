package jquic.example;

import java.util.Arrays;
import java.util.Map;

import jquic.example.http.HttpHeaders;
import jquic.example.http.SimpleHttpMessage;
import jquic.example.http.annotated.AnnotatedHttpServer;
import jquic.example.http.annotated.Header;
import jquic.example.http.annotated.Path;
import jquic.example.http.annotated.Request;

public class AnnotatedHttpServerExample {

	// Example Server
	public static AnnotatedHttpServer server;

	// Start server on default setting
	public static void start() {
		server = new AnnotatedHttpServer();
		server.requestHandler.addObject(new Routes());
		server.start(4000, "testcert/cert.crt", "testcert/key.key");
	}

	// Build routes
	public static class Routes {
		
		private Map<Integer, String> status_codes = Map.of(200, "OK", 404, "Not Found", 403, "Forbidden");
		
		public SimpleHttpMessage makeResponse(int status_code, String body, String... headers) {
			
			HttpHeaders h = new HttpHeaders();
			
			h.put(":status", Integer.toString(status_code));
			h.put("content-type", "text/plain");
			h.put("content-length", Integer.toString(body.length()));
			
			return new SimpleHttpMessage(String.format("HTTP/1.1 %d %s", status_code, status_codes.get(status_code)), h, body.getBytes());
		}

		// Default answer is 404
		@Request
		public SimpleHttpMessage onAny() {
			return makeResponse(404, "NOT FOUND!\n");
		}

		// Get params, prio must be 1 else default 0 is used and msg can't be processed
		@Request(method = "GET", path="/params", priority = 1)
		public SimpleHttpMessage onParams(SimpleHttpMessage request){
			return makeResponse(200, Arrays.toString(request.getRequestLine().queries.toArray()) + "\n");
		}

		// Ping request
		@Request(method = "GET", path="/ping", priority = 1)
		public SimpleHttpMessage onPing() {
			return makeResponse(200, "pong!\n");
		}

		// Greet user
		@Request(method = "GET", path = "/user/([0-9]+)", priority = 1)
		public SimpleHttpMessage onGetUser(@Path(1) int id) {
			return makeResponse(200, "Hello user " + id + "\n");
		}

		// Greet admin
		@Request(headers = {"X-Very-Secret-Token:1234[25][46]"}, priority = 100)
		public SimpleHttpMessage adminPanel() {
			return makeResponse(200, "Hello admin :)\n");
		}

		// Admin uses wrong secret token - reply with 403 access forbidden
		@Request(headers = {"X-Very-Secret-Token"}, priority = 99)
		public SimpleHttpMessage wrongToken(@Header("X-Very-Secret-Token") String token) {
			return makeResponse(403, "Wrong token:" + token + "\n");
		}
		
	}
	
}
