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
import it.geosolutions.android.map.geostore.activities.GeoStoreResourcesActivity;
import it.geosolutions.android.map.geostore.activities.GeoStoreResourceDetailActivity.PARAMS;
import it.geosolutions.android.map.geostore.adapters.GeoStoreResourceAdapter;
import it.geosolutions.android.map.geostore.loaders.GeoStoreResourceLoader;
import it.geosolutions.android.map.geostore.model.Resource;
import it.geosolutions.android.map.mapstore.activities.MapStoreLayerListActivity;
import it.geosolutions.android.map.mapstore.model.MapStoreConfiguration;
import it.geosolutions.android.map.mapstore.utils.MapStoreConfigTask;
import it.geosolutions.android.map.model.stores.LayerStore;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;

/**
 * Show a list of the resources from a GeoStore instance.
 * Load 5 at time the resources to fill the screen and provides 
 * search capabilities
 * 
 * @author Lorenzo Natali (www.geo-solutions.it)
 * 
 */
public class GeoStoreResourceListFragment extends SherlockListFragment
        implements 	LoaderCallbacks<List<Resource>>,
        			SearchView.OnQueryTextListener,
        			SearchView.OnCloseListener,
        			OnScrollListener,ActionMode.Callback {
/**
 * The adapter to show resources
 */
private GeoStoreResourceAdapter adapter;

/**
 * The loader index (uses only one loader)
 */
private static final int LOADER_INDEX =0;
private int page =0;
private int size = 5;
private String geoStoreUrl;

// The callbacks through which we will interact with the LoaderManager.
private LoaderManager.LoaderCallbacks<List<Resource>> mCallbacks;

//actionMode
private ActionMode actionMode = null;
private Resource selected;
//the string to search
private String filter;

//a reference to the searchView on top
private SearchView searchView;

//a reference to the current loader to get totalCount
private GeoStoreResourceLoader loader;

//a flag to skip scroll event if already loading
private boolean isLoading;

/**
 * Constructor 
 */
public GeoStoreResourceListFragment(){
	// call seatHasOptionsMenu to allow inflate of 
	// menu in the actionBar
	this.setHasOptionsMenu(true);
}
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
	//start loading first data
    startDataLoading(geoStoreUrl, LOADER_INDEX);
    return inflater.inflate(R.layout.geostore_resource_list, container, false); 
}


/**
 * Set the loading bar and loading text
 */
private void startLoadingGUI() {
    if(getSherlockActivity()!=null){
	    // start progress bars
	    getSherlockActivity().setSupportProgressBarVisibility(true);
	    getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
	    getSherlockActivity().getSupportActionBar();
    }
    // set suggestion text
    ((TextView) getView().findViewById(R.id.empty_text))
            .setText(R.string.geostore_extracting_information);//TODO change the String
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
    isLoading=false;
}

/**
 * Sets the view to show that no data are available
 */
private void setNoData() {
    ((TextView) getView().findViewById(R.id.empty_text))
            .setText(R.string.geostore_extracting_no_result);
    getView().findViewById(R.id.progress_bar).setVisibility(TextView.GONE);
}

/**
 * Create the data loader and bind the loader to the
 * parent callbacks
 * @param URL (not used for now)
 * @param loaderIndex a unique id for query loader
 */
private void startDataLoading(String url, int loaderIndex) {

    // initialize Load Manager
    mCallbacks = this;
    //reset page
    LoaderManager lm = getSherlockActivity().getSupportLoaderManager();
    adapter.clear();
    page=0;
    lm.initLoader(loaderIndex, null, this); 
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
    final GeoStoreResourceListFragment callback =this;
    
    //long click starts the action mode
    getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position,
                long id) {
        	Resource current = (Resource) parent.getAdapter().getItem(position);
        	if(current == selected){
        		 closeActionMode();
        		 getListView().setItemChecked(position, false);
   	         	
        	}else{
        		selected = (Resource) parent.getAdapter().getItem(position);
        		getListView().setItemChecked(position, true);
	        	actionMode = getSherlockActivity().startActionMode(callback);
		    	 //override the done button to clear selection all when the button is pressed
		    	 int doneButtonId = Resources.getSystem().getIdentifier("action_mode_close_button", "id", "android");
		    	 View doneButton = getActivity().findViewById(doneButtonId);
		    	 if(doneButton != null){
			    	 doneButton.setOnClickListener(new View.OnClickListener() {
		
			    	     @Override
			    	     public void onClick(View v) {
			    	    	 closeActionMode();
			    	     }
			    	 });
		    	 }
        	}
        	return true;
        }
        
		
    });
    
    //single click open layer list
    getListView().setOnItemClickListener(new OnItemClickListener() {
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			closeActionMode();
			Resource r = (Resource) parent.getAdapter().getItem(position);
			getListView().setItemChecked(position, true);
			startLayerSelection(r.id);
			
		}
	});
    //associate scroll listener to implement infinite scroll
    getListView().setOnScrollListener(this);

}

protected void loadMore() {
	
	if(loader!=null){
		if(loader.totalCount > adapter.getCount() && loader.totalCount>0){
			page++;
			getLoaderManager().restartLoader(LOADER_INDEX, null, this);
		}else{
			//TODO notify finish loading
		}
		
	}
}

  
/* (non-Javadoc)
 * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onCreateLoader(int, android.os.Bundle)
 */
@Override
public Loader<List<Resource>> onCreateLoader(int id, Bundle args) {
	 //set the loading 
	 getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
	 getSherlockActivity().getSupportActionBar();
	 loader = new GeoStoreResourceLoader(getSherlockActivity(),geoStoreUrl,filter,page,size);
     return loader; 
}

/* (non-Javadoc)
 * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoadFinished(android.support.v4.content.Loader, java.lang.Object)
 */
@Override
public void onLoadFinished(Loader<List<Resource>> loader,
        List<Resource> results) {
   if(results == null){
	   Toast.makeText(getSherlockActivity(), R.string.error_retrieving_resources_from_mapstore, Toast.LENGTH_SHORT).show();//TODO i18n
	   setNoData();
   }else{
	   //add loaded resources to the listView
		for(Resource a : results ){
			adapter.add(a);
		}
		if (adapter.isEmpty()) {
	        setNoData();
	    }else{
	    	 updateView();
	    }
   }
  
   stopLoadingGUI();

}


/**
 * Update the info about pagination and visibility of more button
 */
private void updateView() {
	int count =adapter.getCount();
	if(loader!=null){
		TextView infoView = (TextView)getView().findViewById(R.id.info);
		
		Formatter f = new Formatter();
		String info = f.format(getString(R.string.geostore_info_format),count,loader.totalCount).toString();
		f.close();
		infoView.setText(info);
//		if(count < loader.totalCount){
//			moreButton.setVisibility(Button.VISIBLE);
//		}
	}
	
}


/* (non-Javadoc)
 * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoaderReset(android.support.v4.content.Loader)
 */
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

@Override
public void onCreateOptionsMenu(Menu menu,MenuInflater inflater) {
	super.onCreateOptionsMenu(menu, inflater);
	// TODO Auto-generated method stub
	    inflater.inflate(R.menu.geostore_list, menu);
        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        if(searchView !=null){
        	setupSearch(searchView);
        }else{
        	Toast.makeText(getSherlockActivity(), "Unable to setup search button", Toast.LENGTH_SHORT).show();
        }
	}


/**
 * setup the searchView <QueryTextlistener> and <CloseListener>.
 * This class implements also the interfaces to manage
 * query text changes and close events.
 * @param searchView
 */
private void setupSearch(SearchView searchView) {
	searchView.setOnQueryTextListener(this);
	searchView.setOnCloseListener(this);
	searchView.setIconifiedByDefault(true);
    //item.setActionView(searchView);
	
}


/* (non-Javadoc)
 * @see com.actionbarsherlock.widget.SearchView.OnQueryTextListener#onQueryTextSubmit(java.lang.String)
 */
@Override
public boolean onQueryTextSubmit(String query) {
	//change the filter text and reload
	filter = !TextUtils.isEmpty(query) ? query : null;
	adapter.clear();
	page=0;
    getLoaderManager().restartLoader(LOADER_INDEX, null, this);
	return false;
}

@Override
public boolean onQueryTextChange(String newText) {
	//change the filter text and reload
	filter = !TextUtils.isEmpty(newText) ? newText : null;
	adapter.clear();
	page=0;
    getLoaderManager().restartLoader(LOADER_INDEX, null, this);
	return false;
}

@Override
public boolean onClose() {
	//reset the query when searchView is closed
    if (!TextUtils.isEmpty(searchView.getQuery())) {
        searchView.setQuery(null, true);
    }
    return true;
}


@Override
public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	//check if applicable
	if (adapter == null){
        return ;
	}
    if (adapter.getCount() == 0){
        return ;
    }
    if(loader==null){
    	return;
    }
    if(loader.totalCount==0){
    	return;
    }
    //if the last item is visible and can load more resources
    //load more resources
    int l = visibleItemCount + firstVisibleItem;
    if (l >= totalItemCount && !isLoading && adapter.getCount()<loader.totalCount) {
        // It is time to add new data. We call the listener
        //this.addFooterView(footer);
        isLoading = true;
        loadMore();
    }
	
}
@Override
public void onScrollStateChanged(AbsListView view, int scrollState) {
	//Nothing to do for now
	
}

//
// ACTION MODE CALLBACKS
//
/* (non-Javadoc)
 * @see com.actionbarsherlock.view.ActionMode.Callback#onCreateActionMode(com.actionbarsherlock.view.ActionMode, com.actionbarsherlock.view.Menu)
 */
@Override
public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	mode.getMenuInflater().inflate(R.menu.details_loadmap_selectlayers, menu);
	this.actionMode =mode;
	return true;
}
/* (non-Javadoc)
 * @see com.actionbarsherlock.view.ActionMode.Callback#onPrepareActionMode(com.actionbarsherlock.view.ActionMode, com.actionbarsherlock.view.Menu)
 */
@Override
public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
	// TODO Auto-generated method stub
	return false;
}
/* (non-Javadoc)
 * @see com.actionbarsherlock.view.ActionMode.Callback#onActionItemClicked(com.actionbarsherlock.view.ActionMode, com.actionbarsherlock.view.MenuItem)
 */
@Override
public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
	int itemId = item.getItemId();
	if(selected == null) return true;
	if(itemId == R.id.details){
		showDetailsActivity(selected);
		return true;
	}else if( itemId == R.id.load_map){
		loadAllMap();
		//TODO
	}else if ( itemId == R.id.select_layers){
		startLayerSelection(selected.id);
			
			
	}
	return true;
	
	

}

/**
 * return the resource and the GeoStore URL to make the
 * map load the map itself
 */
private void loadAllMap() {
	Intent data = new Intent();
	data.putExtra(PARAMS.RESOURCE, selected);
	data.putExtra(GeoStoreResourcesActivity.PARAMS.GEOSTORE_URL, geoStoreUrl);
	getActivity().setResult(Activity.RESULT_OK, data);
	getActivity().finish();
}

/**
 * Start the activity that shows layer selection
 */
private void startLayerSelection(Long id) {
	//TODO loading
	final Activity ac = getActivity();
			AsyncTask<String, String, MapStoreConfiguration> task = new MapStoreConfigTask(
					id, geoStoreUrl) {

				@Override
				protected void onPostExecute(MapStoreConfiguration result) {
					Log.d("MapStore", result.toString());
					// call the loadMapStore config on the Activity
					Intent i  = new Intent(ac, MapStoreLayerListActivity.class);
					//TODO put MapStore config
					i.putExtra(MapsActivity.MAPSTORE_CONFIG	,result);
					startActivityForResult(i, MapsActivity.MAPSTORE_REQUEST_CODE);
					getSherlockActivity().overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
				}
			};
			task.execute("");
}

/* (non-Javadoc)
 * @see com.actionbarsherlock.view.ActionMode.Callback#onDestroyActionMode(com.actionbarsherlock.view.ActionMode)
 */
@Override
public void onDestroyActionMode(ActionMode mode) {
	//nothing to do
	
}

/**
 * Open the Activity that shows details about the map
 * @param ctx
 * @param item
 */
private void showDetailsActivity(Resource item) {
	Intent i = new Intent(this.getSherlockActivity(),
            GeoStoreResourceDetailActivity.class);
    i.putExtras(getActivity().getIntent().getExtras());
   
    i.putExtra(GeoStoreResourceDetailActivity.PARAMS.RESOURCE,item);
    String action = getActivity().getIntent().getAction();
    i.setAction(action);
    getActivity().startActivityForResult(i, GeoStoreResourcesActivity.GET_MAP_CONFIG);
}

/**
 * Close the action mode and clear selection
 */
private void closeActionMode() {
	 getListView().clearChoices();
	 getListView().clearFocus();	
	 selected = null;
	 if(actionMode!=null){
		 actionMode.finish();
	 }

}
}
