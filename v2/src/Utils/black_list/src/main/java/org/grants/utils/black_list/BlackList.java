package org.grants.utils.black_list;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.grants.graph.GraphUtils;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class BlackList {
	
	private static final int MAX_RELATIONSHIPS = 20;
	
	private static final String E_01 = "E01";
	private static final String E_02 = "E02";
	private static final String E_03 = "E03";
	
	private static final String W_01 = "W01";
	
	private static final String E_01_DESC = "The title has been blacklisted";
	private static final String E_02_DESC = "The title is less that 20 symbols";
	private static final String E_03_DESC = "The title is less that two words";
	
	private static final String W_01_DESC = "The node has more that 20 connections to Web:Researcher";
	
	private GraphDatabaseService graphDb;
	private GlobalGraphOperations global;
	
	private Set<String> blackList;
	private Set<String> trashList;
	private Set<String> wirredList;
	
	private Operation operation;
	
	private String fileBlackList;
	
	private CSVWriter exceptions;
	private CSVWriter warnings;
	
	Label labelWeb = DynamicLabel.label( "Web" );
	Label labelResearcher = DynamicLabel.label( "Researcher" );
	
	public enum Operation {
		test, execute
	};

	public BlackList(Operation operation, String folderNeo4j, String fileBlackList) 
					throws FileNotFoundException, IOException {
		System.out.println("Source Neo4j folder: " + folderNeo4j);
		System.out.println("Black List File: " + fileBlackList);
		
		this.operation = operation;
		
		this.graphDb = GraphUtils.getReadOnlyGraphDb(folderNeo4j);
		this.global = GraphUtils.getGlobalOperations(graphDb);
		
		this.fileBlackList = fileBlackList;
		
		this.blackList = GraphUtils.loadBlackList(fileBlackList);
		
		//this.exceptions = new PrintWriter("exceptions.log");
		//this.warnings = new PrintWriter("warnings.log");
		
		this.exceptions = new CSVWriter(new FileWriter("exceptions.csv"));
		this.warnings = new CSVWriter(new FileWriter("warnings.csv"));
		
		this.exceptions.writeNext(new String[] { "node source", "node type", "node key", "node title", "exceptioncode", "exception description" });
		this.warnings.writeNext(new String[] { "node source", "node type", "node key", "node title", "warning code", "warning description" });
	}
	
	public void processNodes(String source, String type, String field) {
		System.out.println("Processing " + source + ":" + type + "(" + field + ")");
		int stringCount = 0;
		
		try ( Transaction tx = graphDb.beginTx() ) {
			Label labelSource = DynamicLabel.label( source );
			Label labelType = DynamicLabel.label( type );
			
			ResourceIterable<Node> nodes = global.getAllNodesWithLabel( labelSource );
			for (Node node : nodes) 
				if (node.hasLabel(labelType) &&  node.hasProperty(field)) {
					Object value = node.getProperty(field);
					if (value instanceof String) {
						++stringCount;
						processString(node, (String) value);
					} else if (value instanceof String[]) {
						for (String s : (String[]) value) {
							++stringCount;
							processString(node, s);
						}
					} else if (value instanceof Iterable<?>) {
						for (Object v : (Iterable<?>) value) 
							if (value instanceof String) {
								++stringCount;
								processString(node, (String) v);
							}
					}
				}
			
			tx.success();
		}
		
		System.out.println("Done. Tested " + stringCount + " strings");
	}
	
	public void processString(Node node, String value) {
		String title = value.trim().toLowerCase();
		if (blackList.contains(title)) {
			if (operation == Operation.execute)
				cleanRelationships(node);
			logNode(exceptions, node, title, E_01, E_01_DESC); 
		} else if (GraphUtils.isTitleShort(title)) {
			if (operation == Operation.execute)
				cleanRelationships(node);
			logNode(exceptions, node, title, E_02, E_02_DESC); 
		} else if (GraphUtils.isTitleSimple(title)) {
			if (operation == Operation.execute)
				cleanRelationships(node);
			logNode(exceptions, node, title, E_03, E_03_DESC); 
		} else if (!checkRelationships(node)) {
			logNode(warnings, node, title, W_01, W_01_DESC); 
		}
	}
	
	public boolean checkRelationships(Node node) {
		int relCount = 0;
		Iterator<Relationship> rels = node.getRelationships().iterator();
		while (rels.hasNext()) {
			Relationship rel = rels.next();
			Node startNode = rel.getStartNode();
			Node endNode = rel.getEndNode();
			
			if (startNode.getId() == node.getId() && endNode.hasLabel(labelWeb) && endNode.hasLabel(labelResearcher) ||
				endNode.getId() == node.getId() && startNode.hasLabel(labelWeb) && startNode.hasLabel(labelResearcher)) {
				
				if (++relCount > MAX_RELATIONSHIPS)
					return false;
			}
		}
		
		return true;
	}

	public void cleanRelationships(Node node) {
		Iterator<Relationship> rels = node.getRelationships().iterator();
		while (rels.hasNext()) {
			Relationship rel = rels.next();
			Node startNode = rel.getStartNode();
			Node endNode = rel.getEndNode();
			
			if (startNode.getId() == node.getId() && endNode.hasLabel(labelWeb) && endNode.hasLabel(labelResearcher) ||
				endNode.getId() == node.getId() && startNode.hasLabel(labelWeb) && startNode.hasLabel(labelResearcher)) {
				
				rel.delete();
			}
		}
	}
	
	public void logNode(CSVWriter csvWriter, Node node, String title, String code, String description) {
		String[] line = new String[] {
				(String) node.getProperty(GraphUtils.PROPERTY_NODE_SOURCE, ""),
				(String) node.getProperty(GraphUtils.PROPERTY_NODE_TYPE, ""),
				(String) node.getProperty(GraphUtils.PROPERTY_KEY, ""),
				title,
				code,
				description
		};
		
		csvWriter.writeNext(line);
	}

/*	public void save() throws FileNotFoundException, IOException {
		GraphUtils.saveBlackList(fileBlackList, blackList);
		GraphUtils.saveBlackList(fileTrashList, trashList);
		GraphUtils.saveBlackList(fileWirredList, wirredList);
	}*/
}
