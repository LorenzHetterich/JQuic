package jquic.example.http;

import static lsquicbindings.Constants.LSENG_HTTP;
import static lsquicbindings.Constants.LSQVER_ID34;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.sun.jna.Pointer;

import jquic.base.DatagramServer;
import jquic.base.PacketHandler;
import jquic.base.connection.QuicConnection;
import jquic.base.engine.QuicEngine;
import jquic.base.stream.QuicStream;

/**
 * Simple HttpClient. <br>
 * Can {@link #connect(int, String, String, int) connect} to a server and {@link #sendRequest(SimpleHttpMessage, long, TimeUnit) send requests}. <br>
 * Before connecting, it needs to be {@link #start() started}
 */
public class HttpClient {
	
	/**
	 * Datagram Server
	 */
	private DatagramServer server;
	
	/**
	 * Handler for packages
	 */
	private PacketHandler handler;
	
	/**
	 * Used QUIC engine
	 */
	private QuicEngine engine;
	
	/**
	 * false if the client is not running
	 */
	private boolean running;

	/**
	 * Used connection
	 */
	public QuicConnection connection;

	/**
	 * List used for queuing outgoing requests
	 */
	private LinkedList<SimpleHttpMessage> send_queue;
	
	/**
	 * Map used for temporary storing requests until they are processed
	 */
	private Map<SimpleHttpMessage, ResponseHelper> response_buf;

	/**
	 * Runnable called on connection close
	 */
	public Runnable onConnClosed;

	/**
	 * Constructor
	 */
	public HttpClient() {
		send_queue = new LinkedList<>();
		response_buf = new ConcurrentHashMap<>();
	}

	/**
	 * Server pushes not implemented - returns error
	 * @param stream to push on
	 */
	public void handlePush(QuicStream stream) {
		throw new RuntimeException("TODO: Implement server push");
	}

	/**
	 * stop client
	 */
	public void close() {
		try {
			server.close();
		} catch(Exception e) {
			
		}
		handler.stop();
	}

	/**
	 * Send next request
	 * @param stream used for sending
	 */
	public void sendRequest(QuicStream stream) {
		// Request as HTTP message
		final SimpleHttpMessage request;
		// Send requests stored in queue
		// synchronized for thread based handling
		synchronized(send_queue) {
			if(send_queue.isEmpty()) {
				throw new RuntimeException("Trying to send request with empty request queue");
			}
			request = send_queue.pop();
		}
		// send headers
		stream.sendHeaders(request.headers);
		
		// check if request contains data and write to stream
		if(request.hasData()) {
			stream.out.write(request.data);
		}
		engine.stream_shutdown(stream.getNative(), 1);
		
		// Response to request
		SimpleHttpMessage response;

		// parse response from stream
		try {
			response = SimpleHttpMessage.parse(stream);
		} catch(Exception e) {
			// error reading response
			response = null;
		}

		// ReponseHelper processes response
		ResponseHelper helper = response_buf.remove(request);
		// locking required for multithreading
		try {
			helper.lock.lock();
			helper.response = response;
			helper.done = true;
			helper.responseReady.signal();
		} finally {
			helper.lock.unlock();
		}
		engine.close_stream(stream.getNative());
	}

	/**
	 * Send Request
	 * @param request HTTP message
	 * @param timeout value
	 * @param unit for time
	 * @return response from server or null if timeout is reached
	 */
	public SimpleHttpMessage sendRequest(SimpleHttpMessage request, long timeout, TimeUnit unit) {
		// Check if connection exists
		if(connection == null) {
			throw new RuntimeException("Open connection first!");
		}
		// Create ResponseHelper and add request to response buffer
		ResponseHelper helper = new ResponseHelper();		
		response_buf.put(request, helper);

		// synchronized adding to send queue
		synchronized(send_queue) {
			send_queue.add(request);
		}
		// Create Stream
		engine.make_stream(connection.getNative());

		// Lock helper until processing of response is done
		try {
			helper.lock.lock();
			
			while(!helper.done)
				try {
					if(timeout != -1) {
						if(!helper.responseReady.await(timeout, unit)) {
							// time limit exceeded
							return null;
						}
					} else {
						helper.responseReady.await();
					}
				} catch (InterruptedException e) {
					// Interrupted
					return null;
				}
			return helper.response;
		} finally {
			helper.lock.unlock();
		}
	}

	/**
	 * Send request with millisecond timeout
	 * @param request to be send
	 * @param timeout for request
	 * @return response from server
	 */
	public SimpleHttpMessage sendRequest(SimpleHttpMessage request, long timeout) {
		return sendRequest(request, timeout, TimeUnit.MILLISECONDS);
	}

	/**
	 * Send request (without timeout)
	 * @param request to be send
	 * @return response from server
	 */
	public SimpleHttpMessage sendRequest(SimpleHttpMessage request) {
		return sendRequest(request, -1, null);
	}

	/**
	 * Start HTTPClient
	 */
	public void start() {
		// Check status
		if (running) {
			throw new RuntimeException("Server is already running!");
		}

		running = true;
		// Create new Datagram Server
		this.server = new DatagramServer();

		//Create Package handler
		this.handler = new PacketHandler(server);

		// Create QUIC engine
		engine = new QuicEngine(LSENG_HTTP, null, null, handler::packets_out, null);

		// Create Connection
		engine.setConnectionSupplier(ClientConnection::new);

		// Set handler to handle package on engine
		handler.setEngine(engine);
		new Thread(handler).start();

	}

	/**
	 * Connect to server
	 * @param host hostname of server
	 * @param sni server name indication to use
	 * @param port port to use
	 */
	public void connect(String host, String sni, int port) {
		engine.connect(LSQVER_ID34, (InetSocketAddress) server.getAddress(), new InetSocketAddress(host, port), null, null, sni);
	}
	
	/**
	 * Connect to server
	 * @param draft quic draft to use
	 * @param host hostname of server
	 * @param sni server name indication to use
	 * @param port port to use
	 */
	public void connect(int draft, String host, String sni, int port) {
		engine.connect(draft, (InetSocketAddress) server.getAddress(), new InetSocketAddress(host, port), null, null, sni);
	}

	/**
	 * class that contains things required for seemingly synchronous sending of requests (and reading of responses)
	 */
	public class ResponseHelper {
		
		/**
		 * user for locking (obviously xD)
		 */
		public final Lock lock;
		
		/**
		 * Signaled once the response is ready
		 */
		public final Condition responseReady;
		
		/**
		 * response 
		 */
		public SimpleHttpMessage response;
		
		/**
		 * true, if the response was received / waiting was interrupted
		 */
		public boolean done;
		
		/**
		 * thread that is waiting for response
		 */
		public Thread thread;

		/**
		 * Constructor. <br>
		 * Initializes everything required for thread safety.
		 */
		public ResponseHelper() {
			lock = new ReentrantLock();
			responseReady = lock.newCondition();
			thread = Thread.currentThread();
		}
		
	}
	
	/**
	 * Connection used to make streams to send requests
	 */
	public class ClientConnection extends QuicConnection {
		
		/**
		 * Constructor for Client Connection
		 * @param id of connection
		 * @param connection pointer
		 * @param engine used
		 */
		public ClientConnection(long id, Pointer connection, QuicEngine engine) {
			super(id, connection, engine);
			HttpClient.this.connection = this;
			this.setStreamSupplier(HttpClientStream::new);
		}

		/**
		 * Close connection. <br>
		 * cancels all requests that are currently sent.
		 */
		@Override
		public void onClosed() {
			if(onConnClosed != null && connection == this) {
				
				response_buf.forEach((k,v) -> {
					v.done = true;
					v.thread.interrupt();
				});
				
				onConnClosed.run();
			}
		}

	}
	
	/**
	 * Streams used to send requests. <br>
	 * For now there is one request per stream.
	 */
	public class HttpClientStream extends QuicStream {
		
		/**
		 * Constructor for Client Stream
		 * @param id of connection
		 * @param context pointer to native context
		 * @param stream used
		 * @param connection pointer
		 */
		public HttpClientStream(long id, Pointer context, Pointer stream, QuicConnection connection) {
			super(id, context, stream, connection);
			if(engine.lsquic_stream_is_pushed(stream)) {
				new Thread(() -> handlePush(this)).start();
			} else {
				new Thread(() -> sendRequest(this)).start();
			}
		}

	}

}
