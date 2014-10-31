package org.grants.orcid;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

public class Contributor {
	private String creditName;
	private ContributorAttributes contributorAttributes;

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
		return "Contributor [creditName=" + creditName
				+ ", contributorAttributes=" + contributorAttributes + "]";
	}
}
