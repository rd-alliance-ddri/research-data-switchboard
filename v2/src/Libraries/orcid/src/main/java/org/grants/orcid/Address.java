package org.grants.orcid;

import org.codehaus.jackson.map.annotate.JsonDeserialize;

/**
 * Class to store Orcid Address information
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */
public class Address {
	private String country;

	public String getCountry() {
		return country;
	}

	@JsonDeserialize(using = ValueDeserializer.class)
	public void setCountry(String country) {
		this.country = country;
	}

	@Override
	public String toString() {
		return "Address [country=" + country + "]";
	}
}
