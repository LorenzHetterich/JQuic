package jquic.base.engine;

import com.sun.jna.Pointer;

import jquic.base.connection.QuicConnection;

/**
 * Supplies wrapper for native lsquic_conn_t
 */
@FunctionalInterface
public interface ConnectionSupplier {

	/**
	 * wrap the given native lsquic_conn_t into a {@link jquic.base.connection.QuicConnection QuicConnection}
	 * @param id of the connection
	 * @param Stream native lsquic_conn_t
	 * @param parent associated engine
	 * @return wrapped connection
	 */
	public QuicConnection apply(long id, Pointer Stream, QuicEngine parent);
	
}
