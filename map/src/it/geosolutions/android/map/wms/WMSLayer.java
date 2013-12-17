package it.geosolutions.android.map.wms;

import java.util.HashMap;
/**
 * Abstraction of WMSLayer 
 * @author  Lorenzo Natali (lorenzo.natali@geo-solutions.it) 
 */
public class WMSLayer {
	/**
	 * The name of the layer
	 */
	private String name;
	
	/**
	 * Parameters like style and cql_filter
	 */
	public HashMap<String,String> baseParams;
	
	/**
	 * The Source of the WMSLayer
	 */
	private WMSSource source;
	
	public void setName(String name) {
		this.name = name;
	}

	public WMSSource getSource() {
		return source;
	}
	public void setSource(WMSSource source) {
		this.source = source;
	}
	public WMSLayer(WMSSource source, String name){
		this.source = source;
		this.name=name;
	}
	public String getName(){
		return name;
	}
}
