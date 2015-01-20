package org.grants.exporters.graph;

import java.util.Map;

import org.grants.neo4j.Neo4jUtils;
import org.neo4j.rest.graphdb.entity.RestNode;

public class GraphNode {
	private Map<String, Object> properties;

	public GraphNode() {
		
	}
	
	public GraphNode(RestNode node) {
		this.properties = Neo4jUtils.getProperties(node);
	}

	public Map<String, Object> getProperties() {
		return properties;
	}
	
	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}
	
	@Override
	public String toString() {
		return "GraphNode [properties=" + properties + "]";
	}
}
