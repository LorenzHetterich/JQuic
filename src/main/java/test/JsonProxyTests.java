package test;

import static test.utils.Assertions.assertEquals;
import static test.utils.Assertions.assertHttp;
import static test.utils.HttpUtils.request;
import static test.utils.HttpUtils.response;

import com.google.gson.Gson;

import jquic.example.http.HttpClient;
import jquic.example.http.SimpleHttpMessage;
import jquic.example.http.annotated.AnnotatedHttpServer;
import jquic.example.http.annotated.Header;
import jquic.example.http.annotated.Parameter;
import jquic.example.http.annotated.Request;
import jquic.main.providers.json.Replacement;
import jquic.main.providers.json.ReplacementAdapter;
import jquic.main.providers.json.Rules;
import jquic.main.providers.json.SimpleJsonProxy;
import logging.Logger;
import test.resources.Resources;

/**
 * Contains tests for {@link jquic.main.providers.json.SimpleJsonProxy}
 */
public class JsonProxyTests {

	public static final int REQ_TIMEOUT = System.getProperty("os.name").startsWith("Windows") ? 20000 : 1000;

	public static String doNothing = Resources.loadString("doNothing.json");
	public static String modifyRequests = Resources.loadString("modifyRequests.json");

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
		SimpleJsonProxy proxy = new SimpleJsonProxy();
		proxy.defaultServerPort = 4000;
		Gson gson = new Gson().newBuilder().registerTypeAdapter(Replacement.class, new ReplacementAdapter()).create();
		final Rules rules = gson.fromJson(doNothing, Rules.class);
		proxy.requestModifier.addRules(rules.requests);
		proxy.responseModifier.addRules(rules.responses);
		proxy.start(4001, "../testcert/cert.crt", "../testcert/key.key");

		// start client and test some responses
		logger.info("Starting client");
		HttpClient client = new HttpClient();
		client.start();
		client.connect("localhost", "localhost", 4001);

		logger.info("Testing 404");
		SimpleHttpMessage notFound = client.sendRequest(request("GET", "/blah", "", "user-agent", "test-quic"),
				REQ_TIMEOUT);
		assertHttp(response(404, "not found", "content-type", "text-plain"), notFound, false);
		logger.info("OK");
		client.close();
		client = new HttpClient();
		client.start();
		client.connect("localhost", "localhost", 4001);
		logger.info("Testing repeat");
		SimpleHttpMessage request = request("POST", "/repeat?count=5", "", "user-agent", "test-quic");
		SimpleHttpMessage repeat = client.sendRequest(request, REQ_TIMEOUT);
		assertHttp(response(200, "AAAAA", "content-type", "text-plain"), repeat, false);
		logger.info("OK");
		client.close();
		client = new HttpClient();
		client.start();
		client.connect("localhost", "localhost", 4001);
		logger.info("Testing coffee");
		SimpleHttpMessage coffee = client.sendRequest(request("GET", "/coffee", "", "user-agent", "test-quic"),
				REQ_TIMEOUT);
		assertHttp(response(418, "V" + "|" + "." + "U", "X-Tea-Type", "Yorkshire Tea", "content-type", "tea"), coffee,
				false);
		logger.info("OK");
		client.close();
		client = new HttpClient();
		client.start();
		client.connect("localhost", "localhost", 4001);
		logger.info("Testing admin (1/2)");
		SimpleHttpMessage unauthorized = client.sendRequest(
				request("GET", "/admin", "", "X-Password", "Password", "user-agent", "test-quic"), REQ_TIMEOUT);
		assertEquals("wrong status code", 401, unauthorized.getResponseLine().status_code);
		logger.info("OK");
		client.close();
		client = new HttpClient();
		client.start();
		client.connect("localhost", "localhost", 4001);
		logger.info("Testing admin (2/2)");
		SimpleHttpMessage authorized = client.sendRequest(
				request("GET", "/admin", "", "X-Password", "super secret!", "user-agent", "test-quic"), REQ_TIMEOUT);
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
		SimpleJsonProxy proxy = new SimpleJsonProxy();
		proxy.defaultServerPort = 4000;
		Gson gson = new Gson().newBuilder().registerTypeAdapter(Replacement.class, new ReplacementAdapter()).create();
		final Rules rules = gson.fromJson(modifyRequests, Rules.class);
		proxy.requestModifier.addRules(rules.requests);
		proxy.responseModifier.addRules(rules.responses);
		proxy.start(4001, "../testcert/cert.crt", "../testcert/key.key");

		// start client and test some responses
		logger.info("Starting client");
		HttpClient client = new HttpClient();
		client.start();
		client.connect("localhost", "localhost", 4001);

		logger.info("Testing repeat");
		SimpleHttpMessage request = request("POST", "/repeat?count=5", "", "user-agent", "test-quic");
		SimpleHttpMessage repeat = client.sendRequest(request, REQ_TIMEOUT);
		assertHttp(response(200, "A".repeat(55), "content-type", "text-plain"), repeat, false);
		logger.info("OK");
		client.close();
		client = new HttpClient();
		client.start();
		client.connect("localhost", "localhost", 4001);
		logger.info("Testing tea");
		SimpleHttpMessage coffee = client.sendRequest(request("GET", "/tea", "", "user-agent", "test-quic"),
				REQ_TIMEOUT);
		assertHttp(response(418, "V" + "|" + "." + "U", "X-Tea-Type", "Yorkshire Tea", "content-type", "tea"), coffee,
				false);
		logger.info("OK");
		client.close();
		client = new HttpClient();
		client.start();
		client.connect("localhost", "localhost", 4001);
		logger.info("Testing admin");
		SimpleHttpMessage unauthorized = client.sendRequest(
				request("GET", "/admin", "", "X-Password", "super secret!", "user-agent", "test-quic"), REQ_TIMEOUT);
		assertEquals("wrong status code", 401, unauthorized.getResponseLine().status_code);
		logger.info("OK");
		// dispose of resources (stop threads etc.)
		client.close();
		server.stop();
		proxy.stop();
	}

	public static class TestServerRoutes {
		// Some requests
		@Request
		public SimpleHttpMessage notFound(SimpleHttpMessage request) {
			return response(404, "not found", "content-type", "text-plain");
		}

		@Request(priority = 1, method = "GET", path = "/coffee")
		public SimpleHttpMessage teapot(SimpleHttpMessage request) {
			return response(418, "V" + "|" + "." + "U", "X-Tea-Type", "Yorkshire Tea", "content-type", "tea");
		}

		@Request(priority = 1, method = "POST", path = "/repeat", parameters = { "count:[0-9]{1,5}" })
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

	}
}
