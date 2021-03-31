package jquic.base.stream;

import java.io.OutputStream;

/**
 * Wrapper class for output of native lsquic_stream_t
 * wraps writing to a native lsquic_stream_t into an OutputStream
 */
public class QuicOutputStream extends OutputStream {

	/**
	 * underlying quic stream
	 */
	public final QuicStream qstream;
	
	/**
	 * true once the stream is closed <br>
	 * TODO: This is not really thread safe even though it is uses as if it was
	 */
	private boolean closed;

	/**
	 * Constructor
	 * @param qstream quic stream
	 */
	public QuicOutputStream(QuicStream qstream) {
		this.qstream = qstream;
	}


	/**
	 * Write on byte array basis
	 * @param arr data
	 * @param offset given
	 * @param amount to be written
	 */
	@Override
	public void write(byte[] arr, int offset, int amount) {
		if(amount == 0)
			return;
		
		if(closed)
			return;
		
		qstream.connection.engine.stream_write(qstream, qstream.context, arr, offset, amount);
		qstream.connection.engine.stream_flush(qstream);
			
	}

	/**
	 * Write byte array
	 * @param arr given
	 */
	@Override
	public void write(byte[] arr) {
		this.write(arr, 0, arr.length);
	}

	/**
	 * Write int as byte
	 * @param b int
	 */
	@Override
	public void write(int b) {
		write(new byte[] {(byte) b});
	}

	/**
	 * Flush specified by {@link jquic.base.engine.QuicEngine engine}
	 */
	@Override
	public void flush() {
		if(!closed)
			qstream.connection.engine.stream_flush(qstream);
	}

	/**
	 * Getter for {@link #closed}
	 * @return {@link #closed}
	 */
	public boolean isClosed() {
		return closed;
	}
	
	/**
	 * Close connection
	 */
	@Override
	public void close() {
		closed = true;
	}

}
