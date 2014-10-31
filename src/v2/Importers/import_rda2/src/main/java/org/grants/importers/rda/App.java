package org.grants.importers.rda;

public class App {
	private static final String NEO4J_URL = "http://localhost:7474/db/data/";
	private static final String JSON_FOLDER_URI = "rda";
	
	public static void main(String[] args) {
		String folderUri = JSON_FOLDER_URI;
		if (args.length > 0)
			folderUri = args[0];
		
		if (folderUri.isEmpty()) {
			System.out.print( "Error: No JSON folder has been specyfied." );
			return;
		}
		
		String neo4jUrl = NEO4J_URL;
		if (args.length > 1)
			neo4jUrl = args[1];
		
		if (neo4jUrl.isEmpty()) {
			System.out.print( "Error: No Neo4J Server URL has been specyfied." );
			return;
		}

		Importer importer = new Importer(folderUri, neo4jUrl);
		importer.importRecords();
	}

}
