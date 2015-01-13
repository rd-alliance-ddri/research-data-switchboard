package org.grants.scopus.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Link extends ScopusObject {
	private final String ref;
	private final String href;
	private final String type;
	
	@JsonCreator
	public Link(
			@JsonProperty("@_fa") final boolean _fa,
			@JsonProperty("@ref") final String ref,
			@JsonProperty("@href") final String href,
			@JsonProperty("@type") final String type) {
		super(_fa);
		
		this.ref = ref;
		this.href = href;
		this.type = type;
	}
	
	public String getRef() {
		return ref;
	}

	public String getHref() {
		return href;
	}
	
	public String getType() {
		return type;
	}
	
	@Override
	public String toString() {
		return "Link [ref=" + ref 
				+ ", href=" + href 
				+ ", type=" + type 
				+ "]";
	}
}
