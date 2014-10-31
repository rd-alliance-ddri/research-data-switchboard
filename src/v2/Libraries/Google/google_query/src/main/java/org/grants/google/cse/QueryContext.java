package org.grants.google.cse;

public class QueryContext {
	private String title;
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(final String title) {
		this.title = title;
	}
	
	@Override
	public String toString() {
		return "QueryContext [title=" + title + "]";
	}
}
