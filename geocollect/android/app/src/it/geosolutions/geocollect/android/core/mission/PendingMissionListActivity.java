/*******************************************************************************
 * Copyright 2014-2015 GeoSolutions
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
 * 
 *******************************************************************************/
package it.geosolutions.geocollect.android.core.mission;

import it.geosolutions.android.map.MapsActivity;
import it.geosolutions.android.map.MapsActivity.DrawerMode;
import it.geosolutions.android.map.model.Layer;
import it.geosolutions.android.map.model.MSMMap;
import it.geosolutions.android.map.utils.SpatialDbUtils;
import it.geosolutions.android.map.view.MapViewManager;
import it.geosolutions.geocollect.android.app.BuildConfig;
import it.geosolutions.geocollect.android.core.GeoCollectApplication;
import it.geosolutions.geocollect.android.app.R;
import it.geosolutions.geocollect.android.core.form.FormEditActivity;
import it.geosolutions.geocollect.android.core.login.LoginActivity;
import it.geosolutions.geocollect.android.core.login.LogoutActivity;
import it.geosolutions.geocollect.android.core.login.utils.LoginRequestInterceptor;
import it.geosolutions.geocollect.android.core.login.utils.NetworkUtil;
import it.geosolutions.geocollect.android.core.mission.utils.MissionUtils;
import it.geosolutions.geocollect.android.core.mission.utils.NavUtils;
import it.geosolutions.geocollect.android.core.mission.utils.PersistenceUtils;
import it.geosolutions.geocollect.android.core.mission.utils.SQLiteCascadeFeatureLoader;
import it.geosolutions.geocollect.android.core.mission.utils.SpatialiteUtils;
import it.geosolutions.geocollect.android.core.navigation.AbstractNavDrawerActivity;
import it.geosolutions.geocollect.android.core.navigation.NavDrawerActivityConfiguration;
import it.geosolutions.geocollect.android.core.navigation.NavDrawerAdapter;
import it.geosolutions.geocollect.android.core.navigation.NavDrawerItem;
import it.geosolutions.geocollect.android.map.GeoCollectMapActivity;
import it.geosolutions.geocollect.android.template.TemplateDownloadTask;
import it.geosolutions.geocollect.model.config.MissionTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.MapView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.newrelic.agent.android.NewRelic;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * An activity representing a list of Pending Missions. This activity has different presentations for handset and tablet-size devices. On handsets,
 * the activity presents a list of items, which when touched, lead to a {@link PendingMissionDetailActivity} representing item details. On tablets,
 * the activity presents the list of items and item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a {@link PendingMissionListFragment} and the item details (if present) is a
 * {@link PendingMissionDetailFragment}.
 * <p>
 * This activity also implements the required {@link PendingMissionListFragment.Callbacks} interface to listen for item selections.
 */
public class PendingMissionListActivity extends AbstractNavDrawerActivity implements
        PendingMissionListFragment.Callbacks, MapActivity, LocationListener {

    /**
     * TAG for logging
     */
    public static String TAG = "PendingMissionListActivity";
    
    public static int SPATIAL_QUERY = 7001;

    public static final String ARG_CREATE_MISSIONFEATURE = "CREATE_MISSIONFEATURE";
    public static final String ARG_CREATING_TEMPLATE = "CREATING_MISSIONTEMPLATE";
    public static final String PREFS_USES_DOWNLOADED_TEMPLATE = "USES_DOWNLOADED_TEMPLATE";
    public static final String PREFS_DOWNLOADED_TEMPLATE_INDEX = "DOWNLOADED_TEMPLATE_INDEX";
    public static final String PREFS_SELECTED_TEMPLATE_ID = "SELECTED_TEMPLATE_ID";
    
    /**
     * Contract key for map configuration
     */
    public static String KEY_MAPSTARTLAT = "mapStartLat";
    public static String KEY_MAPSTARTLON = "mapStartLon";
    public static String KEY_MAPSTARTZOOM = "mapStartZoom";

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet device.
     */
    private boolean mTwoPane;

    /**
     * Manage mapviews (two pane mode
     */
    MapViewManager mapViewManager = new MapViewManager();

    /**
     * Spatialite Database for persistence
     */
    public jsqlite.Database spatialiteDatabase;

    private IntentFilter mIntentFilter;

    private BroadcastReceiver mReceiver;

    private static long LOCATION_MINTIME = 7 * 1000; // Minimum time interval for update in seconds, i.e. 5 seconds.
    private static long LOCATION_MINDISTANCE = 10; // Minimum distance change for update in meters, i.e. 10 meters.
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(BuildConfig.DEBUG){
            Log.v(TAG, "onCreate()");
        }
        
        // Load the application properties file
        Properties properties = new Properties();
        try {
            // access to the folder ‘assets’
            AssetManager am = getAssets();
            // opening the file
            InputStream inputStream = am.open("geocollect.properties");
            // loading of the properties
            properties.load(inputStream);
            
        } catch (IOException e) {
            Log.e(GeoCollectApplication.class.getSimpleName(), e.toString());
        }

        // Get the NewRelic token, if found start the monitoring
        String newRelicToken = properties.getProperty("newRelicToken"); 
        if(newRelicToken != null){
            NewRelic.withApplicationToken(newRelicToken).start(this);
        }
        
        // Login data is always necessary to load mission data
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        final String authKey = prefs.getString(LoginActivity.PREFS_AUTH_KEY, null);

        if (authKey == null) {

            startActivityForResult(new Intent(this, LoginActivity.class),
                    LoginActivity.REQUEST_LOGIN);

        } else if (NetworkUtil.isOnline(getBaseContext())) {

            // TODO --> set when to check the remote templates, for now always onCreate when authkey available && online
            Log.d(TAG, "fetching remote Templates");

            final TemplateDownloadTask task = new TemplateDownloadTask() {
                @Override
                public void complete(final ArrayList<MissionTemplate> downloadedTemplates) {

                    /**
                     * download successful, elaborate result
                     **/
                    // 1. update database
                    ArrayList<MissionTemplate> validTemplates = new ArrayList<MissionTemplate>();
                    if (downloadedTemplates != null && downloadedTemplates.size() > 0) {
                        if (spatialiteDatabase == null) {

                            spatialiteDatabase = SpatialiteUtils.openSpatialiteDB(
                                    PendingMissionListActivity.this, "geocollect/genova.sqlite");
                        }
                        for (MissionTemplate t : downloadedTemplates) {
                            if (!PersistenceUtils.createOrUpdateTablesForTemplate(t,
                                    spatialiteDatabase)) {
                                Log.w(TAG, "error creating/updating table");
                            } else {
                                // if insert succesfull add to list of valid templates
                                validTemplates.add(t);
                            }
                        }
                    }
                    Log.d(TAG, "database updated");

                    // 2. save valid templates
                    PersistenceUtils.saveDownloadedTemplates(getBaseContext(), validTemplates);

                    if(BuildConfig.DEBUG){
                        Log.d(TAG, "valid templates persisted");
                    }

                    // 3. update navdrawer menu
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            navConf = getNavDrawerConfiguration();

                            NavDrawerItem[] menu = NavUtils
                                    .getNavMenu(PendingMissionListActivity.this);

                            // cannot modify items of navdraweradapter --> create a new one
                            NavDrawerAdapter newNavDrawerAdapter = new NavDrawerAdapter(
                                    PendingMissionListActivity.this, R.layout.navdrawer_item, menu);

                            mDrawerList.setAdapter(newNavDrawerAdapter);
                            
                            Log.d(TAG, "navdrawer updated");

                        }
                    });
                }
            };
            
            String username = prefs.getString(LoginActivity.PREFS_USER_EMAIL, null);
            String password = prefs.getString(LoginActivity.PREFS_PASSWORD, null);

            String authorizationString = LoginRequestInterceptor.getB64Auth(username, password);
            
            task.execute(authKey, authorizationString);

        }

        // Default template
        // Initialize database
        // This should be the first thing the Activity does
        if (spatialiteDatabase == null) {

            spatialiteDatabase = SpatialiteUtils.openSpatialiteDB(this, "geocollect/genova.sqlite");

            if (spatialiteDatabase != null && !spatialiteDatabase.dbversion().equals("unknown")) {

                MissionTemplate t = MissionUtils.getDefaultTemplate(this);

                if (!PersistenceUtils.createOrUpdateTablesForTemplate(t, spatialiteDatabase)) {
                    Log.e(TAG, "error creating/updating tables for " + t.nameField);
                }
                
                HashMap<String, ArrayList<String>> uploadables = PersistenceUtils.loadUploadables(this);
                if (uploadables!= null && uploadables.size() > 0) {
                    PersistenceUtils.sanitizePendingFeaturesList(uploadables, spatialiteDatabase);
                    PersistenceUtils.saveUploadables(this, uploadables);
                }

            }

        }

        // Set the layout
        getLayoutInflater().inflate(R.layout.activity_pendingmission_list,
                (FrameLayout) findViewById(R.id.content_frame));
        if (findViewById(R.id.pendingmission_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((PendingMissionListFragment) getSupportFragmentManager().findFragmentById(
                    R.id.pendingmission_list)).setActivateOnItemClick(true);
        }

        // TODO: If exposing deep links into your app, handle intents here.
        if (getIntent().getExtras() != null) {
            boolean createMission = getIntent().getExtras().getBoolean(ARG_CREATE_MISSIONFEATURE);
            if (createMission) {
                // MissionTemplate t = MissionUtils.getDefaultTemplate(getBaseContext());
                // ((PendingMissionListFragment) getSupportFragmentManager().findFragmentById(R.id.pendingmission_list)).setTemplate(t);
                ((PendingMissionListFragment) getSupportFragmentManager().findFragmentById(
                        R.id.pendingmission_list)).switchAdapter();

            }
        }

    }

    /**
     * Callback method from {@link PendingMissionListFragment.Callbacks} indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(Object obj) {

        if(obj == null || !(obj instanceof MissionFeature)){
            if(BuildConfig.DEBUG){
                Log.w(TAG, "Tried to select an invalid object");
            }
            return;
        }
        
        MissionFeature selectedFeature = (MissionFeature) obj;
        
        if (selectedFeature.typeName == null
        || !selectedFeature.typeName.endsWith(MissionTemplate.NEW_NOTICE_SUFFIX)) {
            
            if (mTwoPane) {
                // DELETE PREVIOUS MAP VIEWS
                mapViewManager.destroyMapViews();
                // In two-pane mode, show the detail view in this activity by
                // adding or replacing the detail fragment using a
                // fragment transaction.

                Bundle arguments = new Bundle();
                arguments.putString(PendingMissionDetailFragment.ARG_ITEM_ID, selectedFeature.id);
                arguments.putSerializable(PendingMissionDetailFragment.ARG_ITEM_FEATURE,selectedFeature);
                PendingMissionDetailFragment fragment = new PendingMissionDetailFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.pendingmission_detail_container, fragment).commit();

            } else {
                // In single-pane mode, simply start the detail activity
                // for the selected item ID.
                Intent detailIntent = new Intent(this, PendingMissionDetailActivity.class);
                detailIntent.putExtra(PendingMissionDetailFragment.ARG_ITEM_ID, selectedFeature.id);
                detailIntent.putExtra(PendingMissionDetailFragment.ARG_ITEM_FEATURE, selectedFeature);
                startActivity(detailIntent);
            }
        } else {

            Log.d(TAG, "Clicked new feature with ID: " + selectedFeature.id);

            Intent editIntent = new Intent(this, FormEditActivity.class);
            editIntent.putExtra(ARG_CREATE_MISSIONFEATURE, true);
            editIntent.putExtra(PendingMissionDetailFragment.ARG_ITEM_FEATURE, selectedFeature);
            startActivity(editIntent);

        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see it.geosolutions.geocollect.android.core.navigation.AbstractNavDrawerActivity#getNavDrawerConfiguration()
     */
    @Override
    protected NavDrawerActivityConfiguration getNavDrawerConfiguration() {

        NavDrawerItem[] menu = NavUtils.getNavMenu(this);
        // setup navigation configuration options
        NavDrawerActivityConfiguration navDrawerActivityConfiguration = new NavDrawerActivityConfiguration();
        navDrawerActivityConfiguration.setMainLayout(R.layout.geocollect_main);
        navDrawerActivityConfiguration.setDrawerLayoutId(R.id.drawer_layout);
        navDrawerActivityConfiguration.setLeftDrawerId(R.id.left_drawer);
        navDrawerActivityConfiguration.setNavItems(menu);
        navDrawerActivityConfiguration.setBaseAdapter(new NavDrawerAdapter(this,
                R.layout.navdrawer_item, menu));
        return navDrawerActivityConfiguration;
    }

    @Override
    protected void onNavItemSelected(int id) {
        switch ((int) id) {
        // first option
        case 101:
        case 1001:
            // only one fragment for now, the other options are independent activities

            clearDetailFragment();


            Editor ed = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();
            ed.putBoolean(PREFS_USES_DOWNLOADED_TEMPLATE, false);
            ed.commit();

            MissionTemplate mt = MissionUtils.getDefaultTemplate(getBaseContext());

            ((PendingMissionListFragment) getSupportFragmentManager().findFragmentById(
                    R.id.pendingmission_list)).setTemplate(mt);
            
            ((GeoCollectApplication)getApplication()).setTemplate(mt);
            
            if (id == 101) {
                ((PendingMissionListFragment) getSupportFragmentManager().findFragmentById(
                        R.id.pendingmission_list)).restartLoader(mt.getLoaderIndex());
            }
            
            ((PendingMissionListFragment) getSupportFragmentManager().findFragmentById(
                    R.id.pendingmission_list)).switchAdapter();
                    
            break;
        // The Map button is disabled
        /*
         * case 102: // Start the Map activity launchFullMap(); break;
         */
        // Settings
        case 203:
            final SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(getBaseContext());

            final String user_email = prefs.getString(LoginActivity.PREFS_USER_EMAIL, null);
            final String user_pw = prefs.getString(LoginActivity.PREFS_PASSWORD, null);

            if (user_email != null && user_pw != null) {
                // user is most likely logged in, show LogoutActivity
                startActivityForResult(new Intent(this, LogoutActivity.class),
                        LogoutActivity.REQUEST_LOGOUT);
            } else {

                startActivity(new Intent(this, LoginActivity.class));
            }

            break;
        // quit
        case 204:
            confirmExit();
            break;
        
        // logout
        case 205:
            confirmLogout();
            break;
        }

        // downloaded templates will have a dynamic id currently starting from 2000
        if (id >= 2000) {

            final ArrayList<MissionTemplate> downloadedTemplates = PersistenceUtils
                    .loadSavedTemplates(getBaseContext());

            if(downloadedTemplates == null){
                return;
            }
            
            final int index = id % 2000;

            final int templateIndex = index ;

            final MissionTemplate t = downloadedTemplates.get(templateIndex);

            Log.d(TAG, "downloaded template "+ templateIndex + " selected : " + t.id);

            clearDetailFragment();

            Editor ed = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();
            ed.putBoolean(PREFS_USES_DOWNLOADED_TEMPLATE, true);
            ed.putInt(PREFS_DOWNLOADED_TEMPLATE_INDEX, templateIndex);
            ed.putString(PREFS_SELECTED_TEMPLATE_ID, t.id);
            ed.commit();

            ((PendingMissionListFragment) getSupportFragmentManager().findFragmentById(
                    R.id.pendingmission_list)).setTemplate(t);
            
            ((GeoCollectApplication)getApplication()).setTemplate(t);
            
            ((PendingMissionListFragment) getSupportFragmentManager().findFragmentById(
                        R.id.pendingmission_list)).restartLoader(t.getLoaderIndex());
            
            ((PendingMissionListFragment) getSupportFragmentManager().findFragmentById(
                    R.id.pendingmission_list)).switchAdapter();
            
            MissionUtils.checkMapStyles(getResources(), t);

        }
    }

    /**
     * removes the detailfragment in the twoPane layout, if necessary
     */
    public void clearDetailFragment() {

        if (mTwoPane) {
            final PendingMissionDetailFragment fragment = (PendingMissionDetailFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.pendingmission_detail_container);
            if (fragment != null) {
                getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            }
        }
    }

    /**
     * Ask to confirm when exit
     */
    @Override
    public void onBackPressed() {
        if (mTwoPane) {
            final PendingMissionDetailFragment fragment = (PendingMissionDetailFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.pendingmission_detail_container);
            if (fragment != null) {
                getSupportFragmentManager().beginTransaction().remove(fragment).commit();
//            } else {
//                confirmExit();
            }
//        } else {
//            confirmExit();
        }
    }

    public void confirmExit() {
        new AlertDialog.Builder(this).setTitle(R.string.button_confirm_exit_title)
                .setMessage(R.string.button_confirm_exit)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();

                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                }).show();
    }

    /**
     * Display a confirm prompt before logging out the user
     */
    public void confirmLogout() {
        new AlertDialog.Builder(this).setTitle(R.string.action_logout)
                .setMessage(R.string.button_confirm_logout)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        //clear user data
                        final Editor ed = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();

                        ed.putString(LoginActivity.PREFS_USER_EMAIL, null);
                        ed.putString(LoginActivity.PREFS_USER_FORENAME, null);
                        ed.putString(LoginActivity.PREFS_USER_SURNAME, null);
                        ed.putString(LoginActivity.PREFS_PASSWORD, null);
                        ed.putString(LoginActivity.PREFS_AUTH_KEY, null);
                        ed.putString(LoginActivity.PREFS_USER_ENTE, null);

                        ed.commit();

                        Toast.makeText(getBaseContext(), getString(R.string.logout_logged_out),Toast.LENGTH_LONG).show();
                        
                        startActivityForResult(
                                new Intent(getBaseContext(), LoginActivity.class),
                                LoginActivity.REQUEST_LOGIN);

                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                }).show();
    }

    // **********************************************
    // *********MAP VIEWS MANAGEMENT ***********
    // **********************************************
    /**
     * @return a unique MapView ID on each call.
     */
    public final int getMapViewId() {
        if (this.mapViewManager == null) {
            // registration auto creates mapViewManager
            this.mapViewManager = new MapViewManager();
        }
        int i = this.mapViewManager.getMapViewId();
        Log.v(TAG, "created mapview with id:" + i);
        return i;
    }

    /**
     * This method is called once by each MapView during its setup process.
     * 
     * @param mapView the calling MapView.
     */
    public final void registerMapView(MapView mapView) {
        if (this.mapViewManager == null) {
            // registration auto creates mapViewManager
            this.mapViewManager = new MapViewManager();
        }
        this.mapViewManager.registerMapView(mapView);
    }

    public Context getActivityContext() {
        return this;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // In the tablet layout the map can be visible within this activity
        if (this.mapViewManager != null) {
            this.mapViewManager.destroyMapViews();
        }
        if (this.spatialiteDatabase != null) {
            try {
                this.spatialiteDatabase.close();
                Log.v(TAG, "Spatialite Database Closed");
            } catch (jsqlite.Exception e) {
                Log.e(TAG, Log.getStackTraceString(e));
            }
        }
    }

    /**
     * Since the triggering intent is launched by the activity and not bay a Fragment the child Fragments will not receive the result We override the
     * default onActivityResult() to propagate the result to the child Fragments
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LoginActivity.REQUEST_LOGIN) {

            if (resultCode == RESULT_CANCELED) {
                // user cancelled to enter credentials
                Toast.makeText(getBaseContext(), getString(R.string.login_canceled),
                        Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }
        if (requestCode == LogoutActivity.REQUEST_LOGOUT) {
            if (resultCode == LogoutActivity.LOGGED_OUT) {
                // there is a notification in LogoutActivity already
                startActivityForResult(
                        new Intent(this, LoginActivity.class),
                        LoginActivity.REQUEST_LOGIN);
                return;
            }
        }
        
        navConf = getNavDrawerConfiguration();

        NavDrawerItem[] menu = NavUtils.getNavMenu(PendingMissionListActivity.this);

        // cannot modify items of navdraweradapter --> create a new one
        NavDrawerAdapter newNavDrawerAdapter = new NavDrawerAdapter(
                PendingMissionListActivity.this, R.layout.navdrawer_item, menu);

        mDrawerList.setAdapter(newNavDrawerAdapter);

        Log.d(TAG, "navdrawer updated");
        newNavDrawerAdapter.notifyDataSetChanged();
        
        // We need to explicitly call the child Fragments onActivityResult()
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            try {
                fragment.onActivityResult(requestCode, resultCode, data);
            }catch (NullPointerException npe){
                // TODO This is ABS bug, need to switch to ActionBarCompat
                Log.e(TAG, npe.getLocalizedMessage(), npe);
            }
        }
    }

    /**
     * Launches the Map activity
     */
    public void launchFullMap() {
        // setup map options
        // TODO parametrize it
        Intent launch = new Intent(this, GeoCollectMapActivity.class);
        launch.setAction(Intent.ACTION_VIEW);
        launch.putExtra(MapsActivity.PARAMETERS.CONFIRM_ON_EXIT, false);

        // ArrayList<Layer> layers = (ArrayList<Layer>) LocalPersistence.readObjectFromFile(this, LocalPersistence.CURRENT_MAP);
        // if(layers == null || layers.isEmpty()){

        MissionTemplate t = ((PendingMissionListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.pendingmission_list)).getCurrentMissionTemplate();

        MSMMap m = SpatialDbUtils.mapFromDb();
        ArrayList<String> bg_layers = null;
        
        try{
            if(t.config != null && t.config.get(MissionTemplate.BG_LAYERS_KEY) != null){
                if(t.config.get(MissionTemplate.BG_LAYERS_KEY) instanceof ArrayList<?>){
                    bg_layers = (ArrayList<String>) t.config.get(MissionTemplate.BG_LAYERS_KEY);
                }
            }
        }catch (ClassCastException cce){
            if(BuildConfig.DEBUG){
                Log.w(TAG, "backgroundLayers tag is not an ArrayList, ignoring");
            }
            bg_layers = null;
        }
        
        // Use only the layers that are related to this Mission
        ArrayList<Layer> layersList = new ArrayList<Layer>();
        Layer layer = null;
        
        for (Iterator<Layer> it = m.layers.iterator(); it.hasNext();) {
            layer = it.next();
            if (layer.getTitle().equals(t.schema_seg.localSourceStore)
               || layer.getTitle().equals(t.schema_sop.localFormStore) 
               || layer.getTitle().equals(t.schema_seg.localSourceStore + MissionTemplate.NEW_NOTICE_SUFFIX)
            ) {

                layersList.add(layer);
                
            }else if (bg_layers != null && bg_layers.contains(layer.getTitle())){
                
                // Adding in the head position, so the layer will
                // be on the last in the LayerSwitcher order 
                layersList.add(0, layer);
            }
        }
        
        // Set the correct layers
        m.layers = layersList;
        

        // if(m.layers == null || m.layers.isEmpty()){
        // // retry, SpatialDataSourceManager is buggy
        // SpatialDataSourceManager dbManager = SpatialDataSourceManager.getInstance();
        //
        // try {
        // //Only if not already loaded some tables
        // if (dbManager.getSpatialVectorTables(false).size() <= 0) {
        // dbManager.init(this, MapFilesProvider.getBaseDirectoryFile());
        // }
        // } catch (Exception e) {
        // // ignore
        // }
        // m = SpatialDbUtils.mapFromDb();
        // }
        
        // Set map configurations, if available
        if(t.config != null){
            if(t.config.get(KEY_MAPSTARTLAT) != null){
                try {
                    double mapStartLat = Double.parseDouble((String) t.config.get(KEY_MAPSTARTLAT));
                    launch.putExtra(MapsActivity.PARAMETERS.LAT, mapStartLat);
                } catch (NumberFormatException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                } 
            }
            
            if(t.config.get(KEY_MAPSTARTLON) != null){
                try {
                    double mapStartLon = Double.parseDouble((String) t.config.get(KEY_MAPSTARTLON));
                    launch.putExtra(MapsActivity.PARAMETERS.LON, mapStartLon);
                } catch (NumberFormatException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                } 
            }
            
            if(t.config.get(KEY_MAPSTARTZOOM) != null){
                try {
                    int mapStartZoom = Integer.parseInt((String) t.config.get(KEY_MAPSTARTZOOM));
                    launch.putExtra(MapsActivity.PARAMETERS.ZOOM_LEVEL, (byte) mapStartZoom);
                } catch (NumberFormatException e) {
                    Log.e(TAG, Log.getStackTraceString(e));
                } 
            }

        }
        
        launch.putExtra(MapsActivity.PARAMETERS.ZOOM_LEVEL_MIN, (byte) 11);
        launch.putExtra(MapsActivity.PARAMETERS.ZOOM_LEVEL_MAX, (byte) 19);
        launch.putExtra(MapsActivity.MSM_MAP, m);
        // select here a drawer mode
        launch.putExtra(MapsActivity.PARAMETERS.DRAWER_MODE, DrawerMode.ONLY_LEFT.ordinal());
        // }

        /*
        launch.putExtra(MapsActivity.PARAMETERS.CUSTOM_MAPINFO_CONTROL,
                new ReturningMapInfoControl());
                */

        // launch.putExtra(MapsActivity.LAYERS_TO_ADD, m.layers) ;
        startActivityForResult(launch, SPATIAL_QUERY);
    }
    
    public void checkGPSandStartCreation(View v){
        checkGPSandStartCreation(this);
    }
    
    public static void checkGPSandStartCreation(final SherlockFragmentActivity abs_activity) {
        if (!isGPSAvailable(abs_activity)) {

            new AlertDialog.Builder(abs_activity).setTitle(R.string.app_name).setMessage(R.string.gps_not_enabled)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            abs_activity.startActivityForResult(new Intent(
                                    android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), PendingMissionListFragment.ARG_ENABLE_GPS);
                        }
                    }).show();
        } else {

            startMissionFeatureCreation(abs_activity);
        }
    }

    /**
     * checks if location services are available
     */
    public static boolean isGPSAvailable(Context context) {
        if(context==null){
            return false;
        }
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

    }

    /**
     * starts the {@link MissionFeature} creation
     */
    public static void startMissionFeatureCreation(SherlockFragmentActivity abs_activity) {

        Intent i = new Intent(abs_activity, FormEditActivity.class);
        i.putExtra(PendingMissionListActivity.ARG_CREATE_MISSIONFEATURE, true);
        abs_activity.startActivityForResult(i, FormEditActivity.FORM_CREATE_NEW_MISSIONFEATURE);

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.v(TAG, "Location: \n lat  "+location.getLatitude()+"\n lon  "+location.getLongitude());
        
        SharedPreferences sp = getSharedPreferences(SQLiteCascadeFeatureLoader.PREF_NAME,Context.MODE_PRIVATE);
        // If it is the first time we get a Location, and the list is ordered by distance, refresh the list automatically 
        boolean needRefresh = sp.getBoolean(SQLiteCascadeFeatureLoader.ORDER_BY_DISTANCE, false)
                && ( !sp.contains(SQLiteCascadeFeatureLoader.LOCATION_X)
                   || sp.getLong(SQLiteCascadeFeatureLoader.LOCATION_X, 0) == 0);
        
        SharedPreferences.Editor editor = sp.edit();
        // Set position
        editor.putLong(SQLiteCascadeFeatureLoader.LOCATION_X, Double.doubleToRawLongBits(location.getLongitude()));
        editor.putLong(SQLiteCascadeFeatureLoader.LOCATION_Y, Double.doubleToRawLongBits(location.getLatitude()));
        editor.apply();
        
        if(needRefresh){
            PendingMissionListFragment listFragment = ((PendingMissionListFragment) getSupportFragmentManager().findFragmentById(
                    R.id.pendingmission_list));
            if(listFragment != null){
                listFragment.onRefresh();
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //Log.d("PMLA", "Status Changed - Provider: " + provider +" Status: "+status);
        
    }

    @Override
    public void onProviderEnabled(String provider) {
        //Log.d("PMLA", "Provider Enabled: " + provider );
        
    }

    @Override
    public void onProviderDisabled(String provider) {
        //Log.d("PMLA", "Provider Enabled: " + provider );
        
    }
    
    /**
     * Get provider name.
     * @return Name of best suiting provider.
     * */
    String getProviderName() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
     
        Criteria criteria = new Criteria();
        criteria.setPowerRequirement(Criteria.POWER_LOW); // Chose your desired power consumption level.
        criteria.setAccuracy(Criteria.ACCURACY_FINE); // Choose your accuracy requirement.
        criteria.setSpeedRequired(false); // Chose if speed for first location fix is required.
        criteria.setAltitudeRequired(false); // Choose if you use altitude.
        criteria.setBearingRequired(false); // Choose if you use bearing.
        criteria.setCostAllowed(false); // Choose if this provider can waste money :-)
     
        // Provide your criteria and flag enabledOnly that tells
        // LocationManager only to return active providers.
        return locationManager.getBestProvider(criteria, true);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        if(mIntentFilter == null){
            mIntentFilter=new IntentFilter("android.location.PROVIDERS_CHANGED");
        }
        
        if(mReceiver == null){
            mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    LocationManager locationManager = (LocationManager) PendingMissionListActivity.this.getSystemService(Context.LOCATION_SERVICE);
                    if(locationManager != null){
                        locationManager.requestLocationUpdates(getProviderName(), LOCATION_MINTIME, LOCATION_MINDISTANCE, PendingMissionListActivity.this);
                    }
                  }
                };
        }
        
        registerReceiver(mReceiver, mIntentFilter);
        
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if(locationManager != null){
            locationManager.requestLocationUpdates(getProviderName(), LOCATION_MINTIME, LOCATION_MINDISTANCE, this);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        
        if(mReceiver != null){
            unregisterReceiver(mReceiver);
        }
        
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if(locationManager != null){
            locationManager.removeUpdates(this);
        }
    }
}
