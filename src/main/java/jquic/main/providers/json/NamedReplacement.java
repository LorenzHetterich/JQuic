package jquic.main.providers.json;

public class NamedReplacement extends Replacement {

	public String match_name = ".+", match_val = ".+", replace_name = "${name:0}", replace_val = "${val:0}";

	/**
	 * Replace with rule set of named replacement as specified by enum {@link jquic.main.providers.json.ReplacementType type}
 	 * @param type of replacement
	 */
	public NamedReplacement(ReplacementType type) {
		super(type);
	}

}
