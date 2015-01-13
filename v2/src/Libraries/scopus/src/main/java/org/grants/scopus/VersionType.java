package org.grants.scopus;

public enum VersionType {
	versionTypeFacetexpand("facetexpand"), 
	versionTypeAllexpand("allexpand"), 
	versionTypeNew("new");
	
	private String versionName;
	
	private VersionType(String versionName) {
		this.versionName = versionName;
    }
     
	@Override
    public String toString(){
		return versionName;
    } 
}
