import jquic.example.http.SimpleHttpMessage;
import jquic.example.http.annotated.*;

import test.utils.HttpUtils;

import java.io.PrintStream;

public class HttpServer {
	
	public static void main(String[] args) {
		
		// create server
		AnnotatedHttpServer server = new AnnotatedHttpServer();
		
		// add rules for requests (client -> server)
		server.requestHandler.addObject(new RequestRules());
		
		System.out.println("starting server");
	
		// start server on port 4000 with example certificate and quic draft h3-29
		server.start("h3-29", 4000, "../testcert/cert.crt", "../testcert/key.key");
	}
	
	
	public static class RequestRules {
		
		/**
		 * default 404
		 */
		@Request
		public SimpleHttpMessage notFound() {
			return HttpUtils.response(404, "not found", "user-agent", "jquic");
		}
		
		/**
		 * send status code
		 */
		@Request(priority = 1, path = "/status/([0-9]+)")
		public SimpleHttpMessage echoStatus(@Path(1) int status_code) {
			return HttpUtils.response(status_code, "status echo :O", "user-agent", "jquic");
		}
		
		/**
		 * echo data
		 */
		@Request(priority = 1, path = "/echo")
		public SimpleHttpMessage echoData(SimpleHttpMessage request){
			return HttpUtils.response(200, request.data, "user-agent", "jquic");
		}
		
		// feel free to add a few more :)
		
	}
	
}