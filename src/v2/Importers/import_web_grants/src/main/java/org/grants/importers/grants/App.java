package org.grants.importers.grants;

public class App {
	private static final String JSON_FOLDER_URI = "grants/json";
	private static final String CACHE_FOLDER_URI = "grants/cache";
	//private static final String NEO4J_URL = "http://localhost:7476/db/data/";
	private static final String NEO4J_URL = "http://ec2-54-69-203-235.us-west-2.compute.amazonaws.com:7476/db/data/";
	public static void main(String[] args) {

		String folderJson = JSON_FOLDER_URI;
		if (args.length > 0 && !args[0].isEmpty())
			folderJson = args[0];

		String folderCache = CACHE_FOLDER_URI;
		if (args.length > 1 && !args[1].isEmpty())
			folderCache = args[0];

		String neo4jUrl = NEO4J_URL;
		if (args.length > 2 && !args[2].isEmpty())
			neo4jUrl = args[2];
		
		Importer importer = new Importer(folderJson, folderCache, neo4jUrl);
		importer.importGrants();
	}	
}
