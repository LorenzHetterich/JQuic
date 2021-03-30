package jquic.example.http.annotated;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

/**
 * matches a specific header name and injects value of that header for annotated parameter
 */
@Retention(RUNTIME)
public @interface Header {

	/**
	 * header name
	 */
	public String value();
	
}
