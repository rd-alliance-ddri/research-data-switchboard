package org.grants.exporters.graph;

import org.grants.neo4j.Neo4jUtils;
import org.neo4j.rest.graphdb.entity.RestNode;

public class GraphConnection {
	private String source;
	private String type;
	private String key;
	
	public GraphConnection() {
		
	}
	
	public GraphConnection(RestNode node) {
		if (node.hasProperty(Neo4jUtils.PROPERTY_NODE_SOURCE))
			source = (String) node.getProperty(Neo4jUtils.PROPERTY_NODE_SOURCE);
		if (node.hasProperty(Neo4jUtils.PROPERTY_NODE_TYPE))
			type = (String) node.getProperty(Neo4jUtils.PROPERTY_NODE_TYPE);
		if (node.hasProperty(Neo4jUtils.PROPERTY_KEY))
			key = (String) node.getProperty(Neo4jUtils.PROPERTY_KEY);
	}
	
	public String getSource() {
		return source;
	}
	
	public void setSource(String source) {
		this.source = source;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public String toString() {
		return "GraphConnection [source=" + source + ", type=" + type
				+ ", key=" + key + "]";
	}
}
