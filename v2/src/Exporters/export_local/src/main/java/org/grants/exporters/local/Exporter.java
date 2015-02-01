package org.grants.exporters.local;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.grants.graph.GraphConnection;
import org.grants.graph.GraphNode;
import org.grants.graph.GraphRelationship;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.tooling.GlobalGraphOperations;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Exporter {
	private static final String FOLDER_NODE = "node";
	private static final String FOLDER_RELATIONSHIP = "relationship";
	private static final int MAX_COMMANDS = 1024;
	
	private static final String PROPERTY_KEY = "key";
	private static final String PROPERTY_NODE_TYPE = "node_type";
	private static final String PROPERTY_NODE_SOURCE = "node_source";
	
	private GraphDatabaseService graphDb;
	//private ExecutionEngine engine; 
	private GlobalGraphOperations global;
	
	private ObjectMapper mapper; 
	private File nodesFolder;
	private File relationshipsFolder;
	
	private int nodeCounter;
	private int relationshipCounter;

	private int nodeFileCounter;
	private int relationshipFileCounter;

	private List<GraphNode> graphNodes;
	private List<GraphRelationship> graphRelationships;

	public Exporter(String dbFolder, final String outputFolder) {
		System.out.println("Source Neo4j folder: " + dbFolder);
		System.out.println("Target folder: " + outputFolder);
	
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
		nodesFolder = new File(folder, FOLDER_NODE);
		relationshipsFolder = new File(folder, FOLDER_RELATIONSHIP);
		
		nodesFolder.mkdirs();
		relationshipsFolder.mkdirs();
	}
	
	
	public void process() throws JsonGenerationException, JsonMappingException, IOException {
		nodeCounter = relationshipCounter = nodeFileCounter = relationshipFileCounter = 0;

		long beginTime = System.currentTimeMillis();
		
		try ( Transaction tx = graphDb.beginTx() ) {
			Iterable<Node> nodes = global.getAllNodes();
			
			for (Node node : nodes) {
				exportNode(node);
			
				if (null != graphNodes && graphNodes.size() >= MAX_COMMANDS)
					saveNodes();
				
				if (null != graphRelationships && graphRelationships.size() >= MAX_COMMANDS)
					saveRelationships();
			}
		}

		if (null != graphNodes)
			saveNodes();
		
		if (null != graphRelationships)
			saveRelationships();
		
		long endTime = System.currentTimeMillis();
		
		System.out.println(String.format("Done. Exported %d nodes and %d relationships over %d ms. Average %f ms per node", 
				nodeCounter, relationshipCounter, endTime - beginTime, (float)(endTime - beginTime) / (float)nodeCounter));
	}
	
	private void exportNode(Node node) {
	//	System.out.println("Node: " + node.getId());
			
		GraphNode graphNode = new GraphNode(getProperties(node));
		if (null == graphNodes)
			graphNodes = new ArrayList<GraphNode>();
		graphNodes.add(graphNode);
		++nodeCounter;

		Iterable<Relationship> relationships = node.getRelationships(Direction.OUTGOING);
		if (null != relationships && relationships.iterator().hasNext()) {
			GraphConnection start = createConnection(node);
			for (Relationship relationship : relationships)
				exportRelationship(start, relationship);
		}
	}
	
	
	private void exportRelationship(GraphConnection start, Relationship relationship)  {
	//	System.out.println("Relationship: " + relationship.getId());

		GraphConnection end = createConnection(relationship.getEndNode());
		GraphRelationship graphRelationship = new GraphRelationship(
				relationship.getType().name(), getProperties(relationship), start, end);
		
		if (null == graphRelationships)
			graphRelationships = new ArrayList<GraphRelationship>();
		graphRelationships.add(graphRelationship);		
		++relationshipCounter;		
	}
	
	private void saveNodes() throws JsonGenerationException, JsonMappingException, IOException {
		String fileName = Long.toString(nodeFileCounter) + ".json";
		mapper.writeValue(new File(nodesFolder, fileName), graphNodes);
		graphNodes = null;
		++nodeFileCounter;
	}
	
	private void saveRelationships() throws JsonGenerationException, JsonMappingException, IOException {
		String fileName = Long.toString(relationshipFileCounter) + ".json";
		mapper.writeValue(new File(relationshipsFolder, fileName), graphRelationships);
		graphRelationships = null;
		++relationshipFileCounter;
	}
	
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
	
	private static Map<String, Object> getProperties(Node node) {
		Iterable<String> keys = node.getPropertyKeys();
		Map<String, Object> pars = null;
		
		for (String key : keys) {
			if (null == pars)
				pars = new HashMap<String, Object>();
			
			pars.put(key, node.getProperty(key));
		}
		
		return pars;
	}
	
	private static Map<String, Object> getProperties(Relationship relationship) {
		Iterable<String> keys = relationship.getPropertyKeys();
		Map<String, Object> pars = null;
		
		for (String key : keys) {
			if (null == pars)
				pars = new HashMap<String, Object>();
			
			pars.put(key, relationship.getProperty(key));
		}
		
		return pars;
	}
	
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
