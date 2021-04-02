package test;

import static test.utils.Assertions.assertEquals;
import static test.utils.Assertions.assertHttp;
import static test.utils.HttpUtils.request;
import static test.utils.HttpUtils.response;

import java.util.AbstractMap.SimpleEntry;

import jquic.example.http.HttpClient;
import jquic.example.http.RequestLine;
import jquic.example.http.SimpleHttpMessage;
import jquic.example.http.annotated.AnnotatedHttpServer;
import jquic.example.http.annotated.Header;
import jquic.example.http.annotated.Parameter;
import jquic.example.http.annotated.Request;
import jquic.example.http.annotated.Response;
import jquic.example.http.annotated.SimpleAnnotatedProxy;
import logging.Logger;

/**
 * Tests for {@link jquic.example.http.annotated.AnnotatedHttpServer} and {@link jquic.example.http.annotated.SimpleAnnotatedProxy}.
 */
public class AnnotatedHttpProxyTests {

	public static final int REQ_TIMEOUT = System.getProperty("os.name").startsWith("Windows") ? 20000 : 1000;
	
	@Test("Dummy Test")
	public static void TestDummy() {
		// Logger
		final Logger logger = new Logger("Dummy Test");

		// this is a dummy test ;)
		logger.info("I am a dummy test %s", "\uD83E\uDD84");
	}

	@Test("Test Performance")
	public static void TestPerformance() {
		// Logger
		final Logger logger = new Logger("Modify Requests");

		// start server
		logger.info("Starting server");
		AnnotatedHttpServer server = new AnnotatedHttpServer();
		server.requestHandler.addObject(new TestServerRoutes());
		server.start(4000, "../testcert/cert.crt", "../testcert/key.key");

		// start proxy
		logger.info("Starting proxy");
		SimpleAnnotatedProxy proxy = new SimpleAnnotatedProxy();
		Object modifyRequests = new ModifyRequestsRoutes(4000);
		proxy.requestModifier.addObject(modifyRequests);
		proxy.responseModifier.addObject(modifyRequests);
		proxy.start(4001, "../testcert/cert.crt", "../testcert/key.key");

		// start client and test some responses
		logger.info("Starting client");
		

		// make 20 requests to 1MB resources and stop time
		long start = System.currentTimeMillis();
		
		logger.info("Connecting client");
		logger.info("Sending requests");
		for(int i = 0; i < 5; i++) {
			logger.info("%d / %d", i + 1, 5);
			HttpClient client = new HttpClient();
			client.start();
			client.connect("localhost", "localhost", 4001);
			client.sendRequest(request(null, "GET", "/benchmark", ""));
			client.close();
		}
		long end = System.currentTimeMillis();
		
		logger.info("50MB of data took %.2fs. (~%.2f Mbit/s.)", (end-start) / 1000D, 8 * 50D / ((end-start) / 1000D));
		
		// dispose of resources (stop threads etc.)
		server.stop();
		proxy.stop();
	}

	@Test("Let Through")
	public static void TestLetThrough() {
		// Logger
		final Logger logger = new Logger("Let Through");

		// start server
		logger.info("Starting server");
		AnnotatedHttpServer server = new AnnotatedHttpServer();
		server.requestHandler.addObject(new TestServerRoutes());
		server.start(4000, "../testcert/cert.crt", "../testcert/key.key");

		// start proxy
		logger.info("Starting proxy");
		SimpleAnnotatedProxy proxy = new SimpleAnnotatedProxy();
		Object letThrough = new LetThroughRoutes(4000);
		proxy.requestModifier.addObject(letThrough);
		proxy.responseModifier.addObject(letThrough);
		proxy.start(4001, "../testcert/cert.crt", "../testcert/key.key");

		// start client and test some responses
		logger.info("Starting client");
		HttpClient client = new HttpClient();
		client.start();
		client.connect("localhost", "localhost", 4001);

		logger.info("Testing 404");
		SimpleHttpMessage notFound = client.sendRequest(request(null, "GET", "/blah", "", "user-agent", "test-quic"), REQ_TIMEOUT);
		assertHttp(response(404, "not found", "content-type", "text-plain"), notFound, false);
		logger.info("OK");
		client.close();
		client = new HttpClient();
		client.start();
		client.connect("localhost", "localhost", 4001);
		logger.info("Testing repeat");
		SimpleHttpMessage request = request(null, "POST", "/repeat?count=5", "", "user-agent", "test-quic");
		SimpleHttpMessage repeat = client.sendRequest(request, REQ_TIMEOUT);
		assertHttp(response(200, "AAAAA", "content-type", "text-plain"), repeat, false);
		logger.info("OK");
		client.close();
		client = new HttpClient();
		client.start();
		client.connect("localhost", "localhost", 4001);
		logger.info("Testing coffee");
		SimpleHttpMessage coffee = client.sendRequest(request(null, "GET", "/coffee", "", "user-agent", "test-quic"), REQ_TIMEOUT);
		assertHttp(response(418, "V" + "|" + "." + "U", "X-Tea-Type", "Yorkshire Tea", "content-type", "tea"), coffee,
				false);
		logger.info("OK");
		client.close();
		client = new HttpClient();
		client.start();
		client.connect("localhost", "localhost", 4001);
		logger.info("Testing admin (1/2)");
		SimpleHttpMessage unauthorized = client
				.sendRequest(request(null, "GET", "/admin", "", "X-Password", "Password", "user-agent", "test-quic"), REQ_TIMEOUT);
		assertEquals("wrong status code", 401, unauthorized.getResponseLine().status_code);
		logger.info("OK");
		client.close();
		client = new HttpClient();
		client.start();
		client.connect("localhost", "localhost", 4001);
		logger.info("Testing admin (2/2)");
		SimpleHttpMessage authorized = client.sendRequest(
				request(null, "GET", "/admin", "", "X-Password", "super secret!", "user-agent", "test-quic"), REQ_TIMEOUT);
		assertEquals("wrong status code", 200, authorized.getResponseLine().status_code);
		logger.info("OK");

		// dispose of resources (stop threads etc.)
		client.close();
		server.stop();
		proxy.stop();
	}

	@Test("Modify Requests")
	public static void TestModifyRequests() {
		// Logger
		final Logger logger = new Logger("Modify Requests");

		// start server
		logger.info("Starting server");
		AnnotatedHttpServer server = new AnnotatedHttpServer();
		server.requestHandler.addObject(new TestServerRoutes());
		server.start(4000, "../testcert/cert.crt", "../testcert/key.key");

		// start proxy
		logger.info("Starting proxy");
		SimpleAnnotatedProxy proxy = new SimpleAnnotatedProxy();
		Object modifyRequests = new ModifyRequestsRoutes(4000);
		proxy.requestModifier.addObject(modifyRequests);
		proxy.responseModifier.addObject(modifyRequests);
		proxy.start(4001, "../testcert/cert.crt", "../testcert/key.key");

		// start client and test some responses
		logger.info("Starting client");
		HttpClient client = new HttpClient();
		client.start();
		client.connect("localhost", "localhost", 4001);
		logger.info("Testing repeat");
		SimpleHttpMessage request = request(null, "POST", "/repeat?count=5", "", "user-agent", "test-quic");
		SimpleHttpMessage repeat = client.sendRequest(request, REQ_TIMEOUT);
		assertHttp(response(200, "AAAAAAAAAA", "content-type", "text-plain"), repeat, false);
		logger.info("OK");
		client.close();
		client = new HttpClient();
		client.start();
		client.connect("localhost", "localhost", 4001);
		logger.info("Testing admin");
		SimpleHttpMessage unauthorized = client.sendRequest(
				request(null, "GET", "/admin", "", "X-Password", "super secret!", "user-agent", "test-quic"), REQ_TIMEOUT);
		assertEquals("wrong status code", 401, unauthorized.getResponseLine().status_code);
		logger.info("OK");
		
		logger.info("Testing tea");
		SimpleHttpMessage coffee = client.sendRequest(request(null, "GET", "/tea", "", "user-agent", "test-quic"), REQ_TIMEOUT);
		assertHttp(response(418, "V" + "|" + "." + "U", "X-Tea-Type", "Yorkshire Tea", "content-type", "tea"), coffee,
				false);
		logger.info("OK");

		

		// dispose of resources (stop threads etc.)
		client.close();
		server.stop();
		proxy.stop();
	}

	public static class LetThroughRoutes {

		public final int serverPort;

		public LetThroughRoutes(int serverPort) {
			this.serverPort = serverPort;
		}

		// Let msg trough
		@Request
		@Response
		public SimpleHttpMessage letThrough(SimpleHttpMessage message) {
			message.port = serverPort; /* ignored for responses anyways */
			return message;
		}

	}

	public static class ModifyRequestsRoutes {
		public final int serverPort;

		public ModifyRequestsRoutes(int serverPort) {
			this.serverPort = serverPort;
		}

		// default policy: let through
		@Request
		@Response
		public SimpleHttpMessage letThrough(SimpleHttpMessage message) {
			message.port = serverPort; /* ignored for responses anyways */
			return message;
		}

		// change path /tea to /coffee
		@Request(priority = 1, path = "/tea")
		public SimpleHttpMessage noTea(SimpleHttpMessage message) {
			message.port = serverPort;
			RequestLine line = message.getRequestLine();
			line.path = "/coffee";
			message.firstLine = line.toString();
			message.headers.put(":path", line.path);
			return message;
		}

		// double amount of count to /repeat
		@Request(priority = 1, method = "POST", path = "/repeat", parameters = { "count" })
		public SimpleHttpMessage doubleRepeat(SimpleHttpMessage request, @Parameter("count") int count) {
			request.port = serverPort;

			// this is way to tedious ... we need to re-do the RequestLine / ResponseLine
			// classes
			// such that they automatically update all affected values (also WITHIN the
			// request itself)
			RequestLine line = request.getRequestLine();
			line.queries.removeIf(x -> x.getKey().equals("count"));
			line.queries.add(new SimpleEntry<>("count", Integer.toString(count * 2)));
			line.query = line.queryString();
			request.firstLine = line.toString();
			request.headers.put(":path", line.path + "?" + line.query);
			return request;
		}

		// change password for /admin
		@Request(priority = 1, method = "GET", path = "/admin", headers = { "X-Password" })
		public SimpleHttpMessage changePassword(SimpleHttpMessage request) {
			request.port = serverPort;
			request.headers.remove("X-password");
			request.headers.put("X-Password", "Wrong Password!");
			return request;
		}
	}

	public static class TestServerRoutes {

		// 10 MB of 'A'
		public String benchmarkContent = "A".repeat(1024 * 1024 * 10);

		// Some requests
		@Request
		public SimpleHttpMessage notFound(SimpleHttpMessage request) {
			return response(404, "not found", "content-type", "text-plain");
		}

		@Request(priority = 1, method = "GET", path = "/coffee")
		public SimpleHttpMessage teapot(SimpleHttpMessage request) {
			return response(418, "V" + "|" + "." + "U", "X-Tea-Type", "Yorkshire Tea", "content-type", "tea");
		}

		@Request(priority = 1, method = "POST", path = "/repeat", parameters = { "count" })
		public SimpleHttpMessage repeate(SimpleHttpMessage request, @Parameter("count") int count) {

			if (count <= 0) {
				return response(500, ":(");
			}

			return response(200, "A".repeat(count), "content-type", "text-plain");
		}

		@Request(priority = 1, method = "GET", path = "/admin", headers = { "X-Password" })
		public SimpleHttpMessage admin(SimpleHttpMessage request, @Header("X-Password") String password) {
			if (password.equals("super secret!")) {
				return response(200, "Hello Admin :)", "content-type", "text-plain");
			} else {
				return response(401, "you are not admin!", "content-type", "text-plain");
			}
		}

		@Request(priority = 1, method = "GET", path = "/benchmark")
		public SimpleHttpMessage benchmark(SimpleHttpMessage request) {
			return response(200, benchmarkContent);
		}
	}

}
