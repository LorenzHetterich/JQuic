package jquic.main;

import static lsquicbindings.Constants.LSQUIC_GLOBAL_CLIENT;
import static lsquicbindings.Constants.LSQUIC_GLOBAL_SERVER;
import static lsquicbindings.Constants.lsquic;
import static usercodebindings.Constants.usercode;

import jquic.example.http.SimpleHttpMessage;
import jquic.example.http.annotated.AnnotatedHttpServer;
import jquic.example.http.annotated.Request;
import test.utils.HttpUtils;

/**
 * main class when running from gradle task 'gradleRun'. <br>
 * used for testing
 */
public class GradleMain {

	/*
	 * Just some test code :)
	 */
	
	public static AnnotatedHttpServer server;
	
	public static void main(String[] args) throws InterruptedException {
		System.out.println("HALLEOASDAD");
		usercode.init_logging();

		// Init lsquic on user
		if(0 != lsquic.lsquic_global_init(LSQUIC_GLOBAL_CLIENT | LSQUIC_GLOBAL_SERVER)) {
			System.err.println("Could not initialize lsquic library!");
			System.exit(1);
		}
		
		System.out.println("Running from gradle build :)");
		
		server = new AnnotatedHttpServer();
		
		server.requestHandler.addObject(new Routes());
		
		server.start("h3-34", 4000, "testcert/cert.crt", "testcert/key.key");
	}
	
	public static class Routes {
		
		@Request
		public SimpleHttpMessage notFound(SimpleHttpMessage request) {
			return HttpUtils.response(200, "A".repeat(100000000), "x-server", "jquic");
		}
		
	}
	
}
