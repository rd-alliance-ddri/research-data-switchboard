package org.grants.scopus.response;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;


public class AuthorArrayDeserializer extends JsonDeserializer<List<Author>> {

	private static final String AUTHOR = "author";
	private static final ObjectMapper mapper = new ObjectMapper();
	private static final CollectionType collectionType =
	        TypeFactory
            .defaultInstance()
            .constructCollectionType(List.class, Author.class);
	
	@Override
	public List<Author> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException, JsonProcessingException {
		
		ObjectNode objectNode = mapper.readTree(jsonParser);
		JsonNode nodeAuthors = objectNode.get(AUTHOR);
	
		if (null == nodeAuthors 					// if no author node could be found
				|| !nodeAuthors.isArray() 			// or author node is not an array
				|| !nodeAuthors.elements().hasNext()) 	// or author node doesn't contain any authors
			return null;
		
		return mapper.reader(collectionType).readValue(nodeAuthors);
	}
}