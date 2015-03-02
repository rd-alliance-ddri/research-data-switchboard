package org.grants.utils.black_list;


import java.io.IOException;

import org.grants.utils.black_list.BlackList.Operation;

public class App {
	private static final String SOURCE_NEO4J_FOLDER = "neo4j";	
	private static final String BLACK_LIST_FILE = "conf/black.list";	
	
	public static void printUsage() {
		System.out.println("USAGE : java -jar black_list-<version>.jar (test | execute) [<neo4j folder>] [<black list file>]");
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		if (args.length == 0 && args[0].isEmpty()) {
 			printUsage();
 			return;
		}
		
		Operation operation = BlackList.Operation.valueOf(args[0]);
		if (null == operation) {
			printUsage();
 			return;
		}

		String sourceNeo4jFolder = SOURCE_NEO4J_FOLDER;
		if (args.length > 1 && !args[1].isEmpty())
			sourceNeo4jFolder = args[1];
			
		String blackListFile = BLACK_LIST_FILE;
		if (args.length > 2 && !args[2].isEmpty())
			blackListFile = args[2];
		
		try {
			BlackList creator = new BlackList(operation, sourceNeo4jFolder, blackListFile);
			
			creator.processNodes("Orcid", "Work", "title");
			creator.processNodes("FigShare", "Publication", "title");
			creator.processNodes("RDA", "Grant", "name_primary");
			creator.processNodes("RDA", "Grant", "name_alternative");
			creator.processNodes("CrossRef", "Publication", "title");
			creator.processNodes("Dryad", "Publication", "title");
			
			creator.save();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}
}
