package org.grants.importers.mets.dryad;

import javax.xml.bind.JAXBException;

import org.grants.importers.mets.Importer;

public class App {
	
	private static final String XML_FOLDER_URI = "dryad";
	private static final String NEO4J_URL = "http://localhost:7474/db/data/";
	
	public static void main(String[] args) {

		String folderXml = XML_FOLDER_URI;
		if (args.length > 0)
			folderXml = args[0];
		
		if (folderXml.isEmpty()) {
			System.out.print( "Error: No Dryad XML folder has been specyfied." );
			return;
		}

		String neo4jUrl = NEO4J_URL;
		if (args.length > 1)
			neo4jUrl = args[1];
		
		if (neo4jUrl.isEmpty()) {
			System.out.print( "Error: No Neo4J Server URL has been specyfied." );
			return;
		}
	
		
		try {
			Importer importer = new Importer(folderXml, neo4jUrl);
			
			importer.importRecords();
			
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

}
