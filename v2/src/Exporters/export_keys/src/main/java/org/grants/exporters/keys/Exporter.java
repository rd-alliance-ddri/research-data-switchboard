package org.grants.exporters.keys;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.grants.neo4j.local.Neo4jUtils;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;

import au.com.bytecode.opencsv.CSVWriter;

public class Exporter {
	private GraphDatabaseService graphDb;
	private GlobalGraphOperations global;
	//private ExecutionEngine engine;
	
	private File exportFolder;
	private long keysCounter;
	private long rowCounter;
	private long fileCounter;
	private int pagination;
	
	private File folder1;
	private File folder2;
	
	private CSVWriter writer1;
	private CSVWriter writer2;
	
	public Exporter(String dbFolder, final String outputFolder) {
		System.out.println("Neo4j folder: " + dbFolder);
		System.out.println("Target folder: " + outputFolder);
	
		graphDb = Neo4jUtils.getReadOnlyGraphDb(dbFolder);
		global = Neo4jUtils.getGlobalOperations(graphDb);
		//engine = Neo4jUtils.getExecutionEngine(graphDb);
		
		// Set output folder
		exportFolder= new File(outputFolder);
		exportFolder.mkdirs();
	}	
	
	public void process(String label1, String property1, String label2, String property2, 
			int minRels, int maxRels, int pagination) throws IOException {
		this.keysCounter = 0;
		this.rowCounter = 0;
		this.fileCounter = 0;
		this.pagination = pagination;
				
		folder1 = new File(exportFolder, label1 + "/" + label2);
		folder2 = new File(exportFolder, label2 + "/" + label1);
		folder1.mkdirs();
		folder2.mkdirs();

		long beginTime = System.currentTimeMillis();
		
		Label _label1 = DynamicLabel.label(label1);
		Label _label2 = DynamicLabel.label(label2);
		
		try ( Transaction tx = graphDb.beginTx() ) {
			ResourceIterable<Node> nodes = global.getAllNodesWithLabel(_label1);
			for (Node node1 : nodes) 
				if (node1.hasProperty(property1)) {
					List<Node> related = getRelatedNodes(node1,_label1, property1, _label2, property2, maxRels, null, null);
					for (Node node2 : related) {
						Object key1 = node1.getProperty(property1);
						Object key2 = node2.getProperty(property2);
							
						initExporter(label1, label2);
							
						if (key1 instanceof String)
							exportKey1((String)key1, key2);
						else if (key1 instanceof String[]) 
							for (String key : (String[]) key1) 
								exportKey1(key, key2);
						else 
							throw new IOException("Invalid key1: " + key1);
					}
				}
		}
		
		
		/*
		String cypher = getCypherQuery(label2, property2, minRels, maxRels);
		
		Map<Long, Object> keys = getKeys(_label1, property1);
		Map<String, Object> pars = new HashMap<String, Object>();
		
		for (Map.Entry<Long, Object> entry : keys.entrySet()) {
			try ( Transaction tx = graphDb.beginTx() ) {
				System.out.println("Node: " + entry.getKey());

				pars.put("id", node.getKey());
				Object key1 = node.getValue();
				ExecutionResult result = engine.execute( cypher , pars);
				ResourceIterator<Object> columnKey = result.columnAs("key");
				for (Object key2 : IteratorUtil.asIterable(columnKey)) {
					initExporter(label1, label2);
					
					if (key1 instanceof String)
						exportKey1((String)key1, key2);
					else if (key1 instanceof String[]) 
						for (String key : (String[]) key1) 
							exportKey1(key, key2);
					else 
						throw new IOException("Invalid key1: " + key1);
				}
			}
		}*/
		
		if (null != writer1)
			writer1.close();
		if (null != writer2)
			writer2.close();
		
		keysCounter += rowCounter;
		
		long endTime = System.currentTimeMillis();
		
		System.out.println(String.format("Done. Exported %d keys over %d ms. Average %f ms per key", 
				keysCounter, endTime - beginTime, (float)(endTime - beginTime) / (float)keysCounter));
	}
	
	private List<Node> getRelatedNodes(Node node, 
			Label label1, String property1, Label label2, String property2, int depth, 
			Set<Long> set, List<Node> list) {
		// create objects if they not exist
		if (null == set)
			set = new HashSet<Long>();
		if (null == list)
			list = new ArrayList<Node>();
		
		// recod this node id, so we do not need to check it again
		set.add(node.getId());
			
		// query all it's relationships
		Iterable<Relationship> rels = node.getRelationships();
		for (Relationship rel : rels) {
			// obtain other node
			Node other = rel.getOtherNode(node);
			// check what node do exist and we haven't processed that node already
			if (null != other && !set.contains(other.getId())) {
				// check do we have found the target node
				if (other.hasLabel(label2) && other.hasProperty(property2)) {
					// the node has been found, record it
					list.add(other);
					set.add(other.getId());
					// if node is not a target and depth allows it, check it connections
					// optional check what found node does not contains an input property, since we will check it later
				} else if (depth > 0 && (!other.hasLabel(label1) || !other.hasProperty(property1)))
					getRelatedNodes(other, label1, property1, label2, property2, depth -1, set, list);
			}
		}
		
		// return result list
		return list;
	}
	
	private void initExporter(String label1, String label2) throws IOException {
		if (null == writer1) {
			System.out.println("Recording file " + fileCounter);
			String fileName = (fileCounter++) + ".csv";
			
			File file1 = new File(folder1, fileName);
			File file2 = new File(folder2, fileName);
			writer1 = new CSVWriter(new FileWriter(file1));
			writer2 = new CSVWriter(new FileWriter(file2));

			writer1.writeNext(new String[] {label1, label2});
			writer2.writeNext(new String[] {label2, label1});
		}	
	}
	
	private void exportKey1(String key1, Object key2) throws IOException {
		if (key2 instanceof String) 
			exportKey2(key1, (String) key2);
		else if (key2 instanceof String[]) 
			for (String key : (String[]) key2) 
				exportKey2(key1, key);
		else 
			throw new IOException("Invalid key2: " + key2);
	}

	private void exportKey2(String key1, String key2) throws IOException {
		++rowCounter;
		
		//System.out.println(key1 + " - " + key2);
		
		writer1.writeNext(new String[] {key1, key2});
		writer2.writeNext(new String[] {key2, key1});
		
		if (pagination > 0 && rowCounter >= pagination) {
			writer1.close();
			writer1 = null;
			writer2.close();
			writer2 = null;
			
			keysCounter += rowCounter;
			rowCounter = 0;
		}
	}
	
	/*private Map<Long, Object> getKeys(Label label, String property) {
		Map<Long, Object> result = null;
		
		try ( Transaction tx = graphDb.beginTx() ) {
			ResourceIterable<Node> nodes = global.getAllNodesWithLabel(label);
			for (Node node : nodes) 
				if (node.hasProperty(property)) {
					if (null == result)
						result = new HashMap<Long, Object>();
					
					result.put(node.getId(), node.getProperty(property));
				}
		}
		
		return result;
	}
	
	private String getCypherQuery(String label, String property, int minRels, int maxRels) {
		StringBuilder sb = new StringBuilder();
		sb.append("MATCH (n1)-[*");
		if (minRels > 0 || maxRels > 0) {
			if (minRels == maxRels) 
				sb.append(minRels);
			else {
				if (minRels > 0)
					sb.append(minRels);
				sb.append("..");
				if (maxRels > 0)
					sb.append(maxRels);
			}
		}
		sb.append("]-(n2:`");
		sb.append(label);
		sb.append("`) WHERE ID(n1) = {id} AND HAS(n2.`");
		sb.append(property);
		sb.append("`) RETURN DISTINCT n2.key AS key");
		
		return sb.toString();
	}*/
}
