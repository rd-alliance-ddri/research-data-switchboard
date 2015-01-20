package org.grants.scopus;

import java.io.File;

import org.grants.scopus.response.SearchResults;

public class App {

	public static void main(String[] args) {
		Scopus scopus = new Scopus(null, null);
		
		// Parse test
		SearchResults results = scopus.parseJson(new File("/home/dima/response-1.json"));
		System.out.println(results);
		
		String json = scopus.queryString("8528261900");
		System.out.println(json);

		results = scopus.parseJson(json);
		System.out.println(results);
	}

}
