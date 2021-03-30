package jquic.example.http.annotated;

import java.lang.reflect.Method;

import jquic.example.http.ResponseLine;
import jquic.example.http.SimpleHttpMessage;

/**
 * Handles responses and invokes methods based on annotations
 */
public class ResponseHandler extends AnnotatedHandler<SimpleHttpMessage, Response>{

	/**
	 * Constructor
	 */
	public ResponseHandler() {
		super(SimpleHttpMessage.class, Response.class);
	}

	/**
	 * Matches request on annotation
	 * @param annotation
	 * @param request
	 * @return
	 */
	@Override
	public boolean matches(Response annotation, SimpleHttpMessage request) {
		ResponseLine response = request.getResponseLine();

		// Check if status code matches
		if(!Integer.toString(response.status_code).matches(annotation.status_code())) {
			return false;
		}

		// Check headers, especially name, value...
		for(String s: annotation.headers()) {
			if(s.contains(":")) {
				String name = s.substring(0, s.indexOf(":"));
				if(!request.headers.contains(name)) {
					return false;
				}
				String value = s.substring(name.length()+1);
				if(!request.headers.get(name).matches(value)) {
					return false;
				}
			} else {
				if(!request.headers.contains(s)) {
					return false;
				}
			}
		}
		// Everything is fine
		return true;
	}

	/**
	 * Parse parameters using fancy reflections
	 * @param annotation specifies param
	 * @param request HTTP msg
	 * @param parameter params
	 * @return request or null
	 */
	@Override
	protected Object parseParameter(Response annotation, SimpleHttpMessage request, java.lang.reflect.Parameter parameter) {

		// Get type
		Class<?> targetType = parameter.getType();

		// Check status code
		if(parameter.getAnnotation(Path.class) != null) {
			return parse(Integer.toString(request.getResponseLine().status_code), targetType);
		}

		// Get header
		Header header = parameter.getAnnotation(Header.class);

		// Parse header
		if(header != null) {
			return parse(request.headers.get(header.value()), targetType);
		}

		// If type matches return request
		if(targetType == SimpleHttpMessage.class) {
			return request;
		}
		
		System.err.println("Could not instanciate parameter " + parameter.getName());
		
		return null;
	}

	/**
	 * Add object
	 * @param o obj
	 */
	@Override
	public void addObject(Object o) {
		super.addObject(o);

		methods.sort((a,b) -> Integer.compare(b.getKey().priority(), a.getKey().priority()));
	}

	/**
	 * Check signature
	 * @param m method
	 * @return true/false
	 */
	@Override
	public boolean checkSignature(Method m) {
		if(clazz.isAssignableFrom(m.getReturnType())) {
			return true;
		}
		System.err.printf("[WARNING]: Ignoring method %s with invalid signature!\n", m.getName());
		return false;
	}

}
