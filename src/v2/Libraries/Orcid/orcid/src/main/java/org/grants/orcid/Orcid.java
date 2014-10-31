package org.grants.orcid;

import java.io.IOException;

import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

public class Orcid {
	private static final String ORCID_URL = "http://pub.orcid.org/";
	private static final String ORCID_HOST = "orcid.org/";
	
	//private static final String ORCID_BIO = "/orcid-bio";
	private static final String ORCID_WORKS = "/orcid-works";
	private static final String ORCID_RECORD = "/orcid-record";
	
	private static final ObjectMapper mapper = new ObjectMapper();  
	
	public OrcidMessage queryId(String orcidId, RequestType responseType) {
		
		int idx = orcidId.indexOf("orcid.org/");
		if (idx >= 0)
			orcidId = orcidId.substring(idx + ORCID_HOST.length());
		
		String url = ORCID_URL + orcidId;
		if (responseType == RequestType.works)
			url += ORCID_WORKS;
		else if (responseType == RequestType.record)
			url += ORCID_RECORD;
			
			
		String json = get(url);
		
		System.out.println(json);
		
		try {
			return mapper.readValue(json, OrcidMessage.class);
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
				
		ClientResponse response = Client.create()
								  .resource( url )
								  .accept( MediaType.APPLICATION_JSON ) 
								  .get( ClientResponse.class );
		
		if (response.getStatus() == 200) 
			return response.getEntity( String.class );
		
		return null;
    } 
}
