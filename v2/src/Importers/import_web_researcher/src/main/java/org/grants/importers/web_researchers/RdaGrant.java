package org.grants.importers.web_researchers;

import java.util.regex.Pattern;

public class RdaGrant {
	private final String title;
	private final String titleLoverCase;
	private final Pattern pattern;
	private final long nodeId;
	
	public RdaGrant(String title, long nodeId) {
		this.title = title;
		this.titleLoverCase = title.toLowerCase();
		this.pattern = Pattern.compile(this.titleLoverCase.replaceAll("[^a-z0-9]", "."));
		this.nodeId = nodeId;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getTitleLoverCase() {
		return titleLoverCase;
	}	
	
	public Pattern getPattern() {
		return pattern;
	}
	
	public long getNodeId() {
		return nodeId;
	}
	
	@Override
	public String toString() {
		return "RDA:Grant [title=" + title + ", pattern=" + pattern + ", nodeId="
				+ nodeId + "]";
	}	
}
