package org.grants.properties;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.grants.graph.GraphUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;

public class PropertiesAnalizer {
	private GraphDatabaseService graphDb;
	private GlobalGraphOperations global;
		
	private File folder; 
	
	private Map<String, Set<String>> mapProperties = new HashMap<String, Set<String>>();
	
	public PropertiesAnalizer(String dbFolder, final String outputFolder) {
		System.out.println("Source Neo4j folder: " + dbFolder);
		System.out.println("Target folder: " + outputFolder);
	
		graphDb = GraphUtils.getReadOnlyGraphDb(dbFolder);
		global = GraphUtils.getGlobalOperations(graphDb);
		
		// Set output folder
		folder = new File(outputFolder);
		folder.mkdirs();
	}	
	
	public void process() throws IOException {	
		try ( Transaction tx = graphDb.beginTx() ) {
			Iterable<Node> nodes = global.getAllNodes();
			
			for (Node node : nodes) 
				analizeNode(node);
		}
		
		for (Map.Entry<String, Set<String>> entry : mapProperties.entrySet()) {
			File file = new File(folder, entry.getKey() + ".txt");
			
			try(BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
			    for(String line : entry.getValue()) {
			    	bw.write(line);
			    	bw.write("\n");
			    }	
		    }
		}
	}
	
	private void analizeNode(Node node) {
	//	System.out.println("Node: " + node.getId());
		String source = (String) node.getProperty(GraphUtils.PROPERTY_NODE_SOURCE);	
		String type = (String) node.getProperty(GraphUtils.PROPERTY_NODE_TYPE);	
		
		String index = type + "_" + source;
		Set<String> setProperties = null;
		
		if (mapProperties.containsKey(index)) 
			setProperties = mapProperties.get(index);
		else {
			setProperties = new HashSet<String>();
			mapProperties.put(index, setProperties);
		}
		
		Iterable<String> keys = node.getPropertyKeys();
		for (String key : keys) 
			setProperties.add(key);
	}
}
