package org.grants.scopus.response;

public class Author {
	private final String authorId;
	private final String authorUrl;
	private final String fullName;
	private final String givenName;
	private final String familyName;
	private final String initials;

	public Author(
			final String authorId,
			final String authorUrl,
			final String fullName,
			final String givenName,
			final String familyName,
			final String initials) {

		this.authorId = authorId;
		this.authorUrl = authorUrl;
		this.fullName = fullName;
		this.givenName = givenName;
		this.familyName = familyName;
		this.initials = initials;
	}

	public String getAuthorId() {
		return authorId;
	}

	public String getAuthorUrl() {
		return authorUrl;
	}

	public String getFullName() {
		return fullName;
	}

	public String getGivenName() {
		return givenName;
	}

	public String getFamilyName() {
		return familyName;
	}

	public String getInitials() {
		return initials;
	}

	@Override
	public String toString() {
		return "Author [authorId=" + authorId + ", authorUrl=" + authorUrl
				+ ", fullName=" + fullName + ", givenName=" + givenName
				+ ", familyName=" + familyName + ", initials=" + initials + "]";
	}
}
