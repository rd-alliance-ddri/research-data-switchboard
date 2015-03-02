package org.grants.linkers.rda;

public class App {
	private static final String SOURCE_NEO4J_FOLDER = "neo4j-1";	
	private static final String TARGET_NEO4J_FOLDER = "neo4j-2";	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String sourceNeo4jFolder = SOURCE_NEO4J_FOLDER;
		if (args.length > 0 && !args[0].isEmpty())
			sourceNeo4jFolder = args[0];
		
		String targetNeo4j4jFolder = TARGET_NEO4J_FOLDER;
		if (args.length > 1 && !args[1].isEmpty())
			targetNeo4j4jFolder = args[1];
			
		Linker linker = new Compiler(sourceNeo4jFolder, targetNeo4j4jFolder);			
		linker.process();
	}
}
