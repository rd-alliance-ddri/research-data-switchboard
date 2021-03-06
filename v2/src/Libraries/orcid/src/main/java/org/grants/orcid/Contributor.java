package org.grants.orcid;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

/**
 * History
 * 
 * 1.0.10: added orcidId
 * 
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */

public class Contributor {
	private OrcidIdentifier orcidId;
	private String creditName;
	private ContributorAttributes contributorAttributes;

	@JsonProperty("contributor-orcid")
	public OrcidIdentifier getOrcidId() {
		return orcidId;
	}

	@JsonProperty("contributor-orcid")
	public void setOrcidId(OrcidIdentifier orcidId) {
		this.orcidId = orcidId;
	}
	
	@JsonProperty("credit-name")
	public String getCreditName() {
		return creditName;
	}

	@JsonProperty("credit-name")
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setCreditName(String creditName) {
		this.creditName = creditName;
	}

	@JsonProperty("contributor-attributes")
	public ContributorAttributes getContributorAttributes() {
		return contributorAttributes;
	}

	@JsonProperty("contributor-attributes")
	public void setContributorAttributes(ContributorAttributes contributorAttributes) {
		this.contributorAttributes = contributorAttributes;
	}

	@Override
	public String toString() {
		return "Contributor [orcidId=" + orcidId 
				+ ", creditName=" + creditName
				+ ", contributorAttributes=" + contributorAttributes 
				+ "]";
	}
}
