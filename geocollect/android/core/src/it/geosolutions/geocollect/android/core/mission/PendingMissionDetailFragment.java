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

import it.geosolutions.android.map.fragment.MapFragment;
import it.geosolutions.android.map.wfs.geojson.feature.Feature;
import it.geosolutions.geocollect.android.core.R;
import it.geosolutions.geocollect.android.core.form.FormEditActivity;
import it.geosolutions.geocollect.android.core.form.utils.FormBuilder;
import it.geosolutions.geocollect.android.core.mission.utils.MissionUtils;
import it.geosolutions.geocollect.android.core.mission.utils.PersistenceUtils;
import it.geosolutions.geocollect.model.config.MissionTemplate;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * A fragment representing a single Pending Mission detail screen. This fragment
 * is either contained in a {@link PendingMissionListActivity} in two-pane mode
 * (on tablets) or a {@link PendingMissionDetailActivity} on handsets.
 */
public class PendingMissionDetailFragment extends MapFragment implements LoaderCallbacks<Void> {
	/**
	 * Tag for logging
	 */
	public static final String TAG = "MissionDetail";
	public static final int EDIT_ACTIVITY_CODE = 0;
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";
	public static final String ARG_ITEM_FEATURE = "item_FEATURE";
	/**
	 * The <ScrollView> that display fragment content
	 */
	private ScrollView mScrollView;
	/**
	 * The <LinearLayout> inside the <ScrollView> 
	 * This displays dynamic content as the other forms.
	 */
	private LinearLayout mFormView;
	/**
	 * <ProgressBar> for loading
	 */
	private ProgressBar mProgressView;
	private boolean mDone;
	protected Mission mission;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			//TODO get the Feature from db
		}
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView(): container = " + container
				+ "savedInstanceState = " + savedInstanceState);

		if (mScrollView == null) {
			// normally inflate the view hierarchy
			mScrollView = (ScrollView) inflater.inflate(R.layout.preview_page_fragment,
					container, false);
			mFormView = (LinearLayout) mScrollView.findViewById(R.id.formcontent);
			mProgressView = (ProgressBar) mScrollView
					.findViewById(R.id.loading);
		} else {
			// mScrollView is still attached to the previous view hierarchy
			// we need to remove it and re-attach it to the current one
			ViewGroup parent = (ViewGroup) mScrollView.getParent();
			parent.removeView(mScrollView);
		}
		//TODO it should be false because orientation change can 
		// have different layout (for map view )
		//setRetainInstance(true);
		return mScrollView;


	}
	
	@Override
	public void onCreateOptionsMenu(
	      Menu menu, MenuInflater inflater) {
	   inflater.inflate(R.menu.editable, menu);
	}

	/* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragment#onOptionsItemSelected(com.actionbarsherlock.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		int id = item.getItemId();
		if(id==R.id.accept){
			Intent i = new Intent(getSherlockActivity(),FormEditActivity.class);
			i.putExtra("MISSION", mission);
			startActivityForResult(i, EDIT_ACTIVITY_CODE);
			return true;
		}
		
		
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Handle the results
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult()");
	    super.onActivityResult(requestCode, resultCode, data);
	    if(requestCode == EDIT_ACTIVITY_CODE){
			MissionTemplate t = MissionUtils.getDefaultTemplate(getSherlockActivity());
			PersistenceUtils.loadPageData(t.preview, mFormView, mission, getSherlockActivity());

	    }
	}
	
	/**
	 * Fills the Page layout with widgets based on page template
	 */
	private void buildForm() {
		// if the view hierarchy was already build, skip this
		if (mDone)
			return;
		MissionTemplate t = MissionUtils.getDefaultTemplate(getSherlockActivity());
		FormBuilder.buildForm(getActivity(), this.mFormView,t.preview.fields,mission);//TODO page is not enough, some data should be accessible like constants and data

		PersistenceUtils.loadPageData(t.preview, mFormView, mission, getSherlockActivity());
		// the view hierarchy is now complete
		mDone = true;
	}
    
    
    
    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "onActivityCreated(): savedInstanceState = "
				+ savedInstanceState);
		if(savedInstanceState == null){
			toggleLoading(true);
			getLoaderManager().restartLoader(0, null, this);
		}else{
			toggleLoading(true);
			Feature origin  = (Feature)savedInstanceState.getSerializable(ARG_ITEM_FEATURE);
			Mission m = new Mission();
			m.setTemplate(MissionUtils.getDefaultTemplate(getSherlockActivity()));
			m.setOrigin(origin);
			if(getSherlockActivity() instanceof PendingMissionDetailActivity){
				Log.d(TAG, "Loader: Connecting to Activity database");
				m.db = ((PendingMissionDetailActivity)getSherlockActivity()).spatialiteDatabase;
			}else{
				Log.w(TAG, "Loader: Could not connect to Activity database");
			}
			mission =m;
			toggleLoading(false);
			buildForm();
		}
		
	}
    
    public Loader<Void> onCreateLoader(int id, Bundle args) {
		Log.d(TAG, "onCreateLoader(): id=" + id);
		Loader<Void> loader = new AsyncTaskLoader<Void>(getActivity()) {

			@Override
			public Void loadInBackground() {
				Activity activity = getSherlockActivity();
				Feature myFeature = (Feature) getArguments().getSerializable(ARG_ITEM_FEATURE);
				Mission m = new Mission();
				m.setTemplate(MissionUtils.getDefaultTemplate(activity));
				m.setOrigin(myFeature);
				
				if(activity instanceof PendingMissionDetailActivity){
					Log.d(TAG, "Loader: Connecting to Activity database");
					m.db = ((PendingMissionDetailActivity)activity).spatialiteDatabase;
				}else{
					Log.w(TAG, "Loader: Could not connect to Activity database");
				}
				
				mission =m;
				return null;
			}
		};
		//TODO create loader;
		loader.forceLoad();
		return loader;
	}
    @Override 
    public void onSaveInstanceState(Bundle outState) {
    	//save the origin
    	outState.putSerializable(ARG_ITEM_FEATURE, mission.getOrigin());
    	
    	
    }
    
   
	public void onLoadFinished(Loader<Void> id, Void result) {
		Log.d(TAG, "onLoadFinished(): id=" + id);
		toggleLoading(false);
		buildForm();
	}

	public void onLoaderReset(Loader<Void> loader) {
		Log.d(TAG, "onLoaderReset(): id=" + loader.getId());
	}

	private void toggleLoading(boolean show) {
		mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
		mFormView.setGravity(show ? Gravity.CENTER : Gravity.TOP);
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Log.d(TAG, "onDestroyView()");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy()");
	}

	@Override
	public void onDetach() {
		super.onDetach();
		Log.d(TAG, "onDetach()");
	}
	
}
