/*
 * GeoSolutions - MapstoreMobile - GeoSpatial Framework on Android based devices
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
import it.geosolutions.android.map.geostore.utils.GeoStoreClient;
import it.geosolutions.android.map.mapstore.model.MapStoreConfiguration;
import it.geosolutions.android.map.utils.ProjectionUtils;

import org.mapsforge.core.model.GeoPoint;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

/**
 * Task that loads a resource from geostore and parse to create a <MapStoreConfiguration> object
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public class MapStoreConfigTask extends AsyncTask<String, String, MapStoreConfiguration> {
			
			private String geoStoreUrl;
			private Long id;

			public MapStoreConfigTask(Long id, String geoStoreUrl){
				this.id = id;
				this.geoStoreUrl = geoStoreUrl;
			}
			@Override
			protected MapStoreConfiguration doInBackground(String... params) {
				GeoStoreClient client = new GeoStoreClient();
				client.setUrl(geoStoreUrl);
				
				String configString = client.getData(id);
				MapStoreConfiguration ctnrl = null;
				try{
					//try to parse the downloaded MapStore Configuration
					Gson gson = new GsonBuilder().create();
					ctnrl = gson.fromJson(configString, MapStoreConfiguration.class);
					//check "data" object if sources and map field are null)
					if(ctnrl != null && !MapStoreUtils.isValidConfiguration(ctnrl)){
						if(ctnrl.data!=null){
							MapStoreConfiguration config1 = gson.fromJson(ctnrl.data,  MapStoreConfiguration.class);
							if(config1!=null && MapStoreUtils.isValidConfiguration(config1)){
								ctnrl=config1;
							}
						}
					}
				}catch(IllegalStateException e){
					Log.e("MapStore","Unable to parse response");
					//TODO Toast.makeText(mapsActivity, "ERROR PARSING MAPSTOREMAP", Toast.LENGTH_LONG).show();
				}catch(JsonSyntaxException e){
					Log.e("MapStore","Unable to parse response");
					//TODO Toast.makeText(mapsActivity, "ERROR PARSING MAPSTOREMAP", Toast.LENGTH_LONG).show();
				}
				return ctnrl;
			}

			public GeoPoint getPoint(MapStoreConfiguration result) {
				
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
}
