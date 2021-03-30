package test.resources;

import java.io.IOException;

/**
 * Used to load resources from src/main/resources/test/
 */
public class Resources {

	/**
	 * Load file
	 * @param file path
	 * @return loaded resource
	 */
	public static String loadString(String file) {
		try {
			return new String(Resources.class.getResourceAsStream("/test/" + file).readAllBytes());
		} catch (IOException e) {
			throw new RuntimeException("Could not load file " + file, e);
		}
	}
	
}
