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
import it.geosolutions.android.map.database.SpatialDataSourceManager;
import it.geosolutions.android.map.model.MSMMap;
import it.geosolutions.android.map.utils.MapFilesProvider;
import it.geosolutions.android.map.utils.SpatialDbUtils;
import it.geosolutions.android.map.view.MapViewManager;
import it.geosolutions.android.map.wfs.geojson.feature.Feature;
import it.geosolutions.geocollect.android.core.R;
import it.geosolutions.geocollect.android.core.mission.utils.MissionUtils;
import it.geosolutions.geocollect.android.core.mission.utils.NavUtils;
import it.geosolutions.geocollect.android.core.mission.utils.PersistenceUtils;
import it.geosolutions.geocollect.android.core.mission.utils.SpatialiteUtils;
import it.geosolutions.geocollect.android.core.navigation.AbstractNavDrawerActivity;
import it.geosolutions.geocollect.android.core.navigation.NavDrawerActivityConfiguration;
import it.geosolutions.geocollect.android.core.navigation.NavDrawerAdapter;
import it.geosolutions.geocollect.android.core.navigation.NavDrawerItem;
import it.geosolutions.geocollect.android.core.preferences.GeoCollectPreferences;
import it.geosolutions.geocollect.android.map.ReturningMapInfoControl;
import it.geosolutions.geocollect.model.config.MissionTemplate;
import it.geosolutions.geocollect.model.source.XDataType;

import java.util.HashMap;

import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.MapView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.FrameLayout;

/**
 * An activity representing a list of Pending Missions. This activity has
 * different presentations for handset and tablet-size devices. On handsets, the
 * activity presents a list of items, which when touched, lead to a
 * {@link PendingMissionDetailActivity} representing item details. On tablets,
 * the activity presents the list of items and item details side-by-side using
 * two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link PendingMissionListFragment} and the item details (if present) is a
 * {@link PendingMissionDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link PendingMissionListFragment.Callbacks} interface to listen for item
 * selections.
 */
public class PendingMissionListActivity extends AbstractNavDrawerActivity implements
		PendingMissionListFragment.Callbacks , MapActivity{

	public static int SPATIAL_QUERY = 7001;
	
	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;
	
	/**
	 * Manage mapviews (two pane mode
	 */
	MapViewManager mapViewManager = new MapViewManager();

	/**
	 * Spatialite Database for persistence
	 */
	jsqlite.Database spatialiteDatabase;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v("MISSION_LIST", "onCreate()");
		// Initialize database
		// This should be the first thing the Activity does
		if(spatialiteDatabase == null){
	        
			spatialiteDatabase = SpatialiteUtils.openSpatialiteDB(this, "geocollect/genova.sqlite");
			
			if(spatialiteDatabase != null && !spatialiteDatabase.dbversion().equals("unknown")){
	            MissionTemplate t = MissionUtils.getDefaultTemplate(this);
	            if(t != null && t.id != null){
	            	
	            	// Save Source Missions on database
	            	HashMap<String, XDataType> sourceDataTypes = t.source.dataTypes;
	            	HashMap<String, XDataType> formDataTypes = PersistenceUtils.getTemplateFieldsList(t);
	            	// default value
	            	String formTableName = t.id+"_data";
	            	if(t.source != null ){
	            		if( t.source.localFormStore != null
	            			&& !t.source.localFormStore.isEmpty()){
		            		formTableName = t.source.localFormStore;
		            	}
		            	if( t.source.localSourceStore != null
		            		&& !t.source.localSourceStore.isEmpty()){
				            if(PersistenceUtils.createTableFromTemplate(spatialiteDatabase, t.source.localSourceStore, sourceDataTypes, true)){
			            		//SpatialiteUtils.checkOrCreateTable(spatialiteDatabase, t.id+"_data")){
				            	Log.v("MISSION_LIST", "Table Found, checking for schema updates");
					            if(PersistenceUtils.updateTableFromTemplate(spatialiteDatabase, t.source.localSourceStore, sourceDataTypes)){
					            	Log.v("MISSION_LIST", "All good");
					            }else{
					            	Log.w("MISSION_LIST", "Something went wrong during the update, the data can be inconsistent");
					            }
				            }else{
					            Log.w("MISSION_LIST", "Table could not be created, edits will not be saved");
				            }
	
		            	}
	            	}else{
		            	Log.w("MISSION_LIST", "MissionTemplate source could not be found!");
		            }
	            	
		            if(PersistenceUtils.createTableFromTemplate(spatialiteDatabase, formTableName, formDataTypes)){
			            Log.v("MISSION_LIST", "Table Found, checking for schema updates");
			            if(PersistenceUtils.updateTableFromTemplate(spatialiteDatabase, formTableName, formDataTypes)){
			            	Log.v("MISSION_LIST", "All good");
			            }else{
			            	Log.w("MISSION_LIST", "Something went wrong during the update, the data can be inconsistent");
			            }
			            
		            }else{
			            Log.w("MISSION_LIST", "Table could not be created, edits will not be saved");
		            }
	            }else{
	            	Log.w("MISSION_LIST", "MissionTemplate could not be found, edits will not be saved");
	            }
			}

		}
		
		
		//Set the layout
		getLayoutInflater().inflate(R.layout.activity_pendingmission_list, (FrameLayout)findViewById( R.id.content_frame));
		if (findViewById(R.id.pendingmission_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			((PendingMissionListFragment) getSupportFragmentManager()
					.findFragmentById(R.id.pendingmission_list))
					.setActivateOnItemClick(true);
		}

		// TODO: If exposing deep links into your app, handle intents here.
	}

	/**
	 * Callback method from {@link PendingMissionListFragment.Callbacks}
	 * indicating that the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(Object obj) {
		if (mTwoPane) {
			//DELETE PREVIOUS MAP VIEWS
			mapViewManager.destroyMapViews();
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putString(PendingMissionDetailFragment.ARG_ITEM_ID, ((Feature) obj).id);
			arguments.putSerializable(PendingMissionDetailFragment.ARG_ITEM_FEATURE, (Feature) obj);
			PendingMissionDetailFragment fragment = new PendingMissionDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.pendingmission_detail_container, fragment)
					.commit();

		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			Intent detailIntent = new Intent(this,
					PendingMissionDetailActivity.class);
			detailIntent.putExtra(PendingMissionDetailFragment.ARG_ITEM_ID, ((Feature) obj).id);
			detailIntent.putExtra(PendingMissionDetailFragment.ARG_ITEM_FEATURE, (Feature) obj);
			startActivity(detailIntent);
		}
	}

	/* (non-Javadoc)
	 * @see it.geosolutions.geocollect.android.core.navigation.AbstractNavDrawerActivity#getNavDrawerConfiguration()
	 */
	@Override
    protected NavDrawerActivityConfiguration getNavDrawerConfiguration() {
        
        NavDrawerItem[] menu = NavUtils.getNavMenu(this);
        //setup navigation configuration options
        NavDrawerActivityConfiguration navDrawerActivityConfiguration = new NavDrawerActivityConfiguration();
        navDrawerActivityConfiguration.setMainLayout(R.layout.geocollect_main);
        navDrawerActivityConfiguration.setDrawerLayoutId(R.id.drawer_layout);
        navDrawerActivityConfiguration.setLeftDrawerId(R.id.left_drawer);
        navDrawerActivityConfiguration.setNavItems(menu);
        navDrawerActivityConfiguration.setBaseAdapter(
            new NavDrawerAdapter(this, R.layout.navdrawer_item, menu ));
        return navDrawerActivityConfiguration;
    }
    
    @Override
    protected void onNavItemSelected(int id) {
        switch ((int)id) {
        //first option
        case 101:
            //only one fragment for now, the other options are indipendent activities
        	//so we don't have to do anything here
        	//getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new FriendMainFragment()).commit();
            break;
        //Map
        case 102:
        	//setup map options
        	//TODO parametrize it 
        	Intent launch = new Intent(this,MapsActivity.class);
    		launch.setAction(Intent.ACTION_VIEW);
    		launch.putExtra(MapsActivity.PARAMETERS.CONFIRM_ON_EXIT, false);
        	
    		//ArrayList<Layer> layers =  (ArrayList<Layer>) LocalPersistence.readObjectFromFile(this, LocalPersistence.CURRENT_MAP);
        	//if(layers == null || layers.isEmpty()){
    			MapFilesProvider.setBaseDir("/geocollect");
    			
				MSMMap m = SpatialDbUtils.mapFromDb();
	    		if(m.layers == null || m.layers.isEmpty()){
	    			// retry, SpatialDataSourceManager is buggy
	    			SpatialDataSourceManager dbManager = SpatialDataSourceManager.getInstance();

	    			try {
	    				//Only if not already loaded some tables
	    				if (dbManager.getSpatialVectorTables(false).size() <= 0) {
	    					dbManager.init(this, MapFilesProvider.getBaseDirectoryFile());
	    				} 
	    			} catch (Exception e) {
	    				// ignore
	    			}
	    			m = SpatialDbUtils.mapFromDb();
	    		}
	    		launch.putExtra(MapsActivity.PARAMETERS.LAT, 44.40565);
	    		launch.putExtra(MapsActivity.PARAMETERS.LON, 8.946256);
	    		launch.putExtra(MapsActivity.PARAMETERS.ZOOM_LEVEL, (byte)11);
	    		launch.putExtra(MapsActivity.MSM_MAP, m);
			//}
        	
        	launch.putExtra(MapsActivity.PARAMETERS.CUSTOM_MAPINFO_CONTROL, new ReturningMapInfoControl());
        	
    		//launch.putExtra(MapsActivity.LAYERS_TO_ADD, m.layers) ;
    		startActivityForResult(launch, SPATIAL_QUERY);
    		
            break;
        //Settings	
        case 203:
        	Intent intent = new Intent(this,
        			GeoCollectPreferences.class);
		      startActivity(intent);
        	break;
        //quit
        case 204:
        	confirmExit();
        	break;
        }
    }
	
    /**
	 * Ask to confirm when exit
	 */
    @Override
    public void onBackPressed() {
    	confirmExit();
    }
    
    public void confirmExit(){
    	new AlertDialog.Builder(this)
	    .setTitle(R.string.button_confirm_exit_title)
	    .setMessage(R.string.button_confirm_exit)
	    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	        	 finish();
	        	
	        }
	     })
	    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            // do nothing
	        }
	     })
	     .show();
    }
    
    // **********************************************
    // *********MAP VIEWS MANAGEMENT      ***********
    // **********************************************
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
    protected void onDestroy() {
        super.onDestroy();
        // TODO: check utility of this block (it wasn't present)
        if(this.mapViewManager!=null){
       	 this.mapViewManager.destroyMapViews();
        }
        if(this.spatialiteDatabase!=null){
		    try {
		    	this.spatialiteDatabase.close();
		    	Log.v("MISSION_LIST", "Spatialite Database Closed");
			} catch (jsqlite.Exception e) {
				Log.e("MISSION_LIST", Log.getStackTraceString(e));
			}
        }
    }
	
	/**
	 * Since the triggering intent is launched by the activity and not bay a Fragment
	 * the child Fragments will not receive the result
	 * We override the default onActivityResult() to propagate the result to the 
	 * child Fragments
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// We need to explicitly call the child Fragments onActivityResult()
		for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
	}
}
