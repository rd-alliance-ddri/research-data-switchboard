package org.grants.connectors.web_researcher_connector;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.RestAPIFacade;
import org.neo4j.rest.graphdb.entity.RestNode;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;
import org.neo4j.rest.graphdb.util.Config;
import org.neo4j.rest.graphdb.util.QueryResult;

/**
 * Connector class 
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */
public class Connector {
	
	private static final String LABEL_GRANT = "Grant";
	private static final String LABEL_RESEARCHER = "Researcher";
	
	private static final String LABEL_NHMRC = "NHMRC";
	private static final String LABEL_ARC = "ARC";
	private static final String LABEL_WEB = "Web";
	
	private static final String FIELD_NHMRC_GRANT_ID = "nhmrc_grant_id";
	private static final String FIELD_ARC_GRANT_ID = "arc_grant_id";
	
	private static final String FIELD_SCIENTIFIC_TITLE = "scientific_title";
	private static final String FIELD_SIMPLIFIED_TITLE = "simplified_title";
	
	private static final String FIELD_FULL_NAME = "full_name";
	private static final String FIELD_UNI_URL = "university_url";
	private static final String FIELD_UNI_HAVE_GRANTS = "have_grants";
	
	private static final String RESULT_NHMRC_GRANT_ID = "n." + FIELD_NHMRC_GRANT_ID;
	private static final String RESULT_ARC_GRANT_ID = "n." + FIELD_ARC_GRANT_ID;
	
	private static final String RESULT_SCIENTIFIC_TITLE = "n." + FIELD_SCIENTIFIC_TITLE;
	private static final String RESULT_SIMPLIFIED_TITLE = "n." + FIELD_SIMPLIFIED_TITLE;
	
	private static final String RESULT_ID = "id(n)";
	private static final String RESULT_FULL_NAME = "n." + FIELD_FULL_NAME;
	private static final String RESULT_UNI_URL = "n." + FIELD_UNI_URL;
	private static final String RESULT_UNI_HAVE_GRANTS = "n." + FIELD_UNI_HAVE_GRANTS;
	
	private static enum RelTypes implements RelationshipType
    {
        Investigator
    }
	
	public void Connect(final String serverRoot) {
		System.setProperty(Config.CONFIG_STREAM, "true");
		
		// connect to graph database
		RestAPI graphDb = new RestAPIFacade(serverRoot);  
		RestCypherQueryEngine engine=new RestCypherQueryEngine(graphDb); 
		
		System.out.println("Loading NHMRC Titles");
		
		// 1. Read all NHMRC grants id and titles
		QueryResult<Map<String, Object>> titlesNHMRC = engine.query("MATCH (n:" + LABEL_GRANT + ":" + LABEL_NHMRC + ") return " + RESULT_ID + ", " + RESULT_SCIENTIFIC_TITLE + ", " + RESULT_SIMPLIFIED_TITLE, null);
		
		System.out.println("Loading ARC Titles");
		
		// 2. Read all ARC grants id and titles
		QueryResult<Map<String, Object>> titlesARC = engine.query("MATCH (n:" + LABEL_GRANT + ":" + LABEL_ARC + ") return " + RESULT_ID + ", " + RESULT_SCIENTIFIC_TITLE, null);
		
		System.out.println("Loading Researchers");
		
		//1 Enumerate all Web:Researchers and get all URL's
		QueryResult<Map<String, Object>> researchers = engine.query("MATCH (n:" + LABEL_RESEARCHER + ":" + LABEL_WEB + ") return " + RESULT_ID + ", " + RESULT_FULL_NAME + ", " + RESULT_UNI_URL + ", " + RESULT_UNI_HAVE_GRANTS, null);
		for (Map<String, Object> rowResearcher : researchers) {
			
			Integer nodeId = (Integer) rowResearcher.get(RESULT_ID);
			String fullName = (String) rowResearcher.get(RESULT_FULL_NAME);
			String uniURL = (String) rowResearcher.get(RESULT_UNI_URL);
			
			System.out.println("Processing Researcher: " + fullName);
			if (uniURL != null && !uniURL.isEmpty()) {
				try {
					System.out.println("Downloading URL: " + uniURL);
					String html = DownloadPage(uniURL, null);
					
					Writer writer = new BufferedWriter(new OutputStreamWriter(
					          new FileOutputStream(new File("html/" + nodeId + ".html")), "utf-8"));
					
					writer.write(html);
					writer.close();
					
					// check grants info
					
					for (Map<String, Object> rowHNMRCTitle : titlesNHMRC) { 
						String scientificTitle = (String) rowHNMRCTitle.get(RESULT_SCIENTIFIC_TITLE);
						String simplifiedTitle = (String) rowHNMRCTitle.get(RESULT_SIMPLIFIED_TITLE);
						
						if (scientificTitle.equals("Development of") 
								|| scientificTitle.equals("Using") 
								|| scientificTitle.equals("Research Fellowship"))
							scientificTitle = null;
						
						if (simplifiedTitle.equals("Research Fellowship"))
							simplifiedTitle = null;
						
						if (null != scientificTitle && !scientificTitle.isEmpty() && html.contains(scientificTitle)) 
					    {
							System.out.println("MATCH has been found on NHMRC scientific title: " + scientificTitle);

							Integer grantId = (Integer) rowHNMRCTitle.get(RESULT_ID); 
							
							RestNode nodeResearcher = graphDb.getNodeById(nodeId);
							RestNode nodeGrant = graphDb.getNodeById(grantId);
							
							CreateUniqueRelationship(nodeResearcher, nodeGrant, RelTypes.Investigator, false);							
							
						} else if (null != simplifiedTitle && !simplifiedTitle.isEmpty() && html.contains(simplifiedTitle))
						{
							System.out.println("MATCH has been found on NHMRC simplified title: " + scientificTitle);
							
							Integer grantId = (Integer) rowHNMRCTitle.get(RESULT_ID); 
							
							RestNode nodeResearcher = graphDb.getNodeById(nodeId);
							RestNode nodeGrant = graphDb.getNodeById(grantId);
							
							CreateUniqueRelationship(nodeResearcher, nodeGrant, RelTypes.Investigator, false);							
						}
					}
					
					for (Map<String, Object> rowARCTitle : titlesARC) { 
						String scientificTitle = (String) rowARCTitle.get(RESULT_SCIENTIFIC_TITLE);
						
						if (null != scientificTitle && !scientificTitle.isEmpty() && html.contains(scientificTitle)) 
						{
							System.out.println("MATCH has been found on ARC scientific title: " + scientificTitle);
						
							Integer grantId = (Integer) rowARCTitle.get(RESULT_ID); 
							
							RestNode nodeResearcher = graphDb.getNodeById(nodeId);
							RestNode nodeGrant = graphDb.getNodeById(grantId);
							
							CreateUniqueRelationship(nodeResearcher, nodeGrant, RelTypes.Investigator, false);							
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					System.out.println("Invalid URL");
				}			
			}
		}
		
		
		// make sure we have an index on ARC_Grant:arc_project_id
		// RestAPI does not supported indexes created by schema, so we will use Cypher for that
		/*QueryResult<Map<String, Object>> result = engine.query("MATCH (n:Grant:NHMRC) return n.nhmrc_grant_id, n.scientific_title, n.simplified_title", null);
		
		int nResult = 0;
		
		for (Map<String, Object> row : result) {
			
			System.out.println("Result: " + nResult);
			
			for (Map.Entry<String, Object> entry : row.entrySet())
			{
    			String key = entry.getKey();
    			Object value = entry.getValue();
			}
			
			Iterator it = row.entrySet().iterator();
		    while (it.hasNext()) {
			
		    	 Map.Entry pairs = (Map.Entry)it.next();
		    	 
		    	 System.out.println(pairs.getKey() + " = " + pairs.getValue());
		    	 it.remove(); // avoids a ConcurrentModificationException
		    }
		    
			
			++nResult;
		}*/
		
		System.out.println("Done!");
		
	}
	
	public String DownloadPage(final String url, final String cookies) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		conn.setReadTimeout(5000);
		if (null != cookies && !cookies.isEmpty())
			conn.setRequestProperty("Cookie", cookies);
		conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
		conn.addRequestProperty("User-Agent", "Mozilla");
		//conn.addRequestProperty("Referer", "google.com");
	 
		// normally, 3xx is redirect
		int status = conn.getResponseCode();
		if (status != HttpURLConnection.HTTP_OK) {
			if (status == HttpURLConnection.HTTP_MOVED_TEMP
				|| status == HttpURLConnection.HTTP_MOVED_PERM
				|| status == HttpURLConnection.HTTP_SEE_OTHER) 				
			return DownloadPage(conn.getHeaderField("Location"), conn.getHeaderField("Set-Cookie"));
		}
		
		BufferedReader in = new BufferedReader(
				new InputStreamReader(conn.getInputStream()));
		
		String inputLine;
		StringBuffer html = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			html.append(inputLine);
		}
		
		in.close();


		return html.toString();		
	}
	
	private void CreateUniqueRelationship(RestNode nodeStart, RestNode nodeEnd, 
			RelTypes type, boolean checkOpposite) {
		// get all node relationships. They should be empty for a new node
		Iterable<Relationship> rels = nodeStart.getRelationships(type);
		
		for (Relationship rel : rels) {
			long startId = rel.getStartNode().getId();
			long endId = rel.getEndNode().getId();
			
			// check that relationship exists
			if (startId == nodeStart.getId() && endId == nodeEnd.getId() || 
			    checkOpposite && startId == nodeEnd.getId() && endId == nodeStart.getId())
				return;
		} 
		
		nodeStart.createRelationshipTo(nodeEnd, type);
	}	
}
