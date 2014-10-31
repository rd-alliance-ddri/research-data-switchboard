package org.grants.importers.institutions;

public class App {
	public static final String INSTITUTIONS_SCV = "institutions.csv";
	private static final String NEO4J_URL = "http://ec2-54-69-203-235.us-west-2.compute.amazonaws.com:7476/db/data/";//"http://localhost:7474/db/data/";

	public static void main(String[] args) {
		String institutionCsv = INSTITUTIONS_SCV;
		if (args.length > 0 && null != args[0] && !args[0].isEmpty())
			institutionCsv = args[0];
		
		String neo4jUrl = NEO4J_URL;
		if (args.length > 1 && null != args[1] && !args[1].isEmpty())
			neo4jUrl = args[1];
		
		Importer importer = new Importer(neo4jUrl);
		importer.importInstitutions(institutionCsv);
	}

	// avid-involution-736
}
