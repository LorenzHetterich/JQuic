package jquic.example.generic;

import java.net.InetSocketAddress;

import jquic.base.DatagramServer;
import jquic.base.PacketHandler;
import jquic.base.connection.QuicConnection;
import jquic.base.engine.ConnectionSupplier;
import jquic.base.engine.QuicEngine;
import lsquicbindings.Constants;

/**
 * Generic quic client. <br>
 * Handles UDP packets and engine event loop
 */
public class QuicClient {

	/**
	 * Server status
	 */
	private boolean running;

	/**
	 * Packet handler
	 */
	private PacketHandler handler;

	/**
	 * Datagram Server (@link #Datagram Server)
	 */
	private DatagramServer server;

	/**
	 * QUIC engine (@link QUIC engine)
	 */
	public QuicEngine engine;

	/**
	 * Connect to server
	 * @param version quic version to use
	 * @param host destination hostname
	 * @param port destination port
	 * @return the connection just made
	 */
	public QuicConnection connect(int version, String host, int port) {
		return engine.connect(version, (InetSocketAddress) server.getAddress(), new InetSocketAddress(host, port), null,
				null, host);
	}

	/**
	 * Connect client to server
	 * @param host of server
	 * @param port of server
	 * @return QuicConnection
	 */
	public QuicConnection connect(String host, int port) {
		return this.connect(Constants.LSQVER_ID34, host, port);
	}

	/**
	 * Start Client
	 * @param local_port used
	 * @param alpn name
	 * @param connectionSupplier used
	 */
	public void start(int local_port, String alpn, ConnectionSupplier connectionSupplier) {
		// Check if already running
		if (running) {
			throw new RuntimeException("Server is already running!");
		}

		running = true;

		// Create new Datagram Server on local port
		server = local_port == -1 ? new DatagramServer() : new DatagramServer(local_port);

		// Create packet handler for server
		handler = new PacketHandler(server);

		// Create Quic Engine
		engine = new QuicEngine(0, null, x -> x.ea_alpn = alpn, handler::packets_out, null);

		// Open connection supplier and new connection
		engine.setConnectionSupplier(connectionSupplier);

		// Set packet handler for QUIC engine
		handler.setEngine(engine);
		new Thread(handler).start();
	}

	/**
	 * Stop Client
	 */
	public void stop() {
		running = false;
		handler.stop();
		try {
			server.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
