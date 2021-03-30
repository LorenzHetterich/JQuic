package jquic.example.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import jquic.base.stream.QuicStream;

/**
 * Utility class to read http requests / responses. <br>
 * TODO: Maybe we should rely on some library, as this really should exist out there ...
 */
public class RequestReader {
	
	/**
	 * internally used buffer
	 */
	private byte[] buf;
	
	/**
	 * internally used buffer offset
	 */
	private int buf_offset;
	
	/**
	 * amount of data inside {@link #buf}
	 */
	private int buf_size;
	
	/**
	 * stream to read from
	 */
	private QuicStream stream;

	/**
	 * Constructor
	 * @param stream stream to read from
	 */
	public RequestReader(QuicStream stream) {
		this.stream = stream;
		// TODO: maybe not hardcode the buffer size?
		this.buf = new byte[8192];
	}

	/**
	 * Try to read some data into {@link #buf} from {@link #stream}
	 * @return true, iff data could be buffered
	 */
	public boolean buffer() {
		int amount = stream.in.read(buf);
		buf_size = amount;
		buf_offset = 0;
		return buf_size > 0;
	}

	/**
	 * Read one byte from {@link #buf}
	 * @return byte read
	 */
	public byte read() {
		if(buf_offset >= buf_size) {
			if(!buffer()) {
				throw new RuntimeException("unexpected End of input");
			}
		}
		
		return buf[buf_offset ++];
	}

	/**
	 * Read content with chunked transfer encoding. <br>
	 * Used with Transfer-Encoding: chunked
	 * @return data read
	 */
	public byte[] readChunked() {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		while(true) {
			// size of the chunk (in hex)
			String line = nextLine();
			
			try {
				int length = Integer.parseInt(line, 16);
				
				// read chunk
				out.write(read(length));
				
				// read remaining \r\n
				byte[] r = read(2);
				if(r[0] != '\r' || r[1] != '\n') {
					throw new IOException("Chunk does not end with magic bytes!");
				}
				
				if(length == 0) {
					break;
				}
			} catch(Exception e) {
				throw new RuntimeException("Error while reading chunk", e);
			}
		}
		
		return out.toByteArray();
	}

	/**
	 * Read a certain specified amount. <br>
	 * Used with Content-Length.
	 * @param amount to be read
	 * @return byte array of data read
	 */
	public byte[] read(int amount) {
		byte[] result = new byte[amount];
		int buf_left = Math.max(0, buf_size - buf_offset);
		if(buf_left >= amount) {
			System.arraycopy(buf, buf_offset, result, 0, amount);
			buf_offset += amount;
			return result;
		} else if(buf_left > 0) {
			System.arraycopy(buf, buf_offset, result, 0, buf_left);
			buf_offset += buf_left;
		}
		try {
			byte[] tmp = stream.in.readNBytes(amount - buf_left);
			System.arraycopy(tmp, 0, result, buf_left, tmp.length);
			return result;
		} catch (IOException e) {
			throw new RuntimeException("IOException while reading", e);
		}
	}

	/**
	 * Reads next line and returns it as string
	 * @return String
	 */
	public String nextLine() {
		StringBuilder builder = new StringBuilder();
		
		boolean last = false;
		
		while(true) {
			char cur = (char) read();
			if(last && cur == '\n') {
				break;
			}
			builder.append(cur);
			last = cur == '\r';
		}
		
		return builder.substring(0, builder.length() - 1);
	}

	/**
	 * Reads all data from the stream and {@link #buf} (blocks until stream is closed for reading)
	 * @return byte array of data
	 */
	public byte[] readAll() {
		
		try {
			byte[] before = buf_size-buf_offset > 0 ? read(buf_size-buf_offset) : new byte[] {};
			byte[] all = stream.in.readAllBytes();
			
			if(before.length == 0)
				return all;
			
			byte[] result = new byte[before.length + all.length];
			System.arraycopy(before, 0, result, 0, before.length);
			System.arraycopy(all, 0, result, before.length, all.length);
			
			return result;
		} catch (IOException e) {
			throw new RuntimeException("IOException while reading!", e);
		}
	}
}
