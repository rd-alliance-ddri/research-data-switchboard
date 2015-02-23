package org.grants.scopus.deserilaize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.grants.scopus.response.Author;
import org.grants.scopus.response.Date;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;


public class AuthorDeserializer extends JsonDeserializer<Author> {

	private static final String JSON_AUID = "@auid";
	private static final String JSON_AUTHOR_URL = "author-url";
	private static final String JSON_CE_INDEXED_NAME = "ce:indexed-name";
	private static final String JSON_CE_GIVEN_NAME = "ce:given-name";
	private static final String JSON_CE_SURNAME = "ce:surname";
	private static final String JSON_CE_INNITIALS = "ce:initials";
	private static final String JSON_PREFERRED_NAME = "preferred-name";
	
	private static final String PROPERTY_AUTHOR_ID = "authorId";
	private static final String PROPERTY_AUTHOR_URL = "authorUrl";
	private static final String PROPERTY_FULL_NAME = "fullName";
	private static final String PROPERTY_GIVEN_NAME = "familyName";
	private static final String PROPERTY_FAMILY_NAME = "familyName";
	private static final String PROPERTY_INITIALS = "initials";
	
	protected List<Object> parseArray(JsonParser jp) throws IOException {
		// check, that we actually have an object
		//if (jp.getCurrentToken() != JsonToken.START_ARRAY)
		//	throw new IOException("Wrong array start token: "+jp.getCurrentToken());
		
		List<Object> list = null;
		for(JsonToken token = jp.nextToken(); token != null && token != JsonToken.END_ARRAY; token = jp.nextToken()) {
			if (token != JsonToken.START_OBJECT)
				throw new IOException("Wrong object start token: "+jp.getCurrentToken());
			
			Object value = parseObject(jp);
			
			if (null == list)
				list = new ArrayList<Object>();
			list.add(value);
		}
		
		return list;
	}
	
	protected Map<String, Object> parseObject(JsonParser jp) throws IOException {
		// check, that we actually have an object
		//if (jp.getCurrentToken() != JsonToken.START_OBJECT)
		//	throw new IOException("Wrong object start token: "+jp.getCurrentToken());
		
		// create empty map
		Map<String, Object> map = null;
		// run the main cicle until we will actually get end of the object
		for(JsonToken token = jp.nextToken(); token != null && token != JsonToken.END_OBJECT; token = jp.nextToken()) {
			// check that next object is a field name
			if (token != JsonToken.FIELD_NAME)
				throw new IOException("Wrong object field token: "+jp.getCurrentToken());
			
			// retrive object field key
			String key = jp.getCurrentName();
			Object value = null;
			// go to the next token
			token = jp.nextToken();
			
			if (token == JsonToken.START_ARRAY)
				value = parseArray(jp);
			else if (token == JsonToken.START_OBJECT)
				value = parseObject(jp);
			else if (token == JsonToken.VALUE_STRING)
				value = jp.getValueAsString();
			else if (token == JsonToken.VALUE_NUMBER_INT)
				value = jp.getValueAsDouble();
			else if (token == JsonToken.VALUE_NUMBER_INT)
				value = jp.getIntValue();
			else if (token == JsonToken.VALUE_TRUE)
				value = true;
			else if (token == JsonToken.VALUE_FALSE)
				value = false;
			else
				value = null;
			
			if (null == map)
				map = new HashMap<String, Object>();
			map.put(key,  value);
		}
		
		return map;
	}
	
	protected Object parse(JsonParser jp) throws IOException {
		Object object = null;
		JsonToken token = jp.getCurrentToken();
		if (token == JsonToken.START_ARRAY)
			object = parseArray(jp);
		else if (token == JsonToken.START_OBJECT)
			object = parseObject(jp);
		else
			throw new IOException("Wrong start token: "+jp.getCurrentToken()+". The START_ARRAY or START_OBJECT expected");	
	
		// close the current object or array
	//	jp.nextToken();
		
		return object;
	}
	
	public void putIfAbsent(Map<String, Object> src, Map<String, Object> dst, String keySrc, String keyDst) {
		if (!dst.containsKey(keyDst) && src.get(keySrc) != null)
			dst.put(keyDst, src.get(keySrc));
	}
	
	@Override
	public Author deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		
	/*	if (jp.getCurrentToken() != JsonToken.START_OBJECT)
			throw new IOException("Wrong start token:"+jp.getCurrentToken());
		
		jp.nextToken();*/
		
		Map<String, Object> author = new HashMap<String, Object>();
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) parse(jp);
		
		putIfAbsent(map, author, JSON_AUID, PROPERTY_AUTHOR_ID);
		putIfAbsent(map, author, JSON_AUTHOR_URL, PROPERTY_AUTHOR_URL);
		putIfAbsent(map, author, JSON_CE_INDEXED_NAME, PROPERTY_FULL_NAME);
		putIfAbsent(map, author, JSON_CE_INNITIALS, PROPERTY_INITIALS);
		putIfAbsent(map, author, JSON_CE_SURNAME, PROPERTY_FAMILY_NAME);
		putIfAbsent(map, author, JSON_CE_GIVEN_NAME, PROPERTY_GIVEN_NAME);
		
		@SuppressWarnings("unchecked")
		Map<String, Object> prefered = (Map<String, Object>) map.get(JSON_PREFERRED_NAME);
		if (null != prefered) {
			putIfAbsent(prefered, author, JSON_CE_INDEXED_NAME, PROPERTY_FULL_NAME);
			putIfAbsent(prefered, author, JSON_CE_INNITIALS, PROPERTY_INITIALS);
			putIfAbsent(prefered, author, JSON_CE_SURNAME, PROPERTY_FAMILY_NAME);
			putIfAbsent(prefered, author, JSON_CE_GIVEN_NAME, PROPERTY_GIVEN_NAME);
		}
		
		return new Author(
				(String) author.get(PROPERTY_AUTHOR_ID),
				(String) author.get(PROPERTY_AUTHOR_URL),
				(String) author.get(PROPERTY_FULL_NAME),
				(String) author.get(PROPERTY_GIVEN_NAME),
				(String) author.get(PROPERTY_FAMILY_NAME),
				(String) author.get(PROPERTY_INITIALS));
	}
}

/*private void skipObject(JsonParser jp) throws JsonParseException, IOException {
		System.out.println("====== SKIP OBJECT =======");
		for(JsonToken token = jp.nextToken(); token != null && token != JsonToken.END_OBJECT; token = jp.nextToken()) 
			if (token == JsonToken.START_OBJECT)
				skipObject(jp);
	}
	
	private void parseObject(JsonParser jp, Map<String, String> map) throws IOException {
		System.out.println(jp.getCurrentToken());
		
		System.out.println("====== PARSE OBJECT =======");
		
		if (jp.getCurrentToken() != JsonToken.START_OBJECT)
			throw new IOException("[AuthorDeserializer] Wrong start token: "+jp.getCurrentToken());
		
		for(JsonToken token = jp.nextToken(); token != null && token != JsonToken.END_OBJECT; token = jp.nextToken()) {
			System.out.println(jp.getCurrentToken());
			
			if (token == JsonToken.FIELD_NAME) {
				String fieldName = jp.getCurrentName();
				String propertyName = null;
				if (fieldName.equals(JSON_AUID))
					propertyName = PROPERTY_AUTHOR_ID;
				else if (fieldName.equals(JSON_AUTHOR_URL))
					propertyName = PROPERTY_AUTHOR_URL;
				else if (fieldName.equals(JSON_CE_INDEXED_NAME))
					propertyName = PROPERTY_FULL_NAME;
				else if (fieldName.equals(JSON_CE_INNITIALS))
					propertyName = PROPERTY_INITIALS;
				else if (fieldName.equals(JSON_CE_SURNAME))
					propertyName = PROPERTY_FAMILY_NAME;
				else if (fieldName.equals(JSON_CE_GIVEN_NAME))
					propertyName = PROPERTY_GIVEN_NAME;
				
				// move to the next token
				token = jp.nextToken();
				System.out.println(jp.getCurrentToken());
				
				if (token == JsonToken.START_OBJECT) {
					if (fieldName.equals(JSON_PREFERRED_NAME)) 
						parseObject(jp, map);  // parse embedded object
					 else 
						skipObject(jp);	// ignore embedded object
				} else {
					// extract token value
					String value = jp.getValueAsString();
					
					if (propertyName != null && value != null && !map.containsKey(propertyName)) 
						map.put(propertyName, value);					
				}
				
			} else
				throw new IOException("[AuthorDeserializer] Wrong token: "+jp.getCurrentToken()+", the FIELD_NAME expected");
		}
	}
	
	public void putIfAbsent(Map<String, Object> src, Map<String, Object> dst, String key) {
		if (!src.containsKey(key) && )
	}
	
	@Override
	public Author deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		
		ObjectCodec codec = jp.getCodec();
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) codec.readValues(jp, Map.class);
		@SuppressWarnings("unchecked")
		Map<String, Object> result = (Map<String, Object>) map.get(JSON_PREFERRED_NAME);
		
		if (null != result) {
			
		}
		
		if (map.containsKey(JSON_AUID))
			result.put(PROPERTY_AUTHOR_ID, map.get(JSON_AUID));
		
		
		Map<String, String> map = new HashMap<String, String>();
		parseObject(jp, map);

		// pass end Object
		System.out.println("====== DONE =======");

		return new Author(
				map.get(PROPERTY_AUTHOR_ID),
				map.get(PROPERTY_AUTHOR_URL),
				map.get(PROPERTY_FULL_NAME),
				map.get(PROPERTY_GIVEN_NAME),
				map.get(PROPERTY_FAMILY_NAME),
				map.get(PROPERTY_INITIALS));
	}*/
	
	

/*	private static final String AUTHOR = "author";
	private static final ObjectMapper mapper = new ObjectMapper();
	private static final CollectionType collectionType =
	        TypeFactory
            .defaultInstance()
            .constructCollectionType(List.class, Author.class);
	
	@Override
	public Author deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException, JsonProcessingException {
		
		ObjectNode objectNode = mapper.readTree(jsonParser);
		JsonNode nodeAuthors = objectNode.get(AUTHOR);
	
		if (null == nodeAuthors 					// if no author node could be found
				|| !nodeAuthors.isArray() 			// or author node is not an array
				|| !nodeAuthors.elements().hasNext()) 	// or author node doesn't contain any authors
			return null;
		
		return mapper.reader(collectionType).readValue(nodeAuthors);
	}*/