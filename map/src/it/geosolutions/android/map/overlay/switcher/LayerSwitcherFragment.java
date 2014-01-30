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
package it.geosolutions.android.map.overlay.switcher;

import it.geosolutions.android.map.MapsActivity;
import it.geosolutions.android.map.R;
import it.geosolutions.android.map.adapters.LayerSwitcherAdapter;
import it.geosolutions.android.map.geostore.activities.GeoStoreResourcesActivity;
import it.geosolutions.android.map.listeners.LayerChangeListener;
import it.geosolutions.android.map.model.Layer;
import it.geosolutions.android.map.overlay.managers.MultiSourceOverlayManager;
import it.geosolutions.android.map.overlay.managers.OverlayManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.actionbarsherlock.app.SherlockListFragment;

/**
 * This fragment shows a view o the attributes of a single feature from a
 * feature passed as Extra. 
 * 
 * Is binded with The <SimpleOverlayManager> methods.
 * 
 * This object is retained, but the activity itself is recreated. So it has also 
 * to provide to the loaders that initializes all the data. This is why it implements <LayerProvider>.
 * 
 * Catch events from <LayerChangeListener> to be sincronized with add/changes to the map.
 * 
 * When things change (visibility,add) the list and the map are refreshed.
 * 
 * 
 * @author Lorenzo Natali (www.geo-solutions.it)
 */
public class LayerSwitcherFragment extends SherlockListFragment implements LayerChangeListener, LoaderCallbacks<List<Layer>>, LayerProvider {
private int LOADER_INDEX =1290;
private LayerSwitcherAdapter adapter;
private LayerListLoader loader;
private boolean isLoading = false;

/**
 * Called only once
 */
@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
   
}

@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    //set the adapter for the layers
    adapter = new LayerSwitcherAdapter(getSherlockActivity(),
            R.layout.layer_checklist_row,this);
    setListAdapter(adapter);
    //star loading Layers
    
    getSherlockActivity().getSupportLoaderManager().initLoader(LOADER_INDEX, null, this);
    
    //return the layout inflater
    return inflater.inflate(R.layout.layer_switcher, container, false);
}

/*
 * (non-Javadoc)
 * @see android.support.v4.app.ListFragment#onViewCreated(android.view.View,
 * android.os.Bundle)
 */
@Override
public void onViewCreated(View view, Bundle savedInstanceState) {
    final MapsActivity ac = (MapsActivity)getActivity(); 
    
    //the map selection activity launcher
    ImageButton msEd = (ImageButton) view.findViewById(R.id.mapstore_edit);
	msEd.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
		    Intent pref = new Intent(ac,GeoStoreResourcesActivity.class);
			pref.putExtra(GeoStoreResourcesActivity.PARAMS.GEOSTORE_URL,"http://mapstore.geo-solutions.it/geostore/rest/");
		    ac.startActivityForResult(pref, MapsActivity.MAPSTORE_REQUEST_CODE);
		}
	});
	
	//force reload
	reload();
    super.onViewCreated(view, savedInstanceState);
    // setup of the checkboxes
}

@Override
public Loader<List<Layer>> onCreateLoader(int arg0, Bundle arg1) {
	 this.loader = new LayerListLoader(getSherlockActivity(),this);
	return this.loader;
}

@Override
public void onLoadFinished(Loader<List<Layer>> arg0, List<Layer> layers) {
	isLoading =true;
	adapter.clear();
	ArrayList<Layer> ll = new ArrayList<Layer>();
	int size = layers.size();
	//reverse add to the layer list to 
	//have the checkbox stacked as the layers
	if(size > 0){
		for(int i = size-1 ; i >=0 ; i--){
			adapter.add( layers.get(i) );
		}
	}
	isLoading =false;
	adapter.notifyDataSetChanged();
}

@Override
public void onLoaderReset(Loader<List<Layer>> layers) {
	adapter.clear();
}

@Override
public void onSetLayers(ArrayList<Layer> layers) {
	reload();
}
private void reload() {
	if(adapter !=null){
		adapter.clear();
		//force reload
		getSherlockActivity().getSupportLoaderManager().getLoader(LOADER_INDEX).forceLoad();

	}
	
}

/**
 * privide the overlayManager binded to this layer switcher
 * @return
 */
public MultiSourceOverlayManager getOverlayManager(){
	final MapsActivity ac = (MapsActivity)getActivity(); 
    return (MultiSourceOverlayManager) ac.overlayManager;
}

@Override
public void onLayerVisibilityChange(Layer layer) {
	if(!isLoading ){
		getOverlayManager().redrawLayer(layer);
	}
}

@Override
public List<Layer> getLayers() {
	//returns the layers from the current overlayManager
	//NOTE: this is needed because the instance of the overlay manager
	// can change during time. It needs to be get from the current
	// activity.
	return getOverlayManager().getLayers();
}

}


