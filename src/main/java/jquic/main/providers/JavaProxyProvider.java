package jquic.main.providers;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;


public class JavaProxyProvider extends ProxyProvider {

	public static final String name = "java";

	/**
	 * Start Proxy
	 * @param args arguments
	 */
	@Override
	public void start(String[] args) {

		// from command line
		Args cmdl = new Args();
		
		JCommander.newBuilder().acceptUnknownOptions(true).addObject(cmdl).build().parse(args);

		// Put into file
		File f = new File(cmdl.file);
		
		// Check if file exists else error
		if(!f.exists()) {
			System.err.println("File " + cmdl.file + " does not exist.");
			System.exit(1);
		}

		// Check if file is a file else error
		if(!f.isFile()) {
			System.err.println(cmdl.file + " is not a file.");
			System.exit(1);
		}
		
		File compiled = null;

		// Build temp directory for file
		try {
			compiled = Files.createTempDirectory("jquic_").toFile();
		} catch (IOException e) {
			System.err.println("Cannot create temporary file: " + e.getMessage());
			System.exit(1);
		}

		// Build, compile and start process
		ProcessBuilder builder = new ProcessBuilder().command("javac", "-cp", "jquic.jar", f.getAbsolutePath(), "-d", compiled.getAbsolutePath());
		Process proc = null;
		try {
			proc = builder.start();
		} catch (IOException e) {
			System.err.println("Could not start compiling: " + e.getMessage());
			System.exit(1);
		}

		// Compile it, throw error if exit_code==0 or process interrupted
		try {
			int exit_code = proc.waitFor();
			if(exit_code != 0) {
				System.err.println("Compiling ended with error code " + exit_code);
				System.err.println("stderr:");
				System.err.println(new String(proc.getErrorStream().readAllBytes()));
				System.err.println("stdout:");
				System.err.println(new String(proc.getInputStream().readAllBytes()));
				System.exit(1);
			}
		} catch (InterruptedException e) {
			System.err.println("Interrupted while compiling!");
			proc.destroyForcibly();
			System.exit(1);
		} catch(IOException e) {
			System.err.println("IOException while compiling: " + e.getMessage());
			System.exit(1);
		}

		// Create URL
		URLClassLoader loader = null;
		try {
			loader = new URLClassLoader(new URL[] {compiled.toURI().toURL()}, ClassLoader.getSystemClassLoader());
		} catch (MalformedURLException e) {
			System.err.println("Could not create URL from temp dir: " + e.getMessage());
			System.exit(1);
		}

		// Compiled files
		File[] files = compiled.listFiles();

		// Check if list not empty
		if(files.length == 0) {
			System.err.println("Compiling produced no file!");
			System.exit(1);
		}

		// List classes
		List<Class<?>> classes = new ArrayList<>();

		// Check files
		for(File file : files) {
			if(!file.isFile())
				continue;
			
			String name = file.getName();
			
			// remove .class
			name = name.substring(0, name.length() - ".class".length());
			Class<?> clazz;
			// Load classes
			try {
				clazz = loader.loadClass(name);
			} catch (ClassNotFoundException e) {
				System.err.println("Error while loading " + name + ": " + e.getMessage());
				continue;
			}
			
			classes.add(clazz);
		}
		
		for(Class<?> clazz : classes) {
			// Find main
			for(Method m : clazz.getMethods()) {
				if(m.getName().equals("main") && Modifier.isStatic(m.getModifiers()) && m.getParameterCount() == 1 && m.getParameters()[0].getType().equals(String[].class)) {
					System.out.println("Found main in class " + clazz.getName());
					// Execute it
					try {
						m.invoke(null, new Object[] {args});
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						System.err.println("Error while execution main of " + clazz.getName() + ": " + e.getMessage());
					}
				}
			}
		}
	}

	public class Args {
		// Arguments as param
		@Parameter(names = {"-f", "--file"}, required = true, description = "input java file")
		public String file;
		
	}
	
}
