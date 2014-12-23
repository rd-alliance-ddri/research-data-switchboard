package org.grants.importers.web_researchers;

import java.util.regex.Pattern;

public class DryadPublication {
	private final String title;
	private final Pattern pattern;
	private final long nodeId;
	
	public DryadPublication(String title, long nodeId) {
		this.title = title;
		this.pattern = Pattern.compile(title.toLowerCase().replaceAll("[^a-z0-9]", "."));
		this.nodeId = nodeId;
	}
	
	public String getTitle() {
		return title;
	}
	
	public Pattern getPattern() {
		return pattern;
	}
	
	public long getNodeId() {
		return nodeId;
	}
	
	@Override
	public String toString() {
		return "Dryad:Publication [title=" + title + ", pattern=" + pattern + ", nodeId="
				+ nodeId + "]";
	}	
}
