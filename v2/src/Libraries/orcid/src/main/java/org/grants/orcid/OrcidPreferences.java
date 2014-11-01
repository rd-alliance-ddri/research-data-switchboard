package org.grants.orcid;

/**
 * Class to store Orcid Preferences
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */
public class OrcidPreferences {
	private String locale;

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	@Override
	public String toString() {
		return "OrcidPreferences [locale=" + locale + "]";
	}
}
