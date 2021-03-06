package org.grants.orcid;

import java.util.List;

import org.codehaus.jackson.map.annotate.JsonDeserialize;

public class Keywords {
	private List<String> keyword;
	private String visibility;
	
	public List<String> getKeyword() {
		return keyword;
	}

	@JsonDeserialize(using = ValueDeserializer.class)
	public void setKeyword(List<String> keyword) {
		this.keyword = keyword;
	}
	
	public String getVisibility() {
		return visibility;
	}

	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}

	@Override
	public String toString() {
		return "Keywords [keyword=" + keyword + ", visibility=" + visibility + "]";
	}	
}
