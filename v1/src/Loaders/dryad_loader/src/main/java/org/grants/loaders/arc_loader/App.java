package org.grants.loaders.arc_loader;

/**
 * Main class
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */
public class App {
	//private static final String SERVER_ROOT_URI = "http://54.83.73.225:7474/db/data/";
		private static final String SERVER_ROOT_URI = "http://localhost:7474/db/data/";
		
	    public static void main( String[] args )
	    {
	    	// init server uri
			String serverUri = SERVER_ROOT_URI;
			if (args.length > 0 && !args[0].isEmpty())
				serverUri = args[0];
	    		          
	        Loader loader = new Loader();
	        loader.Load(serverUri);
	    }
}
