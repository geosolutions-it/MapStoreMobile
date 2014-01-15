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
import it.geosolutions.android.map.control.CoordinateControl;
import it.geosolutions.android.map.control.LocationControl;
import it.geosolutions.android.map.control.MapControl;
import it.geosolutions.android.map.control.MapInfoControl;
import it.geosolutions.android.map.control.MarkerControl;
import it.geosolutions.android.map.database.SpatialDataSourceManager;
import it.geosolutions.android.map.dto.MarkerDTO;
import it.geosolutions.android.map.fragment.GenericMenuFragment;
import it.geosolutions.android.map.fragment.OverlaySwitcherFragment;
import it.geosolutions.android.map.geostore.activities.GeoStoreResourceDetailActivity;
import it.geosolutions.android.map.geostore.activities.GeoStoreResourcesActivity;
import it.geosolutions.android.map.geostore.model.Resource;
import it.geosolutions.android.map.listeners.OneTapListener;
import it.geosolutions.android.map.mapstore.model.MapStoreConfiguration;
import it.geosolutions.android.map.mapstore.utils.MapStoreUtils;
import it.geosolutions.android.map.model.Attribute;
import it.geosolutions.android.map.model.Feature;
import it.geosolutions.android.map.overlay.MarkerOverlay;
import it.geosolutions.android.map.overlay.items.DescribedMarker;
import it.geosolutions.android.map.style.StyleManager;
import it.geosolutions.android.map.utils.MapFilesProvider;
import it.geosolutions.android.map.utils.MarkerUtils;
import it.geosolutions.android.map.utils.OverlayManager;
import it.geosolutions.android.map.utils.SpatialDbUtils;
import it.geosolutions.android.map.view.AdvancedMapView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.mapsforge.android.maps.mapgenerator.TileCache;
import org.mapsforge.core.model.GeoPoint;
import org.mapsforge.core.model.MapPosition;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
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

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

/**
 * This is an implementation of the custom view for the map component
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
    
    private static final String  FEATUREIDFIELD_FLAG = "fidField";
    /** choosen featureID field */
	private String featureIdField;
	

	
	/** CANCONFRIM_FLAG */
	private static final String CANCONFRIM_FLAG = "canConfirm_flag";
	public static final String MAPSTORE_CONFIG = "MAPSTORE_CONFIG";
	private static boolean canConfirm;

	
	/**
	 * LAYOUT PARAMETERS 
	 */
    private DrawerLayout mDrawerLayout;
    private View mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
	private View mLayerMenu;

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
		overlayManager= new OverlayManager(mapView);
		//setup slide menu(es)
		setupDrawerLayout();
		dbLoaded = initDb();
		//if something went wrong durind db and map initialization,
		// we should stop
		if (!mapLoaded && !dbLoaded) {
		        //TODO: notify the user the problem
			this.finish();
		}
		
		overlayManager.setMarkerOverlay(new MarkerOverlay());
		if(savedInstanceState !=null){	
			   overlayManager.restoreInstanceState(savedInstanceState);
		}else{
			overlayManager.setMarkerVisible();
			overlayManager.setDataVisible();
			//setup left drawer fragments
			FragmentManager fManager = getSupportFragmentManager();
	        OverlaySwitcherFragment osf = new OverlaySwitcherFragment();
	        overlayManager.setOverlayChangeListener(osf);
			FragmentTransaction fragmentTransaction = fManager.beginTransaction();
			fragmentTransaction.add(R.id.left_drawer_container,osf);
			GenericMenuFragment other = new GenericMenuFragment();
			fragmentTransaction.add(R.id.left_drawer_container, other);
			fragmentTransaction.commit();
		}
		this.registerForContextMenu(mapView);
		mapView.getMapScaleBar().setShowMapScaleBar(true);// TODO preferences;
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
		
		centerMapFile();
		
        

	}
	
	/**
	 * Setup the Drawer as a left menu and layer's menu as the right one
	 */
	private void setupDrawerLayout() {
		
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (View) findViewById(R.id.left_drawer);
        //remove comment the following line
        //and remove comment to right_drawer in main.xml (check also the comment about map.xml)
        //to enable also a right drawer
        //mLayerMenu = (View) findViewById(R.id.right_drawer);
        
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
                getSupportActionBar().setTitle(mTitle);
                supportInvalidateOptionsMenu();
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
	 * * markers position (TODO)
	 */
	@Override
	protected void onResume() {
	    super.onResume();
	    loadPersistencePreferences();
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
	 * creates the list of overlays as a checkbox list and set the items checked
         * or not
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

		int itemId = item.getItemId();
		// Toggle selection
		if (item.isChecked()) {
			item.setChecked(false);
		} else {
			item.setChecked(true);
		}
		if (itemId == R.id.data || itemId == R.id.markers) { //TODO move this operation in another place
			overlayManager.toggleOverlayVisibility(itemId, item.isChecked());
			return true;
	        
		//Drawer part
        }else  if (item.getItemId() == android.R.id.home) {

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
            	mDrawerLayout.closeDrawer(mDrawerList);
            } else {
            	if(mLayerMenu!=null){
            		mDrawerLayout.openDrawer(mLayerMenu);
            	}
            	mDrawerLayout.closeDrawer(mDrawerList);
            }
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
		    //retieve geopoint if missing
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
		//TODO uncomment this if an element is present already
		
		// add the ArrayItemizedOverlay to the MapView
		

	}

	
	
	
	// TODO move this initialization in a better place (config stuff)
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
		addControls(savedInstanceState);
		mapView.setClickable(true);
		mapView.setBuiltInZoomControls(true);

		// TODO parametrize these zoom levels
		mapView.getMapZoomControls().setZoomLevelMax((byte) 24);
		mapView.getMapZoomControls().setZoomLevelMin((byte) 1);

		// TODO d get this path on initialization
		
		if (MAP_FILE!=null) {
			Log.i("MAP","setting background file");
			mapView.setMapFile(MAP_FILE);
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
		 MapInfoControl ic= new MapInfoControl(mapView,this);
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
        	 new MapPosition(new GeoPoint(lat,lon),zoom_level);
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
			manageMarkerSubstitutionAction(data);
		}
		for(MapControl control : mapView.getControls()){
			control.refreshControl(requestCode,resultCode, data);
		}
		if(data==null)return;
		Bundle b = data.getExtras();
		if(requestCode==DATAPROPERTIES_REQUEST_CODE){
			mapView.getOverlayController().redrawOverlays();
		}else if(requestCode==MAPSTORE_REQUEST_CODE){
			Resource resource = (Resource) data.getSerializableExtra(GeoStoreResourceDetailActivity.PARAMS.RESOURCE);
			if(resource!=null){
				String geoStoreUrl = data.getStringExtra(GeoStoreResourcesActivity.PARAMS.GEOSTORE_URL);
				MapStoreUtils.loadMapStoreConfig(geoStoreUrl, resource, this);
			}
			if(b.containsKey(MAPSTORE_CONFIG)){
	        	overlayManager.loadMapStoreConfig((MapStoreConfiguration)b.getSerializable(MAPSTORE_CONFIG));
	        }
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
     * Save tile caching preferences
     * used sharedPreferences :
     * * TileCachePersistence
     * * TileCacheSize
     */
    public void loadPersistencePreferences(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean persistent = sharedPreferences.getBoolean("TileCachePersistence", true);
        int capacity = Math.min(sharedPreferences.getInt("TileCacheSize", FILE_SYSTEM_CACHE_SIZE_DEFAULT),
                        FILE_SYSTEM_CACHE_SIZE_MAX);
        TileCache fileSystemTileCache = this.mapView.getFileSystemTileCache();
        fileSystemTileCache.setPersistent(persistent);
        fileSystemTileCache.setCapacity(capacity);
        // text size
        String textScaleDefault = getString(R.string.preferences_text_scale_default);
        this.mapView.setTextScale(Float.parseFloat(sharedPreferences.getString("mapTextScale", textScaleDefault)));
    }
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_MENU) {
	        mDrawerLayout.openDrawer(mDrawerList);
	        return true;
	    }
	    return super.onKeyUp(keyCode, event);
	}
	
}