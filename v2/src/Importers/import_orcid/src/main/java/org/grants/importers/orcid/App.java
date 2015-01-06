package org.grants.importers.orcid;

public class App {

	public static final String ORCID_FOLDER = "orcid/json";
	//private static final String NEO4J_URL = "http://ec2-54-69-203-235.us-west-2.compute.amazonaws.com:7476/db/data/"; 
	private static final String NEO4J_URL = "http://localhost:7474/db/data/";

	/**
	 * Main class function
	 * @param args String[] Expected to have path to the institutions.csv file and Neo4J URL.
	 * If missing, the default parameters will be used.
	 */
	public static void main(String[] args) {
		String orcidFolder = ORCID_FOLDER;
		if (args.length > 0 && null != args[0] && !args[0].isEmpty())
			orcidFolder = args[0];
		
		String neo4jUrl = NEO4J_URL;
		if (args.length > 1 && null != args[1] && !args[1].isEmpty())
			neo4jUrl = args[1];
		
		//Importer.GetAmirRecord();
		
		
		Importer importer = new Importer(neo4jUrl);
		importer.importOrcid(orcidFolder);
	}

}
