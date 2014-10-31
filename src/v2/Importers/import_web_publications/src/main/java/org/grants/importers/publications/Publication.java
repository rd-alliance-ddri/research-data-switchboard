package org.grants.importers.publications;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Publication implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4570675172229771634L;
	private long nodeId;
	private String title;
	private String crossrefTitle;	
	private String[] authors;
	private Set<String> links = new HashSet<String>();
	private List<Researcher> researchers;
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(final String title) {
		this.title = title;
	}

	public String getCrossrefTitle() {
		return crossrefTitle;
	}

	public void setCrossrefTitle(String crossrefTitle) {
		this.crossrefTitle = crossrefTitle;
	}
	
	public Set<String> getLinks() {
		return links;
	}
	
	public void addLink(final String link) {
		links.add(link);
	}

	public void setLinks(Set<String> links) {
		this.links = links;
	}
	
	public long getNodeId() {
		return nodeId;
	}

	public void setNodeId(long nodeId) {
		this.nodeId = nodeId;
	}

	public List<Researcher> getResearchers() {
		return researchers;
	}

	public void setResearchers(List<Researcher> researchers) {
		this.researchers = researchers;
	}
	
	public void addResearcher(Researcher researcher) {
		if (null == researchers)
			researchers = new ArrayList<Researcher>(); 
		
		researchers.add(researcher);
	}
	
	public String[] getAuthors() {
		return authors;
	}

	public void setAuthors(String[] authors) {
		this.authors = authors;
	}

	@Override
	public String toString() {
		return "Publication [nodeId=" + nodeId 
				+ ", title=" + title
				+ ", crossrefTitle=" + crossrefTitle 
				+ ", authors="+ Arrays.toString(authors) 
				+ ", links=" + links
				+ ", researchers=" + researchers + "]";
	}	
}
