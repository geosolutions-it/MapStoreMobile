/*
 * GeoSolutions Android Map Library - Digital field mapping on Android based devices
 * Copyright (C) 2013  GeoSolutions (www.geo-solutions.it)
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
package it.geosolutions.android.map;

import it.geosolutions.android.map.activities.GetFeatureInfoLayerListActivity;
import it.geosolutions.android.map.activities.MapActivityBase;
import it.geosolutions.android.map.activities.about.InfoView;
import it.geosolutions.android.map.control.CoordinateControl;
import it.geosolutions.android.map.control.LocationControl;
import it.geosolutions.android.map.control.MapControl;
import it.geosolutions.android.map.control.MapInfoControl;
import it.geosolutions.android.map.control.MarkerControl;
import it.geosolutions.android.map.database.SpatialDataSourceManager;
import it.geosolutions.android.map.dialog.FilePickerDialog;
import it.geosolutions.android.map.dialog.FilePickerDialog.FilePickCallback;
import it.geosolutions.android.map.dto.MarkerDTO;
import it.geosolutions.android.map.fragment.GenericMenuFragment;
import it.geosolutions.android.map.fragment.sources.SourcesFragment;
import it.geosolutions.android.map.geostore.activities.GeoStoreResourceDetailActivity;
import it.geosolutions.android.map.geostore.activities.GeoStoreResourcesActivity;
import it.geosolutions.android.map.geostore.model.Resource;
import it.geosolutions.android.map.mapstore.model.MapStoreConfiguration;
import it.geosolutions.android.map.mapstore.utils.MapStoreUtils;
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
import it.geosolutions.android.map.utils.LocalPersistence;
import it.geosolutions.android.map.utils.MapFilesProvider;
import it.geosolutions.android.map.utils.MarkerUtils;
import it.geosolutions.android.map.utils.SpatialDbUtils;
import it.geosolutions.android.map.utils.StorageUtils;
import it.geosolutions.android.map.view.AdvancedMapView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.mapsforge.android.maps.DebugSettings;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.mapgenerator.MapRenderer;
import org.mapsforge.android.maps.mapgenerator.TileCache;
import org.mapsforge.android.maps.mapgenerator.databaserenderer.DatabaseRenderer;
import org.mapsforge.android.maps.mapgenerator.mbtiles.MbTilesDatabaseRenderer;
import org.mapsforge.core.model.GeoPoint;
import org.mapsforge.core.model.MapPosition;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

/**
 * This is an implementation of the custom view for the map component.
 * Allows to be started in 2 models:
 *  * MODE_VIEW : does not allow to change the markers position
 *  * MODE_EDIT : allow to change marker position and select a feature to use to replace current markers
 *  
 * 
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it) 
 * 
 * 
 */
public class MapsActivity extends MapActivityBase {
    
	
		// default path for files
        private static final File MAP_DIR = MapFilesProvider.getBaseDirectoryFile();
        private static final File MAP_FILE = MapFilesProvider.getBackgroundMapFile();
	
        //------------------------------------------------------
	// PARAMETERS FOR INTENT
        //------------------------------------------------------
        /**
         * Inner class for references of the managed Extras in the Intent
         * @author Lorenzo Natali (www.geo-solutions.it)
         *
         */
	public final static class PARAMETERS {
		public static final String FEATURE_ID_FIELD = "FEATURE_ID_FIELD";
		public static final String MARKERS = "MARKERS";
		public static final String RES_ID = "ID";
		public static final int MODE_VIEW =0;
		public static final int MODE_EDIT =1;
		public static final int MODE_SCREEN =2;
		public static final String LON = "LON";
		public static final String LAT = "LAT";
		public static final String ZOOM_LEVEL = "ZOOM_LEVEL";
		public static final String RESOURCE = "RESOURCE";
		public static final String GEOSTORE_URL = "GEOSTORE_URL";
		public static final String CONFIRM_ON_EXIT = "CONFIRM_ON_EXIT";
		public static final String CUSTOM_MAPINFO_CONTROL = "CustomMapInfoControlParcel";
	}

	
	//------------------------------------------------------
	// PREFERENCES
        //------------------------------------------------------
	/**
     * The default number of tiles in the file system cache.
     */
    public static final int FILE_SYSTEM_CACHE_SIZE_DEFAULT = 250;

    /**
     * The maximum number of tiles in the file system cache.
     */
    public static final int FILE_SYSTEM_CACHE_SIZE_MAX = 500;
    
    //------------------------------------------------------
    // SAVE INSTANCE STATE BUNDLE PARAMETERS
    //------------------------------------------------------
    private boolean dbLoaded;
    
    //------------------------------------------------------
    //PUBLIC VARIABLES
    //------------------------------------------------------
    public AdvancedMapView mapView;
    public OverlayManager overlayManager;
    //------------------------------------------------------
	// CONSTANTS
    //------------------------------------------------------
	public static final int MAPSTORE_REQUEST_CODE = 1;
	/** FEATURE_DEFAULT_ID */
	private static final String FEATURE_DEFAULT_ID = "OGC_FID";
	
	/** DB_LOADED_FLAG */
    private static final String DB_LOADED_FLAG = "dbLoaded";

    /** DATAPROPERTIES_REQUEST_CODE */
    public static final int DATAPROPERTIES_REQUEST_CODE = 671;
    
    /** ADD LAYERS REQUEST_CODE */
	public static final int LAYER_ADD = 98;
    
    private static final String  FEATUREIDFIELD_FLAG = "fidField";
    /** choosen featureID field */
	private String featureIdField;
	

	
	/** CANCONFRIM_FLAG */
	private static final String CANCONFRIM_FLAG = "canConfirm_flag";
	public static final String MAPSTORE_CONFIG = "MAPSTORE_CONFIG";
	public static final String LAYERS_TO_ADD = "LAYERS_TO_ADD";
	public static final String MSM_MAP = "MSM_MAP";
	private static boolean canConfirm;

	
	/**
	 * LAYOUT PARAMETERS 
	 */
    private DrawerLayout mDrawerLayout;
    private View mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
	private View mLayerMenu;
	private MultiSourceOverlayManager layerManager;

	private ActionMode currentActionMode;
	
	/**
	 * BACK BUTTON PARAMETER

    private static final long delay = 2000L;
    private boolean mRecentlyBackPressed = false;
    private Handler mExitHandler = new Handler();
    
    private Runnable mExitRunnable = new Runnable() {
        @Override
        public void run() {
            mRecentlyBackPressed=false;   
        }
    };
	 */
	
	/**
	 * Initialize Application and restores state
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		 // setup loading 
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        getSupportActionBar();
        setSupportProgressBarIndeterminateVisibility(false); 
		super.onCreate(savedInstanceState);
		
		//
		// LAYOUT INITIALIZATION 
		//
		setContentView(R.layout.main);
		//Setup the left menu (Drawer)
        
        //
		// MAP INIZIALIZATION 
		//
		//create overlay manager
		boolean mapLoaded = initMap(savedInstanceState);
		layerManager =  new MultiSourceOverlayManager(mapView);
		overlayManager=layerManager;
		//setup slide menu(es)
		setupDrawerLayout();
		dbLoaded = initDb();
		//if something went wrong durind db and map initialization,
		// we should stop
		if (!mapLoaded && !dbLoaded) {
		        //TODO: notify the user the problem
			Toast.makeText(this, "DB not loaded", Toast.LENGTH_LONG).show();//TODO i18n
		}
		// 
		// LEFT MENU INITIALIZATION
		//
		setupLeftMenu(savedInstanceState, layerManager);
		
		// CONTEXT MENU 
		this.registerForContextMenu(mapView);
		mapView.getMapScaleBar().setShowMapScaleBar(true);// TODO preferences;
		
		overlayManager.setMarkerOverlay(new MarkerOverlay());
		createMarkers(savedInstanceState);
		
		String action = getIntent().getAction();
		if(Intent.ACTION_VIEW.equals(action)){
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
	}

	/**
	 * load a map from bundle
	 */
	private void loadFromBundle() {
		Bundle data = getIntent().getExtras();
		if(data == null) return;
		Resource resource = (Resource) data.getSerializable(PARAMETERS.RESOURCE);
		if(resource!=null){
			String geoStoreUrl = data.getString(PARAMETERS.GEOSTORE_URL);
			loadGeoStoreResource(resource, geoStoreUrl);
		}

		if(data.containsKey(MSM_MAP)){
	        layerManager.loadMap((MSMMap)data.getSerializable(MSM_MAP));

		}
		
		ArrayList<Layer> layersToAdd = (ArrayList<Layer>) data.getSerializable(LAYERS_TO_ADD);
		if(layersToAdd != null){
			addLayers(layersToAdd);
		}
		
	}

	/**
	 * Creates/Restore the layer switcher or restore the old one and add
	 * all other menu 
	 * @param savedInstanceState
	 * @param layerManager
	 */
	private void setupLeftMenu(Bundle savedInstanceState,
			MultiSourceOverlayManager layerManager) {
		//work on fragment management
		FragmentManager fManager = getSupportFragmentManager();
		LayerSwitcherFragment osf;
		if(savedInstanceState !=null){	
			osf=  (LayerSwitcherFragment)fManager.findFragmentById(R.id.left_drawer_container);
			if(osf == null){
				Log.e("MAPSACTIVITY", "unable to restore layer switcher");
			}
			layerManager.setLayerChangeListener(osf);
			layerManager.restoreInstanceState(savedInstanceState);
			
		}else{
			layerManager.defaultInit();
			@SuppressWarnings("unchecked")
			ArrayList<Layer> layers =  (ArrayList<Layer>) LocalPersistence.readObjectFromFile(this, LocalPersistence.CURRENT_MAP);
			if(layers != null){
				layerManager.setLayers(layers);
			}else{
				MSMMap map = SpatialDbUtils.mapFromDb(true);
				StorageUtils.setupSources(this);
		        layerManager.loadMap(map);
			}
			//setup left drawer fragments
	        osf =  new LayerSwitcherFragment();
	        layerManager.setLayerChangeListener(osf);
			FragmentTransaction fragmentTransaction = fManager.beginTransaction();
			fragmentTransaction.add(R.id.left_drawer_container,osf);
			GenericMenuFragment other = new GenericMenuFragment();
			//fragmentTransaction.add(R.id.right_drawer, other);
			SourcesFragment sf = new SourcesFragment();
			fragmentTransaction.add(R.id.right_drawer, sf);
			fragmentTransaction.commit();
		}
	}
	
	/**
	 * Setup the Drawer as a left menu and layer's menu as the right one
	 */
	private void setupDrawerLayout() {
		
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (View) findViewById(R.id.left_drawer); 
        mLayerMenu = (View) findViewById(R.id.right_drawer);
        
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret  */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
                ) {

            private CharSequence mTitle=getTitle();

			/** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                //getSupportActionBar().setTitle(mTitle);
                supportInvalidateOptionsMenu();
                if (currentActionMode != null){
                	currentActionMode.finish();
                }
                
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
            	mTitle = getSupportActionBar().getTitle();
            	getSupportActionBar().setTitle(R.string.drawer_title);
            	supportInvalidateOptionsMenu();
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        
        //layerList
        
	}
	/**
	 * Resume the state of:
	 * 
	 * * tile cache
	 * * Controls
	 */
	@Override
	protected void onResume() {
	    super.onResume();
	    loadPersistencePreferences();
	    checkIfMapViewNeedsBackgroundUpdate();
	    //Refresh control beacuse any changes can be changed
	    for(MapControl mic : mapView.getControls()){
	    	mic.refreshControl(GetFeatureInfoLayerListActivity.BBOX_REQUEST, GetFeatureInfoLayerListActivity.BBOX_REQUEST, null);	    
	    }
	   
	    // Some debug
	    Intent i = getIntent();
	    if(i!=null){
	    	String a = i.getAction();
	    	Log.v("MapsActivity onResume", "Action:"+a);
	    }
	}
	
	/**
	 * add the confirm button to the control bar
	 */
	private void addConfirmButton() {
		Log.v("MapsActivity", "adding confirm button");
		ImageButton b = (ImageButton)findViewById(R.id.button_confirm_marker_position);
		b.setVisibility(View.VISIBLE);
		final MapsActivity activity = this;
		b.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

				if(!canConfirm){
			        Toast.makeText(activity, R.string.error_unable_getfeature_db, Toast.LENGTH_LONG).show();
			        return;
				}
				
				new AlertDialog.Builder(activity)
			    .setTitle(R.string.button_confirm_marker_position_title)
			    .setMessage(R.string.button_confirm_marker_position)
			    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) { 
			        	 Intent returnIntent = new Intent();
			        	 //get current markers
			        	 ArrayList<DescribedMarker> markers = overlayManager.getMarkerOverlay().getMarkers();
			        	 //serialize markers in the response
			        	 returnIntent.putParcelableArrayListExtra(MapsActivity.PARAMETERS.MARKERS,MarkerUtils.getMarkersDTO(markers));
			        	 setResult(RESULT_OK,returnIntent);
			        	 finish();
			        	 return;
			        	//if you don't want to return data:
//			        	setResult(RESULT_CANCELED, returnIntent);        
//			        	finish();
//			        	activity.finish();
			        }

					
			     })
			    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) { 
			            // do nothing
			        }
			     })
			     .show();
				
			}
		});
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);

		savedInstanceState.putBoolean(DB_LOADED_FLAG, dbLoaded);
		
		savedInstanceState.putString(FEATUREIDFIELD_FLAG, featureIdField);
		
		savedInstanceState.putBoolean(CANCONFRIM_FLAG, canConfirm);
		//MARKERS
		//get current markers
        overlayManager.saveInstanceState(savedInstanceState);
        for(MapControl mc : mapView.getControls()){
		    mc.saveState(savedInstanceState);
		}
	}

	/**
	 * Save the layer state 
	 */
	@Override
	public void onPause() {
		super.onPause();
		LocalPersistence.writeObjectToFile(this, layerManager.getLayers() , LocalPersistence.CURRENT_MAP);
		
	}
	
	/**
	 * Force a double tap to close the app

    @Override
    public void onBackPressed() {

        //You may also add condition if (doubleBackToExitPressedOnce || fragmentManager.getBackStackEntryCount() != 0) // in case of Fragment-based add
        if (mRecentlyBackPressed) {
            mExitHandler.removeCallbacks(mExitRunnable);
            mExitHandler = null;
            super.onBackPressed();
        }
        else
        {
            mRecentlyBackPressed = true;
            Toast.makeText(this, "press again to exit", Toast.LENGTH_SHORT).show();
            mExitHandler.postDelayed(mExitRunnable, delay);
        }
    }

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
			boolean confirmOnExit = getIntent().getExtras().getBoolean(MapsActivity.PARAMETERS.CONFIRM_ON_EXIT ,true);
			if(confirmOnExit){
			new AlertDialog.Builder(this)
		    .setTitle(R.string.button_confirm_exit_title)
		    .setMessage(R.string.button_confirm_exit)
		    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		        	finish();
		        	 return;
		        }
		     })
		    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		            // do nothing
		        }
		     })
		     .show();
	
			}else{
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

		//Restore state of the controls?
		

	}

	/**
	 * creates the menu of the map
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.map, (Menu) menu);

	}


	

	/**
	 * Enable and disable Overlays adding / removing from the map
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		//int itemId = item.getItemId();
		
		//Drawer part
        if (item.getItemId() == android.R.id.home) {

            if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
            	mDrawerLayout.closeDrawer(mDrawerList);
            } else {
            	mDrawerLayout.openDrawer(mDrawerList);
            	if(mLayerMenu!=null){
            		mDrawerLayout.closeDrawer(mLayerMenu);
            	}
            }
        //layer menu part
		}else if(item.getItemId() == R.id.layer_menu_action){
			if (mDrawerLayout.isDrawerOpen(mLayerMenu)) {
            	mDrawerLayout.closeDrawer(mLayerMenu);
            } else {
            	if(mLayerMenu!=null){
            		mDrawerLayout.openDrawer(mLayerMenu);
            	}
            	mDrawerLayout.closeDrawer(mDrawerList);
            }
		}else if(item.getItemId() == R.id.settings){
			Intent pref = new Intent(this,EditPreferences.class);
			 startActivity(pref);
		}else if(item.getItemId() == R.id.infoview){
			Intent info = new Intent(this,InfoView.class);
			 startActivity(info);
		}else if(item.getItemId() == R.id.exitview){
			confirmExit();
		}
        return super.onOptionsItemSelected(item);
		 
	}

	/**
	 * Create the markers and add them to the MarkerOverlay
	 * Gets it from the Intent or from the savedInstanceState
	 * Assign them the proper <GeoPoint> if missing
	 * @param savedInstanceState
	 */
	private void createMarkers(Bundle savedInstanceState) {
		 List<MarkerDTO> markerDTOs=null;
		// add the OverlayItem to the ArrayItemizedOverlay
		 ArrayList<DescribedMarker> markers= null;
		if (savedInstanceState != null) {
			markerDTOs = savedInstanceState.getParcelableArrayList(MapsActivity.PARAMETERS.MARKERS);
			markers= MarkerUtils.markerDTO2DescribedMarkers(this,markerDTOs);
		}else{
		    markerDTOs = getIntent().getParcelableArrayListExtra(MapsActivity.PARAMETERS.MARKERS);
		    markers= MarkerUtils.markerDTO2DescribedMarkers(this,markerDTOs);
		    //retrieve geopoint if missing
		    if(getIntent().getExtras() == null){
		    	return;
		    }
		    featureIdField = getIntent().getExtras().getString(PARAMETERS.FEATURE_ID_FIELD);
		    if(featureIdField==null){
		        featureIdField = FEATURE_DEFAULT_ID;
		    }
		    if(!MarkerUtils.assignFeaturesFromDb(markers,featureIdField)){
		        Toast.makeText(this, R.string.error_unable_getfeature_db, Toast.LENGTH_LONG).show();
		        canConfirm = false;
		        //TODO dialog : download features for this area?
		    }
		}
		// create an ItemizedOverlay with the default marker
		overlayManager.getMarkerOverlay().getOverlayItems().addAll(markers);
	}

	
	
	
	// TODO move this initialization in a better place (config stuff)
	/**
	 * Initializes the database 
	 * @return true if the initialization was successful
	 */
	private boolean initDb() {
		// init styleManager
		StyleManager.getInstance().init(this, MAP_DIR);
		// init Db
		SpatialDataSourceManager dbManager = SpatialDataSourceManager.getInstance();

		try {
			//Only if not already loaded some tables
			if (dbManager.getSpatialVectorTables(false).size() <= 0) {
				dbManager.init(this, MAP_DIR);
			} 
		} catch (Exception e) {
			
			return false;
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.map, menu);
		

		return true;
	}

	/**
	 * Initialize the map with Controls and background
	 * @param savedInstanceState 
	 * 
	 * @return
	 */
	private boolean initMap(Bundle savedInstanceState) {
		//setContentView(R.layout.activity_map);
		Log.v("MAP","Map Activated");
		this.mapView =  (AdvancedMapView) findViewById(R.id.advancedMapView);
		// TODO configurable controls
		mapView.setClickable(true);
		mapView.setBuiltInZoomControls(true);
		
//		mapView.setDebugSettings(new DebugSettings(true, true, false));

		// TODO parametrize these zoom levels
		mapView.getMapZoomControls().setZoomLevelMax((byte) 24);
		mapView.getMapZoomControls().setZoomLevelMin((byte) 1);

		// TODO d get this path on initialization

    	final String filePath = PreferenceManager.getDefaultSharedPreferences(this).getString("mapsforge_background_filepath", null);
    	final int type = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("mapsforge_background_type", "0"));
    	
    	//if the map file was edited in the preferences
		if(filePath != null && type == 0){
			//use it
			mapView.setMapFile(new File(filePath));
			
		}else if (MAP_FILE!=null) {
			
			Log.i("MAP","setting background file");
			mapView.setMapFile(MAP_FILE);
			loadPersistencePreferences();
			
		} else {
			Log.i("MAP","unable to set background file");
			//return false;
		}
		
		
		return true;
	}
	/**
	 * Add controls to the mapView and to the Buttons
	 * @param savedInstanceState 
	 */
	private void addControls(Bundle savedInstanceState) {
		 String action =getIntent().getAction();
		 Log.v("MapsActivity", "action: "+action);
		 
		 //Coordinate Control
		 mapView.addControl(new CoordinateControl(mapView, true));
		 List<MapControl> group = new ArrayList<MapControl>();
		 
		 
		 // Info Control
		 MapInfoControl ic;
		 if(getIntent().hasExtra(MapsActivity.PARAMETERS.CUSTOM_MAPINFO_CONTROL)){
			 ic = (MapInfoControl) getIntent().getParcelableExtra(MapsActivity.PARAMETERS.CUSTOM_MAPINFO_CONTROL) ;
			 ic.activity = this;
			 ic.mapView = mapView;
			 ic.instantiateListener();
		 }else{
			 ic= new MapInfoControl(mapView,this);
		 }
		 ic.setActivationButton( (ImageButton)findViewById(R.id.ButtonInfo) );
		 
		 mapView.addControl(ic);
		 
		 if(!Intent.ACTION_VIEW.equals(action)){
			 Log.v("MapsActivity", "Adding MarkerControl");

			 //Marker Control 
			 MarkerControl mc  =new MarkerControl(mapView);
			 // activation button
			 ImageButton mcbmb = (ImageButton)findViewById(R.id.ButtonMarker);
			 mcbmb.setVisibility(View.VISIBLE);
			 mc.setActivationButton(mcbmb);
			 // info button  // TODO: do we need this button?
			 ImageButton mcib = (ImageButton)findViewById(R.id.marker_info_button);
			 mcib.setVisibility(View.VISIBLE);
			 mc.setInfoButton(mcib);
			 
			 mapView.addControl(mc);
			 group.add(mc);
			 mc.setGroup(group);
	         mc.setMode(MarkerControl.MODE_EDIT);
		 }         
		 
		 //My location Control 
		 LocationControl lc  =new LocationControl(mapView);
		 lc.setActivationButton((ImageButton)findViewById(R.id.ButtonLocation));
		 mapView.addControl(lc);
		 
		 //create and add group 
		 group.add(ic);

		 ic.setGroup(group);

		 //TODO move this in a control

		 //Set modes for controls
         if(Intent.ACTION_VIEW.equals(action)){
             ic.setMode(MapInfoControl.MODE_VIEW);
         }else if(Intent.ACTION_EDIT.equals(action)){
             ic.setMode(MapInfoControl.MODE_EDIT);
         //Default edit mode
         }else{
             ic.setMode(MapInfoControl.MODE_EDIT);
         }
         if(savedInstanceState!=null){
    	         for(MapControl c:mapView.getControls()){
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
   	 	if(intent.hasExtra(PARAMETERS.LAT) && intent.hasExtra(PARAMETERS.LON) && intent.hasExtra(PARAMETERS.ZOOM_LEVEL)){
       	 	 double lat = intent.getDoubleExtra(PARAMETERS.LAT, 43.68411);
        	 double lon = intent.getDoubleExtra(PARAMETERS.LON, 10.84899);
        	 byte zoom_level = intent.getByteExtra(PARAMETERS.ZOOM_LEVEL, (byte) 13);
        	 /*ArrayList<MarkerDTO> list_marker = intent.getParcelableArrayListExtra(PARAMETERS.MARKERS);
        	 MarkerDTO mark = list_marker.get(0);*/
        	 mp = new MapPosition(new GeoPoint(lat,lon),zoom_level);
        	 mapView.getMapViewPosition().setMapPosition(mp);
   	 	}
   	 	else{
   	 	 if(mo!=null){   	       	 
    	 	//support only one marker
         	MapPosition newMp = MarkerUtils.getMarkerCenterZoom(mo.getMarkers(),mp);
             if(newMp!=null)
                 mapView.getMapViewPosition().setMapPosition(newMp);
   	 	 	}	       
        }
	}

	/**
	 * Center the map to a point and zoomLevel
	 * @param pp
	 * @param zoomlevel
	 */
	public void setPosition(GeoPoint pp, byte zoomlevel ){
		
		mapView.getMapViewPosition().setMapPosition(new MapPosition(pp,zoomlevel));
	}
	
	
	/**
	 * Opena the Data List activity
	 * @param item
	 * @return
	 */
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == GetFeatureInfoLayerListActivity.BBOX_REQUEST && resultCode == RESULT_OK){
			//the response can contain a feature to use to replace the current marker 
			//on the map
			manageMarkerSubstitutionAction(data);
		}
		
		//controls can be refreshed getting the result of an intent, in this case
		// each control knows which intent he sent with their requestCode/resultCode
		for(MapControl control : mapView.getControls()){
			control.refreshControl(requestCode,resultCode, data);
		}
		// reload stores in the panel (we do it everyTime, maybe there is a better way
		SourcesFragment sf = (SourcesFragment) getSupportFragmentManager().findFragmentById(R.id.right_drawer);
		if(sf!=null){
			sf.reloadStores();
		}
		//manager mapstore configuration load 
		//TODO: with the new interface this will load a map instead of the mapstoreconfig
		if(data==null)return;
		Bundle b = data.getExtras();
		if(requestCode==DATAPROPERTIES_REQUEST_CODE){
			mapView.getOverlayController().redrawOverlays();
			// close right drawer
            if (mDrawerLayout.isDrawerOpen(mLayerMenu)) {
            	mDrawerLayout.closeDrawer(mLayerMenu);
            }
		}
		Resource resource = (Resource) data.getSerializableExtra(GeoStoreResourceDetailActivity.PARAMS.RESOURCE);
		if(resource!=null){
			String geoStoreUrl = data.getStringExtra(GeoStoreResourcesActivity.PARAMS.GEOSTORE_URL);
			loadGeoStoreResource(resource, geoStoreUrl);
		}
		if(b.containsKey(MAPSTORE_CONFIG)){
        	overlayManager.loadMapStoreConfig((MapStoreConfiguration)b.getSerializable(MAPSTORE_CONFIG));
        }
		if(b.containsKey(MSM_MAP)){
	        layerManager.loadMap((MSMMap)b.getSerializable(MSM_MAP));

		}
		ArrayList<Layer> layersToAdd = (ArrayList<Layer>) b.getSerializable(LAYERS_TO_ADD);
		if(layersToAdd != null){
			addLayers(layersToAdd);
		}
		
	}

	/**
	 * Add layers to the map
	 * @param layersToAdd
	 */
	private void addLayers(ArrayList<Layer> layersToAdd) {
		ArrayList<Layer> layers =new ArrayList<Layer> (layerManager.getLayers());
		layers.addAll(layersToAdd);
		layerManager.setLayers(layers);
		// close right drawer
		if (mDrawerLayout.isDrawerOpen(mLayerMenu)) {
			mDrawerLayout.closeDrawer(mLayerMenu);
		}
	}

	/**
	 * Load a geostore resource on the map
	 * @param resource the resource id
	 * @param geoStoreUrl
	 */
	private void loadGeoStoreResource(Resource resource, String geoStoreUrl) {
		MapStoreUtils.loadMapStoreConfig(geoStoreUrl, resource, this);
		// close right drawer
		if (mDrawerLayout.isDrawerOpen(mLayerMenu)) {
			mDrawerLayout.closeDrawer(mLayerMenu);
		}
	}

	/**
	 * Manages the marker substitution
	 */
	private void manageMarkerSubstitutionAction(Intent data) {
		
		@SuppressWarnings("unchecked")
		ArrayList<Attribute> arrayList = (ArrayList<Attribute>) data.getExtras().getSerializable(GetFeatureInfoLayerListActivity.RESULT_FEATURE_EXTRA);
		Feature f = new Feature(arrayList);
		String layer = data.getExtras().getString(GetFeatureInfoLayerListActivity.LAYER_FEATURE_EXTRA);
		//TODO parametrize id column name
		
		Attribute a = f.getAttribute(featureIdField);
		
		String attributeValue=null;
		if(a!=null){
			attributeValue = a.getValue();
		}
		replaceMarker(layer, featureIdField, attributeValue,f);
		
	}

    /**
     * Replace the default marker with position and properties from the arguments
     * @param layer
     * @param attributeName
     * @param attributeValue
     * @param f 
     */
    private void replaceMarker(String layer, String attributeName, String attributeValue, Feature f) {
        DescribedMarker marker = getDefaultMarker();
		
		if(marker != null){
			setMarkerProperties(layer, attributeName, attributeValue, attributeValue, marker,f);
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
    private void setMarkerProperties(String layer, String attributeName,
            String id, String attributeValue, DescribedMarker marker, Feature f) {
        GeoPoint p  = SpatialDbUtils.getGeopointFromLayer(layer, attributeName, attributeValue);
        //get Only the first
        if(p!=null){
                //TODO ask if you want to change
                //if yes move and center map
        	marker.setGeoPoint(p);
        	marker.setFeatureId(id);
        	marker.setFeature(f);
        	mapView.redraw();
        	canConfirm = true;
        }else{
        	Toast.makeText(this, R.string.error_getting_data_from_database, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * get a marker from markerOverlay.
     * The one highlighted or the first one
     * @return
     */
    private DescribedMarker getDefaultMarker() {
        MarkerOverlay m = mapView.getMarkerOverlay();
            //add the marker overlay if not present
            if(m==null){
                overlayManager.toggleOverlayVisibility(R.id.markers, true);
                m =  mapView.getMarkerOverlay();
            }

            DescribedMarker marker = m.getHighlighted();
            if (marker == null) {
                List<DescribedMarker> markers = m.getMarkers();
                if (markers.size() > 0) {
                    marker = markers.get(0);
                }else{
                    //TODO add a new marker
                }
            }
        return marker;
    }
    
    @Override
    public void onPostCreate(Bundle savedInstanceState){
    	super.onPostCreate(savedInstanceState);
    	// Sync the toggle state after onRestoreInstanceState has occurred.
    	TileCache fileSystemTileCache = this.mapView.getFileSystemTileCache();
    	
    	Log.v("PERSISTENCE","capacity"+fileSystemTileCache.getCapacity()+",persistence:"+fileSystemTileCache.isPersistent());
        mDrawerToggle.syncState();
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
        // Checks the orientation of the screen for landscape and portrait and set portrait mode always
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
           
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
           
        }
    }

    /**
     * Load tile caching preferences
     * used sharedPreferences :
     * * TileCachePersistence
     * * TileCacheSize
     */
    public void loadPersistencePreferences(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean persistent = sharedPreferences.getBoolean("TileCachePersistence", true);
        Log.v("PERSISTENCE","cache size:"+sharedPreferences.getInt("TileCacheSize", FILE_SYSTEM_CACHE_SIZE_DEFAULT)+",persistent"+persistent);
        int capacity = Math.min(sharedPreferences.getInt("TileCacheSize", FILE_SYSTEM_CACHE_SIZE_DEFAULT),
                        FILE_SYSTEM_CACHE_SIZE_MAX);
        TileCache fileSystemTileCache = this.mapView.getFileSystemTileCache();
        
        fileSystemTileCache.setPersistent(persistent);
        fileSystemTileCache.setCapacity(capacity);
        // text size
        String textScaleDefault = getString(R.string.preferences_text_scale_default);
        this.mapView.setTextScale(Float.parseFloat(sharedPreferences.getString("mapTextScale", textScaleDefault)));
    }
	/**
	 * checks if the preferences of the background renderer changed
	 * if so, the mapview is informed and is cleared and redrawed
	 */
    public void  checkIfMapViewNeedsBackgroundUpdate()
    {
    	final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	final boolean thingsChanged = prefs.getBoolean("mapsforge_background_file_changed", false);
    	if(!thingsChanged)return;
    	
    	final int currentMapRendererType = this.mapView.getMapRendererType();
    	final String fileName = prefs.getString("mapsforge_background_file", null);
    	final String filePath = prefs.getString("mapsforge_background_filepath", null);
    	final int type = Integer.parseInt(prefs.getString("mapsforge_background_type", "0"));
    	final Editor ed = prefs.edit();
    	ed.putBoolean("mapsforge_background_file_changed", false);
    	ed.commit();
    	
    	//1. renderer changed
    	if(type != currentMapRendererType){

    		MapRenderer mapRenderer = null;
    		switch (type) {
    		case 0:
    			if(filePath == null){
    				throw new IllegalArgumentException("no filepath selected to change to mapsforge renderer");
    			}
    			mapView.setMapFile(new File(filePath));
    			mapRenderer = new DatabaseRenderer(mapView.getMapDatabase());
    			break;
    		case 1:
    			mapRenderer = new MbTilesDatabaseRenderer(getBaseContext(), fileName);
    			break;
    		case 2:
    			// TODO
    			break;
    		default:
    			break;
    		}
    		mapView.setRenderer(mapRenderer, true);
    		mapView.clearAndRedrawMapView();

    	}else if(fileName != null && !fileName.equals(mapView.getMapRenderer().getFileName())){

    		//2.renderer is the same but file changed
    		switch (type) {
    		case 0:
    			if(filePath == null){
    				throw new IllegalArgumentException("no filepath selected to change to mapsforge renderer");
    			}
    			mapView.setMapFile(new File(filePath));
    			break;
    		case 1:
    			mapView.setRenderer(new MbTilesDatabaseRenderer(getBaseContext(), fileName), true);
    			break;
    		case 2:
    			// TODO
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
//	        mDrawerLayout.openDrawer(mDrawerList);
//	        return true;
	    	
	    }
	    return super.onKeyUp(keyCode, event);
	}
	
	/**
	 * Get the current action mode if present
	 */
	 @Override
    public void onActionModeStarted(ActionMode mode) {
		 currentActionMode =mode;
	 }

    @Override
    public void onActionModeFinished(ActionMode mode) {
    	currentActionMode = null;
    }

}