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
package it.geosolutions.android.map.geostore.fragment;

import it.geosolutions.android.map.MapsActivity;
import it.geosolutions.android.map.R;
import it.geosolutions.android.map.geostore.activities.GeoStoreResourceDetailActivity;
import it.geosolutions.android.map.geostore.activities.GeoStoreResourceDetailActivity.PARAMS;
import it.geosolutions.android.map.geostore.activities.GeoStoreResourcesActivity;
import it.geosolutions.android.map.geostore.model.Resource;
import it.geosolutions.android.map.mapstore.activities.MapStoreLayerListActivity;
import it.geosolutions.android.map.mapstore.model.MapStoreConfiguration;
import it.geosolutions.android.map.mapstore.utils.MapStoreConfigTask;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * Show a list of the layers from a feature info query This fragment is
 * optimized to get only the available features doing a query on the visible
 * layers to check if at least one is present.
 * 
 * @author Lorenzo Natali (www.geo-solutions.it)
 */
public class GeoStoreResourceDetailsFragment extends SherlockFragment {

private String geoStoreUrl;
private Resource resource;

// The callbacks through which we will interact with the LoaderManager.

private LoaderManager.LoaderCallbacks<List<Resource>> mCallbacks;

/**
 * Called once on creation
 */
@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // view operations

    setRetainInstance(true);

    // get parameters to create the task query
    // TODO use arguments instead
    Bundle extras = getActivity().getIntent().getExtras();
    geoStoreUrl =  extras.getString(GeoStoreResourcesActivity.PARAMS.GEOSTORE_URL);
    resource = (Resource) getArguments().getSerializable(PARAMS.RESOURCE);
}

@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    View v = inflater.inflate(R.layout.geostore_resource_details, container, false);
    //name
    TextView name = (TextView)v.findViewById(R.id.name);
    name.setText(resource.name);
    //owner
    TextView owner = (TextView)v.findViewById(R.id.owner);
    owner.setText(resource.owner);
    //Creation
    TextView creation = (TextView)v.findViewById(R.id.creation);
    creation.setText(resource.creation);
    //Last Update
    TextView update = (TextView)v.findViewById(R.id.lastUpdate);
    update.setText(resource.lastUpdate);
    
    //description
    TextView description = (TextView)v.findViewById(R.id.description);
    description.setText(resource.description);
    
    Button selectLayers = (Button)v.findViewById(R.id.select_layers);
    selectLayers.setOnClickListener(new OnClickListener() {
		GeoStoreResourceDetailActivity ac = (GeoStoreResourceDetailActivity)getActivity();
		@Override
		public void onClick(View v) {
		
				AsyncTask<String, String, MapStoreConfiguration> task = new MapStoreConfigTask(
						resource.id, geoStoreUrl) {

					@Override
					protected void onPostExecute(MapStoreConfiguration result) {
						Log.d("MapStore", result.toString());
						// call the loadMapStore config on the Activity
						Intent i  = new Intent(ac, MapStoreLayerListActivity.class);
						//TODO put MapStore config
						i.putExtra(MapsActivity.MAPSTORE_CONFIG	,result);
						startActivityForResult(i, MapsActivity.MAPSTORE_REQUEST_CODE);
					}
				};
				task.execute("");
			
			
		}
	});
    
    Button loadMap = (Button)v.findViewById(R.id.load_map);
    loadMap.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent data = new Intent();
			data.putExtra(PARAMS.RESOURCE, resource);
			data.putExtra(GeoStoreResourcesActivity.PARAMS.GEOSTORE_URL, geoStoreUrl);
			getActivity().setResult(Activity.RESULT_OK, data);
			getActivity().finish();
			
		}
	});
    return v;
}

}