/**
 * Used to test the lsquic echo_client with jquic
 */
public class EchoServer {
	
	// ./jquic -t java -f ../test/lsquic/EchoServer.java
	
	public static void main(String[] args) {
		
		// start echo client somewhat like this:
		// ./echo_client -s localhost:1234 -H localhost
		
		// create echo client
		jquic.example.echo.EchoServer server = new jquic.example.echo.EchoServer();
		
		// start server on port 1234
		server.start(1234, "../testcert/cert.crt", "../testcert/key.key");
		
		// reads a line from incoming streams and echos it back
	}
	
}