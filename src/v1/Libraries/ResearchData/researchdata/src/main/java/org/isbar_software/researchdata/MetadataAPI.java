package org.isbar_software.researchdata;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

import com.sun.jersey.api.client.ClientResponse;

/**
 * RDA Medatada API interface
 * 
 * This library required org.isbar_software.rest library to be installed locally
 * 
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */
public class MetadataAPI extends RegistryAPI {
	
	protected static final String API_URL = BASE_URL + "getMetadata";
	
	/**
	 * The q parameter is normally the main query for the request.
	 * <p>
	 * All values of q must be URL-encoded. It is also recommended 
	 * that string values are surrounded by quotes.
	 * <p>
	 * By default, the field seperator is "OR" unless "AND" 
	 * is explicitly specified (see example usage #4).
	 * <p>
	 * Examples:
	 * <br>
	 * 1. Matching all records (i.e. unrestricted search):
	 * <br>
	 * q=*:*
	 * <p>
	 * 2. Matching records based on a particular field:
	 * <br>
	 * q=class:(collection)
	 * <p>
	 * 3. Matching multiple field values (OR):
	 * <br>
	 * q=class:(collection OR party)
	 * <p>
	 * 4. Matching on multiple fields (AND):
	 * <br>
	 * q=class:(collection) AND group_search("Monash")
	 * <p>
	 * 5. Wildcard searching on string fields:
	 * <br>
	 * q=title_search:(*survey*)
	 */
	
	public String query; 
	
	/**
	 * Restricts the metadata fields which are returned by the search query.
	 * <p>
	 * By default, the following fields are returned:
	 * <br>
	 * id,key,slug,display_title,class
	 * <p>
	 * Examples:
	 * <br>
	 * 1. Select only keys and record titles:
	 * <br>
	 * ?fl=key,display_title
	 * <p>
	 * 2. Select all information about related objects:
	 * <br>
	 * ?fl=key,related_object_*
	 * <p>
	 * 2. Select all fields which are indexed:
	 * <br>
	 * ?fl=*
	 */
	
	public String fields;
	
	/**
	 * Sorting can be done on any indexed field which only has single values. 
	 * Sort can be either: desc or asc.
	 * <p>
	 * Note: Fields which are indexed as string or integers are sortable. 
	 * Other field types may produce unexpected results.
	 * <p>
	 * Examples:
	 * <br>
	 * 1. Sort based on key in ascending order:
	 * <br>
	 * q=*:*&sort=key asc
	 * <p>
	 * 2. Sort based on update time in descending order (most recent first):
	 * <br>
	 * q=key:*:*&sort=update_timestamp desc
	 */
	
	public String sort;
	
	/**
	 * This parameter is used to paginate results from a query.
	 * <p>
	 * When specified, it indicates the offset in the complete result set 
	 * for where the set of returned documents should begin and end.
	 * <p>
	 * By default, results start at 0 with 20 rows displayed. 
	 * That is, by default only 20 rows are displayed in response to a search query.
	 * <p>
	 * Examples:
	 * <br>1. Display the first 20 records with a key starting AODN:
	 * <br>q=key:AODN*
	 * <p>
	 * 2. Display the next 20 records with a key starting AODN:
	 * <br>q=key:AODN*&start=20
	 * <p>
	 * 3. Display 100 records starting from number 1000:
	 * <br>q=*:*&start=1000&rows=100
	 */
	
	public Integer start;
	public Integer rows;
	
	/**
	 * Faceting mode is analogous to a GROUP BY statement in SQL.
	 * <p>
	 * When specified, faceting mode will retrict the resultset based on your query 
	 * and then display the field value and count of records matching that field value.
	 * <p>
	 * Examples:
	 * <br>1. Display the number of records for each group, where the group has 10 or more records.
	 * <br>?facet=on&facet.field=group&facet.mincount=10
	 * <p>
	 * 2. Display the number of records for each group (collections only)
	 * <br>?facet=on&facet.field=group&q=class:(collection)
	 */

	public String facetField;
	public Integer facetMinCount;
	
	/**
	 * Query function. Will perform an query for specyfed data
	 * @return {@code LinkedHashMap<String, Object>}
	 * @throws UnsupportedEncodingException
	 */
	public LinkedHashMap<String, Object> Query() throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		if (query != null && !query.isEmpty()) {
			sb.append("q=");
			sb.append(URLEncoder.encode(query, "UTF-8"));
		}
		
		if (fields != null && !fields.isEmpty()) {
			if (!sb.toString().isEmpty())
				sb.append("&");
			sb.append("fl=");
			sb.append(fields);
		}
		
		if (sort != null && !sort.isEmpty()) {
			if (!sb.toString().isEmpty())
				sb.append("&");
			sb.append("sort=");
			sb.append(URLEncoder.encode(sort, "UTF-8"));
		}
		
		if (start != null){
			if (!sb.toString().isEmpty())
				sb.append("&");
			sb.append("start=");
			sb.append((int)start);
		}
		
		if (rows != null){
			if (!sb.toString().isEmpty())
				sb.append("&");
			sb.append("rows=");
			sb.append((int)rows);
		}
		
		if (facetField != null && !facetField.isEmpty()) {
			if (!sb.toString().isEmpty())
				sb.append("&");
			sb.append("facet=on&facet.field=");
			sb.append((int)rows);
			
			if (facetMinCount != null) {
				sb.append("&facet.mincount=");
				sb.append(facetMinCount);
			}
		}
		
		String url = API_URL;
		if (!sb.toString().isEmpty())
			url += "?" + sb.toString();
		
		//System.out.println(url);
		
		ClientResponse response = Invoke(OperationTypes.GET, url);
		int result = response.getStatus();
		if (result == 200) {
			String json = response.getEntity( String.class );
		
			try {
				return fromJson(json, linkedHashMapTypeReference);
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return null;		
	}	
	
	/**
	 * Query specified filed
	 * @param field String containing a field
	 * @return {@code Set<String>}
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings("unchecked")
	public Set<String> QueryField(final String field) throws UnsupportedEncodingException {
		LinkedHashMap<String, Object> result = Query();
		
		HashSet<String> r = null;
		String status = (String) result.get("status");
		if (status.equals("success")) {
			Object message = result.get("message");
			//LinkedHashMap<String, Object> message = (LinkedHashMap<String, Object>) ;
			if (null != message && message instanceof LinkedHashMap<?, ?>) {
				int numFound = (Integer) ((LinkedHashMap<String, Object>) message).get("numFound");
				if (numFound > 0) {
					Object docs = ((LinkedHashMap<String, Object>) message).get("docs");
					if (null != docs && docs instanceof ArrayList<?>)
						for (int n = 0; n < ((ArrayList<Object>) docs).size(); ++n) {
							Object doc = (LinkedHashMap<String, Object>) ((ArrayList<Object>) docs).get(n);
							if (null != doc && doc instanceof LinkedHashMap<?,?>) {
								Object fields = ((LinkedHashMap<String, Object>)doc).get(field);
								if (null != fields && fields instanceof ArrayList<?>) 
									for (int i = 0; i < ((ArrayList<String>)fields).size(); ++i) {
										String f = ((ArrayList<String>)fields).get(i);
										if (f != null && !f.isEmpty()) {
											if (null == r)
												r = new HashSet<String>();
											
											r.add(f);
										}
									}
							}
						}								
				}
			}
		}
		
		return r;
	}
	
	/**
	 * Query field with filter on other field
	 * @param field String containing specified field
	 * @param filterField String containing filter field
	 * @param filterData  String containing filter data
	 * @return {@code Set<String>}
	 * @throws UnsupportedEncodingException
	 */
	@SuppressWarnings("unchecked")
	public Set<String> QueryField(final String field, final String filterField, final String filterData) 
			throws UnsupportedEncodingException {
		LinkedHashMap<String, Object> result = Query();
		
		HashSet<String> r = null;
		String status = (String) result.get("status");
		if (status.equals("success")) {
			Object message = result.get("message");
			//LinkedHashMap<String, Object> message = (LinkedHashMap<String, Object>) ;
			if (null != message && message instanceof LinkedHashMap<?, ?>) {
				int numFound = (Integer) ((LinkedHashMap<String, Object>) message).get("numFound");
				if (numFound > 0) {
					Object docs = ((LinkedHashMap<String, Object>) message).get("docs");
					if (null != docs && docs instanceof ArrayList<?>)
						for (int n = 0; n < ((ArrayList<Object>) docs).size(); ++n) {
							Object doc = (LinkedHashMap<String, Object>) ((ArrayList<Object>) docs).get(n);
							if (null != doc && doc instanceof LinkedHashMap<?,?>) {
								Object filter = ((LinkedHashMap<String, Object>)doc).get(filterField);
								if (null != filter && filter instanceof String) 
									if (((String)filter).equals(filterData)) {
										Object fields = ((LinkedHashMap<String, Object>)doc).get(field);
										if (null != fields && fields instanceof ArrayList<?>) 
											for (int i = 0; i < ((ArrayList<String>)fields).size(); ++i) {
												String f = ((ArrayList<String>)fields).get(i);
												if (f != null && !f.isEmpty()) {
													if (null == r)
														r = new HashSet<String>();
													
													r.add(f);
												}
											}										
									}
							}
						}								
				}
			}
		}
		
		return r;
	}
}
