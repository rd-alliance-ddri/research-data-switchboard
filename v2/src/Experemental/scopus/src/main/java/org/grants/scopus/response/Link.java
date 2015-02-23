package org.grants.scopus.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties({ "@_fa" })
public class Link {
	private final String ref;
	private final String href;
	private final String type;
	private final String rel;
	

	@JsonCreator
	public Link(
			@JsonProperty("@ref") final String ref,
			@JsonProperty("@href") final String href,
			@JsonProperty("@type") final String type,
			@JsonProperty("@rel") final String rel) {
		
		this.ref = ref;
		this.href = href;
		this.type = type;
		this.rel = rel;
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
	
	public String getRel() {
		return rel;
	}
	
	@Override
	public String toString() {
		return "Link [ref=" + ref 
				+ ", href=" + href 
				+ ", type=" + type 
				+ ", rel=" + rel
				+ "]";
	}
}
