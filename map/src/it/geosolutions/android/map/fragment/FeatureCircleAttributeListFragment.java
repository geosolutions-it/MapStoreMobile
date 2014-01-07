package it.geosolutions.android.map.fragment;

import it.geosolutions.android.map.R;
import it.geosolutions.android.map.activities.GetFeatureInfoLayerListActivity;
import it.geosolutions.android.map.adapters.FeatureInfoAttributesAdapter;
import it.geosolutions.android.map.loaders.FeatureCircleLoader;
import it.geosolutions.android.map.model.Feature;
import it.geosolutions.android.map.model.FeatureCircleQuery;
import it.geosolutions.android.map.model.FeatureCircleQueryResult;
import it.geosolutions.android.map.model.FeatureCircleTaskQuery;

import it.geosolutions.android.map.utils.FeatureInfoUtils;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListFragment;

/**
 * This fragment shows a view containing the attributes of a single feature from a
 * selected layer Supports pagination and returns to the activity in case of
 * selection.
 * 
 * @author Jacopo Pianigiani (jacopo.pianigiani85@gmail.com)
 */
public class FeatureCircleAttributeListFragment extends SherlockListFragment
implements LoaderManager.LoaderCallbacks<List<FeatureCircleQueryResult>>{
	
	private FeatureInfoAttributesAdapter adapter;
	private FeatureCircleTaskQuery[] queryQueue;
	
	// The callbacks through which we will interact with the LoaderManager.
	private LoaderManager.LoaderCallbacks<List<FeatureCircleQueryResult>> mCallbacks;

	protected Integer start;

	protected Integer limit;

	protected FeatureCircleQuery query;

	protected ArrayList<String> layers;

	protected ArrayList<Feature> currentFeatures;

	/**
	 * Called only once
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    // view operations

	    setRetainInstance(true);
	    // start progress bars
	    getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
	    getSherlockActivity().setSupportProgressBarVisibility(true);

	    // get data from the intent
	    // TODO get them from arguments
	    Bundle extras = getActivity().getIntent().getExtras();
	    ;
	    // TODO get already loaded data;
	    query = (FeatureCircleQuery) extras.getParcelable("query");
	    layers = extras.getStringArrayList("layers");
	    start = extras.getInt("start");
	    limit = extras.getInt("limit");

	    // setup the listView
	    adapter = new FeatureInfoAttributesAdapter(getSherlockActivity(),
	            R.layout.feature_info_attribute_row);
	    setListAdapter(adapter);
	    startDataLoading(query, layers, start, 2);// use 2 to check availability of
	                                              // the next page
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	        Bundle savedInstanceState) {
	    startDataLoading(query, layers, start, 2);
	    return inflater.inflate(R.layout.feature_info_attribute_list, container,
	            false);
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.ListFragment#onViewCreated(android.view.View,
	 * android.os.Bundle)
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {

	    super.onViewCreated(view, savedInstanceState);
	    setButtonBarVisibility(currentFeatures);
	    startLoadingGUI();
	    ImageButton prev = (ImageButton) view.findViewById(R.id.previousButton);
	    ImageButton next = (ImageButton) view.findViewById(R.id.nextButton);
	    ImageButton marker = (ImageButton) view.findViewById(R.id.use_for_marker);

	    // load the previous page on click
	    prev.setOnClickListener(new OnClickListener() {

	        @Override
	        public void onClick(View v) {

	            startLoadingGUI();
	            adapter.clear();
	            start--;
	            startDataLoading(query, layers, start, 2);

	        }
	    });

	    // load the next page on press
	    next.setOnClickListener(new OnClickListener() {

	        @Override
	        public void onClick(View v) {

	            startLoadingGUI();
	            adapter.clear();
	            start++;
	            startDataLoading(query, layers, start, 2);

	        }
	    });
	    // show a dialog and return if ok
	    final Context context = this.getActivity();
	    marker.setOnClickListener(new OnClickListener() {

	        @Override
	        public void onClick(View v) {
	            AlertDialog.Builder confirm = new AlertDialog.Builder(context);
	            confirm.setTitle(R.string.use_this_feature);
	            confirm.setMessage(R.string.use_this_feature_description);
	            confirm.setPositiveButton(android.R.string.ok,
	                    new DialogInterface.OnClickListener() {
	                        public void onClick(DialogInterface dialog, int id) {
	                            returnSelectedItem();
	                        }
	                    });
	            confirm.setNegativeButton(android.R.string.cancel,
	                    new DialogInterface.OnClickListener() {
	                        public void onClick(DialogInterface dialog, int id) {
	                            // TODO close
	                            dialog.cancel();
	                        }
	                    });
	            AlertDialog alert = confirm.create();
	            alert.show();

	        }
	    });
	}
	
	/**
	 * Create an array of <FeatureInfoTaskQuery> to pass to the loader and
	 * initialize the loader
	 * 
	 * @param query the <FeatureInfoQuery> with bbox
	 * @param layers array of <String> to generate the queryQueue
	 * @param start
	 * @param limit
	 */
	private void startDataLoading(FeatureCircleQuery query, ArrayList<String> layers,
	        Integer start, Integer limit) {
	    // create task query
	    queryQueue = FeatureInfoUtils.createTaskQueryQueue(layers, query, start,
	            limit);

	    // initialize Load Manager
	    mCallbacks = this;
	    LoaderManager lm = getSherlockActivity().getSupportLoaderManager();
	    // NOTE: use the start variable as index in the loadermanager
	    // if you use more than one
	    adapter.clear();
	    lm.initLoader(start, null, this); // uses start to get the
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
	    super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
	    super.onAttach(activity);
	    // mCallbacks = (TaskCallbacks) activity;
	}

	@Override
	public void onDetach() {
	    super.onDetach();
	    // mCallbacks = null;
	}

	/*
	 * (non-Javadoc)
	 * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onCreateLoader(int,
	 * android.os.Bundle)
	 */
	@Override
	public Loader<List<FeatureCircleQueryResult>> onCreateLoader(int id, Bundle args) {

	    return new FeatureCircleLoader(getSherlockActivity(), queryQueue);
	}

	// populate the list and set buttonbar visibility options
	@Override
	public void onLoadFinished(Loader<List<FeatureCircleQueryResult>> loader,
	        List<FeatureCircleQueryResult> data) {
	    setListAdapter(adapter);
	    if (data.size() > 0) {
	        // only one layer display
	        FeatureCircleQueryResult result = data.get(0);
	        currentFeatures = result.getFeatures();
	        setButtonBarVisibility(currentFeatures);
	        if (currentFeatures.size() > 0) {
	            // only the first feature display.
	            // other will be used to check availability
	            adapter.addAll(currentFeatures.get(0));
	        }

	    } else {
	        setButtonBarVisibility(null);
	    }

	    Log.v("FEATURE_INFO", "added " + adapter.getCount() + " items to the view");
	    stopLoadingGUI();

	}

	/**
	 * sets no data view in default listview empty text
	 */
	private void setNoData() {
	    ((TextView) getView().findViewById(R.id.empty_text))
	            .setText(R.string.feature_info_extracting_no_result);
	}

	/**
	 * Set the loading bar and loading text
	 */
	private void startLoadingGUI() {
	    if (getSherlockActivity() != null) {
	        // start progress bars
	        getSherlockActivity()
	                .setSupportProgressBarIndeterminateVisibility(true);
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
	        getSherlockActivity().setSupportProgressBarIndeterminateVisibility(
	                false);
	        getSherlockActivity().setSupportProgressBarVisibility(false);
	        Log.v("FEATURE_INFO_TASK", "task terminated");

	    }
	    adapter.notifyDataSetChanged();
	}

	/**
	 * Set the visibility using the size of the features
	 * 
	 * @param features
	 */
	private void setButtonBarVisibility(ArrayList<Feature> features) {
	    if (features == null) {
	        getView().findViewById(R.id.attributeButtonBar).setVisibility(
	                View.INVISIBLE);
	        setNoData();
	        return;
	    }
	    if (features.size() > 0) {
	        getView().findViewById(R.id.attributeButtonBar).setVisibility(
	                View.VISIBLE);
	        //previous button
	        if (start > 0) {
	            getView().findViewById(R.id.previousButton).setVisibility(
	                    View.VISIBLE);
	        } else {
	            getView().findViewById(R.id.previousButton).setVisibility(
	                    View.INVISIBLE);
	        }
	        //next button
	        if (features.size() > 1) {
	            getView().findViewById(R.id.nextButton).setVisibility(View.VISIBLE);
	        } else {
	            getView().findViewById(R.id.nextButton).setVisibility(
	                    View.INVISIBLE);
	        }
	        //marker button
	        if(Intent.ACTION_VIEW.equals(getActivity().getIntent().getAction())){
	            getView().findViewById(R.id.use_for_marker).setVisibility(View.INVISIBLE);
	        }else{
	            getView().findViewById(R.id.use_for_marker).setVisibility(View.VISIBLE);

	        }
	    } else {
	        getView().findViewById(R.id.attributeButtonBar).setVisibility(
	                View.INVISIBLE);
	        setNoData();
	    }

	}

	@Override
	public void onLoaderReset(Loader<List<FeatureCircleQueryResult>> arg0) {
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

	private void returnSelectedItem() {
	    Intent returnIntent = new Intent();
	    Activity activity = getSherlockActivity();
	    // get current markers
	    // currentFeatures is present
	    if (currentFeatures != null && currentFeatures.size() > 0) {
	        returnIntent.putExtra(
	                GetFeatureInfoLayerListActivity.RESULT_FEATURE_EXTRA,
	                currentFeatures.get(0));
	        returnIntent.putExtra(
	                GetFeatureInfoLayerListActivity.LAYER_FEATURE_EXTRA,
	                layers.get(0));
	        activity.setResult(Activity.RESULT_OK, returnIntent);
	    	}
	    activity.finish();
		}

}

