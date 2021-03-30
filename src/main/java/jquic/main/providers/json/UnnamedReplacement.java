package jquic.main.providers.json;

public class UnnamedReplacement extends Replacement{

	public String match = ".+", replace = "${0}";

	/**
	 * Replace with rule set of unnamed replacement as specified by enum {@link jquic.main.providers.json.ReplacementType type}
	 * @param type of replacement
	 */
	public UnnamedReplacement(ReplacementType type) {
		super(type);
	}

}
