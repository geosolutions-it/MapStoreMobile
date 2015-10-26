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
package it.geosolutions.geocollect.android.map;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.mapsforge.android.maps.BackgroundSourceType;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.mapgenerator.MapRenderer;
import org.mapsforge.android.maps.mapgenerator.TileCache;
import org.mapsforge.android.maps.mapgenerator.databaserenderer.DatabaseRenderer;
import org.mapsforge.android.maps.mapgenerator.mbtiles.MbTilesDatabaseRenderer;
import org.mapsforge.core.model.GeoPoint;
import org.mapsforge.core.model.MapPosition;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import it.geosolutions.android.map.MapsActivity;
import it.geosolutions.android.map.MapsActivity.PARAMETERS;
import it.geosolutions.android.map.activities.GetFeatureInfoLayerListActivity;
import it.geosolutions.android.map.activities.MBTilesLayerOpacitySettingActivity;
import it.geosolutions.android.map.activities.MapActivityBase;
import it.geosolutions.android.map.activities.about.InfoView;
import it.geosolutions.android.map.control.CoordinateControl;
import it.geosolutions.android.map.control.LocationControl;
import it.geosolutions.android.map.control.MapControl;
import it.geosolutions.android.map.control.MapInfoControl;
import it.geosolutions.android.map.control.MarkerControl;
import it.geosolutions.android.map.database.SpatialDataSourceManager;
import it.geosolutions.android.map.dto.MarkerDTO;
import it.geosolutions.android.map.fragment.sources.SourcesFragment;
import it.geosolutions.android.map.geostore.activities.GeoStoreResourceDetailActivity;
import it.geosolutions.android.map.geostore.activities.GeoStoreResourcesActivity;
import it.geosolutions.android.map.geostore.model.Resource;
import it.geosolutions.android.map.mapstore.model.MapStoreConfiguration;
import it.geosolutions.android.map.mbtiles.MbTilesLayer;
import it.geosolutions.android.map.model.Attribute;
import it.geosolutions.android.map.model.Feature;
import it.geosolutions.android.map.model.Layer;
import it.geosolutions.android.map.model.MSMMap;
import it.geosolutions.android.map.overlay.MarkerOverlay;
import it.geosolutions.android.map.overlay.items.DescribedMarker;
import it.geosolutions.android.map.overlay.managers.MultiSourceOverlayManager;
import it.geosolutions.android.map.overlay.managers.OverlayManager;
import it.geosolutions.android.map.overlay.switcher.LayerSwitcherFragment;
import it.geosolutions.android.map.preferences.EditPreferences;
import it.geosolutions.android.map.style.StyleManager;
import it.geosolutions.android.map.utils.MapFilesProvider;
import it.geosolutions.android.map.utils.MarkerUtils;
import it.geosolutions.android.map.utils.SpatialDbUtils;
import it.geosolutions.android.map.utils.StorageUtils;
import it.geosolutions.android.map.view.AdvancedMapView;
import it.geosolutions.geocollect.android.app.CreditsActivity;
import it.geosolutions.geocollect.android.app.R;
import it.geosolutions.geocollect.android.core.login.LoginActivity;
import it.geosolutions.geocollect.android.core.login.LogoutActivity;
import it.geosolutions.geocollect.android.core.mission.PendingMissionListActivity;
import it.geosolutions.geocollect.android.core.mission.utils.NavUtils;
import it.geosolutions.geocollect.android.core.navigation.NavDrawerActivityConfiguration;
import it.geosolutions.geocollect.android.core.navigation.NavDrawerAdapter;
import it.geosolutions.geocollect.android.core.navigation.NavDrawerItem;

/**
 * Custom Map to use with GeoCollect application
 * This version adds a button to zoom to the initial BBOX
 * There are also a custom layout on the
 * resources layout-land/activity_map.xml
 * to place the button bar on the bottom, as in the other maps
 * 
 * @author Lorenzo Pini (lorenzo.pini@geo-solutions.it)
 */
public class GeoCollectMapActivity extends MapActivityBase {

    private static String TAG = GeoCollectMapActivity.class.getName();

    // default path for files
    private static final File MAP_DIR = MapFilesProvider.getBaseDirectoryFile();
    private static final File MAP_FILE = MapFilesProvider.getBackgroundMapFile();

    // ------------------------------------------------------
    // SAVE INSTANCE STATE BUNDLE PARAMETERS
    // ------------------------------------------------------
    private boolean dbLoaded;

    // ------------------------------------------------------
    // PUBLIC VARIABLES
    // ------------------------------------------------------
    public AdvancedMapView mapView;

    public OverlayManager overlayManager;

    // ------------------------------------------------------
    // CONSTANTS
    // ------------------------------------------------------

    /** FEATURE_DEFAULT_ID */
    private static final String FEATURE_DEFAULT_ID = "OGC_FID";

    /** DB_LOADED_FLAG */
    private static final String DB_LOADED_FLAG = "dbLoaded";

    private static final String FEATUREIDFIELD_FLAG = "fidField";

    /** Chosen featureID field */
    private String featureIdField;

    /** CANCONFRIM_FLAG */
    private static final String CANCONFRIM_FLAG = "canConfirm_flag";

    private static boolean canConfirm;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    /**
     * LAYOUT PARAMETERS
     */
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private View mLayerMenu;

    private MultiSourceOverlayManager layerManager;

    protected MapInfoControl mic;

    protected NavDrawerActivityConfiguration navConf ;
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inf = getSupportMenuInflater();
        inf.inflate(R.menu.simple_map_menu, (Menu) menu);

        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        MenuInflater inf = getSupportMenuInflater();
        inf.inflate(R.menu.simple_map_menu, (Menu) menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // center on the marker, preferably the updated
        if (item.getItemId() == R.id.center) {
            centerMapFile();
        } else
        // Activate the filter control
        if (item.getItemId() == R.id.filter) {
            if (mic != null && mic.getActivationListener() != null) {
                mic.getActivationListener().onClick(item.getActionView());
            }
        } else 
        // Drawer part
        if (item.getItemId() == android.R.id.home) {

            if (mDrawerList != null && mDrawerLayout.isDrawerOpen(mDrawerList)) {
                mDrawerLayout.closeDrawer(mDrawerList);
            } else {
                if (mDrawerList != null) {
                    mDrawerLayout.openDrawer(mDrawerList);
                }
                if (mLayerMenu != null) {
                    mDrawerLayout.closeDrawer(mLayerMenu);
                }
            }
            // layer menu part
        } else if (item.getItemId() == R.id.layer_menu_action) {
            if (mLayerMenu != null && mDrawerLayout.isDrawerOpen(mLayerMenu)) {
                mDrawerLayout.closeDrawer(mLayerMenu);
            } else {
                if (mLayerMenu != null) {
                    mDrawerLayout.openDrawer(mLayerMenu);
                }
                if (mDrawerList != null) {
                    mDrawerLayout.closeDrawer(mDrawerList);
                }
            }
        } else if (item.getItemId() == R.id.settings) {
            Intent pref = new Intent(this, EditPreferences.class);
            startActivity(pref);
        } else if (item.getItemId() == R.id.infoview) {
            Intent info = new Intent(this, InfoView.class);
            startActivity(info);
        } else if (item.getItemId() == R.id.exitview) {
            confirmExit();
        }
        return super.onOptionsItemSelected(item);
        
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // setup loading
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setSupportProgressBarIndeterminateVisibility(false);
        super.onCreate(savedInstanceState);

        //
        // LAYOUT INITIALIZATION
        //
        setContentView(R.layout.geocollect_main);

        //
        // MAP INITIALIZATION
        //
        // create overlay manager
        boolean mapLoaded = initMap(savedInstanceState);
        layerManager = new MultiSourceOverlayManager(mapView);
        overlayManager = layerManager;
        // setup slide menu(es)
        //setupDrawerLayout();
        dbLoaded = initDb();
        // if something went wrong during db and map initialization,
        // we should stop
        if (!mapLoaded && !dbLoaded) {
            Toast.makeText(this, "DB not loaded", Toast.LENGTH_LONG).show();
        }
        
        // SETUP MAP
        
        if(savedInstanceState !=null){  
            layerManager.restoreInstanceState(savedInstanceState);
        }else{
            layerManager.defaultInit();
            
            if(getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey(MapsActivity.MSM_MAP)){
                
                  layerManager.loadMap((MSMMap)getIntent().getExtras().getSerializable(MapsActivity.MSM_MAP));
                
            }else{
                boolean dontLoadMBTileLayer = MapFilesProvider.getBackgroundSourceType() == BackgroundSourceType.MBTILES ? true : false;
                MSMMap map = SpatialDbUtils.mapFromDb(dontLoadMBTileLayer);
                StorageUtils.setupSources(this);
                //This adds layers also if its called loadMap but it will not order layers
                //layerManager.loadMap(map);
                //so use this instead
                addLayersOrdered(map.layers);
            }

        }
        
        //
        // LEFT MENU INITIALIZATION
        //
        navConf = getNavDrawerConfiguration();
        
        mTitle = mDrawerTitle = getTitle();
        
        mDrawerLayout = (DrawerLayout) findViewById(navConf.getDrawerLayoutId());
        mDrawerList = (ListView) findViewById(navConf.getLeftDrawerId());
        mDrawerList.setAdapter(navConf.getBaseAdapter());
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.drawable.ic_drawer,
                navConf.getDrawerOpenDesc(),
                navConf.getDrawerCloseDesc()
                ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                supportInvalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        
        
        // CONTEXT MENU
        this.registerForContextMenu(mapView);
        mapView.getMapScaleBar().setShowMapScaleBar(true);

        overlayManager.setMarkerOverlay(new MarkerOverlay());
        createMarkers(savedInstanceState);

        String action = getIntent().getAction();
        if (Intent.ACTION_VIEW.equals(action)) {
            // prevent editing
            canConfirm = false;
        } else {
            // Default edit
            canConfirm = true;
            this.addConfirmButton();
        }
        addControls(savedInstanceState);

        centerMapFile();
        loadFromBundle();
        
        mic = new ReturningMapInfoControl();
        mic.activity = this;
        mic.mapView = mapView;

        // if the mapView had some controls, get the group of them
        // this check is needed until the map library will have named groups
        List<MapControl> mcList = mapView.getControls();
        if (mcList != null && !mcList.isEmpty()) {
            MapControl mc;
            boolean found = false;
            for (int i = 0; i < mcList.size() && !found; i++) {
                mc = mcList.get(i);
                if (mc.getGroup() != null) {
                    mic.setGroup(mc.getGroup());
                    mic.getGroup().add(mic);
                    found = true;
                }
            }

        }

        mapView.addControl(mic);
        mic.instantiateListener();
        
        // Enable Home button on ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

    }

    /**
     * 
     * @return
     */
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

    
    /**
     * load a map from bundle
     */
    private void loadFromBundle() {
        Bundle data = getIntent().getExtras();
        if (data == null)
            return;
        Resource resource = (Resource) data.getSerializable(PARAMETERS.RESOURCE);
        if (resource != null) {
            String geoStoreUrl = data.getString(PARAMETERS.GEOSTORE_URL);
            loadGeoStoreResource(resource, geoStoreUrl);
        }

    }

    /**
     * Resume the state of:
     * 
     * * tile cache * Controls
     */
    @Override
    protected void onResume() {
        super.onResume();
        loadPersistencePreferences();
        checkIfMapViewNeedsBackgroundUpdate();
        // Refresh control beacuse any changes can be changed
        for (MapControl mic : mapView.getControls()) {
            mic.refreshControl(GetFeatureInfoLayerListActivity.BBOX_REQUEST,
                    GetFeatureInfoLayerListActivity.BBOX_REQUEST, null);
        }

        // Some debug
        Intent i = getIntent();
        if (i != null) {
            String a = i.getAction();
            Log.v(TAG, "onResume() Action:" + a);
        }
    }

    /**
     * add the confirm button to the control bar
     */
    private void addConfirmButton() {
        Log.v(TAG, "adding confirm button");
        ImageButton b = (ImageButton) findViewById(R.id.button_confirm_marker_position);
        b.setVisibility(View.VISIBLE);
        final MapActivityBase activity = this;
        b.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                if (!canConfirm) {
                    Toast.makeText(activity, R.string.error_unable_getfeature_db, Toast.LENGTH_LONG).show();
                    return;
                }

                new AlertDialog.Builder(activity).setTitle(R.string.button_confirm_marker_position_title)
                        .setMessage(R.string.button_confirm_marker_position)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent returnIntent = new Intent();
                                // get current markers
                                ArrayList<DescribedMarker> markers = overlayManager.getMarkerOverlay().getMarkers();
                                // serialize markers in the response
                                returnIntent.putParcelableArrayListExtra(MapsActivity.PARAMETERS.MARKERS,
                                        MarkerUtils.getMarkersDTO(markers));
                                setResult(RESULT_OK, returnIntent);
                                finish();
                                return;
                                // if you don't want to return data:
                                // setResult(RESULT_CANCELED, returnIntent);
                                // finish();
                                // activity.finish();
                            }

                        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        }).show();

            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putBoolean(DB_LOADED_FLAG, dbLoaded);

        savedInstanceState.putString(FEATUREIDFIELD_FLAG, featureIdField);

        savedInstanceState.putBoolean(CANCONFRIM_FLAG, canConfirm);
        // MARKERS
        // get current markers
        overlayManager.saveInstanceState(savedInstanceState);
        for (MapControl mc : mapView.getControls()) {
            mc.saveState(savedInstanceState);
        }
    }

    /**
     * Save the layer state
     */
    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause saving layers");
    }

    /**
     * Force a double tap to close the app
     * 
     * @Override public void onBackPressed() {
     * 
     *           //You may also add condition if (doubleBackToExitPressedOnce || fragmentManager.getBackStackEntryCount() != 0) // in case of
     *           Fragment-based add if (mRecentlyBackPressed) { mExitHandler.removeCallbacks(mExitRunnable); mExitHandler = null;
     *           super.onBackPressed(); } else { mRecentlyBackPressed = true; Toast.makeText(this, "press again to exit", Toast.LENGTH_SHORT).show();
     *           mExitHandler.postDelayed(mExitRunnable, delay); } }
     */

    /**
     * Ask to confirm when exit
     */
    @Override
    public void onBackPressed() {

        confirmExit();

    }

    /**
     * Show a confirm message to exit
     */
    public void confirmExit() {
        boolean confirmOnExit = getIntent().getExtras().getBoolean(MapsActivity.PARAMETERS.CONFIRM_ON_EXIT, true);
        if (confirmOnExit) {
            new AlertDialog.Builder(this).setTitle(R.string.button_confirm_exit_title)
                    .setMessage(R.string.button_confirm_exit)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            return;
                        }
                    }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    }).show();
        } else {
            finish();
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
        dbLoaded = savedInstanceState.getBoolean(DB_LOADED_FLAG);

        featureIdField = savedInstanceState.getString(FEATUREIDFIELD_FLAG);
        canConfirm = savedInstanceState.getBoolean(CANCONFRIM_FLAG);

        // Restore state of the controls?

    }


    /**
     * Create the markers and add them to the MarkerOverlay Gets it from the Intent or from the savedInstanceState Assign them the proper <GeoPoint>
     * if missing
     * 
     * @param savedInstanceState
     */
    private void createMarkers(Bundle savedInstanceState) {
        List<MarkerDTO> markerDTOs = null;
        // add the OverlayItem to the ArrayItemizedOverlay
        ArrayList<DescribedMarker> markers = null;
        if (savedInstanceState != null) {
            markerDTOs = savedInstanceState.getParcelableArrayList(MapsActivity.PARAMETERS.MARKERS);
            markers = MarkerUtils.markerDTO2DescribedMarkers(this, markerDTOs);
        } else {
            markerDTOs = getIntent().getParcelableArrayListExtra(MapsActivity.PARAMETERS.MARKERS);
            markers = MarkerUtils.markerDTO2DescribedMarkers(this, markerDTOs);
            // retrieve geopoint if missing
            if (getIntent().getExtras() == null) {
                return;
            }
            featureIdField = getIntent().getExtras().getString(PARAMETERS.FEATURE_ID_FIELD);
            if (featureIdField == null) {
                featureIdField = FEATURE_DEFAULT_ID;
            }
            if (!MarkerUtils.assignFeaturesFromDb(markers, featureIdField)) {
                Toast.makeText(this, R.string.error_unable_getfeature_db, Toast.LENGTH_LONG).show();
                canConfirm = false;
                // TODO dialog : download features for this area?
            }
        }
        // create an ItemizedOverlay with the default marker
        overlayManager.getMarkerOverlay().getOverlayItems().addAll(markers);
    }

    /**
     * Initializes the database
     * 
     * @return true if the initialization was successful
     */
    private boolean initDb() {
        // init styleManager
        StyleManager.getInstance().init(this, MAP_DIR);
        // init Db
        SpatialDataSourceManager dbManager = SpatialDataSourceManager.getInstance();

        try {
            // Only if not already loaded some tables
            if (dbManager.getSpatialVectorTables(false).size() <= 0) {
                dbManager.init(MAP_DIR);
            }
        } catch (Exception e) {

            return false;
        }
        return true;
    }
    
    /**
     * Initialize the map with Controls and background
     * 
     * @param savedInstanceState
     * 
     * @return
     */
    private boolean initMap(Bundle savedInstanceState) {
        // setContentView(R.layout.activity_map);
        Log.v(TAG, "Map Activated");
        LayoutInflater inflater = LayoutInflater.from(GeoCollectMapActivity.this);
        inflater.inflate(R.layout.activity_map, (FrameLayout)findViewById(R.id.content_frame));
        
        this.mapView = (AdvancedMapView) findViewById(R.id.advancedMapView);

        mapView.setClickable(true);
        mapView.setBuiltInZoomControls(true);

        // mapView.setDebugSettings(new DebugSettings(true, true, false));

        mapView.getMapZoomControls().setZoomLevelMax((byte) 24);
        mapView.getMapZoomControls().setZoomLevelMin((byte) 1);

        final String filePath = PreferenceManager.getDefaultSharedPreferences(this).getString(
                MapView.MAPSFORGE_BACKGROUND_FILEPATH, null);
        final int type = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString(
                MapView.MAPSFORGE_BACKGROUND_RENDERER_TYPE, "0"));
        File mapfile = null;

        // if the map file was edited in the preferences
        if (filePath != null && type == 0) {
            mapfile = new File(filePath);
        }

        if (mapfile != null && mapfile.exists()) {
            // use it
            mapView.setMapFile(new File(filePath));

        } else if (MAP_FILE != null) {

            Log.i(TAG, "setting background file");
            mapView.setMapFile(MAP_FILE);
            loadPersistencePreferences();

        } else {
            Log.i(TAG, "unable to set background file");
            // return false;
        }

        return true;
    }

    /**
     * Add controls to the mapView and to the Buttons
     * 
     * @param savedInstanceState
     */
    private void addControls(Bundle savedInstanceState) {
        String action = getIntent().getAction();
        Log.v(TAG, "action: " + action);

        // Coordinate Control
        mapView.addControl(new CoordinateControl(mapView, true));
        List<MapControl> group = new ArrayList<MapControl>();

        // Info Control
        MapInfoControl ic;
        if (getIntent().hasExtra(MapsActivity.PARAMETERS.CUSTOM_MAPINFO_CONTROL)) {
            ic = (MapInfoControl) getIntent().getParcelableExtra(MapsActivity.PARAMETERS.CUSTOM_MAPINFO_CONTROL);
            ic.activity = this;
            ic.mapView = mapView;
            ic.instantiateListener();
        } else {
            ic = new MapInfoControl(mapView, this);
        }
        ic.setActivationButton((ImageButton) findViewById(R.id.ButtonInfo));

        mapView.addControl(ic);

        if (!Intent.ACTION_VIEW.equals(action)) {
            Log.v(TAG, "Adding MarkerControl");

            // Marker Control
            MarkerControl mc = new MarkerControl(mapView);
            // activation button
            ImageButton mcbmb = (ImageButton) findViewById(R.id.ButtonMarker);
            mcbmb.setVisibility(View.VISIBLE);
            mc.setActivationButton(mcbmb);
            // info button
            ImageButton mcib = (ImageButton) findViewById(R.id.marker_info_button);
            mcib.setVisibility(View.VISIBLE);
            mc.setInfoButton(mcib);

            mapView.addControl(mc);
            group.add(mc);
            mc.setGroup(group);
            mc.setMode(MarkerControl.MODE_EDIT);
        }

        // My location Control
        LocationControl lc = new LocationControl(mapView);
        lc.setActivationButton((ImageButton) findViewById(R.id.ButtonLocation));
        mapView.addControl(lc);

        // create and add group
        group.add(ic);

        ic.setGroup(group);

        // Set modes for controls
        if (Intent.ACTION_VIEW.equals(action)) {
            ic.setMode(MapInfoControl.MODE_VIEW);
        } else if (Intent.ACTION_EDIT.equals(action)) {
            ic.setMode(MapInfoControl.MODE_EDIT);
            // Default edit mode
        } else {
            ic.setMode(MapInfoControl.MODE_EDIT);
        }
        if (savedInstanceState != null) {
            for (MapControl c : mapView.getControls()) {
                c.restoreState(savedInstanceState);
            }
        }

    }

    /**
     * center the map on the markers
     */
    public void centerMapFile() {
        MarkerOverlay mo = mapView.getMarkerOverlay();
        MapPosition mp = mapView.getMapViewPosition().getMapPosition();

        Intent intent = getIntent();
        if (intent.hasExtra(PARAMETERS.LAT) && intent.hasExtra(PARAMETERS.LON)
                && intent.hasExtra(PARAMETERS.ZOOM_LEVEL)) {
            double lat = intent.getDoubleExtra(PARAMETERS.LAT, 43.68411);
            double lon = intent.getDoubleExtra(PARAMETERS.LON, 10.84899);
            byte zoom_level = intent.getByteExtra(PARAMETERS.ZOOM_LEVEL, (byte) 13);
            byte zoom_level_min = intent.getByteExtra(PARAMETERS.ZOOM_LEVEL_MIN, (byte) 0);
            byte zoom_level_max = intent.getByteExtra(PARAMETERS.ZOOM_LEVEL_MAX, (byte) 30);
            /*
             * ArrayList<MarkerDTO> list_marker = intent.getParcelableArrayListExtra(PARAMETERS.MARKERS); MarkerDTO mark = list_marker.get(0);
             */
            mp = new MapPosition(new GeoPoint(lat, lon), zoom_level);
            mapView.getMapViewPosition().setMapPosition(mp);
            mapView.getMapZoomControls().setZoomLevelMin(zoom_level_min);
            mapView.getMapZoomControls().setZoomLevelMax(zoom_level_max);
        } else {
            if (mo != null) {
                // support only one marker
                MapPosition newMp = MarkerUtils.getMarkerCenterZoom(mo.getMarkers(), mp);
                if (newMp != null) {
                    mapView.getMapViewPosition().setMapPosition(newMp);
                }
            }
        }
    }

    /**
     * Center the map to a point and zoomLevel
     * 
     * @param pp
     * @param zoomlevel
     */
    public void setPosition(GeoPoint pp, byte zoomlevel) {

        mapView.getMapViewPosition().setMapPosition(new MapPosition(pp, zoomlevel));
    }

    /**
     * Opena the Data List activity
     * 
     * @param item
     * @return
     */

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent incomingIntent) {
        super.onActivityResult(requestCode, resultCode, incomingIntent);
        Log.d(TAG, "onActivityResult");

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
        
        if (requestCode == LayerSwitcherFragment.OPACITY_SETTIN_REQUEST_ID) {

            final int newValue = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getInt(
                    MBTilesLayerOpacitySettingActivity.MBTILES_OPACITY_ID, 192);

            ArrayList<Layer> layers = layerManager.getLayers();

            for (Layer l : layers) {
                if (l instanceof MbTilesLayer) {
                    l.setOpacity(newValue);
                    layerManager.redrawLayer(l);

                }
            }

            // its not necessary to handle the other stuff
            return;
        }

        if (requestCode == GetFeatureInfoLayerListActivity.BBOX_REQUEST && resultCode == RESULT_OK) {
            // the response can contain a feature to use to replace the current marker
            // on the map
            manageMarkerSubstitutionAction(incomingIntent);
        }

        // controls can be refreshed getting the result of an intent, in this case
        // each control knows which intent he sent with their requestCode/resultCode
        for (MapControl control : mapView.getControls()) {
            control.refreshControl(requestCode, resultCode, incomingIntent);
        }
        // reload stores in the panel (we do it everyTime, maybe there is a better way
        SourcesFragment sf = (SourcesFragment) getSupportFragmentManager().findFragmentById(R.id.right_drawer);
        if (sf != null) {
            sf.reloadStores();
        }
        // manager mapstore configuration load
        if (incomingIntent == null){
            return;
        }
        Bundle b = incomingIntent.getExtras();
        if (requestCode == MapsActivity.DATAPROPERTIES_REQUEST_CODE) {
            mapView.getOverlayController().redrawOverlays();
            // close right drawer
            if (mLayerMenu != null) {
                if (mDrawerLayout.isDrawerOpen(mLayerMenu)) {
                    mDrawerLayout.closeDrawer(mLayerMenu);
                }
            }
        }
        Resource resource = (Resource) incomingIntent.getSerializableExtra(GeoStoreResourceDetailActivity.PARAMS.RESOURCE);
        if (resource != null) {
            String geoStoreUrl = incomingIntent.getStringExtra(GeoStoreResourcesActivity.PARAMS.GEOSTORE_URL);
            loadGeoStoreResource(resource, geoStoreUrl);
        }
        if (b.containsKey(MapsActivity.MAPSTORE_CONFIG)) {
            overlayManager.loadMapStoreConfig((MapStoreConfiguration) b.getSerializable(MapsActivity.MAPSTORE_CONFIG));
        }
        if (b.containsKey(MapsActivity.MSM_MAP)) {
            layerManager.loadMap((MSMMap) b.getSerializable(MapsActivity.MSM_MAP));

        }
        ArrayList<Layer> layersToAdd = (ArrayList<Layer>) b.getSerializable(MapsActivity.LAYERS_TO_ADD);
        if (layersToAdd != null) {
            addLayers(layersToAdd);
        }

    }

    /**
     * Add layers to the map
     * 
     * @param layersToAdd
     */
    private void addLayers(ArrayList<Layer> layersToAdd) {
        ArrayList<Layer> layers = new ArrayList<Layer>(layerManager.getLayers());
        layers.addAll(layersToAdd);
        layerManager.setLayers(layers);
        // close right drawer
        if (mLayerMenu != null) {
            if (mDrawerLayout.isDrawerOpen(mLayerMenu)) {
                mDrawerLayout.closeDrawer(mLayerMenu);
            }
        }
    }

    /**
     * Load a geostore resource on the map
     * 
     * @param resource the resource id
     * @param geoStoreUrl
     */
    private void loadGeoStoreResource(Resource resource, String geoStoreUrl) {
        MapUtils.loadMapStoreConfig(geoStoreUrl, resource, overlayManager, mapView);
        // close right drawer
        if (mLayerMenu != null) {
            if (mDrawerLayout.isDrawerOpen(mLayerMenu)) {
                mDrawerLayout.closeDrawer(mLayerMenu);
            }
        }
    }

    /**
     * Manages the marker substitution
     */
    private void manageMarkerSubstitutionAction(Intent data) {

        @SuppressWarnings("unchecked")
        ArrayList<Attribute> arrayList = (ArrayList<Attribute>) data.getExtras().getSerializable(
                GetFeatureInfoLayerListActivity.RESULT_FEATURE_EXTRA);
        Feature f = new Feature(arrayList);
        String layer = data.getExtras().getString(GetFeatureInfoLayerListActivity.LAYER_FEATURE_EXTRA);

        Attribute a = f.getAttribute(featureIdField);

        String attributeValue = null;
        if (a != null) {
            attributeValue = a.getValue();
        }
        replaceMarker(layer, featureIdField, attributeValue, f);

    }

    /**
     * Replace the default marker with position and properties from the arguments
     * 
     * @param layer
     * @param attributeName
     * @param attributeValue
     * @param f
     */
    private void replaceMarker(String layer, String attributeName, String attributeValue, Feature f) {
        DescribedMarker marker = getDefaultMarker();

        if (marker != null) {
            setMarkerProperties(layer, attributeName, attributeValue, attributeValue, marker, f);
        }
    }

    /**
     * @param layer
     * @param attributeName
     * @param a
     * @param attributeValue
     * @param marker
     * @param f
     */
    private void setMarkerProperties(String layer, String attributeName, String id, String attributeValue,
            DescribedMarker marker, Feature f) {
        GeoPoint p = SpatialDbUtils.getGeopointFromLayer(layer, attributeName, attributeValue);
        // get Only the first
        if (p != null) {
            // TODO ask if you want to change
            // if yes move and center map
            marker.setGeoPoint(p);
            marker.setFeatureId(id);
            marker.setFeature(f);
            mapView.redraw();
            canConfirm = true;
        } else {
            Toast.makeText(this, R.string.error_getting_data_from_database, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * get a marker from markerOverlay. The one highlighted or the first one
     * 
     * @return
     */
    private DescribedMarker getDefaultMarker() {
        MarkerOverlay m = mapView.getMarkerOverlay();
        // add the marker overlay if not present
        if (m == null) {
            overlayManager.toggleOverlayVisibility(R.id.markers, true);
            m = mapView.getMarkerOverlay();
        }

        DescribedMarker marker = m.getHighlighted();
        if (marker == null) {
            List<DescribedMarker> markers = m.getMarkers();
            if (markers.size() > 0) {
                marker = markers.get(0);
            } else {
                // TODO add a new marker
            }
        }
        return marker;
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        TileCache fileSystemTileCache = this.mapView.getFileSystemTileCache();

        Log.v(TAG,"Capacity: " + fileSystemTileCache.getCapacity() + ", Persistence: " + fileSystemTileCache.isPersistent());
        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
        // Checks the orientation of the screen for landscape and portrait and set portrait mode always
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        }
    }

    /**
     * Load tile caching preferences used sharedPreferences : * TileCachePersistence * TileCacheSize
     */
    public void loadPersistencePreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean persistent = sharedPreferences.getBoolean("TileCachePersistence", true);
        Log.v(TAG, "Cache Size: " + sharedPreferences.getInt("TileCacheSize", MapsActivity.FILE_SYSTEM_CACHE_SIZE_DEFAULT)
                + ", Persistent: " + persistent);
        int capacity = Math.min(sharedPreferences.getInt("TileCacheSize", MapsActivity.FILE_SYSTEM_CACHE_SIZE_DEFAULT),
                MapsActivity.FILE_SYSTEM_CACHE_SIZE_MAX);
        TileCache fileSystemTileCache = this.mapView.getFileSystemTileCache();

        fileSystemTileCache.setPersistent(persistent);
        fileSystemTileCache.setCapacity(capacity);
        // text size
        String textScaleDefault = getString(R.string.preferences_text_scale_default);
        this.mapView.setTextScale(Float.parseFloat(sharedPreferences.getString("mapTextScale", textScaleDefault)));
    }

    /**
     * checks if the preferences of the background renderer changed if so, the mapview is informed and is cleared and redrawed
     */
    public void checkIfMapViewNeedsBackgroundUpdate() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean thingsChanged = prefs.getBoolean(MapView.MAPSFORGE_BACKGROUND_FILEPATH_CHANGED, false);
        if (!thingsChanged)
            return;

        final BackgroundSourceType currentMapRendererType = this.mapView.getMapRendererType();

        String filePath = prefs.getString(MapView.MAPSFORGE_BACKGROUND_FILEPATH, null);
        final String defaultType = getApplicationContext().getPackageName().equals(
                "it.geosolutions.geocollect.android.app") ? "1" : "0";
        BackgroundSourceType type = BackgroundSourceType.values()[Integer.parseInt(prefs.getString(
                MapView.MAPSFORGE_BACKGROUND_RENDERER_TYPE, defaultType))];

        final Editor ed = prefs.edit();
        ed.putBoolean(MapView.MAPSFORGE_BACKGROUND_FILEPATH_CHANGED, false);
        ed.commit();

        File mapFile = new File(filePath);
        if (mapFile == null || !mapFile.exists()) {
            mapFile = MapFilesProvider.getBackgroundMapFile();
            filePath = mapFile.getPath();
            type = BackgroundSourceType.MAPSFORGE;
        }

        // 1. renderer changed
        if (type != currentMapRendererType) {

            MapRenderer mapRenderer = null;
            switch (type) {
            case MAPSFORGE:
                if (filePath == null) {
                    throw new IllegalArgumentException("no filepath selected to change to mapsforge renderer");
                }
                mapView.setMapFile(new File(filePath));
                mapRenderer = new DatabaseRenderer(mapView.getMapDatabase());
                // TODO it was MBTILES with no or dimmed mbtiles layer, add MBTiles layer ?

                MSMMap map = SpatialDbUtils.mapFromDb(false);
                Log.d(TAG, "Mapsforge maps includes " + map.layers.size() + " layers");

                addLayersOrdered(map.layers);

                break;
            case MBTILES:
                mapRenderer = new MbTilesDatabaseRenderer(getBaseContext(), filePath);

                MSMMap map2 = SpatialDbUtils.mapFromDb(true);

                layerManager.setLayers(map2.layers);

                break;
            default:
                break;
            }
            if (mDrawerToggle != null) {
                mDrawerToggle.syncState();
            }
            mapView.setRenderer(mapRenderer, true);
            mapView.clearAndRedrawMapView();
            MapFilesProvider.setBackgroundSourceType(type);

        } else if (filePath != null && !filePath.equals(mapView.getMapRenderer().getFileName())) {

            // 2.renderer is the same but file changed
            switch (type) {
            case MAPSFORGE:
                mapView.setMapFile(new File(filePath));
                break;
            case MBTILES:
                mapView.setRenderer(new MbTilesDatabaseRenderer(getBaseContext(), filePath), true);
                break;
            default:
                break;
            }
            mapView.clearAndRedrawMapView();
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            // mDrawerLayout.openDrawer(mDrawerList);
            // return true;

        }
        return super.onKeyUp(keyCode, event);
    }

    public void addLayersOrdered(final ArrayList<Layer> layers) {

        ArrayList<Layer> originalLayers = layers;
        ArrayList<Layer> orderedLayers = new ArrayList<Layer>();

        // check if there is a MBTiles layer which needs to be ordered
        boolean layersContainMBTilesLayer = false;
        for (Layer l : originalLayers) {
            if (l instanceof MbTilesLayer) {
                layersContainMBTilesLayer = true;
                break;
            }
        }
        // if there is, add this flag to wait until it has been added
        boolean mbTilesAdded = !layersContainMBTilesLayer;

        while (!originalLayers.isEmpty()) {

            final Layer layer = originalLayers.get(originalLayers.size() - 1); // get last

            if (layer instanceof MbTilesLayer) {

                final int currentValue = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getInt(
                        MBTilesLayerOpacitySettingActivity.MBTILES_OPACITY_ID, 192);
                layer.setOpacity(currentValue);
                orderedLayers.add(layer);
                mbTilesAdded = true;
                originalLayers.remove(layer);
                Log.d(TAG, "mbtiles layer added , size " + orderedLayers.size());
            } else if (mbTilesAdded == true) {
                orderedLayers.add(layer);
                originalLayers.remove(layer);
                Log.d(TAG, "other added , size " + orderedLayers.size());
            }

        }

        layerManager.setLayers(orderedLayers);
    }

    public View getLayerMenu() {
        return mLayerMenu;
    }

    public View getDrawerList() {
        return mDrawerList;
    }
    
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }
    
    public void selectItem(int position) {
        NavDrawerItem selectedItem = navConf.getNavItems()[position];
        
        this.onNavItemSelected(selectedItem.getId());
        mDrawerList.setItemChecked(position, true);
        
        if ( selectedItem.updateActionBarTitle()) {
            setTitle(selectedItem.getLabel());
        }
        
        if ( this.mDrawerLayout.isDrawerOpen(this.mDrawerList)) {
            mDrawerLayout.closeDrawer(mDrawerList);
        }
    }
    

    protected void onNavItemSelected(int id) {
        if(id != 900){
            Intent intent = new Intent();
            intent.putExtra(PendingMissionListActivity.KEY_MAP_RESULT, id);
            setResult(RESULT_OK, intent);
            finish();
        }else{
            startActivity(new Intent(this, CreditsActivity.class));
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ( keyCode == KeyEvent.KEYCODE_MENU ) {
            if ( this.mDrawerLayout.isDrawerOpen(this.mDrawerList)) {
                this.mDrawerLayout.closeDrawer(this.mDrawerList);
            }
            else {
                this.mDrawerLayout.openDrawer(this.mDrawerList);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    
    /**
     * Display a confirm prompt before logging out the user
     */
    public static void confirmLogout(final Activity act) {
        final Context ctx = act.getBaseContext();
        new AlertDialog.Builder(act).setTitle(R.string.action_logout)
                .setMessage(R.string.button_confirm_logout)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        //clear user data
                        final Editor ed = PreferenceManager.getDefaultSharedPreferences(ctx).edit();

                        ed.putString(LoginActivity.PREFS_USER_EMAIL, null);
                        ed.putString(LoginActivity.PREFS_USER_FORENAME, null);
                        ed.putString(LoginActivity.PREFS_USER_SURNAME, null);
                        ed.putString(LoginActivity.PREFS_PASSWORD, null);
                        ed.putString(LoginActivity.PREFS_AUTH_KEY, null);
                        ed.putString(LoginActivity.PREFS_USER_ENTE, null);

                        ed.commit();

                        Toast.makeText(ctx, ctx.getString(R.string.logout_logged_out),Toast.LENGTH_LONG).show();
                        
                        if(act != null){
                            act.startActivityForResult(
                                    new Intent(ctx, LoginActivity.class),
                                    LoginActivity.REQUEST_LOGIN);
                        }

                    }
                }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                }).show();
    }


}
