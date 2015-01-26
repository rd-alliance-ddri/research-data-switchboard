package org.grants.exporters.graph;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.grants.graph.GraphConnection;
import org.grants.graph.GraphNode;
import org.grants.graph.GraphRelationship;
import org.grants.neo4j.Neo4jUtils;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Relationship;
import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.RestAPIFacade;
import org.neo4j.rest.graphdb.batch.BatchCallback;
import org.neo4j.rest.graphdb.entity.RestNode;
import org.neo4j.rest.graphdb.entity.RestRelationship;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;
import org.neo4j.rest.graphdb.util.QueryResult;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Exporter {
	
	private static final String FOLDER_NODE = "node";
	private static final String FOLDER_RELATIONSHIP = "relationship";
	private static final int MAX_COMMANDS = 256;
	
	private File outputFolder;
	private RestAPI graphDb;
	private RestCypherQueryEngine engine;
	private ObjectMapper mapper; 
	
	public Exporter(final String neo4jUrl, final String outputFolder) {
		System.out.println("Source Neo4j: " + neo4jUrl);
		System.out.println("Target folder: " + outputFolder);
	
		// setup Object mapper
		mapper = new ObjectMapper(); 
		
		// connect to graph database
		graphDb = new RestAPIFacade(neo4jUrl);  
				
		// Create cypher engine
		engine = new RestCypherQueryEngine(graphDb);  
		
		// Set output folder
		this.outputFolder = new File(outputFolder);
		
	}

	public void process() throws IOException {
		exportNodes();
		exportRelationships();
	}
	
	private void exportNodes() throws IOException {
		File folder = new File (outputFolder, FOLDER_NODE);
		folder.mkdirs();
		
		int offset = 0;
		int counter = 0;
		long beginTime = System.currentTimeMillis();
		
		for (;;) {
			String cypher = "MATCH (n) RETURN n";
			if (offset > 0)
				cypher += " SKIP " + offset;
			cypher += " LIMIT " + MAX_COMMANDS;
			
			offset += MAX_COMMANDS;
			
			QueryResult<Map<String, Object>> nodes = engine.query(cypher, null);
			if (!nodes.iterator().hasNext()) 
				break;
			
			for (Map<String, Object> row : nodes) {
				RestNode node = (RestNode) row.get("n");
				
			//	System.out.println("Node: " + node.getId());
				
				String fileName = Long.toString(node.getId()) + ".json";
				GraphNode nodeGraph = new GraphNode(node);
				
				mapper.writeValue(new File(folder, fileName), nodeGraph);
				++counter;
			}
		}	
		
		long endTime = System.currentTimeMillis();
		
		System.out.println(String.format("Done. Exported %d nodes over %d ms. Average %f ms per node", 
				counter, endTime - beginTime, (float)(endTime - beginTime) / (float)counter));
	}
	
	private void exportRelationships() throws IOException {
		File folder = new File (outputFolder, FOLDER_RELATIONSHIP);
		folder.mkdirs();
		
		int offset = 0;
		int counter = 0;
		long beginTime = System.currentTimeMillis();
		
		for (;;) {
			String cypher = "MATCH (n1)-[r]->(n2) RETURN DISTINCT r, n1.node_source, n1.node_type, n1.key, n2.node_source, n2.node_type, n2.key";
			if (offset > 0)
				cypher += " SKIP " + offset;
			cypher += " LIMIT " + MAX_COMMANDS;
			
			offset += MAX_COMMANDS;
		
			QueryResult<Map<String, Object>> relationships = engine.query(cypher, null);
			if (!relationships.iterator().hasNext()) 
				break;
			
			for (Map<String, Object> row : relationships) {
				RestRelationship relationship = (RestRelationship) row.get("r");
				
				String startSource = (String) row.get("n1.node_source");
				String startType = (String) row.get("n1.node_type");
				String startKey = (String) row.get("n1.key");
				
				String endSource = (String) row.get("n2.node_source");
				String endType = (String) row.get("n2.node_type");
				String endKey = (String) row.get("n2.key");
				
			//	System.out.println("Relationship: " + relationship.getId());
				
				String fileName = Long.toString(relationship.getId()) + ".json";
				GraphConnection start = new GraphConnection(startSource, startType, startKey);
				GraphConnection end = new GraphConnection(endSource, endType, endKey);
				
				GraphRelationship relationshipGraph = new GraphRelationship(relationship, start, end);
					
				mapper.writeValue(new File(folder, fileName), relationshipGraph);	
				++counter;
			}
		}
		
		long endTime = System.currentTimeMillis();
		
		System.out.println(String.format("Done. Exported %d relationsips over %d ms. Average %f ms per relationsip", 
				counter, endTime - beginTime, (float)(endTime - beginTime) / (float)counter));

	}
}
