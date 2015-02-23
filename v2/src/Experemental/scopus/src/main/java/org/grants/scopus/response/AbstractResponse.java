package org.grants.scopus.response;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("abstracts-retrieval-response") 
@JsonIgnoreProperties({ "affiliation", "authkeywords", "idxterms", "subject-areas", "item" })
public class AbstractResponse {
	private final Author[] authors;
	private final Coredata coredata;
	private final Language language;
	
	@JsonCreator
	public AbstractResponse(
			@JsonProperty("authors") final Author[] authors,
			@JsonProperty("coredata") final Coredata coredata,
			@JsonProperty("language") final Language language
			) {
		this.authors = authors;
		this.coredata = coredata;
		this.language = language;
	}
}
