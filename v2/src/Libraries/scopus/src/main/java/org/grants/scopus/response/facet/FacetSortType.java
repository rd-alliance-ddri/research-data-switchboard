package org.grants.scopus.response.facet;

public enum FacetSortType {
	na, // Modifier name, ascending
	fd, // Modifier frequency, descending
	fdna; // Modifier frequency descending, secondary sort through unity by name, ascending
}
