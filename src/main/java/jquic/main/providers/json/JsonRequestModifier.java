package jquic.main.providers.json;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jquic.example.http.HttpHeaders;
import jquic.example.http.RequestLine;
import jquic.example.http.SimpleHttpMessage;
import logging.Logger;

public class JsonRequestModifier {

	private List<RequestRule> rules = new ArrayList<>();

	private Logger logger = new Logger(this);

	/**
	 * Add rules to list
	 * @param rules to be added
	 */
	public void addRules(RequestRule[] rules) {
		for (RequestRule rule : rules) {
			logger.debug("Adding rule %s", rule.name);
			this.rules.add(rule);
		}
		// Sort by priority
		this.rules.sort((a, b) -> Integer.compare(b.priority, a.priority));
	}

	/**
	 * Check if rule match
	 * @param request to be processed
	 * @param rule to be applied
	 * @return true if rule matches else false
	 */
	public boolean doesMatch(SimpleHttpMessage request, RequestRule rule) {
		RequestLine line = request.getRequestLine();

		// Check if method matches
		if (!Pattern.compile(rule.method).matcher(line.method).find())
			return false;

		// Check if path matches
		if (!Pattern.compile(rule.path).matcher(line.path).find())
			return false;

		// Check if header matches on name/value
		for (Entry<String, String> header : rule.headers.entrySet()) {

			Pattern name = Pattern.compile(header.getKey());
			Pattern value = Pattern.compile(header.getValue());

			if (!request.headers.values.stream()
					.anyMatch(x -> name.matcher(x.getKey()).find() && value.matcher(x.getValue()).find()))
				return false;

		}

		// Check if parameters are matching
		for (Entry<String, String> parameter : rule.parameters.entrySet()) {

			Pattern name = Pattern.compile(parameter.getKey());
			Pattern value = Pattern.compile(parameter.getValue());

			if (!line.queries.stream()
					.anyMatch(x -> name.matcher(x.getKey()).find() && value.matcher(x.getValue()).find()))
				return false;
		}

		// Check if content matches
		if(rule.content != null) {
			if(!request.hasData())
				return false;
			
			if(!Pattern.compile(rule.content).matcher(new String(request.data)).find())
				return false;
		}

		// Everything is fine
		return true;
	}

	/**
	 * Apply replacements
	 * @param input string
	 * @param result matching
	 * @param request processed
	 * @return with rules applied
	 */
	public String applyReplacements(String input, MatchResult result, SimpleHttpMessage request) {
		RequestLine line = request.getRequestLine();

		return JsonModifier.insertReplacements(input, x -> {
			if (x.contains(":")) {
				final int index = x.indexOf(":");
				String name = x.substring(0, index);
				String value = x.substring(index + 1);

				switch (name) {

				// Apply to content
				case "content":
					if(request.data == null)
						return "";
					return new String(request.data);
				
				// Apply to headers
				case "header":
					if (!request.headers.contains(value)) {
						JsonRequestModifier.this.logger.error("header '%s' does not exist", value);
						return x;
					}
					return request.headers.get(value);

				// Apply to parameters
				case "parameter":
					if (!line.hasQuery(value)) {
						JsonRequestModifier.this.logger.error("parameter '%s' does not exist", value);
						return x;
					}
					return line.getQuery(value);

				// Path is default
				default:
					if(x.equals("path"))
						return line.path;
					JsonRequestModifier.this.logger.error("Unknown replacement variable '%s'", name);
					return x;
				}

			} else {
				if(x.equals("path"))
					return line.path;
				try {
					return result.group(Integer.valueOf(x));
				} catch (NumberFormatException e) {
					JsonRequestModifier.this.logger.error("could not convert '%s' to int", x);
					return x;
				} catch (IndexOutOfBoundsException e) {
					JsonRequestModifier.this.logger.error("could not get group %s, as it is out of bounds", x);
					return x;
				}
			}
		});
	}

	/**
	 * Apply replacements
	 * @param input string
	 * @param n name matching
	 * @param val value matching
	 * @param request processed
	 * @return string with replacements
	 */
	public String applyReplacements(String input, MatchResult n, MatchResult val, SimpleHttpMessage request) {
		RequestLine line = request.getRequestLine();

		return JsonModifier.insertReplacements(input, x -> {
			if (x.contains(":")) {
				final int index = x.indexOf(":");
				String name = x.substring(0, index);
				String value = x.substring(index + 1);

				switch (name) {

				// Replace header
				case "header":
					if (!request.headers.contains(value)) {
						JsonRequestModifier.this.logger.error("header '%s' does not exist", value);
						return x;
					}
					return request.headers.get(value);

				// Replace parameters
				case "parameter":
					if (!line.hasQuery(value)) {
						JsonRequestModifier.this.logger.error("parameter '%s' does not exist", value);
						return x;
					}
					return line.getQuery(value);

				// Replace name
				case "name":
					try {
						return n.group(Integer.valueOf(value));
					} catch (NumberFormatException e) {
						JsonRequestModifier.this.logger.error("could not conver '%s' to int", value);
						return x;
					} catch (IndexOutOfBoundsException e) {
						JsonRequestModifier.this.logger.error("could not get group %s, as it is out of bounds", value);
						return x;
					}
				// Replace values
				case "val":
					try {
						return val.group(Integer.valueOf(value));
					} catch (NumberFormatException e) {
						JsonRequestModifier.this.logger.error("could not convert '%s' to int", value);
						return x;
					} catch (IndexOutOfBoundsException e) {
						JsonRequestModifier.this.logger.error("could not get group %s, as it is out of bounds", value);
						return x;
					}
				// Replace content
				case "content":
					if(request.data == null)
						return "";
					return new String(request.data);

				// Default error message
				default:
					JsonRequestModifier.this.logger.error("Unknown replacement variable '%s'", name);
					return x;
				}

			} else {
				// Error error
				JsonRequestModifier.this.logger.error("Unknown replacement thingy '%s'", x);
				return x;
			}
		});
	}

	/**
	 * Replaces path in request
	 * @param request processed
	 * @param replacement used
	 */
	public void replacePath(SimpleHttpMessage request, UnnamedReplacement replacement) {
		RequestLine line = request.getRequestLine();

		// Replace path
		line.path = Pattern.compile(replacement.match).matcher(line.path)
				.replaceAll(x -> this.applyReplacements(replacement.replace, x, request));

		// Change headers
		request.headers.put(":path", line.path + (line.query.isEmpty() ? "" : "?" + line.query));
		request.firstLine = line.toString();
	}

	/**
	 * Replace method used
	 * @param request processed
	 * @param replacement used
	 */
	public void replaceMethod(SimpleHttpMessage request, UnnamedReplacement replacement) {
		RequestLine line = request.getRequestLine();

		// Replace method
		line.method = Pattern.compile(replacement.match).matcher(line.method)
				.replaceAll(x -> this.applyReplacements(replacement.replace, x, request));

		// Change headers
		request.headers.put(":method", line.method);
		request.firstLine = line.toString();
	}

	/**
	 * Change headers
	 * @param request processed
	 * @param replacement used
	 */
	public void replaceHeaders(SimpleHttpMessage request, NamedReplacement replacement) {
		// Build new header
		HttpHeaders newHeaders = new HttpHeaders();
		Pattern name = Pattern.compile(replacement.match_name);
		Pattern value = Pattern.compile(replacement.match_val);

		// Match header values
		for(Entry<String, String> header : request.headers.values) {
			Matcher nm = name.matcher(header.getKey());
			if(!nm.find()) {
				newHeaders.put(header.getKey(), header.getValue());
				continue;
			}
			Matcher nv = value.matcher(header.getValue());
			if(!nv.find()) {
				newHeaders.put(header.getKey(), header.getValue());
				continue;
			}
			
			// header matched (at least once)
			// Replace
			String newName = nm.replaceAll(x -> applyReplacements(replacement.replace_name, x, nv.toMatchResult(), request));
			nm.reset();
			String newVal = nv.replaceAll(x -> applyReplacements(replacement.replace_val, nm.toMatchResult(), x, request));
			newHeaders.put(newName, newVal);
		}
		request.headers = newHeaders;
	}

	/**
	 * Replace parameters
	 * @param request processed
	 * @param replacement used
	 */
	public void replaceParams(SimpleHttpMessage request, NamedReplacement replacement) {
		// Build list of parameters
		List<Entry<String, String>> newParams = new ArrayList<>();
		Pattern name = Pattern.compile(replacement.match_name);
		Pattern value = Pattern.compile(replacement.match_val);
		
		RequestLine line = request.getRequestLine();

		// Match parameters in query
		for(Entry<String, String> param : line.queries) {
			Matcher nm = name.matcher(param.getKey());
			if(!nm.find()) {
				newParams.add(param);
				continue;
			}
			Matcher nv = value.matcher(param.getValue());
			if(!nv.find()) {
				newParams.add(param);
				continue;
			}
			
			// parameter matched (at least once)
			// Replace
			String newName = nm.replaceAll(x -> applyReplacements(replacement.replace_name, x, nv.toMatchResult(), request));
			nm.reset();
			String newVal = nv.replaceAll(x -> applyReplacements(replacement.replace_val, nm.toMatchResult(), x, request));
			newParams.add(new SimpleEntry<>(newName, newVal));
		}
		
		line.queries = newParams;
		line.query = line.queryString();
		request.firstLine = line.toString();
		// Change headers
		if(!line.queryString().isEmpty())
			request.headers.put(":path", line.path + "?" + line.queryString());
	}

	/**
	 * Replace content
	 * @param request processed
	 * @param replacement used
	 */
	public void replaceContent(SimpleHttpMessage request, UnnamedReplacement replacement) {
		// TODO: transfer-encoding: chunked? (though do we really want that?)
		request.data = Pattern.compile(replacement.match).matcher(new String(request.data))
				.replaceAll(x -> this.applyReplacements(replacement.replace, x, request)).getBytes();
		// Change headers after replacement
		request.headers.put("content-length", Integer.toString(request.data.length));
	}

	/**
	 * Apply all types replacements
	 * @param request processed
	 * @param replacement used
	 */
	public void applyReplacement(SimpleHttpMessage request, Replacement replacement) {
		// Call method on type basis
		switch (replacement.type) {
		case header:
			replaceHeaders(request, (NamedReplacement) replacement);
			break;
		case method:
			replaceMethod(request, (UnnamedReplacement) replacement);
			break;
		case parameter:
			replaceParams(request, (NamedReplacement) replacement);
			break;
		case path:
			replacePath(request, (UnnamedReplacement) replacement);
			return;
		case content:
			replaceContent(request, (UnnamedReplacement) replacement);
			return;
		default:
			logger.error("Replacement %s cannot be apllied to requests!", replacement.type);
			return;

		}
	}

	/**
	 * Apply rule to request
	 * @param request processed
	 * @param rule used
	 * @return msg with rules applied
	 */
	public SimpleHttpMessage applyRule(SimpleHttpMessage request, RequestRule rule) {
		// Apply all rules
		for (Replacement r : rule.replace) {
			applyReplacement(request, r);
		}
		return request;
	}

	/**
	 * Handle request
	 * @param request processed
	 * @return message with rules applied
	 */
	public SimpleHttpMessage handle(SimpleHttpMessage request) {
		// If a rule matches, apply it
		for (RequestRule rule : rules) {
			if (doesMatch(request, rule)) {
				logger.debug("applying rule %s", rule.name);
				return applyRule(request, rule);
			}
		}

		logger.warning("no rule matching request:\n%s", request);
		return request;
	}

}
