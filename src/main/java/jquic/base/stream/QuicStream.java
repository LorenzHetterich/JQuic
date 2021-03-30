package jquic.base.stream;

import com.sun.jna.Pointer;

import jquic.base.connection.QuicConnection;

/**
 * Wrapper class for native lsquic_stream_t
 */
public class QuicStream {	
	
	/**
	 * Wraps this stream into a {@link java.io.OutputStream}
	 */
	public final QuicOutputStream out;
	
	/**
	 * Wraps this stream into a {@link java.io.InputStream}
	 */
	public final QuicInputStream in;
	
	/**
	 * Quic connection associated with this stream
	 */
	public final QuicConnection connection;
	
	/**
	 * unique stream id (well unique per connection as long as the long does not overflow)
	 */
	public final long id;
	
	/**
	 * Pointer to underlying native lsquic_stream_t
	 */
	private final Pointer stream;
	
	/**
	 * Pointer to native stream context. <br>
	 * This is needed to be able to more efficiently read and write
	 */
	public final Pointer context;
	
	/**
	 * true once the stream is closed
	 */
	private boolean closed;
	
	/**
	 * Constructor 
	 * @param id {@link #id}
	 * @param context {@link #context}
	 * @param stream {@link #stream}
	 * @param connection {@link #connection}
	 */
	public QuicStream(long id, Pointer context, Pointer stream, QuicConnection connection) {
		this.id = id;
		this.context = context;
		this.connection = connection;
		this.out = new QuicOutputStream(this);
		this.in = new QuicInputStream(this);
		this.stream = stream;
	}
	
	/**
	 * Convenience method for {@link jquic.base.engine.QuicEngine#close_stream(Pointer)}
	 */
	public void close() {
		connection.engine.close_stream(getNative());
	}
	
	/**
	 * Getter for {@link #stream}
	 * @return {@link #stream}
	 */
	public Pointer getNative() {
		return stream;
	}
	
	/**
	 * Called when the stream is openend (may be overwritten)
	 */
	public void onOpened() {
		
	}

	/**
	 * Called when the stream is closed <br>
	 * Closes I/O streams
	 */
	public void onClosed() {
		if(closed)
			return;
		closed = true;
		in.close();
		out.close();
	}
	
}
