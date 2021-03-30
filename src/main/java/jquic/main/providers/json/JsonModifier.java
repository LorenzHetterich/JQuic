package jquic.main.providers.json;

import java.util.function.Function;
import java.util.regex.Pattern;

public class JsonModifier {

	// insert replacements
	public static String insertReplacements(String original, Function<String, String> replacer) {
		return Pattern.compile("\\$\\{([^}]+)\\}").matcher(original).replaceAll(x -> replacer.apply(x.group(1)));
	}
	
}
