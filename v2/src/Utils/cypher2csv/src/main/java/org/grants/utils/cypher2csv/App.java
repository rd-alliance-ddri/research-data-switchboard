package org.grants.utils.cypher2csv;

import java.util.Map;
import java.util.regex.Pattern;

import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.RestAPIFacade;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;
import org.neo4j.rest.graphdb.util.QueryResult;

public class App {
	private static final String NEO4J_URL = "http://localhost:7474/db/data/";
	
	private static final Pattern NEO4J_URL_PATTERN = Pattern.compile("^https?://[\\w._-]+:[\\d]+/db/data/$");
	private static final Pattern NEO4J_URL_PATTERN2 = Pattern.compile("^https?://[\\w._-]+:[\\d]+$");
	private static final Pattern NEO4J_URL_PATTERN3 = Pattern.compile("^[\\w._-]+:[\\d]+$");
	private static final Pattern NEO4J_URL_PATTERN4 = Pattern.compile("^[\\d]+$");
	
	public static void main(String[] args) {
		
		String query = null;
		String neo4jUrl = null;
		
		for (String arg : args) {
			if (null == neo4jUrl) {
				if (NEO4J_URL_PATTERN.matcher(arg).find()) {
					neo4jUrl = arg;
				} else if (NEO4J_URL_PATTERN2.matcher(arg).find()) {
					neo4jUrl = arg + "/db/data/";
				} else if (NEO4J_URL_PATTERN3.matcher(arg).find()) {
					neo4jUrl = "http://" + arg + "/db/data/";
				} else if (NEO4J_URL_PATTERN4.matcher(arg).find()) {
					neo4jUrl = "http://localhost:" + arg + "/db/data/";
				} else {
					neo4jUrl = NEO4J_URL;
					query = arg;
				}
			} else {
				query = arg;
				break;
			}
		}
			
		if (null == query)
		{
			System.out.println("Ussage: java -jar cypher2csv.jar [NEO4J URL] \"CYPHER QUERY\" [> OUTPUT FILE]");
			return;
		}
		
//		System.out.println(neo4jUrl);
//		System.out.println(query);
		
		// Connect to neo4j
		RestAPI graphDb = new RestAPIFacade(neo4jUrl);  
		
		// Create cypher engine
		RestCypherQueryEngine engine = new RestCypherQueryEngine(graphDb);  
	
		StringBuilder header = null;
		StringBuilder line = null;
		String key, value;
		
		boolean bHeader = false;
		
		/* Query all JSON records */
		QueryResult<Map<String, Object>> nodes = engine.query(query.toString(), null);
		for (Map<String, Object> row : nodes) {
			for (Map.Entry<String, Object> entry : row.entrySet()) {
				if (!bHeader) {
					key = escapeString(entry.getKey());
					
					if (null == header)
						header = new StringBuilder(key);
					else {
						header.append(",");
						header.append(key);
					}
				}
				
				if (null == entry.getValue())
					value = "null";
				else 
					value = escapeString(entry.getValue().toString());

				if (null == line)
					line = new StringBuilder(value);
				else {
					line.append(",");
					line.append(value);
				}
			}
			
			if (!bHeader) {
				System.out.println(header);
				bHeader = true;
			}

			System.out.println(line);
			line = null;				
		}
	}
	
	private static String escapeString(String str) {
		if (str.contains(" ")) 
			return '"' + str + '"';
		else
			return str;
	}

}
