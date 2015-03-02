package org.grants.exporters.harmonized;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.grants.graph.GraphConnection;
import org.grants.graph.GraphIndex;
import org.grants.graph.GraphNode;
import org.grants.graph.GraphRelationship;
import org.grants.graph.GraphUtils;
import org.grants.neo4j.local.Neo4jUtils;
import org.graph.aggrigation.AggrigationUtils;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Exporter {
	private static final int MAX_COMMANDS = 1024;
	
	private GraphDatabaseService graphDb;
	private GlobalGraphOperations global;
	
	private final File schemaFolder;
	private final File nodesFolder;
	private final File relationshipsFolder;
			
	private static final ObjectMapper mapper = new ObjectMapper(); 
	
	private enum Labels implements Label {
		ARC, NHMRC, Work, Orcid
	}
	
	private static final String PROPERTY_NAME_PRIMARY = "name_primary";
	private static final String PROPERTY_NAME_FORMELY = "name_formerly";
	private static final String PROPERTY_NAME_ALTERNATIVE = "name_alternative";
	private static final String PROPERTY_NAME_TEXT = "name_text";
	private static final String PROPERTY_NAME_FULL = "name_full";
	private static final String PROPERTY_NAME = "name";
	private static final String PROPERTY_IDENTIFIER_NLA = "identifier_nla";
	private static final String PROPERTY_IDENTIFIER_NLA_PARTY = "identifier_nla.party";
	private static final String PROPERTY_IDENTIFIER_PULR = "identifier_purl";
	private static final String PROPERTY_IDENTIFIER_URI = "identifier_uri";
	private static final String PROPERTY_IDENTIFIER_AU_ANL_PEAU = "identifier_AU-ANL:PEAU";
	private static final String PROPERTY_IDENTIFIER_ORCID = "identifier_orcid";
	private static final String PROPERTY_IDENTIFIER_ARC = "identifier_arc";
	private static final String PROPERTY_IDENTIFIER_NHMRC = "identifier_nhmrc";
	private static final String PROPERTY_IDENTIFIER_DOI = "identifier_doi";
	private static final String PROPERTY_IDENTIFIER_DOI2 = "identifier_DOI";
	private static final String PROPERTY_IDENTIFIER_ISBN = "identifier_ISSN";
	private static final String PROPERTY_GYVEN_NAMES = "gyven_names";
	private static final String PROPERTY_GIVEN_NAME = "given_name";
	private static final String PROPERTY_FAMILY_NAME = "family_name";
	private static final String PROPERTY_ORCID_ID = "orcid_id";
	private static final String PROPERTY_SCIENTIFIC_TITLE = "scientific_title";
	private static final String PROPERTY_SIMPLIFIED_TITLE = "simplified_title";
	private static final String PROPERTY_RESEARCHERS = "researchers";
	private static final String PROPERTY_START_YEAR = "start_year";
	private static final String PROPERTY_APPLICATION_YEAR = "application_year";
	private static final String PROPERTY_AUTHOR = "author";
	private static final String PROPERTY_CONTRIBUTORS = "contributors";
	private static final String PROPERTY_PUBLICATION_DATE = "publication_date";
	
	private List<GraphIndex> graphIndex;
	private List<GraphNode> graphNodes;
	private List<GraphRelationship> graphRelationships;
	
//	private Map<String, Set<Long>> mapKeys = new HashMap<String, Set<Long>>();
	private Map<Long, String> mapInstitutionsKeys = new HashMap<Long, String>();
	private Map<Long, String> mapResearchersKeys = new HashMap<Long, String>();
	private Map<Long, String> mapGrantsKeys = new HashMap<Long, String>();
	private Map<Long, String> mapPublicationsKeys = new HashMap<Long, String>();
	private Map<Long, String> mapDatasetsKeys = new HashMap<Long, String>();
	
	private Map<Long, List<PendingRelation>> pendingRelationships = new HashMap<Long, List<PendingRelation>>();
	
	private long nodeFileCounter = 0;
	private long relationshipFileCounter;
	
	/**
	 * Class constructor 
	 * 
	 * @param nodeSource
	 * @param nodeType
	 * @param propertyName
	 * @param dbFolder
	 * @param outputFolder
	 */
	public Exporter(final String dbFolder, final String outputFolder) {
		System.out.println("Source Neo4j folder: " + dbFolder);
		System.out.println("Target folder: " + outputFolder);
			
		//graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbPath);
		graphDb = Neo4jUtils.getReadOnlyGraphDb(dbFolder);
		global = Neo4jUtils.getGlobalOperations(graphDb);
		
		// Set output folder
		File folder = new File(outputFolder);

		schemaFolder = GraphUtils.getSchemaFolder(folder);
		nodesFolder = GraphUtils.getNodeFolder(folder);
		relationshipsFolder = GraphUtils.getRelationshipFolder(folder);
		
		schemaFolder.mkdirs();
		nodesFolder.mkdirs();
		relationshipsFolder.mkdirs();
	}
	
	/**
	 * Function to perform a export
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public void process() throws JsonGenerationException, JsonMappingException, IOException {
		exportIndex();
		
		exportInstitutions();
		exportResearchers();
		exportGrants();
		exportDatasets();
		exportPublications();
		
		if (null != graphNodes)
			saveNodes();
		if (null != graphRelationships)
			saveRelationships();
		
		System.out.println("Done! Exported "
				+ mapInstitutionsKeys.size() + " institutions, "
				+ mapResearchersKeys.size() + " researchers, " 
				+ mapGrantsKeys.size() + " grants, "
				+ mapPublicationsKeys.size() + " publications and "
				+ mapDatasetsKeys.size() + " datasets");
	}
	
	
	public void exportIndex() throws JsonGenerationException, JsonMappingException, IOException {
		System.out.println("Create indexes");
		
		graphIndex = new ArrayList<GraphIndex>();
		
		// init constraints
		graphIndex.add(new GraphIndex(AggrigationUtils.LABEL_INSTITUTION, AggrigationUtils.PROPERTY_KEY, true));
		graphIndex.add(new GraphIndex(AggrigationUtils.LABEL_RESEARCHER, AggrigationUtils.PROPERTY_KEY, true));
		graphIndex.add(new GraphIndex(AggrigationUtils.LABEL_GRANT, AggrigationUtils.PROPERTY_KEY, true));
		graphIndex.add(new GraphIndex(AggrigationUtils.LABEL_DATASET, AggrigationUtils.PROPERTY_KEY, true));
		graphIndex.add(new GraphIndex(AggrigationUtils.LABEL_PUBLICATION, AggrigationUtils.PROPERTY_KEY, true));

		graphIndex.add(new GraphIndex(AggrigationUtils.LABEL_RDA, AggrigationUtils.PROPERTY_KEY, true));
		graphIndex.add(new GraphIndex(AggrigationUtils.LABEL_ORCID, AggrigationUtils.PROPERTY_KEY, true));
		graphIndex.add(new GraphIndex(AggrigationUtils.LABEL_DRYAD, AggrigationUtils.PROPERTY_KEY, true));
		graphIndex.add(new GraphIndex(AggrigationUtils.LABEL_FIGSHARE, AggrigationUtils.PROPERTY_KEY, true));

		graphIndex.add(new GraphIndex(AggrigationUtils.LABEL_INSTITUTION, AggrigationUtils.PROPERTY_TITLE, false));
		graphIndex.add(new GraphIndex(AggrigationUtils.LABEL_GRANT, AggrigationUtils.PROPERTY_TITLE, false));
		graphIndex.add(new GraphIndex(AggrigationUtils.LABEL_DATASET, AggrigationUtils.PROPERTY_TITLE, false));
		graphIndex.add(new GraphIndex(AggrigationUtils.LABEL_PUBLICATION, AggrigationUtils.PROPERTY_TITLE, false));

		graphIndex.add(new GraphIndex(AggrigationUtils.LABEL_RESEARCHER, AggrigationUtils.PROPERTY_INITIALS, false));
		graphIndex.add(new GraphIndex(AggrigationUtils.LABEL_RESEARCHER, AggrigationUtils.PROPERTY_FIRST_NAME, false));
		graphIndex.add(new GraphIndex(AggrigationUtils.LABEL_RESEARCHER, AggrigationUtils.PROPERTY_LAST_NAME, false));
		graphIndex.add(new GraphIndex(AggrigationUtils.LABEL_RESEARCHER, AggrigationUtils.PROPERTY_FULL_NAME, false));
		
		graphIndex.add(new GraphIndex(AggrigationUtils.LABEL_INSTITUTION, AggrigationUtils.PROPERTY_NODE_TYPE, false));
		graphIndex.add(new GraphIndex(AggrigationUtils.LABEL_RESEARCHER, AggrigationUtils.PROPERTY_NODE_TYPE, false));
		graphIndex.add(new GraphIndex(AggrigationUtils.LABEL_GRANT, AggrigationUtils.PROPERTY_NODE_TYPE, false));
		graphIndex.add(new GraphIndex(AggrigationUtils.LABEL_DATASET, AggrigationUtils.PROPERTY_NODE_TYPE, false));
		graphIndex.add(new GraphIndex(AggrigationUtils.LABEL_PUBLICATION, AggrigationUtils.PROPERTY_NODE_TYPE, false));

		graphIndex.add(new GraphIndex(AggrigationUtils.LABEL_INSTITUTION, AggrigationUtils.PROPERTY_DATE, false));
		graphIndex.add(new GraphIndex(AggrigationUtils.LABEL_GRANT, AggrigationUtils.PROPERTY_DATE, false));
		graphIndex.add(new GraphIndex(AggrigationUtils.LABEL_DATASET, AggrigationUtils.PROPERTY_DATE, false));
		graphIndex.add(new GraphIndex(AggrigationUtils.LABEL_PUBLICATION, AggrigationUtils.PROPERTY_DATE, false));
		
		graphIndex.add(new GraphIndex(AggrigationUtils.LABEL_INSTITUTION, AggrigationUtils.PROPERTY_NLA, false));
		graphIndex.add(new GraphIndex(AggrigationUtils.LABEL_RESEARCHER, AggrigationUtils.PROPERTY_NLA, false));

		graphIndex.add(new GraphIndex(AggrigationUtils.LABEL_DATASET, AggrigationUtils.PROPERTY_DOI, false));
		graphIndex.add(new GraphIndex(AggrigationUtils.LABEL_PUBLICATION, AggrigationUtils.PROPERTY_DOI, false));

		graphIndex.add(new GraphIndex(AggrigationUtils.LABEL_RESEARCHER, AggrigationUtils.PROPERTY_ORCID, false));
		
		graphIndex.add(new GraphIndex(AggrigationUtils.LABEL_PUBLICATION, AggrigationUtils.PROPERTY_ISBN, false));

		graphIndex.add(new GraphIndex(AggrigationUtils.LABEL_GRANT, AggrigationUtils.PROPERTY_PURL, false));
		graphIndex.add(new GraphIndex(AggrigationUtils.LABEL_GRANT, AggrigationUtils.PROPERTY_GRANT_NUMBER, false));

		saveSchema();
	}
	
	/**
	 * Institution properties:
	 * key
	 * title
	 * nla
	 * isni
	 * country
	 * 
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonGenerationException 
	 */
	
	private void exportInstitutions() throws JsonGenerationException, JsonMappingException, IOException {
		System.out.println("Export institutions");
		
		try ( Transaction tx = graphDb.beginTx() ) {
			ResourceIterable<Node> institutions = global.getAllNodesWithLabel( AggrigationUtils.Labels.Institution );
			for (Node institution : institutions) 
				if (!mapInstitutionsKeys.containsKey(institution.getId())) {
					
					Map<String, Object> properties = new HashMap<String, Object>();
					properties.put(AggrigationUtils.PROPERTY_NODE_TYPE, AggrigationUtils.LABEL_INSTITUTION);
					
					Set<String> sources = new HashSet<String>();
					
					Map<Long, Node> nodes = getKnownAs(institution, null);
					for (Node node : nodes.values())  
						if (node.hasLabel(AggrigationUtils.Labels.RDA)) {
							exportRdaInstitution(node, properties);
							sources.add(AggrigationUtils.LABEL_RDA);
						}
					for (Node node : nodes.values()) 
						if (node.hasLabel(AggrigationUtils.Labels.Web)) {
							exportWebInstitution(node, properties);
							sources.add(AggrigationUtils.LABEL_WEB);
						}
					for (Node node : nodes.values()) {
						if (node.hasLabel(Labels.ARC)) {
							exportArcOrNhmrcInstitution(node, properties);
							sources.add(Labels.ARC.name());
						}
						if (node.hasLabel(Labels.NHMRC))  {
							exportArcOrNhmrcInstitution(node, properties);
							sources.add(Labels.NHMRC.name());
						}						
					}
					
					if (sources.size() == 1)
						properties.put(AggrigationUtils.PROPERTY_NODE_SOURCE, sources.iterator().next());
					else if (sources.size() > 1)
						properties.put(AggrigationUtils.PROPERTY_NODE_SOURCE, sources.toArray(new String[sources.size()]));

					String key = (String) properties.get(AggrigationUtils.PROPERTY_KEY);
					for (Long nodeId : nodes.keySet())
						mapInstitutionsKeys.put(nodeId, key);
					
					if (null != key) {
						System.out.println(AggrigationUtils.LABEL_INSTITUTION + ": " + key);
						
						exportGraphNode(new GraphNode(properties));
						
						for (Node node : nodes.values()) 
							exportRelationships(node, AggrigationUtils.LABEL_INSTITUTION, key);
					}
				}
		}
	}
	
	
	
	/**
	 * Researcher properties:
	 * key
	 * node_type
	 * initials
	 * first_name
	 * last_name
	 * full_name
	 * nla
	 * orcid
	 * isni
	 * country
	 * for_codes
	 * 
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public void exportResearchers() throws JsonGenerationException, JsonMappingException, IOException {
		System.out.println("Export researchers");
		
		try ( Transaction tx = graphDb.beginTx() ) {
			ResourceIterable<Node> researchers = global.getAllNodesWithLabel( AggrigationUtils.Labels.Researcher );
			for (Node researcher : researchers) 
				if (!mapResearchersKeys.containsKey(researcher.getId())) {
					
					Map<String, Object> properties = new HashMap<String, Object>();
					properties.put(AggrigationUtils.PROPERTY_NODE_TYPE, AggrigationUtils.LABEL_RESEARCHER);
					
					Set<String> sources = new HashSet<String>();

					Map<Long, Node> nodes = getKnownAs(researcher, null);
					for (Node node : nodes.values()) 
						if (node.hasLabel(Labels.Orcid)) {
							exportOrcidResearcher(node, properties);
							sources.add(AggrigationUtils.LABEL_ORCID);
						}
					for (Node node : nodes.values()) 
						if (node.hasLabel(AggrigationUtils.Labels.Web)) {
							exportWebResearcher(node, properties);
							sources.add(AggrigationUtils.LABEL_WEB);
						}
					for (Node node : nodes.values()) 
						if (node.hasLabel(AggrigationUtils.Labels.RDA)) {
							exportRdaResearcher(node, properties);
							sources.add(AggrigationUtils.LABEL_RDA);
						}
					for (Node node : nodes.values()) 
						if (node.hasLabel(AggrigationUtils.Labels.FigShare)) {
							exportFigShareResearcher(node, properties);
							sources.add(AggrigationUtils.LABEL_FIGSHARE);
						}
					for (Node node : nodes.values()) 
						if (node.hasLabel(AggrigationUtils.Labels.CrossRef)) {
							exportCrossRefResearcher(node, properties);
							sources.add(AggrigationUtils.LABEL_CROSSREF);
						}
					for (Node node : nodes.values()) 
						if (node.hasLabel(Labels.NHMRC)) {
							exportNHMRCRefResearcher(node, properties);
							sources.add(Labels.NHMRC.name());
						}
					
					if (sources.size() == 1)
						properties.put(AggrigationUtils.PROPERTY_NODE_SOURCE, sources.iterator().next());
					else if (sources.size() > 1)
						properties.put(AggrigationUtils.PROPERTY_NODE_SOURCE, sources.toArray(new String[sources.size()]));
					
					String key = (String) properties.get(AggrigationUtils.PROPERTY_KEY);
					for (Long nodeId : nodes.keySet())
						mapResearchersKeys.put(nodeId, key);

					if (null != key) {
						System.out.println(AggrigationUtils.LABEL_RESEARCHER + ": " + key);
						
						exportGraphNode(new GraphNode(properties));
						
						for (Node node : nodes.values()) 
							exportRelationships(node, AggrigationUtils.LABEL_RESEARCHER, key);
				}
			}
		}
	}
	
	/**
	 * Grant properties:
	 * key
	 * node_type
	 * title
	 * authors
	 * date
	 * purl
	 * grant_number
	 * country
	 * for_codes
	 * 
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	
	private void exportGrants() throws JsonGenerationException, JsonMappingException, IOException {
		System.out.println("Export grants");
		
		try ( Transaction tx = graphDb.beginTx() ) {
			ResourceIterable<Node> grants = global.getAllNodesWithLabel( AggrigationUtils.Labels.Grant );
			for (Node grant : grants) 
				if (!mapGrantsKeys.containsKey(grant.getId())) {
					
					Map<String, Object> properties = new HashMap<String, Object>();
					properties.put(AggrigationUtils.PROPERTY_NODE_TYPE, AggrigationUtils.LABEL_GRANT);
					
					Set<String> sources = new HashSet<String>();
					
					Map<Long, Node> nodes = getKnownAs(grant, null);
					for (Node node : nodes.values()) 
						if (node.hasLabel(AggrigationUtils.Labels.RDA)) {
							exportRDAGrant(node, properties);
							sources.add(AggrigationUtils.LABEL_RDA);
						}
					for (Node node : nodes.values()) 
						if (node.hasLabel(Labels.ARC)) {
							exportARCGrant(node, properties);
							sources.add(Labels.ARC.name());
						}
					for (Node node : nodes.values()) 
						if (node.hasLabel(Labels.NHMRC)) {
							exportNHMRCGrant(node, properties);
							sources.add(Labels.NHMRC.name());
						}
					
					if (sources.size() == 1)
						properties.put(AggrigationUtils.PROPERTY_NODE_SOURCE, sources.iterator().next());
					else if (sources.size() > 1)
						properties.put(AggrigationUtils.PROPERTY_NODE_SOURCE, sources.toArray(new String[sources.size()]));
									
					String key = (String) properties.get(AggrigationUtils.PROPERTY_KEY);
					for (Long nodeId : nodes.keySet())
						mapGrantsKeys.put(nodeId, key);
					
					if (null != key) {
						System.out.println(AggrigationUtils.LABEL_GRANT + ": " + key);
						
						exportGraphNode(new GraphNode(properties));
						
						for (Node node : nodes.values()) 
							exportRelationships(node, AggrigationUtils.LABEL_GRANT, key);
				}
			}
		}
	}
	
	/**
	 * Dataset properties:
	 * key
	 * node_type
	 * title
	 * authors
	 * date
	 * doi
	 * country
	 * for_codes
	 * 
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	
	private void exportDatasets() throws JsonGenerationException, JsonMappingException, IOException {
		System.out.println("Export datasets");
		
		try ( Transaction tx = graphDb.beginTx() ) {
			ResourceIterable<Node> datasets = global.getAllNodesWithLabel( AggrigationUtils.Labels.Dataset );
			for (Node dataset : datasets) 
				if (!mapDatasetsKeys.containsKey(dataset.getId())) {
					
					Map<String, Object> properties = new HashMap<String, Object>();
					properties.put(AggrigationUtils.PROPERTY_NODE_TYPE, AggrigationUtils.LABEL_DATASET);
					
					Set<String> sources = new HashSet<String>();
					
					Map<Long, Node> nodes = getKnownAs(dataset, null);
					for (Node node : nodes.values()) 
						if (node.hasLabel(AggrigationUtils.Labels.RDA)) {
							exportRDADataset(node, properties);
							sources.add(AggrigationUtils.LABEL_RDA);
						}
					for (Node node : nodes.values()) 
						if (node.hasLabel(AggrigationUtils.Labels.Dryad)) {
							exportDryadDataset(node, properties);
							sources.add(AggrigationUtils.LABEL_DRYAD);
						}
					
					if (sources.size() == 1)
						properties.put(AggrigationUtils.PROPERTY_NODE_SOURCE, sources.iterator().next());
					else if (sources.size() > 1)
						properties.put(AggrigationUtils.PROPERTY_NODE_SOURCE, sources.toArray(new String[sources.size()]));
								
					String key = (String) properties.get(AggrigationUtils.PROPERTY_KEY);
					for (Long nodeId : nodes.keySet())
						mapDatasetsKeys.put(nodeId, key);
					
					if (null != key) {
						System.out.println(AggrigationUtils.LABEL_DATASET + ": " + key);
						
						exportGraphNode(new GraphNode(properties));
						
						for (Node node : nodes.values()) 
							exportRelationships(node, AggrigationUtils.LABEL_DATASET, key);
				}
			}
		}
	}
	
	/**
	 * Dataset properties:
	 * key
	 * node_type
	 * title
	 * authors
	 * publicaiton_type
	 * date
	 * doi
	 * isbn
	 * for_codes
	 * 
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	
	private void exportPublications() throws JsonGenerationException, JsonMappingException, IOException {
		System.out.println("Export publication");
			
		try ( Transaction tx = graphDb.beginTx() ) {
			ResourceIterable<Node> works = global.getAllNodesWithLabel( Labels.Work );
			for (Node work : works) 
				if (!mapPublicationsKeys.containsKey(work.getId())) {
					
					Map<String, Object> properties = new HashMap<String, Object>();
					properties.put(AggrigationUtils.PROPERTY_NODE_TYPE, AggrigationUtils.LABEL_PUBLICATION);
					
					Set<String> sources = new HashSet<String>();
					
					Map<Long, Node> nodes = getKnownAs(work, null);
					for (Node node : nodes.values()) 
						if (node.hasLabel(Labels.Orcid)) {
							exportOrcidPublication(node, properties);							
							sources.add(AggrigationUtils.LABEL_ORCID);
						}
					for (Node node : nodes.values()) 
						if (node.hasLabel(AggrigationUtils.Labels.CrossRef)) {
							exportCrossRefPublication(node, properties);
							sources.add(AggrigationUtils.LABEL_CROSSREF);
						}
					for (Node node : nodes.values()) 
						if (node.hasLabel(AggrigationUtils.Labels.FigShare)) {
							exportFigSharePublication(node, properties);
							sources.add(AggrigationUtils.LABEL_FIGSHARE);
						}
					for (Node node : nodes.values()) 
						if (node.hasLabel(AggrigationUtils.Labels.Dryad)) {
							exportDryadPublication(node, properties);
							sources.add(AggrigationUtils.LABEL_DRYAD);
						}
					
					if (sources.size() == 1)
						properties.put(AggrigationUtils.PROPERTY_NODE_SOURCE, sources.iterator().next());
					else if (sources.size() > 1)
						properties.put(AggrigationUtils.PROPERTY_NODE_SOURCE, sources.toArray(new String[sources.size()]));
					
					String key = (String) properties.get(AggrigationUtils.PROPERTY_KEY);
					for (Long nodeId : nodes.keySet())
						mapPublicationsKeys.put(nodeId, key);
					
					if (null != key) {
						System.out.println(AggrigationUtils.LABEL_PUBLICATION + ": " + key);
						
						exportGraphNode(new GraphNode(properties));
						
						for (Node node : nodes.values()) 
							exportRelationships(node, AggrigationUtils.LABEL_PUBLICATION, key);
				}
			}
			
			ResourceIterable<Node> publications = global.getAllNodesWithLabel( AggrigationUtils.Labels.Publication );
			for (Node publication : publications) 
				if (!mapPublicationsKeys.containsKey(publication.getId())) {
					
					Map<String, Object> properties = new HashMap<String, Object>();
					properties.put(AggrigationUtils.PROPERTY_NODE_TYPE, AggrigationUtils.LABEL_PUBLICATION);
					
					Set<String> sources = new HashSet<String>();
					
					Map<Long, Node> nodes = getKnownAs(publication, null);
					for (Node node : nodes.values()) 
						if (node.hasLabel(AggrigationUtils.Labels.CrossRef)) {
							exportCrossRefPublication(node, properties);
							sources.add(AggrigationUtils.LABEL_CROSSREF);
						}
					for (Node node : nodes.values()) 
						if (node.hasLabel(AggrigationUtils.Labels.FigShare)) {
							exportFigSharePublication(node, properties);
							sources.add(AggrigationUtils.LABEL_FIGSHARE);
						}
					for (Node node : nodes.values()) 
						if (node.hasLabel(AggrigationUtils.Labels.Dryad)) {
							exportDryadPublication(node, properties);
							sources.add(AggrigationUtils.LABEL_DRYAD);
						}
					
					if (sources.size() == 1)
						properties.put(AggrigationUtils.PROPERTY_NODE_SOURCE, sources.iterator().next());
					else if (sources.size() > 1)
						properties.put(AggrigationUtils.PROPERTY_NODE_SOURCE, sources.toArray(new String[sources.size()]));
					
					String key = (String) properties.get(AggrigationUtils.PROPERTY_KEY);
					for (Long nodeId : nodes.keySet())
						mapPublicationsKeys.put(nodeId, key);
					
					if (null != key) {
						System.out.println(AggrigationUtils.LABEL_PUBLICATION + ": " + key);
						
						exportGraphNode(new GraphNode(properties));
						
						for (Node node : nodes.values()) 
							exportRelationships(node, AggrigationUtils.LABEL_PUBLICATION, key);
				}
			}
		}
	}
	
	
	private void exportProperty(Node node, Map<String, Object> properties, String key1, String key2) {
		if (!properties.containsKey(key2) && node.hasProperty(key1)) 
			properties.put(key2, node.getProperty(key1));
	}
	
	private void exportUri(Node node, Map<String, Object> properties, String key1, String key2) {
		if (!properties.containsKey(key2) && node.hasProperty(key1)) {
			Object value = node.getProperty(key1);
			if (null != value && value instanceof String) {
				String uri = AggrigationUtils.extractUri((String) value);
				if (null != uri && !uri.isEmpty())
					properties.put(key2, uri);
			}	
		}
	}
	
	private void exportOrcid(Node node, Map<String, Object> properties, String key1, String key2) {
		if (!properties.containsKey(key2) && node.hasProperty(key1)) {
			Object value = node.getProperty(key1);
			if (null != value && value instanceof String) {
				String orcid = AggrigationUtils.extractOrcid((String) value);
				if (null != orcid && !orcid.isEmpty())
					properties.put(key2, orcid);
			}	
		}
	}

	private void exportDoi(Node node, Map<String, Object> properties, String key1, String key2) {
		if (!properties.containsKey(key2) && node.hasProperty(key1)) {
			Object value = node.getProperty(key1);
			if (null != value && value instanceof String) {
				String doi = AggrigationUtils.extractDoi((String) value);
				if (null != doi && !doi.isEmpty())
					properties.put(key2, doi);
			}	
		}
	}
	
	private void exportDoiUri(Node node, Map<String, Object> properties, String key1, String key2) {
		if (!properties.containsKey(key2) && node.hasProperty(key1)) {
			Object value = node.getProperty(key1);
			if (null != value && value instanceof String) {
				String doi = AggrigationUtils.generateDoiUri(AggrigationUtils.extractDoi((String) value));
				if (null != doi && !doi.isEmpty())
					properties.put(key2, doi);
			}	
		}
	}

	private void exportGraphNode(GraphNode grahpNode) throws JsonGenerationException, JsonMappingException, IOException {
		if (null == graphNodes)
			graphNodes = new ArrayList<GraphNode>();
		
		graphNodes.add(grahpNode);
		
		if (graphNodes.size() >= MAX_COMMANDS)
			saveNodes();
	}
	
	private void exportGraphRelationship(GraphRelationship graphRelationship) throws JsonGenerationException, JsonMappingException, IOException {
		if (null == graphRelationships)
			graphRelationships = new ArrayList<GraphRelationship>();
		
		graphRelationships.add(graphRelationship);
		
		if (graphRelationships.size() >= MAX_COMMANDS)
			saveRelationships();
	}
	
/*	private boolean isConnectionAvaliable(Map<String, Object> properties) {
		return properties.containsKey(AggrigationUtils.PROPERTY_KEY) 
				&& properties.containsKey(AggrigationUtils.PROPERTY_NODE_TYPE);
	}*/
	
	private void exportSourceConnection(Map<String, Object> properties, String nodeSource, String url) throws JsonGenerationException, JsonMappingException, IOException {
		if (properties.containsKey(AggrigationUtils.PROPERTY_KEY)) {
			String key = AggrigationUtils.extractUri(url);
			if (null != key) {
				Map<String, Object> prop = new HashMap<String, Object>();
				prop.put(AggrigationUtils.PROPERTY_KEY, key);
				prop.put(AggrigationUtils.PROPERTY_NODE_TYPE, nodeSource);
				
				GraphNode node = new GraphNode(prop);
				
				GraphConnection start = new GraphConnection(null, 
						(String) properties.get(AggrigationUtils.PROPERTY_NODE_TYPE), 
						(String) properties.get(AggrigationUtils.PROPERTY_KEY));
				GraphConnection end = new GraphConnection(null, nodeSource, key);
				GraphRelationship rel = new GraphRelationship(AggrigationUtils.REL_KNOWN_AS, null, start, end);
				
				exportGraphNode(node);
				exportGraphRelationship(rel);
			}
		}
	}
		
	private void exportRdaInstitution(Node node, Map<String, Object> properties) throws JsonGenerationException, JsonMappingException, IOException {
		exportUri(node, properties, AggrigationUtils.PROPERTY_URL, AggrigationUtils.PROPERTY_KEY);
//		exportUri(node, properties, PROPERTY_IDENTIFIER_URI, AggrigationUtils.PROPERTY_KEY);
		exportUri(node, properties, PROPERTY_IDENTIFIER_AU_ANL_PEAU, AggrigationUtils.PROPERTY_NLA);
		exportUri(node, properties, PROPERTY_IDENTIFIER_NLA_PARTY, AggrigationUtils.PROPERTY_NLA);
		exportUri(node, properties, PROPERTY_IDENTIFIER_PULR, AggrigationUtils.PROPERTY_PURL);
		exportProperty(node, properties, PROPERTY_NAME_PRIMARY, AggrigationUtils.PROPERTY_TITLE);
		exportProperty(node, properties, PROPERTY_NAME_FORMELY, AggrigationUtils.PROPERTY_TITLE);
		exportProperty(node, properties, PROPERTY_NAME, AggrigationUtils.PROPERTY_TITLE);
		exportProperty(node, properties, PROPERTY_NAME_ALTERNATIVE, AggrigationUtils.PROPERTY_TITLE);
		
		exportSourceConnection(properties, AggrigationUtils.LABEL_RDA, (String) node.getProperty(AggrigationUtils.PROPERTY_KEY));
	}	
	
	private void exportWebInstitution(Node node, Map<String, Object> properties) {
		exportUri(node, properties, AggrigationUtils.PROPERTY_URL, AggrigationUtils.PROPERTY_KEY);
		exportProperty(node, properties, AggrigationUtils.PROPERTY_TITLE, AggrigationUtils.PROPERTY_TITLE);
		exportProperty(node, properties, AggrigationUtils.PROPERTY_COUNTRY, AggrigationUtils.PROPERTY_COUNTRY);	
	}
	
	private void exportArcOrNhmrcInstitution(Node node, Map<String, Object> properties) {
		// Only export node, if it key is avaliable
	//	exportUri(node, properties, PROPERTY_NAME, AggrigationUtils.PROPERTY_KEY);
		exportProperty(node, properties, PROPERTY_NAME, AggrigationUtils.PROPERTY_TITLE);

//		exportSourceConnection(properties, (String) node.getProperty(AggrigationUtils.PROPERTY_NODE_SOURCE), (String) node.getProperty(AggrigationUtils.PROPERTY_KEY));
	}
	
	private void exportOrcidResearcher(Node node, Map<String, Object> properties) throws JsonGenerationException, JsonMappingException, IOException {
		exportUri(node, properties, AggrigationUtils.PROPERTY_KEY, AggrigationUtils.PROPERTY_KEY);
		exportProperty(node, properties, PROPERTY_GYVEN_NAMES, AggrigationUtils.PROPERTY_FIRST_NAME);
		exportProperty(node, properties, PROPERTY_FAMILY_NAME, AggrigationUtils.PROPERTY_LAST_NAME);
		exportProperty(node, properties, AggrigationUtils.PROPERTY_FULL_NAME, AggrigationUtils.PROPERTY_FULL_NAME);
		exportOrcid(node, properties, PROPERTY_ORCID_ID, AggrigationUtils.PROPERTY_ORCID);
		
		exportSourceConnection(properties, AggrigationUtils.LABEL_ORCID, (String) properties.get(AggrigationUtils.PROPERTY_KEY));
	}
	
	private void exportWebResearcher(Node node, Map<String, Object> properties) {
		exportUri(node, properties, AggrigationUtils.PROPERTY_KEY, AggrigationUtils.PROPERTY_KEY);
	}
	
	private void exportRdaResearcher(Node node, Map<String, Object> properties) throws JsonGenerationException, JsonMappingException, IOException {
		exportUri(node, properties, AggrigationUtils.PROPERTY_KEY, AggrigationUtils.PROPERTY_KEY);
		exportProperty(node, properties, PROPERTY_NAME_PRIMARY, AggrigationUtils.PROPERTY_FULL_NAME);
		exportProperty(node, properties, PROPERTY_NAME_ALTERNATIVE, AggrigationUtils.PROPERTY_FULL_NAME);
		exportUri(node, properties, PROPERTY_IDENTIFIER_AU_ANL_PEAU, AggrigationUtils.PROPERTY_NLA);
		exportUri(node, properties, PROPERTY_IDENTIFIER_NLA, AggrigationUtils.PROPERTY_NLA);
		exportOrcid(node, properties, PROPERTY_IDENTIFIER_ORCID, AggrigationUtils.PROPERTY_ORCID);
		
		exportSourceConnection(properties, AggrigationUtils.LABEL_RDA, (String) node.getProperty(AggrigationUtils.PROPERTY_KEY));
	}
	
	private void exportFigShareResearcher(Node node, Map<String, Object> properties) throws JsonGenerationException, JsonMappingException, IOException {
		exportUri(node, properties, AggrigationUtils.PROPERTY_KEY, AggrigationUtils.PROPERTY_KEY);
		exportProperty(node, properties, PROPERTY_NAME, AggrigationUtils.PROPERTY_FULL_NAME);
		exportOrcid(node, properties, AggrigationUtils.PROPERTY_ORCID, AggrigationUtils.PROPERTY_ORCID);

		exportSourceConnection(properties, AggrigationUtils.LABEL_FIGSHARE, (String) node.getProperty(AggrigationUtils.PROPERTY_KEY));
	}
	
	private void exportCrossRefResearcher(Node node, Map<String, Object> properties) {
		exportProperty(node, properties, PROPERTY_GIVEN_NAME, AggrigationUtils.PROPERTY_FIRST_NAME);
		exportProperty(node, properties, PROPERTY_FAMILY_NAME, AggrigationUtils.PROPERTY_LAST_NAME);
		exportProperty(node, properties, AggrigationUtils.PROPERTY_FULL_NAME, AggrigationUtils.PROPERTY_FULL_NAME);
		exportOrcid(node, properties, AggrigationUtils.PROPERTY_ORCID, AggrigationUtils.PROPERTY_ORCID);
	}
	
	private void exportNHMRCRefResearcher(Node node, Map<String, Object> properties) {
		exportProperty(node, properties, PROPERTY_FAMILY_NAME, AggrigationUtils.PROPERTY_LAST_NAME);
	}
	
	private void exportRDAGrant(Node node, Map<String, Object> properties) throws JsonGenerationException, JsonMappingException, IOException {
		exportUri(node, properties, PROPERTY_IDENTIFIER_PULR, AggrigationUtils.PROPERTY_KEY);
		exportUri(node, properties, AggrigationUtils.PROPERTY_KEY, AggrigationUtils.PROPERTY_KEY);
		exportProperty(node, properties, PROPERTY_NAME_PRIMARY, AggrigationUtils.PROPERTY_TITLE);
		exportProperty(node, properties, PROPERTY_NAME_ALTERNATIVE, AggrigationUtils.PROPERTY_TITLE);
		exportProperty(node, properties, PROPERTY_NAME, AggrigationUtils.PROPERTY_TITLE);
		exportUri(node, properties, PROPERTY_IDENTIFIER_PULR, AggrigationUtils.PROPERTY_PURL);
		exportProperty(node, properties, PROPERTY_IDENTIFIER_ARC, AggrigationUtils.PROPERTY_GRANT_NUMBER);
		exportProperty(node, properties, PROPERTY_IDENTIFIER_NHMRC, AggrigationUtils.PROPERTY_GRANT_NUMBER);
	
		exportSourceConnection(properties, AggrigationUtils.LABEL_RDA, (String) node.getProperty(AggrigationUtils.PROPERTY_KEY));
	}
	
	private void exportARCGrant(Node node, Map<String, Object> properties) {
		exportUri(node, properties, AggrigationUtils.PROPERTY_KEY, AggrigationUtils.PROPERTY_KEY);
		exportProperty(node, properties, PROPERTY_SCIENTIFIC_TITLE, AggrigationUtils.PROPERTY_TITLE);
		exportUri(node, properties, AggrigationUtils.PROPERTY_PURL, AggrigationUtils.PROPERTY_PURL);
		exportProperty(node, properties, PROPERTY_RESEARCHERS, AggrigationUtils.PROPERTY_AUTHORS);
		exportProperty(node, properties, PROPERTY_IDENTIFIER_ARC, AggrigationUtils.PROPERTY_GRANT_NUMBER);
		exportProperty(node, properties, PROPERTY_START_YEAR, AggrigationUtils.PROPERTY_DATE);
		exportProperty(node, properties, PROPERTY_APPLICATION_YEAR, AggrigationUtils.PROPERTY_DATE);
	}
	
	private void exportNHMRCGrant(Node node, Map<String, Object> properties) {
		exportUri(node, properties, AggrigationUtils.PROPERTY_KEY, AggrigationUtils.PROPERTY_KEY);
		exportProperty(node, properties, PROPERTY_SCIENTIFIC_TITLE, AggrigationUtils.PROPERTY_TITLE);
		exportProperty(node, properties, PROPERTY_SIMPLIFIED_TITLE, AggrigationUtils.PROPERTY_TITLE);
		exportUri(node, properties, AggrigationUtils.PROPERTY_PURL, AggrigationUtils.PROPERTY_PURL);
		exportProperty(node, properties, PROPERTY_START_YEAR, AggrigationUtils.PROPERTY_DATE);
		exportProperty(node, properties, PROPERTY_APPLICATION_YEAR, AggrigationUtils.PROPERTY_DATE);
	}
	
	private void exportRDADataset(Node node, Map<String, Object> properties) throws JsonGenerationException, JsonMappingException, IOException {
		exportDoiUri(node, properties, PROPERTY_IDENTIFIER_DOI, AggrigationUtils.PROPERTY_KEY);
		exportUri(node, properties, AggrigationUtils.PROPERTY_KEY, AggrigationUtils.PROPERTY_KEY);
		exportDoi(node, properties, PROPERTY_IDENTIFIER_DOI, AggrigationUtils.PROPERTY_DOI);
		exportProperty(node, properties, PROPERTY_NAME_PRIMARY, AggrigationUtils.PROPERTY_TITLE);
		exportProperty(node, properties, PROPERTY_NAME_ALTERNATIVE, AggrigationUtils.PROPERTY_TITLE);
		exportProperty(node, properties, PROPERTY_NAME, AggrigationUtils.PROPERTY_TITLE);
		exportProperty(node, properties, PROPERTY_NAME_FULL, AggrigationUtils.PROPERTY_TITLE);
		exportProperty(node, properties, PROPERTY_NAME_TEXT, AggrigationUtils.PROPERTY_TITLE);
		exportDoi(node, properties, PROPERTY_IDENTIFIER_DOI, AggrigationUtils.PROPERTY_DOI);

		exportSourceConnection(properties, AggrigationUtils.LABEL_RDA, (String) node.getProperty(AggrigationUtils.PROPERTY_KEY));
	}
	
	private void exportDryadDataset(Node node, Map<String, Object> properties) throws JsonGenerationException, JsonMappingException, IOException {
		exportDoiUri(node, properties, AggrigationUtils.PROPERTY_DOI, AggrigationUtils.PROPERTY_KEY);
		exportUri(node, properties, AggrigationUtils.PROPERTY_URL, AggrigationUtils.PROPERTY_KEY);
		exportDoi(node, properties, AggrigationUtils.PROPERTY_DOI, AggrigationUtils.PROPERTY_DOI);
		exportProperty(node, properties, AggrigationUtils.PROPERTY_TITLE, AggrigationUtils.PROPERTY_TITLE);
		exportProperty(node, properties, PROPERTY_AUTHOR, AggrigationUtils.PROPERTY_AUTHORS);

		exportSourceConnection(properties, AggrigationUtils.LABEL_DRYAD, (String) properties.get(AggrigationUtils.PROPERTY_KEY));
	}
	
	private void exportOrcidPublication(Node node, Map<String, Object> properties) {
		exportDoiUri(node, properties, PROPERTY_IDENTIFIER_DOI2, AggrigationUtils.PROPERTY_KEY);
		exportDoi(node, properties, PROPERTY_IDENTIFIER_DOI2, AggrigationUtils.PROPERTY_DOI);
		exportProperty(node, properties, AggrigationUtils.PROPERTY_TITLE, AggrigationUtils.PROPERTY_TITLE);
		exportProperty(node, properties, PROPERTY_IDENTIFIER_ISBN, AggrigationUtils.PROPERTY_ISBN);
		exportProperty(node, properties, PROPERTY_CONTRIBUTORS, AggrigationUtils.PROPERTY_AUTHORS);
		exportProperty(node, properties, PROPERTY_PUBLICATION_DATE, AggrigationUtils.PROPERTY_DATE);
	}
	
	private void exportCrossRefPublication(Node node, Map<String, Object> properties) {
		exportDoiUri(node, properties, AggrigationUtils.PROPERTY_KEY, AggrigationUtils.PROPERTY_KEY);
		exportDoi(node, properties, AggrigationUtils.PROPERTY_KEY, AggrigationUtils.PROPERTY_DOI);
		exportProperty(node, properties, AggrigationUtils.PROPERTY_TITLE, AggrigationUtils.PROPERTY_TITLE);
		exportProperty(node, properties, PROPERTY_AUTHOR, AggrigationUtils.PROPERTY_AUTHORS);
	}
	
	private void exportFigSharePublication(Node node, Map<String, Object> properties) throws JsonGenerationException, JsonMappingException, IOException {
		exportDoiUri(node, properties, AggrigationUtils.PROPERTY_KEY, AggrigationUtils.PROPERTY_KEY);
		exportDoi(node, properties, AggrigationUtils.PROPERTY_KEY, AggrigationUtils.PROPERTY_DOI);
		exportProperty(node, properties, AggrigationUtils.PROPERTY_TITLE, AggrigationUtils.PROPERTY_TITLE);
		exportProperty(node, properties, AggrigationUtils.PROPERTY_AUTHORS, AggrigationUtils.PROPERTY_AUTHORS);

		exportSourceConnection(properties, AggrigationUtils.LABEL_FIGSHARE, (String) node.getProperty(AggrigationUtils.PROPERTY_URL));
	}
	
	private void exportDryadPublication(Node node, Map<String, Object> properties) throws JsonGenerationException, JsonMappingException, IOException {
		exportDoiUri(node, properties, AggrigationUtils.PROPERTY_DOI, AggrigationUtils.PROPERTY_KEY);
		exportDoi(node, properties, AggrigationUtils.PROPERTY_DOI, AggrigationUtils.PROPERTY_DOI);
		exportProperty(node, properties, AggrigationUtils.PROPERTY_TITLE, AggrigationUtils.PROPERTY_TITLE);
		exportProperty(node, properties, AggrigationUtils.PROPERTY_AUTHORS, AggrigationUtils.PROPERTY_AUTHORS);
	
		exportSourceConnection(properties, AggrigationUtils.LABEL_DRYAD, (String) properties.get(AggrigationUtils.PROPERTY_KEY));
	}
	
	private Map<Long, Node> getKnownAs(Node node, Map<Long, Node> map) {
		if (null == map)
			map = new HashMap<Long, Node>();
		map.put(node.getId(), node);
		
		Iterable<Relationship> rels = node.getRelationships(AggrigationUtils.RelTypes.knownAs);
		for (Relationship rel : rels) {
			Node other = rel.getOtherNode(node);
			if (!map.containsKey(other.getId()))
				getKnownAs(other, map);
		}
		
		return map;
	}
	
	public void exportRelationships(Node start, String startType, String startKey) throws JsonGenerationException, JsonMappingException, IOException {
		long startId = start.getId();
		if (pendingRelationships.containsKey(startId)) {
			for (PendingRelation rel : pendingRelationships.get(startId)) {
				
				long endId = rel.getNodeId();
				
				String endType = null;
				String endKey = null;
				
				if (mapInstitutionsKeys.containsKey(endId)) {
					endType = AggrigationUtils.LABEL_INSTITUTION;
					endKey = mapInstitutionsKeys.get(endId);
				} else if (mapResearchersKeys.containsKey(endId)) {
					endType = AggrigationUtils.LABEL_RESEARCHER;
					endKey = mapResearchersKeys.get(endId);
				} else if (mapGrantsKeys.containsKey(endId)) {
					endType = AggrigationUtils.LABEL_GRANT;
					endKey = mapGrantsKeys.get(endId);
				} else if (mapDatasetsKeys.containsKey(endId)) {
					endType = AggrigationUtils.LABEL_DATASET;
					endKey = mapDatasetsKeys.get(endId);
				} else if (mapPublicationsKeys.containsKey(endId)) {
					endType = AggrigationUtils.LABEL_PUBLICATION;
					endKey = mapPublicationsKeys.get(endId);
				} 				
				
				// checl that connection is possible and tha we do not connect node to is self
				if (null != endType && null != endKey && (!startType.equals(endType) || !startKey.equals(endKey))) {
					// We need to inverse relationship, because pendingRelationships contains missing id for start node 
					GraphConnection graphStart = new GraphConnection(null, endType, endKey);
					GraphConnection graphEnd = new GraphConnection(null, startType, startKey);
					GraphRelationship graphRel = new GraphRelationship(rel.getType().name(), null, graphStart, graphEnd);
				
					exportGraphRelationship(graphRel);
				}
			}
			
			pendingRelationships.remove(startId);
		}
		
		Iterable<Relationship> rels = start.getRelationships(Direction.OUTGOING);
		for (Relationship rel : rels) {
			Node end = rel.getEndNode();
			long endId = end.getId();
			
			String endType = null;
			String endKey = null;
			
			if (mapInstitutionsKeys.containsKey(endId)) {
				endType = AggrigationUtils.LABEL_INSTITUTION;
				endKey = mapInstitutionsKeys.get(endId);
			} else if (mapResearchersKeys.containsKey(endId)) {
				endType = AggrigationUtils.LABEL_RESEARCHER;
				endKey = mapResearchersKeys.get(endId);
			} else if (mapGrantsKeys.containsKey(endId)) {
				endType = AggrigationUtils.LABEL_GRANT;
				endKey = mapGrantsKeys.get(endId);
			} else if (mapDatasetsKeys.containsKey(endId)) {
				endType = AggrigationUtils.LABEL_DATASET;
				endKey = mapDatasetsKeys.get(endId);
			} else if (mapPublicationsKeys.containsKey(endId)) {
				endType = AggrigationUtils.LABEL_PUBLICATION;
				endKey = mapPublicationsKeys.get(endId);
			} 

			// check that we have found a type
			if (null != endType) {
				// check that connection is possible and we do not connect node to is self
				if (null != endKey && (!startType.equals(endType) || !startKey.equals(endKey))) {
					GraphConnection graphStart = new GraphConnection(null, startType, startKey);
					GraphConnection graphEnd = new GraphConnection(null, endType, endKey);
					GraphRelationship graphRel = new GraphRelationship(rel.getType().name(), null, graphStart, graphEnd);
					
					exportGraphRelationship(graphRel);
				}
			} else {
				PendingRelation pendingRelation = new PendingRelation(startId, rel.getType());
				
				if (pendingRelationships.containsKey(endId)) 
					pendingRelationships.get(endId).add(pendingRelation);
				else {
					List<PendingRelation> list = new ArrayList<PendingRelation>();
					list.add(pendingRelation);
					pendingRelationships.put(endId, list);
				}					
			}
		}
	}

	private void saveSchema() throws JsonGenerationException, JsonMappingException, IOException {
		mapper.writeValue(new File(schemaFolder, GraphUtils.GRAPH_SCHEMA), graphIndex);
		graphIndex = null;
	}
	
	private void saveNodes() throws JsonGenerationException, JsonMappingException, IOException {
		String fileName = Long.toString(nodeFileCounter) + GraphUtils.GRAPH_EXTENSION;
		mapper.writeValue(new File(nodesFolder, fileName), graphNodes);
		graphNodes = null;
		++nodeFileCounter;
	}
	
	private void saveRelationships() throws JsonGenerationException, JsonMappingException, IOException {
		String fileName = Long.toString(relationshipFileCounter) + GraphUtils.GRAPH_EXTENSION;
		mapper.writeValue(new File(relationshipsFolder, fileName), graphRelationships);
		graphRelationships = null;
		++relationshipFileCounter;
	}
}
