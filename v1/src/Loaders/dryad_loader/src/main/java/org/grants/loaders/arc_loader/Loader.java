package org.grants.loaders.arc_loader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.index.IndexHits;
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
 * This class expects data/dryad/dryad.csv file to be available in the working directory.
 * 
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */
public class Loader {

	private static final String CSV_PATH = "data/dryad/dryad.csv";

	private static final String LABEL_GRANT = "Grant";
	private static final String LABEL_RESEARCHER = "Researcher";
	private static final String LABEL_DATA_SET = "Dataset";
	
	private static final String LABEL_DRYAD = "Dryad";
	private static final String LABEL_DRYAD_RESEARCHER = LABEL_DRYAD + "_" + LABEL_RESEARCHER;
	private static final String LABEL_DRYAD_DATA_SET = LABEL_DRYAD + "_" + LABEL_DATA_SET;
	
	private static final String LABEL_WEB = "Web";
	private static final String LABEL_WEB_RESEARCHER = LABEL_WEB + "_" + LABEL_RESEARCHER;
	
	private static final String LABEL_RDA = "RDA";
	private static final String LABEL_RDA_RESEARCHER = LABEL_RDA + "_" + LABEL_RESEARCHER;
	
	private static final String LABEL_NHMRC = "NHMRC";
	private static final String LABEL_NHMRC_GRANT = LABEL_NHMRC + "_" + LABEL_GRANT;

	private static final String LABEL_ARC = "ARC";
	private static final String LABEL_ARC_GRANT = LABEL_ARC + "_" + LABEL_GRANT;

	private static final String FIELD_NODE_TYPE = "node_type";
	private static final String FIELD_NODE_SOURCE = "node_source";
	
	private static final String FIELD_DOI = "doi"; // Dryad Object Id
	private static final String FIELD_RDA = "rda";
//	private static final String FIELD_PURL = "purl";
//	private static final String FIELD_NLA = "nla";
	
	private static final String FIELD_ARC_PROJECT_ID = "arc_grant_id";
	private static final String FIELD_NHMRC_GRANT_ID = "nhmrc_grant_id";
	
	private static final String FIELD_FIRST_NAME = "first_name";
	private static final String FIELD_FULL_NAME = "full_name";
	private static final String FIELD_LAST_NAME = "last_name";
	private static final String FIELD_UNI_URL = "university_url";
	private static final String FIELD_UNI_HAVE_GRANTS = "have_grants";
	private static final String FIELD_EMAIL = "email";
	private static final String FIELD_SOCIAL = "social";
		
    private static enum RelTypes implements RelationshipType
    {
        Investigator, KnownAs, RelatedTo
    }

    private static enum Labels implements Label {
    	Dryad, RDA, Web, Researcher, Grant, Dataset
    };
    
    private Map<String, RestNode> mapDryadDatasets = new HashMap<String, RestNode>();
    private Map<String, RestNode> mapDryadResearchers = new HashMap<String, RestNode>();
    private Map<String, RestNode> mapWebResearchers = new HashMap<String, RestNode>();
    private Map<String, RestNode> mapARCGrants = new HashMap<String, RestNode>();
    private Map<String, RestNode> mapNHMRCGrants = new HashMap<String, RestNode>();
    
    private RestIndex<Node> indexDryadDataSet;
    private RestIndex<Node> indexDryadResearcher;
    private RestIndex<Node> indexWebResearcher;
    private RestIndex<Node> indexRDAResearcher;
    private RestIndex<Node> indexARCGrant;
    private RestIndex<Node> indexNHMRCGrant;
        
    /**
     * Function to load data
     * @param serverRoot String containing address of Neo4J
     */
	public void Load(final String serverRoot)
	{
		System.setProperty(Config.CONFIG_STREAM, "true");
	
		// connect to graph database
		RestAPI graphDb = new RestAPIFacade(serverRoot);  
		
		//GraphDatabaseService graphDb = new RestGraphDatabase(serverRoot);  
			// create a query engine
		RestCypherQueryEngine engine=new RestCypherQueryEngine(graphDb);  

		// make sure we have an unique index on Dryad_DataSet:doi
		engine.query("CREATE CONSTRAINT ON (n:" + LABEL_DRYAD_DATA_SET + ") ASSERT n." + FIELD_DOI + " IS UNIQUE", Collections.<String, Object> emptyMap());

		// make sure we have an unique index on Dryad_Researcher:full_name
		engine.query("CREATE CONSTRAINT ON (n:" + LABEL_DRYAD_RESEARCHER + ") ASSERT n." + FIELD_FULL_NAME + " IS UNIQUE", Collections.<String, Object> emptyMap());
 		
		// make sure we have an unique index on Web_Researcher:full_name
		engine.query("CREATE CONSTRAINT ON (n:" + LABEL_WEB_RESEARCHER + ") ASSERT n." + FIELD_FULL_NAME + " IS UNIQUE", Collections.<String, Object> emptyMap());
		
		// make sure we have an unique index on RDA_Researcher:rda
		engine.query("CREATE CONSTRAINT ON (n:" + LABEL_RDA_RESEARCHER + ") ASSERT n." + FIELD_RDA + " IS UNIQUE", Collections.<String, Object> emptyMap());
		
		// make sure we have an index on ARC_Grant:arc_project_id
	//	engine.query("CREATE CONSTRAINT ON (n:" + LABEL_ARC_GRANT + ") ASSERT n." + FIELD_ARC_PROJECT_ID + " IS UNIQUE", Collections.<String, Object> emptyMap());
				
		// make sure we have an index on NHMRC_Grant:nhmrc_grant_id
//		engine.query("CREATE CONSTRAINT ON (n:" + LABEL_NHMRC_GRANT + ") ASSERT n." + FIELD_NHMRC_GRANT_ID + " IS UNIQUE", Collections.<String, Object> emptyMap());
					
		
		
		// make sure we have an unique index on NLA_Researcher:nla
		//engine.query("CREATE CONSTRAINT ON (n:" + LABEL_NLA_RESEARCHER + ") ASSERT n." + FIELD_NLA + " IS UNIQUE", Collections.<String, Object> emptyMap());

		// make sure we have an unique index on HDL_Researcher:hdl
//		engine.query("CREATE CONSTRAINT ON (n:" + LABEL_HDL_RESEARCHER + ") ASSERT n." + FIELD_HDL + " IS UNIQUE", Collections.<String, Object> emptyMap());


		// make sure we have an index on PLUR_GRant:plur
		//engine.query("CREATE CONSTRAINT ON (n:" + LABEL_PURL_GRANT + ") ASSERT n."+ FIELD_PURL + " IS UNIQUE", Collections.<String, Object> emptyMap());

		
		
		if (!LoadCsv(graphDb, CSV_PATH))
			return;
	}
    
	/**
	 * Function to load a single csv file in the Neo4J
	 * @param graphDb RestAPI object
	 * @param csv path of CSV file to load
	 * @return true if load is successfull
	 */
    private boolean LoadCsv(RestAPI graphDb,final String csv) {
    	
    	// Obtain an index on Dryad DataSet
		indexDryadDataSet = graphDb.index().forNodes(LABEL_DRYAD_DATA_SET);
    	
    	// Obtain an index on Dryad Researcher
		indexDryadResearcher = graphDb.index().forNodes(LABEL_DRYAD_RESEARCHER);
		
		// Obtain an index on Web Researcher
		indexWebResearcher = graphDb.index().forNodes(LABEL_WEB_RESEARCHER);

		// Obtain an index on Rda Researcher
		indexRDAResearcher = graphDb.index().forNodes(LABEL_RDA_RESEARCHER);
		
		// Obtain an index on ARC Grant
		indexARCGrant = graphDb.index().forNodes(LABEL_ARC_GRANT);
		
		// Obtain an index on NHMRC Grant
		indexNHMRCGrant = graphDb.index().forNodes(LABEL_NHMRC_GRANT);
		
		// Imoprt Grant data
		System.out.println("Importing Dryard data");
		long recordsCounter = 0;
		long beginTime = System.currentTimeMillis();
		
		// process grats data file
		CSVReader reader;
		try 
		{
			reader = new CSVReader(new FileReader(csv));
			String[] record;
			boolean header = false;
			while ((record = reader.readNext()) != null) 
			{
				if (!header)
				{
					header = true;
					continue;
				}
				if (record.length != 20)
					continue;
				
				// decode data

				String doi = record[0];
				
				String firstName = record[1];
				String lastName = record[2];
				String fullName = record[3];
				
				String rda1 = record[6];
				String rda2 = record[7];
				
				String grant1 = record[8];
				String grant2 = record[9];
				String grant3 = record[10];
				String grant4 = record[11];
				String grant5 = record[12];
				
				String uni = record[13];
				String uniHaveGrants = record[14];
				
				String email = record[16];
			//	String nla = record[17];
		//		String hdl = record[18];
				String social = record[19];
				
				System.out.println("Researcher: " + fullName);	
				System.out.println("DOI: " + doi);	
				
				// Get or create Dryad Researcher
				RestNode nodeDryadResearcher = GetOrCreateDryadResearcher(graphDb, 
						firstName, lastName, fullName);
		
				// Get or create web researcher		
				RestNode nodeWebResearcher = GetOrCreateWebResearcher(graphDb, 
						firstName, lastName, fullName, email, social, uni, uniHaveGrants);
				
				// create unique connection
				CreateUniqueRelationship(nodeWebResearcher, nodeDryadResearcher, RelTypes.KnownAs, true);
				
				// Process RDA				
				ProcessRda(graphDb, nodeWebResearcher, fullName, rda1); 
				ProcessRda(graphDb, nodeWebResearcher, fullName, rda2); 
				
				// process datase
				ProcessDataSet(graphDb, nodeDryadResearcher, doi);
				
		/*		ProcessNla(graphDb, indexNlaResearcher, nodeResearcher, fullName, nla); 
				
				ProcessHdl(graphDb, indexHdlResearcher, nodeResearcher, fullName, hdl);*/
				
				ProcessGrant(graphDb, nodeWebResearcher, grant1); 
				ProcessGrant(graphDb, nodeWebResearcher, grant2);
				ProcessGrant(graphDb, nodeWebResearcher, grant3);
				ProcessGrant(graphDb, nodeWebResearcher, grant4);
				ProcessGrant(graphDb, nodeWebResearcher, grant5);

				++recordsCounter;			
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
		
		System.out.println(String.format("Done. Imporded %d records over %d ms. Average %f ms per recortd", 
				recordsCounter, endTime - beginTime, (float)(endTime - beginTime) / (float)recordsCounter));
		
		return true;
    }
    
    private RestNode GetOrCreateDryadResearcher(RestAPI graphDb, 
    		String firstName, String lastName, String fullName) {
    	RestNode node = mapDryadResearchers.get(fullName);
    	if (null == node) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(FIELD_FIRST_NAME, firstName);
			map.put(FIELD_LAST_NAME, lastName);
			map.put(FIELD_FULL_NAME, fullName);
			map.put(FIELD_NODE_TYPE, Labels.Researcher.name());
			map.put(FIELD_NODE_SOURCE, Labels.Dryad.name());
			
			node = graphDb.getOrCreateNode(
					indexDryadResearcher, FIELD_FULL_NAME, fullName, map);
			if (!node.hasLabel(Labels.Researcher))
				node.addLabel(Labels.Researcher); 
			if (!node.hasLabel(Labels.Dryad))
				node.addLabel(Labels.Dryad); 
			
			mapDryadResearchers.put(fullName, node);
    	}
    	
		return node;
	}

    private RestNode GetOrCreateWebResearcher(RestAPI graphDb, 
    		String firstName, String lastName, String fullName, 
    		String email, String social, String uni, String uniHaveGrants) {    	
    	RestNode node = mapWebResearchers.get(fullName);
		if (null == node) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(FIELD_FIRST_NAME, firstName);
			map.put(FIELD_LAST_NAME, lastName);
			map.put(FIELD_FULL_NAME, fullName);
			map.put(FIELD_EMAIL, email);
			map.put(FIELD_SOCIAL, social);
			map.put(FIELD_NODE_TYPE, Labels.Researcher.name());
			map.put(FIELD_NODE_SOURCE, Labels.Web.name());
			
			if (null != uni && !uni.isEmpty()) {
				map.put(FIELD_UNI_URL, uni);
				if (null != uniHaveGrants && !uniHaveGrants.isEmpty()) {
					map.put(FIELD_UNI_HAVE_GRANTS, uniHaveGrants.equals("Y"));
				}
			}
			
			node = graphDb.getOrCreateNode(
					indexWebResearcher, FIELD_FULL_NAME, fullName, map);
			if (!node.hasLabel(Labels.Researcher))
				node.addLabel(Labels.Researcher); 
			if (!node.hasLabel(Labels.Web))
				node.addLabel(Labels.Web); 
			mapWebResearchers.put(fullName, node);	
		}
		
		return node;
    }
    
	private RestNode GetOrCreateRDAResearcher(RestAPI graphDb,
			String fullName, String rda) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(FIELD_RDA, rda);
		map.put(FIELD_FULL_NAME, fullName);
		map.put(FIELD_NODE_TYPE, Labels.Researcher.name());
		map.put(FIELD_NODE_SOURCE, Labels.RDA.name());
		
		RestNode node = graphDb.getOrCreateNode(
				indexRDAResearcher, FIELD_RDA, rda, map);
		if (!node.hasLabel(Labels.Researcher))
			node.addLabel(Labels.Researcher); 
		if (!node.hasLabel(Labels.RDA))
			node.addLabel(Labels.RDA); 
		
		return node;
	}
	
	/*
	private RestNode GetOrCreateNLAResearcher(RestAPI graphDb, RestIndex<Node> indexNlaResearcher, 
			String fullName, String nla) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(FIELD_NLA, nla);
		map.put(FIELD_FULL_NAME, fullName);
		
		RestNode nodeNla = graphDb.getOrCreateNode(
				indexNlaResearcher, FIELD_NLA, nla, map);
		if (!nodeNla.hasLabel(Labels.Researcher))
			nodeNla.addLabel(Labels.Researcher); 
		if (!nodeNla.hasLabel(Labels.NLA))
			nodeNla.addLabel(Labels.NLA); 
		
		return nodeNla;
	}
	
	
	private RestNode GetOrCreateHDLResearcher(RestAPI graphDb, RestIndex<Node> indexHdlResearcher, 
			String fullName, String hdl) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(FIELD_HDL, hdl);
		map.put(FIELD_FULL_NAME, fullName);
		
		RestNode nodeHdl = graphDb.getOrCreateNode(
				indexHdlResearcher, FIELD_NLA, hdl, map);
		if (!nodeHdl.hasLabel(Labels.Researcher))
			nodeHdl.addLabel(Labels.Researcher); 
		if (!nodeHdl.hasLabel(Labels.HDL))
			nodeHdl.addLabel(Labels.HDL); 
		
		return nodeHdl;
	}
	*/
	
	private RestNode GetNHMRCGrant(RestAPI graphDb, String grantId) {
		RestNode node = mapNHMRCGrants.get(grantId);
		if (null == node) {
			IndexHits<Node> nodes = indexNHMRCGrant.get(FIELD_NHMRC_GRANT_ID, grantId);
			node = (RestNode) nodes.getSingle();
		
			if (node != null)
				mapNHMRCGrants.put(grantId, node);				
		}
		
		return node;
	} 
	
	private RestNode GetARCGrant(RestAPI graphDb, String grantId) {
		RestNode node = mapARCGrants.get(grantId);
		if (null == node) {
			IndexHits<Node> nodes = indexARCGrant.get(FIELD_ARC_PROJECT_ID, grantId);
			node = (RestNode) nodes.getSingle();
		
			if (node != null)
				mapARCGrants.put(grantId, node);			
		}
		
		return node;
	} 
	
	private RestNode GetOrCreateDryadDataSet(RestAPI graphDb, String doi) {
		RestNode node = mapDryadDatasets.get(doi);
		if (null == node) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(FIELD_DOI, doi);
			map.put(FIELD_NODE_TYPE, Labels.Dataset.name());
			map.put(FIELD_NODE_SOURCE, Labels.Dryad.name());
			
			node = graphDb.getOrCreateNode(indexDryadDataSet, FIELD_DOI, doi, map);
			if (!node.hasLabel(Labels.Dataset))
				node.addLabel(Labels.Dataset); 
			if (!node.hasLabel(Labels.Dryad))
				node.addLabel(Labels.Dryad); 
			
			mapDryadDatasets.put(doi, node);
		}
		
		return node;
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
		
	private void ProcessRda(RestAPI graphDb, RestNode nodeWebResearcher, 
			String fullName, String rda) {
		if (null != rda && !rda.isEmpty()) {
			RestNode nodeRda = GetOrCreateRDAResearcher(graphDb, fullName, rda);
			CreateUniqueRelationship(nodeWebResearcher, nodeRda, RelTypes.KnownAs, true);
		}
	}
	
	/*
	private void ProcessNla(RestAPI graphDb, RestIndex<Node> indexNlaResearcher, RestNode nodeResearcher, 
			String fullName, String nla) {
		if (null != nla && !nla.isEmpty()) {
			RestNode nodeNla = GetOrCreateNLAResearcher(graphDb, indexNlaResearcher, fullName, nla);
			CreateUniqueRelationship(nodeResearcher, nodeNla, RelTypes.KnownAs, true);
		}
	}
	
	private void ProcessHdl(RestAPI graphDb, RestIndex<Node> indexHdlResearcher, RestNode nodeResearcher, 
			String fullName, String hdl) {
		if (null != hdl && !hdl.isEmpty()) {
			RestNode nodeHdl = GetOrCreateHDLResearcher(graphDb, indexHdlResearcher, fullName, hdl);
			CreateUniqueRelationship(nodeResearcher, nodeHdl, RelTypes.KnownAs, true);
		}
	}*/
	
	private void ProcessGrant(RestAPI graphDb, RestNode nodeResearcher, 
			String grant) {
		if (null != grant && !grant.isEmpty()) {
			
			try {
				URL url = new URL(grant);
				String[] segments = url.getPath().split("/");
				
				if (segments.length == 5 
						&& segments[1].equals("au-research") 
						&& segments[2].equals("grants")) {
					RestNode nodeGrant = null;
					if (segments[3].equals("nhmrc")) {
						nodeGrant = GetNHMRCGrant(graphDb, segments[4]);
					} else if (segments[3].equals("arc")) {
						nodeGrant = GetARCGrant(graphDb, segments[4]);
					}	
					
					if (null != nodeGrant)
						CreateUniqueRelationship(nodeResearcher, nodeGrant, RelTypes.Investigator, false);							
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	}	
	
	private void ProcessDataSet(RestAPI graphDb, RestNode nodeDryadResearcher, String doi) {
		if (null != doi && !doi.isEmpty()) {
			RestNode nodeDataSet = GetOrCreateDryadDataSet(graphDb, doi);
			CreateUniqueRelationship(nodeDryadResearcher, nodeDataSet, RelTypes.RelatedTo, false);			
		}
	}	
	
}
