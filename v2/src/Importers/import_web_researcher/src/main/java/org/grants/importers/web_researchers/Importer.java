package org.grants.importers.web_researchers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.grants.google.cse.Item;
import org.grants.google.cse.Query;
import org.grants.google.cse.QueryResponse;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
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

public class Importer {
	private static final String LABEL_PUBLICATION = "Publication";
	private static final String LABEL_RESEARCHER = "Researcher";
	private static final String LABEL_PATTERN = "Pattern";
	private static final String LABEL_GRANT = "Grant";
	private static final String LABEL_DRYAD = "Dryad";
	private static final String LABEL_WEB = "Web";
	private static final String LABEL_RDA = "RDA";
	
	private static final String LABEL_WEB_RESEARCHER = LABEL_WEB + "_" + LABEL_RESEARCHER;
	
	private static final String RELATIONSHIP_RELATED_TO = "relatedTo";
		
	private static final String PROPERTY_KEY = "key"; 
	private static final String PROPERTY_NODE_SOURCE = "node_source";
	private static final String PROPERTY_NODE_TYPE = "node_type";
	private static final String PROPERTY_URL = "url";
	private static final String PROPERTY_NAME = "name";
	
	/*private static final String PROPERTY_COUNTRY = "country";
	private static final String PROPERTY_STATE = "state";
	private static final String PROPERTY_TITLE = "title";
	
	private static final String PROPERTY_HOST = "host";
	private static final String PROPERTY_PATTERN = "pattern";*/
	
	private static final String PART_DATA_FROM = "Data from: ";
	
	
	private final String dataFolder;
	
	private RestAPI graphDb;
	private RestCypherQueryEngine engine;
	
	private RestIndex<Node> indexWebResearcher;
		
	private Label labelResearcher = DynamicLabel.label(LABEL_RESEARCHER);
	private Label labelWeb = DynamicLabel.label(LABEL_WEB);
	
	private RelationshipType relRelatedTo = DynamicRelationshipType.withName(RELATIONSHIP_RELATED_TO);
	
	private List<Pattern> webPatterns = new ArrayList<Pattern>();
	private List<RdaGrant> rdaGrants = new ArrayList<RdaGrant>();
	private List<DryadPublication> dryadPublications = new ArrayList<DryadPublication>();
	
	private Map<String, Integer> grantCounter = new HashMap<String, Integer>();
	
	private JAXBContext jaxbContext;
	private Unmarshaller jaxbUnmarshaller;
	
	private Query googleQuery;// = new Query(null, null);   

	private PrintWriter logger;

	/**
	 * Class constructor. 
	 * @param neo4jUrl An URL to the Neo4J
	 * @throws JAXBException 
	 */
	public Importer(final String neo4jUrl, final String dataFolder) throws JAXBException {
		this.dataFolder = dataFolder;
		
		graphDb = new RestAPIFacade(neo4jUrl);
		engine = new RestCypherQueryEngine(graphDb);  
		
		engine.query("CREATE CONSTRAINT ON (n:" + LABEL_WEB_RESEARCHER + ") ASSERT n." + PROPERTY_KEY + " IS UNIQUE", Collections.<String, Object> emptyMap());
		indexWebResearcher = graphDb.index().forNodes(LABEL_WEB_RESEARCHER);
				
		jaxbContext = JAXBContext.newInstance(Publication.class, Page.class);
		jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		
		googleQuery = new Query(null, null); 
		googleQuery.setJsonFolder(dataFolder + "/json");
		
		try {
			logger = new PrintWriter("import_web_researcher.log", "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}		
	}
	
	private void log(String message) {
		System.out.println(message);
		
		logger.println(message);  
		logger.flush();
	}
	
	public void process() {
		loadWebPatterns();
		loadDryadPublications();
		loadRdaGrants();

		processPages();
	}
	
	private boolean isLinkFollowAPattern(String link) {
		for (Pattern pattern : webPatterns) 
			if (pattern.matcher(link).find())
				return true;
		return false;
	}
	
	/*
	private String titleToPattern(String title) {
		return title.toLowerCase().replaceAll("[^a-z0-9]", ".+");
	}*/
	
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
	
	private void loadDryadPublications() {
		log ("loaging Dryad:Publication's");
		
		QueryResult<Map<String, Object>> articles = engine.query("MATCH (n:" + LABEL_DRYAD + ":" + LABEL_PUBLICATION + ") RETURN id(n) AS id, n.title AS title", null);
		for (Map<String, Object> row : articles) {
			long nodeId = (long) (Integer) row.get("id");
			String title = (String) row.get("title");
			if (null != title) {
				if (title.contains(PART_DATA_FROM))
					title = title.substring(PART_DATA_FROM.length());
				
				dryadPublications.add(new DryadPublication(title, nodeId));			
			}
		}
	}
	
	private void loadRdaGrants() {
		log ("loaging RDA:Grant's");
		
		QueryResult<Map<String, Object>> articles = engine.query("MATCH (n:" + LABEL_RDA + ":" + LABEL_GRANT + ") RETURN id(n) AS id, n.name_primary AS name", null);
		for (Map<String, Object> row : articles) {
			long nodeId = (long) (Integer) row.get("id");
			String name = (String) row.get("name");
			if (null != name) {
				RdaGrant rdaGrant = new RdaGrant(name, nodeId);
				
				Integer counter = grantCounter.get(rdaGrant.getTitleLoverCase());
				if (counter == null)
					grantCounter.put(rdaGrant.getTitleLoverCase(), 0);
				else
					grantCounter.put(rdaGrant.getTitleLoverCase(), counter + 1);
				
				rdaGrants.add(rdaGrant);
			}
		}
	}
	
	private String getAuthor(String link, String publication) {
		QueryResponse response = googleQuery.queryCache(publication);
		
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
	
	private void processPages() {
		log ("Processing cached pages");
		
		String path = dataFolder + "/cache/page/";
			
		File[] files = new File(path).listFiles();
		for (File file : files) 
			if (!file.isDirectory())
				try {
					Page page = (Page) jaxbUnmarshaller.unmarshal(file);
					if (page != null) {
						
						if (isLinkFollowAPattern(page.getLink())) {
							log ("Found matching URL: " + page.getLink());
							
							String author = null;
							Set<String> publications = page.getData();
							if (null != publications && !publications.isEmpty())
								author = getAuthor(page.getLink(), publications.iterator().next());
							
							processPage(page.getLink(), page.getCache(), author, publications);
						}
					}							
				} catch (JAXBException e) {
					e.printStackTrace();
				}
	}
	
	private void processPage(String link, String cache, String author, Set<String> publications) {
		log ("Creating Web:Researcher: url=" + link + ", author=" + link);
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(PROPERTY_KEY, link);
		map.put(PROPERTY_NODE_SOURCE, LABEL_WEB);
		map.put(PROPERTY_NODE_TYPE, LABEL_RESEARCHER);
		map.put(PROPERTY_URL, link);
		if (null != author)
			map.put(PROPERTY_NAME, author);
		
		RestNode nodeResearcher = graphDb.getOrCreateNode(indexWebResearcher, 
				PROPERTY_KEY, link, map);
		if (!nodeResearcher.hasLabel(labelResearcher))
			nodeResearcher.addLabel(labelResearcher); 
		if (!nodeResearcher.hasLabel(labelWeb))
			nodeResearcher.addLabel(labelWeb);	
		
		for (DryadPublication publication : dryadPublications) 
			if (publications.contains(publication.getTitle())) {
				log ("Creating relationsip to Dryad:Publication: node_id=" + publication.getNodeId() + ", title=" + publication.getTitle());
				
				RestNode nodePublication = graphDb.getNodeById(publication.getNodeId());
				
				createUniqueRelationship(graphDb, nodePublication, nodeResearcher, 
						relRelatedTo, Direction.OUTGOING, null);
			}
		
		File cacheFile = new File(cache);
		if (cacheFile.exists() && !cacheFile.isDirectory()) {
			try {
				String cacheData = FileUtils.readFileToString(cacheFile).toLowerCase();
				for (RdaGrant grant : rdaGrants) 
					if (grantCounter.get(grant.getTitleLoverCase()).intValue() == 0)
						if (grant.getPattern().matcher(cacheData).find()) {
							log ("Creating relationsip to Rda:Grant: node_id=" + grant.getNodeId() + ", title=" + grant.getTitle());
							
							RestNode nodeGrant = graphDb.getNodeById(grant.getNodeId());
							
							createUniqueRelationship(graphDb, nodeGrant, nodeResearcher, 
									relRelatedTo, Direction.OUTGOING, null);
						}	
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private static void createUniqueRelationship(RestAPI graphDb, RestNode nodeStart, RestNode nodeEnd, 
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
 }
