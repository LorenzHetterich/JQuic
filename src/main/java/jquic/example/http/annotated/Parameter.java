package jquic.example.http.annotated;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

/**
 * matches a specific parameter name and injects value of that (http) parameter for annotated (method) parameter
 */
@Retention(RUNTIME)
public @interface Parameter {

	/**
	 * (http) parameter name
	 */
	public String value();
	
}
