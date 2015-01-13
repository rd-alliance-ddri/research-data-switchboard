package org.grants.scopus;

public enum ViewType {
	viewTypeStandard("STANDARD"),
	viewTypeComplete("COMPLETE");
	
	private String viewName;
	
	private ViewType(String viewName) {
		this.viewName = viewName;
    }
     
	@Override
    public String toString(){
		return viewName;
    } 
}
