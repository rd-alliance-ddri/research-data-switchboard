package org.grants.imporers.local;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.grants.graph.GraphConnection;
import org.grants.graph.GraphNode;
import org.grants.graph.GraphRelationship;
import org.neo4j.cypher.ExecutionEngine;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.UniqueFactory.UniqueNodeFactory;
import org.neo4j.graphdb.schema.ConstraintDefinition;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.kernel.impl.util.StringLogger;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Importer {
	private static final String FOLDER_NODE = "node";
	private static final String FOLDER_RELATIONSHIP = "relationship";
//	private static final int MAX_COMMANDS = 1024;
	
	private static final String PROPERTY_KEY = "key";
	private static final String PROPERTY_NODE_TYPE = "node_type";
	private static final String PROPERTY_NODE_SOURCE = "node_source";
	
	private Set<String> constrants = new HashSet<String>();
//	private Map<String, UniqueNodeFactory> factories = new HashMap<String, UniqueNodeFactory>();
//	private Map<String, Index<Node>> indexes = new HashMap<String, Index<Node>>();
	private GraphDatabaseService graphDb;
	private ExecutionEngine engine; 
//	private GlobalGraphOperations global;
	
	private ObjectMapper mapper; 
	private File nodesFolder;
	private File relationshipsFolder;
	
	private int nodeCounter;
	private int relationshipCounter;

	public Importer(final String neo4jFolder, final String imputFolder) throws Exception {
		System.out.println("Source folder: " + imputFolder);
		System.out.println("Target Neo4j folder: " + neo4jFolder);
		
		// Set input folder
		File folder = new File(imputFolder);
		nodesFolder = new File(folder, FOLDER_NODE);
		if (!nodesFolder.exists() || !nodesFolder.isDirectory())
			throw new Exception("Invalid nodes path: " + nodesFolder.getPath());
		
		relationshipsFolder = new File(folder, FOLDER_RELATIONSHIP);
		if (!relationshipsFolder.exists() || !relationshipsFolder.isDirectory())
			throw new Exception("Invalid relationships path: " + relationshipsFolder.getPath());
	
		//graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbFolder);
		//graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbFolder)
			//	.setConfig( GraphDatabaseSettings.read_only, "true" )
		//		.newGraphDatabase();
		graphDb = new GraphDatabaseFactory()
			.newEmbeddedDatabaseBuilder( neo4jFolder + "/data/graph.db" )
			.loadPropertiesFromFile( neo4jFolder + "/conf/neo4j.properties" )
			//.setConfig( GraphDatabaseSettings.nodestore_mapped_memory_size, "10M" )
			//.setConfig( GraphDatabaseSettings.string_block_size, "60" )
			//.setConfig( GraphDatabaseSettings.array_block_size, "300" )
			.newGraphDatabase();
		
		// register a shutdown hook
		registerShutdownHook(graphDb);
		
		engine = new ExecutionEngine(graphDb, StringLogger.SYSTEM);
		//global = GlobalGraphOperations.at(graphDb);
		
		// setup Object mapper
		mapper = new ObjectMapper(); 
	}
		
	public void process() throws Exception {
		importNodes();
		importRelationships();
	}	
	
	private void importNodes() throws Exception {
		nodeCounter = 0;

		long beginTime = System.currentTimeMillis();
		
		File[] nodeFiles = nodesFolder.listFiles();
		for (File nodeFile : nodeFiles) 
			if (!nodeFile.isDirectory()) { 
				System.out.println("Processing node: " + nodeFile.getName());

				GraphNode[] graphNodes = mapper.readValue(nodeFile, GraphNode[].class);
				for (GraphNode graphNode : graphNodes) 
					importSchema(graphNode);		
				
				try ( Transaction tx = graphDb.beginTx() ) 
				{
					for (GraphNode graphNode : graphNodes) 
						importNode(graphNode);		
					
					tx.success();
				}
			}
		
		long endTime = System.currentTimeMillis();
		
		System.out.println(String.format("Done. Imported %d nodes over %d ms. Average %f ms per node", 
				nodeCounter, endTime - beginTime, (float)(endTime - beginTime) / (float)nodeCounter));
	}
	
	private void importRelationships() throws Exception {
		relationshipCounter = 0;
		
		long beginTime = System.currentTimeMillis();
		File[] relationshipFiles = relationshipsFolder.listFiles();
				
		for (File relationshipFile : relationshipFiles) 
			if (!relationshipFile.isDirectory()) { 
				System.out.println("Processing relationship: " + relationshipFile.getName());
				
				try ( Transaction tx = graphDb.beginTx() ) 
				{		
					GraphRelationship[] graphRelationships = mapper.readValue(relationshipFile, GraphRelationship[].class);
					for (GraphRelationship graphRelationship : graphRelationships) 
						importRelationship(graphRelationship);
					
					tx.success();
				}
			}
		
		long endTime = System.currentTimeMillis();
		
		System.out.println(String.format("Done. Imported %d relationships over %d ms. Average %f ms per relationship", 
				relationshipCounter, endTime - beginTime, (float)(endTime - beginTime) / (float)relationshipCounter));
	}	
	
	private void importSchema(GraphNode graphNode) throws Exception {
		String source = (String) graphNode.getProperties().get(PROPERTY_NODE_SOURCE);
		if (null == source || source.isEmpty()) 
			throw new Exception("Error in node, the node source is empty");
		
		createConstrant(DynamicLabel.label(source));
	//	getIndex(source);
	}
	
	private void importNode(GraphNode graphNode) throws Exception {
		String source = (String) graphNode.getProperties().get(PROPERTY_NODE_SOURCE);
		if (null == source || source.isEmpty()) 
			throw new Exception("Error in node, the node source is empty");
		String type = (String) graphNode.getProperties().get(PROPERTY_NODE_TYPE);
		if (null == type || type.isEmpty()) 
			throw new Exception("Error in node, the node type is empty");
	
		StringBuilder cypher = new StringBuilder();
		cypher.append("MERGE (n:");
		cypher.append(source);
		cypher.append(":");
		cypher.append(type);
		cypher.append("{");
		boolean init = false;
		for (String property : graphNode.getProperties().keySet()) {
			if (init)
				cypher.append(",");
			else
				init = true;
			
			cypher.append("`");
			cypher.append(property);
			cypher.append("`:{`");
			cypher.append(property);
			cypher.append("`}");
		}
		cypher.append("})");
				
		engine.execute( cypher.toString(), graphNode.getProperties() );
		
		++nodeCounter; 
	}
	
	/*
	private void importNode(GraphNode graphNode) throws Exception {
		String source = (String) graphNode.getProperties().get(PROPERTY_NODE_SOURCE);
		if (null == source || source.isEmpty()) 
			throw new Exception("Error in node, the node source is empty");
		String type = (String) graphNode.getProperties().get(PROPERTY_NODE_TYPE);
		if (null == type || type.isEmpty()) 
			throw new Exception("Error in node, the node type is empty");
		String key = (String) graphNode.getProperties().get(PROPERTY_KEY);
		if (null == key || key.isEmpty()) 
			throw new Exception("Error in node, the node key is empty");

		Index<Node> index = getIndex(source);
		
		Node node = graphDb.createNode();
		for (Entry<String, Object> entry : graphNode.getProperties().entrySet()) {  
			Object value = entry.getValue();
			if (null != value) {
				if (value instanceof Collection<?>) 
					node.setProperty(entry.getKey(), ((Collection<String>) value).toArray(new String[0]));
				else
					node.setProperty(entry.getKey(), value);
			}
		}

		node.addLabel(DynamicLabel.label(source));
		node.addLabel(DynamicLabel.label(type));
		
		index.add(node, PROPERTY_KEY, key);
		
		++nodeCounter; 
	}*/
	
	/*
	private void importNode(GraphNode graphNode) throws Exception {
		String source = (String) graphNode.getProperties().get(PROPERTY_NODE_SOURCE);
		if (null == source || source.isEmpty()) 
			throw new Exception("Error in node, the node source is empty");
		String type = (String) graphNode.getProperties().get(PROPERTY_NODE_TYPE);
		if (null == type || type.isEmpty()) 
			throw new Exception("Error in node, the node type is empty");
		String key = (String) graphNode.getProperties().get(PROPERTY_KEY);
		if (null == key || key.isEmpty()) 
			throw new Exception("Error in node, the node key is empty");

		Label labelSource = DynamicLabel.label(source);
		Label labelType = DynamicLabel.label(type);
		
		Node node = findNode(labelSource, key);
		if (null == node) {
			node = graphDb.createNode();
				 
			++nodeCounter; 
				
			for (Entry<String, Object> entry : graphNode.getProperties().entrySet()) {  
				Object value = entry.getValue();
				if (null != value) {
					if (value instanceof Collection<?>) 
						node.setProperty(entry.getKey(), ((Collection<String>) value).toArray(new String[0]));
					else
						node.setProperty(entry.getKey(), value);
				}
			}
		}

		node.addLabel(labelSource);
		node.addLabel(labelType);
	}*/
		
	private void importRelationship(GraphRelationship graphRelationship) throws Exception {
		StringBuilder cypher = new StringBuilder();
		
		cypher.append("MATCH (from:");
		cypher.append(graphRelationship.getStart().getSource());
		cypher.append("{");
		cypher.append(PROPERTY_KEY);
		cypher.append(":{key_from}}),(to:");
		cypher.append(graphRelationship.getEnd().getSource());
		cypher.append("{");
		cypher.append(PROPERTY_KEY);
		cypher.append(":{key_to}}) MERGE (from)-[:`");
		cypher.append(graphRelationship.getRelationship());
		cypher.append("`");
		if (null != graphRelationship.getProperties()) {
			cypher.append("`{");
			boolean init = false;
			for (String property : graphRelationship.getProperties().keySet()) {
				if (init)
					cypher.append(",");
				else
					init = true;
				
				cypher.append("`");
				cypher.append(property);
				cypher.append("`:{props}.`");
				cypher.append(property);
				cypher.append("`");
			}
			
			cypher.append("}");
		}
		
		cypher.append("]->(to)");
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("key_from", graphRelationship.getStart().getKey());
		map.put("key_to", graphRelationship.getEnd().getKey());
		if (null != graphRelationship.getProperties())
			map.put("props",  graphRelationship.getProperties());	
		
		engine.execute( cypher.toString(), map );
		
		++relationshipCounter; 
	}
	
	/*
	private void importRelationship(GraphRelationship graphRelationship) throws Exception {
		Node nodeStart = findNode(graphRelationship.getStart());
		if (null == nodeStart)
			return;
			//throw new Exception("Error in graph, unable to find start node with key: " + graphRelationship.getStart().getKey());
		Node nodeEnd = findNode(graphRelationship.getEnd());
		if (null == nodeEnd)
			return;
			//throw new Exception("Error in graph, unable to find end node with key: " + graphRelationship.getEnd().getKey());

		RelationshipType relationship = DynamicRelationshipType.withName(graphRelationship.getRelationship());
		
		Iterator<Relationship> rels = 
				nodeStart.getRelationships(relationship, Direction.OUTGOING).iterator();
		if (!rels.hasNext()) {
			Relationship rel = nodeStart.createRelationshipTo(nodeEnd, relationship);
			
			++relationshipCounter;
			
			for (Entry<String, Object> entry : graphRelationship.getProperties().entrySet())
				rel.setProperty(entry.getKey(), entry.getValue());
		}
	}
	*/
	
	private void createConstrant(Label label) {
		if ( !constrants.contains(label.name()) ) {
			constrants.add(label.name());
			
			try ( Transaction tx = graphDb.beginTx() ) 
			{
				Schema schema = graphDb.schema();
				for (ConstraintDefinition constraint : schema.getConstraints(label))
					for (String property : constraint.getPropertyKeys())
						if (property.equals(PROPERTY_KEY))
							return;  // already existing
				
				schema.constraintFor(label)
					.assertPropertyIsUnique(PROPERTY_KEY)
					.create();
	
				tx.success();
			}
		}		
	}
	
	/*
	private Index<Node> getIndex(String label) {
		if (indexes.containsKey(label))
			return indexes.get(label);
		
		Index<Node> index;		
		try ( Transaction tx = graphDb.beginTx() ) 
		{
			index = graphDb.index().forNodes( label );
		}
		
		indexes.put(label, index);
		
		return index;
	}*/
	
	/*
	*/
	
	/*
	private Node findNode(Index index, String key) {
		return (Node) index.get(PROPERTY_KEY, key).getSingle();
	}*/
	
	/*
	private Node findNode(String label, String key) {
		Index<Node> index = getIndex(label);
		return index.get(PROPERTY_KEY, key).getSingle();
	}*/
	
	/*
	private Node findNode(Label label, String key) {
			try ( ResourceIterator<Node> nodes = 
				 graphDb.findNodesByLabelAndProperty( label, PROPERTY_KEY, key ).iterator() )
		{
			if (nodes.hasNext()) 
				return nodes.next();			
			else
				return null;
		}
	}*/
	
	/*
	private Node findNode(GraphConnection connection) {
		return findNode(connection.getSource(), connection.getKey());
	}*/
	
	/*
	private UniqueNodeFactory getNodeFactory(final Label label) {

		if (factories.containsKey(label.name())) 
			return factories.get(label.name());
				
		UniqueNodeFactory factory = getNodeFactory(label, PROPERTY_KEY);
		factories.put(label.name(), factory);
		
		return factory;
	}*/
	
	/*
	private UniqueNodeFactory getNodeFactory(final Label label, final String key) {
		try ( Transaction tx = graphDb.beginTx() )
		{
			UniqueNodeFactory result = new UniqueFactory.UniqueNodeFactory( graphDb, label.name() )
		    {
		        @Override
		        protected void initialize( Node created, Map<String, Object> properties )
		        {
		            created.addLabel( label );
		            created.setProperty( key, properties.get( key ) );
		        }
		    };
		    
		    tx.success();
		    return result;
		 }
	}*/
				
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
