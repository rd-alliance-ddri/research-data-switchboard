package org.grants.graph;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.tooling.GlobalGraphOperations;

public class GraphUtils {
	public static final String FOLDER_NODE = "node";
	public static final String FOLDER_RELATIONSHIP = "relationship";
	public static final String FOLDER_FIELD = "field";
	public static final String FOLDER_SCHEMA = "schema";
	
	public static final String PROPERTY_KEY = "key";
	public static final String PROPERTY_NODE_TYPE = "node_type";
	public static final String PROPERTY_NODE_SOURCE = "node_source";
	
	public static final String NEO4J_CONF = "/conf/neo4j.properties";
	public static final String NEO4J_DB = "/data/graph.db";
	
	public static final String GRAPH_EXTENSION = ".json";
	public static final String GRAPH_SCHEMA = "schema" + GRAPH_EXTENSION;
	
	public static final int MIN_TITLE_LENGTH = 20;
	public static final int MIN_TITLE_WORDS = 2;
	
	
	public static GraphDatabaseService getReadOnlyGraphDb( final String graphDbPath ) {
		GraphDatabaseService graphDb = new GraphDatabaseFactory()
				.newEmbeddedDatabaseBuilder( graphDbPath + NEO4J_DB )
				.loadPropertiesFromFile( graphDbPath + NEO4J_CONF )
				.setConfig( GraphDatabaseSettings.read_only, "true" )
				.newGraphDatabase();
		
		registerShutdownHook( graphDb );
		
		return graphDb;
	}
	
	public static GraphDatabaseService getGraphDb( final String graphDbPath ) {
		GraphDatabaseService graphDb = new GraphDatabaseFactory()
			.newEmbeddedDatabaseBuilder( graphDbPath + NEO4J_DB )
			.loadPropertiesFromFile( graphDbPath + NEO4J_CONF )
			.newGraphDatabase();
		
		registerShutdownHook( graphDb );
		
		return graphDb;
	}
	
	public static GlobalGraphOperations getGlobalOperations( final GraphDatabaseService graphDb ) {
		return GlobalGraphOperations.at(graphDb);
	}
	
	public static void registerShutdownHook( final GraphDatabaseService graphDb )
	{
	    // Registers a shutdown hook for the Neo4j instance so that it
	    // shuts down nicely when the VM exits (even if you "Ctrl-C" the
	    // running application).
	    Runtime.getRuntime().addShutdownHook( new Thread()
	    {
	        @Override
	        public void run()
	        {
	            graphDb.shutdown();
	        }
	    } );
	}
	
	public static final File getSchemaFolder(File folder) {
		return new File(folder, FOLDER_SCHEMA);
	}
	
	public static final File getNodeFolder(File folder) {
		return new File(folder, FOLDER_NODE);
	}
	
	public static final File getRelationshipFolder(File folder) {
		return new File(folder, FOLDER_RELATIONSHIP);
	}
	
	public static final File getFieldFolder(File folder) {
		return new File(folder, FOLDER_FIELD);
	}
	
	public static GraphNode createNode(Node node) {
		return new GraphNode(getProperties(node));
	}
	
	public static GraphConnection createConnection(Node node) {
		String source = null;
		String type = null;
		String key = null;
		
		if (node.hasProperty(GraphUtils.PROPERTY_NODE_SOURCE))
			source = (String) node.getProperty(GraphUtils.PROPERTY_NODE_SOURCE);
		if (node.hasProperty(GraphUtils.PROPERTY_NODE_TYPE))
			type = (String) node.getProperty(GraphUtils.PROPERTY_NODE_TYPE);
		if (node.hasProperty(GraphUtils.PROPERTY_KEY))
			key = (String) node.getProperty(GraphUtils.PROPERTY_KEY);
		
		return new GraphConnection(source, type, key);		
	}	
	
	public static GraphRelationship createRelationship(GraphConnection start, GraphConnection end, Relationship relationship) {
		return new GraphRelationship(relationship.getType().name(), getProperties(relationship), start, end);
	}
			
	public static Map<String, Object> getProperties(Node node) {
		Iterable<String> keys = node.getPropertyKeys();
		Map<String, Object> pars = null;
		
		for (String key : keys) {
			if (null == pars)
				pars = new HashMap<String, Object>();
			
			pars.put(key, node.getProperty(key));
		}
		
		return pars;
	}
	
	/**
	 * Function to Create GraphField instance
	 * 
	 * @param node
	 * @param propertyName
	 * @return
	 */
	public static GraphField createField(Node node, String propertyName) {
		return node.hasProperty(propertyName) ? new GraphField(createConnection(node), (String) node.getProperty(propertyName)) : null;
	}	
	
	public static Map<String, Object> getProperties(Relationship relationship) {
		Iterable<String> keys = relationship.getPropertyKeys();
		Map<String, Object> pars = null;
		
		for (String key : keys) {
			if (null == pars)
				pars = new HashMap<String, Object>();
			
			pars.put(key, relationship.getProperty(key));
		}
		
		return pars;
	}
	
	public static Set<String> loadBlackList(String blackList) throws FileNotFoundException, IOException {
		Set<String> list = new HashSet<String>();
		
		File file = new File(blackList);
		if (file.exists()) {
			try(BufferedReader br = new BufferedReader(new FileReader(new File(blackList)))) {
			    for(String line; (line = br.readLine()) != null; ) 
			    	list.add(line.trim().toLowerCase());
			}
		}
		
		return list;
	}
	
	public static void saveBlackList(String blackListName, Set<String> blackList) throws FileNotFoundException, IOException {
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(new File(blackListName)))) {
		    for(String line : blackList) {
		    	bw.write(line);
		    	bw.write("\n");
		    }	
	    }
	}
	
	public static boolean isTitleShort(String title) {
		return title.length() < MIN_TITLE_LENGTH;
	}
	
	public static boolean isTitleSimple(String title) {
		int counter = 1;
 	 	for (int i = 0; i < title.length(); ++i) 
 	 	    if(title.charAt(i) == ' ') {
 	 	        if (++counter >= MIN_TITLE_WORDS)
 	 	        	return false;
 	 	    }

 	 	return true;
	}
	
	/**
	 * Function to check that grant of publication title is invalid
	 * 
	 * @param title
	 * @return
	 */
	public static boolean isTitleInvalid(String title) {
 	 	return isTitleShort(title) || isTitleSimple(title);	 		
 	 
	}
	
	public static boolean isTitleBlackListed(String title, Set<String> blackList) {
 	 	return isTitleShort(title) || isTitleSimple(title) || blackList.contains(title);
	}

	
}
