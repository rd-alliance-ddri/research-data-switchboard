package org.grants.exports.institutions;

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
import org.neo4j.rest.graphdb.util.Config;
import org.grants.exports.export.CompiledNode;
import org.grants.exports.export.Export;

public class InsitutionExport extends Export{
	
	private static final long INSTITUTION_IDS[] = { 0, 3, 5, 7, 10, 13, 16, 20, 22, 28 };
	
	private static final int MAX_GRANTS	= 1000;
	
	public void Export(final String serverRoot)
	{
		new File("institutions/json").mkdirs();
		
		System.setProperty(Config.CONFIG_STREAM, "true");
		
		// connect to graph database
		RestAPI graphDb = new RestAPIFacade(serverRoot);  
		
		// create object mapper
		ObjectMapper mapper = new ObjectMapper();   
			
		// create map to store result;
		Map<String, String> mapNodes = new HashMap<String, String>();
		
		for (long nodeInstId : INSTITUTION_IDS) {
			// Get Institution node
			RestNode nodeInstitution = graphDb.getNodeById(nodeInstId);
			
			if (null == nodeInstitution) {
				System.out.println("Invalid node id: " + nodeInstId);
				continue;
			}
				
			// extract node id
			long insitutionNodeId = nodeInstitution.getId();
			// extract node name
			String insitutionName = (String) nodeInstitution.getProperty(FIELD_NAME);

			System.out.println("Processing instutution: " + insitutionName + " (" + insitutionNodeId + ")");
			
			// Create Insitution 
			CompiledNode insitution = CreateInstitution(nodeInstitution);
				
			// Query all known as institutions
			QueryAllKnownAsNodes(nodeInstitution, insitution);
						
			// Query all insitution grants
			List<CompiledNode> grants = QueryAllInstitutionGrants(insitution);
			
			// now enumerate the grants
			if (null != grants)
				for (CompiledNode grant : grants) {
					
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
					
					insitution.AddChildern(grant);					
				}
			
			try {
				String json = mapper.writeValueAsString(insitution.getData());
				String fileName = Long.toString(insitutionNodeId) + ".json";
				
				Writer writer = new BufferedWriter(new OutputStreamWriter(
				          new FileOutputStream(new File("institutions/json/" + fileName)), "utf-8"));
				
				writer.write(json);
				writer.close();
				
				mapNodes.put(insitutionName, fileName);			
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
			mapIndex.put("select", "Institution: ");
			mapIndex.put("comments", INSTITUTION_IDS.length + " Insitutions and max " + MAX_GRANTS + " grants has been selected. Click on node to view it's parameters. Double click on it to collapse or expand.");
			mapIndex.put("index", mapNodes);
			mapIndex.put("legend", CompiledNode.getLegend());
			
			String json = mapper.writeValueAsString(mapIndex);
			String fileName = "institutions/json/index.json";
			
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
