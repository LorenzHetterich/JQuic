package test.utils;

import java.util.Arrays;
import java.util.Collection;

import jquic.example.http.RequestLine;
import jquic.example.http.ResponseLine;
import jquic.example.http.SimpleHttpMessage;

/**
 * Assertions reloaded for specific usage
 */
public class Assertions {

	public static void assertEquals(String message, Object a, Object b) {

		if (a == null && b != null || a != null && b == null || a != null && !a.equals(b)) {
			throw new AssertionError(String.format("%s: Expected %s but got %s", message, a, b));
		}
	}

	public static <T> void assertArrayEquals(String message, T[] expected, T[] got) {

		if (expected == null && got != null || expected != null && got == null || !Arrays.equals(expected, got)) {
			throw new AssertionError(message);
		}

	}

	public static void assertArrayEquals(String message, byte[] expected, byte[] got) {

		if (expected == null && got != null || expected != null && got == null || !Arrays.equals(expected, got)) {
			throw new AssertionError(message);
		}

	}

	public static void assertContains(String message, Object o, Collection<?> collection) {
		if (!collection.contains(o)) {
			throw new AssertionError(message);
		}
	}

	public static void assertHttp(SimpleHttpMessage expected, SimpleHttpMessage got, boolean request) {

		// reparse first line just to be sure
		if(request) {
			expected.firstLine = new RequestLine(expected.firstLine).toString();
			got.firstLine = new RequestLine(got.firstLine).toString();
		} else {
			expected.firstLine = ResponseLine.parse(expected.firstLine).toString();
			got.firstLine = ResponseLine.parse(got.firstLine).toString();
		}
		
		if (expected != null && got == null || expected == null && got != null)
			throw new AssertionError("One HttpMessage was null, the other one not");

		assertEquals("First line does not match!", expected.firstLine, got.firstLine);

		for (String header : expected.headers.keys) {
			// ignore meta headers
			if (header.startsWith(":"))
				continue;

			assertContains("Header '" + header + "' is missing", header, got.headers.keys);
			assertEquals("Header '" + header + "' does not match", expected.headers.get(header),
					got.headers.get(header));
		}

		for (String header : got.headers.keys) {
			// ignore meta headers
			if (header.startsWith(":"))
				continue;
			
			assertContains("Header '" + header + "' is too much", header, expected.headers.keys);
		}

		assertArrayEquals("data does not match", expected.data, got.data);

	}
	
}
