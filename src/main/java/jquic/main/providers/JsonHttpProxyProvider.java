package jquic.main.providers;

import java.io.File;
import java.nio.file.Files;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.gson.Gson;

import jquic.main.providers.json.Replacement;
import jquic.main.providers.json.ReplacementAdapter;
import jquic.main.providers.json.Rules;
import jquic.main.providers.json.SimpleJsonProxy;
import logging.Logger;

public class JsonHttpProxyProvider extends ProxyProvider {

	public static final String name = "json_http";

	/**
	 * Start JsonHttpProxy
	 * @param args arguments
	 */
	@Override
	public void start(String[] args) {
		// A logger is all we need
		Logger logger = new Logger(this);

		// For arguments from command line
		Args cmdl = new Args();

		// Process arguments
		JCommander commander = JCommander.newBuilder().programName("jquic -t " + name).acceptUnknownOptions(true).addObject(cmdl).build();
		try {
			commander.parse(args);
		} catch(ParameterException ex) {
			StringBuilder b = new StringBuilder();
			commander.getUsageFormatter().usage(b);
			System.err.println(b);
			ex.printStackTrace();
			System.exit(-1);
		}
		
		// File for args
		File f = new File(cmdl.file);

		// Check if file exists else error
		if(!f.exists()) {
			logger.error("File %s does not exist.", cmdl.file);
			System.exit(1);
		}

		// Check if it's a file else error
		if(!f.isFile()) {
			logger.error("%s is not a file.", cmdl.file);
			System.exit(1);
		}

		// Build replacements
		Gson gson = new Gson().newBuilder().registerTypeAdapter(Replacement.class, new ReplacementAdapter()).create();

		// Rules
		final Rules rules;

		// parse rules from JSON, if error exit
		try {
			rules = gson.fromJson(Files.newBufferedReader(f.toPath()), Rules.class);
		} catch(Exception e) {
			logger.exception(e, "Could not parse Json");
			System.exit(1);
			return;
		}
		
		logger.info("Starting proxy");

		// Start proxy with rules on specified arguments
		SimpleJsonProxy proxy = new SimpleJsonProxy();
		proxy.requestModifier.addRules(rules.requests);
		proxy.responseModifier.addRules(rules.responses);
		proxy.defaultServerPort = cmdl.server_port;
		
		if(cmdl.quicVersion == null) {
			cmdl.quicVersion = "h3-34";
		}
		
		proxy.start(cmdl.quicVersion, cmdl.port, cmdl.certificate, cmdl.key);
	}

	
	public class Args {
		// Cert for SSL
		@Parameter(names = {"-c", "--certificate"}, required = true, description = "certificate")
		public String certificate;
		// Key for SSL
		@Parameter(names = {"-k", "--key"}, required = true, description = "private key for certificate")
		public String key;
		// JSON file for rules
		@Parameter(names = {"-f", "--file"}, required = true, description = "input json file")
		public String file;
		// Port for proxy with default value
		@Parameter(names = {"-sp", "--source-port"}, required = false, description = "proxy port")
		public int port = 4001;
		// Server port with default value
		@Parameter(names = {"-dp", "--dest-port"}, required = false, description = "default server port")
		public int server_port = 443;
		// Quic version to use
		@Parameter(names = {"-v", "--version"}, required = false, description = "quic version to use, defaults to h3-34")
		public String quicVersion;
	}
	
}
