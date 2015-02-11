package org.grants.exporters.field;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.grants.graph.GraphConnection;
import org.grants.graph.GraphField;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.tooling.GlobalGraphOperations;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Exporter designed to export single field from neo4j database
 * 
 * Used for linking Web:Researcher with Grant and Publications nodes
 * 
 * BREF:
 * 
 * Designed to avoid sending big calls into neo4j sever that could result
 * in memory or network error. The idea is the program will export single field 
 * from each Neo4j Node with specific Source and type, that can be loaded after
 * with one of linking program, what will use significally less memory and will
 * not use network connection.
 * 
 * This software require graph_data library to be installed
 * 
 * USAGE:
 * 
 * java -jar export_field-1.0.0.jar <Node Source> <Node Type> <Property Name> <Path to Neo4j database folder> <Path to store exported files>
 * 
 * EXAMPLE:
 * 
 * java -jar export_field-1.0.0.jar Orcid Work title neo4j-1/data/graph.db orcid/fields
 * 
 * @author dima
 *
 */

public class Exporter {
	private static final String FOLDER_FIELD = "field";
	private static final int MAX_COMMANDS = 1024;
	
	private static final String PROPERTY_KEY = "key";
	private static final String PROPERTY_NODE_TYPE = "node_type";
	private static final String PROPERTY_NODE_SOURCE = "node_source";
	
	private GraphDatabaseService graphDb;
	//private ExecutionEngine engine; 
	private GlobalGraphOperations global;
	
	private String nodeSource;
	private String nodeType; 
	private String propertyName;
	
	private ObjectMapper mapper; 
	
	private File fieldsFolder;
	private int fieldCounter;
	private int fieldFileCounter;

	private List<GraphField> graphFields;

	/**
	 * Class constructor 
	 * 
	 * @param nodeSource
	 * @param nodeType
	 * @param propertyName
	 * @param dbFolder
	 * @param outputFolder
	 */
	public Exporter(final String nodeSource, final String nodeType, final String propertyName, 
			final String dbFolder, final String outputFolder) {
		System.out.println("Exporting (" + nodeSource + ":" + nodeType + ")." + propertyName);
		System.out.println("Source Neo4j folder: " + dbFolder);
		System.out.println("Target folder: " + outputFolder);
		
		this.nodeSource = nodeSource;
		this.nodeType = nodeType; 
		this.propertyName = propertyName;
	
		//graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbPath);
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(dbFolder)
				.setConfig( GraphDatabaseSettings.read_only, "true" )
				.newGraphDatabase();
		// register a shutdown hook
		registerShutdownHook(graphDb);
		
	//	engine = new ExecutionEngine(graphDb, StringLogger.SYSTEM);
		global = GlobalGraphOperations.at(graphDb);
		
		// setup Object mapper
		mapper = new ObjectMapper(); 

		// Set output folder
		File folder = new File(outputFolder);
		fieldsFolder = new File(folder, FOLDER_FIELD);
		fieldsFolder.mkdirs();
	}
	
	/**
	 * Function to perform a export
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public void process() throws JsonGenerationException, JsonMappingException, IOException {
		fieldCounter = fieldFileCounter = 0;

		long beginTime = System.currentTimeMillis();
		
		Label labelSource = DynamicLabel.label( nodeSource );
		Label labelType = DynamicLabel.label( nodeType );
		
		try ( Transaction tx = graphDb.beginTx() ) {
			ResourceIterable<Node> nodes = global.getAllNodesWithLabel( labelSource );
			for (Node node : nodes) 
				if (node.hasLabel( labelType )) {
					exportField(node);
			
					if (null != graphFields && graphFields.size() >= MAX_COMMANDS)
						saveFields();
				}
		}

		if (null != graphFields)
			saveFields();
		
		long endTime = System.currentTimeMillis();
		
		System.out.println(String.format("Done. Exported %d fields over %d ms. Average %f ms per node", 
				fieldCounter, endTime - beginTime, (float)(endTime - beginTime) / (float)fieldCounter));
	}
	
	/**
	 * Function to create single GraphField class fron Node class
	 * @param node
	 */
	private void exportField(Node node) {
	//	System.out.println("Node: " + node.getId());
			
		GraphField graphField = createField(node, propertyName);
		if (null != graphField) {
			if (null == graphFields)
				graphFields = new ArrayList<GraphField>();
			graphFields.add(graphField);
			++fieldCounter;
		}
	}
		
	/**
	 * Function to save array of GraphNode classes
	 * 
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	private void saveFields() throws JsonGenerationException, JsonMappingException, IOException {
		String fileName = Long.toString(fieldFileCounter) + ".json";
		mapper.writeValue(new File(fieldsFolder, fileName), graphFields);
		graphFields = null;
		++fieldFileCounter;
	}
	
	/**
	 * Function to Create GraphField instance
	 * 
	 * @param node
	 * @param propertyName
	 * @return
	 */
	private static GraphField createField(Node node, String propertyName) {
		if (node.hasProperty(propertyName)) {
			GraphConnection conn = createConnection(node);
			String field = (String) node.getProperty(propertyName);
			
			return new GraphField(conn, field);
		}
		
		return null;
	}
	
	/**
	 * Function to Create GraphConnection instance
	 * 
	 * @param node
	 * @return
	 */
	private static GraphConnection createConnection(Node node) {
		String source = null;
		String type = null;
		String key = null;
		
		if (node.hasProperty(PROPERTY_NODE_SOURCE))
			source = (String) node.getProperty(PROPERTY_NODE_SOURCE);
		if (node.hasProperty(PROPERTY_NODE_TYPE))
			type = (String) node.getProperty(PROPERTY_NODE_TYPE);
		if (node.hasProperty(PROPERTY_KEY))
			key = (String) node.getProperty(PROPERTY_KEY);
		
		return new GraphConnection(source, type, key);		
	}
	
	/**
	 * Shutdown Hook
	 * 
	 * @param graphDb
	 */
	private static void registerShutdownHook( final GraphDatabaseService graphDb )
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

}