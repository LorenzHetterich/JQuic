package lsquicbindings;

import libraries.Libraries;

/**
 * constants used in lsquic library
 */
public class Constants {

	/**
	 * instance of the shared library
	 */
	public static final LSQuic lsquic = Libraries.lsquic;
	
	/**
	 * flag for server use (used in engine)
	 */
	public static final int LSENG_SERVER = 1 << 0;
	
	/**
	 * flag for http use (used in engine)
	 */
	public static final int LSENG_HTTP = 1 << 1;
	
	/**
	 * flag for client use (used in global initialization)
	 */
	public static final int LSQUIC_GLOBAL_CLIENT = 1 << 0;
	
	/**
	 * flag for server use (used in global initialization)
	 */
	public static final int LSQUIC_GLOBAL_SERVER = 1 << 1;
	
	/*
	 * versions
	 */
	
	/**
	 * draft 29
	 */
	public static final int LSQVER_ID29 = lsquic.lsquic_alpn2ver("h3-29", "h3-29".length());
	
	/**
	 * draft 32
	 */
	public static final int LSQVER_ID32 = lsquic.lsquic_alpn2ver("h3-32", "h3-32".length());
	
	/**
	 * draft 34
	 */
	public static final int LSQVER_ID34 = lsquic.lsquic_alpn2ver("h3-34", "h3-34".length());
}
