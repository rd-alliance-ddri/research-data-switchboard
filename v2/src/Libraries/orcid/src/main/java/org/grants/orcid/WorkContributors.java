package org.grants.orcid;

import java.util.List;

/**
 * Class to store work contributors
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */
public class WorkContributors {
	private List<Contributor> contributor;

	public List<Contributor> getContributor() {
		return contributor;
	}

	public void setContributor(List<Contributor> contributor) {
		this.contributor = contributor;
	}

	@Override
	public String toString() {
		return "WorkContributors [contributor=" + contributor + "]";
	}
}
