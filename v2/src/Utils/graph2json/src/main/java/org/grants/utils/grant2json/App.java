package org.grants.utils.grant2json;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

public class App {

	private static final ObjectMapper mapper = new ObjectMapper();   
	private static final TypeReference<LinkedHashMap<String, Object>> linkedHashMapTypeReference = new TypeReference<LinkedHashMap<String, Object>>() {};   

	private static final String FIELD_TABLE = "table";
	private static final String FIELD_RESPONSE = "_response";
	private static final String FIELD_DATA = "data";
	private static final String FIELD_GRAPH = "graph";
	private static final String FIELD_NODES = "nodes";
	private static final String FIELD_RELATIONSHIPS = "relationships";
	private static final String FIELD_ID = "id";
//	private static final String FIELD_LABELS = "labels";
	private static final String FIELD_PROPERTIES = "properties";
	private static final String FIELD_KEY = "key";
	private static final String FIELD_NODE_TYPE = "node_type";
	private static final String FIELD_DOI = "doi";
	private static final String FIELD_NAME = "name";
	private static final String FIELD_SCIENTIFIC_TITLE = "scientific_title";
	private static final String FIELD_FULL_NAME = "full_name";
	private static final String FIELD_TITLE = "title";
	private static final String FIELD_START_NODE = "startNode";
	private static final String FIELD_END_NODE = "endNode";
	private static final String FIELD_TYPE = "type";
	
	private static Map<String, Node> nodes = new HashMap<String, Node>();
	
	enum NodeType { Grant, Researcher, Publication, Dataset, Institution };
	
	private static NodeType getNodeType(String nodeType) {
		if (null != nodeType)
			for (NodeType type : NodeType.values())
				if (type.name().equals(nodeType))
					return type;
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		/*
		if (args.length != 2) {
			System.out.println("USAGE: graph2json <graph file> <json file>");
			
			return;			
		}
		
		File graphFile = new File(args[0]);
		File jsonFile = new File(args[1]);
		*/

		File graphFile = new File("result.json");
		File jsonFile = new File("output.json");
		
		
		if (!graphFile.exists() || graphFile.isDirectory()) {
			System.out.println("Unable to open GRAPH file.");
			
			return;
		}
			
		try {
			Map<String, Object> json = (Map<String, Object>) mapper.readValue(graphFile, linkedHashMapTypeReference);
			if (null != json) {
				Map<String, Object> table = (Map<String, Object>) json.get(FIELD_TABLE);
				if (null != table) {
					Map<String, Object> response = (Map<String, Object>) table.get(FIELD_RESPONSE);
					if (null != response) {
						List<Object> data = (List<Object>) response.get(FIELD_DATA);
						if (null != data) 
							for (Object dataMap : data) {
								Map<String, Object> graph = (Map<String, Object>) ((Map<String, Object>) dataMap).get(FIELD_GRAPH);
								if (null != graph) {
									List<Object> nodesArray = (List<Object>) graph.get(FIELD_NODES);
									if (null != nodesArray) 
										for (Object nodeMap : nodesArray) 
											parseNode((Map<String, Object>) nodeMap);
										
									List<Object> relationshipsArray = (List<Object>) graph.get(FIELD_RELATIONSHIPS);
									if (null != relationshipsArray)
										for (Object relationshipMap : relationshipsArray)
											parseRelationship((Map<String, Object>) relationshipMap);
									
								}
								
							}						
					}					
				}
			}
			
			String jsonString = mapper.writeValueAsString(nodes.values());
			Writer writer = new BufferedWriter(new OutputStreamWriter(
			          new FileOutputStream(jsonFile), "utf-8"));
			
			writer.write(jsonString);
			writer.close();
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
			System.out.println("Unable to process GRAPH file.");
		}
	}
	
	@SuppressWarnings("unchecked")
	private static void parseNode(Map<String, Object> nodeMap) {
		String id = (String) nodeMap.get(FIELD_ID);
		
		if (nodes.containsKey(id))
			return;
		
	//	List<Object> labels = (List<Object>) nodeMap.get(FIELD_LABELS);
		Map<String, Object> properties = (Map<String, Object>) nodeMap.get(FIELD_PROPERTIES);
		
		if (null != properties) {
			String key = (String) properties.get(FIELD_KEY);
			if (null == key)
				key = id;
			
			NodeType nodeType = getNodeType((String) properties.get(FIELD_NODE_TYPE));
			if (null != nodeType) {
				
				Node node = new Node();
				
				node.setId(id);
				node.setType(nodeType.name());
				node.setKey(key);
				
				switch (nodeType) {
				case Grant:
					node.setName((String) properties.get(FIELD_SCIENTIFIC_TITLE));
					break;
					
				case Researcher:
					node.setName((String) properties.get(FIELD_FULL_NAME));
					break;
					
				case Publication:
					node.setName((String) properties.get(FIELD_TITLE));
					break;
					
				case Dataset:
					node.setName((String) properties.get(FIELD_DOI));
					break;
				
				case Institution:
					node.setName((String) properties.get(FIELD_NAME));
					break;									
				}
				
				nodes.put(id, node);
			}
		}
	}
	
	private static void parseRelationship(Map<String, Object> relationshipMap) {
		String startNode = (String) relationshipMap.get(FIELD_START_NODE);
		String endNode = (String) relationshipMap.get(FIELD_END_NODE);
		String type = (String) relationshipMap.get(FIELD_TYPE);
		
		Node nodeStart = nodes.get(startNode);
		Node nodeEnd = nodes.get(endNode);
		if (null != nodeStart && null != nodeEnd) {
			if (null != nodeStart.getRelationships())
				for (Relationship relationship : nodeStart.getRelationships()) 
					if (relationship.getType().equals(type) 
							&& relationship.getKey().equals(nodeEnd.getKey()))
						return;
			
			nodeStart.addRelationship(type, nodeEnd.getKey());
		}
	}
}
