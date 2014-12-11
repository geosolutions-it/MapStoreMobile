/*
 * GeoSolutions - MapstoreMobile - GeoSpatial Framework on Android based devices
 * Copyright (C) 2014  GeoSolutions (www.geo-solutions.it)
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
package it.geosolutions.geocollect.android.core.mission;

import it.geosolutions.android.map.model.query.BBoxQuery;
import it.geosolutions.android.map.model.query.BaseFeatureInfoQuery;
import it.geosolutions.geocollect.android.core.R;
import it.geosolutions.geocollect.android.core.form.FormEditActivity;
import it.geosolutions.geocollect.android.core.mission.utils.MissionUtils;
import it.geosolutions.geocollect.android.core.mission.utils.PersistenceUtils;
import it.geosolutions.geocollect.android.core.mission.utils.SQLiteCascadeFeatureLoader;
import it.geosolutions.geocollect.model.config.MissionTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jsqlite.Database;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;


/**
 * A list fragment representing a list of Pending Missions. This fragment also
 * supports tablet devices by allowing list items to be given an 'activated'
 * state upon selection. This helps indicate which item is currently being
 * viewed in a {@link PendingMissionDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class PendingMissionListFragment 
	extends SherlockListFragment 
	implements  LoaderCallbacks<List<MissionFeature>>,	OnScrollListener, OnRefreshListener,OnQueryTextListener{

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	private static final int LOADER_INDEX = 0;

	private static final String TAG = "MISSION_LIST";

	public static final String INFINITE_SCROLL = "INFINITE_SCROLL";

	public static int ARG_ENABLE_GPS = 43231;

	/**
	 * mode of this fragment
	 */
	public enum FragmentMode
	{
		PENDING,
		CREATION
	}
	private FragmentMode mMode = FragmentMode.PENDING;
	
	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks listSelectionCallbacks = sDummyCallbacks;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;
	
	/**
	 * The loader to load WFS data
	 */
	private Loader loader;
	
	/**
	 * The adapter for the Feature
	 */
	private FeatureAdapter adapter;
	
	private MissionAdapter missionAdapter;
	
	/**
	 * Callback for the Loader
	 */
	private LoaderCallbacks<List<MissionFeature>> mCallbacks;

	/**
	 * SwipeRefreshLayout, use by the swipeDown gesture
	 */
	ListFragmentSwipeRefreshLayout mSwipeRefreshLayout;
		
	/**
	 * Boolean value that allow to start loading only once at time
	 */
	private boolean isLoading;
	/**
	 * The template that provides the form and the 
	 */
	private MissionTemplate missionTemplate;
	
	/**
	 * page number for remote queries
	 */
	private int page=0;
	/**
	 * page size for remote queries
	 */
	private int pagesize=100;

	private View footer;
	
	/**
	 * Main Activity's jsqlite Database instance reference
	 */
	private Database db;
	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(Object object);
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(Object object) {
		}
	};

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public PendingMissionListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v("MISSION_LIST_FRAGMENT", "onCreate()");

		setRetainInstance(true);
		// setup the listView
		missionTemplate =  MissionUtils.getDefaultTemplate(getSherlockActivity());
	    adapter = new FeatureAdapter(getSherlockActivity(),
	            R.layout.mission_resource_row,missionTemplate);
	    setListAdapter(adapter);
	    //option menu
	    setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	        Bundle savedInstanceState) {
		footer = View.inflate(getActivity(), R.layout.loading_footer, null);
	    
	    // Get the list fragment's content view
        final View listFragmentView = inflater.inflate(R.layout.mission_resource_list, container, false);
 
        // Now create a SwipeRefreshLayout to wrap the fragment's content view
        mSwipeRefreshLayout = new ListFragmentSwipeRefreshLayout(getSherlockActivity());
 
        // Add the list fragment's content view to the SwipeRefreshLayout, making sure that it fills
        // the SwipeRefreshLayout
        mSwipeRefreshLayout.addView(listFragmentView,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
 
        // Make sure that the SwipeRefreshLayout will fill the fragment
        mSwipeRefreshLayout.setLayoutParams(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
       
        mSwipeRefreshLayout.setColorScheme(R.color.geosol_1, R.color.geosol_2, R.color.geosol_3, R.color.geosol_4);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        // Now return the SwipeRefreshLayout as this fragment's content view
        return mSwipeRefreshLayout;
	    
	    
	    //return inflater.inflate(R.layout.mission_resource_list, container, false); 
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
	        //getListView().removeFooterView(footer);
	        Log.v(TAG, "task terminated");
	        
	    }
	    adapter.notifyDataSetChanged();
	    isLoading=false;
        if(mSwipeRefreshLayout != null)
        	mSwipeRefreshLayout.setRefreshing(false);

	}
	
	/**
	 * Sets the view to show that no data are available
	 */
	private void setNoData() {
	    ((TextView) getView().findViewById(R.id.empty_text))
	            .setText(R.string.no_reporting_found);
	    getView().findViewById(R.id.progress_bar).setVisibility(TextView.GONE);
	}
	
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if(getActivity().getIntent().getBooleanExtra(INFINITE_SCROLL, false)){
			getListView().setOnScrollListener(this);
		}else{
			getListView().setOnScrollListener(new OnScrollListener() {
				
				@Override
				public void onScrollStateChanged(AbsListView view, int scrollState) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onScroll(AbsListView view, int firstVisibleItem,
						int visibleItemCount, int totalItemCount) {
					//Log.v("PMLF", "First: "+ firstVisibleItem + ", count: "+visibleItemCount+ ", total: "+totalItemCount);
					if(firstVisibleItem == 0 || visibleItemCount == 0){
						mSwipeRefreshLayout.setEnabled(true);
					}else{
						mSwipeRefreshLayout.setEnabled(false);
					}

					
				}
			});
		}
		
		
		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}

		startDataLoading(missionTemplate, LOADER_INDEX);

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		Log.v("MISSION_LIST_FRAGMENT", "onAttach()");
		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		// If a previous instance of the database was attached, the loader must be restarted
		boolean needReload = false;
		if(db != null && db.dbversion().equals("unknown")){
			needReload = true;
		}
		
		// Check for a database
		if(	getSherlockActivity() instanceof PendingMissionListActivity){
			Log.v(TAG, "Loader: Connecting to Activity database");
			db = ((PendingMissionListActivity)getSherlockActivity()).spatialiteDatabase;
			// restart the loader if needed
			if(needReload){
				LoaderManager lm = getSherlockActivity().getSupportLoaderManager();
				if(lm.getLoader(LOADER_INDEX) != null){
				    lm.restartLoader(LOADER_INDEX, null, this); 
				}
			}
		}else{
			Log.w(TAG, "Loader: Could not connect to Activity database");
		}
		
		listSelectionCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		listSelectionCallbacks = sDummyCallbacks;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		listSelectionCallbacks.onItemSelected(getListAdapter().getItem(position));
	}

	@Override
	public void onSaveInstanceState(Bundle outState) { 
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}

	/**
	 * Creates the actionBar buttons
	 */
	@Override
	public void onCreateOptionsMenu(
	      final Menu menu, MenuInflater inflater) {
		
		if(mMode == FragmentMode.CREATION){
			inflater.inflate(R.menu.creating, menu);
			return;
		}

		// If SRID is set, a filter exists
		SharedPreferences sp = getSherlockActivity().getSharedPreferences(SQLiteCascadeFeatureLoader.PREF_NAME, Context.MODE_PRIVATE);
		if(sp.contains(SQLiteCascadeFeatureLoader.FILTER_SRID)){
			inflater.inflate(R.menu.filterable, menu);
		}

		inflater.inflate(R.menu.searchable, menu);
		
		//get searchview and add querylistener 

		SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
		searchView.setQueryHint(getString(R.string.search_missions));
		searchView.setOnQueryTextListener(this);
		searchView.setOnQueryTextFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {

				if(!hasFocus){
					//keyboard was closed, collapse search action view
					menu.findItem(R.id.search).collapseActionView();
				}
			}
		}); 

		if ( missionTemplate != null 
				&& missionTemplate.schema_sop != null
				&& missionTemplate.schema_sop.orderingField != null){
			inflater.inflate(R.menu.orderable, menu);
			MenuItem orderButton = menu.findItem(R.id.order);
			if(orderButton != null){
				String stringFormat = getResources().getString(R.string.order_by);
				String formattedTitle = String.format(stringFormat, missionTemplate.schema_sop.orderingField);
				orderButton.setTitle(formattedTitle);
			}
		}
		
		//inflater.inflate(R.menu.refreshable, menu);
	}
	@Override
    public boolean onQueryTextSubmit(String query) {
        return query.length() > 0;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        if (adapter != null){  
        	//filters the adapters entries using its overridden filter
        	adapter.getFilter().filter(newText);
        }
        return true;
    }
	
	/* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragment#onOptionsItemSelected(com.actionbarsherlock.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		int id = item.getItemId();
		if (id == R.id.create_new){
			
			if(!isGPSAvailable()){
		
				new AlertDialog.Builder(getActivity())
			    .setTitle(R.string.app_name)
			    .setMessage(R.string.gps_not_enabled)
			    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) { 

			        	startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS),ARG_ENABLE_GPS);
			        }
			     })
			     .show();
			}else{
				
				startMissionFeatureCreation();
			}
			
			
		
			return true;
			
		}else if (id==R.id.order){

			// Get it from the mission template
			if(loader !=null){
				
				SharedPreferences sp = getSherlockActivity().getSharedPreferences(SQLiteCascadeFeatureLoader.PREF_NAME, Context.MODE_PRIVATE);
				boolean reverse = sp.getBoolean(SQLiteCascadeFeatureLoader.REVERSE_ORDER_PREF, false);
				SharedPreferences.Editor editor = sp.edit();
				// Change the ordering
				Log.v(TAG, "Changing to "+reverse);
				editor.putBoolean(SQLiteCascadeFeatureLoader.REVERSE_ORDER_PREF, !reverse);
				editor.commit();

				adapter.clear();
				loader.forceLoad();
			}
			return true;
			
		} else if (id==R.id.filter){

			// Clear the Spatial Filter
			if(loader !=null){
				
				SharedPreferences sp = getSherlockActivity().getSharedPreferences(SQLiteCascadeFeatureLoader.PREF_NAME, Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = sp.edit();
				// Change the ordering
				editor.remove(SQLiteCascadeFeatureLoader.FILTER_N);
				editor.remove(SQLiteCascadeFeatureLoader.FILTER_S);
				editor.remove(SQLiteCascadeFeatureLoader.FILTER_W);
				editor.remove(SQLiteCascadeFeatureLoader.FILTER_E);
				editor.remove(SQLiteCascadeFeatureLoader.FILTER_SRID);
				editor.commit();

				adapter.clear();
				loader.forceLoad();
				item.setVisible(false);
			}
			return true;
		}

		
		return super.onOptionsItemSelected(item);
	}
	/**
	 * checks if location services are available
	 */
	private boolean isGPSAvailable(){
		LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE );
		return manager.isProviderEnabled(LocationManager.GPS_PROVIDER) || manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		
	}
	/**
	 * starts the missionfeature creation
	 */
	private void startMissionFeatureCreation() {
		
		Intent i = new Intent(getSherlockActivity(),FormEditActivity.class);
		i.putExtra(PendingMissionListActivity.ARG_CREATE_MISSIONFEATURE, true);
		startActivityForResult(i, FormEditActivity.FORM_CREATE_NEW_MISSIONFEATURE);
		
	}

	/* (non-Javadoc)
	 * @see android.widget.AbsListView.OnScrollListener#onScrollStateChanged(android.widget.AbsListView, int)
	 */
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see android.widget.AbsListView.OnScrollListener#onScroll(android.widget.AbsListView, int, int, int)
	 */
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		
		Log.v("PMLF", "First: "+ firstVisibleItem + ", count: "+visibleItemCount+ ", total: "+totalItemCount);
		if(firstVisibleItem == 0 || visibleItemCount == 0){
			mSwipeRefreshLayout.setEnabled(true);
		}else{
			mSwipeRefreshLayout.setEnabled(false);
		}
		
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
	    
	    //if the last item is visible and can load more resources
	    //load more resources
	    int l = visibleItemCount + firstVisibleItem;
	    if (l >= totalItemCount && !isLoading ) {
	        // It is time to add new data. We call the listener
	        //getListView().addFooterView(footer);
	    	
	        isLoading = true;
	        loadMore();
	    }
		
	}
	/* (non-Javadoc)
	 * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onCreateLoader(int, android.os.Bundle)
	 */
	@Override
	public Loader<List<MissionFeature>> onCreateLoader(int arg0, Bundle arg1) {
		getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
		getSherlockActivity().getSupportActionBar();

		Log.v("MISSION_LIST", "onCreateLoader()");
		
		loader = MissionUtils.createMissionLoader(missionTemplate,getSherlockActivity(),page,pagesize,db);
	    return loader; 
	}

	/**
	 * 
	 */
	/* (non-Javadoc)
	 * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoadFinished(android.support.v4.content.Loader, java.lang.Object)
	 */
	@Override
	public void onLoadFinished(Loader<List<MissionFeature>> loader, List<MissionFeature> results) {
		if(results == null){
			   Toast.makeText(getSherlockActivity(), R.string.error_connectivity_problem, Toast.LENGTH_SHORT).show();
			   setNoData();
		   }else{
			   
			   //add loaded resources to the listView
				for(MissionFeature a : results ){
					adapter.add(a);
				}
				if (adapter.isEmpty()) {
			        setNoData();
			    }else{
			    }
		   }
		   stopLoadingGUI();
	}
	

	/* (non-Javadoc)
	 * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoaderReset(android.support.v4.content.Loader)
	 */
	@Override
	public void onLoaderReset(Loader<List<MissionFeature>> arg0) {
		adapter.clear();
        if(mSwipeRefreshLayout != null)
        	mSwipeRefreshLayout.setRefreshing(false);

	}
	
	/**
	 * Loads more data from the Loader
	 */
	protected void loadMore() {
		
		if(loader!=null){
			
				page++;
				getLoaderManager().restartLoader(LOADER_INDEX, null, this);
			
			
		}
	}
	
	/**
	 * Create the data loader and bind the loader to the
	 * parent callbacks
	 * @param URL (not used for now)
	 * @param loaderIndex a unique id for query loader
	 */
	private void startDataLoading(MissionTemplate t, int loaderIndex) {

	    // initialize Load Manager
	    mCallbacks = this;
	    //reset page
	    LoaderManager lm = getSherlockActivity().getSupportLoaderManager();
	    adapter.clear();
	    page=0;
	    lm.initLoader(loaderIndex, null, this); 
	}
	
	/**
	 * Refresh the list
	 */
	@Override
	public void onResume() {
		super.onResume();

		if(mMode == FragmentMode.CREATION){
			if(missionAdapter != null){
				fillCreatedMissionFeatureAdapter();
			}
		}else if(mMode == FragmentMode.PENDING){			
			if(adapter != null && loader != null){
				adapter.clear();
				loader.forceLoad();
			}
		}
			
	}	

	/**
	 * Handle the results
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult()");
	    super.onActivityResult(requestCode, resultCode, data);
	    if(requestCode == PendingMissionListActivity.SPATIAL_QUERY){
	    	
	    	if(data!= null && data.hasExtra("query")){
	    	    BaseFeatureInfoQuery query = data.getParcelableExtra("query");
	    	    
	    	    // Create task query
	    	    if(query instanceof BBoxQuery){
	    	    	BBoxQuery bbox = (BBoxQuery) query;
	    	    	
	    	    	// TODO: Refactor the Preferences Double Storage
	    	    	// This is just a fast hack to store Doubles in Long but It should be handled by a more clean utility class
	    	    	// Maybe extend the Preference Editor?
	    	    	
	    	    	Log.v(TAG, "Received:\nN: "+bbox.getN()+"\nN: "+bbox.getS()+"\nE: "+bbox.getE()+"\nW: "+bbox.getW()+"\nSRID: "+bbox.getSrid());
					SharedPreferences sp = getSherlockActivity().getSharedPreferences(SQLiteCascadeFeatureLoader.PREF_NAME, Context.MODE_PRIVATE);
					SharedPreferences.Editor editor = sp.edit();
					editor.putLong(SQLiteCascadeFeatureLoader.FILTER_N, Double.doubleToRawLongBits( bbox.getN()));
					editor.putLong(SQLiteCascadeFeatureLoader.FILTER_S, Double.doubleToRawLongBits( bbox.getS()));
					editor.putLong(SQLiteCascadeFeatureLoader.FILTER_W, Double.doubleToRawLongBits( bbox.getW()));
					editor.putLong(SQLiteCascadeFeatureLoader.FILTER_E, Double.doubleToRawLongBits( bbox.getE()));
					editor.putInt(SQLiteCascadeFeatureLoader.FILTER_SRID, Integer.parseInt(bbox.getSrid()));
					editor.commit();

					adapter.clear();
					loader.forceLoad();
					// TODO: This call could need to move the onCreateMenu() code into a onPrepareOptionMenu()
					getSherlockActivity().supportInvalidateOptionsMenu();
					

					Toast.makeText(getActivity(), getString(R.string.selection_filtered), Toast.LENGTH_SHORT).show();
	    	    }

	    	    //else if(query instanceof CircleQuery){ }
	    		
	    	}else{
	    		
	    		// No result, clean the filter
	    		SharedPreferences sp = getSherlockActivity().getSharedPreferences(SQLiteCascadeFeatureLoader.PREF_NAME, Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = sp.edit();
				editor.remove(SQLiteCascadeFeatureLoader.FILTER_N);
				editor.remove(SQLiteCascadeFeatureLoader.FILTER_S);
				editor.remove(SQLiteCascadeFeatureLoader.FILTER_W);
				editor.remove(SQLiteCascadeFeatureLoader.FILTER_E);
				editor.remove(SQLiteCascadeFeatureLoader.FILTER_SRID);
				editor.commit();

				adapter.clear();
				loader.forceLoad();
				// TODO: This call could need to move the onCreateMenu() code into a onPrepareOptionMenu()
				getSherlockActivity().supportInvalidateOptionsMenu();
	    	}

	    }else if(requestCode == ARG_ENABLE_GPS ){
			Log.d(FormEditActivity.class.getSimpleName(), "back from GPS settings");

			if(!isGPSAvailable()){
				Toast.makeText(getActivity(), R.string.gps_still_not_enabled, Toast.LENGTH_LONG).show();
			}else{				
				startMissionFeatureCreation();
			}
		}
	}

	/**
	 * Callback for the {@link SwipeRefreshLayout}
	 */
	@Override
	public void onRefresh() {
		
		SharedPreferences sp = getSherlockActivity().getSharedPreferences(SQLiteCascadeFeatureLoader.PREF_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		// Reset the preference to force update
		editor.putLong(SQLiteCascadeFeatureLoader.LAST_UPDATE_PREF, 0);
		editor.commit();
		
		adapter.clear();
		loader.forceLoad();
        if(mSwipeRefreshLayout != null)
        	mSwipeRefreshLayout.setRefreshing(true);

	}
	
    /**
     * Utility method to check whether a {@link ListView} can scroll up from it's current position.
     * Handles platform version differences, providing backwards compatible functionality where
     * needed.
     */
    private static boolean canListViewScrollUp(ListView listView) {
        if (android.os.Build.VERSION.SDK_INT >= 14) {
            // For ICS and above we can call canScrollVertically() to determine this
            return ViewCompat.canScrollVertically(listView, -1);
        } else {
            // Pre-ICS we need to manually check the first visible item and the child view's top
            // value
            return listView.getChildCount() > 0 &&
                    (listView.getFirstVisiblePosition() > 0
                            || listView.getChildAt(0).getTop() < listView.getPaddingTop());
        }
    }
    
    /**
     * Sub-class of {@link android.support.v4.widget.SwipeRefreshLayout} for use in this
     * {@link android.support.v4.app.ListFragment}. The reason that this is needed is because
     * {@link android.support.v4.widget.SwipeRefreshLayout} only supports a single child, which it
     * expects to be the one which triggers refreshes. In our case the layout's child is the content
     * view returned from
     * {@link android.support.v4.app.ListFragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)}
     * which is a {@link android.view.ViewGroup}.
     *
     * <p>To enable 'swipe-to-refresh' support via the {@link android.widget.ListView} we need to
     * override the default behavior and properly signal when a gesture is possible. This is done by
     * overriding {@link #canChildScrollUp()}.
     */
    private class ListFragmentSwipeRefreshLayout extends SwipeRefreshLayout {
 
        public ListFragmentSwipeRefreshLayout(Context context) {
            super(context);
        }
 
        /**
         * As mentioned above, we need to override this method to properly signal when a
         * 'swipe-to-refresh' is possible.
         *
         * @return true if the {@link android.widget.ListView} is visible and can scroll up.
         */
        @Override
        public boolean canChildScrollUp() {
            final ListView listView = getListView();
            if (listView.getVisibility() == View.VISIBLE) {
                return canListViewScrollUp(listView);
            } else {
                return false;
            }
        }
 
    }
    /**
     * switches between pending mission mode and created mission mode
     * @param FragmentMode the new mode
     */
    public void switchAdapter(FragmentMode newMode){
    	
    	if(newMode == mMode){
    		return;
    	}
    	
    	mMode = newMode;
    	
    	if(mMode == FragmentMode.CREATION){

    		missionAdapter = new MissionAdapter(getSherlockActivity(), R.layout.created_mission_row);
    		
    		//delete created items on long click listener
    		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

				@Override
				public boolean onItemLongClick(AdapterView<?> parent,View view,final int position, long id) {
					
					new AlertDialog.Builder(getSherlockActivity())
				    .setTitle(R.string.my_inspections)
				    .setMessage(R.string.created_inspection_delete)
				    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int which) { 
				        	dialog.dismiss();
				        	//nothing		        	
				        	final MissionFeature f = (MissionFeature) getListView().getItemAtPosition(position);
				        	
				        	final MissionTemplate t = MissionUtils.getDefaultTemplate(getSherlockActivity());
				        	
				        	final Database db = ((PendingMissionListActivity)getSherlockActivity()).spatialiteDatabase;
				        	
				        	PersistenceUtils.deleteCreatedMissionFeature(db, t.schema_seg.localSourceStore+ "_new", f);
				        	
				        	fillCreatedMissionFeatureAdapter();
				        }
				     })
				     .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					        public void onClick(DialogInterface dialog, int which) { 
					        	//nothing		
					        	dialog.dismiss();
					        }
					     })
					 .show();
					
					return true;
				}
			});

    		if(	getSherlockActivity() instanceof PendingMissionListActivity){

    			fillCreatedMissionFeatureAdapter();

    		}

    		setListAdapter(missionAdapter);
    		

    	}else if (mMode == FragmentMode.PENDING){
    		
    		//remove longclicklistener
    		getListView().setOnItemLongClickListener(null);
    		
    		setListAdapter(adapter);
    		
    	}
    	
    	//invalidate actionbar
    	getSherlockActivity().supportInvalidateOptionsMenu();
    	
    	
    }
    /**
     * loads the locally available "created missionfeatures" from the database
     */
    private void fillCreatedMissionFeatureAdapter(){
    	
    	final MissionTemplate t = MissionUtils.getDefaultTemplate(getSherlockActivity());

    	final Database db = ((PendingMissionListActivity)getSherlockActivity()).spatialiteDatabase;

		Log.v(TAG, "Loading created missions");
		final ArrayList<MissionFeature> missions = MissionUtils.getCreatedMissionFeatures(t.schema_seg.localSourceStore+ "_new", db);
		
		final String prio = t.priorityField;
		final HashMap<String,String> colors = t.priorityValuesColors;
		if(prio != null && colors != null){			
			for(MissionFeature f : missions){
				if ( f.properties.containsKey(prio)){
					 f.displayColor = colors.get(f.properties.get(prio));
				}
			}
		}

		missionAdapter.clear();
		
		missionAdapter.addAll(missions);

		missionAdapter.notifyDataSetChanged();
		
		if (missionAdapter.isEmpty()) {
	        setNoData();
	    }
    }
    
	/**
	 * returns the current mode of this fragment
	 * either PENDING or CREATING
	 */
	public FragmentMode getFragmentMode(){
		return mMode;
	}
    /**
     * Adapter for MissionFeatures "created missions"
     *
     */
	public class MissionAdapter extends ArrayAdapter<MissionFeature>{

		private int resourceId;
		
		public MissionAdapter(Context context, int resource) {
			super(context, resource);
			
			this.resourceId = resource;
		}
		
		public View getView(int position, View convertView, ViewGroup parent) {

		    // assign the view we are converting to a local variable
		    View v = convertView;

		    // first check to see if the view is null. if so, we have to inflate it.
		    // to inflate it basically means to render, or show, the view.
		    if (v == null) {
		        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		        v = inflater.inflate(resourceId, null);
		    }

		    /*
		     * Recall that the variable position is sent in as an argument to this
		     * method. The variable simply refers to the position of the current object
		     * in the list. (The ArrayAdapter iterates through the list we sent it)
		     * Therefore, i refers to the current Item object.
		     */
		    MissionFeature mission = getItem(position);

		    if (mission != null) {

		    	// display name

		    	TextView name = (TextView) v.findViewById(R.id.created_mission_resource_name);
		    	if (name != null && mission.properties != null && mission.properties.containsKey("CODICE")) {
		    		Object prop =mission.properties.get("CODICE");
		    		if(prop!=null){
		    			name.setText(prop.toString());
		    		}else{
		    			name.setText("");
		    		}

		    	}

		    	//display rifiuti

		    	TextView desc = (TextView) v.findViewById(R.id.created_mission_resource_description);
		    	if (desc != null && mission.properties != null && mission.properties.containsKey("RIFIUTI_NO")) {
		    		Object prop =mission.properties.get("RIFIUTI_NO");
		    		if(prop!=null){
		    			desc.setText(prop.toString());
		    		}else{
		    			desc.setText("");
		    		}

		    	}
		    	
				ImageView priorityIcon = (ImageView) v.findViewById(R.id.created_mission_resource_priority_icon);
				if ( priorityIcon != null && priorityIcon.getDrawable() != null ){
					
					// Get the icon and tweak the color
					Drawable d = priorityIcon.getDrawable();
					
					if ( mission.displayColor != null ){
						try{
							d.mutate().setColorFilter(Color.parseColor(mission.displayColor), PorterDuff.Mode.SRC_ATOP);
						}catch(IllegalArgumentException iae){
							Log.e("MissionAdapter", "A feature has an incorrect color value" );
						}
			    	}else{
			    		d.mutate().clearColorFilter();
			    	}

		    	}

		    }
		    return v;

		}
		
	}
}
