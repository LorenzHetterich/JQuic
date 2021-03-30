package lsquicbindings.struct;

import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * Wrapper class for native struct lsquic_http_headers
 */
public class lsquic_http_headers extends Structure {

	public int count;
    public lsxpack_header.ByReference headers;
	
	public lsquic_http_headers() {
		super();
	}
	
	public lsquic_http_headers(Pointer pointer) {
		super(pointer);
		read();
	}
	
	
	@Override
	protected List<String> getFieldOrder() {
		return List.of(
				"count",
				"headers"
		);
	}
	
	public static class ByReference extends lsquic_http_headers implements Structure.ByReference {
		public ByReference() {
			super();
		}
		
		public ByReference(Pointer pointer) {
			super(pointer);
			read();
		}
	}

}
