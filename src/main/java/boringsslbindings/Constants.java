package boringsslbindings;

import libraries.Libraries;

/**
 * Constants used in openssl library
 */
public class Constants {

	/**
	 * constant for TLS version 1.3
	 */
	public static final int TLS1_3_VERSION = 0x0304;
	
	/**
	 * Constant for PEM files
	 */
	public static final int SSL_FILETYPE_PEM  = 1;
	
	/**
	 * Constant to indicate NPN was negotiated sucessfully
	 */
	public static final int OPENSSL_NPN_NEGOTIATED = 1;
	
	/**
	 * Constant to indicate there was no error
	 */
	public static final int SSL_TLSEXT_ERR_OK = 0;
	
	/**
	 * Constant to indicate there was a fatal error
	 */
	public static final int SSL_TLSEXT_ERR_ALERT_FATAL = 2;
	
	/**
	 * Instance of the shared library
	 */
	public static BoringSsl openssl;
	
	/**
	 * executed on class load
	 */
	static {
		// load shared library
		openssl = Libraries.ssl;
		// initialize library 
		openssl.SSL_library_init();
		// load error strings (even though we don't use them yet ...)
		openssl.SSL_load_error_strings();
	}
}
