package org.grants.importers.grants;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import org.grants.google.cse.Query;
import org.grants.google.cse.QueryResponse;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.RestAPIFacade;
import org.neo4j.rest.graphdb.entity.RestNode;
import org.neo4j.rest.graphdb.index.RestIndex;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;
import org.neo4j.rest.graphdb.util.QueryResult;

public class Importer {

	private static final String CSE = "009969831710440871878:5grzvn8ipgw";
	private static final String API_KEY = "AIzaSyCCXNoW06VwXWQcOTCAsIMyxo1Z0pUip_o";
	
	private final String folderJson;
	private final String folderCache;
	
	private Query query = new Query(CSE, API_KEY);
	private RestAPI graphDb;
	private RestCypherQueryEngine engine;
	//private RestIndex<Node> indexDryadRecord;
	
	public Importer(final String folderJson, final String folderCache, final String neo4jUrl) {
		this.folderJson = folderJson;
		this.folderCache = folderCache;
		
		new File(this.folderJson).mkdirs();
		new File(this.folderCache).mkdirs();
		
		graphDb = new RestAPIFacade(neo4jUrl); //"http://localhost:7474/db/data/");  
		engine = new RestCypherQueryEngine(graphDb);  
	}
	
	public void importGrants() {
		QueryResult<Map<String, Object>> nodes = engine.query("MATCH (n:RDA:Grant) RETURN ID(n) AS id, n.name_primary AS primary, n.name_alternative AS alternative", null);
		for (Map<String, Object> row : nodes) {
			int nodeId = (int) row.get("id");
			String namePrimary = (String) row.get("primary");
			String nameAlternative = (String) row.get("alternative");
			
			if (null != namePrimary && !namePrimary.isEmpty()) {
				QueryResponse response = query.query(namePrimary);
				
				System.out.println(response);
			}
	
		}
	}
}
