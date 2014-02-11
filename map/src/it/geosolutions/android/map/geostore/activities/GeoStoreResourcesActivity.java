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
package it.geosolutions.android.map.geostore.activities;

import java.util.ArrayList;

import it.geosolutions.android.map.MapsActivity;
import it.geosolutions.android.map.R;
import it.geosolutions.android.map.geostore.fragment.GeoStoreResourceListFragment;
import it.geosolutions.android.map.mapstore.model.MapStoreConfiguration;
import it.geosolutions.android.map.mapstore.utils.MapStoreUtils;
import it.geosolutions.android.map.model.Layer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

/**
 * This Activity manages the list of resources from GeoStore
 * Int manages also the return type from the map detail 
 * @author Lorenzo Natali (www.geo-solutions.it)
 * 
 */
public class GeoStoreResourcesActivity extends SherlockFragmentActivity {
	public class PARAMS {
		public static final String GEOSTORE_URL = "geostore_url";
		public static final String LAYERSTORE_NAME = "LAYERSTORE_NAME";
	}

	public static final int GET_MAP_CONFIG = 0;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
		//set title
		String layerStoreName = getIntent().getExtras().getString(PARAMS.LAYERSTORE_NAME);
		setTitle(layerStoreName +  " - " + getString(R.string.available_maps));
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		if (savedInstanceState == null) {
			// During initial setup, plug in the details fragment.
			GeoStoreResourceListFragment resources = new GeoStoreResourceListFragment();
			resources.setArguments(getIntent().getExtras());
			getSupportFragmentManager().beginTransaction()
					.add(android.R.id.content, resources).commit();

		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			return true;
		}
		return false;
	}
	
	@Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
    }
	
	@Override
	protected void onActivityResult(int request_code, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(request_code, resultCode, data);
		if(RESULT_OK==resultCode){
			Bundle b;
			if(data!=null){
				 b= data.getExtras();
				 if (b.containsKey(MapsActivity.MAPSTORE_CONFIG)) {
					MapStoreConfiguration config = (MapStoreConfiguration)b.getSerializable(MapsActivity.MAPSTORE_CONFIG);
					ArrayList<Layer> allLayers  = MapStoreUtils.buildWMSLayers(config);
					ArrayList<Layer> layerToAdd = new ArrayList<Layer>();
					for(Layer l : allLayers){
						if(l.isVisibility()){
							layerToAdd.add(l);
						}
					}
					returnData(layerToAdd);
				}else{
					setResult(resultCode, data);
					finish();
					
				}
				 overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);	 
			}
		}
		
	}
	/**
	 * @param layerToAdd
	 */
	private void returnData(ArrayList<Layer> layerToAdd) {
		Intent i = new Intent();
		Bundle bundle = new Bundle();
		bundle.putSerializable(MapsActivity.LAYERS_TO_ADD, layerToAdd) ;
        i.putExtras(bundle);
        setResult(RESULT_OK, i);
        finish();
	}
}
