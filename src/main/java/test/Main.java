package test;

import static lsquicbindings.Constants.LSQUIC_GLOBAL_CLIENT;
import static lsquicbindings.Constants.LSQUIC_GLOBAL_SERVER;
import static lsquicbindings.Constants.lsquic;

import logging.LogLevel;
import logging.Logger;
import test.utils.TestExecutor;

/**
 * main class to run Tests. <br>
 * Can be run using gradle task 'runTests'
 */
public class Main {

	public static void main(String[] args) {

		// set default log level to debug
		Logger.defaultLevel = LogLevel.DEBUG.level;
		
		// Init LSQUIC
		if(0 != lsquic.lsquic_global_init(LSQUIC_GLOBAL_CLIENT | LSQUIC_GLOBAL_SERVER)) {
			System.out.println("Could not initialize lsquic library!");
			System.exit(1);
		}

		// Create executor
		TestExecutor executor = new TestExecutor();

		// Add Test classes
		executor.addClass(AnnotatedHttpProxyTests.class);
		executor.addClass(JsonProxyTests.class);
		executor.addClass(QuicProxyTest.class);

		// Return value aka status of tests
		int status = executor.runTests();
		
		if(status != 0)
			System.exit(status);
		
		// do not call exit, so we see if we have resource leaks due to Threads that should be dead but are still running
	}
	
}
