package org.grants.importers.publications;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author dima
 *
 */
public class Researcher implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 704918220107561981L;
	private long nodeId;
	private String full;
	private String given;
	private String family;

	private Set<String> names;
	
	public String getFull() {
		return full;
	}

	public void setFull(String full) {
		this.full = full;
		
		addName(full);
	}

	public String getGiven() {
		return given;
	}

	public String getFamily() {
		return family;
	}

	public void setGivenAndFamily(String given, String family) {
		this.given = given;
		this.family = family;
		
		addName(given + " " + family);
		addName(family + ", " + given);		
	}

	public Set<String> getNames() {
		return names;
	}

	public void setNames(Set<String> names) {
		this.names = names;
	}
	
	public void addName(String name) {
		if (null == names)
			names = new HashSet<String>();
		
		names.add(name);
	}
	
	public long getNodeId() {
		return nodeId;
	}

	public void setNodeId(long nodeId) {
		this.nodeId = nodeId;
	}

	@Override
	public String toString() {
		return "Researcher [nodeId=" + nodeId 
				+ ", names=" + names + "]";
	}
}
