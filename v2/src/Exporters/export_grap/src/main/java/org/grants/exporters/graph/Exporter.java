package org.grants.exporters.graph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;

import org.grants.graph.GraphNode;
import org.grants.graph.GraphRelationsip;
import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.RestAPIFacade;
import org.neo4j.rest.graphdb.entity.RestNode;
import org.neo4j.rest.graphdb.entity.RestRelationship;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;
import org.neo4j.rest.graphdb.util.QueryResult;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Exporter {
	
	private static final String FOLDER_NODE = "node";
	private static final String FOLDER_RELATIONSHIP = "relationship";
	
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
		
		/* Query all JSON records */
		QueryResult<Map<String, Object>> nodes = engine.query("MATCH (n) RETURN n", null);
		for (Map<String, Object> row : nodes) {
			RestNode node = (RestNode) row.get("n");
			
			System.out.println("Node: " + node.getId());
			
			String fileName = Long.toString(node.getId()) + ".json";
			GraphNode nodeGraph = new GraphNode(node);
			
			mapper.writeValue(new File(folder, fileName), nodeGraph);
			
			/*
			String jsonString = mapper.writeValueAsString(nodeGraph);
			Writer writer = new BufferedWriter(new OutputStreamWriter(
			          new FileOutputStream(new File(folder, fileName)), "utf-8"));
			
			writer.write(jsonString);
			writer.close();*/
		}
	}
	
	private void exportRelationships() throws IOException {
		File folder = new File (outputFolder, FOLDER_RELATIONSHIP);
		folder.mkdirs();
		
		QueryResult<Map<String, Object>> nodes = engine.query("MATCH ()-[r]-() RETURN DISTINCT r", null);
		for (Map<String, Object> row : nodes) {
			RestRelationship relationship = (RestRelationship) row.get("r");
			
			System.out.println("Relationship: " + relationship.getId());
			
			String fileName = Long.toString(relationship.getId()) + ".json";
			GraphRelationsip relationshipGraph = new GraphRelationsip(relationship);
				
			mapper.writeValue(new File(folder, fileName), relationshipGraph);
			/*
			
			String jsonString = mapper.writeValueAsString(relationshipGraph);
			Writer writer = new BufferedWriter(new OutputStreamWriter(
			          new FileOutputStream(new File(folder, fileName)), "utf-8"));
			
			writer.write(jsonString);
			writer.close();*/
		}
	}
}
