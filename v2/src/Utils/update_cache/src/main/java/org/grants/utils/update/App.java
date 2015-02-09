package org.grants.utils.update;

public class App {
	private static final String FOLDER_SOURCE = "/home/dima/Grants/google";
	private static final String FOLDER_TARGET = "/home/dima/Grants/google2";
	
	/**
	 * Class main function 
	 * @param args String[]
	 */
	public static void main(String[] args) {
		String sourcePath = FOLDER_SOURCE;
		if (args.length > 0 && !args[0].isEmpty())
			sourcePath = args[0];
		
		String targetPath = FOLDER_TARGET;
		if (args.length > 1 && !args[1].isEmpty())
			targetPath = args[1];
		
		try {
			UpdateCache updateCache = new UpdateCache(sourcePath, targetPath);
			updateCache.process();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
