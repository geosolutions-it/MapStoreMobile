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
package it.geosolutions.android.map.fragment.featureinfo;

import it.geosolutions.android.map.R;
import it.geosolutions.android.map.activities.GetFeatureInfoAttributeActivity;
import it.geosolutions.android.map.adapters.FeatureInfoLayerAdapter;
import it.geosolutions.android.map.common.Constants;
import it.geosolutions.android.map.database.SpatialDataSourceManager;
import it.geosolutions.android.map.loaders.FeatureCircleLoader;
import it.geosolutions.android.map.loaders.FeatureInfoLoader;
import it.geosolutions.android.map.loaders.FeaturePolygonLoader;
import it.geosolutions.android.map.model.Layer;
import it.geosolutions.android.map.model.query.CircleQuery;
import it.geosolutions.android.map.model.query.CircleTaskQuery;
import it.geosolutions.android.map.model.query.FeatureInfoQueryResult;
import it.geosolutions.android.map.model.query.PolygonQuery;
import it.geosolutions.android.map.model.query.PolygonTaskQuery;
import it.geosolutions.android.map.model.query.BBoxQuery;
import it.geosolutions.android.map.model.query.BBoxTaskQuery;
import it.geosolutions.android.map.utils.FeatureInfoUtils;

import java.util.ArrayList;
import java.util.List;

import jsqlite.Exception;
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

import eu.geopaparazzi.spatialite.database.spatial.core.SpatialVectorTable;

/**
 * Show a list of the layers from a feature info query This fragment is
 * optimized to get only the available features doing a query on the visible
 * layers to check if at least one is present.
 * @author Lorenzo Natali (www.geo-solutions.it)
 */
public class FeatureInfoLayerListFragment extends SherlockListFragment
        implements LoaderCallbacks<List<FeatureInfoQueryResult>> {
	
private FeatureInfoLayerAdapter adapter;
private static final int LOADER_INDEX =0;

private String selection_type;
private BBoxTaskQuery[] queryQueueRect;
private CircleTaskQuery[] queryQueueCircle;
private PolygonTaskQuery[] queryQueuePolygon;

// The callbacks through which we will interact with the LoaderManager.

private LoaderManager.LoaderCallbacks<List<FeatureInfoQueryResult>> mCallbacks;

/**
 * Called once a time on creation moment
 * @param savedInstanceState
 */
@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // view operations

    setRetainInstance(true);

    // get parameters to create the task query
    // TODO use arguments instead
    Bundle extras = getActivity().getIntent().getExtras();
    selection_type = extras.getString("selection"); //Discover which selection user has selected
    ArrayList<Layer> layers = (ArrayList<Layer>)extras.getSerializable(Constants.ParamKeys.LAYERS);
    // create a unique loader index
    // TODO use a better system to get the proper loader
    // TODO check if needed,maybe the activity has only one loader
      
    BBoxQuery query_r;
    CircleQuery query_c;
    PolygonQuery query_p;
    
    // create task query
    if(selection_type.equals("Rectangular")){
    	query_r = (BBoxQuery) extras.getParcelable("query");
    	queryQueueRect = FeatureInfoUtils.createTaskQueryQueue(layers, query_r, null, 1);
    }
    else 
    	if(selection_type.equals("Circular")){
	    	query_c = (CircleQuery) extras.getParcelable("query");
	    	queryQueueCircle = FeatureInfoUtils.createTaskQueryQueue(layers, query_c, null, 1);
    	}
    	else{
    		query_p = (PolygonQuery) extras.getParcelable("query");
        	queryQueuePolygon = FeatureInfoUtils.createTaskQueryQueue(layers, query_p, null, 1);
    	}
    
    // Initialize loader and callbacks for the parent activity

    // setup the listView
    adapter = new FeatureInfoLayerAdapter(getSherlockActivity(),
            R.layout.feature_info_layer_list_row);
    setListAdapter(adapter);

}

@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
	
	if(selection_type.equals("Rectangular")){
	    startDataLoading(queryQueueRect, LOADER_INDEX);
    }
    else 
    	if(selection_type.equals("Circular")){
    	    startDataLoading(queryQueueCircle, LOADER_INDEX);
    	}
    	else{
    	    startDataLoading(queryQueuePolygon, LOADER_INDEX);
    	}

    return inflater.inflate(R.layout.feature_info_layer_list, container, false);
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
            .setText(R.string.feature_info_extracting_information);
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
        Log.v("FEATURE_INFO_TASK", "task terminated");
        
    }
    adapter.notifyDataSetChanged();
}

private void setNoData() {
    ((TextView) getView().findViewById(R.id.empty_text))
            .setText(R.string.feature_info_extracting_no_result);
}
/**
 * Create the data loader and bind the loader to the
 * parent callbacks
 * @param query array of <FeatureInfoTaskQuery> to pass to the loader
 * @param loaderIndex a unique id for query loader
 */
private void startDataLoading(BBoxTaskQuery[] query, int loaderIndex) {
    // create task query

    // initialize Load Manager
    mCallbacks = this;
    LoaderManager lm = getSherlockActivity().getSupportLoaderManager();
    // NOTE: use the start variable as index in the loadermanager
    // if you use more than one
    adapter.clear();
    lm.initLoader(loaderIndex, null, this); // uses start to get the
}

/**
 * Create the data loader and bind the loader to the
 * parent callbacks
 * @param query array of <FeatureCircleTaskQuery> to pass to the loader
 * @param loaderIndex a unique id for query loader
 */
private void startDataLoading(CircleTaskQuery[] query, int loaderIndex) {
    // create task query

    // initialize Load Manager
    mCallbacks = this;
    LoaderManager lm = getSherlockActivity().getSupportLoaderManager();
    // NOTE: use the start variable as index in the loadermanager
    // if you use more than one
    adapter.clear();
    lm.initLoader(loaderIndex, null, this); // uses start to get the
}

/**
 * Create the data loader and bind the loader to the
 * parent callbacks
 * @param queryQueue2 array of <FeaturePolygonTaskQuery> to pass to the loader
 * @param loaderIndex a unique id for query loader
 */
private void startDataLoading(PolygonTaskQuery[] queryQueue2, int loaderIndex) {
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
                    GetFeatureInfoAttributeActivity.class);
            i.putExtras(getActivity().getIntent().getExtras());
            i.removeExtra(Constants.ParamKeys.LAYERS);
            // add a list with only one layer
            ArrayList<Layer<?>> subList = new ArrayList<Layer<?>>();
            FeatureInfoQueryResult item = (FeatureInfoQueryResult) parent
                    .getAdapter().getItem(position);
            subList.add(item.getLayer());
            i.putExtra(Constants.ParamKeys.LAYERS, subList);
            i.putExtra("start", 0);
            i.putExtra("limit", 1);
            //don't allow picking the position 
            String action = getActivity().getIntent().getAction();
            i.setAction(action);
            getActivity().startActivityForResult(i,
                    GetFeatureInfoAttributeActivity.GET_ITEM);
        }
    });
}


/**
 * Create the loader
 */
@Override
public Loader<List<FeatureInfoQueryResult>> onCreateLoader(int id, Bundle args) {

	if(selection_type.equals("Rectangular")){
		return new FeatureInfoLoader(getSherlockActivity(), queryQueueRect);
    }
    else 
    	if(selection_type.equals("Circular")){
    		return new FeatureCircleLoader(getSherlockActivity(), queryQueueCircle);
    	}
  
    return new FeaturePolygonLoader(getSherlockActivity(), queryQueuePolygon);
}

@Override
public void onLoadFinished(Loader<List<FeatureInfoQueryResult>> loader,
        List<FeatureInfoQueryResult> results) {
    //for each result, an entry is added to the list if
    // it contains a result
    for (FeatureInfoQueryResult result : results) {
        if (result.getFeatures().size() > 0) {
            adapter.add(result);
        }
    }
    if (adapter.isEmpty()) {
        setNoData();
    }
    stopLoadingGUI();

}



@Override
public void onLoaderReset(Loader<List<FeatureInfoQueryResult>> arg0) {
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