package org.grants.importres.search;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.grants.google.cache2.GoogleUtils;
import org.grants.google.cache2.Link;
import org.grants.graph.GraphConnection;
import org.grants.graph.GraphField;
import org.grants.graph.GraphNode;
import org.grants.graph.GraphRelationship;
import org.grants.neo4j.Neo4jUtils;
import org.grants.utils.aggrigation.AggrigationUtils;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Transaction;
import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.RestAPIFacade;
import org.neo4j.rest.graphdb.entity.RestNode;
import org.neo4j.rest.graphdb.entity.RestRelationship;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class Importer {
	private RestAPI graphDb;
	private RestCypherQueryEngine engine;
/*	private RestIndex<Node> indexWebResearcher;*/
	
	private JAXBContext jaxbContext;
	private Unmarshaller jaxbUnmarshaller;
		
	private File fieldsFolder;
	private File googleLinkFolder;
	private File googleDataFolder;
	private File googleMetadataFolder;
	private File nodesFolder;
	private File relationshipsFolder;
	
	private PrintWriter logger;
	private PrintWriter search;
	
	private int fileCounter;
	private int relCounter;
	private int fileNodeCounter;
	private int fileRelCounter;
	
/*	private enum Labels implements Label {
		Web, Researcher //, Dryad, RDA, , Pattern, Publication, Grant
	}
	
	private enum RelTypes implements RelationshipType {
		relatedTo
	}*/
	
	private static final String FOLDER_CACHE = "cache";
	private static final String FOLDER_LINK = "link";
	private static final String FOLDER_DATA = "data";
	private static final String FOLDER_METADATA = "metadata";
	private static final String FOLDER_FIELD = "field"; 
	private static final String FOLDER_NODE = "node";
	private static final String FOLDER_RELATIONSHIP = "relationship";
	
	private static final int MAX_THREADS = 100;
//	private static final int MAX_COMMANDS = 1024;
//	private static final int MAX_NODES = 51200;
	
	private List<Pattern> webPatterns;
	private Set<String> blackList;
	
	private List<GraphNode> graphNodes;
	private List<GraphRelationship> graphRelationships;
	
	/**
	 * Class Constructor
	 * 
	 * @param neo4jUrl
	 * @throws JAXBException
	 * @throws IOException 
	 */
	public Importer(String neo4jUrl, String fieldsPath, String googlePath, String cachePath, String blackList) throws JAXBException, IOException {
		System.setProperty("org.neo4j.rest.read_timeout", "600");
		
		graphDb = new RestAPIFacade(neo4jUrl);
		engine = new RestCypherQueryEngine(graphDb);  
		
		fieldsFolder = new File(fieldsPath, FOLDER_FIELD);
		googleLinkFolder = new File (googlePath, FOLDER_CACHE + "/" + FOLDER_LINK);
		googleDataFolder = new File(googlePath, FOLDER_CACHE + "/" + FOLDER_DATA);
		googleMetadataFolder = new File(googlePath, FOLDER_CACHE + "/" + FOLDER_METADATA);
		nodesFolder = new File(cachePath, FOLDER_NODE);
		relationshipsFolder = new File(cachePath, FOLDER_RELATIONSHIP);

		//Neo4jUtils.createConstraint(engine, Labels.Web, Labels.Researcher);
		//indexWebResearcher = Neo4jUtils.getIndex(graphDb, Labels.Web, Labels.Researcher);
				
		jaxbContext = JAXBContext.newInstance(Link.class);
		jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		
		logger = new PrintWriter("import_search.log", StandardCharsets.UTF_8.name());
		search = new PrintWriter("data_search.log", StandardCharsets.UTF_8.name());
		
			
		nodesFolder.mkdirs();
		relationshipsFolder.mkdirs();
		
		log("Loading black list");
		this.blackList = AggrigationUtils.loadBlackList(blackList);

		log("Loading web patterns");
		this.webPatterns = AggrigationUtils.loadWebPatterns(engine);
	}
	
	/**
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * 
	 */
	public void process() throws JsonParseException, JsonMappingException, IOException {
		Map<String, List<GraphConnection>> nodes = new HashMap<String, List<GraphConnection>>();
		
		log ("Loading Orcid Works");
		File[] fieldFiles = fieldsFolder.listFiles();
		for (File fieldFile : fieldFiles) 
			if (!fieldFile.isDirectory()) { 
				System.out.println("Processing fields: " + fieldFile.getName());
				
				GraphField[] graphfields = GoogleUtils.mapper.readValue(fieldFile, GraphField[].class);
				for (GraphField graphField : graphfields) {
					if (null != graphField) {
						Object field = graphField.getField();
						String title = ((String)field).trim().toLowerCase();
						if (title.length() > AggrigationUtils.MIN_TITLE_LENGTH 
								&& StringUtils.countMatches(title, " ") > 2 
								&& !blackList.contains(title)) {
							if (nodes.containsKey(title))
								nodes.get(title).add(graphField.getConn());
							else {
								List<GraphConnection> list = new ArrayList<GraphConnection>();
								list.add(graphField.getConn());
								
								nodes.put(title, list);
							}
						}
					}
				}
			}
		
		log("Done. " + nodes.size() + " unique works has been loaded");
		log("Processing cached pages");
		
		fileCounter = relCounter = fileNodeCounter = fileRelCounter;
		long beginTime = System.currentTimeMillis();
		
		processPages(nodes);
				
		long endTime = System.currentTimeMillis();
		
		log(String.format("Done. Processed %d pages and found %d relationships over %d ms. Average %f ms per file", 
				fileCounter, relCounter, endTime - beginTime, (float)(endTime - beginTime) / (float)fileCounter));
	
	}
	
	private void processPages(Map<String, List<GraphConnection>> nodes) {

		try {
			Semaphore semaphore = new Semaphore(MAX_THREADS);
		
			List<MatcherThread> threads = new ArrayList<MatcherThread>();
			for (int i = 0; i < MAX_THREADS; ++i) {
				MatcherThread thread = new MatcherThread(semaphore);
				thread.start();
				threads.add(thread);
			}
		
			File[] files = googleLinkFolder.listFiles();
			for (File file : files) 
				if (!file.isDirectory()) {
					Link link = (Link) jaxbUnmarshaller.unmarshal(file);
					if (link != null && AggrigationUtils.isLinkFollowAPattern(webPatterns, link.getLink())) {
						String fileName = Long.toString(fileCounter) + ".json";

						log ("Processing URL [" + fileCounter++ + "]: " + link.getLink());
						
						Matcher matcher = new Matcher(
								link.getLink(), 
								new File(googleDataFolder, link.getData()), 
								link.getMetadata() != null ? new File(googleMetadataFolder, link.getMetadata()) : null, 
								new File(nodesFolder, fileName), 
								new File(relationshipsFolder, fileName),
								nodes);
						
						semaphore.acquire(); 

						boolean matcherAssigned = false;
						for (MatcherThread thread : threads) 
							if (thread.isFree()) {
								thread.addMatcher(matcher);
								matcherAssigned = true;
								
								break;
							}								
						
						if (!matcherAssigned)
							throw new MatcherThreadException("All matcher threads are busy");
					}
				}
			
			for (MatcherThread thread : threads) {
				thread.finishCurrentAndExit();
				thread.join();
			}			
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	private void log(String message) {
		System.out.println(message);
		
		logger.println(message);  	
	}
	
	private void logSearch(String needle) {
		search.println(needle);  	
	}
}
