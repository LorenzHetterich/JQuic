package lsquicbindings.struct;

import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * Wrapper class for native struct iovec
 */
public class iovec extends Structure {

	public Pointer iov_base;
	public long iov_len;
	
	@Override
	protected List<String> getFieldOrder() {
		return List.of("iov_base", "iov_len");
	}
	
	
	public class ByReference extends iovec implements Structure.ByReference {
		
	}

}
