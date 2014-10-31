package org.grants.loaders.arc_loader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
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
 * This class expects data/arc/completed_projects.csv and data/arc/new_projects.csv files to be avaliable in the
 * working directory.
 * 
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */
public class Loader {

	private static final String COMPLETED_GRANTS_CSV_PATH = "data/arc/completed_projects.csv";
	//private static final String COMPLETED_ROLES_CSV_PATH = "data/arc/completed_fellowships.csv";
	private static final String NEW_GRANTS_CSV_PATH = "data/arc/new_projects.csv";
	//private static final String NEW_ROLES_CSV_PATH = "data/arc/new_fellowships.csv";

	//	private static final int MAX_REQUEST_PER_TRANSACTION = 1000;


	private static final String LABEL_GRANT = "Grant";
	private static final String LABEL_RESEARCHER = "Researcher";
	private static final String LABEL_INSTITUTION = "Institution";

	private static final String LABEL_ARC = "ARC";
	private static final String LABEL_ARC_GRANT = LABEL_ARC + "_" + LABEL_GRANT;
	private static final String LABEL_ARC_RESEARCHER = LABEL_ARC + "_" + LABEL_RESEARCHER;
	private static final String LABEL_ARC_INSTITUTION = LABEL_ARC + "_" + LABEL_INSTITUTION;
	
	private static final String LABEL_RDA = "RDA";
	private static final String LABEL_RDA_INSTITUTION = LABEL_RDA + "_" + LABEL_INSTITUTION;
	
	private static final String FIELD_NODE_TYPE = "node_type";
	private static final String FIELD_NODE_SOURCE = "node_source";
	
	private static final String FIELD_NLA = "nla";
//	private static final String FIELD_TYPE = "type";

	private static final String FIELD_NAME = "name";
	private static final String FIELD_STATE = "state";
	private static final String FIELD_SOURCE = "source";
	
	private static final String FIELD_PURL = "purl";
	private static final String FIELD_ARC_PROJECT_ID = "arc_grant_id";
	private static final String FIELD_ARC_SCHEME = "arc_scheme";
	private static final String FIELD_APPLICATION_YEAR = "application_year";
	private static final String FIELD_SCIENTIFIC_TITLE = "scientific_title";
	private static final String FIELD_START_YEAR = "start_year";
	private static final String FIELD_TOTAL_BUDGET = "total_budget";
	private static final String FIELD_RESEARCH_AREA = "research_area";
	private static final String FIELD_RESEARCH_AREA_CODE = "research_area_code";
	private static final String FIELD_DESCRIPTION = "description";
	
	private static final String FIELD_FULL_NAME = "full_name";
	private static final String FIELD_ARC_PERSONAL_ID = "arc_personal_id";
	
	private static final String[] TITLES = { "Mr ", "Ms ", "Dr ", "A/Prof ", "Adj/Prof ", "Asst Prof ", "Prof Dr ", "Prof " }; 
	
    private static enum RelTypes implements RelationshipType
    {
    	AdminInstitute, Investigator, KnownAs
    }

    private static enum Labels implements Label {
    	ARC, RDA, Institution, Grant, Researcher
    };
    
	private Map<String, RestNode> mapARCGrant = new HashMap<String, RestNode>();
	private Map<String, RestNode> mapARCReseracher = new HashMap<String, RestNode>();
	private Map<String, RestNode> mapARCInstitution = new HashMap<String, RestNode>();
	
	private MetadataAPI metadata;
	
	private RestIndex<Node> indexARCGrant;
	private RestIndex<Node> indexARCResearcher;
	private RestIndex<Node> indexARCInstitution;
	private RestIndex<Node> indexRDAInstitution;
    
	/**
	 * Function load data into Neo4J
	 * @param serverRoot String containing address of Neo4J instance
	 */
    public void Load(final String serverRoot)
	{
		System.setProperty(Config.CONFIG_STREAM, "true");
	
		// connect to graph database
		RestAPI graphDb = new RestAPIFacade(serverRoot);  
		
		//GraphDatabaseService graphDb = new RestGraphDatabase(serverRoot);  
			// create a query engine
		RestCypherQueryEngine engine=new RestCypherQueryEngine(graphDb);  
		
		// make sure we have an index on ARC_Grant:arc_project_id
		// RestAPI does not supported indexes created by schema, so we will use Cypher for that
		engine.query("CREATE CONSTRAINT ON (n:" + LABEL_ARC_GRANT + ") ASSERT n." + FIELD_ARC_PROJECT_ID + " IS UNIQUE", Collections.<String, Object> emptyMap());
		
		// make sure we have an index on ARC_Researcher:arc_personal_id
		engine.query("CREATE CONSTRAINT ON (n:" + LABEL_ARC_RESEARCHER + ") ASSERT n."+ FIELD_ARC_PERSONAL_ID + " IS UNIQUE", Collections.<String, Object> emptyMap());

		// make sure we have an index on ARC_Institution:name
		engine.query("CREATE CONSTRAINT ON (n:" + LABEL_ARC_INSTITUTION + ") ASSERT n." + FIELD_NAME + " IS UNIQUE", Collections.<String, Object> emptyMap());

		// make sure we have an index on RDA_Institution:nla
		engine.query("CREATE CONSTRAINT ON (n:" + LABEL_RDA_INSTITUTION + ") ASSERT n." + FIELD_NLA + " IS UNIQUE", Collections.<String, Object> emptyMap());
					
		// Obtain an index on Grant
		indexARCGrant = graphDb.index().forNodes(LABEL_ARC_GRANT);
		
		// Obtain an index on Researcher
		indexARCResearcher = graphDb.index().forNodes(LABEL_ARC_RESEARCHER);

		// Obtain an index on Institution
		indexARCInstitution = graphDb.index().forNodes(LABEL_ARC_INSTITUTION);

		// Obtain an index on Institution
		indexRDAInstitution = graphDb.index().forNodes(LABEL_RDA_INSTITUTION);
		
		metadata = new MetadataAPI();
		
		if (!LoadCsv(graphDb, COMPLETED_GRANTS_CSV_PATH))
			return;
		
		if (!LoadCsv(graphDb, NEW_GRANTS_CSV_PATH))
			return;
	}
	
    /**
     * Function lo load data into Neo4J
     * @param graphDb RestAPI object
     * @param csv address of csv file
     * @return
     */
    public boolean LoadCsv(RestAPI graphDb,final String csv) {
    	// Imoprt Grant data
		System.out.println("Importing Grant data");
		long grantsCounter = 0;
	//	long transactionCount = 0;
		long beginTime = System.currentTimeMillis();
			
	//// process grats data file
		CSVReader reader;
		try 
		{
			reader = new CSVReader(new FileReader(csv));
			String[] grant;
			boolean header = false;
			while ((grant = reader.readNext()) != null) 
			{
				if (!header)
				{
					header = true;
					continue;
				}
				if (grant.length != 29)
					continue;
				
				String projectId = grant[0];
				System.out.println("Project id: " + projectId);			
				
				if (!mapARCGrant.containsKey(projectId)) {					
					RestNode nodeInstitution = GetOrCreateAUInstitution(graphDb, 
							grant[4], grant[5]);
				
					Map<String, Object> map = new HashMap<String, Object>();
					String purl = "http://purl.org/au-research/grants/arc/" + projectId;

					map.put(FIELD_ARC_PROJECT_ID, projectId);
					map.put(FIELD_PURL, purl);
					map.put(FIELD_NODE_TYPE, Labels.Grant.name());
					map.put(FIELD_NODE_SOURCE, Labels.ARC.name());
					map.put(FIELD_ARC_SCHEME, grant[1]);
					int applicationYear = 0;
					
					try {
						applicationYear = Integer.parseInt(grant[2]);
					} catch(Exception e) {}
					
					if (applicationYear != 0)
						map.put(FIELD_APPLICATION_YEAR, applicationYear);
					
					int commitmentYear = 0;
					try {
						commitmentYear = Integer.parseInt(grant[3]);
					} catch(Exception e) {}
					
					if (commitmentYear != 0)
						map.put(FIELD_START_YEAR, commitmentYear);
					
					map.put(FIELD_SCIENTIFIC_TITLE, grant[7]);
					map.put(FIELD_DESCRIPTION, grant[8]);
					map.put(FIELD_RESEARCH_AREA_CODE, grant[9]);
					map.put(FIELD_RESEARCH_AREA, grant[10]);
					map.put(FIELD_TOTAL_BUDGET, grant[11]);
				
					RestNode nodeGrant = graphDb.getOrCreateNode(indexARCGrant, FIELD_ARC_PROJECT_ID, projectId, map);
					if (!nodeGrant.hasLabel(Labels.Grant))
						nodeGrant.addLabel(Labels.Grant); 
					if (!nodeGrant.hasLabel(Labels.ARC))
						nodeGrant.addLabel(Labels.ARC);
					
					CreateUniqueRelationship(nodeGrant, nodeInstitution, RelTypes.AdminInstitute, false);
					
					mapARCGrant.put(projectId, nodeGrant);	

					
					String investigator = grant[6];
					if (!investigator.contains("n.a.")) {
						
						List<String> investigators = null;
						if (investigator.contains(";"))
							investigators = Arrays.asList(investigator.split(";"));
						else
						{
							investigators = new ArrayList<String>();
							
							int tagSize = 0;
							while (null != investigator && !investigator.isEmpty()) {
								int pos = -1;
								int size = 0;
								for (String title : TITLES) {
									int pos1 = investigator.indexOf(title, tagSize);
									if (pos1 != -1 && (pos == -1 || pos > pos1)) {
										pos = pos1;
										size = title.length();
									}
								}
								if (pos != -1) {
									// we have find a tag
									tagSize = size;
									
									if (pos > 0) {									
										String inv = investigator.substring(0, pos).trim();
										investigator = investigator.substring(pos);
										if (!inv.isEmpty()) {
										//	System.out.println(inv);
											investigators.add(inv);
										}
									}
								} else {
									// we didn't find any 
									
									investigator = investigator.trim();
									if (!investigator.isEmpty()) {
								//		System.out.println(investigator);
										investigators.add(investigator);
									}
									investigator = null;
								}
							}
						}
						
						if (null != investigators && !investigators.isEmpty()) {
							for (String grantee : investigators) {
								String personalId = projectId + ":" + grantee;
								RestNode nodeGrantee = mapARCReseracher.get(personalId);
								if (null == nodeGrantee)
								{	
									
									map = new HashMap<String, Object>();
									map.put(FIELD_ARC_PERSONAL_ID, personalId);
									map.put(FIELD_NODE_TYPE, Labels.Researcher.name());
									map.put(FIELD_NODE_SOURCE, Labels.ARC.name());
									map.put(FIELD_FULL_NAME, grantee);
									
									nodeGrantee = graphDb.getOrCreateNode(indexARCResearcher, FIELD_ARC_PERSONAL_ID, personalId, map);
									if (!nodeGrantee.hasLabel(Labels.Researcher))
										nodeGrantee.addLabel(Labels.Researcher);
									if (!nodeGrantee.hasLabel(Labels.ARC))
										nodeGrantee.addLabel(Labels.ARC);
									if (!nodeGrantee.hasRelationship(RelTypes.Investigator))
										nodeGrantee.createRelationshipTo(nodeGrant, RelTypes.Investigator);
									
									mapARCReseracher.put(grantee, nodeGrantee);	
								}								
							}
						}
					}
				}
				else
					System.out.println("The Grants map already contains the key: " + projectId);
				
				++grantsCounter;
			
			}
			
			reader.close();			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			
			return false;
		} catch (IOException e) {
			e.printStackTrace();
	
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			
			return false;
		}
		
		long endTime = System.currentTimeMillis();
		
		System.out.println(String.format("Done. Imporded %d grants over %d ms. Average %f ms per grant", 
				grantsCounter, endTime - beginTime, (float)(endTime - beginTime) / (float)grantsCounter));

		
		return true;
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
	
	private RestNode GetOrCreateAUInstitution(RestAPI graphDb, String name, String state) {
		RestNode nodeInstitution = mapARCInstitution.get(name);
		if (null == nodeInstitution) {
	//		System.out.println("Query administration institute NLA");
		
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(FIELD_NAME, name);
			map.put(FIELD_NODE_TYPE, Labels.Institution.name());
			map.put(FIELD_NODE_SOURCE, Labels.ARC.name());
			map.put(FIELD_STATE, state);
			map.put(FIELD_SOURCE, "ARC");
							
			nodeInstitution = graphDb.getOrCreateNode(
					indexARCInstitution, FIELD_NAME, name, map);
			if (!nodeInstitution.hasLabel(Labels.Institution))
				nodeInstitution.addLabel(Labels.Institution); 
			if (!nodeInstitution.hasLabel(Labels.ARC))
				nodeInstitution.addLabel(Labels.ARC); 
					
			// only check NLA once
			try {
				FindAndConnectInstitutionNLA(graphDb, nodeInstitution, name);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			
			mapARCInstitution.put(name, nodeInstitution);
		}		
		
		return nodeInstitution;
	}
	
}
