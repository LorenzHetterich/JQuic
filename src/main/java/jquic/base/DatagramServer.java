package jquic.base;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
 
/**
 * used for sending and receiving UDP packets
 */
public class DatagramServer implements AutoCloseable{

	/**
	 * underlying java socket
	 */
	private DatagramSocket socket;
	
	/**
	 * Constructor <br>
	 * Opens a DatagramSocket
	 */
	public DatagramServer() {
		try {
			socket = new DatagramSocket();
		} catch (SocketException e) {
			throw new RuntimeException("Could not open socket", e);
		}
	}
	
	/**
	 * Constructor <br>
	 * Opens a DatagramSocket
	 * @param port to listen on
	 */
	public DatagramServer(int port) {
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			throw new RuntimeException("Could not open socket", e);
		}
	}
	
	/**
	 * Constructor <br>
	 * Creates a DatagramSocket and connects to a remote host
	 * @param host to connect to
	 * @param port of host
	 */
	public DatagramServer(String host, int port) {
		try {
			socket = new DatagramSocket();
			socket.connect(InetAddress.getByName(host), port);
		} catch (SocketException | UnknownHostException e) {
			throw new RuntimeException("Could not open socket", e);
		}
	}

	/**
	 * Gets the local address of the {@link #socket}
	 * @return local address
	 */
	public SocketAddress getAddress() {
		return socket.getLocalSocketAddress();
	}
	
	/**
	 * Getter for {@link #socket}
	 * @return {@link #socket}
	 */
	public DatagramSocket getSocket() {
		return socket;
	}
	
	/**
	 * receives a DatagramPacket
	 * @param packet to receive to
	 * @return true on success, false on error
	 */
	public boolean receive(DatagramPacket packet) {
		try {
			socket.receive(packet);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	/**
	 * sends a DatagramPacket
	 * @param packet to send
	 * @return true on success, false on error
	 */
	public boolean send(DatagramPacket packet) {
		try {
			socket.send(packet);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
	
	/**
	 * closes the underlying socket
	 */
	@Override
	public void close() throws Exception {
		if(socket != null && !socket.isClosed())
			socket.close();
	}
	
}
