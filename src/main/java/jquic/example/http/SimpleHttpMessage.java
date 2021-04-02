package jquic.example.http;

import java.util.Map.Entry;

import jquic.base.stream.QuicStream;

/**
 * Generic http request / response. <br>
 * only use {@link #getRequestLine()} and {@link #getResponseLine()} if you know it is a request or response respectively!
 */
public class SimpleHttpMessage {

	/**
	 * request data, may be null or empty
	 */
	public byte[] data;
	
	/**
	 * request / response headers
	 */
	public HttpHeaders headers;
	
	/**
	 * request / response line
	 */
	public String firstLine;
	
	/**
	 * request line. Only available for requests
	 */
	private RequestLine requestLine;
	
	/**
	 * response line. Only available for responses
	 */
	private ResponseLine responseLine;
	
	/**
	 * server name indication
	 */
	public String sni;
	
	/**
	 * destination port
	 */
	public int port = 443;

	/**
	 * Constructor
	 * @param firstLine of msg
	 * @param headers of msg
	 * @param data contained
	 */
	public SimpleHttpMessage(String firstLine, HttpHeaders headers, byte[] data) {
		this.firstLine = firstLine;
		this.data = data;
		this.headers = headers;
	}

	/**
	 * Convert data and headers to string
	 * @param includeData include data or not
	 * @return string msg
	 */
	public String toString(boolean includeData) {
		StringBuilder str = new StringBuilder();
		str.append(firstLine).append("\r\n");
		for (Entry<String, String> header : headers.values) {
			str.append(header.getKey()).append(": ").append(header.getValue()).append("\r\n");
		}
		if(includeData && hasData())
			str.append(new String(data));
		return str.toString();
	}

	/**
	 * Convert full message to string
	 * @return msg as string
	 */
	@Override
	public String toString() {
		return toString(true);
	}

	/**
	 * Get request line
	 * @return request line forged out of first line of msg
	 */
	public RequestLine getRequestLine() {
		if(requestLine == null) {
			return requestLine = new RequestLine(firstLine);
		}
		return requestLine;
	}

	/**
	 * Get response line
	 * @return response line parsed from first line of rsp
	 */
	public ResponseLine getResponseLine() {
		if(responseLine == null) {
			return responseLine = ResponseLine.parse(firstLine);
		}
		return responseLine;
	}

	/**
	 * Parse message from stream
	 * @param stream listened
	 * @return Http Message
	 */
	public static SimpleHttpMessage parse(QuicStream stream) {

		RequestReader s = new RequestReader(stream);

		String firstLine = s.nextLine();

		HttpHeaders headers = new HttpHeaders();

		String header;

		// Parse header
		while (!(header = s.nextLine()).isEmpty()) {

			if (header.isEmpty())
				break;

			if (!header.contains(":")) {
				throw new RuntimeException("illegal header line does not contain ':'");
			}
			
			String name = header.substring(0, header.indexOf(':', 1));
			String value = header.substring(name.length() + 1);

			headers.put(name, value);
		}

		byte[] data = null;

		// Parse Data
		if(headers.contains("transfer-encoding") && headers.get("transfer-encoding").contains("chunked")) {
			data = s.readChunked();
		} else if (headers.contains("content-length")) { // content-length is ignored if 'Transfer-Encoding: chunked' is present
			data = s.read(Integer.valueOf(headers.get("content-length").strip()));
		} else {
			// no data
		}

		SimpleHttpMessage msg = new SimpleHttpMessage(firstLine, headers, data);
		return msg;
	}

	/**
	 * Check if message has data
	 * @return true/false
	 */
	public boolean hasData() {
		return data != null;
	}

}
