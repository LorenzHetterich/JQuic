package jquic.example.http;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import lsquicbindings.struct.header_buf;
import lsquicbindings.struct.lsquic_http_headers;
import lsquicbindings.struct.lsxpack_header;

/**
 * Wrapps native struct lsquic_http_headers
 */
public class HttpHeaders {

	/**
	 * values. <br>
	 * This is a list, as the order is important!
	 */
	public List<Entry<String, String>> values;
	
	/**
	 * header names. <br>
	 * allows faster check if header is present (well not really unless you send like alot of them)
	 */
	public Set<String> keys;

	/**
	 * Constructor for Headers
	 */
	public HttpHeaders() {
		keys = new HashSet<>();
		values = new ArrayList<>();
	}

	/**
	 * Check if header is contained in key list
	 * @param name of hdr
	 * @return success/fail
	 */
	public boolean contains(String name) {
		return keys.contains(name.toLowerCase());
	}

	/**
	 * Put header in first place of the list
	 * @param name of hdr
	 * @param value of hdr
	 */
	public void putFirst(String name, String value) {
		name = name.toLowerCase();
		if(keys.contains(name)) {
			remove(name);
		}
		
		values.add(0, new AbstractMap.SimpleEntry<String, String>(name, value.strip()));
		keys.add(name);
	}

	/**
	 * Remove header
	 * @param name of hdr
	 */
	public void remove(String name){
		final String n = name.toLowerCase();
		values.removeIf(x -> x.getKey().equals(n));
		keys.remove(n);
	}

	/**
	 * Put header
	 * @param name of hdr
	 * @param value of hdr
	 */
	public void put(String name, String value) {
		final String n = name.toLowerCase();
		if(keys.contains(n)) {
			values.replaceAll(x -> x.getKey().equals(n) ? new AbstractMap.SimpleEntry<String, String>(n, value.strip()) : x);
		} else {
			values.add(new AbstractMap.SimpleEntry<String, String>(n, value.strip()));
			keys.add(n);
		}
	}

	/**
	 * Get header based on its name
	 * @param name of hdr
	 * @return String
	 */
	public String get(String name) {
		name = name.toLowerCase();
		if(!keys.contains(name)) {
			return null;
		}
		
		for(Entry<String, String> header : values) {
			if(header.getKey().equals(name)) {
				return header.getValue();
			}
		}
		
		throw new RuntimeException("header is broken!");
	}

	/**
	 * Config for headers <br>
	 * @param hdr header
	 * @param header_buf buffer
	 * @param name_offset offeset for name
	 * @param name_len length of name
	 * @param val_offset value offset
	 * @param val_len size of value
	 * @deprecated use {@link usercodebindings.Usercode#send_headers(com.sun.jna.Pointer, com.sun.jna.StringArray, int)} instead!
	 */
	@Deprecated
	private void lsxpack_header_set_offset2(lsxpack_header hdr, header_buf.ByReference header_buf, int name_offset, int name_len, int val_offset, int val_len) {
		hdr.buf = header_buf.getPointer().share(4 + header_buf.offset);
		hdr.name_offset = (short) name_offset;
		hdr.name_len = (short) name_len;
		hdr.val_offset = (short) val_offset;
		hdr.val_len = (short) val_len;
	}

	/**
	 * Set pointer to header
	 * @param hdr header
	 * @param header_buf buffer
	 * @param name of header
	 * @param value given
	 * @return sucess/fail
	 * @deprecated use {@link usercodebindings.Usercode#send_headers(com.sun.jna.Pointer, com.sun.jna.StringArray, int)} instead!
	 */
	@Deprecated
	private int header_set_ptr(lsxpack_header hdr, header_buf.ByReference header_buf, String name, String value) {
		int name_len = name.length();
		int val_len = value.length();
		
		if (header_buf.offset + name_len + val_len <= 0xFFFF)
	    {
			
			System.arraycopy(name.getBytes(), 0, header_buf.buf, header_buf.offset, name.length());
	        System.arraycopy(value.getBytes(), 0, header_buf.buf, header_buf.offset + name_len, value.length());
	        
	        lsxpack_header_set_offset2(hdr, header_buf, 0, name_len, name_len, val_len);
	        
	        header_buf.offset += name_len + val_len;
	        return 0;
	    }
	    else
	        return -1;
	}
	
	/**
	 * Transfer headers to native lsquic lib
	 * @deprecated use {@link usercodebindings.Usercode#send_headers(com.sun.jna.Pointer, com.sun.jna.StringArray, int)} instead!
	 */
	@Deprecated
	public lsquic_http_headers.ByReference toNative(){
		
		lsquic_http_headers.ByReference headers = new lsquic_http_headers.ByReference();

		header_buf.ByReference hbuf = new header_buf.ByReference();

		headers.count = values.size();
		lsxpack_header.ByReference ref = new lsxpack_header.ByReference();
		lsxpack_header[] header_arr = (lsxpack_header[])ref.toArray(headers.count);
		
		
		int index = 0;
		for(Entry<String, String> entry : values) {
			
			lsxpack_header header = header_arr[index];
			
			header_set_ptr(header, hbuf, entry.getKey(), entry.getValue());
			
			header.write();
			
			index ++;
		} 
		
		// TODO: hbug might get Garbage Collected :O
		
		headers.headers = ref;
		ref.write();
		hbuf.write();
		headers.write();
		return headers;
	}
	
}
