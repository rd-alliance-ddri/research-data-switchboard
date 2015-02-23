package org.grants.imporers.local2;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.grants.graph.GraphConnection;
import org.grants.graph.GraphNode;
import org.grants.graph.GraphRelationship;
import org.grants.graph.GraphSchema;
import org.grants.graph.GraphUtils;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.schema.ConstraintDefinition;
import org.neo4j.graphdb.schema.Schema;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Importer {
	
	private Set<String> constrants = new HashSet<String>();
	private Map<String, Index<Node>> indexes = new HashMap<String, Index<Node>>();

	private GraphDatabaseService graphDb;
	
	private static final ObjectMapper mapper = new ObjectMapper(); 
	
	private File schemaFolder;
	private File nodesFolder;
	private File relationshipsFolder;
	
	private int indexCounter;
	private int nodeCounter;
	private int relationshipCounter;

	public Importer(final String neo4jFolder, final String imputFolder) throws Exception {
		System.out.println("Source folder: " + imputFolder);
		System.out.println("Target Neo4j folder: " + neo4jFolder);
		
		graphDb = GraphUtils.getGraphDb( neo4jFolder );
		
		// Set input folder
		File folder = new File(imputFolder);
		
		schemaFolder = GraphUtils.getSchemaFolder(folder);
		if (!schemaFolder.isDirectory())
			throw new Exception("Invalid schema path: " + schemaFolder.getPath());
		
		nodesFolder = GraphUtils.getNodeFolder(folder);
		if (!nodesFolder.isDirectory())
			throw new Exception("Invalid nodes path: " + nodesFolder.getPath());
		
		relationshipsFolder = GraphUtils.getRelationshipFolder(folder);
		if (!relationshipsFolder.isDirectory())
			throw new Exception("Invalid relationships path: " + relationshipsFolder.getPath());
	}
		
	public void process() throws Exception {
		importSchema();
		importNodes();
		importRelationships();
	}	
	
	private void importSchema() throws Exception {
		System.out.println("Creating constrants and obtain indexes");

		indexCounter = 0;
		long beginTime = System.currentTimeMillis();
		
		GraphSchema graphSchema = mapper.readValue(new File(schemaFolder, GraphUtils.GRAPH_SCHEMA), GraphSchema.class);
		
		// First make sure the constant exists
		for (String index : graphSchema.getIndexes())
			try ( Transaction tx = graphDb.beginTx() ) 
			{
				createConstrant(index);
			
				tx.success();
			}
			
		// Next obtain an index
		for (String index : graphSchema.getIndexes())
			try ( Transaction tx = graphDb.beginTx() ) 
			{
				obtainIndex(index);
			}
		
		long endTime = System.currentTimeMillis();
		System.out.println(String.format("Done. Imported %d constrants over %d ms. Average %f ms per constrant", 
				indexCounter, endTime - beginTime, (float)(endTime - beginTime) / (float)indexCounter));

	}
	
	private void importNodes() throws Exception {
		System.out.println("Importing nodes");
		
		nodeCounter = 0;
		long beginTime = System.currentTimeMillis();
		
		File[] nodeFiles = nodesFolder.listFiles();
		for (File nodeFile : nodeFiles) 
			if (!nodeFile.isDirectory()) { 
				System.out.println("Processing node: " + nodeFile.getName());

				GraphNode[] graphNodes = mapper.readValue(nodeFile, GraphNode[].class);
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
		System.out.println("Importing relationsips");
		
		relationshipCounter = 0;
		long beginTime = System.currentTimeMillis();
		
		File[] relationshipFiles = relationshipsFolder.listFiles();
		for (File relationshipFile : relationshipFiles) 
			if (!relationshipFile.isDirectory()) { 
				System.out.println("Processing relationship: " + relationshipFile.getName());
				
				GraphRelationship[] graphRelationships = mapper.readValue(relationshipFile, GraphRelationship[].class);
				
				try ( Transaction tx = graphDb.beginTx() ) 
				{		
					for (GraphRelationship graphRelationship : graphRelationships) 
						importRelationship(graphRelationship);
					
					tx.success();
				}
			}
		
		long endTime = System.currentTimeMillis();
		System.out.println(String.format("Done. Imported %d relationships over %d ms. Average %f ms per relationship", 
				relationshipCounter, endTime - beginTime, (float)(endTime - beginTime) / (float)relationshipCounter));
	}	
	


	/*
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
	*/
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
	
	@SuppressWarnings("unchecked")
	private void importNode(GraphNode graphNode) throws Exception {
		String source = (String) graphNode.getProperties().get(GraphUtils.PROPERTY_NODE_SOURCE);
		if (null == source || source.isEmpty()) 
			throw new Exception("Error in node, the node source is empty");
		String type = (String) graphNode.getProperties().get(GraphUtils.PROPERTY_NODE_TYPE);
		if (null == type || type.isEmpty()) 
			throw new Exception("Error in node, the node type is empty");
		String key = (String) graphNode.getProperties().get(GraphUtils.PROPERTY_KEY);
		if (null == key || key.isEmpty()) 
			throw new Exception("Error in node, the node key is empty");
	
		Index<Node> index = indexes.get(source + "_" + type);
		Node node = (Node) index.get(GraphUtils.PROPERTY_KEY, key).getSingle();
		if (null == node) {
			node = graphDb.createNode();
				 
			++nodeCounter; 
				
			if (null != graphNode.getProperties())
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
			
			index.add(node, GraphUtils.PROPERTY_KEY, key);
		}

	}
		
	/*
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
	*/
	
	private Node findNode(GraphConnection connection) {
		return indexes
				.get(connection.getSource() + "_" + connection.getType())
				.get(GraphUtils.PROPERTY_KEY, connection.getKey())
				.getSingle();
	}
	
	private void importRelationship(GraphRelationship graphRelationship) throws Exception {
		Node nodeStart = findNode(graphRelationship.getStart());
		if (null == nodeStart)
			throw new Exception("Error in graph, unable to find start node with key: " + graphRelationship.getStart().getKey());
		Node nodeEnd = findNode(graphRelationship.getEnd());
		if (null == nodeEnd)
			throw new Exception("Error in graph, unable to find end node with key: " + graphRelationship.getEnd().getKey());

		RelationshipType relationship = DynamicRelationshipType.withName(graphRelationship.getRelationship());
		
		Iterable<Relationship> rels = nodeStart.getRelationships(relationship, Direction.OUTGOING);
		for (Relationship rel : rels) 
			if (rel.getEndNode().getId() == nodeEnd.getId())
				return;
		
		Relationship rel = nodeStart.createRelationshipTo(nodeEnd, relationship);
			
		++relationshipCounter;
			
		if (null != graphRelationship.getProperties())
			for (Entry<String, Object> entry : graphRelationship.getProperties().entrySet())
				rel.setProperty(entry.getKey(), entry.getValue());
	}
	
	private void createConstrant(String label) {
		if ( !constrants.contains(label) ) {
			constrants.add(label);
			
			Schema schema = graphDb.schema();
			Label l = DynamicLabel.label(label);
			for (ConstraintDefinition constraint : schema.getConstraints(l))
				for (String property : constraint.getPropertyKeys())
					if (property.equals(GraphUtils.PROPERTY_KEY))
						return;  // already existing
				
			schema.constraintFor(l)
				.assertPropertyIsUnique(GraphUtils.PROPERTY_KEY)
				.create();
	
		}		
	}
	
	private void obtainIndex(String label) {
		if ( !indexes.containsKey(label) ) 
			indexes.put(label, graphDb.index().forNodes( label ));			
	}
	
	/*
	private Index<Node> getIndex(String label) {
		return indexes.get(label);
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
}
