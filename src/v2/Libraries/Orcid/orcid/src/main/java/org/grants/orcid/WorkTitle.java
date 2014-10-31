package org.grants.orcid;

import org.codehaus.jackson.map.annotate.JsonDeserialize;

/**
 * Class to store Work Title
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */
public class WorkTitle {
	private String title;
	private String subtitle;
	
	public String getTitle() {
		return title;
	}
	
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setTitle(String title) {
		this.title = title;
	}

	public String getSubtitle() {
		return subtitle;
	}

	@JsonDeserialize(using = ValueDeserializer.class)
	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	@Override
	public String toString() {
		return "WorkTitle [title=" + title + ", subtitle=" + subtitle + "]";
	}
}
