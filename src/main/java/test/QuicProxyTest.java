package test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicBoolean;

import jquic.example.echo.EchoClient;
import jquic.example.echo.EchoServer;
import jquic.example.generic.QuicProxy;
import logging.Logger;

/**
 * Tests for {@link jquic.example.generic.QuicProxy}
 */
public class QuicProxyTest {

	@Test("Echo Proxy Test")
	public static void TestEchoProxy() throws Exception {
		// A logger is all we need
		Logger logger = new Logger("Echo Proxy Test");
		
		logger.info("starting server");
		EchoServer server = new EchoServer();
		server.start(4000, "../testcert/cert.crt", "../testcert/key.key");
		
		logger.info("starting proxy");
		QuicProxy proxy = new QuicProxy();
		proxy.destPort = 4000;
		proxy.start(4001, "echo", "../testcert/cert.crt", "../testcert/key.key");
		
		logger.info("starting client");
		EchoClient client = new EchoClient();
		client.in = new ByteArrayInputStream("Hello World\n".getBytes());
		
		AtomicBoolean bool = new AtomicBoolean();
		
		client.out = new PrintStream(new OutputStream() {

			StringBuilder str = new StringBuilder();
			
			@Override
			public void write(int b) throws IOException {
				if(b == '\r')
					return;
				str.append((char)b);
			}
			
			@Override
			public void flush() {
				if(!str.toString().equals("Hello World\n"))
					throw new RuntimeException(String.format("Expected Hello World, got %s", str));
				bool.set(true);
			}
			
		});
		
		client.start(-1, "localhost", 4001);
		
		while(!bool.get()) {
			Thread.sleep(100);
		}
		
		server.stop();
		proxy.stop();
		client.stop();
	}
	
}
