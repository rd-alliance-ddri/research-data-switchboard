package org.grants.utils.graph2json;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class Node {
	private String id;
	private String key;
	private String name;
	private String type;
	
	private List<Property> properties;
	private List<Relationship> relationships;
	
	public String getType() {
		return type;
	
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public List<Property> getProperties() {
		return properties;
	}
	
	public void setProperties(List<Property> properties) {
		this.properties = properties;
	}
	
	public void addProperty(Property property) {
		if (null == this.properties)
			this.properties = new ArrayList<Property>();
		
		this.properties.add(property);
	}

	public void addData(String name, String value) {
		addProperty(new Property(name, value));
	}
	
	public List<Relationship> getRelationships() {
		return relationships;
	}
	
	public void setRelationships(List<Relationship> relationships) {
		this.relationships = relationships;
	}
	
	public void addRelationship(Relationship relatipnship) {
		if (null == this.relationships)
			this.relationships = new ArrayList<Relationship>();
		
		this.relationships.add(relatipnship);
	}
	
	public void addRelationship(String type, String key) {
		addRelationship(new Relationship(type, key));
	}
	
	@Override
	public String toString() {
		return "Node [type=" + type + ", key=" + key + ", id=" + id + ", name="
				+ name + ", properties=" + properties + ", relationships=" + relationships + "]";
	}	
}
