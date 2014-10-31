package org.grants.orcid;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

/**
 * Class to store Orcid Bio
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */
public class OrcidBio {
	private PersonalDetails personalDetails;
	private String biography;
	private ContactDetails contactDetails;
	private String keywords;
	private String delegation;
	private String applications;
	private String scope;

	@JsonProperty("personal-details")
	public PersonalDetails getPersonalDetails() {
		return personalDetails;
	}

	@JsonProperty("personal-details")
	public void setPersonalDetails(PersonalDetails personalDetails) {
		this.personalDetails = personalDetails;
	}

	public String getBiography() {
		return biography;
	}

	@JsonDeserialize(using = ValueDeserializer.class)
	public void setBiography(String biography) {
		this.biography = biography;
	}

	@JsonProperty("contact-details")
	public ContactDetails getContactDetails() {
		return contactDetails;
	}

	@JsonProperty("contact-details")
	public void setContactDetails(ContactDetails contactDetails) {
		this.contactDetails = contactDetails;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getDelegation() {
		return delegation;
	}

	public void setDelegation(String delegation) {
		this.delegation = delegation;
	}

	public String getApplications() {
		return applications;
	}

	public void setApplications(String applications) {
		this.applications = applications;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}
	
	@Override
	public String toString() {
		return "OrcidBio [personalDetails=" + personalDetails + ", biography="
				+ biography + ", contactDetails=" + contactDetails
				+ ", keywords=" + keywords + ", delegations=" + delegation
				+ ", applications=" + applications + ", scope=" + scope + "]";
	}
}
