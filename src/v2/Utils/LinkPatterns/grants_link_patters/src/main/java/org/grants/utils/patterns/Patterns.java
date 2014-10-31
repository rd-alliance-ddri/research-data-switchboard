package org.grants.utils.patterns;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

public class Patterns {
	private Set<String> links = new HashSet<String>();
	private List<Pattern> patterns = new ArrayList<Pattern>();
	
	private JAXBContext jaxbContext;
	private Unmarshaller jaxbUnmarshaller;
	
	
	public Patterns() throws JAXBException {
		jaxbContext = JAXBContext.newInstance(Page.class);
		jaxbUnmarshaller = jaxbContext.createUnmarshaller();
	}
	
	public Set<String> getLinks() {
		return links;
	}
	
	public List<Pattern> getPatterns() {
		return patterns;
	}
	
	public Map<String, Pattern> getUniquePatterns() {
		Map<String, Pattern> map = new HashMap<String, Pattern>();
		
		for (Pattern pattern : patterns) {
			Pattern selected = map.get(pattern.getHost());
			if (null == selected || selected.getCount() < pattern.getCount())
				map.put(pattern.getHost(), pattern);				
		}	
		
		return map;
	}
		
	public void loadLinks(final String path) {
		File[] files = new File(path).listFiles();
		for (File file : files) 
			if (!file.isDirectory())
			{
				try {
					Page page = (Page) jaxbUnmarshaller.unmarshal(file);
					if (page != null) 
						links.add(page.getLink());
				} catch (JAXBException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	}
	
	public void identifyPatterns() {
		for (String link : links) {
			try {
				addPattern(new Pattern(link));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void addPattern(Pattern pattern) {
		for (Pattern p : patterns) 
			if (p.combineWith(pattern))
				return;
			
		patterns.add(pattern);	
	}
}
