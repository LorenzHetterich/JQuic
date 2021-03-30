package jquic.example.http.annotated;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

/**
 * specifies a group of a match of the path and injects value of that group for annotated parameter
 */
@Retention(RUNTIME)
public @interface Path {

	/**
	 * group match index for path regex
	 */
	public int value() default 0;
	
}
