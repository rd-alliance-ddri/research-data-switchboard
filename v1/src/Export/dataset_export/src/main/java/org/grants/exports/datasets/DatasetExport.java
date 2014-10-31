package org.grants.exports.datasets;

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

public class DatasetExport extends Export {
	
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
		
		// make sure we have an index on ARC_Grant:arc_project_id
		// RestAPI does not supported indexes created by schema, so we will use Cypher for that
		QueryResult<Map<String, Object>> datasets = engine.query("MATCH (n:Dataset)--()--()--(:Grant) RETURN n", null);
		for (Map<String, Object> row : datasets) {
			RestNode nodeDataset = (RestNode) row.get("n");
		
			if (null == nodeDataset) {
				System.out.println("Invalid node");
				break;
			}
			
			// extract node id
			long datasetNodeId = nodeDataset.getId();
			// extract node name
			String datasetName = (String) nodeDataset.getProperty(FIELD_DOI);
			
			System.out.println("Processing dataset: " + datasetName + " (" + datasetNodeId + ")");
			
			// Create Dataset 
			CompiledNode dataset = CreateDataset(nodeDataset);

			// Query all dataset researchers
			List<CompiledNode> researchers = QueryAllDatasetResearchers(dataset);
			
			
			
			// Now enumerate researchers 
			if (null != researchers)
				for (CompiledNode researcher : researchers) {
					
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
					
					dataset.AddChildern(researcher);
				}
			
			try {
				String json = mapper.writeValueAsString(dataset.getData());
				String fileName = Long.toString(datasetNodeId) + ".json";
				
				Writer writer = new BufferedWriter(new OutputStreamWriter(
				          new FileOutputStream(new File("json/" + fileName)), "utf-8"));
				
				writer.write(json);
				writer.close();
				
				mapNodes.put(datasetName, fileName);			
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
			mapIndex.put("select", "Dataset: ");
			mapIndex.put("comments", "Click on node to view it's parameters. Double click on it to collapse or expand.");
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
