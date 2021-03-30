package jquic.example.http.annotated;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

/**
 * Utility class that does alot of reflection and annotation magic. <br>
 * Used by {@link ResponseHandler} and {@link RequestHandler}
 *
 * @param <T> Type of whatever annotated methods must return
 * @param <A> Type of annotation methods can be annotated with
 */
public abstract class AnnotatedHandler<T, A extends Annotation> {

	/**
	 * known annotated methods (List of annotation, method and object the methods are in)
	 */
	protected List<Entry<A, Entry<Object, Method>>> methods = new ArrayList<>();
	
	/**
	 * parsers used to parse method arguments
	 */
	protected Map<Class<?>, Function<String, ?>> parsers = new HashMap<>();
	
	/**
	 * Class of method return type (used for casting so we get type errors as early as possible)
	 */
	protected Class<T> clazz;
	
	/**
	 * Class of Annotation that might be present on methods
	 */
	protected Class<A> annotationClass;

	/**
	 * Constructor
	 * @param clazz {@link #clazz}
	 * @param annotationClass {@link #annotationClass}
	 */
	public AnnotatedHandler(Class<T> clazz, Class<A> annotationClass) {
		this.clazz = clazz;
		this.annotationClass = annotationClass;
		
		parsers.put(String.class, Function.identity());
		
		parsers.put(Integer.TYPE, Integer::valueOf);
		parsers.put(Integer.class, Integer::valueOf);
		
		parsers.put(Long.TYPE, Long::valueOf);
		parsers.put(Long.class, Long::valueOf);
		
		parsers.put(BigInteger.class, BigInteger::new);
	}

	/**
	 * parse argument to target type. <br>
	 * TODO: use loggers instead of System.err
	 * @param value value before parsing
	 * @param type class of type to parse to
	 * @param <E> type to cast to
	 * @return parsed value or null if parsing is not possible.
	 */
	@SuppressWarnings("unchecked")
	public <E> E parse(String value, Class<E> type) {
		if(parsers.containsKey(type)) {
			if(type.isPrimitive()) {
				// primitive types cannot be cast using the above thingy
				return (E)parsers.get(type).apply(value);
			}
			return type.cast(parsers.get(type).apply(value));
		}
		System.err.println("No parser for type " + type.getName());
		return null;
	}

	/**
	 * Checks whether a annotated method should be invoked for a given object
	 * @param annotation annotation of the method
	 * @param value value to check
	 * @return true/false
	 */
	public abstract boolean matches(A annotation, T value);

	/**
	 * Parse parameter of an annotated method
	 * @param annotation given
	 * @param value given
	 * @param parameter parameter to parse
	 * @return parsed parameter (may be null)
	 */
	protected abstract Object parseParameter(A annotation, T value, Parameter parameter);

	/**
	 * Check signature of annotated method
	 * @param m method to check
	 * @return success/fail
	 */
	public abstract boolean checkSignature(Method m);

	/**
	 * Add Object containing annotated methods
	 * @param o object to add
	 */
	public void addObject(Object o) {
		
		for(Method m : o.getClass().getDeclaredMethods()) {
			if(m.isAnnotationPresent(annotationClass)) {
				if(checkSignature(m)) {
					methods.add(new AbstractMap.SimpleEntry<>(m.getDeclaredAnnotation(annotationClass), new AbstractMap.SimpleEntry<>(o, m)));
				}
			}
		}
	}

	/**
	 * parse parameters and invoke annotated method
	 * @param annotation annotation of method
	 * @param value object used to parse parameters
	 * @param o instance of the class containing the method
	 * @param target method to be invoked
	 * @return whatever the invoked method returned
	 */
	public T invokeMethod(A annotation, T value, Object o, Method target) {

		java.lang.reflect.Parameter[] p = target.getParameters();
		Object[] params = new Object[p.length];
		
		
		for(int i = 0; i < params.length; i++) {
			params[i] = parseParameter(annotation, value, p[i]);
		}
		
		try {
			return clazz.cast(target.invoke(o, params));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Handles value by applying correct annotated method
	 * @param value value to handle
	 * @param defaultValue value to return if no method matches
	 * @return value after applying correct method (or defaultValue of none matched)
	 */
	public T handle(T value, T defaultValue) {
		for(Entry<A, Entry<Object,Method>> possible : methods) {
			if(matches(possible.getKey(), value)) {
				Entry<Object, Method> target = possible.getValue();
				return invokeMethod(possible.getKey(), value, target.getKey(), target.getValue());
			}
		}
		return defaultValue;
	}
	
	/**
	 * Handles value by applying correct annotated method
	 * @param value value to handle
	 * @return value after putting it through the correclty annotated method or null if no method matched.
	 */
	public T handle(T value) {
		return handle(value, null);
	}
	
	
	
}
