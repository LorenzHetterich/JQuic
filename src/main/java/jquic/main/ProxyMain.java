package jquic.main;

import static lsquicbindings.Constants.LSQUIC_GLOBAL_CLIENT;
import static lsquicbindings.Constants.LSQUIC_GLOBAL_SERVER;
import static lsquicbindings.Constants.lsquic;
import static usercodebindings.Constants.usercode;


import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Map;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import jquic.main.providers.JavaProxyProvider;
import jquic.main.providers.JsonHttpProxyProvider;
import jquic.main.providers.ProxyProvider;
import logging.LogLevel;
import logging.Logger;

/**
 * main class of production environment
 */
public class ProxyMain {
	
	/**
	 * proxy providers. <br>
	 * Provider to be used is specified by the -t commandline parameter
	 */
	public static Map<String, ProxyProvider> providers = Map
			.ofEntries(
					new SimpleEntry<>(JavaProxyProvider.name, new JavaProxyProvider()),
					new SimpleEntry<>(JsonHttpProxyProvider.name, new JsonHttpProxyProvider())
			);

	/**
	 * Main method
	 * @param args -t: provider to use
	 */
	public static void main(String[] args) {
		// init LSQUIC
		if(0 != lsquic.lsquic_global_init(LSQUIC_GLOBAL_CLIENT | LSQUIC_GLOBAL_SERVER)) {
			System.err.println("Could not initialize lsquic library!");
			System.exit(1);
		}

		// Arguments from command line
		Args cmdl = new Args();

		// Parse arguments
		JCommander commander = JCommander.newBuilder().programName("jquic").acceptUnknownOptions(true).addObject(cmdl).build();
		try {
			commander.parse(args);
		} catch(ParameterException ex) {
			StringBuilder b = new StringBuilder();
			commander.getUsageFormatter().usage(b);
			System.err.println(b);
			ex.printStackTrace();
			System.exit(-1);
		}
		
		// Get proxy provider based on given type
		ProxyProvider provider = providers.get(cmdl.type);

		// Case no provider, throw error
		if (provider == null) {
			System.err.println("unsupported proxy type " + cmdl.type);
			System.err.println("Supported: " + Arrays.toString(providers.keySet().toArray(new String[] {})));
			System.exit(1);
		}
		
		// setup logging
		if(cmdl.lsquicLogLevel == null) {
			cmdl.lsquicLogLevel = "NONE";
		}
		if(!cmdl.lsquicLogLevel.toLowerCase().equals("NONE")) {
			usercode.init_logging();
			lsquic.lsquic_set_log_level(cmdl.lsquicLogLevel);
		}
		
		if(cmdl.logLevel == null) {
			cmdl.logLevel = "INFO";
		}
		try {
			Logger.defaultLevel = LogLevel.valueOf(cmdl.logLevel.toUpperCase()).level;
		} catch(Throwable e) {
			System.err.printf("Unrecognized log level '%s', supported levels: %s", cmdl.logLevel, Arrays.toString(LogLevel.values()));
		}
		
		// Start
		provider.start(args);

	}

	/**
	 * commandline arguments of {@link ProxyMain#ProxyMain()} (before choosing provider)
	 */
	public static class Args {
		
		/**
		 * provider to use
		 */
		@Parameter(names = { "-t", "--type" }, description = "Proxy type", required = true)
		public String type;
		
		/**
		 * lsquic log level
		 */
		@Parameter(names = {"-L", "--lsquic-log-level"}, description = "Lsquic log level, default NONE", required = false)
		public String lsquicLogLevel;

		
		/**
		 * log level
		 */
		@Parameter(names = {"-l", "--log-level"}, description = "JQuic log level, default INFO", required = false)
		public String logLevel;
	}

}
