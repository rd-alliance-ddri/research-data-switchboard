package org.grants.scopus;

import java.io.File;
import java.io.IOException;

import org.grants.scopus.response.Result;
import org.grants.scopus.response.SearchResults;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class App {
	
	private static final ObjectMapper mapper = new ObjectMapper();   
	
	static {
		mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
		//mapper.configure(Feature.UNWRAP_ROOT_VALUE, true);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/*File graphFile = new File("/home/dima/response-1.json");
		
		try {
			SearchResults results = mapper.readValue(graphFile, SearchResults.class);
			
			System.out.println(results);
			System.out.println("Successfull");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		Scopus scopus = new Scopus("", "");
		
		String json = scopus.query("Math");
		
		System.out.println(json);
		
		try {
			SearchResults results = mapper.readValue(json, SearchResults.class);
			
			System.out.println(results);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
