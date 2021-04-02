package test.utils;

import jquic.example.http.HttpHeaders;
import jquic.example.http.ResponseLine;
import jquic.example.http.SimpleHttpMessage;

/**
 * Utilities for HTTP stuff <br>
 * TODO: This does not really belong into test.utils but rather into jquic.example.http I guess
 */
public class HttpUtils {
	/**
	 * Reponse case
	 * @param status code
	 * @param d provided in msg
	 * @param headers of msg
	 * @return HTTP msg
	 */
	public static SimpleHttpMessage response(int status, byte[] d, String... headers) {
		HttpHeaders h = new HttpHeaders();
		h.put(":status", Integer.toString(status));

		for (int i = 0; i < headers.length - 1; i += 2) {
			h.put(headers[i], headers[i + 1]);
		}
		
		if(d == null) {
			d = new byte[] {};
		}
		
		if (d.length > 0) {
			h.put("content-length", Integer.toString(d.length));
		}

		return new SimpleHttpMessage(new ResponseLine(status).toString(), h, d);
	}
	
	public static SimpleHttpMessage response(int status, String data, String... headers) {
		return response(status, data.getBytes(), headers);
	}

	/**
	 * Case request
	 * @param authority same as sni (not really though)
	 * @param method provided
	 * @param path used
	 * @param data provided
	 * @param headers used
	 * @return HTTP msg
	 */
	public static SimpleHttpMessage request(String authority, String method, String path, byte[] data, String... headers) {
		HttpHeaders h = new HttpHeaders();
		if(authority != null) {
			h.put(":authority", authority);
		}
		h.put(":method", method);
		h.put(":path",  path);
		h.put(":scheme", "https");

		for (int i = 0; i < headers.length - 1; i += 2) {
			h.put(headers[i], headers[i + 1]);
		}

		byte[] d = data == null ? new byte[] {} : data;

		if (d.length > 0) {
			h.put("content-length", Integer.toString(d.length));
		}

		return new SimpleHttpMessage(String.format("%s %s HTTP/1.1", method, path), h, d);
	}
	
	/**
	 * Case request
	 * @param method provided
	 * @param path used
	 * @param data provided
	 * @param headers used
	 * @return HTTP msg
	 */
	public static SimpleHttpMessage request(String method, String path, byte[] data, String... headers) {
		return request(null, method, path, data, headers);
	}
	
	/**
	 * Case request
	 * @param authority same as sni (not really though)
	 * @param method provided
	 * @param path used
	 * @param data provided
	 * @param headers used
	 * @return HTTP msg
	 */
	public static SimpleHttpMessage request(String authority, String method, String path, String data, String... headers) {
		return request(authority, method, path, data == null ? new byte[] {} : data.getBytes(), headers);
	}
	
}
