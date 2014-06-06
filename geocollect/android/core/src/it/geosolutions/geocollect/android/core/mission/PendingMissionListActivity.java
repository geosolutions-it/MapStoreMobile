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
import it.geosolutions.android.map.wfs.geojson.feature.Feature;
import it.geosolutions.geocollect.android.core.R;
import it.geosolutions.geocollect.android.core.mission.utils.NavUtils;
import it.geosolutions.geocollect.android.core.navigation.AbstractNavDrawerActivity;
import it.geosolutions.geocollect.android.core.navigation.NavDrawerActivityConfiguration;
import it.geosolutions.geocollect.android.core.navigation.NavDrawerAdapter;
import it.geosolutions.geocollect.android.core.navigation.NavDrawerItem;
import it.geosolutions.geocollect.android.core.preferences.GeoCollectPreferences;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.widget.FrameLayout;
import android.widget.ListView;

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
		PendingMissionListFragment.Callbacks {

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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
    		launch.putExtra(MapsActivity.PARAMETERS.LAT, 44.40565);
    		launch.putExtra(MapsActivity.PARAMETERS.LON, 8.946256);
    		launch.putExtra(MapsActivity.PARAMETERS.ZOOM_LEVEL, (byte)11);
    		startActivity(launch);
    		
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
    
    
	
}
