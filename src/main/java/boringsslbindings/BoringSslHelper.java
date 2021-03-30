package boringsslbindings;

import static boringsslbindings.Constants.OPENSSL_NPN_NEGOTIATED;
import static boringsslbindings.Constants.SSL_FILETYPE_PEM;
import static boringsslbindings.Constants.SSL_TLSEXT_ERR_ALERT_FATAL;
import static boringsslbindings.Constants.SSL_TLSEXT_ERR_OK;
import static boringsslbindings.Constants.TLS1_3_VERSION;
import static boringsslbindings.Constants.openssl;

import java.io.File;

import com.sun.jna.Pointer;

import boringsslbindings.BoringSsl.ssl_alpn_cb;

/**
 * Helper class that makes using openssl easier
 */
public class BoringSslHelper {

	/**
	 * basically a list of identifiers for the protocol (e.g. h3-29 for draft 29 of ietf-quic for http3) <br>
	 * Each entry is prefixed by its length, end is nullbyte (e.g. 5 || "h3-29" || 5 || "h3-28" || 0)
	 */
	private String alpn = "";
	
	/**
	 * pointer to native SSL context struct
	 */
	private Pointer context;
	
	/**
	 * adds a supported alnp
	 * @param alpn 
	 */
	public void addAlpn(String alpn) {
		this.alpn += (char)alpn.length() + alpn;
	}

	/**
	 * Cleanup SSL Lib
	 */
	public void cleanup() {
		openssl.EVP_cleanup();
	}

	/**
	 * Create SSL context by given default values
	 * @return Pointer to context
	 */
	public Pointer createContext() {	
		context = openssl.SSL_CTX_new(openssl.TLS_method());
		openssl.SSL_CTX_set_min_proto_version(context, TLS1_3_VERSION);
		openssl.SSL_CTX_set_max_proto_version(context, TLS1_3_VERSION);
		openssl.SSL_CTX_set_default_verify_paths(context);
		return context;
	}

	/**
	 * Select callback to be used in Lib
	 * @param context SSL
	 * @param out outgoing
	 * @param outlen specified
	 * @param in ingoing
	 * @param inlen specified
	 * @param arg arguments
	 * @return success/fail
	 */
	public int select_alpn_callback(Pointer context, Pointer out, Pointer outlen, String in, int inlen, Pointer arg) {
		if(openssl.SSL_select_next_proto(out, outlen, in, inlen, alpn, alpn.length()) == OPENSSL_NPN_NEGOTIATED) {
			return SSL_TLSEXT_ERR_OK;
		}
		return SSL_TLSEXT_ERR_ALERT_FATAL;
	}
	
	/**
	 * This reference is required so the GC does not collect and free it
	 */
	private ssl_alpn_cb callback;

	/**
	 * Set up certificate in SSL
	 * @param context SSL
	 * @param certificate given
	 * @param key for cert
	 */
	public void setupCertivicatePEM(Pointer context, String certificate, String key) {
		
		if(!new File(certificate).isFile() || !new File(key).isFile())
			throw new RuntimeException("Certificate or Key not found!");
		
		this.callback = this::select_alpn_callback;
		openssl.SSL_CTX_set_alpn_select_cb(context, callback, Pointer.NULL);
		openssl.SSL_CTX_use_certificate_chain_file(context, certificate);
		openssl.SSL_CTX_use_PrivateKey_file(context, key, SSL_FILETYPE_PEM);
	}

	/**
	 * Free context
	 * @param context SSL
	 */
	public void destroyContext(Pointer context) {
		openssl.SSL_CTX_free(context);
	}
	
}
