package org.grants.crossref;

import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

/**
 * Class to parse CrossRef Date object
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */
public class CrossRefDateDeserializer  extends JsonDeserializer<Date> {
	
	private static final String NODE_TIMESTAMP = "timestamp";
	private static final String NODE_DATE_PARTS = "date-parts";

	@Override
	public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException, JsonProcessingException {
		ObjectCodec oc = jsonParser.getCodec();
		JsonNode node = oc.readTree(jsonParser);
		
		final JsonNode nodeStamp = node.get(NODE_TIMESTAMP);
		if (null != nodeStamp) 
			return new Date(nodeStamp.asLong(0));

		final JsonNode nodeParts = node.get(NODE_DATE_PARTS);
		if (null != nodeParts && nodeParts.isArray()) {
			for (final JsonNode nodePart : nodeParts) {
				if (nodePart.isArray()) {
					switch (nodePart.size()) {
					case 1:
						return new GregorianCalendar(nodePart.get(0).asInt(), 0, 1).getTime();
					case 2:
						return new GregorianCalendar(nodePart.get(0).asInt(), nodePart.get(1).asInt()-1, 1).getTime();
					case 3:
						return new GregorianCalendar(nodePart.get(0).asInt(), nodePart.get(1).asInt()-1, nodePart.get(2).asInt()).getTime();
					}
				}			
			}
		}

		return null;
	}
}
