package org.grants.importers.google;

import java.util.HashSet;
import java.util.Set;
//import java.util.regex.Pattern;

public class LinkingNode {
	//private final String title;
	private final String type;
//	private final String titleLowerCase;
//	private final Pattern pattern;
	private Set<Long> nodesId = new HashSet<Long>();
	
	public LinkingNode(long nodeId, /*String title,*/ String type) {
		this.nodesId.add(nodeId);
	//	this.title = title;
		this.type = type;
		
	//	titleLowerCase = title.toLowerCase();
	//	pattern = Pattern.compile(titleLowerCase.replaceAll("[^a-z0-9]", "."));
	}
	
	public void addNodeId(long nodeId) {
		nodesId.add(nodeId);
	}
	
	public boolean isUnique() {
		return nodesId.size() == 1;
	}
	/*
	public String getTitle() {
		return title;
	}*/
	
	public String getType() {
		return type;
	}
	/*
	public String getTitleLowerCase() {
		return titleLowerCase;
	}*/	
	
/*	public Pattern getPattern() {
		return pattern;
	}
	*/
	public Set<Long> getNodesId() {
		return nodesId;
	}
	
	@Override
	public String toString() {
		return "RDA:Grant [type=" + type 
			//	+ ", title=" + title 
			//	+ ", pattern=" + pattern 
				+ ", nodesId=" + nodesId + "]";
	}	
}
