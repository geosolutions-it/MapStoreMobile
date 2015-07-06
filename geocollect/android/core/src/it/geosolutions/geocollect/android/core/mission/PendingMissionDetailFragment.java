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

import it.geosolutions.android.map.MapsActivity;
import it.geosolutions.android.map.fragment.MapFragment;
import it.geosolutions.android.map.model.Layer;
import it.geosolutions.android.map.model.MSMMap;
import it.geosolutions.android.map.utils.SpatialDbUtils;
import it.geosolutions.android.map.wfs.geojson.feature.Feature;
import it.geosolutions.geocollect.android.core.BuildConfig;
import it.geosolutions.geocollect.android.core.GeoCollectApplication;
import it.geosolutions.geocollect.android.core.R;
import it.geosolutions.geocollect.android.core.form.FormEditActivity;
import it.geosolutions.geocollect.android.core.form.utils.FormBuilder;
import it.geosolutions.geocollect.android.core.mission.utils.MissionUtils;
import it.geosolutions.geocollect.android.core.mission.utils.PersistenceUtils;
import it.geosolutions.geocollect.model.config.MissionTemplate;
import it.geosolutions.geocollect.model.viewmodel.Field;
import it.geosolutions.geocollect.model.viewmodel.type.XType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import jsqlite.Exception;
import jsqlite.Stmt;

import org.mapsforge.core.model.GeoPoint;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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
import com.vividsolutions.jts.geom.Point;

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

	public static int DEFAULT_COLOR = Color.GRAY;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			// TODO: get the Feature from db
			Log.d(TAG, "onCreate() ARG_ITEM_ID: " + getArguments().getString(ARG_ITEM_ID));
			
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
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	      // Remove the button to open the full map
	            MenuItem full_map = menu.findItem(R.id.full_map);
	            if (full_map != null) {
	                menu.removeItem(R.id.full_map);
	            }
	   inflater.inflate(R.menu.nav_map_editable, menu);
	}

	/* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragment#onOptionsItemSelected(com.actionbarsherlock.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		int id = item.getItemId();
		
		if(id==R.id.accept){
			
			//check if this mission was already marked as uploadable and is going to be altered
			final String gcid = MissionUtils.getFeatureGCID( mission.getOrigin());
			
			HashMap<String,ArrayList<String>> uploadables = PersistenceUtils.loadUploadables(getActivity());
			
			final String table = mission.getTemplate().schema_sop.localFormStore;
			final ArrayList<String> ids = uploadables.get(table);
			
			//if an entry exists, remove it and save
			if(ids != null && ids.size() > 0 && ids.contains(gcid)){
				ids.remove(gcid);
				uploadables.put(table, ids);
				PersistenceUtils.saveUploadables(getActivity(), uploadables);
			}
			
			Intent i = new Intent(getSherlockActivity(),FormEditActivity.class);
			i.putExtra("MISSION", mission);
			startActivityForResult(i, EDIT_ACTIVITY_CODE);
			return true;		
		}else if(id == R.id.single_map){
			
			final GeoPoint geoPoint = getOriginGeoPoint();
			
			if(geoPoint != null){

				Intent mapIntent = new Intent(getSherlockActivity(),SimpleMapActivity.class);
				
				mapIntent.putExtra(SimpleMapActivity.ARG_PRIORITY_COLOR, getPriorityColor());
				
				mapIntent.putExtra(SimpleMapActivity.ARG_FIRST_POINT_LAT, geoPoint.latitude);
				mapIntent.putExtra(SimpleMapActivity.ARG_FIRST_POINT_LON, geoPoint.longitude);
				mapIntent.putExtra(SimpleMapActivity.ARG_ZOOM,((byte) 18));
				
				final GeoPoint updatedPoint = getUpdatedGeoPoint();
				
				if(updatedPoint != null){
					mapIntent.putExtra(SimpleMapActivity.ARG_SECOND_POINT_LAT, updatedPoint.latitude);
					mapIntent.putExtra(SimpleMapActivity.ARG_SECOND_POINT_LON, updatedPoint.longitude);
				}
				
		        MissionTemplate t = ((GeoCollectApplication) getActivity().getApplication()).getTemplate();

		        MSMMap m = SpatialDbUtils.mapFromDb();

		        for (Iterator<Layer> it = m.layers.iterator(); it.hasNext();) {
		            Layer layer = it.next();
		            if (!(layer.getTitle().equals(t.schema_seg.localSourceStore)
		                    || layer.getTitle().equals(t.schema_sop.localFormStore) || layer.getTitle()
		                    .equals(t.schema_seg.localSourceStore + MissionTemplate.NEW_NOTICE_SUFFIX))) {
		                Log.d(this.getClass().getSimpleName(), layer.getTitle()
		                        + " not corresponding to current schema " + t.schema_seg.localSourceStore);
		                it.remove();
		            }

		        }
		        mapIntent.putExtra(MapsActivity.MSM_MAP, m);
		        
				startActivity(mapIntent);

			}else{
				Log.e(TAG, "could not retrieve geopoint");
			}
			
		}else if(id == R.id.navigate){
			
			//start an intent to navigate to this position
			GeoPoint geoPoint = getUpdatedGeoPoint();
			
			if(geoPoint == null){
				geoPoint = getOriginGeoPoint();
			}
			
			if(geoPoint == null){
				Log.e(TAG, "no coordinate to navigate to available");
				return super.onOptionsItemSelected(item);
			}
			
			/**
			 * http://stackoverflow.com/questions/5801684/intent-to-start-a-navigation-activity
			 * 
			 * The bad news is, there isn't a standard Intent URI for navigation.
			 * 
			 * possibilities :
			 * 
			 * Uri.parse("google.navigation:q= lat,lon) //won't start navigation
			 * Uri.parse("geo:latitude,longitude //will only zoom map on position
			 * Uri.parse("http://maps.google.com/maps?daddr=lat,lon) worked best for now
			 * 
			 * problematic for users without google maps App but they should be few ?!
			 */
//			String uri_string = String.format("google.navigation:q%f",geoPoint.latitude,geoPoint.longitude);
			String uri_string = String.format("http://maps.google.com/maps?daddr=%s,%s",Double.toString(geoPoint.latitude),Double.toString(geoPoint.longitude));
			Log.d(TAG, "uri "+ uri_string);
			
			Intent navIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri_string));
			startActivity(navIntent);
		}
		
		
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Return the color code of the priority field of the mission
	 * Default is Color.GRAY
	 * @return
	 */
	public int getPriorityColor(){
		
		Field colorField = getField(XType.separatorWithIcon);
		
		if(colorField == null){
		    return DEFAULT_COLOR;
		}
		
		HashMap <String,String> colors  = mission.getTemplate().priorityValuesColors;

 		final String key = mission.getValueAsString(getActivity(), colorField);
 		
 		final String color = colors.get(key);	

 		if(color == null){
 			return DEFAULT_COLOR;
 		}
 		
 		try {
 			
 			return Color.parseColor(color);
 			
 		}catch(IllegalArgumentException iae){
 			if(BuildConfig.DEBUG){
 				Log.w(TAG, "Could not parse color: "+color);
 			}
			return DEFAULT_COLOR;
 		}

	}
	/**
	 * reads out the missions origin GeoPoint by retrieving the mapView field and using it
	 * to extract it out of the missions tags
	 * @return the geopoint of this mission
	 */
	public GeoPoint getOriginGeoPoint(){
				
		final Field mapField = getField(XType.mapViewPoint);
		if(mapField == null){
		    return null;
		}
		//extract the point
		GeoPoint geoPoint = null;
		List<String> tags = MissionUtils.getTags(mapField.value);
		if(tags != null && tags.size() ==1){
			Point geom = (Point) mission.getValueByTag(getActivity(), tags.get(0));

			if(geom !=null){
				if(!geom.isEmpty()){
					double lat = geom.getY();
					double lon = geom.getX();
					geoPoint = new GeoPoint(lat, lon);
				}

			}
		}
		return geoPoint;
	}
	/**
	 * accesses the database to acquire an updated position for this mission
	 * @return the updated GeoPoint or null if none available
	 */
	public GeoPoint getUpdatedGeoPoint(){
		
		final Field f = getField(XType.mapViewPoint);
		
		final String tableName = getTablename();
		
		// Default ID
		String originIDString = MissionUtils.getMissionGCID(mission);
		final String s = "SELECT Y(" + f.fieldId + "), X(" + f.fieldId + ") FROM '" + tableName + "' WHERE ORIGIN_ID = '" + originIDString +"';";
		
		if(mission == null || mission.db == null){
			Log.w(TAG, "Cannot retrieve mission database");
			return null;
		}
		
		try {
			if(jsqlite.Database.complete(s)){
				Stmt st = mission.db.prepare(s);
				if(st.step()){
					final GeoPoint p = new GeoPoint(st.column_double(0), st.column_double(1));
					st.close();
					return p;
				}
			}else{
				if(BuildConfig.DEBUG){
		    		Log.w(TAG, "Query is not complete:\n"+s);
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "Error retrieving updatedPoint",e);
		}
		
		return null;
		
	}
	/**
	 * returns a field from the default templates previews form
	 * @param xType to search for
	 * @return the field according to the xType
	 */
	public Field getField(final XType xType){
		
		final MissionTemplate t = MissionUtils.getDefaultTemplate(getSherlockActivity());

		for(Field f : t.preview.fields){
			if(f.xtype == xType){
				return f;
			}
		}
		return null;
	}
	/**
	 * gets the tablename of this mission's results 
	 * @return
	 */
	public String getTablename(){
		
		String tableName = mission.getTemplate().id+"_data";
    	if(mission.getTemplate().schema_sop != null 
    			&& mission.getTemplate().schema_sop.localFormStore != null
    			&& !mission.getTemplate().schema_sop.localFormStore.isEmpty()){
    		tableName = mission.getTemplate().schema_sop.localFormStore;
    	}
    	return tableName;
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

			PersistenceUtils.loadPageData(t.preview, mFormView, mission, getSherlockActivity(),true);

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

		PersistenceUtils.loadPageData(t.preview, mFormView, mission, getSherlockActivity(),false);
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
			}else if(getSherlockActivity() instanceof PendingMissionListActivity){
				Log.d(TAG, "Loader: Connecting to Activity database");
				m.db = ((PendingMissionListActivity)getSherlockActivity()).spatialiteDatabase;
			}else {
				Log.w(TAG, "Loader: Could not connect to Activity database");
			}
			mission =m;
			toggleLoading(false);
			buildForm();
		}
		
	}
    
    /**
     * Load the mission and template data either from the database or from the intent
     */
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
				}else if(activity instanceof PendingMissionListActivity){
					Log.d(TAG, "Loader: Connecting to Activity database");
					m.db = ((PendingMissionListActivity)activity).spatialiteDatabase;
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
        if(mission != null){
        	//save the origin
        	outState.putSerializable(ARG_ITEM_FEATURE, mission.getOrigin());
        }

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
