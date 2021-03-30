package jquic.example.http;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.StringJoiner;

/**
 * first line of a http request
 */
public class RequestLine {

	/**
	 * query parameters
	 */
	public List<Entry<String, String>> queries = new ArrayList<>();
	
	/**
	 * version string
	 */
	public String version = "HTTP/1.1";
	
	/**
	 * path (WITHOUT query)
	 */
	public String path = "/";
	
	/**
	 * http method
	 */
	public String method = "GET";
	
	/**
	 * query string
	 * @see #queries
	 */
	public String query = "";
	
	/**
	 * Constructor. <br>
	 * creates an empty request line.
	 */
	public RequestLine() {
		
	}

	/**
	 * Build query String
	 * @return query
	 */
	public String queryString() {
		StringJoiner joiner = new StringJoiner("&");
		
		for(Entry<String, String> query : queries) {
			joiner.add(
				query.getValue() == null ? URLEncoder.encode(query.getKey(), StandardCharsets.UTF_8) :
				URLEncoder.encode(query.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(query.getValue(), StandardCharsets.UTF_8)
			);
		}
		
		return joiner.toString();
	}

	/**
	 * Check if query contained
	 * @param name of searched query
	 * @return true/false
	 */
	public boolean hasQuery(String name) {
		for(Entry<String, String> query : queries) {
			if(query.getKey().equals(name))
				return true;
		}
		return false;
	}

	/**
	 * Getter for query
	 * @param name of searched query
	 * @return query or null
	 */
	public String getQuery(String name) {
		for(Entry<String, String> query : queries) {
			if(query.getKey().equals(name))
				return query.getValue();
		}
		return null;
	}

	/**
	 * Build Request line
	 * @param requestLine
	 */
	public RequestLine(String requestLine) {
		String[] tmp = requestLine.split(" ");
		assert(tmp.length == 3);
		method = tmp[0];
		path = tmp[1];
		version = tmp[2];
		
		if(path.contains("?")) {
			query = path.substring(path.indexOf('?')+1);
			path = path.substring(0, path.indexOf('?'));
			for(String q : query.split("&")) {
				if(!q.contains("=")) {
					queries.add(new AbstractMap.SimpleEntry<>(URLDecoder.decode(q, StandardCharsets.UTF_8), ""));
					continue;
				}
				String[] split = q.split("=");
				assert(split.length == 2);
				queries.add(new AbstractMap.SimpleEntry<>(
					URLDecoder.decode(split[0], StandardCharsets.UTF_8), 
					URLDecoder.decode(split[1], StandardCharsets.UTF_8)
				));
			}
		}
	}
	
	/**
	 * Convert the Request line back to a string (uses {@link #query} and does not deparse {@link #queries})
	 * @return String representation of the request line
	 */
	@Override
	public String toString() {
		return String.format("%s %s %s", method, path + (query.isEmpty() ? "" : "?" + query), version);
	}
	
}
