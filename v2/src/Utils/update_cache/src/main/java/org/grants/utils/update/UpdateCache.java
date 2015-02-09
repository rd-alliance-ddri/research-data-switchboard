package org.grants.utils.update;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.grants.google.cache.Grant;
import org.grants.google.cache.Page;
import org.grants.google.cache.Publication;
import org.grants.google.cache2.Link;
import org.grants.google.cache2.Result;
import org.grants.google.cse.Item;
import org.grants.google.cse.Query;
import org.grants.google.cse.QueryResponse;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UpdateCache {

	private static final String FOLDER_CACHE = "cache";
	private static final String FOLDER_PAGE = "page";
	private static final String FOLDER_DATA = "data";
	private static final String FOLDER_LINK = "link";
	private static final String FOLDER_METADATA = "metadata";
	private static final String FOLDER_JSON = "json";
	private static final String FOLDER_GRANT = "grant";
	private static final String FOLDER_GRANTS = "grants";
	private static final String FOLDER_PUBLICATION = "publication";
	private static final String FOLDER_PUBLICATIONS = "publications";
	private static final String FOLDER_RESULT = "result";
	
	private static final String EXTENSION_XML = "xml";
	private static final String EXTENSION_JSON = "json";
	
	private JAXBContext jaxbContext;
	private Marshaller jaxbMarshaller;
	private Unmarshaller jaxbUnmarshaller;
	
	private ObjectMapper mapper; 
	
	private Map<String, Link> links = new HashMap<String, Link>();
	
	private File sourceFolder;
	private File targetFolder;
	
	private Query googleQuery;
	
	public UpdateCache(String sourcePath, String targetPath) throws JAXBException {
		jaxbContext = JAXBContext.newInstance(Publication.class, Grant.class, Page.class, Link.class, Result.class);
		jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		
		mapper = new ObjectMapper(); 
		
		sourceFolder = new File(sourcePath);
		targetFolder = new File(targetPath);
		
		new File(targetFolder, FOLDER_CACHE + "/" + FOLDER_LINK).mkdirs();
		new File(targetFolder, FOLDER_CACHE + "/" + FOLDER_DATA).mkdirs();
		new File(targetFolder, FOLDER_CACHE + "/" + FOLDER_METADATA).mkdirs();
		new File(targetFolder, FOLDER_CACHE + "/" + FOLDER_GRANT).mkdirs();
		new File(targetFolder, FOLDER_CACHE + "/" + FOLDER_PUBLICATION).mkdirs();
		
		googleQuery = new Query(null, null); 
	}	
	
	public void process() throws JAXBException, JsonGenerationException, JsonMappingException, IOException {
		loadLinks();
		
	//	processPages(FOLDER_GRANTS);
	//	processPages(FOLDER_PUBLICATIONS);
		
		processGrants();
		processPublications();
	}	
	
	private void loadLinks() throws JAXBException {
		System.out.println("Loading existing links");
		
		File[] files = new File(targetFolder, FOLDER_CACHE + "/" + FOLDER_LINK).listFiles();
		for (File file : files) 
			if (!file.isDirectory()) {
				Link link = (Link) jaxbUnmarshaller.unmarshal(file);
				links.put(link.getLink(), link);
			}
	}
	
	private void processPages(String sourceType) throws JAXBException, JsonGenerationException, JsonMappingException, IOException {
		System.out.println("Processing " + sourceType + " pages");
		
		googleQuery.setJsonFolder(new File(sourceFolder, sourceType + "/" + FOLDER_JSON).getPath());

		File[] files = new File(sourceFolder, sourceType + "/" + FOLDER_CACHE + "/" + FOLDER_PAGE).listFiles();
		for (File file : files) 
			if (!file.isDirectory()) {
				Page page = (Page) jaxbUnmarshaller.unmarshal(file);
				System.out.println(page.getSelf());
				
				if (!links.containsKey(page.getLink())) {
					Link link = new Link();
					link.setLink(page.getLink());
					link.setSelf(FilenameUtils.getName(page.getSelf()));
					link.setData(FilenameUtils.getName(page.getCache()));
					
					String metadata = null;
					
					// attempt tp get any data
					for (String data : page.getData()) {
						QueryResponse response = googleQuery.queryCache(data);
						if (null != response) 
							for (Item item : response.getItems()) {
								if (item.getLink().equals(link.getLink())) {
									Map<String, Object> pagemap = item.getPagemap();
									if (null != pagemap) {
										metadata = link.getSelf()
												.replace(FOLDER_LINK, FOLDER_METADATA)
												.replace(EXTENSION_XML, EXTENSION_JSON);
										
										mapper.writeValue(new File(targetFolder, FOLDER_CACHE + "/" + FOLDER_METADATA + "/" + metadata), pagemap);
									}
									
									break;
								}
							}
						
						if (null != metadata)
							break;
					}
					
					link.setMetadata(metadata);
					
					File sourceData = new File(sourceFolder, sourceType + "/" + FOLDER_CACHE + "/" + FOLDER_DATA + "/" + link.getData());
					File targetData = new File(targetFolder, FOLDER_CACHE + "/" + FOLDER_DATA + "/" + link.getData());
					
					FileUtils.copyFile(sourceData, targetData);
					
					jaxbMarshaller.marshal(link, new File(targetFolder, FOLDER_CACHE + "/" + FOLDER_LINK + "/" + link.getSelf()));
					
					links.put(link.getLink(), link);
				}
			}
	}
	
	private void processGrants() throws JAXBException {
		System.out.println("Processing grants");
		
		File[] files = new File(sourceFolder, FOLDER_GRANTS + "/" + FOLDER_CACHE + "/" + FOLDER_GRANT).listFiles();
		for (File file : files) 
			if (!file.isDirectory()) {
				Grant grant = (Grant) jaxbUnmarshaller.unmarshal(file);
				System.out.println(grant.getSelf());
				
				Set<String> links = new HashSet<String>();
				for (String link : grant.getLinks()) 
					links.add(this.links.get(link).getSelf());
				
				String self = FilenameUtils.getName(grant.getSelf()).replace(FOLDER_GRANT, FOLDER_RESULT);
				
				Result result = new Result();
				result.setText(grant.getName());
				result.setLinks(links);
				result.setSelf(self);
				
				jaxbMarshaller.marshal(result, new File(targetFolder, FOLDER_CACHE + "/" + FOLDER_GRANT + "/" + self));				
			}
	}
	
	private void processPublications() throws JAXBException {
		System.out.println("Processing publications");
		
		File[] files = new File(sourceFolder, FOLDER_PUBLICATIONS + "/" + FOLDER_CACHE + "/" + FOLDER_PUBLICATION).listFiles();
		for (File file : files) 
			if (!file.isDirectory()) {
				Publication publication = (Publication) jaxbUnmarshaller.unmarshal(file);
				System.out.println(publication.getSelf());

				Set<String> links = new HashSet<String>();
				for (String link : publication.getLinks()) 
					links.add(this.links.get(link).getSelf());
				
				String self = FilenameUtils.getName(publication.getSelf()).replace(FOLDER_PUBLICATION, FOLDER_RESULT);
				
				Result result = new Result();
				result.setText(publication.getTitle());
				result.setLinks(links);
				result.setSelf(self);
				
				jaxbMarshaller.marshal(result, new File(targetFolder, FOLDER_CACHE + "/" + FOLDER_PUBLICATION + "/" + self));				
			}
	}
}
