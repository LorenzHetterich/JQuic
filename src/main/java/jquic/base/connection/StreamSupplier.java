package jquic.base.connection;

import com.sun.jna.Pointer;

import jquic.base.stream.QuicStream;

/**
 * Supplies wrapper for native lsquic_stream_t
 */
@FunctionalInterface
public interface StreamSupplier {

	/**
	 * wrap the given native lsquic_stream_t into a {@link jquic.base.stream.QuicStream QuicStream}
	 * @param id of the stream
	 * @param context pointer to native stream context
	 * @param Stream native lsquic_stream_t
	 * @param parent associated connection
	 * @return wrapped stream
	 */
	public QuicStream apply(long id, Pointer context, Pointer Stream, QuicConnection parent);
	
}
