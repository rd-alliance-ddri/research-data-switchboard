package org.grants.scopus;

public enum AuthorFormatType {
	authorFormatTypeScource("source");
	
	private String authorFormatName;
	
	private AuthorFormatType(String authorFormatName) {
		this.authorFormatName = authorFormatName;
    }
     
	@Override
    public String toString(){
		return authorFormatName;
    } 
}
