package jquic.example.http.annotated;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation to match http responses. <br>
 * basically everything is a regex
 */
@Retention(RUNTIME)
@Target(ElementType.METHOD)
public @interface Response {

	// Default matches everthing

	/**
	 * status code (e.g. 2..)
	 */
	String status_code() default ".*";
	
	/**
	 * response headers. <br>
	 * Works like {@link Request#headers()}
	 * @return
	 */
	String[] headers() default {};
	
	/**
	 * priority. <br>
	 * annotated method that matches with highest priority will be invoked
	 */
	int priority() default 0;
	
}
