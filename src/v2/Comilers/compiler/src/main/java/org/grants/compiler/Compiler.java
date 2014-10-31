package org.grants.compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.grants.crossref.Author;
import org.grants.crossref.CrossRef;
import org.grants.crossref.Item;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.RestAPIFacade;
import org.neo4j.rest.graphdb.entity.RestNode;
import org.neo4j.rest.graphdb.index.RestIndex;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;
import org.neo4j.rest.graphdb.util.QueryResult;

/**
 * Compiler class
 * <p>
 * This class designed to load data from first Neo4j Instance and populate it into second instance
 * The instances expected to be hosted ad 4747 and 4746 ports
 * <p>
 * The program will automatically create all indexes and constraints on the second instance, needed 
 * for a job.
 * 
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */
public class Compiler {
	private static final String NEO4J1_URI = "http://localhost:7474/db/data/";
	private static final String NEO4J2_URI = "http://localhost:7476/db/data/";
	
	/*
	private static final String LABEL_RECORD = "Record";
	private static final String LABEL_PUBLICATION = "Publication";
	private static final String LABEL_RESEARCHER = "Researcher";
	private static final String LABEL_DATASET = "Dataset";
	
	private static final String LABEL_RDA = "RDA";
	
	private static final String LABEL_DRYAD = "Dryad";
	private static final String LABEL_DRYAD_PUBLICATION = LABEL_DRYAD + "_" + LABEL_PUBLICATION;
	private static final String LABEL_DRYAD_DATASET = LABEL_DRYAD + "_" + LABEL_DATASET;
	
	private static final String LABEL_CROSSREF = "CrossRef";
	private static final String LABEL_CROSSREF_PUBLICATION = LABEL_CROSSREF + "_" + LABEL_PUBLICATION;
	private static final String LABEL_CROSSREF_RESEARCHER = LABEL_CROSSREF + "_" + LABEL_RESEARCHER;*/
	
/*	private static final String RELATIONSHIP_AUTHOR = "author";
	private static final String RELATIONSHIP_EDITOR = "editor";
	private static final String RELATIONSHIP_KNOWN_AS = "knownAs";
	private static final String RELATIONSHIP_CONSTITUENT = "constituent";
*/
	
	private static final String PROPERTY_KEY = "key";
	//private static final String PROPERTY_RDA_KEY = "key";
	private static final String PROPERTY_OAI = "oai";
	private static final String PROPERTY_RDA_ID = "rda_id";
	private static final String PROPERTY_NODE_TYPE = "node_type";
	private static final String PROPERTY_NODE_SOURCE = "node_source";
	private static final String PROPERTY_IS_REFERENCED_BY = "isReferencedBy";
	private static final String PROPERTY_URL = "url";
	private static final String PROPERTY_PREFIX = "url";
	private static final String PROPERTY_ISSUE = "issue";
	private static final String PROPERTY_VOLUME = "volume";
	private static final String PROPERTY_TYPE = "type";
	private static final String PROPERTY_PAGE = "page";
	private static final String PROPERTY_PUBLISHER = "publisher";
	private static final String PROPERTY_ISSN = "issn";
	private static final String PROPERTY_TITLE = "title";
	private static final String PROPERTY_SUBTITLE = "subtitle";
	private static final String PROPERTY_SUBJECT = "subject";
	private static final String PROPERTY_CONTAINER_TITLE = "title_container";
	private static final String PROPERTY_AUTHOR = "author";
	private static final String PROPERTY_EDITOR = "editor";
	private static final String PROPERTY_ISSUED = "issued";
	private static final String PROPERTY_DEPOSITED = "deposited";
	private static final String PROPERTY_INDEXED = "indexed";
	private static final String PROPERTY_SCORE = "score";
	private static final String PROPERTY_UPDATE_POLICY = "update_policy";
	private static final String PROPERTY_SUFFIX = "suffix";
	private static final String PROPERTY_FAMILY_NAME = "family_name";
	private static final String PROPERTY_GIVEN_NAME = "given_name";
	private static final String PROPERTY_FULL_NAME = "full_name";
	private static final String PROPERTY_ORCID = "orcid";
	private static final String PROPERTY_RDA_URL = "rda_url";
	private static final String PROPERTY_RDA_SLUG = "rda_slug";
		
	private static final String PART_DOI = "doi:";
	
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
	private static final String COLLECTION_TYPE_SOFTWARE = "software";
	private static final String COLLECTION_TYPE_CATALOGUE_OR_INDEX = "catalogueOrIndex";
	//private static final String COLLECTION_TYPE_SERIES = "series";
	//private static final String COLLECTION_TYPE_MODEL = "model";
	
	
	/*
	private static Label labelPublication = DynamicLabel.label(LABEL_PUBLICATION);
	private static Label labelResearcher = DynamicLabel.label(LABEL_RESEARCHER);
	private static Label labelDataset = DynamicLabel.label(LABEL_DATASET);
	private static Label labelRDA = DynamicLabel.label(LABEL_RDA);
	private static Label labelDryad = DynamicLabel.label(LABEL_DRYAD);
	private static Label labelCrossRef = DynamicLabel.label(LABEL_CROSSREF);*/

	/*private static RelationshipType relAuthor = DynamicRelationshipType.withName(RELATIONSHIP_AUTHOR);
	private static RelationshipType relEditor = DynamicRelationshipType.withName(RELATIONSHIP_EDITOR);
	private static RelationshipType relKnownAs = DynamicRelationshipType.withName(RELATIONSHIP_KNOWN_AS);
	private static RelationshipType relConstituent =  DynamicRelationshipType.withName(RELATIONSHIP_CONSTITUENT);*/
	
	
	private RestAPI graphDb1;
	private RestAPI graphDb2;
	
	private RestCypherQueryEngine engine1;  
	private RestCypherQueryEngine engine2;  
	
	private Map<String, RestIndex<Node>> mapIndexes = new HashMap<String, RestIndex<Node>>();
	
	private static enum Labels implements Label {
	    RDA, Dryad, CrossRef, Collection, Service, Party, Activity, Publication, 
	    Researcher, Dataset, Record, Institution, Grant, Award, Repository, 
	    Registry, CatalogOrIndex, AdministrativePosition
    };
    
    private static enum Relationhips implements RelationshipType {
    	author, editor, knownAs, constituent
    };
    
   
	CrossRef crossref = new CrossRef();
	
	/**
	 * Class constructor
	 * 
	 * The class could be enchanded with Neo4J instance address (no needed for this project)
	 */
	public Compiler() {
		// connect to graph database
		graphDb1 = new RestAPIFacade(NEO4J1_URI);  
		graphDb2 = new RestAPIFacade(NEO4J2_URI);  
				
		// Create cypher engine
		engine1 = new RestCypherQueryEngine(graphDb1);  
		engine2 = new RestCypherQueryEngine(graphDb2);  
		
		// create constraints
		createConstraint(Labels.Dryad, Labels.Publication);
		createConstraint(Labels.Dryad, Labels.Dataset);
		createConstraint(Labels.CrossRef, Labels.Publication);
		createConstraint(Labels.CrossRef, Labels.Researcher);
		createConstraint(Labels.RDA, Labels.Researcher);
		createConstraint(Labels.RDA, Labels.Institution);
		createConstraint(Labels.RDA, Labels.Grant);
		createConstraint(Labels.RDA, Labels.Award);
		createConstraint(Labels.RDA, Labels.Dataset);
		createConstraint(Labels.RDA, Labels.Collection);
		createConstraint(Labels.RDA, Labels.Repository);
		createConstraint(Labels.RDA, Labels.Registry);
		createConstraint(Labels.RDA, Labels.CatalogOrIndex);
		createConstraint(Labels.RDA, Labels.Collection);
		createConstraint(Labels.RDA, Labels.Service);
		createConstraint(Labels.RDA, Labels.AdministrativePosition);
		
		// get indexes
	/*	indexDryadPublication = graphDb2.index().forNodes(LABEL_DRYAD_PUBLICATION);
		indexDryadDataset = graphDb2.index().forNodes(LABEL_DRYAD_DATASET);
		indexCrossrefPublication = graphDb2.index().forNodes(LABEL_CROSSREF_PUBLICATION);
		indexCrossrefResearcher = graphDb2.index().forNodes(LABEL_CROSSREF_RESEARCHER);*/
	}
	
	/**
	 * Main function. Will subsequently create Dryad::Dataset, Dryad::Publication and RDA:Record nodes
	 * with all relationships. The Cern:Publication node currently does not contains any relationship 
	 * and for that job import_cern software could be used. To generate Web:Institutions please use 
	 * import_institutions software.
	 */
	public void process() {
		importDryadDataset();
		importDryadPublications();
		importDryadRelations();			
		importRDARecords();
	}
	
	@SuppressWarnings("unchecked")
	private void importDryadPublications() {
		QueryResult<Map<String, Object>> articles = engine1.query("MATCH (n:Dryad:Record) WHERE n.genre='Article' RETURN n", null);
		for (Map<String, Object> row : articles) {
			RestNode nodeRecord = (RestNode) row.get("n");
		
			if (null == nodeRecord) {
				System.out.println("Invalid node");
				break;
			}
			
			Map<String, Object> props = getProperties(nodeRecord);
			
			String key = (String) props.get(PROPERTY_OAI);
			if (null == key || key.isEmpty()) {
				System.out.println("Invalid node key");
				break;
			}
			
			System.out.println(key);
			
			RestNode nodePublication = createNode(graphDb2, Labels.Dryad, Labels.Publication, PROPERTY_KEY, key, props);
			if (null == nodePublication) {
				System.out.println("Unable to create publication");
				break;
			}
			
			Object referencedBy = props.get(PROPERTY_IS_REFERENCED_BY);
			if (null != referencedBy) {
				if (referencedBy instanceof String) {
					RestNode node = createCrossRefPublication((String) referencedBy);
					if (null != node)
						createUniqueRelationship(graphDb2, nodePublication, node, Relationhips.knownAs, Direction.BOTH, null);
				} else if (referencedBy instanceof String[]) {
					for (String doi :(String[]) referencedBy) {
						RestNode node = createCrossRefPublication((String) doi);
						if (null != node)
							createUniqueRelationship(graphDb2, nodePublication, node, Relationhips.knownAs, Direction.BOTH, null);
					}
				} else if (referencedBy instanceof List<?>) {
					for (String doi :(List<String>) referencedBy) {
						RestNode node = createCrossRefPublication((String) doi);
						if (null != node)
							createUniqueRelationship(graphDb2, nodePublication, node, Relationhips.knownAs, Direction.BOTH, null);
					}
				}
					
			}
		}
	}
	
	private void importDryadDataset() {
		// SKIP {skip_number} LIMIT {limit_number}
		QueryResult<Map<String, Object>> datasets = engine1.query("MATCH (n:Dryad:Record) WHERE n.genre IN ['Dataset', 'dataset']  RETURN n", null);
		for (Map<String, Object> row : datasets) {
			RestNode nodeRecord = (RestNode) row.get("n");
		
			if (null == nodeRecord) {
				System.out.println("Invalid node");
				break;
			}
			
			Map<String, Object> props = getProperties(nodeRecord);
			
			String key = (String) props.get(PROPERTY_OAI);
			if (null == key || key.isEmpty()) {
				System.out.println("Invalid node key");
				break;
			}
			
			System.out.println(key);
			
			createNode(graphDb2, Labels.Dryad, Labels.Dataset, PROPERTY_KEY, key, props);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void importDryadRelations() {
		QueryResult<Map<String, Object>> publications = engine2.query("MATCH (n:Dryad:Publication) WHERE has (n.constituent) RETURN ID(n) as id, n.constituent as doi", null);
		for (Map<String, Object> row : publications) {
			
			int id = (Integer) row.get("id");
			Object doi = row.get("doi");
			
			if (doi != null) {
				RestNode nodePublication = graphDb2.getNodeById(id);
				if (doi instanceof String) {
					 List<RestNode> nodesDatasets = findDryadDatasetByDoi((String) doi);
					 if (null != nodesDatasets) 
						 for (RestNode nodeDataset : nodesDatasets) 
							 createUniqueRelationship(graphDb2, nodePublication, nodeDataset, Relationhips.constituent, Direction.OUTGOING, null);
				} else 
					for (String d : (List<String>) doi) {
						List<RestNode> nodesDatasets = findDryadDatasetByDoi(d);
						if (null != nodesDatasets) 
							for (RestNode nodeDataset : nodesDatasets) 
								createUniqueRelationship(graphDb2, nodePublication, nodeDataset, Relationhips.constituent, Direction.OUTGOING, null);
		
					}
			}
		}
		
		QueryResult<Map<String, Object>> datasets = engine2.query("MATCH (n:Dryad:Dataset) WHERE has (n.host) RETURN ID(n) as id, n.host as doi", null);
		for (Map<String, Object> row : datasets) {
			
			int id = (Integer) row.get("id");
			Object doi = row.get("doi");
			
			if (doi != null) {
				RestNode nodeDataset = graphDb2.getNodeById(id);
				if (doi instanceof String) {
					 List<RestNode> nodesPublications = findDryadPublicationByDoi((String) doi);
					 if (null != nodesPublications) 
						 for (RestNode nodePublication : nodesPublications) 
							 createUniqueRelationship(graphDb2, nodePublication, nodeDataset, Relationhips.constituent, Direction.OUTGOING, null);
				} else 
					for (String d : (List<String>) doi) {
						 List<RestNode> nodesPublications = findDryadPublicationByDoi(d);
						 if (null != nodesPublications) 
							 for (RestNode nodePublication : nodesPublications) 
								 createUniqueRelationship(graphDb2, nodePublication, nodeDataset, Relationhips.constituent, Direction.OUTGOING, null);	
					}
			}
		}
	}
	
	private void importRDARecords() {
		List<Relation> relations = new ArrayList<Relation>();
		
		QueryResult<Map<String, Object>> articles = engine1.query("MATCH (n:RDA:Record) RETURN n", null);
		for (Map<String, Object> row : articles) {
			RestNode nodeRecord = (RestNode) row.get("n");
		
			if (null == nodeRecord) {
				System.out.println("Invalid node");
				return;
			}
			
			Map<String, Object> props = getProperties(nodeRecord);			
			String rdaId = (String) props.get(PROPERTY_RDA_ID);		
			String url = (String) props.get(PROPERTY_RDA_URL);		
			String slug = (String) props.get(PROPERTY_RDA_SLUG);	
			if (null == rdaId || rdaId.isEmpty()) {
				System.out.println("Invalid node key");
				return;
			}
			
			if (null == slug || slug.isEmpty()) {
				System.out.println("The node does not contains slug, ignoring");
				continue;
			}
			
			String rdaKey = "oai:ands.org.au::" + rdaId;
			System.out.println(rdaKey);
			
			List<RestNode> nodes = findRDANodeByRdaKey(rdaKey);
			if (null == nodes || nodes.size() == 0) {
				System.out.println("Unable to find any RDA node by rda_key");
				continue;
			}
			if (nodes.size() != 1) {
				System.out.println("Too many nodes has been find by rda_key");
				return;
			}
				 
			RestNode nodeRda = nodes.get(0);
			props = getProperties(nodeRda);
			
			props.put(PROPERTY_RDA_SLUG, slug);
			props.put(PROPERTY_RDA_ID, rdaId);
			
			String type = (String) props.get(PROPERTY_TYPE);
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
				} else if (type.equalsIgnoreCase(ACTIVITY_TYPE_AWARD)) {
					labelType = Labels.Award;
				} else if (type.equalsIgnoreCase(ACTIVITY_TYPE_DATASET)) {
					labelType = Labels.Dataset;
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
			
			RestNode node =  createNode(graphDb2, Labels.RDA, labelType, PROPERTY_KEY, url, props); 
			
			
			Iterable<Relationship> rels = nodeRecord.getRelationships();
			for (Relationship rel : rels) {
				Relation r = new Relation();
				
				r.keyFrom = (String) ((RestNode) rel.getStartNode()).getProperty(PROPERTY_RDA_URL);
				r.keyTo = (String) ((RestNode) rel.getEndNode()).getProperty(PROPERTY_RDA_URL);
				r.relationName = rel.getType().name().trim();
				
				if (r.relationName.contains(" "))
					r.relationName = "relatedTo";
				
				relations.add(r);		
			}
		}
		
		for (Relation r : relations) {
			
			System.out.println("(" + r.keyFrom + ")-[" + r.relationName + "]->(" + r.keyTo + ")");
			
			List<RestNode> nodesFrom = findRDANodeByKey(r.keyFrom);
			if (null == nodesFrom || nodesFrom.size() == 0) {
				System.out.println("Unable to find any RDA node by the  key");
				continue;
			}
			if (nodesFrom.size() > 1) {
				System.out.println("Too many RDA node has been found by the key");
				return;
			}
			
			List<RestNode> nodesTo = findRDANodeByKey(r.keyTo);
			if (null == nodesTo || nodesTo.size() == 0) {
				System.out.println("Unable to find any RDA node by the  key");
				continue;
			}
			if (nodesTo.size() > 1) {
				System.out.println("Too many RDA node has been found by the key");
				return;
			}
			
			createUniqueRelationship(graphDb2, nodesFrom.get(0), nodesTo.get(0), 
					DynamicRelationshipType.withName(r.relationName), Direction.OUTGOING, null);	
		}
	}
		
	private List<RestNode> findDryadDatasetByDoi(final String doi) {
		List<RestNode> result = null;
		
		Map<String, Object> pars = new HashMap<String, Object>();
		pars.put("doi", doi);
		
		QueryResult<Map<String, Object>> nodes = engine2.query("MATCH (n:Dryad:Dataset) WHERE has(n.doi) and any (m in n.doi WHERE m = {doi}) RETURN n", pars);
		for (Map<String, Object> row : nodes) {
			RestNode node = (RestNode) row.get("n");
			if (null != node) {
				if (null == result)
					result = new ArrayList<RestNode>();
				
				result.add(node);
			}
		}
		
		return result;
	}
	
	private List<RestNode> findRDANodeByRdaKey(final String rdaKey) {
		List<RestNode> result = null;
		
		Map<String, Object> pars = new HashMap<String, Object>();
		pars.put("rda_key", rdaKey);
		
		QueryResult<Map<String, Object>> nodes = engine1.query("MATCH (n:RDA) WHERE has(n.rda_key) and n.rda_key={rda_key} RETURN n", pars);
		for (Map<String, Object> row : nodes) {
			RestNode node = (RestNode) row.get("n");
			if (null != node) {
				if (null == result)
					result = new ArrayList<RestNode>();
				
				result.add(node);
			}
		}
		
		return result;
	}
	
	private List<RestNode> findRDANodeByKey(final String key) {
		List<RestNode> result = null;
		
		Map<String, Object> pars = new HashMap<String, Object>();
		pars.put("key", key);
		
		QueryResult<Map<String, Object>> nodes = engine2.query("MATCH (n:RDA) WHERE has(n.key) and n.key={key} RETURN n", pars);
		for (Map<String, Object> row : nodes) {
			RestNode node = (RestNode) row.get("n");
			if (null != node) {
				if (null == result)
					result = new ArrayList<RestNode>();
				
				result.add(node);
			}
		}
		
		return result;
	}
	
	private List<RestNode> findDryadPublicationByDoi(final String doi) {
		List<RestNode> result = null;
		
		Map<String, Object> pars = new HashMap<String, Object>();
		pars.put("doi", doi);
		
		QueryResult<Map<String, Object>> nodes = engine2.query("MATCH (n:Dryad:Publication) WHERE has(n.doi) and any (m in n.doi WHERE m = {doi}) RETURN n", pars);
		for (Map<String, Object> row : nodes) {
			RestNode node = (RestNode) row.get("n");
			if (null != node) {
				if (null == result)
					result = new ArrayList<RestNode>();
				
				result.add(node);
			}
		}
		
		return result;
	}
	
	
	private RestNode createCrossRefPublication(final String key) {
		if (key.contains(PART_DOI)) {
			String doi = key.substring(PART_DOI.length());
			Item item = crossref.requestWork(doi);
			if (null != item) {
				Map<String, Object> props = new HashMap<String, Object>();
				addProperty(props, PROPERTY_URL, item.getUrl());
				addProperty(props, PROPERTY_PREFIX, item.getPrefix());
				addProperty(props, PROPERTY_ISSUE, item.getIssue());
				addProperty(props, PROPERTY_VOLUME, item.getVolume());
				addProperty(props, PROPERTY_TYPE, item.getType());
				addProperty(props, PROPERTY_PAGE, item.getPage());
				addProperty(props, PROPERTY_PUBLISHER, item.getPublisher());
				addProperty(props, PROPERTY_PUBLISHER, item.getPublisher());
				addProperty(props, PROPERTY_ISSN, item.getIssn());
				addProperty(props, PROPERTY_TITLE, item.getTitle());
				addProperty(props, PROPERTY_SUBTITLE, item.getSubtitle());
				addProperty(props, PROPERTY_SUBJECT, item.getSubject());
				addProperty(props, PROPERTY_CONTAINER_TITLE, item.getContainerTitle());
				addProperty(props, PROPERTY_AUTHOR, item.getAuthorString());
				addProperty(props, PROPERTY_EDITOR, item.getEditorString());
				addProperty(props, PROPERTY_ISSUED, item.getIssuedString());
				addProperty(props, PROPERTY_DEPOSITED, item.getDepositedString());
				addProperty(props, PROPERTY_INDEXED, item.getIndexedString());
				addProperty(props, PROPERTY_SCORE, String.valueOf(item.getScore()));
				addProperty(props, PROPERTY_UPDATE_POLICY, item.getUpdatePolicy());
				
				if (!props.isEmpty()) {
					RestNode node =  createNode(graphDb2, Labels.CrossRef, Labels.Publication, PROPERTY_KEY, doi, props); 
					
					if (null != item.getAuthor())
						for (Author author : item.getAuthor()) {
							RestNode nodeAuthor = createCrossRefResearcher(doi, author);
							
							createUniqueRelationship(graphDb2, nodeAuthor, node, Relationhips.author, Direction.OUTGOING, null);
						}
					
					if (null != item.getEditor())
						for (Author editor : item.getEditor()) {
							RestNode nodeAuthor = createCrossRefResearcher(doi, editor);
							
							createUniqueRelationship(graphDb2, nodeAuthor, node, Relationhips.editor, Direction.OUTGOING, null);
						}
					
					return node;
				}
			}
		}
			
		return null;
	}
	
	private RestNode createCrossRefResearcher(final String doi, Author author) {
		Map<String, Object> props = new HashMap<String, Object>();
		addProperty(props, PROPERTY_SUFFIX, author.getSuffix());
		addProperty(props, PROPERTY_GIVEN_NAME, author.getGiven());
		addProperty(props, PROPERTY_FAMILY_NAME, author.getFamily());
		addProperty(props, PROPERTY_FULL_NAME, author.getFullName());
		addProperty(props, PROPERTY_ORCID, author.getOrcid());
		
		return createNode(graphDb2, Labels.CrossRef, Labels.Researcher, PROPERTY_KEY, doi + ":" + author.getFullName(), props); 

	}
	
	private void addProperty(Map<String, Object> map, final String key, final Object value) {
		if (null != key && !key.isEmpty() && null != value) {
			if (value instanceof String && ((String) value).isEmpty())
				return;
			
			map.put(key, value);				
		}
	}
	
	private Map<String, Object> getProperties(RestNode node) {
		Iterable<String> keys = node.getPropertyKeys();
		Map<String, Object> pars = null;
		
		for (String key : keys) {
			if (null == pars)
				pars = new HashMap<String, Object>();
			
			pars.put(key, node.getProperty(key));
		}
		
		return pars;
	}
	
	private RestNode createNode(RestAPI graphDb, final Label labelSource, final Label labelType, 
			final String key, final Object value,  Map<String, Object> props) {
				
	//	System.out.println("Creating new node " +  labelSorce.name() + ":" + labelType.name() + " " + key + "=" + value + " " + props);
		
		props.put(key, value);
		props.put(PROPERTY_NODE_TYPE, labelType.name());
		props.put(PROPERTY_NODE_SOURCE, labelSource.name());
		
		RestNode node = graphDb.getOrCreateNode(getIndex(labelSource.name() + "_" + labelType.name()), key, value, props);
		
		if (!node.hasLabel(labelSource))
			node.addLabel(labelSource); 
		if (!node.hasLabel(labelType))
			node.addLabel(labelType);
		
		return node;
	}
	
	private void createUniqueRelationship(RestAPI graphDb, RestNode nodeStart, RestNode nodeEnd, 
			RelationshipType type, Direction direction, Map<String, Object> data) {

		// get all node relationships. They should be empty for a new node
		Iterable<Relationship> rels = nodeStart.getRelationships(type, direction);		
		for (Relationship rel : rels) {
			switch (direction) {
			case INCOMING:
				if (rel.getStartNode().getId() == nodeEnd.getId())
					return;
			case OUTGOING:
				if (rel.getEndNode().getId() == nodeEnd.getId())
					return;				
			case BOTH:
				if (rel.getStartNode().getId() == nodeEnd.getId() || 
				    rel.getEndNode().getId() == nodeEnd.getId())
					return;
			}
		}
		
		if (direction == Direction.INCOMING)
			graphDb.createRelationship(nodeEnd, nodeStart, type, data);
		else
			graphDb.createRelationship(nodeStart, nodeEnd, type, data);
	}	
	
	private RestIndex<Node> getIndex(String label) {
		RestIndex<Node> index = mapIndexes.get(label);
		if (null == index) 
			mapIndexes.put(label, index = graphDb2.index().forNodes(label));
		
		
		return index;
	}
	
	private void createConstraint(Label labelSource, Label labelType) {
		engine2.query("CREATE CONSTRAINT ON (n:" +  labelSource.name() + "_" + labelType.name() + ") ASSERT n." + PROPERTY_KEY + " IS UNIQUE", Collections.<String, Object> emptyMap());
	}
	
	/*
	private void createUniqueRelationship(RestNode nodeStart, RestNode nodeEnd, 
			RelationshipType type, Direction direction, Map<String, Object> data) {

		// get all node relationships. They should be empty for a new node
		Iterable<Relationship> rels = nodeStart.getRelationships(type, direction);
		for (Relationship rel : rels) {
			switch ()
			
			if (rel.getStartNode().getId() == nodeStart.getId() && 
				rel.getEndNode().getId() == nodeEnd.getId())
				return;
		}
		
		graphDb.createRelationship(nodeStart, nodeEnd, type, data);
	}	*/
}
