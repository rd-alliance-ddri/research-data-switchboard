package org.grants.google.cache2;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GoogleUtils {
	public static final ObjectMapper mapper = new ObjectMapper();
	public static final TypeReference<Map<String, Object>> refPagemap = new TypeReference<Map<String, Object>>() {};

	public static final String METDATA_DC_TITLE = "dc.title";
	/**
	 * Function to return metatag information from pagemap
	 * @param pagemap Map<String, Object>
	 * @param field field name (dc.title for dublin core title)
	 * @return metatag - String
	 */
	public static String getMetatag(Map<String, Object> pagemap, String field) { 
		if (null != pagemap) {
			 @SuppressWarnings("unchecked")
			 List<Object> metatags = (List<Object>) pagemap.get("metatags");
			 if (null != metatags && metatags.size() > 0) {
				 @SuppressWarnings("unchecked")
				 Map<String, Object> metatag = (Map<String, Object>) metatags.get(0);
				 if (null != metatag) {
					 return (String) metatag.get(field);
				 }
			 }
		}
		
		return null;
	}
	
	/**
	 * Function to parse pagemap into Java Map
	 * @param filePagemap
	 * @return pagemap - Map<String, Object>
	 */
	public static Map<String, Object> getPagemap(File filePagemap) {
		try {
			return mapper.readValue(filePagemap, refPagemap);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Function to return metatag information from a file
	 * @param filePagemap a file, containing pagemap metadata
	 * @param field field name (dc.title for dublin core title)
	 * @return metatag - String
	 */
	public static String getMetatag(File filePagemap, String field) { 
		return getMetatag(getPagemap(filePagemap), field);
	}
	
	
	
}
