package libraries;

import java.io.File;
import java.io.IOException;

import com.sun.jna.Library;
import com.sun.jna.Native;

import boringsslbindings.BoringSsl;
import lsquicbindings.LSQuic;
import usercodebindings.Usercode;

/**
 * Utility class used to load libraries. <br>
 * This makes future cross platform support easier.
 */
public class Libraries {

	/**
	 * Loads a library
	 * @param <T> type of wrapper interface
	 * @param name library name
	 * @param clazz class of wrapper interface
	 * @return an instance of the wrapper interface wrapping native library calls
	 */
	private static final <T extends Library> T loadLibrary(String name, Class<T> clazz) {
		// for cross platform support, we need to change the code below and add libraries for other platforms!
		
		if(!System.getProperty("os.name").equals("Linux")) {
			throw new RuntimeException("JProxy only works on linux, I'm sorry :(");
		}
		
		if(!System.getProperty("os.arch").equals("amd64")) {
			throw new RuntimeException("JProxy only supports x86-64 processors, I'm sorry :(");
		}
		File file;
		try {
			file = Native.extractFromResourcePath("/linux-x86-64/" + "lib" + name + ".so");
		} catch (IOException e) {
			throw new RuntimeException("Could not extract library!", e);
		}
		
		return Native.load(file.getAbsolutePath(), clazz);
	}
	
	/**
	 * crypto library, required by {@link #ssl}
	 */
	public static final Library crypto = loadLibrary("crypto", Library.class);

	/**
	 * ssl library, required by {@link #lsquic} and {@link #decrepit}
	 */
	public static final BoringSsl ssl = loadLibrary("ssl", BoringSsl.class);
	
	/**
	 * I don't know, compiling boringssl spit out this library as well, so maybe {@link #lsquic} needs it?
	 */
	public static final Library decrepit = loadLibrary("decrepit", Library.class);
	
	/**
	 * lsquic library. QUIC + HTTP/3
	 */
	public static final LSQuic lsquic = loadLibrary("lsquic", LSQuic.class);
	
	/**
	 * usercode library. Uses native code for some performance gain
	 */
	public static final Usercode usercode = loadLibrary("usercode", Usercode.class);
	
}
