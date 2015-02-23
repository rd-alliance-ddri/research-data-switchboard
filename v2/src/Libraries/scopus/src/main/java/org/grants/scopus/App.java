package org.grants.scopus;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.FileUtils;
import org.grants.scopus.response.SearchResults;
import org.grants.scopus.type.AbstractType;
import org.grants.scopus.type.ResourceType;

public class App {

	public static void main(String[] args) {
		Scopus scopus = new Scopus("", "");
		
	/*	String scopusJson = scopus.abstractString(AbstractType.abstractTypeScopusId, "0037070197");
		try {
			FileUtils.write(new File("scopus.json"), scopusJson);
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
		// Parse test
/*		AbstractResponse result  = scopus.parseAbstractResponse(new File("/home/dima/abstractResult.json"));
		System.out.println(result);
	*/	

	/*	
		try {
			String json = scopus.searchString(ResourceType.resourceTypeScopus, "AU-ID(8528261900)");
			if (null != json) {
				System.out.println(json);
				FileUtils.write(new File("scopus.json"), json);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
		
		SearchResults results = scopus.parseSearchResult(new File("scopus.json"));
		
		/*
		

		results = scopus.parseJson(json);
		System.out.println(results);*/
	}

}
