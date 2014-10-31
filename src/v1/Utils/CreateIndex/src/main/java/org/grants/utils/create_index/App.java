package org.grants.utils.create_index;

import org.neo4j.graphdb.Node;
import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.RestAPIFacade;
import org.neo4j.rest.graphdb.entity.RestNode;
import org.neo4j.rest.graphdb.index.RestIndex;
//import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;

public class App {
	private static final String SERVER_ROOT_URI = "http://54.89.177.83:7474/db/data/";
	//private static final String SERVER_ROOT_URI = "http://localhost:7474/db/data/";
			
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		RestAPI graphDb = new RestAPIFacade(SERVER_ROOT_URI);  
		
		//GraphDatabaseService graphDb = new RestGraphDatabase(serverRoot);  
			// create a query engine
	//	RestCypherQueryEngine engine=new RestCypherQueryEngine(graphDb);  
		
		// Obtain an index on Grant
		RestIndex<Node> index = graphDb.index().forNodes("Service");
		
		Iterable<RestNode> nodes = graphDb.getNodesByLabel("Service");
		
		for (RestNode node : nodes) {
			index.add(node, "service_url", node.getProperty("service_url"));
			index.add(node, "name", node.getProperty("name"));
		}
	}

}
