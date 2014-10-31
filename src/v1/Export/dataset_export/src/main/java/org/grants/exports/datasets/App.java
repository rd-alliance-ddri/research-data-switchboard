package org.grants.exports.datasets;

public class App {

	//private static final String SERVER_ROOT_URI = "http://54.83.73.225:7474/db/data/";
		private static final String SERVER_ROOT_URI = "http://localhost:7474/db/data/";
		
	    public static void main( String[] args )
	    {
	    	// init server uri
			String serverUri = SERVER_ROOT_URI;
			if (args.length > 0)
				serverUri = args[0];
			
			// check what server uri has been supplied
			if (serverUri.length() == 0) {
				System.out.print( "Error: No server address has been specyfied. Please provide server addres." );
				return;
			}
	        
			DatasetExport export = new DatasetExport();
	        export.Export(serverUri);
	    }
}
