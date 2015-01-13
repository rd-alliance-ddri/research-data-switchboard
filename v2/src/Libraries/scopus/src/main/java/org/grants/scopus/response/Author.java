package org.grants.scopus.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Author extends ScopusObject {
	private final String givenName;
	private final String surname;
	
	@JsonCreator
	public Author(
			@JsonProperty("@_fa") final boolean _fa,
			@JsonProperty("given-name") final String givenName,
			@JsonProperty("surname") final String surname) {
		super(_fa);
		
		this.givenName = givenName;
		this.surname = surname;
	}
	
	public String getGivenName() {
		return givenName;
	}
	
	public String getSurname() {
		return surname;
	}
	
	@Override
	public String toString() {
		return "Author [givenName=" + givenName + ", surname=" + surname + "]";
	}
}
