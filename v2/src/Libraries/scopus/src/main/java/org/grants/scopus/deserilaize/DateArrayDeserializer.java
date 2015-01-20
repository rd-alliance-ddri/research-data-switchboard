package org.grants.scopus.deserilaize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.grants.scopus.response.Date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class DateArrayDeserializer extends JsonDeserializer<List<Date>> {
	//private static final TypeReference ref = new TypeReference<List<Date>>() {};
	
	@Override
	public List<Date> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException, JsonProcessingException {
		
		// If we have an string, just create an Date object from it
		if (jsonParser.getCurrentToken() == JsonToken.VALUE_STRING) {
			List<Date> list = new ArrayList<Date>();
			list.add(new Date(false, jsonParser.getValueAsString()));
			return list;
		} else if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
			// check that array is not empty
			if (jsonParser.nextToken() != JsonToken.END_ARRAY) {
				ObjectCodec codec = jsonParser.getCodec();
				Iterator<Date> dates = codec.readValues(jsonParser, Date.class);
				List<Date> list = new ArrayList<Date>();
				while (dates.hasNext())
					list.add(dates.next());
				
				// jsonParser.nextToken(); // seems not to be required
				return list;
			}
		}
		
	//	System.out.println(jsonParser.getCurrentToken() + "," + jsonParser.getText());
		
		return null;
	}
}
