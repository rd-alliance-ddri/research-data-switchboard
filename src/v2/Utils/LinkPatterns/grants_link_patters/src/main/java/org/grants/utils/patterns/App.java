package org.grants.utils.patterns;

import java.util.Map;

import javax.xml.bind.JAXBException;

import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.RestAPIFacade;
import org.neo4j.rest.graphdb.entity.RestNode;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;
import org.neo4j.rest.graphdb.util.QueryResult;

public class App {
	private static final String NEO4J_URL = "http://ec2-54-69-203-235.us-west-2.compute.amazonaws.com:7476/db/data/";//"http://localhost:7474/db/data/";

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		try {
			Patterns patterns = new Patterns();
			
			patterns.loadLinks("publications/cache/page");
			patterns.identifyPatterns();
			
			Map<String, Pattern> map = patterns.getUniquePatterns();
			
			RestAPI graphDb = new RestAPIFacade(NEO4J_URL); //"http://localhost:7474/db/data/");  
			RestCypherQueryEngine engine = new RestCypherQueryEngine(graphDb);

			QueryResult<Map<String, Object>> nodes = engine.query("MATCH (n:Web:Institution) RETURN n.host", null);
			for (Map<String, Object> row : nodes) {
				String host = (String) row.get("n.host");
				
				Pattern p = map.get(host);
				if (null != p && p.getCount() > 1) 
					System.out.println("Link: " + p.getLink() + ", Pattern: " + p.getPattern() + ", Count: " + p.getCount());
			}
			
			/*for (String link : patterns.getLinks())
				System.out.println(link);*/
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
