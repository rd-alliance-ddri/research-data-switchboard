package org.grants.importers.publications;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.RestAPIFacade;
import org.neo4j.rest.graphdb.entity.RestNode;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;
import org.neo4j.rest.graphdb.util.QueryResult;

public class Importer {
	private static final String NEO4J_URL = "http://ec2-54-69-203-235.us-west-2.compute.amazonaws.com:7476/db/data/";//"http://localhost:7474/db/data/";

	private static final String PART_DATA_FROM = "data from: ";
	
	private Map<String, Page> pages = new HashMap<String, Page>();
	private List<Publication> publications = null;
	private Map<String, Grant> grants = new HashMap<String, Grant>();
	
	private JAXBContext jaxbContext;
	private Unmarshaller jaxbUnmarshaller;
	
	private RestAPI graphDb;
	private RestCypherQueryEngine engine;

	public Importer() throws JAXBException {
		jaxbContext = JAXBContext.newInstance(Publication.class, Grant.class, Page.class);
		jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		
		graphDb = new RestAPIFacade(NEO4J_URL); //"http://localhost:7474/db/data/");  
		engine = new RestCypherQueryEngine(graphDb);  		
	}
	
	public void loadPages(final String folder) {
		File[] files = new File(folder).listFiles();
		for (File file : files) 
			if (!file.isDirectory())
			{
				try {
					Page page = (Page) jaxbUnmarshaller.unmarshal(file);
					if (page != null) {
						page.setSelf(file.getPath());
						pages.put(page.getLink(), page);
					}
				} catch (JAXBException e) {
					e.printStackTrace();
				}
			}		
	}
	
	/*
	public void loadPublications(final String folder) {
		File[] files = new File(folder).listFiles();
		for (File file : files) 
			if (!file.isDirectory())
			{
				try {
					Publication publication = (Publication) jaxbUnmarshaller.unmarshal(file);
					if (publication != null) {
						publication.setSelf(file.getPath());
						publications.put(publication.getTitle(), publication);
					}
				} catch (JAXBException e) {
					e.printStackTrace();
				}
			}		
	}
	
	public void loadGrants(final String folder) {
		File[] files = new File(folder).listFiles();
		for (File file : files) 
			if (!file.isDirectory())
			{
				try {
					Grant grant = (Grant) jaxbUnmarshaller.unmarshal(file);
					if (grant != null) {
						grant.setSelf(file.getPath());
						grants.put(grant.getName(), grant);
					}
				} catch (JAXBException e) {
					e.printStackTrace();
				}
			}		
	}
	*/
	
	public void importData() {
		
		// Load data
		loadDryadPublications();
		loadPages("publications/cache/page");
		identifyRelatedPublications();
	}
	
	public void loadDryadPublications() {
		
		loadPublications("publications.dat");
		if (null != publications)
			return;
		
		publications = new ArrayList<Publication>();
		
		QueryResult<Map<String, Object>> nodes = engine.query("MATCH (n:Dryad:Publication) RETURN n", null);
		for (Map<String, Object> row : nodes) {
			RestNode dryadPublication = (RestNode) row.get("n");
			if (null != dryadPublication) {
				try {
					Publication publication = new Publication();
					publication.setTitle(getStringProperty(dryadPublication, "title"));
					publication.setNodeId(dryadPublication.getId());
					
					String[] authors = getArrayProperty(dryadPublication, "author");
					publication.setAuthors(authors);
					
					RestNode crossrefPublication = getRelatedNode(dryadPublication, DynamicRelationshipType.withName("knownAs"), Direction.BOTH);
					if (null != crossrefPublication) {
						
						publication.setCrossrefTitle(getStringProperty(crossrefPublication, "title"));
						
						Collection<RestNode> crossrefResearchers = getRelatedNodes(crossrefPublication, DynamicRelationshipType.withName("author"), Direction.INCOMING);
						for (RestNode crossrefResearcher : crossrefResearchers) {
							String given = getStringProperty(crossrefResearcher, "given_name");
							String family = getStringProperty(crossrefResearcher, "family_name");
							String full = findNameFromArray(authors, given, family);
							
							Researcher researcher = new Researcher();
							researcher.setNodeId(crossrefResearcher.getId());
							researcher.setGivenAndFamily(given, family);
							if (null != full)
								researcher.setFull(full);
							
							publication.addResearcher(researcher);						
						}
					}
	
		//			System.out.println(publication);				
					publications.add(publication);
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}		
		}
		
		savePublications("publications.dat");
	}
	
	public void identifyRelatedPublications() {
		for (Page page : pages.values()) {
			try {
				System.out.println("Processing: " + page.getLink());
				
				byte[]  encoded = Files.readAllBytes(Paths.get(page.getCache()));
				String data = new String(encoded, "UTF-8");
				data =  StringUtils.stripAccents(data).toLowerCase();
				
			
				for (Publication publication : publications) {
					String title = StringUtils.stripAccents(publication.getTitle()).toLowerCase();
					if (title.startsWith(PART_DATA_FROM))
						title = title.substring(PART_DATA_FROM.length());
					
					if (data.contains(title)) {
						System.out.println("Found publication: " + title);
						
						for (Researcher researcher : publication.getResearchers()) {
							for (String name : researcher.getNames()) {
								String _name = StringUtils.stripAccents(name).toLowerCase();
								if (data.contains(_name)) {
									System.out.println("Found author: " + _name);
								}
							}
						}
					}
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	private String findNameFromArray(String[] names, String given, String family) {
		if (null == names)
			return null; // nothing to search
		
		String _given = StringUtils.stripAccents(given).toLowerCase();
		String _family = StringUtils.stripAccents(family).toLowerCase();
		String full = null;
		
		if (_given.contains("."))
			_given = _given.replace(".", ".+");
		if (_family.contains("."))
			_family = _family.replace(".", ".+");
		
		for (String name : names) {
			String _name = StringUtils.stripAccents(name).toLowerCase();
			if (_name.contains(",")) {
				String[] parts = _name.split(", ");
				if (parts.length == 2)
					_name = parts[1] + " " + parts[0];
			}
			
			if (_name.matches(_given + ".*" + _family) || _name.matches(_family + ".*" + _given))
				if (null == full)
					full = name;
				else {
					System.out.println("Duplicate name has been found for author " + given + " " + family + ": " + full + ", " + name);
					return null;
				}
		}
		
		if (null == full && _family.length() > 2) {
			for (String name : names) {
				String _name = StringUtils.stripAccents(name).toLowerCase();
				if (_name.contains(_family))
					if (null == full)
						full = name;
					else {
						System.out.println("Duplicate name has been found for author " + given + " " + family + ": " + full + ", " + name);
						return null;
					}
			}			
		}
		
		if (null == full && _given.length() > 2) {
			for (String name : names) {
				String _name = StringUtils.stripAccents(name).toLowerCase();
				if (_name.contains(_given))
					if (null == full)
						full = name;
					else {
						System.out.println("Duplicate name has been found for author " + given + " " + family + ": " + full + ", " + name);
						return null;
					}
			}			
		}
		
		if (null == full)
			System.out.println("Unable to found any matches for author " + given + " " + family);
		return full;		
	}
	
	private String getStringProperty(RestNode node, String key) {
		try {
			Object value = node.getProperty(key);
			if (value instanceof String) 
				return (String) value;
			if (value instanceof String[]) {
				String[] array = (String[]) value;
				if (array.length > 0)
					return array[0];			
				return null;
			}
			if (value instanceof List<?>) {
				List<?> list = (List<?>)value;
				if (list.size() > 0) {
					Object item = list.get(0);
					if (item instanceof String)
						return (String) item;
				}
				return null;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}		
		
		return null;
	}
	
	private String[] getArrayProperty(RestNode node, String key) {
		try {
			Object value = node.getProperty(key);
			if (value instanceof String[]) 
				return (String[]) value;
			if (value instanceof List<?>) {
				List<?> list = (List<?>)value;
				if (list.size() > 0) {
					Object item = list.get(0);
					if (item instanceof String)
						return list.toArray(new String[0]);		
				}
				return null;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}		
		
		return null;
	}
	
	
	@SuppressWarnings("unchecked")
	private Set<String> getSetProperty(RestNode node, String key) {
		Object value = node.getProperty(key);
		if (value instanceof String[]) 
			return new HashSet<String>(Arrays.asList((String[]) value));
		if (value instanceof List<?>) {
			List<?> list = (List<?>)value;
			if (list.size() > 0) {
				Object item = list.get(0);
				if (item instanceof String)
					return new HashSet<String>((List<String>) list);
			}
			return null;
		}
		
		return null;
	}
	
	private RestNode getRelatedNode(RestNode node, RelationshipType relationship, Direction direction) {
		Iterable<Relationship> relations = node.getRelationships(direction, relationship);
		if (relations != null) 
			for (Relationship relation : relations) {
				switch (direction) {
				case INCOMING:
					return (RestNode) relation.getStartNode();
				case OUTGOING:
					return (RestNode) relation.getEndNode();
				case BOTH:
					{
						long nodeId = node.getId();
						RestNode startNode = (RestNode) relation.getStartNode();
						if (startNode.getId() != nodeId)
							return startNode;
						RestNode endNode = (RestNode) relation.getEndNode();
						if (endNode.getId() != nodeId)
							return endNode;						
						return null;
					}
				}
			}
		
		return null;
	}
	
	private Collection<RestNode> getRelatedNodes(RestNode node, RelationshipType relationship, Direction direction) {
		Iterable<Relationship> relations = node.getRelationships(direction, relationship);
		List<RestNode> nodes = new ArrayList<RestNode>();
		if (relations != null) 
			for (Relationship relation : relations) {
				switch (direction) {
				case INCOMING:
					nodes.add((RestNode) relation.getStartNode());
					break;
					
				case OUTGOING:
					nodes.add((RestNode) relation.getEndNode());
					break;
				case BOTH:
					{
						long nodeId = node.getId();
						RestNode startNode = (RestNode) relation.getStartNode();
						if (startNode.getId() != nodeId)
							nodes.add(startNode);
						RestNode endNode = (RestNode) relation.getEndNode();
						if (endNode.getId() != nodeId)
							nodes.add(endNode);						
					}
					break;
				}
			}
		
		return nodes;
	}
	
	private void savePublications(String filename) {
		try {
			FileOutputStream f = new FileOutputStream(filename);
			ObjectOutputStream out = new ObjectOutputStream(f);
			try {
				for (Publication publication : publications) {
				    out.writeObject(publication);
				}
			} finally {
				out.close();
				f.close();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void loadPublications(String filename) {
		publications = new ArrayList<Publication>();
		
		try {
			FileInputStream f = new FileInputStream(filename);
			ObjectInputStream in = new ObjectInputStream(f);
			Publication publication = null;
			try {
				do {
					if ((publication = (Publication) in.readObject()) != null) 
						publications.add(publication);
					
				} while (publication != null);
			} finally {
				in.close();
				f.close();
			}
		} catch (FileNotFoundException e) { 
			publications = null;
		} catch (EOFException e) {
			
		} catch (Exception e) {
			e.printStackTrace();
			
			publications = null;
		}		
	}
}
 