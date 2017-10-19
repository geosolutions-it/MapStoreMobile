package it.geosolutions.geocollect.model.http;

import it.geosolutions.geocollect.model.source.XDataType;

import java.util.HashMap;

/**
 * container for a vector layer
 * 
 * @author Robert Oehler
 */

public class VectorLayer {
	
	public final static String URL      = "URL";
	public final static String VERSION  = "VERSION";
	public final static String STYLE    = "STYLE";
	
	public String name;
	public String url;
	public int version;
	public String styleFile;
	
	public VectorLayer(String name, String url, int version, String styleFile) {
		super();
		this.name = name;
		this.url = url;
		this.version = version;
		this.styleFile = styleFile;
	}
	
	public static HashMap<String, XDataType> getDatabaseSchema(){
		
		HashMap<String, XDataType> map = new HashMap<String, XDataType>();
		
		map.put(URL , XDataType.text);
		map.put(VERSION, XDataType.integer);
		map.put(STYLE, XDataType.text);
		
		return map;
	}
}
