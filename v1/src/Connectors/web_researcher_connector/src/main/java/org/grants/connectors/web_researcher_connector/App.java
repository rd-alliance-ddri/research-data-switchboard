package org.grants.connectors.web_researcher_connector;

/*
 * Main class for the project
 */
public class App {
	private static final String SERVER_ROOT_URI = "http://localhost:7474/db/data/";
	
    public static void main( String[] args )
    {
    	// init server uri
		String serverUri = SERVER_ROOT_URI;
		if (args.length > 0 && !args[0].isEmpty())
			serverUri = args[0];
        
        Connector connector = new Connector();
        connector.Connect(serverUri);
    }
	
	
}
