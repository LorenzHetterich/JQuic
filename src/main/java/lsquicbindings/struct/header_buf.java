package lsquicbindings.struct;

import java.util.List;

import com.sun.jna.Structure;

/**
 * Wrapper class for native struct iovec
 */
public class header_buf extends Structure {

	public int offset;
	public byte[] buf = new byte[0xFFFF];
	
	@Override
	protected List<String> getFieldOrder() {
		return List.of("offset", "buf");
	}
	
	public static class ByReference extends header_buf implements Structure.ByReference {
		
	}

}
