package org.grants.orcid;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Class to store Orcid work citation information
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */
public class WorkCitation {
	private String type;
	private String citation;
	
	@JsonProperty("work-citation-type")
	public String getType() {
		return type;
	}
	
	@JsonProperty("work-citation-type")
	public void setType(String type) {
		this.type = type;
	}

	public String getCitation() {
		return citation;
	}

	public void setCitation(String citation) {
		this.citation = citation;
	}

	@Override
	public String toString() {
		return "WorkCitation [type=" + type + ", citation=" + citation + "]";
	}	
}
