package lsquicbindings.struct;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * Wrapper class for native struct suckaddr_in
 * 
 * should support both, IPv4 and IPv6 (though it is only really tested with IPv4 :D)
 */
public class sockaddr_in extends Structure{

	public static final short AF_INET = 2;
	public static final short AF_INET6 = 10;
	
	public short sin_family;
	public short sin_port;
	public byte[] sin_addr = new byte[12];
	
	
	public sockaddr_in() {
		super();
	}
	
	public sockaddr_in(Pointer pointer) {
		super(pointer);
		read();
	}
	
	@Override
	protected List<String> getFieldOrder() {
		return List.of("sin_family", "sin_port", "sin_addr");
	}

	public static class ByReference extends sockaddr_in implements Structure.ByReference {
		public ByReference() {
			super();
		}
		
		public ByReference(Pointer pointer) {
			super(pointer);
			read();
		}
		
		public InetSocketAddress toInetAddress() {
			
			byte[] addr = new byte[sin_family == AF_INET ? 4 : sin_family == AF_INET6 ? 6 : 0];
			
			if(addr.length == 0) {
				throw new RuntimeException("Illegal family " + sin_family);
			}
			
			System.arraycopy(sin_addr, 0, addr, 0, addr.length);
			
			try {
				return new InetSocketAddress(InetAddress.getByAddress(addr), sin_port & 0xFFFF);
			} catch (UnknownHostException e) {
				throw new RuntimeException("conversion failed", e);
			}
		}
		
	}
	
	
	public static sockaddr_in.ByReference fromInetAddress(InetSocketAddress address) {
		
		sockaddr_in.ByReference struct = new sockaddr_in.ByReference();
		
		if(address.getAddress().getClass().getName().equals("java.net.Inet4Address")) {
			struct.sin_family = AF_INET;
		} else if(address.getAddress().getClass().getName().equals("java.net.Inet6Address")) {
			struct.sin_family = AF_INET6;
		} else {
			throw new RuntimeException("Illegal address class: " + address.getAddress().getClass().getName());
		}
		struct.sin_port = (short) address.getPort();
		
		ByteBuffer buf = ByteBuffer.wrap(struct.sin_addr);
		byte[] addr = address.getAddress().getAddress();
		buf.put(addr, 0, Math.min(12, addr.length));
		
		return struct;
	}
}
