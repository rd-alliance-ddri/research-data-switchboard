package org.grants.google.cse;

import java.util.List;

public class QueryInfo {
	private List<QueryPage> nextPage;
	private List<QueryPage> request;
	
	public List<QueryPage> getNextPage() {
		return nextPage;
	}
	
	public void setNextPage(List<QueryPage> nextPage) {
		this.nextPage = nextPage;
	}
	 
	public List<QueryPage> getRequest() {
		return request;
	}
	
	public void setRequest(List<QueryPage> request) {
		this.request = request;
	}
	
	@Override
	public String toString() {
		return "QueryInfo [nextPage=" + nextPage + 
				", request=" + request + "]";				
	}
}
