package org.grants.imporers.local2;

public class App {
	private static final String TARGET_NEO4J_FOLDER = "neo4j";	
	private static final String INPUT_FOLDER = "graph";	
	
	public static void main(String[] args) {
		
		// TODO Auto-generated method stub
	
		String targetNeo4jFolder = TARGET_NEO4J_FOLDER;
		if (args.length > 0 && !args[0].isEmpty())
			targetNeo4jFolder = args[0];
			
		String inputFolder = INPUT_FOLDER;
		if (args.length > 1 && !args[1].isEmpty())
			inputFolder = args[1];
		
		try {
			Importer importer = new Importer(targetNeo4jFolder, inputFolder);
			importer.process();
		} catch (Exception e) {
			e.printStackTrace();
		}			
	}
}
