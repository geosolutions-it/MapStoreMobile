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
import it.geosolutions.android.map.model.Layer;
import it.geosolutions.android.map.model.MSMMap;
import it.geosolutions.android.map.utils.SpatialDbUtils;
import it.geosolutions.android.map.MapsActivity.DrawerMode;
import it.geosolutions.android.map.view.MapViewManager;
import it.geosolutions.android.map.wfs.geojson.feature.Feature;
import it.geosolutions.geocollect.android.core.GeoCollectApplication;
import it.geosolutions.geocollect.android.core.R;
import it.geosolutions.geocollect.android.core.form.FormEditActivity;
import it.geosolutions.geocollect.android.core.login.LoginActivity;
import it.geosolutions.geocollect.android.core.login.LogoutActivity;
import it.geosolutions.geocollect.android.core.login.utils.NetworkUtil;
import it.geosolutions.geocollect.android.core.mission.PendingMissionListFragment.FragmentMode;
import it.geosolutions.geocollect.android.core.mission.utils.MissionUtils;
import it.geosolutions.geocollect.android.core.mission.utils.NavUtils;
import it.geosolutions.geocollect.android.core.mission.utils.PersistenceUtils;
import it.geosolutions.geocollect.android.core.mission.utils.SpatialiteUtils;
import it.geosolutions.geocollect.android.core.navigation.AbstractNavDrawerActivity;
import it.geosolutions.geocollect.android.core.navigation.NavDrawerActivityConfiguration;
import it.geosolutions.geocollect.android.core.navigation.NavDrawerAdapter;
import it.geosolutions.geocollect.android.core.navigation.NavDrawerItem;
import it.geosolutions.geocollect.android.map.ReturningMapInfoControl;
import it.geosolutions.geocollect.android.template.TemplateDownloadTask;
import it.geosolutions.geocollect.model.config.MissionTemplate;

import java.util.ArrayList;
import java.util.Iterator;

import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.MapView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
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
        PendingMissionListFragment.Callbacks, MapActivity {

    public static int SPATIAL_QUERY = 7001;

    public static final String ARG_CREATE_MISSIONFEATURE = "CREATE_MISSIONFEATURE";

    public static final String ARG_CREATING_TEMPLATE = "CREATING_MISSIONTEMPLATE";

    public static final String PREFS_USES_DOWNLOADED_TEMPLATE = "USES_DOWNLOADED_TEMPLATE";

    public static final String PREFS_DOWNLOADED_TEMPLATE_INDEX = "DOWNLOADED_TEMPLATE_INDEX";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("MISSION_LIST", "onCreate()");

        // TODO when is the login data necessary to load mission data ?
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getBaseContext());

        final String authKey = prefs.getString(LoginActivity.PREFS_AUTH_KEY, null);

        if (authKey == null) {

            startActivityForResult(new Intent(this, LoginActivity.class),
                    LoginActivity.REQUEST_LOGIN);

        } else if (NetworkUtil.isOnline(getBaseContext())) {

            // TODO --> set when to check the remote templates, for now always onCreate when authkey available && online
            Log.d(PendingMissionDetailActivity.class.getSimpleName(), "fetching remote Templates");

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
                                Log.w(PendingMissionListActivity.class.getSimpleName(),
                                        "error creating/updating table");
                            } else {
                                // if insert succesfull add to list of valid templates
                                validTemplates.add(t);
                            }
                        }
                    }
                    Log.d(PendingMissionListActivity.class.getSimpleName(), "database updated");

                    // 2. save valid templates
                    PersistenceUtils.saveDownloadedTemplates(getBaseContext(), validTemplates);

                    Log.d(PendingMissionListActivity.class.getSimpleName(),
                            "valid templates persisted");

                    // 3. update navdrawer menu
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            NavDrawerItem[] menu = NavUtils
                                    .getNavMenu(PendingMissionListActivity.this);

                            // cannot modify items of navdraweradapter --> create a new one

                            NavDrawerAdapter newNavDrawerAdapter = new NavDrawerAdapter(
                                    PendingMissionListActivity.this, R.layout.navdrawer_item, menu);

                            mDrawerList.setAdapter(newNavDrawerAdapter);

                            Log.d(PendingMissionListActivity.class.getSimpleName(),
                                    "navdrawer updated");

                        }
                    });
                }
            };
            task.execute(authKey);

        }

        // Default template
        // Initialize database
        // This should be the first thing the Activity does
        if (spatialiteDatabase == null) {

            spatialiteDatabase = SpatialiteUtils.openSpatialiteDB(this, "geocollect/genova.sqlite");

            if (spatialiteDatabase != null && !spatialiteDatabase.dbversion().equals("unknown")) {

                MissionTemplate t = MissionUtils.getDefaultTemplate(this);

                if (!PersistenceUtils.createOrUpdateTablesForTemplate(t, spatialiteDatabase)) {
                    Log.e(PendingMissionListActivity.class.getSimpleName(),
                            "error creating/updating tables for " + t.nameField);
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
                        R.id.pendingmission_list)).switchAdapter(FragmentMode.CREATION);

            }
        }
    }

    /**
     * Callback method from {@link PendingMissionListFragment.Callbacks} indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(Object obj) {

        FragmentMode fragmentMode = ((PendingMissionListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.pendingmission_list)).getFragmentMode();

        if (fragmentMode == FragmentMode.PENDING) {
            if (mTwoPane) {
                // DELETE PREVIOUS MAP VIEWS
                mapViewManager.destroyMapViews();
                // In two-pane mode, show the detail view in this activity by
                // adding or replacing the detail fragment using a
                // fragment transaction.

                Bundle arguments = new Bundle();
                arguments.putString(PendingMissionDetailFragment.ARG_ITEM_ID, ((Feature) obj).id);
                arguments.putSerializable(PendingMissionDetailFragment.ARG_ITEM_FEATURE,
                        (Feature) obj);
                PendingMissionDetailFragment fragment = new PendingMissionDetailFragment();
                fragment.setArguments(arguments);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.pendingmission_detail_container, fragment).commit();

            } else {
                // In single-pane mode, simply start the detail activity
                // for the selected item ID.
                Intent detailIntent = new Intent(this, PendingMissionDetailActivity.class);
                detailIntent.putExtra(PendingMissionDetailFragment.ARG_ITEM_ID, ((Feature) obj).id);
                detailIntent.putExtra(PendingMissionDetailFragment.ARG_ITEM_FEATURE, (Feature) obj);
                startActivity(detailIntent);
            }
        } else {

            Log.d(PendingMissionListFragment.class.getSimpleName(), "created mission clicked "
                    + ((MissionFeature) obj).id);

            Intent editIntent = new Intent(this, FormEditActivity.class);
            editIntent.putExtra(ARG_CREATE_MISSIONFEATURE, true);
            editIntent
                    .putExtra(PendingMissionDetailFragment.ARG_ITEM_FEATURE, (MissionFeature) obj);
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
            // only one fragment for now, the other options are indipendent activities
            // switch to pending mode

            clearDetailFragment();

            final FragmentMode mode = id == 101 ? FragmentMode.PENDING : FragmentMode.CREATION;

            Editor ed = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();
            ed.putBoolean(PREFS_USES_DOWNLOADED_TEMPLATE, false);
            ed.commit();

            MissionTemplate mt = MissionUtils.getDefaultTemplate(getBaseContext());

            ((PendingMissionListFragment) getSupportFragmentManager().findFragmentById(
                    R.id.pendingmission_list)).setTemplate(mt);
            
            ((GeoCollectApplication)getApplication()).setTemplate(mt);
            
            if (id == 101) {
                ((PendingMissionListFragment) getSupportFragmentManager().findFragmentById(
                        R.id.pendingmission_list)).restartLoader(0);
            }
            ((PendingMissionListFragment) getSupportFragmentManager().findFragmentById(
                    R.id.pendingmission_list)).switchAdapter(mode);

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
        }

        // downloaded templates will have a dynamic id currently starting from 2000
        if (id >= 2000) {

            final ArrayList<MissionTemplate> downloadedTemplates = PersistenceUtils
                    .loadSavedTemplates(getBaseContext());

            final int index = id % 2000;

            final int templateIndex = index / 2;

            final MissionTemplate t = downloadedTemplates.get(templateIndex);

            Log.d(PendingMissionListActivity.class.getSimpleName(), "downloaded template "
                    + templateIndex + " selected : " + t.id);

            final FragmentMode mode = index % 2 == 0 ? FragmentMode.PENDING : FragmentMode.CREATION;

            clearDetailFragment();

            Editor ed = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();
            ed.putBoolean(PREFS_USES_DOWNLOADED_TEMPLATE, true);
            ed.putInt(PREFS_DOWNLOADED_TEMPLATE_INDEX, templateIndex);
            ed.commit();

            ((PendingMissionListFragment) getSupportFragmentManager().findFragmentById(
                    R.id.pendingmission_list)).setTemplate(t);
            
            ((GeoCollectApplication)getApplication()).setTemplate(t);
            
            if (index % 2 == 0) {
                ((PendingMissionListFragment) getSupportFragmentManager().findFragmentById(
                        R.id.pendingmission_list)).restartLoader(templateIndex + 1);
            }
            ((PendingMissionListFragment) getSupportFragmentManager().findFragmentById(
                    R.id.pendingmission_list)).switchAdapter(mode);

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
            } else {
                confirmExit();
            }
        } else {
            confirmExit();
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
        Log.v("MAPVIEW", "created mapview with id:" + i);
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
                Log.v("MISSION_LIST", "Spatialite Database Closed");
            } catch (jsqlite.Exception e) {
                Log.e("MISSION_LIST", Log.getStackTraceString(e));
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
                finish();
                return;
            }
        }
        // We need to explicitly call the child Fragments onActivityResult()
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            try {
                fragment.onActivityResult(requestCode, resultCode, data);
            }catch (NullPointerException npe){
                // TODO This is ABS bug, need to switch to ActionBarCompat
                Log.e("PMLA", npe.getLocalizedMessage(), npe);
            }
        }
    }

    /**
     * Launches the Map activity
     */
    public void launchFullMap() {
        // setup map options
        // TODO parametrize it
        Intent launch = new Intent(this, MapsActivity.class);
        launch.setAction(Intent.ACTION_VIEW);
        launch.putExtra(MapsActivity.PARAMETERS.CONFIRM_ON_EXIT, false);

        // ArrayList<Layer> layers = (ArrayList<Layer>) LocalPersistence.readObjectFromFile(this, LocalPersistence.CURRENT_MAP);
        // if(layers == null || layers.isEmpty()){

        MissionTemplate t = ((PendingMissionListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.pendingmission_list)).getCurrentMissionTemplate();

        MSMMap m = SpatialDbUtils.mapFromDb();

        for (Iterator<Layer> it = m.layers.iterator(); it.hasNext();) {
            Layer layer = it.next();
            if (!(layer.getTitle().equals(t.schema_seg.localSourceStore)
                    || layer.getTitle().equals(t.schema_sop.localFormStore) || layer.getTitle()
                    .equals(t.schema_seg.localSourceStore + "_new"))) {
                Log.d(PendingMissionListActivity.class.getSimpleName(), layer.getTitle()
                        + " not corresponding to current schema " + t.schema_seg.localSourceStore);
                it.remove();
            }

        }

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
        launch.putExtra(MapsActivity.PARAMETERS.LAT, 44.40565);
        launch.putExtra(MapsActivity.PARAMETERS.LON, 8.946256);
        launch.putExtra(MapsActivity.PARAMETERS.ZOOM_LEVEL, (byte) 11);
        launch.putExtra(MapsActivity.PARAMETERS.ZOOM_LEVEL_MIN, (byte) 11);
        launch.putExtra(MapsActivity.PARAMETERS.ZOOM_LEVEL_MAX, (byte) 19);
        launch.putExtra(MapsActivity.MSM_MAP, m);
        // select here a drawer mode
        launch.putExtra(MapsActivity.PARAMETERS.DRAWER_MODE, DrawerMode.ONLY_LEFT.ordinal());
        // }

        launch.putExtra(MapsActivity.PARAMETERS.CUSTOM_MAPINFO_CONTROL,
                new ReturningMapInfoControl());

        // launch.putExtra(MapsActivity.LAYERS_TO_ADD, m.layers) ;
        startActivityForResult(launch, SPATIAL_QUERY);
    }
}
