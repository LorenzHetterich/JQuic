package lsquicbindings.struct;

import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * Wrapper class for native struct lsquic_out_spec
 */
public class lsquic_out_spec extends Structure {

	public Pointer iov;
	// TODO: is length really twice?
	public long iovlen;
	public Pointer local_sa;
	public Pointer dest_sa;
	public Pointer peer_ctx;
	
	/* will be NULL when sending out the first batch of handshake packets */
	public Pointer conn_ctx;  
	public int ecn;   
	
	
	@Override
	protected List<String> getFieldOrder() {
		return List.of(
				"iov",
				"iovlen",
				"local_sa",
				"dest_sa",
				"peer_ctx",
				"conn_ctx",
				"ecn"
		);
	}
	
	public static class ByReference extends lsquic_out_spec implements Structure.ByReference {
		
	}

}
