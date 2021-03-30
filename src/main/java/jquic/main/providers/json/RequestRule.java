package jquic.main.providers.json;

import java.util.HashMap;
import java.util.Map;

public class RequestRule {

	// Name of rule
	public String name;

	// Content null per default
	public String content = null;
	
	public int priority = 0;

	// Match all paths and methods
	public String path = ".*", method = ".*";

	// Headers
	public Map<String, String> headers = new HashMap<>(), parameters = new HashMap<>();

	// Replacements for rule
	public Replacement[] replace = new Replacement[] {};
}
