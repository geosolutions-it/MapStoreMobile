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
import it.geosolutions.android.map.geostore.activities.GeoStoreResourceDetailActivity;
import it.geosolutions.android.map.geostore.activities.GeoStoreResourcesActivity;
import it.geosolutions.android.map.geostore.adapters.GeoStoreResourceAdapter;
import it.geosolutions.android.map.geostore.loaders.GeoStoreResourceLoader;
import it.geosolutions.android.map.geostore.model.Resource;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

/**
 * Show a list of the resources from a GeoStore instance 
 * 
 * @author Lorenzo Natali (www.geo-solutions.it)
 */
public class GeoStoreResourceListFragment extends SherlockListFragment
        implements LoaderCallbacks<List<Resource>> {
private GeoStoreResourceAdapter adapter;
private static final int LOADER_INDEX =0;
private String geoStoreUrl;


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
    
    // create a unique loader index
    // TODO use a better system to get the proper loader
    // TODO check if needed,maybe the activity has only one loader
    

    // create task query
    //queryQueue = createTaskQueryQueue(layers, query);
    // Initialize loader and callbacks for the parent activity

    // setup the listView
    adapter = new GeoStoreResourceAdapter(getSherlockActivity(),
            R.layout.geostore_resource_row);
    setListAdapter(adapter);

}

@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
    startDataLoading(geoStoreUrl, LOADER_INDEX);

    return inflater.inflate(R.layout.geostore_resource_list, container, false); 
}


/**
 * Set the loading bar and loading text
 */
private void startLoadingGUI() {
    if(getSherlockActivity()!=null){
    // start progress bars
        getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
        getSherlockActivity().setSupportProgressBarVisibility(true);
    }
    // set suggestion text
    ((TextView) getView().findViewById(R.id.empty_text))
            .setText(R.string.feature_info_extracting_information);//TODO change the String
}

/**
 * hide loading bar and set loading task
 */
private void stopLoadingGUI() {
    if (getSherlockActivity() != null) {
        getSherlockActivity()
                        .setSupportProgressBarIndeterminateVisibility(false);
        getSherlockActivity()
                        .setSupportProgressBarVisibility(false);
        Log.v("GEOSTORE_LOADER", "task terminated");
        
    }
    adapter.notifyDataSetChanged();
}

private void setNoData() {
    ((TextView) getView().findViewById(R.id.empty_text))
            .setText(R.string.feature_info_extracting_no_result);//TODO change the string
}
/**
 * Create the data loader and bind the loader to the
 * parent callbacks
 * @param URL (not used for now)
 * @param loaderIndex a unique id for query loader
 */
private void startDataLoading(String url, int loaderIndex) {
    // create task query

    // initialize Load Manager
    mCallbacks = this;
    LoaderManager lm = getSherlockActivity().getSupportLoaderManager();
    // NOTE: use the start variable as index in the loadermanager
    // if you use more than one
    adapter.clear();
    lm.initLoader(loaderIndex, null, this); // uses start to get the
}

/*
 * (non-Javadoc)
 * @see android.support.v4.app.ListFragment#onViewCreated(android.view.View,
 * android.os.Bundle)
 */
@Override
public void onViewCreated(View view, Bundle savedInstanceState) {
    // TODO Auto-generated method stub
    super.onViewCreated(view, savedInstanceState);
    //init progress bar and loading text
    startLoadingGUI();
    //set the click listener for the items
    getListView().setOnItemClickListener(new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            Intent i = new Intent(view.getContext(),
                    GeoStoreResourceDetailActivity.class);
            i.putExtras(getActivity().getIntent().getExtras());
            Resource item = (Resource) parent.getAdapter().getItem(position);
            i.putExtra(GeoStoreResourceDetailActivity.PARAMS.RESOURCE,item);
            String action = getActivity().getIntent().getAction();
            i.setAction(action);
            getActivity().startActivityForResult(i, GeoStoreResourcesActivity.GET_MAP_CONFIG);

        }
    });
}

/**
 * Create the loader
 */
@Override
public Loader<List<Resource>> onCreateLoader(int id, Bundle args) {
	 getSherlockActivity()
     .setSupportProgressBarIndeterminateVisibility(true);
    return new GeoStoreResourceLoader(getSherlockActivity(),geoStoreUrl,null);
}

@Override
public void onLoadFinished(Loader<List<Resource>> loader,
        List<Resource> results) {
   
	for(Resource a : results ){
		adapter.add(a);
	}
	if (adapter.isEmpty()) {
        setNoData();
    }else{
    	adapter.addAll(results);	
    }
    stopLoadingGUI();

}



@Override
public void onLoaderReset(Loader<List<Resource>> arg0) {
    adapter.clear();

}

/*
 * (non-Javadoc)
 * @see android.support.v4.app.Fragment#onDestroy()
 */
@Override
public void onDestroy() {
    // TODO try to kill the load process
    super.onDestroy();
}

}
