package lsquicbindings.struct;

import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * Wrapper class for native struct lsquic_http_headers
 */
public class lsxpack_header extends Structure {

	public Pointer buf;
	public int name_hash;
	public int nameval_hash;
	
	public short name_offset, name_len, val_offset, val_len;
	
	public short chain_next_idx;
	
	public byte hpack_index, qpack_index, app_index;
	
	public byte flags;
	
	public byte index_type, dec_overhead;

	
	public lsxpack_header() {
		super();
	}
	
	public lsxpack_header(Pointer pointer) {
		super(pointer);
		read();
	}
	
	@Override
	protected List<String> getFieldOrder() {
		return List.of(
			"buf", "name_hash", "nameval_hash", "name_offset", "name_len", "val_offset", "val_len",
			"chain_next_idx", "hpack_index", "qpack_index", "app_index", "flags", "index_type", "dec_overhead"
		);
	}
	
	public static class ByReference extends lsxpack_header implements Structure.ByReference {
		
		public ByReference() {
			super();
		}
		
		public ByReference(Pointer pointer) {
			super(pointer);
			read();
		}
		
	}

}
