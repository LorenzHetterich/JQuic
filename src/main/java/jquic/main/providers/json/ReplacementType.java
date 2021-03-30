package jquic.main.providers.json;

public enum ReplacementType {

	// type of replacement
	content(false, false, false),
	path(false, true, false),
	method(false, true, false),
	status_code(false, false, true),
	header(true, false, false),
	parameter(true, true, false);
	
	
	public final boolean named;
	public final boolean requestOnly, responseOnly;
	
	ReplacementType(boolean named, boolean requestOnly, boolean responseOnly){
		this.named = named;
		this.requestOnly = requestOnly;
		this.responseOnly = responseOnly;
	}
}
