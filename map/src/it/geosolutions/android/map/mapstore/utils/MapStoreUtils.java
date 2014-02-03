/*
 * GeoSolutions Android map Library - Digital field mapping on Android based devices
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
package it.geosolutions.android.map.mapstore.utils;

import it.geosolutions.android.map.MapsActivity;
import it.geosolutions.android.map.geostore.model.Resource;
import it.geosolutions.android.map.geostore.utils.GeoStoreClient;
import it.geosolutions.android.map.mapstore.model.MapStoreConfiguration;
import it.geosolutions.android.map.mapstore.model.MapStoreLayer;
import it.geosolutions.android.map.mapstore.model.MapStoreSource;
import it.geosolutions.android.map.model.Layer;
import it.geosolutions.android.map.utils.ProjectionUtils;
import it.geosolutions.android.map.wms.WMSLayer;
import it.geosolutions.android.map.wms.WMSSource;

import java.util.ArrayList;
import java.util.HashMap;

import org.mapsforge.core.model.GeoPoint;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
/**
 * Utility class for MapStore configuration reading and management
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public class MapStoreUtils {
	public final static  String WMS_PTYPE ="gxp_wmssource";

	/**
	 * Creates an async task to get and read a mapstore configuration from the geostore url.
	 * 
	 * @param geoStoreUrl
	 * @param resource
	 * @param mapsActivity
	 * @return
	 */
	public static WMSLayer loadMapStoreConfig(final String geoStoreUrl,final Resource resource, final MapsActivity mapsActivity) {
		if(resource ==null || geoStoreUrl== null){
			//TODO notify
			return null;
			
		}
		
		AsyncTask<String, String, MapStoreConfiguration> task= new MapStoreConfigTask(resource.id, geoStoreUrl){

			@Override
			protected void onPostExecute(MapStoreConfiguration result) {
				Log.d("MapStore",result.toString());
				//call the loadMapStore config on the Activity
				mapsActivity.overlayManager.loadMapStoreConfig(result);
				GeoPoint p = getPoint(result);
				if(p!=null){
					mapsActivity.setPosition(p, (byte)result.map.zoom);
				}
			}
		};
		task.execute("");
		return null;
				
	}

	/**
	 * Check if the needed fields are present
	 * @param ms the configuration to check
	 * @return true if the configuration contains layers and sources
	 */
	public static boolean  isValidConfiguration(MapStoreConfiguration ms) {
		if(ms==null) return false;
		if(ms.map==null)return false;
		if(ms.map.layers==null)return false;
		if(ms.sources ==null)return false;
		return true;
	}
	/**
	 * Create list if <WMSLayer> from a <MapStoreConfiguration> object 
	 * @param result
	 * @return
	 */
	public static ArrayList<Layer>  buildWMSLayers(MapStoreConfiguration result) {
		ArrayList<Layer> layers = new ArrayList<Layer>();
		if(result != null){
			HashMap<String,WMSSource> sources= new HashMap<String,WMSSource>();
			if(result.sources==null) return layers;
			for(String sourceID :result.sources.keySet()){
				MapStoreSource s = result.sources.get(sourceID);
				if(s!=null && isWMS(s, result.defaultSourceType)){
					sources.put(sourceID,mapStoreSource2WMSSource(s,result.defaultSourceType));
				}
			} 
			for(MapStoreLayer l : result.map.layers){
				WMSLayer ll = mapStoreLayer2Layer(l, sources);
				//don't add layers without Source
				if(ll== null) continue;
				if(ll.getSource()==null){
					result.map.layers.remove(l);
					Log.w("MapStore","layer not added because the source is missing or not supported:"+ll.getName());
				}else{
					layers.add(ll);
				}
			}
			Log.d("MapStore","converted layers:" + layers.size());
		}
		return layers;
		
	}
	
	/**
	 * Convert a MapStoreSource in a WMSSource
	 * @param mss
	 * @param defaultSourceType
	 * @return
	 */
	public static WMSSource mapStoreSource2WMSSource(MapStoreSource mss, String defaultSourceType){
		boolean isWMS = isWMS(mss, defaultSourceType);
		if(isWMS)	{
			WMSSource source = new WMSSource(mss.url);
			if(mss.version != null){
				source.baseParams.put("version",mss.version);
			}
			if(mss.version != null){
				source.baseParams.put("version",mss.version);
			}
			//Override after all
			if(mss.layerBaseParams != null){
				for(String k:mss.layerBaseParams.keySet()){
					source.baseParams.put(k, mss.layerBaseParams.get(k).toString());
				}
			}
			return source;
		}
		return null;
	}

	/**
	 * Check if the ptype is a wms type. The default source type is managed to avoid missing ptypes for
	 * some configurations.
	 * @param mss the MapStore configuration 
	 * @param defaultSourceType if ptype is null, the default ptype should be the wms one. in this case the layer is wms by default.
	 * @return
	 */
	private static boolean isWMS(MapStoreSource mss, String defaultSourceType) {
		boolean isWMS =WMS_PTYPE.equals(mss.ptype) || (mss.ptype == null && WMS_PTYPE.equals(defaultSourceType));
		return isWMS;
	}
	/**
	 * Put the proper layer configurations from a MapStore's one. Get the <WMSSource> from the map passed as parameter and
	 * create the <WMSLayer> using it.
	 * (The MapStore configuration contains the name of the source and the map passed as second parameter
	 *  maps the names and the already converted sources )
	 * Set base params properly
	 * @param mlayer the <MapStoreLayer> to convert
	 * @param sources a map of WMSSources by name
	 * @return
	 */
	public static WMSLayer mapStoreLayer2Layer(MapStoreLayer mlayer,HashMap<String,WMSSource> sources){
		if (sources==null){
			Log.e("MapStore","unable any source for layer " + mlayer.name);
			return null;
		}
		if(!sources.containsKey(mlayer.source)) {
			Log.w("MapStore","unable to find the source for layer " + mlayer.name);
			return null;
		}
		WMSLayer layer = new WMSLayer(sources.get(mlayer.source), mlayer.name);
		layer.setTitle(mlayer.title);
		layer.setGroup(mlayer.group);
		//TODO Now skip tiled option
		layer.setVisibility(mlayer.visibility);
		layer.setTiled(mlayer.tiled!=null?mlayer.tiled:false);
		//create base parameters
		HashMap<String, String> baseParams = new HashMap<String, String>();
		if(mlayer.format != null) baseParams.put("format", mlayer.format);
		if(mlayer.styles != null) baseParams.put("styles",mlayer.styles );
		if(mlayer.buffer != null) baseParams.put("buffer",mlayer.buffer.toString() );
		if(mlayer.transparent != null) baseParams.put("transparent",mlayer.transparent );
		layer.setBaseParams(baseParams);
		return layer;
		
		
	}
	
	
}
