package test;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

/**
 * annotation to mark a method as test. <br>
 * Classes containing Tests need to be registered first in {@link Main#main(String[])}
 */
@Retention(RUNTIME)
public @interface Test {

	/**
	 * name of the test
	 */
	String value() default "unnamed test";
	
}
