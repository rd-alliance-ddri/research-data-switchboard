package org.grants.scopus.deserilaize;

import java.io.IOException;
import java.util.ArrayList;

import org.grants.scopus.response.Author;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.google.common.collect.Lists;

public class AuthorsDeserealizer  extends JsonDeserializer<Author[]>{
	private static final String JSON_AUTHOR = "author";
	
	@Override
	public Author[] deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		if (jp.getCurrentToken() != JsonToken.START_OBJECT)
			throw new IOException("Wrong author object start token: "+jp.getCurrentToken());
		if (jp.nextToken() != JsonToken.FIELD_NAME) 
			throw new IOException("Wrong author field token: "+jp.getCurrentToken());
		if (!jp.getCurrentName().equals(JSON_AUTHOR))
			throw new IOException("Wrong author field: "+jp.getCurrentName());
		if (jp.nextToken() != JsonToken.START_ARRAY)
			throw new IOException("Wrong author array start token: "+jp.getCurrentToken());
		jp.nextToken();
		
		ArrayList<Author> list = Lists.newArrayList(jp.readValuesAs(Author.class));
		
		if (jp.getCurrentToken() != JsonToken.END_ARRAY)
			throw new IOException("Wrong author array end token: "+jp.getCurrentToken());
		if (jp.nextToken() != JsonToken.END_OBJECT)
			throw new IOException("Wrong author object end token: "+jp.getCurrentToken());
		
		return list.toArray(new Author[list.size()]);
	}
}
