package jquic.example.http.annotated;

import java.lang.reflect.Method;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jquic.example.http.RequestLine;
import jquic.example.http.SimpleHttpMessage;

/**
 * Handles requests and invokes methods based on annotations
 */
public class RequestHandler extends AnnotatedHandler<SimpleHttpMessage, Request>{

	/**
	 * Constructor
	 */
	public RequestHandler() {
		super(SimpleHttpMessage.class, Request.class);
	}

	/**
	 * Method matching request processing methods on annotation basis
	 * @param annotation of request
	 * @param request HTTP msg
	 * @return true if matching found, false else
	 */
	@Override
	public boolean matches(Request annotation, SimpleHttpMessage request) {
		RequestLine requestline = request.getRequestLine();

		// Search for method matching annotation
		if(!requestline.method.matches(annotation.method())) {
			return false;
		}
		// Search for matching path
		if(!requestline.path.matches(annotation.path())) {
			return false;
		}
		// Check header, especially name, length, values
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
		// Check given parameters
		for(String s: annotation.parameters()) {
			// Check if params provided
			if(s.contains(":")) {
				// Look for query name
				String name = s.substring(0, s.indexOf(":"));
				if(!requestline.hasQuery(name)) {
					return false;
				}
				// Check length
				String value = s.substring(name.length()+1);
				if(!requestline.getQuery(name).matches(value)) {
					return false;
				}
			} else {
				// Check query
				if(!requestline.hasQuery(s)) {
					return false;
				}
			}
		}
		// Everything is fine
		return true;
	}

	/**
	 * Parse parameters using fancy reflections <br>
	 * TODO: use logger
	 * @param annotation specifies param used
	 * @param request HTTP msg
	 * @param parameter listed
	 * @return
	 */
	@Override
	protected Object parseParameter(Request annotation, SimpleHttpMessage request, java.lang.reflect.Parameter parameter) {
		RequestLine requestline = request.getRequestLine();

		//Get path and type
		Path path = parameter.getAnnotation(Path.class);
		Class<?> targetType = parameter.getType();

		// Match and parse values of params
		if(path != null) {
			Matcher matcher = Pattern.compile(annotation.path()).matcher(request.getRequestLine().path);
			matcher.find();
			MatchResult paths = matcher.toMatchResult();
			return parse(paths.group(path.value()), targetType);
		}

		// Get header
		Header header = parameter.getAnnotation(Header.class);

		// Parse headers
		if(header != null) {
			return parse(request.headers.get(header.value()), targetType);
		}

		// Get params
		Parameter param = parameter.getAnnotation(Parameter.class);

		// Parse parameters
		if(param != null) {
			return parse(requestline.getQuery(param.value()), targetType);
		}

		// If it's a HTTP msg return request
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
	 * Check signature. <br>
	 * TODO: use logger
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
