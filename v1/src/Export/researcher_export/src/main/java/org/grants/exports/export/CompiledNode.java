package org.grants.exports.export;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.rest.graphdb.entity.RestNode;

public class CompiledNode {
	public static final String PROPERTY_SELF = "self"; 
	public static final String PROPERTY_NAME = "name";
	public static final String PROPERTY_CLASS = "class"; 
	public static final String PROPERTY_COLOR = "color"; 	
	public static final String PROPERTY_SIZE = "size"; 	
	public static final String PROPERTY_DATA = "data"; 
	public static final String PROPERTY_CHILDREN = "children"; 
	
	private static final String COLOR_INSTITUTION = "#0066FF";
	private static final String COLOR_GRANT = "#FF4D4D";
	private static final String COLOR_RESEARCHER = "#336699";
	private static final String COLOR_DATASET = "#123456";
	private static final String COLOR_PUBLICATION = "#666666";
	
	//private static final int MAX_NAME = 16;
	
	public static enum NodeType
    {
		Institution, Grant, Researcher, Dataset, Publication
    };
	
	private Map<Long, RestNode> nodes = new HashMap<Long, RestNode>();
	private Map<String, Object> map = new HashMap<String, Object>();
	
	public CompiledNode(RestNode node, NodeType nodeType, final String name) {
		map.put(PROPERTY_SELF, node.getUri());
		map.put(PROPERTY_NAME, name);
		map.put(PROPERTY_CLASS, nodeType.name());
		try {
			map.put(PROPERTY_COLOR, getNodeColor(nodeType));
		} catch (Exception e) {
			e.printStackTrace();
		}
		map.put(PROPERTY_SIZE, 1);
		map.put(PROPERTY_DATA, new HashMap<String, Object>());
		
		addRestNode(node);
	}
	
	public static String getNodeColor(NodeType nodeType) throws Exception {
		switch (nodeType) {
		case Institution:
			return COLOR_INSTITUTION;
			
		case Grant:
			return COLOR_GRANT;
	
		case Researcher:
			return COLOR_RESEARCHER;
			
		case Dataset:
			return COLOR_DATASET;

		case Publication:	
			return COLOR_PUBLICATION;
			
		default:
			throw new Exception("Unknwon node type");
		}
	}
	
	public void addRestNode(RestNode node) {
		if (!nodes.containsKey(node.getId())) {
			nodes.put(node.getId(), node); 
			copyNodeData(node);
		}
	}
	
	public Collection<RestNode> getNodes() {
		return nodes.values();
	}
	
	public Set<Long> getNodeIds() {
		return nodes.keySet();
	}
	
	public Map<String, Object> getData() {
		return map;
	}
	
	public boolean isNodeExists(long nodeId) {
		return nodes.containsKey(nodeId);
	}
	
	private void copyNodeData(RestNode node) {
		@SuppressWarnings("unchecked")
		Map<String, Object> data = (Map<String, Object>) map.get(PROPERTY_DATA);
		Iterable<String> keys = node.getPropertyKeys();
		for (String key : keys) 
			if (!data.containsKey(key)) 
				data.put(key, node.getProperty(key));
	}
	
	public void AddChildern(CompiledNode children) {
		if (!map.containsKey(PROPERTY_CHILDREN))
			map.put(PROPERTY_CHILDREN, new ArrayList<Object>());
		
		@SuppressWarnings("unchecked")
		List<Object> childrens = (List<Object>) map.get(PROPERTY_CHILDREN);
		childrens.add(children.map);
	}
	
	/*
	private String MakeName(final String name) {
		if (name.length() <= MAX_NAME)
			return name;
		else
			return name.substring(0, MAX_NAME - 3) + "...";
	}*/
	
	public static boolean isNodeExistsInList(List<CompiledNode> nodes, long nodeId) {
		for (CompiledNode node : nodes) 
			if (node.isNodeExists(nodeId))
				return true;
		
		return false;
	}
	
	public static CompiledNode findNodeInList(List<CompiledNode> nodes, long nodeId) {
		for (CompiledNode node : nodes) 
			if (node.isNodeExists(nodeId))
				return node;
		
		return null;
	}
	
	public static Map<String, Object> getLegend() {
		Map<String, Object> mapLegend = new HashMap<String, Object>();
		
		mapLegend.put(NodeType.Institution.name(), COLOR_INSTITUTION);
		mapLegend.put(NodeType.Grant.name(), COLOR_GRANT);
		mapLegend.put(NodeType.Researcher.name(), COLOR_RESEARCHER);
		mapLegend.put(NodeType.Dataset.name(), COLOR_DATASET);
		mapLegend.put(NodeType.Publication.name(), COLOR_PUBLICATION);
		
		return mapLegend;		
	}	
}
