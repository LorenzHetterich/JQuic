package usercodebindings;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

/**
 * Wrapper class for user-written native code
 */
public interface Usercode extends Library {

	/**
	 * native example method
	 */
	void init_logging();

	/**
	 * destroy stream context
	 */
	void delete_stream_ctx(Pointer context);
	
	/**
	 * write to stream
	 */
	void stream_write(Pointer stream, Pointer context, Pointer data, int offset, int length);

	
	/**
	 * wait until current write operation is done
	 */
	int stream_write_wait(Pointer context);
	
	/**
	 * write callback
	 */
	void stream_write_cb(Pointer stream, Pointer context);
	
	/**
	 * read from stream
	 */
	void stream_read(Pointer stream, Pointer context, Pointer data, int min, int max);

	
	/**
	 * wait until current read operation is done
	 */
	int stream_read_wait(Pointer context, int max);
	
	/**
	 * read callback
	 */
	void stream_read_cb(Pointer stream, Pointer context);
	
	/**
	 * create stream context
	 */
	Pointer create_stream_ctx(long id);
}
