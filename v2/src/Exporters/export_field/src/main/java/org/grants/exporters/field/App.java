package org.grants.exporters.field;

import java.io.IOException;

public class App {
	private static final String SOURCE_NEO4J_FOLDER = "neo4j/data/graph.db";	
	private static final String OUTPUT_FOLDER = "fields";
	private static final String NODE_SOURCE = "Orcid";
	private static final String NODE_TYPE = "Work";
	private static final String PROPERTY_NAME = "title";
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String nodeSource = NODE_SOURCE;
		if (args.length > 0 && !args[0].isEmpty())
			nodeSource = args[0];
		
		String nodeType = NODE_TYPE;
		if (args.length > 1 && !args[1].isEmpty())
			nodeType = args[1];
		
		String propertyName = PROPERTY_NAME;
		if (args.length > 2 && !args[2].isEmpty())
			propertyName = args[2];
		
		String sourceNeo4jFolder = SOURCE_NEO4J_FOLDER;
		if (args.length > 3 && !args[3].isEmpty())
			sourceNeo4jFolder = args[3];
			
		String outputFolder = OUTPUT_FOLDER;
		if (args.length > 4 && !args[4].isEmpty())
			outputFolder = args[4];
		
		try {
			Exporter expoter = new Exporter(nodeSource, nodeType, propertyName, sourceNeo4jFolder, outputFolder);
			expoter.process();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}
}

