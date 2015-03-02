package org.grants.exporters.keys;

import java.io.IOException;

public class App {
	private static final String SOURCE_NEO4J_FOLDER = "neo4j";	
	private static final String FIRST_LABEL = "RDA";	
	private static final String SECOND_LABEL = "ORCID";	
	private static final int MIN_RELATIONSHIPS = 1;	
	private static final int MAX_RELATIONSHIPS = 4;	
	private static final String OUTPUT_FOLDER = "keys";	
	private static final int PAGINATION = 0;	
	
	private static String getArgument(String[] args, int id) {
		return args.length > id && !args[id].isEmpty() ? args[id] : null;
	}
		
	
	private static String getArgument(String[] args, int id, String defValue) {
		String value = getArgument(args, id);
		return null == value ? defValue : value;
	}
	
	private static int getIntArgument(String[] args, int id, int defValue) {
		String value = getArgument(args, id);
		return null == value ? defValue : Integer.parseInt(value);
	}		
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String neo4jFolder = getArgument(args, 0, SOURCE_NEO4J_FOLDER);
		String label1 = getArgument(args, 1, FIRST_LABEL);
		String label2 = getArgument(args, 2, SECOND_LABEL);
		int minRels = getIntArgument(args, 3, MIN_RELATIONSHIPS);
		int maxRels = getIntArgument(args, 4, MAX_RELATIONSHIPS);
		String outputFolder = getArgument(args, 5, OUTPUT_FOLDER);
		int pagination = getIntArgument(args, 6, PAGINATION);
		
		try {
			Exporter expoter = new Exporter(neo4jFolder, outputFolder);
			expoter.process(label1, label2, minRels, maxRels, pagination, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}
}
