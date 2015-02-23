package org.grants.scopus.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Organization {
	private final String organization;
	
	@JsonCreator
	public Organization(
			@JsonProperty("$") final String organization) {
		this.organization = organization;
	}
		
	public String getKeyword() {
		return organization;
	}
	
	@Override
	public String toString() {
		return "Organization [organization=" + organization + "]";
	}
}
