package jquic.example.http;

import java.util.Map;
import java.util.AbstractMap.SimpleEntry;

/**
 * first line of http responses. <br>
 * TODO: maybe use some library... they should exist
 */
public class ResponseLine {

	/**
	 * Listing all status codes
	 */
	public static final Map<Integer, String> status_code_map = Map.ofEntries(
		new SimpleEntry<Integer, String>(100, "Continue"),
		new SimpleEntry<Integer, String>(101, "Switching Protocols"),
		new SimpleEntry<Integer, String>(102, "Processing"),
		new SimpleEntry<Integer, String>(103, "Early Hints"),
		
		new SimpleEntry<Integer, String>(200, "OK"),
		new SimpleEntry<Integer, String>(201, "Created"),
		new SimpleEntry<Integer, String>(202, "Accepted"),
		new SimpleEntry<Integer, String>(203, "Non-Authoritative Information"),
		new SimpleEntry<Integer, String>(204, "No Content"),
		new SimpleEntry<Integer, String>(205, "Reset Content"),
		new SimpleEntry<Integer, String>(206, "Partial Content"),
		new SimpleEntry<Integer, String>(207, "Multi-Status"),
		new SimpleEntry<Integer, String>(208, "Already Reported"),
		new SimpleEntry<Integer, String>(226, "IM Used"),
		
		new SimpleEntry<Integer, String>(300, "Multiple Choices"),
		new SimpleEntry<Integer, String>(301, "Moved Permanently"),
		new SimpleEntry<Integer, String>(302, "Found"),
		new SimpleEntry<Integer, String>(303, "See Other"),
		new SimpleEntry<Integer, String>(304, "Not Modified"),
		new SimpleEntry<Integer, String>(305, "Use Proxy"),
		new SimpleEntry<Integer, String>(307, "Temporary Redirect"),
		new SimpleEntry<Integer, String>(308, "Permanent Redirect"),
		
		new SimpleEntry<Integer, String>(400, "Bad Request"),
		new SimpleEntry<Integer, String>(401, "Unauthorized"),
		new SimpleEntry<Integer, String>(402, "Payment Required"),
		new SimpleEntry<Integer, String>(403, "Forbidden"),
		new SimpleEntry<Integer, String>(404, "Not Found"),
		new SimpleEntry<Integer, String>(405, "Method Not Allowed"),
		new SimpleEntry<Integer, String>(406, "Not Acceptable"),
		new SimpleEntry<Integer, String>(407, "Proxy Authentication Required"),
		new SimpleEntry<Integer, String>(408, "Request Timeout"),
		new SimpleEntry<Integer, String>(409, "Conflict"),
		new SimpleEntry<Integer, String>(410, "Gone"),
		new SimpleEntry<Integer, String>(411, "Length Required"),
		new SimpleEntry<Integer, String>(412, "Precondition Failed"),
		new SimpleEntry<Integer, String>(413, "Payload Too Large"),
		new SimpleEntry<Integer, String>(414, "URI Too Long"),
		new SimpleEntry<Integer, String>(415, "Unsupported Media Type"),
		new SimpleEntry<Integer, String>(416, "Range Not Satisfiable"),
		new SimpleEntry<Integer, String>(417, "Expectation Failed"),
		new SimpleEntry<Integer, String>(421, "Misdirected Request"),
		new SimpleEntry<Integer, String>(422, "Unprocessable Entity"),
		new SimpleEntry<Integer, String>(423, "Locked"),
		new SimpleEntry<Integer, String>(424, "Failed Dependency"),
		new SimpleEntry<Integer, String>(425, "Too Early"),
		new SimpleEntry<Integer, String>(426, "Upgrade Required"),
		new SimpleEntry<Integer, String>(428, "Precondition Required"),
		new SimpleEntry<Integer, String>(429, "Too Many Requests"),
		new SimpleEntry<Integer, String>(431, "Request Header Fields Too Large"),
		new SimpleEntry<Integer, String>(451, "Unavailable For Legal Reasons"),
		
		new SimpleEntry<Integer, String>(418, "I'm a teapot"),
		new SimpleEntry<Integer, String>(420, "Policy Not Fullfiled"),
		new SimpleEntry<Integer, String>(444, "No Response"),
		new SimpleEntry<Integer, String>(449, "Retry"), // TODO
		new SimpleEntry<Integer, String>(499, "Client Closed Request"),
		
		new SimpleEntry<Integer, String>(500, "Internal Server Error"),
		new SimpleEntry<Integer, String>(501, "Not Implemented"),
		new SimpleEntry<Integer, String>(502, "Bad Gateway"),
		new SimpleEntry<Integer, String>(503, "Service Unavailable"),
		new SimpleEntry<Integer, String>(504, "Gateway Timeout"),
		new SimpleEntry<Integer, String>(505, "HTTP Version not supported"),
		new SimpleEntry<Integer, String>(506, "Variant Also Negotiates"),
		new SimpleEntry<Integer, String>(507, "Insufficient Storage"),
		new SimpleEntry<Integer, String>(508, "Loop Detected"),
		new SimpleEntry<Integer, String>(509, "Bandwidth Limit Exceeded"),
		new SimpleEntry<Integer, String>(510, "Not Extended"),
		new SimpleEntry<Integer, String>(511, "Network Authentication Required")
	);
	
	/**
	 * status message
	 */
	public String status = "OK";
	
	/**
	 * status code
	 */
	public int status_code = 200;
	
	/**
	 * version string
	 */
	public String version = "HTTP/1.1";

	/**
	 * Constructor.
	 * @param status_code status code to set (will also set {@link #status} if possible)
	 */
	public ResponseLine(int status_code) {
		setStatus(status_code);
	}
	
	/**
	 * Constructor. <br>
	 * creates default Response Line (HTTP/1.1 200 OK)
	 */
	public ResponseLine() {
		
	}

	/**
	 * Parse from first line of http response
	 * @param line first line of http response
	 * @return a new {@link ResponseLine} representing the given line
	 */
	public static ResponseLine parse(String line) {
		ResponseLine r = new ResponseLine();
		
		int offset = line.indexOf(' ');
		
		r.version = line.substring(0, offset);
		
		int offset2 = line.indexOf(' ', offset + 1);
		
		r.status_code = Integer.parseInt(line.substring(offset + 1, offset2));
		r.status = line.substring(offset2+1);
		
		if(r.status.isBlank()) {
			// infer status from code
			r.status = status_code_map.get(r.status_code);
			if(r.status == null)
				r.status = "";
		}
		
		return r;
	}

	/**
	 * Set {@link #status_code}. Will also update {@link #status} if possible
	 * @param code status code to set
	 */
	public void setStatus(int code) {
		this.status_code = code;
		String status = status_code_map.get(code);
		if(status != null) {
			this.status = status;
		} else {
			System.err.println(String.format("[WARNING]: no status registered for code %d", code));
		}
	}
	
	@Override
	public String toString() {
		return String.format("%s %d %s", version, status_code, status);
	}
	
	
	
}
