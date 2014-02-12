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
import it.geosolutions.android.map.geostore.fragment.GeoStoreResourceDetailsFragment;
import it.geosolutions.android.map.geostore.model.Resource;
import it.geosolutions.android.map.mapstore.model.MapStoreConfiguration;
import it.geosolutions.android.map.mapstore.utils.MapStoreUtils;
import it.geosolutions.android.map.model.Layer;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

/**
 * This Activity shows the Details of a Resource
 * @author Lorenzo Natali (www.geo-solutions.it)
 * 
 */
public class GeoStoreResourceDetailActivity extends SherlockFragmentActivity {
	Resource resource;
	public class PARAMS{
		public static final String RESOURCE = "resource";
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		if (savedInstanceState == null) {
			// During initial setup, plug in the details fragment.
			GeoStoreResourceDetailsFragment detail = new GeoStoreResourceDetailsFragment();
			detail.setArguments(getIntent().getExtras());
			resource = (Resource) getIntent().getSerializableExtra(PARAMS.RESOURCE);
			
			//set activity title
			String title = resource.name;
			if(title!=null){
				setTitle(title + "-" + getString(R.string.details));
			}else{
				setTitle(getString(R.string.details));
			}
			getSupportFragmentManager().beginTransaction()
					.add(android.R.id.content, detail).commit();

		}

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			finish();
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
		} 
		return false;
	}
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			if(RESULT_OK==resultCode){
				Bundle b;
				if(data!=null){
					 b= data.getExtras();
					 //return only the selected layers from mapstore config (visible = true)
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
						}
				}
			}
			
		
		
	}
	
	@Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
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
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
		
		
	}
	
}
