package org.grants.tests.test_nodes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Relationship;
import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.RestAPIFacade;
import org.neo4j.rest.graphdb.entity.RestNode;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;
import org.neo4j.rest.graphdb.util.QueryResult;

import au.com.bytecode.opencsv.CSVReader;

public class Test {
	private static final String TEST_LOG = "test.log";
	
	private static final String[] NODE_LABELS = { "Grant", "Publication" };
	
	private RestAPI graphDb;
	private RestCypherQueryEngine engine;
	private PrintWriter logWriter;
	
	private int nodesTested;
	private int connectionsExisting;
	private int connectionsMissing;
	private int connectionsOmmited;
			
	public Test(final String neo4jUrl) throws FileNotFoundException, UnsupportedEncodingException {
		graphDb = new RestAPIFacade(neo4jUrl);
		engine = new RestCypherQueryEngine(graphDb);  
		
		logWriter = new PrintWriter(TEST_LOG, "UTF-8");
		
		//engine.query("CREATE CONSTRAINT ON (n:" + LABEL_WEB_INSTITUTION + ") ASSERT n." + PROPERTY_KEY + " IS UNIQUE", Collections.<String, Object> emptyMap());
		
		//indexWebInstitution = graphDb.index().forNodes(LABEL_WEB_INSTITUTION);
	}
	
	public void testNodes(String testFolder) {
		nodesTested = 
				connectionsExisting = 
				connectionsMissing = 
				connectionsOmmited = 0;
		
		File[] files = new File(testFolder).listFiles();
		for (File file : files) 
			if (!file.isDirectory()) 
				testNode(file);
		
		log (nodesTested + "tests has been completed. Found total " 
				+ connectionsExisting + " exising, " 
				+ connectionsMissing + " missing and " 
				+ connectionsOmmited + " ommited connections");
	}
	
	private void testNode(File node) {
		try {
			log ("Starting new test");
			
			RestNode nodeStart = null;
			List<RestNode> nodesEnd = null;
			Set<Long> existingId = new HashSet<Long>();
			
			CSVReader reader = new CSVReader(new FileReader(node));
			
			String[] lines;
			boolean header = false;
			while ((lines = reader.readNext()) != null) 
			{
				if (!header)
				{
					header = true;
					continue;
				}
				if (lines.length != 3)
					continue;
				
				String type = lines[0];
				String field = lines[1];
				String value = lines[2];
				
				log ("Searching for "+ type + ": " + field + "=" + value);
				
				List<RestNode> nodes = findNode(type, field, value);
				if (null == nodeStart) {
					int nodesCnt = checkNodes(nodes);
					if (0 == nodesCnt) {
						log ("Unable to find any Start node, the test is FAILED");
						
						return;
					} else if (nodesCnt > 1) {
						log ("Too many Start nodes has been found by this request, the test is FAILED");
						
						return;
					}
					
					nodeStart = nodes.get(0);
				} else {
					if (checkNodes(nodes) > 0) {
						if (null == nodesEnd)
							nodesEnd = nodes;
						else
							nodesEnd.addAll(nodes);
					} 
				}
			}
			
			reader.close();
			
			if (null == nodesEnd) {
				log ("Unable to find any End node, the test is FAILED");
				
				return;
			}
			
			++nodesTested;
			
			int existing = 0;
			int missing = 0;
			int ommited = 0;
			
			Iterable<Relationship> rels = nodeStart.getRelationships(Direction.BOTH);	
			for (Relationship rel : rels) {
				boolean relationshipFound = false;
				
				RestNode nodeTest = (RestNode) rel.getOtherNode(nodeStart);
				if (checkNodeLabels(nodeTest)) {
					for (RestNode nodeEnd : nodesEnd)
						if (nodeTest.getId() == nodeEnd.getId()) {
							
							relationshipFound = true;
							existingId.add(nodeEnd.getId());
							++existing;
							
							break;
						}
					
					if (!relationshipFound)
						++ommited;
				}
			}

			for (RestNode nodeEnd : nodesEnd)
				if (!existingId.contains(nodeEnd.getId()))
					++missing;			
			
			log ("Test PASSED. Found " + existing + " exising, " 
					+ missing + " missing and " + ommited + " ommited connections");
			
			connectionsExisting += existing;
			connectionsMissing += missing;
			connectionsOmmited += ommited;			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void log(String message) {
		logWriter.println(message);
		System.out.println(message);
	}
	
	private int checkNodes(List<RestNode> nodes) {
		if (null == nodes) {
			log ("Found 0 results");
			
			return 0;
		}
		StringBuilder sb = new StringBuilder();
		sb.append("Found ");
		sb.append(nodes.size());
		sb.append(" results: [");
		
		boolean comma = false;
		for (RestNode node : nodes) {
			if (comma)
				sb.append(", ");
			else
				comma = true;
			
			sb.append(node.getId());
		}
		
		sb.append("]");
		
		log(sb.toString());
			
		return nodes.size();
	}
	
	private boolean checkNodeLabels(RestNode node) {
		for (String label : NODE_LABELS) 
			if (node.hasLabel(DynamicLabel.label(label)))
				return true;
		
		return false;
	}
	
	private List<RestNode> findNode(String type, String field, String value) {
		List<RestNode> result = null;
		
		Map<String, Object> pars = new HashMap<String, Object>();
		pars.put(field, value);
		
		QueryResult<Map<String, Object>> nodes = engine.query("MATCH (n:" + type + ") WHERE n." + field + "={" + field + "} RETURN n LIMIT 25", pars);
		for (Map<String, Object> row : nodes) {
			RestNode node = (RestNode) row.get("n");
			if (null != node) {
				if (null == result)
					result = new ArrayList<RestNode>();
				
				result.add(node);
			}
		}
		
		return result;
	}
	
	private Relationship findAnyRelationship(RestNode nodeStart, RestNode nodeEnd) {
		Iterable<Relationship> rels = nodeStart.getRelationships(Direction.BOTH);	
		for (Relationship rel : rels) 
			if (rel.getOtherNode(nodeStart).getId() == nodeEnd.getId())
				return rel;

		return null;
	}
}
