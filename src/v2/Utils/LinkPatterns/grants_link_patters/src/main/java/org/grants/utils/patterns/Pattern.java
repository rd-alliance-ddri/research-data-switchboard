package org.grants.utils.patterns;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

public class Pattern {
	private static final String DELEMITER = "/";
	private static final String DOT = "\\.";
	private static final String ANY = "*";
	
	private String link;
	private String protocol;
	private String authority;
	private String host;
	private String[] path;
	
	private int count = 1;
	
	public Pattern(final String link) throws MalformedURLException {
		this.link = link;
		
		URL url = new URL(link);
		
		protocol = url.getProtocol();
		authority = url.getAuthority();
		path = url.getFile().split(DELEMITER);
		if (path.length > 1)
			path[path.length-1] = ANY;
		
		
		String[] h = url.getHost().split(DOT);
		if (h.length > 2) { 
			String part = h[h.length - 2];
			int length = 2;
			if (part.equals("edu") || 
					part.equals("com") || 
					part.equals("ac"))
				length = 3;
			host = StringUtils.join(Arrays.copyOfRange(h, h.length - length, h.length), ".");
		} else
			host = url.getHost();
	}
	
	public String getLink() {
		return link;
	}
	
	public String getProtocol() {
		return protocol;
	}	
	
	public String getAuthority() {
		return authority;
	}
	
	public String[] getPath() {
		return path;
	}
	
	public String getHost() {
		return host;
	}
	
	public String getPattern() {
		return protocol + "://" + authority + StringUtils.join(path, DELEMITER);
	}
	
	public int getCount() {
		return count;
	}
	
	public boolean combineWith(Pattern pattern) {
		if (protocol.equals(pattern.protocol) &&
			authority.equals(pattern.authority) &&
			path.length == pattern.path.length) {
			
			for (int i = 0; i < path.length - 1; ++i) 
				if (!path[i].equals(pattern.path[i]))
					return false;
			
			++count;
			
			return true;
		} else
			return false;
	}
}
