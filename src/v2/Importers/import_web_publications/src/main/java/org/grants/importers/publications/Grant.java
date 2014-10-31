package org.grants.importers.publications;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Grant implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7661719778495353332L;
	private int nodeId;
	private String name;
	private Set<String> links = new HashSet<String>();
	private List<Researcher> researchers;
	
	public String getName() {
		return name;
	}
	
	public void setName(final String name) {
		this.name = name;
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
	
	public int getNodeId() {
		return nodeId;
	}

	public void setNodeId(int nodeId) {
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
	
	@Override
	public String toString() {
		return "Grant [nodeId=" + nodeId 
				+ ", name=" + name 
				+ ", links=" + links + "]";
	}
}