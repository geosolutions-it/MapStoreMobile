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

import java.io.File;

import it.geosolutions.android.map.view.MapViewManager;
import it.geosolutions.geocollect.android.core.R;
import it.geosolutions.geocollect.android.core.mission.utils.MissionUtils;
import it.geosolutions.geocollect.android.core.mission.utils.SpatialiteUtils;
import it.geosolutions.geocollect.model.config.MissionTemplate;

import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.MapView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;

import eu.geopaparazzi.library.util.ResourcesManager;

/**
 * An activity representing a single Pending Mission detail screen. This
 * activity is only used on handset devices. On tablet-size devices, item
 * details are presented side-by-side with a list of items in a
 * {@link PendingMissionListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than
 * a {@link PendingMissionDetailFragment}.
 */
public class PendingMissionDetailActivity extends SherlockFragmentActivity implements MapActivity {

	private MapViewManager mapViewManager;

	/**
	 * Spatialite Database for persistence
	 */
	jsqlite.Database spatialiteDatabase;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pendingmission_detail);
		if(mapViewManager != null){
			this.mapViewManager = new MapViewManager();
		}
		// Show the Up button in the action bar.
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		// Initialize database
		if(spatialiteDatabase == null){
	        try {
	            
	            File sdcardDir = ResourcesManager.getInstance(this).getSdcardDir();
	            File spatialDbFile = new File(sdcardDir, "geocollect/genova.sqlite");
	
	            if (!spatialDbFile.getParentFile().exists()) {
	                throw new RuntimeException();
	            }
	            spatialiteDatabase = new jsqlite.Database();
	            spatialiteDatabase.open(spatialDbFile.getAbsolutePath(), jsqlite.Constants.SQLITE_OPEN_READWRITE
	                    | jsqlite.Constants.SQLITE_OPEN_CREATE);
	            
	            Log.v("MISSION_DETAIL", SpatialiteUtils.queryVersions(spatialiteDatabase));
	            Log.v("MISSION_DETAIL", spatialiteDatabase.dbversion());
	            
	            MissionTemplate t = MissionUtils.getDefaultTemplate(this);
	            if(t != null && t.id != null){
		            if(SpatialiteUtils.checkOrCreateTable(spatialiteDatabase, t.id+"_data")){
			            Log.v("MISSION_DETAIL", "Table Found");
		            }else{
			            Log.w("MISSION_DETAIL", "Table could not be created, edits will not be saved");
		            }
	            }else{
	            	Log.w("MISSION_DETAIL", "MissionTemplate could not be found, edits will not be saved");
	            }
	            
	        } catch (Exception e) {
	            Log.v("MISSION_DETAIL", Log.getStackTraceString(e));
	        }
		}
		
		// savedInstanceState is non-null when there is fragment state
		// saved from previous configurations of this activity
		// (e.g. when rotating the screen from portrait to landscape).
		// In this case, the fragment will automatically be re-added
		// to its container so we don't need to manually add it.
		// For more information, see the Fragments API guide at:
		//
		// http://developer.android.com/guide/components/fragments.html
		//
		if (savedInstanceState == null) {
			// Create the detail fragment and add it to the activity
			// using a fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putString(
					PendingMissionDetailFragment.ARG_ITEM_ID,
					getIntent().getStringExtra(
							PendingMissionDetailFragment.ARG_ITEM_ID));
			arguments.putSerializable(PendingMissionDetailFragment.ARG_ITEM_FEATURE, getIntent().getSerializableExtra(PendingMissionDetailFragment.ARG_ITEM_FEATURE));
			PendingMissionDetailFragment fragment = new PendingMissionDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.pendingmission_detail_container, fragment)
					.commit();
		}
		

	}

	/* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragmentActivity#onOptionsItemSelected(com.actionbarsherlock.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpTo(this, new Intent(this,
					PendingMissionListActivity.class));
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
	
	
	/**
	 * @return a unique MapView ID on each call.
	 */
	public final int getMapViewId() {
		if(this.mapViewManager==null){
			//registration auto creates mapViewManager
			this.mapViewManager =new MapViewManager();
		}
		int i = this.mapViewManager.getMapViewId();
		Log.v("MAPVIEW","created mapview with id:"+i);
		return i;
	}

	/**
	 * This method is called once by each MapView during its setup process.
	 * 
	 * @param mapView
	 *            the calling MapView.
	 */
	public final void registerMapView(MapView mapView) {
		if(this.mapViewManager==null){
			//registration auto creates mapViewManager
			this.mapViewManager =new MapViewManager();
		}
		this.mapViewManager.registerMapView(mapView);
	}
	
	public Context getActivityContext(){
		return this;
	}
	
	 @Override
     protected void onResume() {
             super.onResume();
             if(this.mapViewManager!=null){
            	 this.mapViewManager.resumeMapViews();
             }
     }
	 
	 @Override
     protected void onDestroy() {
             super.onDestroy();
             if(this.mapViewManager!=null){
            	 this.mapViewManager.destroyMapViews();
             }
             if(this.spatialiteDatabase!=null){
            	 try {
					this.spatialiteDatabase.close();
 		            Log.v("MISSION_DETAIL", "Spatialite Database Closed");
				} catch (jsqlite.Exception e) {
		            Log.e("MISSION_DETAIL", Log.getStackTraceString(e));
				}
             }
     }
}
