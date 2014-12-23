package org.grants.importers.web_researchers;

import javax.xml.bind.JAXBException;

public class App {
	private static final String DATA_FOLDER = "publications";
	private static final String NEO4J_URL = "http://ec2-54-69-203-235.us-west-2.compute.amazonaws.com:7476/db/data/"; 
//	private static final String NEO4J_URL = "http://localhost:7474/db/data/";
//	private static final String NEO4J_URL = "http://localhost:7476/db/data/";

	/**
	 * Main class function
	 * @param args String[] Expected to have path to the institutions.csv file and Neo4J URL.
	 * If missing, the default parameters will be used.
	 */
	public static void main(String[] args) {
		String dataFolder = DATA_FOLDER;
		if (args.length > 0 && null != args[0] && !args[0].isEmpty())
			dataFolder = args[0];
		
		String neo4jUrl = NEO4J_URL;
		if (args.length > 1 && null != args[1] && !args[1].isEmpty())
			neo4jUrl = args[1];
		
		try {
			Importer importer = new Importer(neo4jUrl, dataFolder);
			importer.process();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
}
