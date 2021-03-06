package org.grants.orcid;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

/**
 * History:
 * 
 * 1.0.11 : added clientId
 * 
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */

public class Source {
	private OrcidIdentifier identifier;
	private OrcidIdentifier clientId;
	private String name;
	
	@JsonProperty("source-orcid")
	public OrcidIdentifier getIdentifier() {
		return identifier;
	}
	
	@JsonProperty("source-orcid")
	public void setIdentifier(OrcidIdentifier identifier) {
		this.identifier = identifier;
	}
	
	@JsonProperty("source-client-id")
	public OrcidIdentifier getClientId() {
		return clientId;
	}

	@JsonProperty("source-client-id")
	public void setClientId(OrcidIdentifier clientId) {
		this.clientId = clientId;
	}	
	
	@JsonProperty("source-name")
	public String getName() {
		return name;
	}
	
	@JsonProperty("source-name")
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Source [identifier=" + identifier 
				+ ", clientId=" + clientId
				+ ", name=" + name + "]";
	}
}
