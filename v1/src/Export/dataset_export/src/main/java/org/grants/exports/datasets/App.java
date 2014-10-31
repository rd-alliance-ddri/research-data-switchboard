package org.grants.exports.datasets;

/**
 * Main class to export Datasets graphs. 
 * The Graps and index will be exported into json folder.
 * 
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
			
			DatasetExport export = new DatasetExport();
	        export.Export(serverUri);
	    }
}
