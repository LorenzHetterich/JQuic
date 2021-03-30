package jquic.example.http.annotated;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

/**
 * injects status code into annotated parameter
 */
@Retention(RUNTIME)
public @interface StatusCode {

}
