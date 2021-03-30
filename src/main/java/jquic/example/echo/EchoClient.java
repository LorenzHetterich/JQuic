package jquic.example.echo;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.util.Scanner;

import com.sun.jna.Pointer;

import jquic.base.DatagramServer;
import jquic.base.PacketHandler;
import jquic.base.connection.QuicConnection;
import jquic.base.engine.QuicEngine;
import jquic.base.stream.QuicStream;
import lsquicbindings.Constants;

/**
 * Example EchoClient implementation
 */
public class EchoClient {
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
	private QuicEngine engine;

	/**
	 * input to read from
	 */
	public InputStream in = System.in;
	
	/**
	 * output to write to
	 */
	public PrintStream out = System.out;
	
	/**
	 * Start echo client
	 * @param local_port to be used
	 * @param host of client
	 * @param port to used
	 */
	public void start(int local_port, String host, int port) {
		// Check if already running
		if(running) {
			throw new RuntimeException("Server is already running!");
		}
		
		running = true;

		// Create new Datagram Server on local port
		server = local_port == -1 ? new DatagramServer() : new DatagramServer(local_port);

		// Create packet handler for server
		handler = new PacketHandler(server);

		// Create Quic Engine
		engine = new QuicEngine(0, null, x -> x.ea_alpn = "echo", handler::packets_out, null);

		// Open connection supplier and new connection
		engine.setConnectionSupplier(EchoClientConnection::new);

		// Set packet handler for QUIC engine
		handler.setEngine(engine);
		new Thread(handler).start();

		// build connection
		engine.connect(Constants.LSQVER_ID34, (InetSocketAddress) server.getAddress(), new InetSocketAddress(host, port), null, null, host);
	}
	
	/**
	 * Quic connection implementation used for {@link EchoClient}
	 */
	public class EchoClientConnection extends QuicConnection {
		/**
		 * Constructor for QUIC Connection on client side
		 * @param id of connection
		 * @param connection used
		 * @param engine used by client
		 */
		public EchoClientConnection(long id, Pointer connection, QuicEngine engine) {
			super(id, connection, engine);
			// Create new QUIC stream
			this.setStreamSupplier(EchoClientStream::new);
			engine.make_stream(connection);
		}
		
		
	}
	
	/**
	 * Quic stream implementation used for {@link EchoClient}
	 */
	public class EchoClientStream extends QuicStream implements Runnable{
		/**
		 * Constructor for Client Stream
		 * @param id of connection
		 * @param stream used
		 * @param connection used
		 */
		public EchoClientStream(long id, Pointer context, Pointer stream, QuicConnection connection) {
			super(id, context, stream, connection);
			new Thread(this).start();
		}

		/**
		 * Run EchoClient
		 */
		@Override
		public void run() {
			Scanner stdin = new Scanner(EchoClient.this.in);
			Scanner in = new Scanner(this.in);
			PrintWriter writer = new PrintWriter(out);
			while (stdin.hasNextLine()) {
				writer.println(stdin.nextLine());
				writer.flush();

				if (!in.hasNextLine() || !running) {
					break;
				}

				EchoClient.this.out.println(in.nextLine());
				EchoClient.this.out.flush();
			}
			running = false;
			
			in.close();
			stdin.close();
			writer.close();
		}

		/**
		 * Close Echo client
		 */
		@Override
		public void onClosed() {
			super.onClosed();
			running = false;
			handler.stop();
			try {
				server.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	/**
	 * Stop Client
	 */
	public void stop() {
		running = false;
		try {
			server.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		handler.stop();
	}
	
}
