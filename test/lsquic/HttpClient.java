import test.utils.HttpUtils;

/**
 * used to test the lsquic http_server with jquic
 */
public class HttpClient {
	
	// ./jquic -t java -f ../test/lsquic/HttpClient.java
	
	public static void main(String[] args) {
		
		// start http_server somewhat like this:
		// ./http_server -s localhost:1234 -c localhost,cert.crt,key.key
		
		// create client
		jquic.example.http.HttpClient client = new jquic.example.http.HttpClient();
		
		// start client
		client.start();
		
		// connect to localhost:1234
		client.connect("localhost", "localhost", 1234);
		
		// send GET / and log response
		System.out.println("GET /");
		System.out.println(client.sendRequest(HttpUtils.request("GET", "/", new byte[] {}, "user-agent", "jquic")));
		System.out.println();
		
		// send GET /1000 and log response
		System.out.println("GET /1000");
		System.out.println(client.sendRequest(HttpUtils.request("GET", "/1000", new byte[] {}, "user-agent", "jquic")));
		System.out.println();
		
		// close client
		client.close();
	}
	
}