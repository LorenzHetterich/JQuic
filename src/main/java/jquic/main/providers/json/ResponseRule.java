package jquic.main.providers.json;

import java.util.HashMap;
import java.util.Map;

public class ResponseRule {

	// Name of rule
	public String name;

	// Content - default null
	public String content = null;
	
	public int priority = 0;

	// Match all status codes
	public String status_code = ".*";

	// Headers
	public Map<String, String> headers = new HashMap<>();

	// Replacement for rule
	public Replacement[] replace = new Replacement[] {};
	
}
