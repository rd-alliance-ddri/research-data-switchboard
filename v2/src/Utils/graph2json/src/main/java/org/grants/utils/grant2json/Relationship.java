
package org.grants.utils.grant2json;

public class Relationship {
	private String type;
	private String key;
	
	public Relationship() {
		
	}
	
	public Relationship(String type, String key) {
		this.type = type;
		this.key = key;
	}
	
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

	@Override
	public String toString() {
		return "Relationship [type=" + type + ", key=" + key + "]";
	}	
}
