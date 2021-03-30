package jquic.base.stream;

import java.io.IOException;
import java.io.InputStream;

/**
 * Wrapper class for input of native lsquic_stream_t
 * wraps reading from a native lsquic_stream_t into an InputStream
 */
public class QuicInputStream extends InputStream {

	/**
	 * quic stream to read from
	 */
	public final QuicStream qstream;
	
	/**
	 * true once the stream is closed.
	 * If this value is true, all read operations will return -1 instantly upon calling
	 */
	private boolean closed;
	
	/**
	 * Constructor
	 * @param stream {@link #stream}
	 * @param qstream {@link #qstream}
	 */
	public QuicInputStream(QuicStream qstream) {
		this.qstream = qstream;
	}
	
	/**
	 * closes this InputStream (all read operations will be cancelled and new operations return -1)
	 */
	@Override
	public void close() {
		closed = true;
	}

	/**
	 * Read byte
	 * @return converted to int
	 */
	@Override
	public int read()  {
		if(closed)
			return -1;
		
		byte[] arr = new byte[1];
		int amount = read(arr);
		if(amount != 1)
			return -1;
		return arr[0] & 0xFF;
	}

	/**
	 * (see javadoc of super for description) <br>
	 * This method is faster than combining multiple reads as it attempts to read all the bytes in one go.
	 */
	@Override
	public byte[] readNBytes(int amount) throws IOException{
		if(closed)
			throw new IOException("Stream is closed!");
		
		if(amount == 0)
			return new byte[] {};
		
		byte[] arr = new byte[amount];
		
		int a = qstream.connection.engine.stream_read(qstream, arr, 0, amount, amount);
		
		if(a != amount) {
			throw new RuntimeException("Did not read the right amount of data!");
		}
		
		return arr;
	}
	
	/**
	 * Read byte array
	 * @param arr to be read
	 * @return data
	 */
	@Override 
	public int read(byte[] arr) {
		if(closed)
			return -1;
		
		return read(arr, 0, arr.length);
	}

	public boolean isClosed() {
		return closed;
	}
	
	/**
	 * Concurrent method to read Streams
	 * @param arr byte array
	 * @param offset given
	 * @param length of data stream
	 * @return data
	 */
	@Override
	public int read(byte[] arr, int offset, int length) {
		if(closed)
			return -1;
		
		if(length == 0)
			return 0;
		
		return qstream.connection.engine.stream_read(qstream, arr, offset, 1, length);
	}

}
