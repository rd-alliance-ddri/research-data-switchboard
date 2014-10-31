package org.grants.crossref;

import java.io.IOException;
import java.net.URLEncoder;

import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

public class CrossRef {
	private static final String URL_CROSSREF = "http://api.crossref.org/";

	private static final String FUNCTION_WORKS = "works";
	/*private static final String FUNCTION_FUNDERS = "funders";
	private static final String FUNCTION_MEMBERS = "members";
	private static final String FUNCTION_TYPES = "types";
	private static final String FUNCTION_LICENSES = "licenses";
	private static final String FUNCTION_JOURNALS = "journals";*/
	
	private static final String URL_CROSSREF_WORKDS = URL_CROSSREF + FUNCTION_WORKS;
	/*private static final String URL_CROSSREF_FUNDERS = URL_CROSSREF + FUNCTION_FUNDERS;
	private static final String URL_CROSSREF_MEMBERS = URL_CROSSREF + FUNCTION_MEMBERS;
	private static final String URL_CROSSREF_TYPES = URL_CROSSREF + FUNCTION_TYPES;
	private static final String URL_CROSSREF_LICENSES = URL_CROSSREF + FUNCTION_LICENSES;
	private static final String URL_CROSSREF_JOURNALS = URL_CROSSREF + FUNCTION_JOURNALS;*/
		
	private static final String URL_ENCODING = "UTF-8";
	
	/*
	private static final String PARAM_QUERY = "q";
	private static final String PARAM_HEADER = "header";*/
	
	private static final String STATUS_OK = "ok";
	
	private static final String MESSAGE_WORK = "work";
	private static final String MESSAGE_WORK_LIST = "work-list";
		
	private static final ObjectMapper mapper = new ObjectMapper();   
	private static final TypeReference<Response<ItemList>> itemListType = new TypeReference<Response<ItemList>>() {};   
	private static final TypeReference<Response<Item>> itemType = new TypeReference<Response<Item>>() {};   
	
	public ItemList requestWorks() {
		try {
			String json = get(URL_CROSSREF_WORKDS);
			if (null != json) {			
				Response<ItemList> response = mapper.readValue(json, itemListType);
				
				//System.out.println(response);
				
				if (response.getStatus().equals(STATUS_OK) && 
					response.getMessageType().equals(MESSAGE_WORK_LIST)) 
					return response.getMessage();
			}		
			else
				System.out.println("Inavlid response");
			
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
	
	public Item requestWork(final String doi) {
		try {
			String json = get(URL_CROSSREF_WORKDS + "/" + URLEncoder.encode(doi, URL_ENCODING).replace("%2F", "/"));
			if (null != json) {			
				Response<Item> response = mapper.readValue(json, itemType);
				
				//System.out.println(response);
				
				if (response.getStatus().equals(STATUS_OK) && 
					response.getMessageType().equals(MESSAGE_WORK)) 
					return response.getMessage();
			}		
			else
				System.out.println("Inavlid response");
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
	
	/*
	
	public List<Item> requestDois(final String query)
	{
		Map<String, String> pars = new HashMap<String, String>();
		pars.put(PARAM_QUERY, query);
		
		String json = requestDois(pars);
		if (null != json) {
			try {
				return mapper.readValue( json, itemListReference );
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	public Header requestDoisHeader(final String query)
	{
		Map<String, String> pars = new HashMap<String, String>();
		pars.put(PARAM_QUERY, query);
		pars.put(PARAM_HEADER, "true");
		
		String json = requestDois(pars);
		if (null != json) {
			try {
				return mapper.readValue( json, headerReference );
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
	
	public String requestDois(Map<String, String> pars)
	{
		try {
			return get(formatUrl(URL_CROSSREF_DOIS, pars));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}		
		
		return null;
	}
	
	
	private String get( final String url ) {
		System.out.println("Downloading: " + url);
				
		ClientResponse response = Client.create()
								  .resource( url )
								  .accept( MediaType.APPLICATION_JSON ) 
								  .type( MediaType.APPLICATION_JSON )
								  .get( ClientResponse.class );
		
		try {
			String entity =  response.getEntity( String.class );
			System.out.println(entity);
			return entity; // mapper.readValue( entity, linkedListReference );
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
    } 
	
	private String formatUrl(final String url, Map<String, String> pars) throws UnsupportedEncodingException {
		if (null == pars)
			return url;
		
		StringBuilder sb = null;
		for (Map.Entry<String, String> entry : pars.entrySet()) {
			if (null == sb) 
				sb = new StringBuilder();
			else
				sb.append('&');
			sb.append(entry.getKey());
			sb.append('=');
			sb.append(URLEncoder.encode(entry.getValue(), URL_ENCODING));
		}
		
		if (sb != null)
			return url + '?' + sb.toString();
		else
			return url;
	}
	*/
}
