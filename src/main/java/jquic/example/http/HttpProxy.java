package jquic.example.http;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jquic.base.stream.QuicStream;

/**
 * Simple http proxy, that allows modifying requests and responses. <br>
 * TODO: we do not use a logger here yet and the implementation is a bit ... written in a rush ... <br>
 * @deprecated use {@link SimpleProxy} instead
 */
@Deprecated
public class HttpProxy {
	
	/**
	 * Server for Proxy
	 */
	private HttpServer server;

	/**
	 * Client connection to server
	 */
	private Map<Long, ClientConnection> clients;

	/**
	 * Constructor for Proxy
	 */
	public HttpProxy() {
		 // Uses concurrent data structure for thread based handling
		clients = new ConcurrentHashMap<>();
		// Server
		server = new HttpServer() {
			/**
			 * Gets request from client stream
			 * @param stream to write to
			 * @param request to be send
			 */
			@Override
			public void onRequest(QuicStream stream, SimpleHttpMessage request) {
				ClientConnection client = clients.get(stream.connection.getId());
				
				if(client == null) {
					client = new ClientConnection();
					clients.put(stream.connection.getId(), client);
				}
				
				HttpProxy.this.onRequest(client, stream, request);
			}
			
		};
	}

	/**
	 * Start server
	 * @param port used
	 * @param certPath used
	 * @param keyPath used
	 */
	public void start(int port, String certPath, String keyPath) {
		server.start(port, certPath, keyPath);
	}

	/**
	 * Create Clients
	 * @param request made
	 * @param sni used for indication
	 * @return HTTP Client
	 */
	public HttpClient createClient(SimpleHttpMessage request, String sni) {
		HttpClient client = new HttpClient();
		client.start();
		client.connect(sni, sni, 443); // TODO: port?
		return client;
	}

	/**
	 * Modifies requests
	 * @param client giving message
	 * @param sni naming client
	 * @param request to be modified
	 * @return modified message
	 */
	public SimpleHttpMessage modifyRequest(ClientConnection client, String sni, SimpleHttpMessage request) {
		return request;
	}

	/**
	 * Handle requests from clients to server
	 * @param client connection
	 * @param stream to read from
	 * @param request to be read
	 */
	public void onRequest(ClientConnection client, QuicStream stream, SimpleHttpMessage request) {
		System.out.println("CLIENT -> PROXY");

		String method = request.firstLine.substring(0, request.firstLine.indexOf(" "));
		String path = request.firstLine.substring(method.length() + 1, request.firstLine.indexOf(" ", method.length() + 1));
		
		String sni = stream.connection.sni;
		
		request.headers.putFirst(":authority", sni);
		request.headers.putFirst(":path", path);
		request.headers.putFirst(":scheme", "https");
		request.headers.putFirst(":method", method);		
		
		HttpClient toSend = client.connections.get(sni);
		
		if(toSend == null) {
			toSend = createClient(request, sni);
			toSend.onConnClosed = () -> client.connections.remove(sni);
			client.connections.put(sni, toSend);
		}
		
		SimpleHttpMessage modified = modifyRequest(client,sni,request);
		
		onResponse(client, stream, sni, modified, toSend.sendRequest(modified));
	}

	/**
	 * Modifies response at will
	 * @param client connection
	 * @param sni not used here
	 * @param request to be modified
	 * @param response message
	 * @return modified message
	 */
	public SimpleHttpMessage modifyResponse(ClientConnection client, String sni, SimpleHttpMessage request, SimpleHttpMessage response) {
		return response;
	}

	/**
	 * Response handling
	 * @param client connection
	 * @param stream listened
	 * @param sni
	 * @param request made
	 * @param response returned
	 */
	public void onResponse(ClientConnection client, QuicStream stream, String sni, SimpleHttpMessage request, SimpleHttpMessage response) {
		System.out.println("PROXY -> CLIENT");
		
		if(response != null) {
			response.headers.putFirst(":status", response.firstLine.split(" ")[2]);
		}
		
		SimpleHttpMessage modified = modifyResponse(client, sni, request, response);
		
		if(modified == null) {
			System.err.println("Got null response for request!");
			return;
		}
		
		server.sendResponse(stream, modified);
	}
	
	/**
	 * Connection from client to proxy
	 */
	public class ClientConnection {
	
		/**
		 * clients for different hosts
		 */
		public Map<String, HttpClient> connections;

		/**
		 * Constructor. <br>
		 * initialize {@link #connections} with an empty, but thread safe map.
		 */
		public ClientConnection() {
			connections = new ConcurrentHashMap<>();
		}
		
	}
	
}
