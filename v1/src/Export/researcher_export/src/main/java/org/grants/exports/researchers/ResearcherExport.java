package org.grants.exports.researchers;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

public class ResearcherExport extends Export {
	
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
		
		Set<Long> researchersNodeId = new HashSet<Long>();
		// make sure we have an index on ARC_Grant:arc_project_id
		// RestAPI does not supported indexes created by schema, so we will use Cypher for that
		//"MATCH (n:NHMRC:Researcher) RETURN n LIMIT 50 UNION MATCH (n:ARC:Researcher) RETURN n LIMIT 25 UNION MATCH (n:Web:Researcher) RETURN n
		QueryResult<Map<String, Object>> researchers = engine.query("MATCH (n:Web:Researcher)-[:Investigator]->() RETURN n", null);
		for (Map<String, Object> row : researchers) {
			RestNode nodeResearcher = (RestNode) row.get("n");
		
			if (null == nodeResearcher) {
				System.out.println("Invalid node");
				break;
			}
			
			// extract node id
			long researcherNodeId = nodeResearcher.getId();
			
			if (researchersNodeId.contains(researcherNodeId))
				continue;
			else
				researchersNodeId.add(researcherNodeId);
			
			// extract node name
			String researcherName = (String) nodeResearcher.getProperty(FIELD_FULL_NAME);
			
			System.out.println("Processing researcher: " + researcherName + " (" + researcherNodeId + ")");
			
			// Create Dataset 
			CompiledNode researcher = CreateResearcher(nodeResearcher);

			// qyery all known as researchers
			// NHMRC, ARC and Web researchers aren't connected in current version of the system, so this should be safe 
			QueryAllKnownAsNodes(nodeResearcher, researcher);
			
			// Query all researcher datatses
			List<CompiledNode> datatsets = QueryAllResearchersDatasets(researcher);
			
			// Now enumerate datasets 
			if (null != datatsets)
				for (CompiledNode datatset : datatsets) 
					researcher.AddChildern(datatset);
					
			// query all this researcher grants
			List<CompiledNode> grants = QueryAllResearcherGrants(researcher);
					
			// now enumerate the grants
			if (null != grants)
				for (CompiledNode grant : grants) {
						
					// query all this grants institutions
					List<CompiledNode> insitutions = QueryAllGrantInstitutions(grant);
					
					// now enumerate the institutions
					if (null != insitutions)
						for (CompiledNode institution : insitutions) 
							grant.AddChildern(institution);					
								
					researcher.AddChildern(grant);
				}
					
			try {
				String json = mapper.writeValueAsString(researcher.getData());
				String fileName = Long.toString(researcherNodeId) + ".json";
				
				Writer writer = new BufferedWriter(new OutputStreamWriter(
				          new FileOutputStream(new File("json/" + fileName)), "utf-8"));
				
				writer.write(json);
				writer.close();
				
				mapNodes.put(researcherName, fileName);			
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
			mapIndex.put("select", "Researcher: ");
			mapIndex.put("comments", "Only 50 HNMRC, 25 ARC and 25 Web Researcher has been selected. Click on node to view it's parameters. Double click on it to collapse or expand.");
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
