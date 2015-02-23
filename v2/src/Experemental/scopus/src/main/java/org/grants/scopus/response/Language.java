package org.grants.scopus.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Language {
	private final String language;
	
	@JsonCreator
	public Language(
			@JsonProperty("@xml:lang") final String language) {
		this.language = language;
	}

	public String getLanguage() {
		return language;
	}

	@Override
	public String toString() {
		return "Language [language=" + language + "]";
	}
}
