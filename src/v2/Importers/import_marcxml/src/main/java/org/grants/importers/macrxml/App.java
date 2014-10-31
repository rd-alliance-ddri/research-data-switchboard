package org.grants.importers.macrxml;

import gov.loc.marc21.slim.DataFieldType;
import gov.loc.marc21.slim.SubfieldatafieldType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.RestAPIFacade;
import org.neo4j.rest.graphdb.entity.RestNode;
import org.neo4j.rest.graphdb.index.RestIndex;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;
import org.openarchives.oai._2.HeaderType;
import org.openarchives.oai._2.RecordType;
import org.openarchives.oai._2.StatusType;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class App {

	private static enum Labels implements Label {
	};
	
	public static Element findXmlElement(Element xml, String name) {
		if (xml.getLocalName().equals(name))
			return xml;

		return null;
	}
	
	public static Element findXmlElement(List<Object> xmls, String name) {
		for (Object xml : xmls) 
			if (xml instanceof Element && ((Element) xml).getLocalName().equals(name))
				return (Element) xml;

		return null;
	}
	
	public static Element findXmlElement(NodeList xmls, String name) {
		if (null != xmls) {
			int length = xmls.getLength();
			for (int i = 0; i < length; ++i) {
				org.w3c.dom.Node xml = xmls.item(i);
				if (xml instanceof Element && ((Element) xml).getLocalName().equals(name))
					return (Element) xml;
			}
		}

		return null;
	}
	
	public static List<Element> findXmlElements(List<Object> xmls, String name) {
		List<Element> list = null;		
		for (Object xml : xmls) 
			if (xml instanceof Element && ((Element) xml).getLocalName().equals(name)) {
				if (null == list)
					list = new ArrayList<Element>();
				
				list.add((Element) xml);
			}

		return list;
	}
	
	@SuppressWarnings("unchecked")
	public static void addData(Map<String, Object> map, String field, String data) {
		if (null != field && null != data) {
			Object par = map.get(field);
			if (null == par) 
				map.put(field, data);
			else if (par instanceof String) {
				List<String> pars = new ArrayList<String>();
				pars.add((String) par);
				pars.add(data);
				map.put(field, par);				
			} else 
				((List<String>)par).add(data);			
		}
	}
	
	public static DataFieldType getDataFieldByTag(List<DataFieldType> dataFields, String tag) {
		for (DataFieldType dataField : dataFields) 
			if (dataField.getTag() != null && dataField.getTag().equals(tag))
				return dataField;
		
		return null;
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		// connect to graph database
		RestAPI graphDb = new RestAPIFacade("http://localhost:7474/db/data/");  
		RestCypherQueryEngine engine=new RestCypherQueryEngine(graphDb);  
		
		engine.query("CREATE CONSTRAINT ON (n:Dryad_Book) ASSERT n.doi IS UNIQUE", Collections.<String, Object> emptyMap());
		engine.query("CREATE CONSTRAINT ON (n:Dryad_Protocol) ASSERT n.dryad_id IS UNIQUE", Collections.<String, Object> emptyMap());
		engine.query("CREATE CONSTRAINT ON (n:Dryad_Activity) ASSERT n.doi IS UNIQUE", Collections.<String, Object> emptyMap());
		engine.query("CREATE CONSTRAINT ON (n:Dryad_Dataset) ASSERT n.doi IS UNIQUE", Collections.<String, Object> emptyMap());
		engine.query("CREATE CONSTRAINT ON (n:Dryad_Map) ASSERT n.doi IS UNIQUE", Collections.<String, Object> emptyMap());
		engine.query("CREATE CONSTRAINT ON (n:Dryad_Image) ASSERT n.doi IS UNIQUE", Collections.<String, Object> emptyMap());
		engine.query("CREATE CONSTRAINT ON (n:Dryad_Article) ASSERT n.doi IS UNIQUE", Collections.<String, Object> emptyMap());
		
		Map<String, RestIndex<Node>> mapIndexes = new HashMap<String, RestIndex<Node>>();
		mapIndexes.put("Book", graphDb.index().forNodes("Dryad_Book"));
		mapIndexes.put("Protocol", graphDb.index().forNodes("Dryad_Protocol"));
		mapIndexes.put("Activity", graphDb.index().forNodes("Dryad_Activity"));
		mapIndexes.put("Dataset", graphDb.index().forNodes("Dryad_Dataset"));
		mapIndexes.put("Map", graphDb.index().forNodes("Dryad_Map"));
		mapIndexes.put("Image", graphDb.index().forNodes("Dryad_Image"));
		mapIndexes.put("Article", graphDb.index().forNodes("Dryad_Article"));
		
		try {
			JAXBContext jc = JAXBContext.newInstance( "org.openarchives.oai._2:gov.loc.marc21.slim" );
			Unmarshaller u = jc.createUnmarshaller();
			
			File[] folders = new File("cern").listFiles();
			for (File folder : folders)
				if (folder.isDirectory() && !folder.getName().equals("_cache")) {
					File[] files = folder.listFiles();
					for (File file : files)  
						if (!file.isDirectory()) {				
							try {
								JAXBElement<?> element = (JAXBElement<?>) u.unmarshal( new FileInputStream( file ) );
								
								RecordType record = (RecordType) element.getValue();	
								HeaderType header = record.getHeader();
								
								if (header.getStatus() != StatusType.DELETED) {
									
									String idetifier = header.getIdentifier();
	//								System.out.println(idetifier.toString());
									String datestamp = header.getDatestamp();
		//							System.out.println(datestamp.toString());
									List<String> specs = header.getSetSpec();
									
									if (null != record.getMetadata()) {
										JAXBElement<gov.loc.marc21.slim.RecordType> metadata = (JAXBElement<gov.loc.marc21.slim.RecordType>) record.getMetadata().getAny();
										if (metadata.getValue() instanceof gov.loc.marc21.slim.RecordType) {
											gov.loc.marc21.slim.RecordType rec = (gov.loc.marc21.slim.RecordType) metadata.getValue();
											
											DataFieldType identificators = getDataFieldByTag(rec.getDatafield(), "024"); 
											if (null != identificators) {
												String cernId = null;
												
												Map<String, Object> map = new HashMap<String, Object>();
												for (SubfieldatafieldType subField : identificators.getSubfield()) {
													String id = subField.getValue();
													if (id != null && !id.isEmpty()) {
														if (id.contains("oai:"))
															cernId = id;
														else {
															addData(map, "identifier", id);
														}															
													}
												}
												
												if (null != cernId) {
													
												}
											}
											
											
											System.out.println(rec.getType().toString());
										}
									}
								}
							
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}
						}
			}
			
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	/*	
		System.out.println("Done!");
		for (String genre : genres) {
			System.out.println(genre);

		}*/
		
	}
}
