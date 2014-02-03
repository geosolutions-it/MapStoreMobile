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
package it.geosolutions.android.map.fragment.sources;

import java.util.ArrayList;
import java.util.List;

import it.geosolutions.android.map.DataListActivity;
import it.geosolutions.android.map.MapsActivity;
import it.geosolutions.android.map.R;
import it.geosolutions.android.map.adapters.LayerStoreAdapter;
import it.geosolutions.android.map.adapters.LayerSwitcherAdapter;
import it.geosolutions.android.map.geostore.activities.GeoStoreResourcesActivity;
import it.geosolutions.android.map.listeners.OverlayChangeListener;
import it.geosolutions.android.map.mapstore.activities.MapStoreLayerListActivity;
import it.geosolutions.android.map.model.Layer;
import it.geosolutions.android.map.model.stores.LayerStore;
import it.geosolutions.android.map.overlay.managers.SimpleOverlayManager;
import it.geosolutions.android.map.overlay.switcher.LayerListLoader;
import it.geosolutions.android.map.utils.LocalPersistence;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockListFragment;

/**
 * This fragment shows a list of Sources from the local storage.
 * 
 * @author Lorenzo Natali (www.geo-solutions.it)
 */
public class SourcesFragment extends SherlockListFragment implements LayerStoreProvider,LoaderCallbacks<List<LayerStore>> {

private static final int LOADER_INDEX = 50;
private static final String CONTENTS = "MSM_CONTENT";
private LayerStoreAdapter adapter;
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
    
    adapter = new LayerStoreAdapter(getSherlockActivity(),
            R.layout.sources_row,this);
    setListAdapter(adapter);
    //star loading Layers
    getSherlockActivity().getSupportLoaderManager().initLoader(LOADER_INDEX, null, this);
    return inflater.inflate(R.layout.sources_fragment, container,
            false);
}

@Override
public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
}

/* (non-Javadoc)
 * @see it.geosolutions.android.map.fragment.sources.LayerStoreProvider#getSources()
 */
@Override
public List<LayerStore> getSources() {
	@SuppressWarnings("unchecked")
	List<LayerStore> stores = (List<LayerStore>)LocalPersistence.readObjectFromFile(getSherlockActivity(), LocalPersistence.SOURCES);
	return stores;
}

/* (non-Javadoc)
 * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onCreateLoader(int, android.os.Bundle)
 */
@Override
public Loader<List<LayerStore>> onCreateLoader(int arg0, Bundle arg1) {
	Loader<List<LayerStore>>  l = new LayerStoreLoader(getSherlockActivity(),this);
	l.forceLoad();
	return l;
}

/* (non-Javadoc)
 * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoadFinished(android.support.v4.content.Loader, java.lang.Object)
 */
@Override
public void onLoadFinished(Loader<List<LayerStore>> loader, List<LayerStore> result) {
	
	adapter.clear();
	ArrayList<LayerStore> ll = new ArrayList<LayerStore>();
	int size = result.size();
	//reverse add to the layer list to 
	//have the checkbox stacked as the layers
	Log.v("SOURCES","Loaded sources:"+size);
	if(size > 0){
		for(LayerStore ls : result){
			adapter.add(ls );
		}
	}

	adapter.notifyDataSetChanged();
	
}

/* (non-Javadoc)
 * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoaderReset(android.support.v4.content.Loader)
 */
@Override
public void onLoaderReset(Loader<List<LayerStore>> arg0) {
	adapter.clear();
	
}


}
