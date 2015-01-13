package org.grants.scopus.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Result {
	private SearchResults results;

	public SearchResults getResults() {
		return results;
	}

	@JsonProperty("search-results") 
	public void setResults(SearchResults results) {
		this.results = results;
	}

	@Override
	public String toString() {
		return "Result [results=" + results + "]";
	}
}
