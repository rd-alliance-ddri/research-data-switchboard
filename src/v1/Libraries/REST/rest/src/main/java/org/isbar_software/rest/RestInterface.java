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

public class RestInterface {
	protected static final String HEADER_X_STREAM = "X-Stream";
	protected static final ObjectMapper mapper = new ObjectMapper();   
	protected static final TypeReference<LinkedHashMap<String, Object>> linkedHashMapTypeReference = new TypeReference<LinkedHashMap<String, Object>>() {};   

	public static boolean enableXStream = false;
	
	public enum OperationTypes {
        GET, PUT, POST, DELETE
    }
	
	public static ClientResponse Invoke( OperationTypes type, final String url) {
		return Invoke(type, url, null);
	}
	    
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

	public static String toJson(Object o) 
			throws JsonGenerationException, JsonMappingException, IOException {
		return mapper.writeValueAsString(o);
	}
  
	public static <T> T fromJson(final String json, Class<T> valueType) 
            throws JsonParseException, JsonMappingException, IOException {
		return mapper.readValue(json, valueType);
	}
  
	public static <T> T fromJson(final String json, JavaType valueType) 
            throws JsonParseException, JsonMappingException, IOException {
		return mapper.readValue(json, valueType);
	}
  
	public static <T> T fromJson(final String json, TypeReference<T> valueTypeRef) 
            throws JsonParseException, JsonMappingException, IOException {
		return mapper.readValue(json, valueTypeRef);
	}
  
	public static Map<String, Object> fromJson(final String json) 
            throws JsonParseException, JsonMappingException, IOException {
		return fromJson(json, linkedHashMapTypeReference);
	}
}
