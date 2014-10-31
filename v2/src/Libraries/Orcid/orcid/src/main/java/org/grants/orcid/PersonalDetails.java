package org.grants.orcid;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

/**
 * Class to store Oricd Personal Details
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */
public class PersonalDetails {
	private String givenNames;
	private String familyName;
	
	@JsonProperty("given-names")
	public String getGivenNames() {
		return givenNames;
	}
	
	@JsonProperty("given-names")
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setGivenNames(String givenNames) {
		this.givenNames = givenNames;
	}
	
	@JsonProperty("family-name")
	public String getFamilyName() {
		return familyName;
	}
	
	@JsonProperty("family-name")
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}	
	
	@Override
	public String toString() {
		return "PersonalDetails [guvenNames=" + givenNames + ", familyName="
				+ familyName + "]";
	}	
}
