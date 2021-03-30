package jquic.example.echo;

import static lsquicbindings.Constants.LSENG_SERVER;

import java.util.Scanner;

import com.sun.jna.Pointer;

import boringsslbindings.BoringSslHelper;
import jquic.base.DatagramServer;
import jquic.base.PacketHandler;
import jquic.base.connection.QuicConnection;
import jquic.base.engine.QuicEngine;
import jquic.base.stream.QuicStream;

/**
 * Example EchoServer implementation
 */
public class EchoServer {
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
	private QuicEngine engine;

	/**
	 * Start server
	 * @param port used by server
	 * @param certPath path to SSL certificate used by server
	 * @param keyPath path to key
	 */
	public void start(int port, String certPath, String keyPath) {
		// Check status
		if(running) {
			throw new RuntimeException("Server is already running!");
		}

		running = true;

		// Create Datagram Server
		this.server = new DatagramServer(port);

		// Create BoringSSL Helper, SSL Context and Certificate
		ssl = new BoringSslHelper();
		ssl.addAlpn("echo");
		this.sslContext = ssl.createContext();
		ssl.setupCertivicatePEM(this.sslContext, certPath, keyPath);

		// Create Package Handler
		this.handler = new PacketHandler(server);

		// Create QUIC engine
		engine = new QuicEngine(LSENG_SERVER, null, null, handler::packets_out, (a,b) -> sslContext);

		// Create new connection
		engine.setConnectionSupplier(EchoServerConnection::new);

		// Set handler for engine
		handler.setEngine(engine);
		new Thread(handler).start();
		
	}
	
	/**
	 * stops the EchoServer
	 */
	public void stop() {
		if(!running)
			return;
		running = false;
		handler.stop();
		try {
			server.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Quic connection implementation used for {@link EchoServer}
	 */
	public class EchoServerConnection extends QuicConnection {
		/**
		 * Constructor for EchoServer Connection and start new stream
		 * @param id of connection
		 * @param connection used
		 * @param parent engine
		 */
		public EchoServerConnection(long id, Pointer connection, QuicEngine parent) {
			super(id, connection, parent);
			this.setStreamSupplier(EchoServerStream::new);
		}
		
	}
	
	/**
	 * Quic stream implementation used for {@link EchoServer}
	 */
	public class EchoServerStream extends QuicStream {

		/**
		 * Status of stream
		 */
		private boolean active;

		/**
		 * Constructor for Server Stream
		 * @param id of connection
		 * @param context pointer to native context
		 * @param stream used
		 * @param parent engine
		 */
		public EchoServerStream(long id, Pointer context, Pointer stream, QuicConnection parent) {
			super(id, context, stream, parent);
		}

		/**
		 * Open stream
		 */
		@Override
		public void onOpened() {
			
			this.active = true;
			
			new Thread(() -> {
				
				Scanner scanner = new Scanner(this.in);
				
				while(running && active) {
					
					while(scanner.hasNextLine()) {
						String line = scanner.nextLine();
						this.out.write((line + "\n").getBytes());
						this.out.flush();
						
						if(!running || !active) {
							break;
						}
					}
					
				}
				
				scanner.close();
			}).start();
		}

		/**
		 * Close stream
		 */
		@Override
		public void onClosed() {
			super.onClosed();
			active = false;
		}
		
	}
}
