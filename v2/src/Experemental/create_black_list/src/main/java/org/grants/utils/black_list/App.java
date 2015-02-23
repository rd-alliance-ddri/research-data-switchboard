package org.grants.utils.black_list;

import java.io.IOException;

public class App {
	private static final String SOURCE_NEO4J_FOLDER = "neo4j";	
	private static final String BLACK_LIST_FILE = "conf/black.list";	
	private static final String TRASH_LIST_FILE = "conf/trash.list";	
	private static final String WARNING_LIST_FILE = "conf/warning.list";	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String sourceNeo4jFolder = SOURCE_NEO4J_FOLDER;
		if (args.length > 0 && !args[0].isEmpty())
			sourceNeo4jFolder = args[0];
			
		String blackListFile = BLACK_LIST_FILE;
		if (args.length > 1 && !args[1].isEmpty())
			blackListFile = args[1];

		String trashListFile = TRASH_LIST_FILE;
		if (args.length > 2 && !args[2].isEmpty())
			trashListFile = args[3];

		String wariningListFile = WARNING_LIST_FILE;
		if (args.length > 3 && !args[3].isEmpty())
			wariningListFile = args[3];
		
		try {
			BlackListCreator creator = new BlackListCreator(sourceNeo4jFolder, 
					blackListFile, trashListFile, wariningListFile);
			
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
