package test.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import test.Test;

/**
 * uses Annotations and reflection to execute tests
 */
public class TestExecutor {

	private List<Class<?>> classes = new ArrayList<>();

	// Add test class
	public void addClass(Class<?> clazz) {
		classes.add(clazz);
	}

	// Run tests and print results in fancy DIY print based format
	public int runTests() {
		
		int passed = 0, 
			failed = 0;
		
		for(Class<?> clazz : classes) {
			for(Method m : clazz.getMethods()) {
				if(m.isAnnotationPresent(Test.class)) {
					
					Test test = m.getAnnotation(Test.class);
					try {
						System.out.println("############################");
						System.out.println(" ".repeat(13 - (test.value().length() + 1) / 2) + "[" + test.value() + "]" + " ".repeat(13 - test.value().length() / 2));
						System.out.println("############################");
						m.invoke(null);
						passed ++;
						System.out.println();
						System.out.println("SUCCESS");
						System.out.println();
					} catch(InvocationTargetException e) {
						failed ++;
						System.out.println();
						System.out.println("FAILED");
						System.err.println(e.getCause().getClass().getName() + ": " + e.getCause().getMessage());
						e.getCause().printStackTrace();
						System.out.println();
					} catch(Throwable e) {
						failed ++;
						System.out.println();
						System.out.println("FAILED");
						System.err.println(e.getClass().getName() + ": " + e.getMessage());
						e.printStackTrace();
						System.out.println();
					}
					
					// wait a little bit to make sure socket is closed
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
			}
		}
		
		System.out.println();
		System.out.println("--------------------");
		System.out.printf("passed: %d\nfailed: %d", passed, failed);
		System.out.println();
		
		return failed;
	}
	
}
