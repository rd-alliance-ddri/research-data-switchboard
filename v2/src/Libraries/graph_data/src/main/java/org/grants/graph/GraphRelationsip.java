package org.grants.graph;

import java.util.Map;

import org.grants.neo4j.Neo4jUtils;
import org.neo4j.rest.graphdb.entity.RestNode;
import org.neo4j.rest.graphdb.entity.RestRelationship;

public class GraphRelationsip {
	private GraphConnection start;
	private GraphConnection end;
	private Map<String, Object> properties;

	public GraphRelationsip() {
		
	}
	
	public GraphRelationsip(RestRelationship relationship) {
		this.properties = Neo4jUtils.getProperties(relationship);
		this.start = new GraphConnection((RestNode) relationship.getStartNode());
		this.end = new GraphConnection((RestNode) relationship.getEndNode());
	}
	
	public GraphConnection getStart() {
		return start;
	}

	public void setStart(GraphConnection start) {
		this.start = start;
	}

	public GraphConnection getEnd() {
		return end;
	}

	public void setEnd(GraphConnection end) {
		this.end = end;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	@Override
	public String toString() {
		return "GraphRelationsip [start=" + start
				+ ", end=" + end + ", properties="
				+ properties + "]";
	}
}
