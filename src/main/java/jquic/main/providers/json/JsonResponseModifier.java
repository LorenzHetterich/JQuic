package jquic.main.providers.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jquic.example.http.HttpHeaders;
import jquic.example.http.ResponseLine;
import jquic.example.http.SimpleHttpMessage;
import logging.Logger;

public class JsonResponseModifier {

	// List rules
	private List<ResponseRule> rules = new ArrayList<>();

	// Der Logger loggt, der Fehler schweigt
	private Logger logger = new Logger(this);

	/**
	 * Add rule to list
	 * @param rules to be added
	 */
	public void addRules(ResponseRule[] rules) {
		for (ResponseRule rule : rules) {
			// Show rules added
			logger.debug("Adding rule %s", rule.name);
			this.rules.add(rule);
		}
		// Sort by priority
		this.rules.sort((a, b) -> Integer.compare(b.priority, a.priority));
	}

	/**
	 * Match response on rules
	 * @param response HTTP msg
	 * @param rule to be matched
	 * @return true/false
	 */
	public boolean doesMatch(SimpleHttpMessage response, ResponseRule rule) {
		ResponseLine line = response.getResponseLine();

		// Check if status code matches
		if (!Pattern.compile(rule.status_code).matcher(Integer.toString(line.status_code)).find())
			return false;

		// Match headers
		for (Entry<String, String> header : rule.headers.entrySet()) {

			Pattern name = Pattern.compile(header.getKey());
			Pattern value = Pattern.compile(header.getValue());

			if (!response.headers.values.stream()
					.anyMatch(x -> name.matcher(x.getKey()).find() && value.matcher(x.getValue()).find()))
				return false;

		}

		// Check rule for content/data
		if(rule.content != null) {
			if(!response.hasData())
				return false;
			
			if(!Pattern.compile(rule.content).matcher(new String(response.data)).find())
				return false;
		}
		// Everything is fine
		return true;
	}

	/**
	 * Apply replacement rules to response
	 * @param input string
	 * @param result matched to rules
	 * @param response HTTP msg
	 * @return resp string
	 */
	public String applyReplacements(String input, MatchResult result, SimpleHttpMessage response) {
		// Get response line
		ResponseLine line = response.getResponseLine();

		// Call Modifier and substitute replacements
		return JsonModifier.insertReplacements(input, x -> {
			if (x.contains(":")) {
				final int index = x.indexOf(":");
				String name = x.substring(0, index);
				String value = x.substring(index + 1);

				// Case for header/content of msg
				switch (name) {

				case "header":
					if (!response.headers.contains(value)) {
						JsonResponseModifier.this.logger.error("header '%s' does not exist", value);
						return x;
					}
					return response.headers.get(value);

				case "content":
					if(response.data == null)
						return "";
					return new String(response.data);
				
				// Error if no match to previous cases
				default:
					JsonResponseModifier.this.logger.error("Unknown replacement variable '%s'", name);
					return x;
				}

			} else {
				// case status code
				if(x.equals("status_code"))
					return Integer.toString(line.status_code);
				try {
					return result.group(Integer.valueOf(x));
				} catch (NumberFormatException e) {
					JsonResponseModifier.this.logger.error("could not convert '%s' to int", x);
					return x;
				} catch (IndexOutOfBoundsException e) {
					JsonResponseModifier.this.logger.error("could not get group %s, as it is out of bounds", x);
					return x;
				}
			}
		});
	}

	/**
	 * Apply replacements
	 * @param input string
	 * @param n name match result
	 * @param val value match result
	 * @param response treated
	 * @return output with applied replacements
	 */
	public String applyReplacements(String input, MatchResult n, MatchResult val, SimpleHttpMessage response) {
		ResponseLine line = response.getResponseLine();

		return JsonModifier.insertReplacements(input, x -> {
			if (x.contains(":")) {
				final int index = x.indexOf(":");
				String name = x.substring(0, index);
				String value = x.substring(index + 1);

				switch (name) {
				// Apply to header
				case "header":
					if (!response.headers.contains(value)) {
						JsonResponseModifier.this.logger.error("header '%s' does not exist", value);
						return x;
					}
					return response.headers.get(value);

				// Apply to name
				case "name":
					try {
						return n.group(Integer.valueOf(value));
					} catch (NumberFormatException e) {
						JsonResponseModifier.this.logger.error("could not conver '%s' to int", value);
						return x;
					} catch (IndexOutOfBoundsException e) {
						JsonResponseModifier.this.logger.error("could not get group %s, as it is out of bounds", value);
						return x;
					}
				// Apply to values
				case "val":
					try {
						return val.group(Integer.valueOf(value));
					} catch (NumberFormatException e) {
						JsonResponseModifier.this.logger.error("could not convert '%s' to int", value);
						return x;
					} catch (IndexOutOfBoundsException e) {
						JsonResponseModifier.this.logger.error("could not get group %s, as it is out of bounds", value);
						return x;
					}
				// Apply to content
				case "content":
					if(response.data == null)
						return "";
					return new String(response.data);
				// Error case
				default:
					JsonResponseModifier.this.logger.error("Unknown replacement variable '%s'", name);
					return x;
				}

			} else {
				// Status code case
				if(x.equals("status_code"))
					return Integer.toString(line.status_code);
				JsonResponseModifier.this.logger.error("Unknown replacement thingy '%s'", x);
				return x;
			}
		});
	}

	/**
	 * Replace original content of request with modified content
	 * @param request processed
	 * @param replacement unnamed used
	 */
	public void replaceContent(SimpleHttpMessage request, UnnamedReplacement replacement) {
		request.data = Pattern.compile(replacement.match).matcher(new String(request.data))
				.replaceAll(x -> this.applyReplacements(replacement.replace, x, request)).getBytes();
		
		// TODO: transfer-encoding: chunked? (though do we really want that?)
		request.headers.put("content-length", Integer.toString(request.data.length));
	}

	/**
	 * Replace headers
	 * @param request processed
	 * @param replacement named used
	 */
	public void replaceHeaders(SimpleHttpMessage request, NamedReplacement replacement) {
		// Create new header
		HttpHeaders newHeaders = new HttpHeaders();
		// Build based on replacement
		Pattern name = Pattern.compile(replacement.match_name);
		Pattern value = Pattern.compile(replacement.match_val);

		// Match headers on name/value
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
			// replace
			String newName = nm.replaceAll(x -> applyReplacements(replacement.replace_name, x, nv.toMatchResult(), request));
			nm.reset();
			String newVal = nv.replaceAll(x -> applyReplacements(replacement.replace_val, nm.toMatchResult(), x, request));
			newHeaders.put(newName, newVal);
		}
		request.headers = newHeaders;
	}

	/**
	 * Replace status code
	 * @param response processed
	 * @param replacement unnamed used
	 */
	public void replaceStatusCode(SimpleHttpMessage response, UnnamedReplacement replacement) {
		ResponseLine line = response.getResponseLine();
		// Build new status code
		String newStatus = 
				Pattern.compile(replacement.match).matcher(Integer.toString(line.status_code)).replaceAll(x -> this.applyReplacements(replacement.replace, x, response));
		try {
			line.setStatus(Integer.parseInt(newStatus));
		} catch(NumberFormatException e) {
			logger.error("Count not convert new status code %s to int", newStatus);
			return;
		}
		// Replace status code in first line and header
		response.firstLine = line.toString();
		response.headers.put(":status", Integer.toString(line.status_code));
	}

	/**
	 * Apply replacement to message
	 * @param response processed
	 * @param replacement used
	 */
	public void applyReplacement(SimpleHttpMessage response, Replacement replacement) {
		// Processing by type
		switch (replacement.type) {
		case header:
			replaceHeaders(response, (NamedReplacement) replacement);
			break;
		case status_code:
			replaceStatusCode(response, (UnnamedReplacement) replacement);
			break;
		case content:
			replaceContent(response, (UnnamedReplacement) replacement);
			return;
		default:
			logger.error("Replacement %s cannot be apllied to requests!", replacement.type);
			return;
		}
	}

	/**
	 * Apply rule to message
	 * @param request processed
	 * @param rule applied
	 * @return request with rule applied
	 */
	public SimpleHttpMessage applyRule(SimpleHttpMessage request, ResponseRule rule) {
		// Apply replacement by type
		for (Replacement r : rule.replace) {
			applyReplacement(request, r);
		}
		return request;
	}

	/**
	 * Handle request
	 * @param request processed
	 * @return msg with rules applied
	 */
	public SimpleHttpMessage handle(SimpleHttpMessage request) {
		// Process with all speciefied rules
		for (ResponseRule rule : rules) {
			// If matching apply
			if (doesMatch(request, rule)) {
				logger.debug("applying rule %s", rule.name);
				return applyRule(request, rule);
			}
		}

		logger.warning("no rule matching response:\n%s", request);
		return request;
	}
	
}
