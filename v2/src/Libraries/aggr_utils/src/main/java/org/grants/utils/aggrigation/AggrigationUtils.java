package org.grants.utils.aggrigation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.grants.neo4j.Neo4jUtils;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.entity.RestNode;
import org.neo4j.rest.graphdb.index.RestIndex;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;
import org.neo4j.rest.graphdb.util.QueryResult;

/**
 * 
 * @author dima
 * VERSIONS
 * 		1.0.1: added loadOrcidWorks function
 */
public class AggrigationUtils {
	
	public enum Labels implements Label {
		Web, Dryad, RDA, Orcid,
		Pattern,
		Researcher, Grant, Publication, Work
	}
	
	public enum RelTypes implements RelationshipType {
		relatedTo
	}
	
	public static final String LABEL_WEB = Labels.Web.name();
	public static final String LABEL_DRYAD = Labels.Dryad.name();
	public static final String LABEL_RDA = Labels.RDA.name();
	public static final String LABEL_ORCID = Labels.Orcid.name();
	public static final String LABEL_PATTERN = Labels.Pattern.name();
	public static final String LABEL_GRANT = Labels.Grant.name();
	public static final String LABEL_PUBLICATION = Labels.Publication.name();
	public static final String LABEL_RESEARCHER = Labels.Researcher.name();
	public static final String LABEL_WORK = Labels.Work.name();
	
	public static final String RELATIONSHIP_RELATED_TO = RelTypes.relatedTo.name();
	
	public static final String PROPERTY_URL = "url";
	public static final String PROPERTY_NAME = "name";
	
	public static final int MIN_TITLE_LENGTH = 20;
	public static final int MIN_DISTANCE = 1;
	public static final double TITLE_DISTANCE = 0.05;	
	
	private static final String PART_DATA_FROM = "data from: ";
	
	public static Set<String> loadBlackList(String blackList) throws FileNotFoundException, IOException {
		Set<String> list = new HashSet<String>();
		
		try(BufferedReader br = new BufferedReader(new FileReader(new File(blackList)))) {
		    for(String line; (line = br.readLine()) != null; ) 
		    	list.add(line.trim().toLowerCase());
		}
		
		return list;
	}
	
	public static List<Pattern> loadWebPatterns(RestCypherQueryEngine engine) {
		List<Pattern> webPatterns = new ArrayList<Pattern>();
				
		QueryResult<Map<String, Object>> articles = engine.query("MATCH (n:" + LABEL_WEB + ":" + LABEL_PATTERN + ") RETURN n.pattern as pattern", null);
		for (Map<String, Object> row : articles) {
			String pattern = (String) row.get("pattern");
			if (null != pattern) {
				webPatterns.add(Pattern.compile(pattern));			
			}
		}
		
		return webPatterns;
	}
	
	public static Map<String, Set<Long>> loadDryadPublications(RestCypherQueryEngine engine, Map<String, Set<Long>> nodes, Set<String> blackList) {
		if (null == nodes)
			nodes = new HashMap<String, Set<Long>>(); 
		
		QueryResult<Map<String, Object>> articles = engine.query("MATCH (n:" + LABEL_DRYAD + ":" + LABEL_PUBLICATION + ") RETURN id(n) AS id, n.title AS title", null);
		for (Map<String, Object> row : articles) {
			long nodeId = (long) (Integer) row.get("id");
			String title = ((String) row.get("title"));
			if (null != title) {
				title = title.trim().toLowerCase();
				if (title.contains(PART_DATA_FROM))
					title = title.substring(PART_DATA_FROM.length());
				if (title.length() > MIN_TITLE_LENGTH 
						&& !blackList.contains(title)) 
					putUnique(nodes, title, nodeId);
			}
		}
		
		return nodes;
	}
	
	public static int loadOrcidWorks(RestCypherQueryEngine engine, Map<String, Set<Long>> nodes, Set<String> blackList, int skip, int limit) {
		String cypher = "MATCH (n:" + LABEL_ORCID + ":" + LABEL_WORK + ") RETURN id(n) AS id, n.title AS title";
		if (skip > 0)
			cypher += " SKIP " + skip;
		if (limit > 0)
			cypher += " LIMIT " + limit;
		
		int counter = 0;
		
		QueryResult<Map<String, Object>> articles = engine.query(cypher, null);
		for (Map<String, Object> row : articles) {
			long nodeId = (long) (Integer) row.get("id");
			String title = ((String) row.get("title"));
			if (null != title) {
				title = title.trim().toLowerCase();
				if (title.length() > MIN_TITLE_LENGTH 
						&& !blackList.contains(title)) 
					putUnique(nodes, title, nodeId);
			}
			
			++counter;
		}
		
		return counter;
	}
	
	public static Map<String, Set<Long>> loadRdaGrants(RestCypherQueryEngine engine, Map<String, Set<Long>> nodes, Set<String> blackList) {
		if (null == nodes)
			nodes = new HashMap<String, Set<Long>>(); 
		
		QueryResult<Map<String, Object>> articles = engine.query("MATCH (n:" + LABEL_RDA + ":" + LABEL_GRANT + ") WHERE has (n.identifier_purl) RETURN id(n) AS id, n.name_primary AS primary, n.name_alternative AS alternative", null);
		for (Map<String, Object> row : articles) {
			long nodeId = (long) (Integer) row.get("id");
			String primary = ((String) row.get("primary"));
			String alternative = ((String) row.get("alternative"));
			
			if (null != primary) {
				primary = primary.trim().toLowerCase();
				if (primary.length() > MIN_TITLE_LENGTH
						&& !blackList.contains(primary)) 
					putUnique(nodes, primary, nodeId);
			}
			if (null != alternative) {
				alternative = alternative.trim().toLowerCase();
				if (alternative.length() > MIN_TITLE_LENGTH
						&& !alternative.equals(primary) 
						&& !blackList.contains(alternative)) 
					putUnique(nodes, alternative, nodeId);
			}
		}
		
		return nodes;
	}
	
	public static void putUnique(Map<String, Set<Long>> nodes, String key, Long id) {
		if (nodes.containsKey(key))
			nodes.get(key).add(id);
		else {
			Set<Long> set = new HashSet<Long>();
			set.add(id);
			
			nodes.put(key, set);
		}
	}
	
	public static boolean isLinkFollowAPattern(List<Pattern> webPatterns, String link) {
		for (Pattern pattern : webPatterns) 
			if (pattern.matcher(link).find())
				return true;
		return false;
	}
	
	public static int getDistance(int length) {
		int distance = (int) ((double) length * TITLE_DISTANCE + 0.5);
		return distance < MIN_DISTANCE ? MIN_DISTANCE : distance;			
	}
	
	public static RestNode findWebResearcher(RestIndex<Node> indexWebResearcher, String link) {
		IndexHits<Node> hits = indexWebResearcher.get(Neo4jUtils.PROPERTY_KEY, link);
		if (null != hits && hits.hasNext())
			return (RestNode) hits.getSingle();
		
		return null;
	}	
	
	public static RestNode createWebResearcher(RestAPI graphDb, RestIndex<Node> indexWebResearcher, 
			String link, String author) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(Neo4jUtils.PROPERTY_KEY, link);
		map.put(Neo4jUtils.PROPERTY_NODE_SOURCE, AggrigationUtils.LABEL_WEB);
		map.put(Neo4jUtils.PROPERTY_NODE_TYPE, AggrigationUtils.LABEL_RESEARCHER);
		map.put(PROPERTY_URL, link);
			
		if (null != author)
			map.put(PROPERTY_NAME, author);
			
		RestNode node = graphDb.createNode(map);
			
		if (!node.hasLabel(Labels.Researcher))
			node.addLabel(Labels.Researcher); 
		if (!node.hasLabel(Labels.Web))
			node.addLabel(Labels.Web);	
	
		indexWebResearcher.add(node, Neo4jUtils.PROPERTY_KEY, link);	
				
		return node;
	}
	
	/*	private Map<String, Page> loadPages(String googleCache) {
		 Map<String, Page> pages = new HashMap<String, Page>();
		
		File pageCache = new File (googleCache, FOLDER_CACHE + "/" + FOLDER_PAGE);
		File[] files = pageCache.listFiles();
		for (File file : files) 
			if (!file.isDirectory())
				try {
					Page page = (Page) jaxbUnmarshaller.unmarshal(file);
					
					pages.put(page.getLink(), page);
				} catch (JAXBException e) {
					e.printStackTrace();
				}
		
		return pages;
		
	}*/
}
