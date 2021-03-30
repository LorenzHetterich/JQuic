package jquic.base;

import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketException;

import com.sun.jna.Pointer;

import jquic.base.engine.QuicEngine;
import lsquicbindings.struct.lsquic_out_spec;
import lsquicbindings.struct.sockaddr_in;

/**
 * Package handler for packages from DatagramServer
 */

public class PacketHandler implements Runnable {

	private final DatagramServer server;
	private QuicEngine engine;
	private boolean running;
	/**
	 * Hardcoded default timeout
	 */
	private int soTimeout = 10;

	/**
	 * Constructor
	 * @param server requiring handler
	 */
	public PacketHandler(DatagramServer server) {
		this.server = server;
	}

	/**
	 * Set QUIC engine
	 * @param engine used
	 */
	public void setEngine(QuicEngine engine) {
		this.engine = engine;
	}

	/**
	 * Sends packages over Socket
	 * @param packets_out_ctx pointer to context
	 * @param out_specs specs from native
	 * @param n_packets_out number of packages to be send
	 * @return number of packages sent
	 */
	public int packets_out(Pointer packets_out_ctx, lsquic_out_spec.ByReference out_specs, int n_packets_out) {
		int sent = 0;
		
		for(lsquic_out_spec.ByReference out_spec : (lsquic_out_spec.ByReference[])out_specs.toArray(n_packets_out)) {
			
			byte[] arr = out_spec.iov.getPointer(0).getByteArray(0, out_spec.iov.getInt(8));
			
			sockaddr_in.ByReference r = new sockaddr_in.ByReference(out_spec.dest_sa);
			
			InetSocketAddress addr = r.toInetAddress();
			
			DatagramPacket packet = new DatagramPacket(arr, arr.length, addr);
			
			if(!server.send(packet)) {
				break;
			}
			
			sent ++;
		}
		
		return sent;
	}

	/**
	 * Stop handler
	 */
	public void stop() {
		running = false;
	}

	/**
	 * Run handler
	 */
	@Override
	public void run() {

		if(running) {
			throw new RuntimeException("Packet handler is already running!");
		}

		if(engine == null) {
			throw new RuntimeException("Engine is null. Engine must be set before running!");
		}
		
		running = true;
		
		byte[] buf = new byte[Short.MAX_VALUE];
		
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		

		sockaddr_in.ByReference sa_local = sockaddr_in.fromInetAddress((InetSocketAddress) server.getAddress());
		sockaddr_in.ByReference sa_peer;
		
		
		try {
			server.getSocket().setSoTimeout(soTimeout);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		while(running) {
			if(!server.receive(packet)) {
				if(running)
					engine.process_conns();
				continue;
			}
			
			
			sa_peer = sockaddr_in.fromInetAddress((InetSocketAddress) packet.getSocketAddress());
			
			sa_local.write();
			sa_peer.write();
			engine.packet_in(buf, packet.getLength(), sa_local.getPointer(), sa_peer.getPointer(), null, 0);
			sa_local.read();
			sa_peer.read();
			
			engine.process_conns();
		}
		
		engine.process_conns();
		engine.destroy();
	}
	
}
