package org.grants.utils.black_list;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
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

public class BlackListCreator {
	
	private static final int MAX_RELATIONSHIPS = 20;
	
	private GraphDatabaseService graphDb;
	private GlobalGraphOperations global;
	
	private Set<String> blackList;
	private Set<String> trashList;
	private Set<String> wirredList;
	
	private String fileBlackList;
	private String fileTrashList;
	private String fileWirredList;

	public BlackListCreator(String folderNeo4j, 
			String fileBlackList, String fileTrashList, String fileWirredList) 
					throws FileNotFoundException, IOException {
		System.out.println("Source Neo4j folder: " + folderNeo4j);
		System.out.println("Black List File: " + fileBlackList);
		System.out.println("Trash List File: " + fileTrashList);
		System.out.println("Wirred List File: " + fileWirredList);
		
		this.graphDb = GraphUtils.getReadOnlyGraphDb(folderNeo4j);
		this.global = GraphUtils.getGlobalOperations(graphDb);
		
		this.fileBlackList = fileBlackList;
		this.fileTrashList = fileTrashList;
		this.fileWirredList = fileWirredList;
		
		this.blackList = GraphUtils.loadBlackList(fileBlackList);
		this.trashList = GraphUtils.loadBlackList(fileTrashList);
		this.wirredList = GraphUtils.loadBlackList(fileWirredList);
		
		/*for (Iterator<String> iterator = blackList.iterator(); iterator.hasNext();) {
		    String title = iterator.next();
		    if (GraphUtils.isTitleInvalid(title)) {
		    	trashList.add(title);
		        iterator.remove();
		    }
		}*/
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
		}
		
		System.out.println("Done. Tested " + stringCount + " strings");
	}
	
	public void processString(Node node, String value) {
		String title = value.trim().toLowerCase();
		if (!blackList.contains(title) && !trashList.contains(title) && !wirredList.contains(title)) {
			if (GraphUtils.isTitleInvalid(title)) {
				trashList.add(title);
			} else {
				Iterator<Relationship> rels = node.getRelationships().iterator();
				int relCounter = 0;
				
				while (rels.hasNext()) {
					rels.next();
					++relCounter;
				}
				
				if (relCounter > MAX_RELATIONSHIPS)
					wirredList.add(title);
			}
		}
	}

	public void save() throws FileNotFoundException, IOException {
		GraphUtils.saveBlackList(fileBlackList, blackList);
		GraphUtils.saveBlackList(fileTrashList, trashList);
		GraphUtils.saveBlackList(fileWirredList, wirredList);
	}
}
