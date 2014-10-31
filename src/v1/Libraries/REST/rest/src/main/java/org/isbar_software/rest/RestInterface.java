package org.isbar_software.rest;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource.Builder;

/**
 * Simple REST library designed to load REST data
 * 
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */
public class RestInterface {
	protected static final String HEADER_X_STREAM = "X-Stream";
	protected static final ObjectMapper mapper = new ObjectMapper();   
	protected static final TypeReference<LinkedHashMap<String, Object>> linkedHashMapTypeReference = new TypeReference<LinkedHashMap<String, Object>>() {};   

	public static boolean enableXStream = false;
	
	/**
	 * Opetation Types enum
	 *
	 */
	public enum OperationTypes {
        GET, PUT, POST, DELETE
    }
	
	/**
	 * Invoke function with no data
	 * @param type OperationTypes
	 * @param url String containing url
	 * @return ClientResponse
	 */
	public static ClientResponse Invoke( OperationTypes type, final String url) {
		return Invoke(type, url, null);
	}
	    
	/**
	 * Invoke function
	 * @param type OperationTypes
	 * @param url String containing url
	 * @param json String containing JSON data
	 * @return ClientResponse
	 */
	public static ClientResponse Invoke( OperationTypes type, final String url, final String json ) {
        Builder builder = Client.create()
                        .resource( url )
                        .accept( MediaType.APPLICATION_JSON ) 
                        .type( MediaType.APPLICATION_JSON );
        
        if (enableXStream)
        	builder = builder.header( HEADER_X_STREAM, "true" );

        ClientResponse response = null;

        switch (type)
        {
        case GET:
                return builder.get( ClientResponse.class );

        case PUT:
                if (json != null)
                        builder = builder
                                        .type( MediaType.APPLICATION_JSON )
                                        .entity( json );

                return builder.put( ClientResponse.class );

        case POST:
                if (json != null)
                        builder = builder
                                        .type( MediaType.APPLICATION_JSON )
                                        .entity( json );

                return builder.post( ClientResponse.class );

        case DELETE:
                return builder.delete( ClientResponse.class );
    
        default: // invalid operation?
                return null;
        }
    }    

	/**
	 * Converts object to JSON string
	 * @param o any convertable Object
	 * @return String - formatted JSON
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static String toJson(Object o) 
			throws JsonGenerationException, JsonMappingException, IOException {
		return mapper.writeValueAsString(o);
	}
	
	/**
	 * Converts JSON to an object with type, specified with valueType
	 * @param json String containing formatted JSON
	 * @param valueType Type of expected object
 	 * @return Object with specified type
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */  
	public static <T> T fromJson(final String json, Class<T> valueType) 
            throws JsonParseException, JsonMappingException, IOException {
		return mapper.readValue(json, valueType);
	}
  
	/**
	 * Converts JSON to an object with type, specified with valueType
	 * @param json String containing formatted JSON
	 * @param valueType JavaType of expected object
	 * @return Object with specified type
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static <T> T fromJson(final String json, JavaType valueType) 
            throws JsonParseException, JsonMappingException, IOException {
		return mapper.readValue(json, valueType);
	}
  
	/**
	 * Converts JSON to an object with type, specified with valueType
	 * @param json String containing formatted JSON
	 * @param valueTypeRef TypeReference with expected object object
	 * @return Object with specified type
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static <T> T fromJson(final String json, TypeReference<T> valueTypeRef) 
            throws JsonParseException, JsonMappingException, IOException {
		return mapper.readValue(json, valueTypeRef);
	}
  
	/**
	 * Converts JSON to an {@code Map<String, Object>}
	 * @param json String containing formatted JSON
	 * @return {@code Map<String, Object>}
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static Map<String, Object> fromJson(final String json) 
            throws JsonParseException, JsonMappingException, IOException {
		return fromJson(json, linkedHashMapTypeReference);
	}
}
