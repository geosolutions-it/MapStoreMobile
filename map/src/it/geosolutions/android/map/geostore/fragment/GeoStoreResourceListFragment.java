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

import java.util.Formatter;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.widget.SearchView;

/**
 * Show a list of the resources from a GeoStore instance.
 * Load 5 at time the resources to fill the screen and provides 
 * search capabilities
 * 
 * @author Lorenzo Natali (www.geo-solutions.it)
 */
/**
 * @author Admin
 *
 */
/**
 * @author Admin
 *
 */
/**
 * @author Admin
 *
 */
public class GeoStoreResourceListFragment extends SherlockListFragment
        implements 	LoaderCallbacks<List<Resource>>,
        			SearchView.OnQueryTextListener,
        			SearchView.OnCloseListener,
        			OnScrollListener  {
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
	   Toast.makeText(getSherlockActivity(), "error retrieving the resources", Toast.LENGTH_SHORT).show();//TODO i18n
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
    //if the last item is visibile and can load more resources
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
	// TODO Auto-generated method stub
	
}



}
