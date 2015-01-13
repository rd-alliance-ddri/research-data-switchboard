package org.grants.scopus;

public enum ContentType {
	contentTypeCore("core"), 
	contentTypeDummy("dummy"), 
	contentTypeAll("all");
	
	private String contentName;
	
	private ContentType(String contentName) {
		this.contentName = contentName;
    }
     
	@Override
    public String toString(){
		return contentName;
    } 
}
