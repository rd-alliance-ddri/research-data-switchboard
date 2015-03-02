package org.grants.linkers.rda;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.grants.graph.GraphRelationship;
import org.grants.neo4j.local.Neo4jUtils;
import org.graph.aggrigation.AggrigationUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.tooling.GlobalGraphOperations;

public class Linker {
	private static final String PROPERTY_TYPE = "type";
	private static final String PROPERTY_INSTITUTION = "institution";
	private static final String PROPERTY_TITLE= "title";
	private static final String PROPERTY_NAME = "name";
	private static final String PROPERTY_FULL_NAME = "full_name";
	private static final String PROPERTY_FIRST_NAME = "first_name";
	private static final String PROPERTY_MIDDLE_NAME = "middle_name";
	private static final String PROPERTY_LAST_NAME = "last_name";
	private static final String PROPERTY_IDENTIFIER = "identifier";
	private static final String PROPERTY_IDENTIFIER_ARC = "identifier_arc";
	private static final String PROPERTY_IDENTIFIER_NHMRC = "identifier_nhmrc";
	private static final String PROPERTY_DATE = "date";
	private static final String PROPERTY_STATE = "state";
	private static final String PROPERTY_PURL = "purl";
	private static final String PROPERTY_SIMPLIFIED_TITLE = "simplified_title";
	private static final String PROPERTY_SCIENTIFIC_TITLE = "scientific_title";
	private static final String PROPERTY_START_YEAR = "start_year";
	private static final String PROPERTY_END_YEAR = "end_year";
	private static final String PROPERTY_ARC_GRANT_ID = "arc_grant_id";
	private static final String PROPERTY_NHMRC_GRANT_ID = "nhmrc_grant_id";
	private static final String PROPERTY_APPLICATION_YEAR = "application_year";	
	private static final String PROPERTY_RESEARCHERS = "researchers";
	private static final String PROPERTY_CIA_NAME = "cia_name";


	private static final String PROPERTY_RDA_KEY = "rda_key";
	private static final String PROPERTY_RDA_ID = "rda_id";
	private static final String PROPERTY_RDA_URL = "rda_url";
	private static final String PROPERTY_RDA_SLUG = "rda_slug";
	
	private static final String PARTY_TYPE_PERSON = "person";
	private static final String PARTY_TYPE_PUBLISHER = "publisher";
	private static final String PARTY_TYPE_GROUP = "group";
	private static final String PARTY_TYPE_ADMINISTRATIVE_POSITION = "administrativePosition";
	
	private static final String ACTIVITY_TYPE_PROJECT = "project";
	private static final String ACTIVITY_TYPE_PROGRAM = "program";
	private static final String ACTIVITY_TYPE_AWARD = "award";
	private static final String ACTIVITY_TYPE_DATASET = "dataset";
	
	private static final String COLLECTION_TYPE_DATASET = "dataset";
	private static final String COLLECTION_TYPE_NON_GEOGRAPHIC_DATASET = "nonGeographicDataset";
	private static final String COLLECTION_TYPE_RESEARCH_DATASET = "researchDataSet";
	//private static final String COLLECTION_TYPE_COLLECTION = "collection";
	//private static final String COLLECTION_TYPE_DATA_COLLECTION = "Data Collection";
	private static final String COLLECTION_TYPE_REPOSITORY = "repository";
	private static final String COLLECTION_TYPE_REGISTRY = "registry";
	//private static final String COLLECTION_TYPE_SOFTWARE = "software";
	private static final String COLLECTION_TYPE_CATALOGUE_OR_INDEX = "catalogueOrIndex";
	//private static final String COLLECTION_TYPE_SERIES = "series";
	//private static final String COLLECTION_TYPE_MODEL = "model";
	
	private static final String PART_RDA_ID = "oai:ands.org.au::";

	private static enum Labels implements Label {
	    RDA, ARC, NHMRC,															// main RDA label
	    Party, Activity,												// RDA Internal labels
	    Researcher, Institution, AdministrativePosition, 				// RDA Party labels
	    Grant, Award,		 											// RDA Activity labels
	    Dataset, Repository, Registry, CatalogOrIndex, Collection,  // RDA Collection labels
	    Service, Record	     													// RDA Service labels
    };
    
    private static enum Relationhips implements RelationshipType {
    	AdminInstitute, Investigator, adminInsitution, investigator, knownAs
    };
	
    public GraphDatabaseService graphDb1;
    public GraphDatabaseService graphDb2;
    
    private GlobalGraphOperations global;
    
	private Map<String, Index<Node>> mapIndexes = new HashMap<String, Index<Node>>();

	private Index<Node> getIndex(Label labelSource, Label labelType) {
		String label = AggrigationUtils.combineLabel(labelSource, labelType);
		Index<Node> index = mapIndexes.get(label);
		if (null == index) 
			mapIndexes.put(label, index = Neo4jUtils.getNodeIndex(graphDb2, label));
				
		return index;
	}	
	
	public Linker(String neo4j1Folder, String neo4j2Folder) {
		System.out.println("Source Neo4j: " + neo4j1Folder);
		System.out.println("Target Neo4j: " + neo4j2Folder);
		
		// connect to graph database
		graphDb1 = Neo4jUtils.getReadOnlyGraphDb(neo4j1Folder);  
		graphDb2 = Neo4jUtils.getGraphDb(neo4j2Folder);
		
		global = Neo4jUtils.getGlobalOperations(graphDb1);
				
		try ( Transaction tx = graphDb2.beginTx() ) {
			AggrigationUtils.createConstraint(graphDb2,  Labels.RDA, Labels.Researcher);
			AggrigationUtils.createConstraint(graphDb2, Labels.RDA, Labels.Institution);
			AggrigationUtils.createConstraint(graphDb2, Labels.RDA, Labels.Grant);
			AggrigationUtils.createConstraint(graphDb2, Labels.RDA, Labels.Award);
			AggrigationUtils.createConstraint(graphDb2, Labels.RDA, Labels.Dataset);
			AggrigationUtils.createConstraint(graphDb2, Labels.RDA, Labels.Collection);
			AggrigationUtils.createConstraint(graphDb2, Labels.RDA, Labels.Repository);
			AggrigationUtils.createConstraint(graphDb2, Labels.RDA, Labels.Registry);
			AggrigationUtils.createConstraint(graphDb2, Labels.RDA, Labels.CatalogOrIndex);
			AggrigationUtils.createConstraint(graphDb2, Labels.RDA, Labels.Collection);
			AggrigationUtils.createConstraint(graphDb2, Labels.RDA, Labels.Service);
			AggrigationUtils.createConstraint(graphDb2, Labels.RDA, Labels.AdministrativePosition);
			
			AggrigationUtils.createConstraint(graphDb2, Labels.ARC, Labels.Institution);
			AggrigationUtils.createConstraint(graphDb2, Labels.ARC, Labels.Grant);
	
			AggrigationUtils.createConstraint(graphDb2, Labels.NHMRC, Labels.Institution);
			AggrigationUtils.createConstraint(graphDb2, Labels.NHMRC, Labels.Grant);
			AggrigationUtils.createConstraint(graphDb2, Labels.NHMRC, Labels.Researcher);
	
			Neo4jUtils.createIndex(graphDb2, Labels.RDA, AggrigationUtils.PROPERTY_KEY);
			Neo4jUtils.createIndex(graphDb2, Labels.RDA, PROPERTY_IDENTIFIER_ARC);
			Neo4jUtils.createIndex(graphDb2, Labels.RDA, PROPERTY_IDENTIFIER_NHMRC);
			Neo4jUtils.createIndex(graphDb2, Labels.ARC, AggrigationUtils.PROPERTY_KEY);
			Neo4jUtils.createIndex(graphDb2, Labels.NHMRC, AggrigationUtils.PROPERTY_KEY);
			
			tx.success();
		}
	}
	
	public void process() {
		importRDA();
		importARC();
		importNHMRC();
	}
	
	private Node findNode(GraphDatabaseService graphDb, Label label, String key, Object value) {
		ResourceIterator<Node> nodesRda = graphDb.findNodesByLabelAndProperty(label, key, value).iterator();
		if (nodesRda.hasNext())
			return nodesRda.next();

		return null;
	}
	
	private void importRDA() {
		List<GraphRelationship> relations = new ArrayList<GraphRelationship>();
		
		try ( Transaction tx1 = graphDb1.beginTx() ) {
			ResourceIterable<Node> nodes = global.getAllNodesWithLabel( AggrigationUtils.Labels.RDA );
			for (Node node : nodes) 
				if (node.hasLabel( Labels.Record )) {
					String url = (String) node.getProperty(PROPERTY_RDA_URL, null);		
					String slug = (String) node.getProperty(PROPERTY_RDA_SLUG, null);
					String rdaId = (String) node.getProperty(PROPERTY_RDA_ID, null);
					String rdaKey = (String) node.getProperty(PROPERTY_RDA_KEY, null);
					
					if (null == url || url.isEmpty()) 
						throw new Exception("Invalid RDA node url");
					if (null == slug || slug.isEmpty())
						throw new Exception("Invalid RDA node slug");
					if (null == rdaId || rdaId.isEmpty()) 
						throw new Exception("Invalid RDA node id");
					if (null == rdaKey || rdaKey.isEmpty()) 
						throw new Exception("Invalid RDA node key");
					
					
					Node nodeRda = findNode(graphDb1, Labels.RDA, AggrigationUtils.PROPERTY_KEY, rdaKey);
					if (null == nodeRda) {
						String rdaIdString = PART_RDA_ID + rdaId;
				
						nodeRda = findNode(graphDb1, Labels.RDA, PROPERTY_RDA_ID, rdaIdString);
						if (null == nodeRda) {
							System.out.println("Unable to find any RDA node by rda_key: " + rdaKey + " or rda_id:" + rdaIdString);
							continue;
						}
					}
			
					String type = (String) nodeRda.getProperty(PROPERTY_TYPE, null);
					if (null == type || type.isEmpty()) {
						System.out.println("Unknown RDA node type");
						continue;
					}
			
					Label labelType;
			
					// establish the record type
					if (nodeRda.hasLabel(Labels.Party)) {
						// Party
						if (type.equalsIgnoreCase(PARTY_TYPE_PERSON) || type.equalsIgnoreCase(PARTY_TYPE_PUBLISHER)) {
							labelType = Labels.Researcher;
						} else if (type.equalsIgnoreCase(PARTY_TYPE_GROUP)) {
							labelType = Labels.Institution;
						} else if (type.equalsIgnoreCase(PARTY_TYPE_ADMINISTRATIVE_POSITION)) {
							labelType = Labels.AdministrativePosition;
						} else {
							System.out.println("Unknown RDA Node Party type: " + type);
							return;
						}					 
					} else if (nodeRda.hasLabel(Labels.Activity)) {
						/*if (props.containsKey(PROPERTY_IDENTIFIER_NHMRC) || props.containsKey(PROPERTY_IDENTIFIER_ARC)) 
							labelType = Labels.Grant;
						else {
							String purl = (String) props.get(PROPERTY_IDENTIFIER_PURL);
							if (null != purl && (purl.contains("/nhmrc/") || purl.contains("/arc/")))
								labelType = Labels.Grant;
							else
								labelType = Labels.Activity;
						}*/
						
						
						if (type.equalsIgnoreCase(ACTIVITY_TYPE_PROJECT) || type.equalsIgnoreCase(ACTIVITY_TYPE_PROGRAM)) {
							labelType = Labels.Grant;
							
							// add ARC or NHMRC data (if any)
						} else if (type.equalsIgnoreCase(ACTIVITY_TYPE_AWARD)) {
							labelType = Labels.Award;
						} else if (type.equalsIgnoreCase(ACTIVITY_TYPE_DATASET)) {
							System.out.println("Ignoring RDA Node Activity with type Dataset");
							continue;
						} else {
							System.out.println("Unknown RDA Node Activity type: " + type);
							return;
						}		
					} else if (nodeRda.hasLabel(Labels.Collection)) {
						if (type.equalsIgnoreCase(COLLECTION_TYPE_DATASET) || 
								type.equalsIgnoreCase(COLLECTION_TYPE_NON_GEOGRAPHIC_DATASET) ||
								type.equalsIgnoreCase(COLLECTION_TYPE_RESEARCH_DATASET)) {
							labelType = Labels.Dataset;
						} else if (type.equalsIgnoreCase(COLLECTION_TYPE_REPOSITORY)) {
							labelType = Labels.Repository;
						} else if (type.equalsIgnoreCase(COLLECTION_TYPE_REGISTRY)) {
							labelType = Labels.Registry;
						} else if (type.equalsIgnoreCase(COLLECTION_TYPE_CATALOGUE_OR_INDEX)) {
							labelType = Labels.CatalogOrIndex;
						} else {
							labelType = Labels.Collection;
						} 	
					} else if (nodeRda.hasLabel(Labels.Service)) {
						labelType = Labels.Service;
					} else {
						System.out.println("Unknown RDA Node label");
						return;
					}
					
					Map<String, Object> map = new HashMap<String, Object>();
			
					map.put(PROPERTY_RDA_URL, url);
					map.put(PROPERTY_RDA_SLUG, slug);
					map.put(PROPERTY_RDA_ID, rdaId);
					map.put(PROPERTY_RDA_KEY, rdaKey);
			
					for (Map.Entry<String, Object> entry : props.entrySet()) {
						String entryKey = entry.getKey();
						if (entryKey.equals(PROPERTY_INSTITUTION)
								|| entryKey.equals(Neo4jUtils.PROPERTY_URL)
								|| entryKey.equals(PROPERTY_DATE) 
								|| entryKey.equals(PROPERTY_NAME) 
								|| entryKey.equals(PROPERTY_IDENTIFIER)
								|| entryKey.startsWith(PROPERTY_DATE + "_")
								|| entryKey.startsWith(PROPERTY_NAME + "_")
								|| entryKey.startsWith(PROPERTY_IDENTIFIER + "_"))
							map.put(entryKey, entry.getValue());
					}			
			
					System.out.println("Creating Node RDA:" + labelType.name() + " (" + url + ")");
			
					Neo4jUtils.createUniqueNode(graphDb2, getIndex(Labels.RDA, labelType),
							Labels.RDA, labelType, url, map);
			
					Iterable<Relationship> rels = nodeRecord.getRelationships();
					for (Relationship rel : rels) {
						Relation r = new Relation();
						
						r.setKeyFrom((String) ((RestNode) rel.getStartNode()).getProperty(PROPERTY_RDA_URL));
						r.setKeyTo((String) ((RestNode) rel.getEndNode()).getProperty(PROPERTY_RDA_URL));
						r.setRelationName(rel.getType().name().trim());
						
						// awoid bad relationships
						if (r.getRelationName().contains(" "))
							r.setRelationName("relatedTo");
						
						relations.add(r);		
					}
				}
				
				for (Relation r : relations) {
					
					System.out.println("Creating Relationship (" + r.getKeyFrom() + ")-[" + r.getRelationName() + "]->(" + r.getKeyTo() + ")");
					
					RestNode nodeFrom = Neo4jUtils.findNodeByKey(engine2, Labels.RDA, r.getKeyFrom());
					if (null == nodeFrom) {
						System.out.println("Unable to find source RDA node by it's key");
						continue;
					}
				
					RestNode nodeTo = Neo4jUtils.findNodeByKey(engine2, Labels.RDA, r.getKeyTo());
					if (null == nodeTo) {
						System.out.println("Unable to find target RDA node by it's key");
						continue;
					}
		
					Neo4jUtils.createUniqueRelationship(graphDb2, nodeFrom, nodeTo, 
							DynamicRelationshipType.withName(r.getRelationName()), Direction.OUTGOING, null);	
				}
	}

	private void importARC() {
		/* Query all Institutions */
		QueryResult<Map<String, Object>> articles = engine1.query("MATCH (n:ARC:Institution) RETURN n", null);
		for (Map<String, Object> row : articles) {
			RestNode nodeInstitutionSrc = (RestNode) row.get("n");
			
			if (null == nodeInstitutionSrc) {
				System.out.println("Invalid node");
				return;
			}
			Map<String, Object> map = new HashMap<String, Object>();
			
			String key = (String) nodeInstitutionSrc.getProperty(Neo4jUtils.PROPERTY_KEY);
			if (null == key || key.isEmpty()) {
				System.out.println("Unable to find ARC Institution node key");
				continue;
			}
		
			copyProperty(map, nodeInstitutionSrc, PROPERTY_NAME);
			copyProperty(map, nodeInstitutionSrc, PROPERTY_STATE);
			
			System.out.println("Creating Node ARC:Institution (" + key + ")");
			
			RestNode nodeInstitution = Neo4jUtils.createUniqueNode(graphDb2, getIndex(Labels.ARC, Labels.Institution),
					Labels.ARC, Labels.Institution, key, map);
			
			Iterable<Relationship> relsInstitution = nodeInstitutionSrc.getRelationships(Relationhips.AdminInstitute);
			for (Relationship relInsitution : relsInstitution) {
				RestNode nodeGrantSrc = (RestNode) relInsitution.getStartNode();

				//props = Neo4jUtils.getProperties(nodeGrantSrc);			
				map = new HashMap<String, Object>();
				
				key = (String) nodeGrantSrc.getProperty(Neo4jUtils.PROPERTY_KEY);
				if (null == key || key.isEmpty()) {
					System.out.println("Unable to find ARC Grant node key");
					continue;
				}
				
				String grantId = (String) nodeGrantSrc.getProperty(PROPERTY_ARC_GRANT_ID);
				if (null != grantId || !grantId.isEmpty()) 
					map.put(PROPERTY_IDENTIFIER_ARC, grantId);
								
				copyProperty(map, nodeGrantSrc, PROPERTY_PURL);
				copyProperty(map, nodeGrantSrc, PROPERTY_SCIENTIFIC_TITLE);
				copyProperty(map, nodeGrantSrc, PROPERTY_SIMPLIFIED_TITLE);
				copyProperty(map, nodeGrantSrc, PROPERTY_APPLICATION_YEAR);
				copyProperty(map, nodeGrantSrc, PROPERTY_START_YEAR);
				copyProperty(map, nodeGrantSrc, PROPERTY_END_YEAR);
				
				Set<String> researchers = new HashSet<String>();
				
				Iterable<Relationship> relsGrant= nodeGrantSrc.getRelationships(Relationhips.Investigator);
				for (Relationship relGrant : relsGrant) {
					RestNode nodeResearcherSrc = (RestNode) relGrant.getStartNode();
					
					String fullName = (String) nodeResearcherSrc.getProperty(PROPERTY_FULL_NAME);
					fullName = fullName.trim();
					if (null != fullName && !fullName.isEmpty()) 
						researchers.add(fullName);
				}
				
				if (!researchers.isEmpty())
					map.put(PROPERTY_RESEARCHERS, researchers);
							
				System.out.println("Creating Node Grant (" + key + ")");
				
				RestNode nodeGrant = Neo4jUtils.createUniqueNode(graphDb2, getIndex(Labels.ARC, Labels.Grant),
						Labels.ARC, Labels.Grant, key, map);
				
				Neo4jUtils.createUniqueRelationship(graphDb2, nodeInstitution, nodeGrant, 
						Relationhips.adminInsitution, Direction.OUTGOING, null);
				
				if (null != grantId && !grantId.isEmpty()) {
					RestNode nodeRdaGrant = Neo4jUtils.findNodeByKey(engine2, Labels.RDA, Labels.Grant, 
							PROPERTY_IDENTIFIER_ARC, grantId);
				
					if (null != nodeRdaGrant) 
						Neo4jUtils.createUniqueRelationship(graphDb2, nodeRdaGrant, nodeGrant, 
								Relationhips.knownAs, Direction.BOTH, null);
				}
			}		
		}
	}
	
	private void importNHMRC() {
		/* Query all Institutions */
		QueryResult<Map<String, Object>> articles = engine1.query("MATCH (n:NHMRC:Institution) RETURN n", null);
		for (Map<String, Object> row : articles) {
			RestNode nodeInstitutionSrc = (RestNode) row.get("n");
			
			if (null == nodeInstitutionSrc) {
				System.out.println("Invalid node");
				return;
			}
			Map<String, Object> map = new HashMap<String, Object>();
			
			String key = (String) nodeInstitutionSrc.getProperty(Neo4jUtils.PROPERTY_KEY);
			if (null == key || key.isEmpty()) {
				System.out.println("Unable to find ARC Institution node key");
				continue;
			}
		
			copyProperty(map, nodeInstitutionSrc, PROPERTY_NAME);
			copyProperty(map, nodeInstitutionSrc, PROPERTY_STATE);
			
			System.out.println("Creating Node NHMRC:Institution (" + key + ")");
			
			RestNode nodeInstitution = Neo4jUtils.createUniqueNode(graphDb2, getIndex(Labels.NHMRC, Labels.Institution),
					Labels.NHMRC, Labels.Institution, key, map);
			
			Iterable<Relationship> relsInstitution = nodeInstitutionSrc.getRelationships(Relationhips.AdminInstitute);
			for (Relationship relInsitution : relsInstitution) {
				RestNode nodeGrantSrc = (RestNode) relInsitution.getStartNode();

				//props = Neo4jUtils.getProperties(nodeGrantSrc);			
				map = new HashMap<String, Object>();
				
				key = (String) nodeGrantSrc.getProperty(Neo4jUtils.PROPERTY_KEY);
				if (null == key || key.isEmpty()) {
					System.out.println("Unable to find ARC Grant node key");
					continue;
				}
				
				Integer grantId = (Integer) nodeGrantSrc.getProperty(PROPERTY_NHMRC_GRANT_ID);
				if (null != grantId) 
					map.put(PROPERTY_IDENTIFIER_NHMRC, grantId);
								
				copyProperty(map, nodeGrantSrc, PROPERTY_PURL);
				copyProperty(map, nodeGrantSrc, PROPERTY_SCIENTIFIC_TITLE);
				copyProperty(map, nodeGrantSrc, PROPERTY_SIMPLIFIED_TITLE);
				copyProperty(map, nodeGrantSrc, PROPERTY_APPLICATION_YEAR);
				copyProperty(map, nodeGrantSrc, PROPERTY_START_YEAR);
				copyProperty(map, nodeGrantSrc, PROPERTY_END_YEAR);
				copyProperty(map, nodeGrantSrc, PROPERTY_CIA_NAME);
				
				System.out.println("Creating Node NHMRC:Grant (" + key + ")");

				RestNode nodeGrant = Neo4jUtils.createUniqueNode(graphDb2, getIndex(Labels.NHMRC, Labels.Grant),
						Labels.NHMRC, Labels.Grant, key, map);
				
				Neo4jUtils.createUniqueRelationship(graphDb2, nodeInstitution, nodeGrant, 
						Relationhips.adminInsitution, Direction.OUTGOING, null);
				
				Iterable<Relationship> relsGrant= nodeGrantSrc.getRelationships(Relationhips.Investigator);
				for (Relationship relGrant : relsGrant) {
					RestNode nodeResearcherSrc = (RestNode) relGrant.getStartNode();
					
					//props = Neo4jUtils.getProperties(nodeGrantSrc);			
					map = new HashMap<String, Object>();
					
					key = (String) nodeResearcherSrc.getProperty(Neo4jUtils.PROPERTY_KEY);
					if (null == key || key.isEmpty()) {
						System.out.println("Unable to find ARC Grant node key");
						continue;
					}
									
					copyProperty(map, nodeResearcherSrc, PROPERTY_TITLE);
					copyProperty(map, nodeResearcherSrc, PROPERTY_FULL_NAME);
					copyProperty(map, nodeResearcherSrc, PROPERTY_FIRST_NAME);
					copyProperty(map, nodeResearcherSrc, PROPERTY_MIDDLE_NAME);
					copyProperty(map, nodeResearcherSrc, PROPERTY_LAST_NAME);
					
					System.out.println("Creating Node NHMRC:Researcher (" + key + ")");

					RestNode nodeResearcher = Neo4jUtils.createUniqueNode(graphDb2, getIndex(Labels.NHMRC, Labels.Researcher),
							Labels.NHMRC, Labels.Researcher, key, map);
					
					Neo4jUtils.createUniqueRelationship(graphDb2, nodeResearcher, nodeGrant, 
							Relationhips.investigator, Direction.OUTGOING, null);
				}
				
				if (null != grantId) {
					RestNode nodeRdaGrant = Neo4jUtils.findNodeByKey(engine2, Labels.RDA, Labels.Grant, 
							PROPERTY_IDENTIFIER_NHMRC, grantId.toString());
				
					if (null != nodeRdaGrant) 
						Neo4jUtils.createUniqueRelationship(graphDb2, nodeRdaGrant, nodeGrant, 
								Relationhips.knownAs, Direction.BOTH, null);
				}		
			}		
		}
	}
	
	private void copyProperty(Map<String, Object> map, RestNode node, String key) {
		if (node.hasProperty(key)) {
			Object value = node.getProperty(key);
			if (null != value)
				map.put(key, value);
		}
	}
}