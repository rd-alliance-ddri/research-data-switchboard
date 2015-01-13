package org.grants.scopus.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Date extends ScopusObject {
	private final String date;

	@JsonCreator
	public Date(
			@JsonProperty("@_fa") final boolean _fa,
			@JsonProperty("$") final String date) {
		super(_fa);
		
		this.date = date;
	}
		
	public String getDate() {
		return date;
	}
	
	@Override
	public String toString() {
		return "Date [date=" + date + "]";
	}
}
