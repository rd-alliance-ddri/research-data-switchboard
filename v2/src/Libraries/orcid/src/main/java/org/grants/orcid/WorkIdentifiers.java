package org.grants.orcid;

import java.util.List;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Class to store a List of Work Identifiers 
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */
public class WorkIdentifiers {
	private List<WorkIdentifier> identifiers;
	private String scope;

	@JsonProperty("work-external-identifier")
	public List<WorkIdentifier> getIdentifiers() {
		return identifiers;
	}

	@JsonProperty("work-external-identifier")
	public void setIdentifiers(List<WorkIdentifier> identifiers) {
		this.identifiers = identifiers;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	@Override
	public String toString() {
		return "WorkIdentifiers [identifiers=" + identifiers + ", scope="
				+ scope + "]";
	}
}
