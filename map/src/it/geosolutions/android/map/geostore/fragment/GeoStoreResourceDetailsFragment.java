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

import it.geosolutions.android.map.R;
import it.geosolutions.android.map.geostore.activities.GeoStoreResourceDetailActivity.PARAMS;
import it.geosolutions.android.map.geostore.activities.GeoStoreResourcesActivity;
import it.geosolutions.android.map.geostore.model.Resource;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    //description
    TextView description = (TextView)v.findViewById(R.id.description);
    description.setText(resource.description);
  //Creation
    TextView creation = (TextView)v.findViewById(R.id.creation);
    creation.setText(resource.creation);
    //Last Update
    TextView update = (TextView)v.findViewById(R.id.lastUpdate);
    update.setText(resource.lastUpdate);
    //id
    TextView idt = (TextView)v.findViewById(R.id.id);
    idt.setText(resource.id.toString());
    return v;
}

}