/*
 * GeoSolutions MapStoreMobile - Digital field mapping on Android based devices
 * Copyright (C) 2014  GeoSolutions (www.geo-solutions.it)
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
package it.geosolutions.android.map.activities.starters;

import it.geosolutions.android.map.MapsActivity;
import it.geosolutions.android.map.R;
import it.geosolutions.android.map.geostore.model.Resource;
import it.geosolutions.android.map.geostore.utils.GeoStoreClient;
import it.geosolutions.android.map.model.stores.LayerStore;
import it.geosolutions.android.map.model.stores.MapStoreLayerStore;
import it.geosolutions.android.map.utils.LocalPersistence;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * This activity Allows add a MapStore server instance to the 
 * Sources, if not present, and load a map getting the mapId
 * Catches the intent mapstore:/host/path?mapId=id.
 * Finally load the map
 * 
 * 
 * @author Lorenzo Natali (www.geo-solutions.it)
 */
public class LoadMapStoreMap extends Activity {

	private static final String RESOURCE_PATH_IDENTIFIER ="rest/data/";
	protected ProgressDialog progressDialog;

	/**
	 * onCreate method for startup activity. @ param savedInstanceState.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent i = getIntent();
		String data = i.getDataString();
		identifyData(data);
		
	}

	/**
	 * Parse data string to check if valid
	 * @param data the data string
	 */
	private void identifyData(String data) {
		Uri uri = null;
		try {
			uri =  Uri.parse(data);
		} catch (NullPointerException e) {
			//Data string is null
			showErrorToast();
			Log.e("MapStore load", "Data string is null ");
			finish();
			return;
		}
		//parsing schema
		String mapStoreSchema = getString(R.string.mapstore_uri_schema);
		String schema = uri.getScheme();
		Uri resourceURL = null;
		if(mapStoreSchema.equals(schema)){
			if(uri.getPath().contains(RESOURCE_PATH_IDENTIFIER)){
				resourceURL = uri;
			}else{
				showErrorToast();
				finish();
				return;
			}
		}
		if(resourceURL != null){
			loadMap(resourceURL);
		}else{
			showErrorToast();
			finish();
			return;
		}
		
		
	}

	/**
	 * Try to load resource and open map activity
	 * @param resourceURL the URL of the resource
	 * 
	 */
	private void loadMap(Uri resourceURL) {
		final int mapId = getMapId(resourceURL);
		final String geostoreURL = getGeoStoreURL(resourceURL);
		if(mapId < 0){
			finish();
			return;
		}
		progressDialog = ProgressDialog.show(this,"" ,getString( R.string.loading_layers));
		new AsyncTask<Integer, Integer, Resource>(){

			@Override
			protected Resource doInBackground(Integer... params) {
				GeoStoreClient c= new GeoStoreClient();
				c.setUrl(geostoreURL);
				Resource r = c.getResource(mapId);
				return r;
			}
			
			@Override
			protected void onPostExecute(Resource result) {
				super.onPostExecute(result);
				if(result != null){
					Log.v("MapStore load","resource name" + result.name);
					Log.v("MapStore load","geostore URL" + geostoreURL);
					//the source is verified,so we can save source if not present
					saveSource(geostoreURL);
				}
				
				launchMainActivity(result,geostoreURL);
				progressDialog.dismiss();
			}
			
		}.execute();
		
		
	}

	/**
	 * Save the source if not present
	 * @param geostoreURL URL of GeoStore
	 */
	protected void saveSource(String geostoreURL) {
		@SuppressWarnings("unchecked")
		List<LayerStore> stores = (List<LayerStore>)LocalPersistence.readObjectFromFile(this, LocalPersistence.SOURCES);
		//scan the available sources and test if they has the same URL
		for(LayerStore s : stores){
			if(s instanceof MapStoreLayerStore){
				MapStoreLayerStore source =(MapStoreLayerStore) s;
				if(source.getUrl().equals(geostoreURL)){
					return;
				}
			}
		}
		// if we arrived here, no source with the same GeoStore url is present
		// so we can create a new one
		MapStoreLayerStore s = new MapStoreLayerStore();
		// use host name for source name
		Uri uri = Uri.parse(geostoreURL);
		s.setName(uri.getHost());
		// set base URL of GeoStore
		s.setUrl(geostoreURL);
		stores.add(s);
		LocalPersistence.writeObjectToFile(this, stores, LocalPersistence.SOURCES);
		
	}

	/**
	 * Parse the URI (mapstore:// and replace proper strings to get http URL
	 * @param resourceURL
	 */
	private String getGeoStoreURL(Uri resourceURL) {
		String url = resourceURL.toString().split("/rest/")[0] + "/rest/"; 
		url = url.replace(getString(R.string.mapstore_uri_schema)+ "://", "http://");
		Log.v("MapStore load","geostore URL" + url);
		return url;
		
	}

	/**
	 * Parse the URi to get the MapId
	 * @param resourceURL
	 * @return
	 */
	private int getMapId(Uri resourceURL) {
		int mapId = -1;
		String path = resourceURL.getPath(); 
		String[] pices = path.split("/resource/");
		String idString = null;
		if(pices.length == 2){
			idString = pices[1];
		}else{
			 pices = path.split("/data/");
			 if(pices.length == 2){
					idString = pices[1];
			 }
		}
		if(idString !=null){
			try{
			mapId = Integer.parseInt(idString);
			}catch(NumberFormatException e){
				Log.e("MapStore load","unable to parse mapId for" + resourceURL.toString());
				showErrorToast();
			}
		}
		return mapId;
	}

	/**
	 * Launches main activity passing Resource and GeoStore URL to allow
	 * the activity to load the map
	 * @param resourceURL
	 */
	private void launchMainActivity(Resource resource,String geoStoreUrl) {
		Intent launch = new Intent(this, MapsActivity.class);
		launch.setAction(Intent.ACTION_VIEW);
		launch.putExtra(MapsActivity.PARAMETERS.LAT, 43.68411);
		launch.putExtra(MapsActivity.PARAMETERS.LON, 10.84899);
		launch.putExtra(MapsActivity.PARAMETERS.ZOOM_LEVEL, (byte)13);
		launch.putExtra(MapsActivity.PARAMETERS.RESOURCE, resource);
		launch.putExtra(MapsActivity.PARAMETERS.GEOSTORE_URL, geoStoreUrl);
		startActivity(launch);
		finish();
		
	}

	/**
	 * display an error about wrong parse of URI in request
	 */
	private void showErrorToast() {
		Toast.makeText(this,R.string.error_parsing_request, Toast.LENGTH_LONG).show(); 
		
	}

	/**
	 * onDestroy method for StartupActivity
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}

