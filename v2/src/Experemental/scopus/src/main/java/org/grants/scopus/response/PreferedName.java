package org.grants.scopus.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PreferedName {
	private final String gyvenName;
	private final String fullName;
	private final String familyName;
	private final String initials;
	
	@JsonCreator
	public PreferedName(
			@JsonProperty("ce:given-name") String gyvenName, 
			@JsonProperty("ce:indexed-name") String fullName, 
			@JsonProperty("ce:surname") String familyName,
			@JsonProperty("ce:initials") String initials) {
		super();
		this.gyvenName = gyvenName;
		this.fullName = fullName;
		this.familyName = familyName;
		this.initials = initials;
	}

	public String getGyvenName() {
		return gyvenName;
	}

	public String getFullName() {
		return fullName;
	}

	public String getFamilyName() {
		return familyName;
	}

	public String getInitials() {
		return initials;
	}

	@Override
	public String toString() {
		return "PreferedName [gyvenName=" + gyvenName + ", fullName="
				+ fullName + ", familyName=" + familyName + ", initials="
				+ initials + "]";
	}
}
