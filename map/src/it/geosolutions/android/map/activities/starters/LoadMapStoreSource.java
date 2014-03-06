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
import it.geosolutions.android.map.activities.NewSourceActivity;
import it.geosolutions.android.map.common.Constants;
import it.geosolutions.android.map.mapstore.fragment.NewMapStoreSourceFragment;
import it.geosolutions.android.map.model.stores.LayerStore;
import it.geosolutions.android.map.model.stores.MapStoreLayerStore;
import it.geosolutions.android.map.utils.LocalPersistence;

import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * This activity allows to add a new <MapStoreLayerStore> from an <Intent>
 * The parsed request is passed to the New MapStore Source page.
 * Then, when this page is closed (also if the User don't save the source)
 * the map page is opened.
 * 
 * @author Lorenzo Natali (www.geo-solutions.it)
 */
public class LoadMapStoreSource extends Activity {

	private static final String RESOURCE_PATH_IDENTIFIER ="rest/";
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
	 * @param data the data String
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
		String mapStoreSchema = getString(R.string.mapstore_source_schema);
		String schema = uri.getScheme();
		Uri resourceURL = null;
		if(mapStoreSchema.equals(schema)){
			if(!uri.getPath().contains(RESOURCE_PATH_IDENTIFIER)){
				showErrorToast();
				finish();
			}else{
				resourceURL = uri;
			}
		}
		//replace the schema with http and start and open "New Source" page
		if(resourceURL != null){
			String url = resourceURL.toString().replace(mapStoreSchema+"://", "http://");
			//check if the source is present
			MapStoreLayerStore store = getExistingSource(url);
			Intent i = new Intent(this,NewSourceActivity.class);
			// if the store is not present I pass a new one in the intent
			if(store == null){
				store = new MapStoreLayerStore();
				store.setUrl(url);
				store.setName(resourceURL.getHost());
				
			}
			i.putExtra(NewMapStoreSourceFragment.PARAMS.STORE, store);
			startActivityForResult(i,Constants.requestCodes.CREATE_SOURCE);
		}else{
			showErrorToast();
			finish();
			return;
		}
		
		
	}

	
	/**
	 * Check if the source is already saved locally
	 * @param url
	 */
	@SuppressWarnings("unchecked")
	protected MapStoreLayerStore getExistingSource(String url) {
		List<LayerStore> stores = (List<LayerStore>)LocalPersistence.readObjectFromFile(this, LocalPersistence.SOURCES);
		for(LayerStore s : stores){
			if(s instanceof MapStoreLayerStore){
				MapStoreLayerStore source =(MapStoreLayerStore) s;
				if(source.getUrl().equals(url)){
					return source;
				}
			}
		}
		return null;
		
	}


	/**
	 * Start the main activity
	 * @param geoStoreUrl
	 */
	private void launchMainActivity() {
		Intent launch = new Intent(this, MapsActivity.class);
		launch.setAction(Intent.ACTION_VIEW);
		startActivity(launch);
		finish();
		
	}

	/**
	 * show an error if something went wrong during request parsing
	 */
	private void showErrorToast() {
		Toast.makeText(this, R.string.error_parsing_request, Toast.LENGTH_LONG).show(); 
		
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//when the source is added, the map is opened
		launchMainActivity();
		
	}
	

	/**
	 * onDestroy method for StartupActivity
	 */
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}


}