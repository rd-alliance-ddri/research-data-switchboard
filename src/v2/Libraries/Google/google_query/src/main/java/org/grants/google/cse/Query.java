package org.grants.google.cse;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

public class Query {

	private String cseId;
	private String apiKey;
	private String jsonFolder;
	
	private static final String CSE_API = "https://www.googleapis.com/customsearch/v1?key=%s&cx=%s&q=%s";
	private static final String ENCODE_UTF8 = "UTF-8";
	private static final ObjectMapper mapper = new ObjectMapper();   
	
	public Query(final String cseId, final String apiKey) {
		this.cseId = cseId;
		this.apiKey = apiKey;
	}

	public String getJsonFolder() {
		return jsonFolder;
	}

	public void setJsonFolder(String jsonFolder) {
		this.jsonFolder = jsonFolder;
		
		new File(this.jsonFolder).mkdirs();
	} 
	
	public QueryResponse query(final String queryString) {
		try {
			String query = URLEncoder.encode("\"" + queryString + "\"", ENCODE_UTF8);
			File jsonFile = null;
			String json = null;
			if (null != jsonFolder) {
				jsonFile = new File(jsonFolder + "/" + query + ".json");
				if (jsonFile.exists() && !jsonFile.isDirectory())
					json = FileUtils.readFileToString(jsonFile);
			}
			
			if (null == json || json.isEmpty()) {
				json = get(String.format(CSE_API, apiKey, cseId, query));
				
				if (null == json || json.isEmpty())
					return null;
				
				if (null != jsonFile) 
					FileUtils.write(jsonFile, json);
			}
			
			//System.out.println(json);
			
			return mapper.readValue(json, QueryResponse.class);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	
	private String get( final String url ) {
		System.out.println("Downloading: " + url);
		
		for (int i = 0; i < 10; ++i) {					
			try {
					ClientResponse response = Client.create()
							.resource( url )
							.accept( MediaType.APPLICATION_JSON ) 
							.get( ClientResponse.class );
			
				if (response.getStatus() == 200) 
					return response.getEntity( String.class );
				else {
					System.out.println("Error: " + response.getStatus() + ", JSON: " + response.getEntity( String.class ));
			
					return null;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		return null;			
    }

}
