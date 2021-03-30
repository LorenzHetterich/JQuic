/**
 * Used to test the lsquic echo_server with jquic
 */
public class EchoClient {
	
	// ./jquic -t java -f ../test/lsquic/EchoClient.java
	
	public static void main(String[] args) {
		
		// start echo server somewhat like this:
		// ./echo_server -s localhost:1234 -c localhost,cert.crt,key.key
		
		// create echo client
		jquic.example.echo.EchoClient client = new jquic.example.echo.EchoClient();
		
		// connect to localhost:1234
		client.start(-1, "localhost", 1234);
		
		// reads a line from stdin, and sends it to the server. The server should echo it back!
	}
	
}