package org.grants.google.cse;

public class Spelling {
	private String correctedQuery;
	private String htmlCorrectedQuery;

	public String getCorrectedQuery() {
		return correctedQuery;
	}

	public void setCorrectedQuery(String correctedQuery) {
		this.correctedQuery = correctedQuery;
	}

	public String getHtmlCorrectedQuery() {
		return htmlCorrectedQuery;
	}

	public void setHtmlCorrectedQuery(String htmlCorrectedQuery) {
		this.htmlCorrectedQuery = htmlCorrectedQuery;
	}

	@Override
	public String toString() {
		return "Spelling [correctedQuery=" + correctedQuery
				+ ", htmlCorrectedQuery=" + htmlCorrectedQuery + "]";
	}
}
