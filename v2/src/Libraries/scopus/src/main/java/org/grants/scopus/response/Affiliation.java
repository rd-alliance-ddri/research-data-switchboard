package org.grants.scopus.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Affiliation  extends ScopusObject {
	private final String affilname;
	private final String affiliationCity;
	private final String affiliationCountry;
	
	@JsonCreator
	public Affiliation(
			@JsonProperty("@_fa") final boolean _fa,
			@JsonProperty("affilname") final String affilname,
			@JsonProperty("affiliation-city") final String affiliationCity,
			@JsonProperty("affiliation-country") final String affiliationCountry) {
		super(_fa);
		
		this.affilname = affilname;
		this.affiliationCity = affiliationCity;
		this.affiliationCountry = affiliationCountry;
	}
	
	public String getAffilname() {
		return affilname;
	}
	
	public String getAffiliationCity() {
		return affiliationCity;
	}
	
	public String getAffiliationCountry() {
		return affiliationCountry;
	}

	@Override
	public String toString() {
		return "Affiliation [affilname=" + affilname + ", affiliationCity="
				+ affiliationCity + ", affiliationCountry="
				+ affiliationCountry + "]";
	}
}
