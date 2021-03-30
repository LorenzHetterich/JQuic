package usercodebindings;

import com.sun.jna.Native;

/**
 * Constants used in usercode library
 */
public class Constants {

	// Load native lib for usercode
	public static final Usercode usercode = Native.load("native/libs/libusercode.so", Usercode.class);
	
}
