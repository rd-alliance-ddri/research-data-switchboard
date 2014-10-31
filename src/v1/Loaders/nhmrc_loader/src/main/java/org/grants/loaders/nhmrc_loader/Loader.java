package org.grants.loaders.nhmrc_loader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.isbar_software.researchdata.MetadataAPI;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.RestAPIFacade;
import org.neo4j.rest.graphdb.entity.RestNode;
import org.neo4j.rest.graphdb.index.RestIndex;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;
import org.neo4j.rest.graphdb.util.Config;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Loader class
 * 
 * This class expects nhmrc/2014/grants-data.csv and nhmrc/2014/ci-roles.csv files to be 
 * available in the working directory.
 * 
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */
public class Loader {
	
		
	// CSV files are permanent, no problem with defining it
	private static final String GRANTS_CSV_PATH = "nhmrc/2014/grants-data.csv";
	private static final String ROLES_CSV_PATH = "nhmrc/2014/ci-roles.csv";
//	private static final int MAX_REQUEST_PER_TRANSACTION = 1000;

	private static final String LABEL_GRANT = "Grant";
	private static final String LABEL_RESEARCHER = "Researcher";
	private static final String LABEL_INSTITUTION = "Institution";

	private static final String LABEL_NHMRC = "NHMRC";
	private static final String LABEL_NHMRC_GRANT = LABEL_NHMRC + "_" + LABEL_GRANT;
	private static final String LABEL_NHMRC_RESEARCHER = LABEL_NHMRC + "_" + LABEL_RESEARCHER;
	private static final String LABEL_NHMRC_INSTITUTION = LABEL_NHMRC + "_" + LABEL_INSTITUTION;
	
	private static final String LABEL_RDA = "NLA";
	private static final String LABEL_RDA_INSTITUTION = LABEL_RDA + "_" + LABEL_INSTITUTION;

	private static final String FIELD_NODE_TYPE = "node_type";
	private static final String FIELD_NODE_SOURCE = "node_source";
	
	private static final String FIELD_PURL = "purl";
	private static final String FIELD_NLA = "nla";
	private static final String FIELD_NAME = "name";
	private static final String FIELD_STATE = "state";
	private static final String FIELD_TYPE = "type";
	private static final String FIELD_SOURCE = "source";

	private static final String FIELD_NHMRC_GRANT_ID = "nhmrc_grant_id";
	private static final String FIELD_APPLICATION_YEAR = "application_year";
	private static final String FIELD_SUB_TYPE = "sub_type";
	private static final String FIELD_HIGHER_GRANT_TYPE = "higher_grant_type";
	private static final String FIELD_SCIENTIFIC_TITLE = "scientific_title";
	private static final String FIELD_SIMPLIFIED_TITLE = "simplified_title";
	private static final String FIELD_CIA_NAME = "cia_name";
	private static final String FIELD_START_YEAR = "start_year";
	private static final String FIELD_END_YEAR = "end_year";
	private static final String FIELD_TOTAL_BUDGET = "total_budget";
	private static final String FIELD_RESEARCH_AREA = "research_area";
	private static final String FIELD_FOR_CATEGORY = "for_category";
	private static final String FIELD_OF_RESEARCH = "field_of_research";
	private static final String FIELD_KEYWORDS = "keywords";
	private static final String FIELD_HEALTH_KEYWORDS = "health_keywords";
	private static final String FIELD_MEDIA_SUMMARY = "media_summary";
	private static final String FIELD_SOURCE_SYSTEM = "source_system";
	
	private static final String FIELD_ROLE = "role";
	private static final String FIELD_DW_INDIVIDUAL_ID = "dw_individual_id";
	private static final String FIELD_SOURCE_INDIVIDUAL_ID = "source_individual_id";
	private static final String FIELD_TITLE = "title";
	private static final String FIELD_FIRST_NAME = "first_name";
	private static final String FIELD_MIDDLE_NAME = "middle_name";
	private static final String FIELD_LAST_NAME = "last_name";
	private static final String FIELD_FULL_NAME = "full_name";
	private static final String FIELD_ROLE_START_DATE = "role_start_date";
	private static final String FIELD_ROLE_END_DATE = "role_end_date";
	
    private static enum RelTypes implements RelationshipType
    {
        AdminInstitute, Investigator, KnownAs
    }

    private static enum Labels implements Label {
    	NHMRC, RDA, Institution, Grant, Researcher
    };

	private Map<Integer, RestNode> mapNHMRCGrant = new HashMap<Integer, RestNode>();
	private Map<String, RestNode> mapNHMRCReseracher = new HashMap<String, RestNode>();
	private Map<String, RestNode> mapNHMRCInstitution = new HashMap<String, RestNode>();
	
	private MetadataAPI metadata;
	
	private RestIndex<Node> indexNHMRCGrant;
	private RestIndex<Node> indexNHMRCResearcher;
	private RestIndex<Node> indexNHMRCInstitution;
	private RestIndex<Node> indexRDAInstitution;
	
	/**
	 * Loader function
	 * @param serverRoot String containing url of Neo4J
	 */
	public void Load(final String serverRoot)
	{
		System.setProperty(Config.CONFIG_STREAM, "true");
		
		// connect to graph database
		RestAPI graphDb = new RestAPIFacade(serverRoot);  
		
		//GraphDatabaseService graphDb = new RestGraphDatabase(serverRoot);  
			// create a query engine
		RestCypherQueryEngine engine=new RestCypherQueryEngine(graphDb);  
		
		// make sure we have an index on NHMRC_Grant:nhmrc_grant_id
		// RestAPI does not supported indexes created by schema, so we will use Cypher for that
		engine.query("CREATE CONSTRAINT ON (n:" + LABEL_NHMRC_GRANT + ") ASSERT n." + FIELD_NHMRC_GRANT_ID + " IS UNIQUE", Collections.<String, Object> emptyMap());
		
		// make sure we have an index on NHMRC_Researcher:dw_individual_id
		engine.query("CREATE CONSTRAINT ON (n:" + LABEL_NHMRC_RESEARCHER + ") ASSERT n."+ FIELD_DW_INDIVIDUAL_ID + " IS UNIQUE", Collections.<String, Object> emptyMap());

		// make sure we have an index on NHMRC_Institution:name
		engine.query("CREATE CONSTRAINT ON (n:" + LABEL_NHMRC_INSTITUTION + ") ASSERT n." + FIELD_NAME + " IS UNIQUE", Collections.<String, Object> emptyMap());

		// make sure we have an index on RDA_Institution:nla
		engine.query("CREATE CONSTRAINT ON (n:" + LABEL_RDA_INSTITUTION + ") ASSERT n." + FIELD_NLA + " IS UNIQUE", Collections.<String, Object> emptyMap());
					
		// Obtain an index on Grant
		indexNHMRCGrant = graphDb.index().forNodes(LABEL_NHMRC_GRANT);
		
		// Obtain an index on Researcher
		indexNHMRCResearcher = graphDb.index().forNodes(LABEL_NHMRC_RESEARCHER);

		// Obtain an index on Institution
		indexNHMRCInstitution = graphDb.index().forNodes(LABEL_NHMRC_INSTITUTION);

		// Obtain an index on Institution
		indexRDAInstitution = graphDb.index().forNodes(LABEL_RDA_INSTITUTION);
		
		// Obtain an index on Identifies relationship
		//RelationshipIndex indexIdentified = graphDb.index().forRelationships(RELATIONSHIP_IDENTIFIES);
		
		metadata = new MetadataAPI();
		
		// Imoprt Grant data
		System.out.println("Importing Grant data");
		long grantsCounter = 0;
	//	long transactionCount = 0;
		long beginTime = System.currentTimeMillis();
		
		
		
		// process grats data file
		CSVReader reader;
		try 
		{
			//Transaction tx = graphDb.beginTx(); 
			
			reader = new CSVReader(new FileReader(GRANTS_CSV_PATH));
			String[] grant;
			boolean header = false;
			while ((grant = reader.readNext()) != null) 
			{
				if (!header)
				{
					header = true;
					continue;
				}
				if (grant.length != 57)
					continue;
				
				/*
				if (transactionCount > MAX_REQUEST_PER_TRANSACTION)
				{
					tx.success();  
					tx.finish(); 
					tx = graphDb.beginTx(); 
				}*/
				
				int grantId = Integer.parseInt(grant[0]);
				System.out.println("Grant id: " + grantId);			
				
				if (!mapNHMRCGrant.containsKey(grantId)) {					
					RestNode nodeInstitution = GetOrCreateAUInstitution(graphDb, 
							grant[6], grant[7], grant[8]);
					
					Map<String, Object> map = new HashMap<String, Object>();
					String purl = "http://purl.org/au-research/grants/nhmrc/" + grantId;
					
					map.put(FIELD_NHMRC_GRANT_ID, grantId);
					map.put(FIELD_PURL, purl);
					map.put(FIELD_NODE_TYPE, Labels.Grant.name());
					map.put(FIELD_NODE_SOURCE, Labels.NHMRC.name());
					map.put(FIELD_APPLICATION_YEAR, Integer.parseInt(grant[1]));
					map.put(FIELD_SUB_TYPE, grant[2]);
					map.put(FIELD_HIGHER_GRANT_TYPE, grant[3]);					
					map.put(FIELD_SCIENTIFIC_TITLE, grant[9]);
					map.put(FIELD_SIMPLIFIED_TITLE, grant[10]);
					map.put(FIELD_CIA_NAME, grant[11]);
					map.put(FIELD_START_YEAR, Integer.parseInt(grant[12]));
					map.put(FIELD_END_YEAR, Integer.parseInt(grant[13]));
					map.put(FIELD_TOTAL_BUDGET, grant[41]);
					map.put(FIELD_RESEARCH_AREA, grant[42]);
					map.put(FIELD_FOR_CATEGORY, grant[43]);
					map.put(FIELD_OF_RESEARCH, grant[44]);
					
					List<String> keywords = null;
					for (int i = 45; i <= 49; ++i)
						if (grant[i].length() > 0)
						{
							if (null == keywords)
								keywords = new ArrayList<String>();
							keywords.add(grant[i]);
						}
						
					if (null != keywords)
						map.put(FIELD_KEYWORDS, keywords );
					
					keywords = null; 
					for (int i = 50; i <= 54; ++i)
						if (grant[i].length() > 0)
						{
							if (null == keywords)
								keywords = new ArrayList<String>();
							keywords.add(grant[i]);
						}
						
					if (null != keywords)
						map.put(FIELD_HEALTH_KEYWORDS, keywords );
					
			
					map.put(FIELD_MEDIA_SUMMARY, grant[54]);
					map.put(FIELD_SOURCE_SYSTEM, grant[55]);
							
					RestNode nodeGrant = graphDb.getOrCreateNode(indexNHMRCGrant, 
							FIELD_NHMRC_GRANT_ID, grantId, map);
					if (!nodeGrant.hasLabel(Labels.Grant))
						nodeGrant.addLabel(Labels.Grant); 
					if (!nodeGrant.hasLabel(Labels.NHMRC))
						nodeGrant.addLabel(Labels.NHMRC); 
					
					CreateUniqueRelationship(nodeGrant, nodeInstitution, RelTypes.AdminInstitute, false);
					
					mapNHMRCGrant.put(grantId, nodeGrant);	

				}

				++grantsCounter;
			}
			
		/*	tx.success();  
			tx.finish(); */
	
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			
			return;
		} catch (IOException e) {
			e.printStackTrace();

			return;
		} catch (Exception e) {
			e.printStackTrace();
			
			return;
		}
		
		long endTime = System.currentTimeMillis();
		
		System.out.println(String.format("Done. Imporded %d grants over %d ms. Average %f ms per grant", 
				grantsCounter, endTime - beginTime, (float)(endTime - beginTime) / (float)grantsCounter));
	
		
		long granteesCounter = 0;
		beginTime = System.currentTimeMillis();
	
		try 
		{
			reader = new CSVReader(new FileReader(ROLES_CSV_PATH));
			String[] grantee;
			boolean header = false;
			while ((grantee = reader.readNext()) != null) 
			{
				if (!header)
				{
					header = true;
					continue;
				}
				if (grantee.length != 12)
					continue;
				
			/*	Map<String, Object> par = new HashMap<String, Object>();
				par.put(FIELD_GRANT_ID, Integer.parseInt(grantee[0]));*/
				
				int grantId = Integer.parseInt(grantee[0]);
				String dwIndividualId = grantee[2];
				System.out.println("Investigator id: " + dwIndividualId);	
				
				RestNode nodeGrantee = mapNHMRCReseracher.get(dwIndividualId);
				if (null == nodeGrantee)
				{	
					Map<String, Object> map = new HashMap<String, Object>();
					map.put(FIELD_DW_INDIVIDUAL_ID, dwIndividualId);
					map.put(FIELD_NODE_TYPE, Labels.Researcher.name());
					map.put(FIELD_NODE_SOURCE, Labels.NHMRC.name());
					map.put(FIELD_SOURCE_INDIVIDUAL_ID, grantee[3]);
					map.put(FIELD_TITLE, grantee[4]);
					map.put(FIELD_FIRST_NAME, grantee[5]);
					map.put(FIELD_MIDDLE_NAME, grantee[6]);
					map.put(FIELD_LAST_NAME, grantee[7]);
					map.put(FIELD_FULL_NAME, grantee[8]);
					map.put(FIELD_SOURCE_SYSTEM, grantee[11]);
					
					nodeGrantee = graphDb.getOrCreateNode(indexNHMRCResearcher, FIELD_DW_INDIVIDUAL_ID, dwIndividualId, map);
					if (!nodeGrantee.hasLabel(Labels.Researcher))
						nodeGrantee.addLabel(Labels.Researcher);
					if (!nodeGrantee.hasLabel(Labels.NHMRC))
						nodeGrantee.addLabel(Labels.NHMRC);
					
					mapNHMRCReseracher.put(dwIndividualId, nodeGrantee);	
				}
									
				//	graphDb.setLabel(nodeGrantee, LABEL_RESEARCHER);
				RestNode nodeGrant = mapNHMRCGrant.get(grantId);
				
				if (nodeGrant != null) 
				{
					Iterable<Relationship> rels = nodeGrantee.getRelationships(RelTypes.Investigator);
					boolean relationshipExist = false;
					for (Relationship rel : rels) 
						if (rel.getEndNode().getId() == nodeGrant.getId()) {
							relationshipExist = true;
							break;
						}
					
					if (!relationshipExist) {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put(FIELD_ROLE, grantee[1]);
						map.put(FIELD_ROLE_START_DATE, grantee[9]);
						map.put(FIELD_ROLE_END_DATE, grantee[10]);
						
						graphDb.createRelationship(nodeGrantee, nodeGrant, RelTypes.Investigator, map);
					}
				}
				
				++granteesCounter;
			}
	
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			
			return;
		} catch (IOException e) {
			e.printStackTrace();

			return;
		} catch (Exception e) {
			e.printStackTrace();
			
			return;
		}
		
		endTime = System.currentTimeMillis();
		
		System.out.println(String.format("Done. Imporded %d grantees and create relationships over %d ms. Average %f ms per grantee", 
				granteesCounter, endTime - beginTime, (float)(endTime - beginTime) / (float)granteesCounter));
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
	
	
	private void FindAndConnectInstitutionNLA(RestAPI graphDb, RestNode nodeInstitution, String name) 
			throws UnsupportedEncodingException {	
		metadata.query = "class:(party) AND display_title:(\"" + name +  "\") AND identifier_type:\"AU-ANL:PEAU\"";
		metadata.fields = "identifier_value,display_title";
		
		Set<String> nlas = metadata.QueryField("identifier_value", "display_title", name);
		if (nlas != null) {	
			if (nlas.size() > 1) {
				System.out.println("The RDA has return more that obe NLA for this request: " + name + ", return size: " + nlas.size());
			}					
		
			for (String nla : nlas) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put(FIELD_NLA, nla);
				map.put(FIELD_NODE_TYPE, Labels.Institution.name());
				map.put(FIELD_NODE_SOURCE, Labels.RDA.name());
				map.put(FIELD_NAME, name);
				map.put(FIELD_SOURCE, "http://researchdata.ands.org.au/");
								
				RestNode node = graphDb.getOrCreateNode(
						indexRDAInstitution, FIELD_NLA, nla, map);
				if (!node.hasLabel(Labels.Institution))
					node.addLabel(Labels.Institution); 
				if (!node.hasLabel(Labels.RDA))
					node.addLabel(Labels.RDA); 
				
				CreateUniqueRelationship(nodeInstitution, node, RelTypes.KnownAs, true);
			}
		}
	}
	
	private RestNode GetOrCreateAUInstitution(RestAPI graphDb, String name, String state, String type) {
		RestNode nodeInstitution = mapNHMRCInstitution.get(name);
		if (null == nodeInstitution) {
	//		System.out.println("Query administration institute NLA");
		
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(FIELD_NAME, name);
			map.put(FIELD_NODE_TYPE, Labels.Institution.name());
			map.put(FIELD_NODE_SOURCE, Labels.NHMRC.name());
			map.put(FIELD_STATE, state);
			map.put(FIELD_TYPE, type);
			map.put(FIELD_SOURCE, "NHMRC");
							
			nodeInstitution = graphDb.getOrCreateNode(
					indexNHMRCInstitution, FIELD_NAME, name, map);
			if (!nodeInstitution.hasLabel(Labels.Institution))
				nodeInstitution.addLabel(Labels.Institution); 
			if (!nodeInstitution.hasLabel(Labels.NHMRC))
				nodeInstitution.addLabel(Labels.NHMRC); 
					
			// only check NLA once
			try {
				FindAndConnectInstitutionNLA(graphDb, nodeInstitution, name);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
			mapNHMRCInstitution.put(name, nodeInstitution);
		}		
		
		return nodeInstitution;
	}
	
}
