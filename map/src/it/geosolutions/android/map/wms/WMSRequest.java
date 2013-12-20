/*
 * GeoSolutions map - Digital field mapping on Android based devices
 * Copyright (C) 2013  GeoSolutions (www.geo-solutions.it)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.geosolutions.android.map.wms;

import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.util.Log;
/**
 * Class that manages a WMSSource request from the Same Source.
 * Manages Style parameter for the compund request but doesn't 
 * manages the CQL_FILTER parameter yet. 
 * NOTE: The map is generated as lower case keys.
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public class WMSRequest {
	private WMSSource source ;
	

	private ArrayList<WMSLayer> layers;
	private HashMap<String,String> params = new HashMap<String,String>();
	private HashMap<String, String> currentParams;
	public class PARAMS{
		public static final String STYLES = "styles";
		public static final String CQL_FILTER = "cql_filter";
		public static final String LAYERS = "layers";
	}
	public WMSRequest(WMSSource source,ArrayList<WMSLayer> layers) {
		this.source = source;
		this.layers = layers;
		refreshParams();
	}
	
	/**
	 * Creates a request from source,+layers+ other parameters
	 * The Source base parameters can be overidden by the layers
	 * base parameters and than the passed parameters.
	 * @param params
	 */
	@SuppressLint("DefaultLocale")
	public HashMap<String, String> getParams(HashMap<String,String> params){
		HashMap<String, String> newParams = new HashMap<String, String>();
		newParams.putAll(this.currentParams);
		for(String k : params.keySet()){
			newParams.put(k.toLowerCase(), params.get(k));
		}
		return newParams;
	}
	/**
	 * Get a URL for a WMS request with current custom parameters
	 * @param params
	 */
	public URL getURL(HashMap<String,String> params){
		HashMap<String, String> newParams = getParams(params);
		URL url = null;
		try {
			StringWriter sw =new StringWriter();
			sw.append(source.getUrl().split("\\?")[0]);
			sw.append("?");
			for(String paramName : newParams.keySet()){
				try {
					sw.append(paramName + "="+ URLEncoder.encode(newParams.get(paramName), "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					Log.e("WMS","Unsupported encoding");
				}
				sw.append("&");
			}
			url = new URL(sw.toString());
			return url;
		} catch (MalformedURLException e2) {
			Log.e("WMS","malformed url:" + url );
		}
		return null;
	}
	
	/**
	 * Recreates parameters from source and layers.
	 * @return
	 */
	public HashMap<String, String> refreshParams() {
		currentParams = new HashMap<String,String>();
		//add the base parameters from the source
		if(layers==null || source==null) return null;
		HashMap<String, String> bp = source.baseParams;
		if(bp !=null ){
			for(String k : bp.keySet()){
				currentParams.put(k.toLowerCase(), source.baseParams.get(k));
			}
		}

		int count = 0;
		int size = layers.size();
		StringWriter styleStringWriter = new StringWriter();
		styleStringWriter.append("");
		StringWriter layerStringWriter = new StringWriter();
		layerStringWriter.append("");
		for(WMSLayer l : layers){
			//true if iterating the last layer in of the array
			boolean last = count >= size - 1;
			bp = l.baseParams;
			layerStringWriter.append(l.getName());
			if(!last){
				layerStringWriter.append(",");
			}
			if(bp != null){
				//Special management for style
				concatenateParameter(PARAMS.STYLES,styleStringWriter, bp, last);
				//special management for cql_filter
				if(l.baseParams.containsKey(PARAMS.CQL_FILTER)){
					//TODO skip it for now
				}
				//manage other paramters
				for(String paramName : l.baseParams.keySet() ){
					if(paramName == null) continue;
					if(!PARAMS.STYLES.equalsIgnoreCase(paramName) && !PARAMS.CQL_FILTER.equalsIgnoreCase(paramName)){
							currentParams.put(paramName.toLowerCase(), l.baseParams.get(paramName));
					}
				}
			}
			
			count++;
		}
		currentParams.put(PARAMS.STYLES,styleStringWriter.toString());
		currentParams.put(PARAMS.LAYERS,layerStringWriter.toString());
		Log.v("WMS","created request params for layer:"+layerStringWriter.toString());
		
		return currentParams;
	}

	/**
	 * Concatenate layers and add a comma at the end if it last is false
	 * @param param the param name 
	 * @param styleStringWriter the string writer to create the final string
	 * @param l the <WMSLayer> 
	 * @param last if false, add a comma at the end
	 */
	private void concatenateParameter(String param, StringWriter styleStringWriter,
			HashMap<String,String> baseParams, boolean last) {
		if(baseParams.containsKey(param)){
			styleStringWriter.append(baseParams.get(param));
		}if (!last){
			styleStringWriter.append(",");
		}
	}
}
