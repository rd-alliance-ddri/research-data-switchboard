package org.grants.imporers.local;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.grants.graph.GraphConnection;
import org.grants.graph.GraphIndex;
import org.grants.graph.GraphNode;
import org.grants.graph.GraphRelationship;
import org.grants.graph.GraphUtils;
import org.grants.neo4j.local.Neo4jUtils;
import org.graph.aggrigation.AggrigationUtils;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Importer {
	private Map<String, Index<Node>> indexes = new HashMap<String, Index<Node>>();

	private GraphDatabaseService graphDb;
	
	private static final ObjectMapper mapper = new ObjectMapper(); 
	
	private File schemaFolder;
	private File nodesFolder;
	private File relationshipsFolder;
	
	private int nodeCounter;
	private int relationshipCounter;
	
	private String[] labels = new String[] { 
			"Dryad", "RDA", "CrossRef", "ORCID", "Scopus", "Web", "CERN", "figshare", "NHMRC", "ARC"
	};
	
	public Importer(final String neo4jFolder, final String imputFolder) throws Exception {
		System.out.println("Source folder: " + imputFolder);
		System.out.println("Target Neo4j folder: " + neo4jFolder);
		
		graphDb = Neo4jUtils.getGraphDb( neo4jFolder );
		
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

		long beginTime = System.currentTimeMillis();
		
		GraphIndex[] graphIndex = mapper.readValue(new File(schemaFolder, GraphUtils.GRAPH_SCHEMA), GraphIndex[].class);
		
		// First make sure the constant exists
		for (GraphIndex index : graphIndex)
			try ( Transaction tx = graphDb.beginTx() ) 
			{
				if (index.isUnique())
					Neo4jUtils.createConstrant(graphDb, index.getLabel(), index.getKey());
				else 
					Neo4jUtils.createIndex(graphDb, index.getLabel(), index.getKey());
			
				tx.success();
			}
			
/*		// Next obtain an index
		for (String index : graphSchema.getIndexes())
			try ( Transaction tx = graphDb.beginTx() ) 
			{
				obtainIndex(index);
			}*/
		
		long endTime = System.currentTimeMillis();
		System.out.println(String.format("Done. Imported %d indexes over %d ms. Average %f ms per index", 
				graphIndex.length, endTime - beginTime, (float)(endTime - beginTime) / (float)graphIndex.length));

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
	
	private void importNode(GraphNode graphNode) throws Exception {
				
		String key = (String) graphNode.getProperties().get(AggrigationUtils.PROPERTY_KEY);
		if (null == key || key.isEmpty()) 
			throw new Exception("Error in node, the node key is empty");
		String type = (String) graphNode.getProperties().get(AggrigationUtils.PROPERTY_NODE_TYPE);
		if (null == type || type.isEmpty()) 
			throw new Exception("Error in node, the node type is empty");

		Set<String> labels = new HashSet<String>();
		if (graphNode.getProperties().containsKey(AggrigationUtils.PROPERTY_NODE_SOURCE)) {
			Object _source = graphNode.getProperties().get(AggrigationUtils.PROPERTY_NODE_SOURCE);
			if (null != _source) {
				if (_source instanceof String) 
					labels.add(getCorrectLabel((String) _source));
				else if (_source instanceof Collection<?>) 
					for (Object _label : (Collection<?>)_source) 
						labels.add(getCorrectLabel((String) _label));

				List<String> list = new ArrayList<String>();
				list.addAll(labels);
				graphNode.getProperties().put(AggrigationUtils.PROPERTY_NODE_SOURCE, list);
			}
		}

		labels.add(type);

		
	/*	String indexLabel;
		if (null != source && source.isEmpty())
			indexLabel = source + "_" + type;
		else
			indexLabel = type;
		Index<Node> index = getIndex(indexLabel);
		*/
		Index<Node> index = getIndex(type);
		Node node = (Node) index.get(AggrigationUtils.PROPERTY_KEY, key).getSingle();
		if (null == node) {
			node = graphDb.createNode();
				 
			++nodeCounter; 
				
			if (null != graphNode.getProperties())
				for (Entry<String, Object> entry : graphNode.getProperties().entrySet()) {  
					Object value = entry.getValue();
					if (null != value) {
						if (value instanceof Collection<?>) {
							String[] arr = ((Collection<?>) value).toArray(new String[((Collection<?>) value).size()]);
							if (arr.length == 1)
								node.setProperty(entry.getKey(), arr[0]);
							else if (arr.length > 1)
								node.setProperty(entry.getKey(), arr);
						}
						else
							node.setProperty(entry.getKey(), value);
					}
				}
			
			for (String label : labels)
				node.addLabel(DynamicLabel.label(label));
			
			index.add(node, AggrigationUtils.PROPERTY_KEY, key);
		}
	}
	
	private void importRelationship(GraphRelationship graphRelationship) throws Exception {
		Node nodeStart = findNode(graphRelationship.getStart());
		if (null == nodeStart) 
			throw new Exception("Error in graph, unable to find start node " + graphRelationship.getStart().getType() + " with key: " + graphRelationship.getStart().getKey());
		Node nodeEnd = findNode(graphRelationship.getEnd());
		if (null == nodeEnd)
			throw new Exception("Error in graph, unable to find end node " + graphRelationship.getEnd().getType() + " with key: " + graphRelationship.getEnd().getKey());

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
	
	private Index<Node> getIndex(String label) {
		if ( indexes.containsKey(label) ) 
			return indexes.get( label );
		
		Index<Node> index = graphDb.index().forNodes( label );
		indexes.put(label, index);
		return index;
	}
	
	private String getCorrectLabel(String label) throws Exception {
		label = label.toLowerCase();
		for (String l : labels)
			if (l.toLowerCase().equals(label))
				return l;
		
		return null;
	}

	private Node findNode(GraphConnection connection) {
		String indexLabel;
		if (connection.getSource() != null)
			indexLabel = connection.getSource() + "_" + connection.getType();
		else
			indexLabel = connection.getType();
				
		return getIndex(indexLabel)
				.get(AggrigationUtils.PROPERTY_KEY, connection.getKey())
				.getSingle();
		
	/*	ResourceIterable<Node> nodes = graphDb.findNodesByLabelAndProperty(
				DynamicLabel.label(connection.getType()), 
				AggrigationUtils.PROPERTY_KEY, 
				connection.getKey());
		
		try (ResourceIterator<Node> noded = nodes.iterator()) {
			if (noded.hasNext())
				return noded.next();
			else
				return null;
		}*/
	}
}
