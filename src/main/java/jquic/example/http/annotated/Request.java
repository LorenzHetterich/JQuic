package jquic.example.http.annotated;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * matches requests. <br>
 * Basically everything is regex
 */
@Retention(RUNTIME)
@Target(ElementType.METHOD)
public @interface Request {

	// Default matches everything

	/**
	 * method to match (e.g. GET)
	 */
	String method() default ".*";
	
	/**
	 * path to match (e.g. /user/([0-9]+) )
	 */
	String path() default ".*";
	
	/**
	 * headers to match. <br>
	 * each entry is a regex, if it does not contain ':', it is only matched against the header name, else it is split (at the first) ':' and matched against both, name and value.
	 */
	String[] headers() default {};
	
	/**
	 * parameters to match. <br>
	 * Works like {@link #headers()}
	 */
	String[] parameters() default {};
	
	/**
	 * priority to match annotated method. <br>
	 * annotated method that matches with highest priority will be invoked
	 */
	int priority() default 0;
}
