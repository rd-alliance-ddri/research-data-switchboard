package org.grants.importer.graph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.grants.graph.GraphNode;
import org.grants.graph.GraphRelationsip;
import org.grants.neo4j.Neo4jUtils;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.RestAPIFacade;
import org.neo4j.rest.graphdb.batch.BatchCallback;
import org.neo4j.rest.graphdb.entity.RestNode;
import org.neo4j.rest.graphdb.entity.RestRelationship;
import org.neo4j.rest.graphdb.index.RestIndex;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;
import org.neo4j.rest.graphdb.util.QueryResult;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Importer {
	private static final String FOLDER_NODE = "node";
	private static final String FOLDER_RELATIONSHIP = "relationship";
	private static final int MAX_COMMANDS = 1024;
	
	private File outputFolder;
	private RestAPI graphDb;
	private RestCypherQueryEngine engine;
	private ObjectMapper mapper; 
	
	private Set<String> constrants = new HashSet<String>();
	private Set<String> indexes = new HashSet<String>();
	
	private Map<String, RestIndex<Node>> mapIndexes = new HashMap<String, RestIndex<Node>>();
	
	private RestIndex<Node> getIndex(Label labelSource, Label labelType) {
		String label = Neo4jUtils.combineLabel(labelSource, labelType);
		RestIndex<Node> index = mapIndexes.get(label);
		if (null == index) 
			mapIndexes.put(label, index = Neo4jUtils.getIndex(graphDb, label));
				
		return index;
	}
	
	private RestIndex<Node> getIndex(Label labelSource) {
		String label = labelSource.name();
		RestIndex<Node> index = mapIndexes.get(label);
		if (null == index) 
			mapIndexes.put(label, index = Neo4jUtils.getIndex(graphDb, label));
				
		return index;
	}
	
	public Importer(final String neo4jUrl, final String outputFolder) {
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
		
		// Config neo4j to use Bacth transactions
	//	System.setProperty("org.neo4j.rest.batch_transaction", "true");
		
	}

	public void process() throws IOException {
		importNodes();
		importRelationships();
	}
	
	private void importNodes() throws IOException {
	//	Transaction tx = null;
		List<GraphNode> graphNodes = null;
		long beginTime = System.currentTimeMillis();
		int counter = 0;
		int counterTotal = 0;
		
		File folder = new File (outputFolder, FOLDER_NODE);
		File[] nodes = folder.listFiles();
		for (File nodeFile : nodes) 
			if (!nodeFile.isDirectory()) { 
				System.out.println("Processing node: " + nodeFile.getName());
				
				GraphNode node = mapper.readValue(nodeFile, GraphNode.class);
				
				// check vital information
				String key = (String) node.getProperties().get(Neo4jUtils.PROPERTY_KEY);
				if (null == key || key.isEmpty()) {
					System.out.println("Error in node: " + nodeFile.toString() + ", the node key is empty");
					continue;
				}
				String source = (String) node.getProperties().get(Neo4jUtils.PROPERTY_NODE_SOURCE);
				if (null == source || source.isEmpty()) {
					System.out.println("Error in node: " + nodeFile.toString() + ", the node source is empty");
					continue;
				}
				String type = (String) node.getProperties().get(Neo4jUtils.PROPERTY_NODE_TYPE);
				if (null == type || type.isEmpty()) {
					System.out.println("Error in node: " + nodeFile.toString() + ", the node type is empty");
					continue;
				}
				
				createConstrant(source); 

				if (null == graphNodes)
					graphNodes = new ArrayList<GraphNode>();
				
				graphNodes.add(node);
				
				if (++counter >= MAX_COMMANDS) {
					mergeNodes(graphNodes);
					graphNodes = null;
					
					counterTotal += counter;
					counter = 0;
				}
			}
		
		if (null != graphNodes) {
			
			mergeNodes(graphNodes);
			
			counterTotal += counter;
		}		
		
		long endTime = System.currentTimeMillis();
		
		System.out.println(String.format("Done. Imporded %d nodes over %d ms. Average %f ms per grant", 
				counterTotal, endTime - beginTime, (float)(endTime - beginTime) / (float)counterTotal));

	}
	
	private void mergeNodes(final List<GraphNode> nodes) {
		
		graphDb.executeBatch(new BatchCallback<Void>() {
			@Override
			public Void recordBatch(RestAPI batchRestApi) {
				RestCypherQueryEngine engine = new RestCypherQueryEngine(batchRestApi);
				
				for (GraphNode node : nodes) {
					String source = (String) node.getProperties().get(Neo4jUtils.PROPERTY_NODE_SOURCE);
					if (null == source || source.isEmpty()) {
						continue;
					}
					String type = (String) node.getProperties().get(Neo4jUtils.PROPERTY_NODE_TYPE);
					if (null == type || type.isEmpty()) {
						continue;
					}

					Neo4jUtils.mergeNode(engine, type, source, node.getProperties());
				}
				
				return null;
			}
		});
	}
	
	private void mergeNodes2(final List<GraphNode> nodes) {
		
		graphDb.executeBatch(new BatchCallback<Void>() {
			@Override
			public Void recordBatch(RestAPI batchRestApi) {
				for (GraphNode grantNode : nodes) {
					// check vital information
					String key = (String) grantNode.getProperties().get(Neo4jUtils.PROPERTY_KEY);
					if (null == key || key.isEmpty()) {
						continue;
					}
					String source = (String) grantNode.getProperties().get(Neo4jUtils.PROPERTY_NODE_SOURCE);
					if (null == source || source.isEmpty()) {
						continue;
					}
					String type = (String) grantNode.getProperties().get(Neo4jUtils.PROPERTY_NODE_TYPE);
					if (null == type || type.isEmpty()) {
						continue;
					}
					
					Label labelSource = DynamicLabel.label(source);
					Label labelType = DynamicLabel.label(type);
					
					RestNode node = batchRestApi.createNode(grantNode.getProperties());
					if (!node.hasLabel(labelSource))
						node.addLabel(labelSource);
					if (!node.hasLabel(labelType))
						node.addLabel(labelType);
				}
				
				return null;
			}
		});
	}

	private void createConstrant(String label) {
		if (!constrants.contains(label)) {
			Neo4jUtils.createConstraint(engine, label, Neo4jUtils.PROPERTY_KEY);
			constrants.add(label);
		}
	}
	
	/*
	private void createIndex(String label) {
		if (!indexes.contains(label)) {
			Neo4jUtils.createIndex(engine, label, Neo4jUtils.PROPERTY_KEY);
			indexes.add(label);
		}
	}*/
	
	private void importRelationships() throws IOException {
	/*	File folder = new File (outputFolder, FOLDER_RELATIONSHIP);
		folder.mkdirs();
		
		QueryResult<Map<String, Object>> nodes = engine.query("MATCH ()-[r]-() RETURN DISTINCT r", null);
		for (Map<String, Object> row : nodes) {
			RestRelationship relationship = (RestRelationship) row.get("r");
			
			System.out.println("Relationship: " + relationship.getId());
			
			String fileName = Long.toString(relationship.getId()) + ".xml";
			GraphRelationsip relationshipGraph = new GraphRelationsip(relationship);
					
			String jsonString = mapper.writeValueAsString(relationshipGraph);
			Writer writer = new BufferedWriter(new OutputStreamWriter(
			          new FileOutputStream(new File(folder, fileName)), "utf-8"));
			
			writer.write(jsonString);
			writer.close();
		}*/
	}
}
