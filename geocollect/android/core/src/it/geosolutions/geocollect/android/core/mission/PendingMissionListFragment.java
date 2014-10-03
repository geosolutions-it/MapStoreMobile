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
import it.geosolutions.geocollect.android.core.mission.utils.MissionUtils;
import it.geosolutions.geocollect.android.core.mission.utils.SQLiteCascadeFeatureLoader;
import it.geosolutions.geocollect.model.config.MissionTemplate;

import java.util.List;

import jsqlite.Database;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;


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
	implements  LoaderCallbacks<List<MissionFeature>>,	OnScrollListener, OnRefreshListener{

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	private static final int LOADER_INDEX = 0;

	private static final String TAG = "MISSION_LIST";

	public static final String INFINITE_SCROLL = "INFINITE_SCROLL";

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
	      Menu menu, MenuInflater inflater) {
		// If SRID is set, a filter exists
		SharedPreferences sp = getSherlockActivity().getSharedPreferences(SQLiteCascadeFeatureLoader.PREF_NAME, Context.MODE_PRIVATE);
		if(sp.contains(SQLiteCascadeFeatureLoader.FILTER_SRID)){
			inflater.inflate(R.menu.filterable, menu);
		}
		
		if ( missionTemplate != null 
				&& missionTemplate.source != null
				&& missionTemplate.source.orderingField != null){
			inflater.inflate(R.menu.orderable, menu);
			MenuItem orderButton = menu.findItem(R.id.order);
			if(orderButton != null){
				String stringFormat = getResources().getString(R.string.order_by);
				String formattedTitle = String.format(stringFormat, missionTemplate.source.orderingField);
				orderButton.setTitle(formattedTitle);
			}
		}
		
		//inflater.inflate(R.menu.refreshable, menu);
	}
	
	/* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragment#onOptionsItemSelected(com.actionbarsherlock.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		int id = item.getItemId();
		if (id==R.id.order){

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
	 * Shows the "editing" icon upon resuming from launched detail activity
	 */
	@Override
	public void onResume() {
		super.onResume();
		if(getListView()!= null){
			int pos = getListView().getCheckedItemPosition();
			int max = getListView().getCount();
			if( pos != AbsListView.INVALID_POSITION && pos < max){
				MissionFeature f = (MissionFeature) getListView().getItemAtPosition(pos);
				if(f != null){
					f.editing = true;
					getListView().invalidateViews();
				}
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
}
