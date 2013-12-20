package it.geosolutions.android.map.mapstore.utils;

import java.util.ArrayList;
import java.util.HashMap;

import org.mapsforge.core.model.GeoPoint;

import it.geosolutions.android.map.MapsActivity;
import it.geosolutions.android.map.geostore.model.Attribute;
import it.geosolutions.android.map.geostore.model.GeoStoreAttributeTypeAdapter;
import it.geosolutions.android.map.geostore.model.GeoStoreResourceTypeAdapter;
import it.geosolutions.android.map.geostore.model.Resource;
import it.geosolutions.android.map.geostore.utils.GeoStoreClient;
import it.geosolutions.android.map.mapstore.model.MapStoreConfiguration;
import it.geosolutions.android.map.mapstore.model.MapStoreLayer;
import it.geosolutions.android.map.mapstore.model.MapStoreMap;
import it.geosolutions.android.map.mapstore.model.MapStoreSource;
import it.geosolutions.android.map.utils.ProjectionUtils;
import it.geosolutions.android.map.wms.WMSLayer;
import it.geosolutions.android.map.wms.WMSSource;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public class MapStoreUtils {
	public final static  String WMS_PTYPE ="gxp_wmssource";

	public static WMSLayer loadMapStoreConfig(final String geoStoreUrl,final Resource resource, final MapsActivity mapsActivity) {
		if(resource ==null || geoStoreUrl== null){
			//TODO notify
			return null;
			
		}
		
		AsyncTask<String, String, MapStoreConfiguration> task= new AsyncTask<String, String, MapStoreConfiguration>() {

			@Override
			protected MapStoreConfiguration doInBackground(String... params) {
				Long id = resource.id;
				GeoStoreClient client = new GeoStoreClient();
				client.setUrl(geoStoreUrl);
				
				String configString = client.getData(id);
				MapStoreConfiguration ctnrl = null;
				try{
					Gson gson = new GsonBuilder().create();
					ctnrl = gson.fromJson(configString, MapStoreConfiguration.class);
					//check "data" object if sources and map field are null)
					if(ctnrl != null && !isValidConfiguration(ctnrl)){
						if(ctnrl.data!=null){
							MapStoreConfiguration config1 = gson.fromJson(ctnrl.data,  MapStoreConfiguration.class);
							if(config1!=null && isValidConfiguration(config1)){
								ctnrl=config1;
							}
						}
					}
				}catch(IllegalStateException e){
					Log.e("MapStore","Unable to parse response");
					//Toast.makeText(mapsActivity, "ERROR PARSING MAPSTOREMAP", Toast.LENGTH_LONG).show();
				}catch(JsonSyntaxException e){
					Log.e("MapStore","Unable to parse response");
					//Toast.makeText(mapsActivity, "ERROR PARSING MAPSTOREMAP", Toast.LENGTH_LONG).show();
				}
				return ctnrl;
			}
			
			@Override
			protected void onPostExecute(MapStoreConfiguration result) {
				Log.d("MapStore",result.toString());
				mapsActivity.loadMapStoreConfig(result);
				GeoPoint p = getPoint(result);
				if(p!=null){
					mapsActivity.setPosition(p, (byte)result.map.zoom);
				}
			}

			private GeoPoint getPoint(MapStoreConfiguration result) {
				
				if(result.map.center !=null){
					if(result.map.center.length!=2) return null;
					if("EPSG:900913".equals( result.map.projection )){
						double y = ProjectionUtils.toGeographicY(result.map.center[1]);
						double x = ProjectionUtils.toGeographicX(result.map.center[0]);
						return new GeoPoint(y, x);
					}
					if ("EPSG:4326".equals( result.map.projection )){
						return new GeoPoint(result.map.center[1], result.map.center[0]);
					}
					 
				}
				return null;
			};
		};
		task.execute("");
		return null;
		
		

		
		
	}

	

	/*
	 * Check if the needed fields are present
	 */
	private static boolean  isValidConfiguration(MapStoreConfiguration ms) {
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
	public static ArrayList<WMSLayer>  buildWMSLayers(MapStoreConfiguration result) {
		ArrayList<WMSLayer> layers = new ArrayList<WMSLayer>();
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
	 * @param mss
	 * @param defaultSourceType if ptype is null, the default ptype should be the wms one. in this case the layer is wms by default.
	 * @return
	 */
	private static boolean isWMS(MapStoreSource mss, String defaultSourceType) {
		boolean isWMS =WMS_PTYPE.equals(mss.ptype) || (mss.ptype == null && WMS_PTYPE.equals(defaultSourceType));
		return isWMS;
	}
	/**
	 * Put the proper layer configurations from a mapstore one
	 * Set base params properly
	 * @param mlayer
	 * @param sources
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
