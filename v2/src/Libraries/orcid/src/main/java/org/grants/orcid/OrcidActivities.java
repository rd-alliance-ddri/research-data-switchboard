package org.grants.orcid;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Class to store Orcid Activities
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */
public class OrcidActivities {
	private String affiliations;
	private OrcidWorks works;

	public String getAffiliations() {
		return affiliations;
	}

	public void setAffiliations(String affiliations) {
		this.affiliations = affiliations;
	}

	@JsonProperty("orcid-works")
	public OrcidWorks getWorks() {
		return works;
	}

	@JsonProperty("orcid-works")
	public void setWorks(OrcidWorks works) {
		this.works = works;
	}

	@Override
	public String toString() {
		return "OrcidActivities [affiliations=" + affiliations + ", works="
				+ works + "]";
	}	
}
