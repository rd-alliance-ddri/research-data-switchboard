package org.grants.importers.google;

/**
 * 
 * @author dima
 *
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.grants.google.cache.Grant;
import org.grants.google.cache.Page;
import org.grants.google.cache.Publication;
import org.grants.google.cse.Item;
import org.grants.google.cse.Query;
import org.grants.google.cse.QueryResponse;
import org.grants.neo4j.Neo4jUtils;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.RestAPIFacade;
import org.neo4j.rest.graphdb.entity.RestNode;
import org.neo4j.rest.graphdb.index.RestIndex;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;
import org.neo4j.rest.graphdb.util.QueryResult;

public class Importer {

	private RestAPI graphDb;
	private RestCypherQueryEngine engine;
	private RestIndex<Node> indexWebResearcher;
	
	private JAXBContext jaxbContext;
	private Unmarshaller jaxbUnmarshaller;
	
	private Query googleQuery;
	
	private PrintWriter logger;
	private PrintWriter grants;
	private PrintWriter publications;
	
	private enum Labels implements Label {
		Web, Dryad, RDA, Researcher, Pattern, Publication, Grant
	}
	
	private enum RelTypes implements RelationshipType {
		relatedTo
	}
	
	private static final String LABEL_WEB = Labels.Web.name();
	private static final String LABEL_PATTERN = Labels.Pattern.name();
	private static final String LABEL_DRYAD = Labels.Dryad.name();
	private static final String LABEL_PUBLICATION = Labels.Publication.name();
	private static final String LABEL_RDA = Labels.RDA.name();
	private static final String LABEL_GRANT = Labels.Grant.name();
	private static final String LABEL_RESEARCHER = Labels.Researcher.name();
	
	private static final String FOLDER_CACHE = "cache";
	private static final String FOLDER_PUBLICATION = "publication";
	private static final String FOLDER_GRANT = "grant";
	private static final String FOLDER_JSON = "json";
	
	private static final String PROPERTY_URL = "url";
	private static final String PROPERTY_NAME = "name";
	private static final String PROPERTY_SOURCE_GOOGLE = "source_google";
	
	private static final boolean VALUE_TRUE = true;
	
	private static final String TYPE_TITLE = "title";
//	private static final String TYPE_SIMPLEFIED = "simplefied";
	private static final String TYPE_SCIENTIFIC = "scientific";
	private static final String TYPE_SIMPLIFIED = null;
	
	private static final String PART_DATA_FROM = "data from: ";
	
	private List<Pattern> webPatterns = new ArrayList<Pattern>();
	private Set<String> blackList = new HashSet<String>();
	
	public Importer(String neo4jUrl) throws FileNotFoundException, UnsupportedEncodingException, JAXBException {
		graphDb = new RestAPIFacade(neo4jUrl);
		engine = new RestCypherQueryEngine(graphDb);  
		
		Neo4jUtils.createConstraint(engine, Labels.Web, Labels.Researcher);
		indexWebResearcher = Neo4jUtils.getIndex(graphDb, Labels.Web, Labels.Researcher);
				
		jaxbContext = JAXBContext.newInstance(Publication.class, Grant.class, Page.class);
		jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		
		googleQuery = new Query(null, null); 
//		//googleQuery.setJsonFolder(dataFolder + "/json");
		
/*		// Do not need to setup query engine, the data must came only from cache at this stage
		googleQuery = new Query(null, null); 
		//googleQuery.setJsonFolder(dataFolder + "/json");
		*/	
		logger = new PrintWriter("import_google.log", StandardCharsets.UTF_8.name());
		grants  = new PrintWriter("google_grants.log", StandardCharsets.UTF_8.name());
		publications  = new PrintWriter("google_publications.log", StandardCharsets.UTF_8.name());
	}
	
	/**
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * 
	 */
	public void init(String blackList) throws FileNotFoundException, IOException {
		loadBlackList(blackList);
		loadWebPatterns();
	}
	
	/**
	 * 
	 * @param googleCache
	 */
	public void processPublications(String googleCache) {
		Map<String, LinkingNode> dryadPublications = loadDryadPublications();
		processPublications(dryadPublications, googleCache);
	}
	
	/**
	 * 
	 * @param googleCache
	 */
	public void processGrants(String googleCache) {
		Map<String, LinkingNode> rdaGrants = loadRdaGrants();
		processGrants(rdaGrants, googleCache);
	}
	
	private void loadBlackList(String blackList) throws FileNotFoundException, IOException {
		try(BufferedReader br = new BufferedReader(new FileReader(new File(blackList)))) {
		    for(String line; (line = br.readLine()) != null; ) 
		    	this.blackList.add(line.trim().toLowerCase());
		}
	}
	
	private void loadWebPatterns() {
		log ("loaging Web:Pattern's");
		
		QueryResult<Map<String, Object>> articles = engine.query("MATCH (n:" + LABEL_WEB + ":" + LABEL_PATTERN + ") RETURN n.pattern as pattern", null);
		for (Map<String, Object> row : articles) {
			String pattern = (String) row.get("pattern");
			if (null != pattern) {
				log("Add Pattern: " + pattern);  
				webPatterns.add(Pattern.compile(pattern));			
			}
		}
	}
	
	private Map<String, LinkingNode> loadDryadPublications() {
		log ("loaging Dryad:Publication's");
		
		Map<String, LinkingNode> dryadPublications = new HashMap<String, LinkingNode>();
		
		QueryResult<Map<String, Object>> articles = engine.query("MATCH (n:" + LABEL_DRYAD + ":" + LABEL_PUBLICATION + ") RETURN id(n) AS id, n.title AS title", null);
		for (Map<String, Object> row : articles) {
			long nodeId = (long) (Integer) row.get("id");
			String title = ((String) row.get("title")).trim().toLowerCase();
			if (null != title) {
				if (title.contains(PART_DATA_FROM))
					title = title.substring(PART_DATA_FROM.length());
				if (!blackList.contains(title)) {
					if (dryadPublications.containsKey(title)) {
						log ("Found duplicated page name: " + title);
						dryadPublications.get(title).addNodeId(nodeId);
					} else
						dryadPublications.put(title, new LinkingNode(nodeId, TYPE_TITLE));
				}
			}
		}
		
		return dryadPublications;
	}
	
	private Map<String, LinkingNode> loadRdaGrants() {
		log ("loaging RDA:Grant's");
		
		// We only interesting in Grants, that has PURL
		Map<String, LinkingNode> rdaGrants = new HashMap<String, LinkingNode>();

		
		QueryResult<Map<String, Object>> articles = engine.query("MATCH (n:" + LABEL_RDA + ":" + LABEL_GRANT + ") WHERE has (n.identifier_purl) RETURN id(n) AS id, n.name_primary AS primary, n.name_alternative AS alternative", null);
		for (Map<String, Object> row : articles) {
			long nodeId = (long) (Integer) row.get("id");
			String primary = ((String) row.get("primary")).trim().toLowerCase();
			String alternative = ((String) row.get("alternative")).trim().toLowerCase();
			if (null != primary && !blackList.contains(primary)) {
				if (rdaGrants.containsKey(primary)) {
					log ("Found duplicated grant name: " + primary);
					rdaGrants.get(primary).addNodeId(nodeId);
				} else
					rdaGrants.put(primary, new LinkingNode(nodeId, TYPE_SIMPLIFIED));	
			}
			if (null != alternative && !alternative.equals(primary) && !blackList.contains(alternative)) {
				if (rdaGrants.containsKey(alternative)) {
					log ("Found duplicated grant name: " + alternative);
					rdaGrants.get(alternative).addNodeId(nodeId);
				} else
					rdaGrants.put(alternative, new LinkingNode(nodeId, TYPE_SCIENTIFIC));	
			}
		}
		
		return rdaGrants;
	}
	
	private void processPublications(Map<String, LinkingNode> dryadPublications, String googleCache) {
		log ("Processing cached publications");
		
		googleQuery.setJsonFolder(new File(googleCache, FOLDER_CACHE + "/" + FOLDER_JSON).getPath());
		
		Map<String, Object> pars = new HashMap<String, Object>();
		pars.put(PROPERTY_SOURCE_GOOGLE, VALUE_TRUE);
	
		File publicationCache = new File (googleCache, FOLDER_CACHE + "/" + FOLDER_PUBLICATION);
		File[] files = publicationCache.listFiles();
		for (File file : files) 
			if (!file.isDirectory())
				try {
					Publication publication = (Publication) jaxbUnmarshaller.unmarshal(file);
					if (publication != null) {
						LinkingNode dryadPublication = dryadPublications.get(publication.getTitle().trim().toLowerCase());
						if (null != dryadPublication) 
							for (String link : publication.getLinks()) 
								if (isLinkFollowAPattern(link)) {
									log ("Found matching URL: " + link + " for publication: " + publication.getTitle());
									logPublication(publication.getTitle());
									
									RestNode nodeResearcher = getOrCreateWebResearcher(link, publication.getTitle());
									for (Long nodeId : dryadPublication.getNodesId()) {
										RestNode nodePublication = graphDb.getNodeById(nodeId);
									
										Neo4jUtils.createUniqueRelationship(graphDb, nodePublication, nodeResearcher, 
												RelTypes.relatedTo, Direction.OUTGOING, pars);
									}
								}
					}							
				} catch (JAXBException e) {
					e.printStackTrace();
				}
		
	}	
	
	private void processGrants(Map<String, LinkingNode> rdaGrants, String googleCache) {
		log ("Processing cached grants");
		
		googleQuery.setJsonFolder(new File(googleCache, FOLDER_CACHE + "/" + FOLDER_JSON).getPath());
		
		Map<String, Object> pars = new HashMap<String, Object>();
		pars.put(PROPERTY_SOURCE_GOOGLE, VALUE_TRUE);
		
		File grantCache = new File (googleCache, FOLDER_CACHE + "/" + FOLDER_GRANT);
		File[] files = grantCache.listFiles();
		for (File file : files) 
			if (!file.isDirectory())
				try {
					Grant grant = (Grant) jaxbUnmarshaller.unmarshal(file);
					if (grant != null) {
						LinkingNode rdaGrant = rdaGrants.get(grant.getName().trim().toLowerCase());
						if (null != rdaGrant) 
							for (String link : grant.getLinks()) 
								if (isLinkFollowAPattern(link)) {
									log ("Found matching URL: " + link + " for grant: " + grant.getName());
									logGrant(grant.getName());
									
									RestNode nodeResearcher = getOrCreateWebResearcher(link, grant.getName());
									for (Long nodeId : rdaGrant.getNodesId()) {
										RestNode nodeGrant = graphDb.getNodeById(nodeId);
										
										Neo4jUtils.createUniqueRelationship(graphDb, nodeGrant, nodeResearcher, 
												RelTypes.relatedTo, Direction.OUTGOING, pars);	
									}
								}
					}							
				} catch (JAXBException e) {
					e.printStackTrace();
				}
		
	}	
	
	private RestNode findWebResearcher(String link) {
		IndexHits<Node> hits = indexWebResearcher.get(Neo4jUtils.PROPERTY_KEY, link);
		if (null != hits && hits.hasNext())
			return (RestNode) hits.getSingle();
		
		return null;
	}
	
	
	private RestNode getOrCreateWebResearcher(String link, String searchString) {
		RestNode node = findWebResearcher(link);
		if (null != node)
			return node;
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(Neo4jUtils.PROPERTY_KEY, link);
		map.put(Neo4jUtils.PROPERTY_NODE_SOURCE, LABEL_WEB);
		map.put(Neo4jUtils.PROPERTY_NODE_TYPE, LABEL_RESEARCHER);
		map.put(PROPERTY_URL, link);
		
		String author = getAuthor(link, searchString);
		if (null != author)
			map.put(PROPERTY_NAME, author);
		
		node = graphDb.createNode(map);
		
		if (!node.hasLabel(Labels.Researcher))
			node.addLabel(Labels.Researcher); 
		if (!node.hasLabel(Labels.Web))
			node.addLabel(Labels.Web);	
	
		indexWebResearcher.add(node, Neo4jUtils.PROPERTY_KEY, link);	
			
		return node;
	}
	
	private String getAuthor(String link, String searchString) {
		QueryResponse response = googleQuery.queryCache(searchString);
		
		String author = null;
		
		if (null != response) 
			for (Item item : response.getItems()) 
				if (item.getLink().equals(link)) {
					 Map<String, Object> pagemap = item.getPagemap();
					 if (null != pagemap) {
						 @SuppressWarnings("unchecked")
						 List<Object> metatags = (List<Object>) pagemap.get("metatags");
						 if (null != metatags && metatags.size() > 0) {
							 @SuppressWarnings("unchecked")
							 Map<String, Object> metatag = (Map<String, Object>) metatags.get(0);
							 if (null != metatag) {
								 String dcTitle = (String) metatag.get("dc.title");
								 String citationAuthor = (String) metatag.get("citation_author");
								 
								 if (null != citationAuthor) {
									 author = citationAuthor;
									 
									 log("Found citation_author: " + citationAuthor);	
								 }
								 
								 if (null != dcTitle) {
									 author = dcTitle;
									 
									 log("Found dc.title: " + dcTitle);	
								 } 
								 	
								 if (null == author) {
									 log("Unable to find author information in metatag");																 
								 }
							 }
						 }
					 }
					 
					 break;
				}
			
		return author;
	}
	
	
	private boolean isLinkFollowAPattern(String link) {
		for (Pattern pattern : webPatterns) 
			if (pattern.matcher(link).find())
				return true;
		return false;
	}
	
	/**
	 * Service log fiunction
	 * @param message
	 */
	private void log(String message) {
		System.out.println(message);
		
		logger.println(message);  	
	}
	
	private void logGrant(String grant) {
		grants.println(grant);  	
	}
	
	private void logPublication(String publication) {
		publications.println(publication);  	
	}
	
	/**
	 * Servise flush log function
	 */
	/*private void flushLog() {
		logger.flush();
	}*/
}
