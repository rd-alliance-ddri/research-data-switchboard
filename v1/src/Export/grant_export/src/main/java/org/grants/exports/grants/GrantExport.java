package org.grants.exports.grants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.RestAPIFacade;
import org.neo4j.rest.graphdb.entity.RestNode;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;
import org.neo4j.rest.graphdb.util.Config;
import org.neo4j.rest.graphdb.util.QueryResult;
import org.grants.exports.export.CompiledNode;
import org.grants.exports.export.Export;

public class GrantExport extends Export {
	
	private int MAX_GRANT_NAME = 100;

	//MATCH (n:NHMRC:Researcher) RETURN n LIMIT 50 UNION MATCH (n:ARC:Researcher) RETURN n LIMIT 25 UNION MATCH (n:Web:Researcher) RETURN n
	
	// Qyery all instutuions ID:
	// MATCH (n:NHMRC:Institution) RETURN id(n) UNION MATCH (n:ARC:Institution) RETURN id(n) 
	
	public void Export(final String serverRoot)
	{
		new File("json").mkdirs();
		
		System.setProperty(Config.CONFIG_STREAM, "true");
		
		// connect to graph database
		RestAPI graphDb = new RestAPIFacade(serverRoot);  
		
		// Create cypher engine
		RestCypherQueryEngine engine=new RestCypherQueryEngine(graphDb);  
				
		// create object mapper
		ObjectMapper mapper = new ObjectMapper();   
			
		// create map to store result;
		Map<String, String> mapNodes = new HashMap<String, String>();
		
		// Extract some Grants
		QueryResult<Map<String, Object>> grants = engine.query("MATCH (n:NHMRC:Grant) RETURN n LIMIT 50 UNION MATCH (n:ARC:Grant) RETURN n LIMIT 50", null);
		for (Map<String, Object> row : grants) {
			RestNode nodeGrant = (RestNode) row.get("n");
		
			if (null == nodeGrant) {
				System.out.println("Invalid node");
				break;
			}
			
			// extract node id
			long grantNodeId = nodeGrant.getId();
			// extract node name
			String grantName = (String) nodeGrant.getProperty(FIELD_SCIENTIFIC_TITLE);
			if (grantName.length() > MAX_GRANT_NAME)
				grantName = grantName.substring(0, MAX_GRANT_NAME - 3) + "...";
			
			System.out.println("Processing researcher: " + grantName + " (" + grantNodeId + ")");
			
			// Create Grant 
			CompiledNode grant = CreateGrant(nodeGrant);
			
			// query all this grants institutions
			List<CompiledNode> insitutions = QueryAllGrantInstitutions(grant);
			
			// now enumerate the institutions
			if (null != insitutions)
				for (CompiledNode institution : insitutions) 
					grant.AddChildern(institution);	
			
			// Query all this grant researchers			
			List<CompiledNode> researchers = QueryAllGrantResearchers(grant);
			if (null != researchers) 
				for (CompiledNode researcher : researchers) 
				{
					// Query all this researcher datasets
					List<CompiledNode> datatsets = QueryAllResearchersDatasets(researcher);
					
					// Now enumerate datasets 
					if (null != datatsets)
						for (CompiledNode datatset : datatsets) 
							researcher.AddChildern(datatset);
					
					grant.AddChildern(researcher);	
				}
					
			try {
				String json = mapper.writeValueAsString(grant.getData());
				String fileName = Long.toString(grantNodeId) + ".json";
				
				Writer writer = new BufferedWriter(new OutputStreamWriter(
				          new FileOutputStream(new File("json/" + fileName)), "utf-8"));
				
				writer.write(json);
				writer.close();
				
				mapNodes.put(grantName, fileName);			
			} catch (JsonGenerationException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			
			Map<String, Object> mapIndex = new HashMap<String, Object>();
			mapIndex.put("select", "Grant: ");
			mapIndex.put("comments", "Only 50 HNMRC and 50 ARC Grants has been selected. Click on node to view it's parameters. Double click on it to collapse or expand.");
			mapIndex.put("index", mapNodes);
			mapIndex.put("legend", CompiledNode.getLegend());
			
			String json = mapper.writeValueAsString(mapIndex);
			String fileName = "json/index.json";
			
			Writer writer = new BufferedWriter(new OutputStreamWriter(
			          new FileOutputStream(new File(fileName)), "utf-8"));
			
			writer.write(json);
			writer.close();
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
