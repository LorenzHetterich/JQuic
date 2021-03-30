package boringsslbindings;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;

/**
 * Wrapper class for openssl library
 */
public interface BoringSsl extends Library {
	
	/**
	 * Cleans up the library. <br>
	 * Afaik this method is deprecated
	 */
	void EVP_cleanup();

	/**
	 * Create new SSL Context
	 * @param method
	 * @return Pointer to method
	 */
	Pointer SSL_CTX_new(Pointer method);

	/**
	 * Free SSL context
	 * @param context
	 */
	void SSL_CTX_free(Pointer context);

	/**
	 * @return pointer to TLS Method
	 */
	Pointer TLS_method();

	/**
	 * Init SSL Lib
	 */
	void SSL_library_init();

	/**
	 * Load related error messages
	 */
	void SSL_load_error_strings();

	/**
	 * Get servername from SSL context
	 * @param ssl_context giving info
	 * @param type as defined in RFC 3546
	 * @return servername as string
	 */
	String SSL_get_servername(Pointer ssl_context, int type);

	/**
	 * Load private key from file
	 * @param ctx SSL
	 * @param file to load from
	 * @param type as defined in RFC 3546
	 * @return sucess/fail
	 */
	int SSL_CTX_use_PrivateKey_file(Pointer ctx, String file, int type);

	/**
	 * Load certificate chain from file
	 * @param ctx SSL
	 * @param file to load from
	 * @return sucess/fail
	 */
	int SSL_CTX_use_certificate_chain_file(Pointer ctx, String file);

	/**
	 * Callback from context
	 * @param context SSL
	 * @param callback function
	 * @param arg argument
	 */
	void SSL_CTX_set_alpn_select_cb(Pointer context, ssl_alpn_cb callback, Pointer arg);

	/**
	 * Set min protocol version
	 * @param context SSL
	 * @param version of SSL
	 * @return min supported version
	 */
	int SSL_CTX_set_min_proto_version(Pointer context, int version);

	/**
	 * Set max protocol version
	 * @param context SSL
	 * @param version of SSL
	 * @return max supported version
	 */
	int SSL_CTX_set_max_proto_version(Pointer context, int version);

	/**
	 * Verify path in given context
	 * @param context SSL
	 * @return sucess/fail
	 */
	int SSL_CTX_set_default_verify_paths(Pointer context);

	/**
	 * Select next protocol
	 * @param out pointer
	 * @param outlen specified
	 * @param server used
	 * @param server_len size of server
	 * @param client used
	 * @param client_len size of client
	 * @return sucess/fail
	 */
	int SSL_select_next_proto(Pointer out, Pointer outlen, String server, int server_len, String client, int client_len);
	
	/**
	 * callback to select alpn
	 */
	@FunctionalInterface
	public interface ssl_alpn_cb extends Callback {

		/**
		 * Invoke callback for SSL alpn selection
		 * @param context of SSL
		 * @param out outgoing
		 * @param outlen specified
		 * @param in supported alpns sent by peer
		 * @param inlen length of input
		 * @param arg context supplied by user (90% sure)
		 * @return SSL_TLSEXT_ERR_OK on success, SSL_TLSEXT_ERR_ALERT_FATAL on error
		 */
		int invoke(Pointer context, Pointer out, Pointer outlen, String in, int inlen, Pointer arg);
		
	}
}
