package jquic.example.generic;

import static lsquicbindings.Constants.LSENG_SERVER;

import com.sun.jna.Pointer;

import boringsslbindings.BoringSslHelper;
import jquic.base.DatagramServer;
import jquic.base.PacketHandler;
import jquic.base.engine.ConnectionSupplier;
import jquic.base.engine.QuicEngine;

/**
 * Generic quic server. <br>
 * Handles UDP packets and engine event loop
 */
public class QuicServer {

	/**
	 * Datagram Server
	 */
	private DatagramServer server;
	
	/**
	 * Packet handler for connection
	 */
	private PacketHandler handler;
	
	/**
	 * Pointer to SSL context
	 */
	private Pointer sslContext;
	
	/**
	 * Server Status
	 */
	private boolean running;
	
	/**
	 * Helper Class for doing SSL stuff
	 */
	private BoringSslHelper ssl;
	
	/**
	 * Engine for QUIC connection
	 */
	public QuicEngine engine;

	/**
	 * Start server
	 * @param port used by server
	 * @param certPath path to SSL certificate used by server
	 * @param keyPath path to key
	 */
	public void start(int port, String certPath, String keyPath, String alpn, ConnectionSupplier connectionSupplier) {
		// Check status
		if(running) {
			throw new RuntimeException("Server is already running!");
		}

		running = true;

		// Create Datagram Server
		this.server = new DatagramServer(port);

		// Create BoringSSL Helper, SSL Context and Certificate
		ssl = new BoringSslHelper();
		ssl.addAlpn(alpn);
		this.sslContext = ssl.createContext();
		ssl.setupCertivicatePEM(this.sslContext, certPath, keyPath);

		// Create Package Handler
		this.handler = new PacketHandler(server);

		// Create QUIC engine
		engine = new QuicEngine(LSENG_SERVER, null, null, handler::packets_out, (a,b) -> sslContext);

		// Create new connection
		engine.setConnectionSupplier(connectionSupplier);

		// Set handler for engine
		handler.setEngine(engine);
		new Thread(handler).start();
		
	}

	/**
	 * Stop server
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
