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
import it.geosolutions.android.map.control.CoordinateControl;
import it.geosolutions.android.map.control.LocationControl;
import it.geosolutions.android.map.control.MapControl;
import it.geosolutions.android.map.control.MapInfoControl;
import it.geosolutions.android.map.control.MarkerControl;
import it.geosolutions.android.map.database.SpatialDataSourceManager;
import it.geosolutions.android.map.dto.MarkerDTO;
import it.geosolutions.android.map.geostore.activities.GeoStoreResourceDetailActivity;
import it.geosolutions.android.map.geostore.activities.GeoStoreResourcesActivity;
import it.geosolutions.android.map.geostore.model.Resource;
import it.geosolutions.android.map.mapstore.model.MapStoreConfiguration;
import it.geosolutions.android.map.mapstore.utils.MapStoreUtils;
import it.geosolutions.android.map.model.Attribute;
import it.geosolutions.android.map.model.Feature;
import it.geosolutions.android.map.overlay.MarkerOverlay;
import it.geosolutions.android.map.overlay.SpatialiteOverlay;
import it.geosolutions.android.map.overlay.WMSOverlay;
import it.geosolutions.android.map.overlay.items.DescribedMarker;
import it.geosolutions.android.map.preferences.EditPreferences;
import it.geosolutions.android.map.style.StyleManager;
import it.geosolutions.android.map.utils.MapFilesProvider;
import it.geosolutions.android.map.utils.MarkerUtils;
import it.geosolutions.android.map.utils.SpatialDbUtils;
import it.geosolutions.android.map.view.AdvancedMapView;
import it.geosolutions.android.map.wms.WMSLayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.mapgenerator.TileCache;
import org.mapsforge.android.maps.overlay.Overlay;
import org.mapsforge.core.model.GeoPoint;
import org.mapsforge.core.model.MapPosition;

import com.actionbarsherlock.view.Window;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * This is an implementation of the custom view for the map component
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it) 
 * 
 * 
 */
public class MapsActivity extends MapActivity {
    
	
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
    //PRIVATE VARIABLES
    //------------------------------------------------------
	private MarkerOverlay markerOverlay;
	private SpatialiteOverlay spatialiteOverlay;
	private AdvancedMapView mapView;
	
	private boolean markerActivated;
	private boolean spatialActivated;
	private boolean mapstoreActivated;
    //------------------------------------------------------
	// CONSTANTS
    //------------------------------------------------------
	private static final int MAPSTORE_REQUEST_CODE = 1;
	/** FEATURE_DEFAULT_ID */
	private static final String FEATURE_DEFAULT_ID = "OGC_FID";
	
	/** DB_LOADED_FLAG */
    private static final String DB_LOADED_FLAG = "dbLoaded";
    
    /** MARKERS_ENABLED_FLAG */
    private static final String MARKERS_ENABLED_FLAG = "markers";
    
    /** DATA_ENABLED_FLAG */
    private static final String DATA_ENABLED_FLAG = "data";
    /** MAPSTORE_ENABLED_FLAG */
	private static final String MAPSTORE_ENABLED_FLAG = "mapstore";

    /** DATAPROPERTIES_REQUEST_CODE */
    private static final int DATAPROPERTIES_REQUEST_CODE = 671;
    
    private static final String  FEATUREIDFIELD_FLAG = "fidField";
    /** choosen featureID field */
	private String featureIdField;
	private WMSOverlay wmsOverlay;
	private MapStoreConfiguration mapStoreConfig;

	
	/** CANCONFRIM_FLAG */
	private static final String CANCONFRIM_FLAG = "canConfirm_flag";
	private static final String MAPSTORE_LAYER_CONFIG = "MAPSTORE_LAYER_CONFIG";
	public static final String MAPSTORE_CONFIG = "MAPSTORE_CONFIG";
	private static boolean canConfirm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//ProgressDialog pd = ProgressDialog.show(this,"This is the title","This is the detail text",true,false,null);
		// create map view and db
		requestWindowFeature((int) Window.FEATURE_INDETERMINATE_PROGRESS);
        requestWindowFeature((int) Window.FEATURE_PROGRESS);
		boolean mapLoaded = initMap(savedInstanceState);
		dbLoaded = initDb();
		//if something went wrong durind db and map initialization,
		// we should stop
		if (!mapLoaded && !dbLoaded) {
		        //TODO: notify the user the problem
			this.finish();
		}
		//add spatialite overlay
		this.spatialiteOverlay = new SpatialiteOverlay();
		spatialiteOverlay.setProjection(mapView.getProjection());
		wmsOverlay = new WMSOverlay();
		
		if(savedInstanceState !=null){
		        if(savedInstanceState.getBoolean(DATA_ENABLED_FLAG,true)){
		            mapView.getOverlays().add(spatialiteOverlay);
		        }
		        mapStoreConfig = (MapStoreConfiguration) savedInstanceState.getSerializable(MAPSTORE_CONFIG);
		        if(mapStoreConfig!=null){
		        	loadMapStoreConfig(mapStoreConfig);
		        }
		        
		        
		}else{
		    mapView.getOverlays().add(spatialiteOverlay);
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
		

		//
		centerMapFile();
		
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
			        	 ArrayList<DescribedMarker> markers = markerOverlay.getMarkers();
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
		
        if(markerActivated)
        	toggleOverlayVisibility(R.id.markers, markerActivated);
        else if(spatialActivated)
        	toggleOverlayVisibility(R.id.data, spatialActivated);

		savedInstanceState.putBoolean(DB_LOADED_FLAG, dbLoaded);
		
		savedInstanceState.putString(FEATUREIDFIELD_FLAG, featureIdField);
		
		savedInstanceState.putBoolean(CANCONFRIM_FLAG, canConfirm);
		//MARKERS
		//get current markers
        ArrayList<DescribedMarker> markers = markerOverlay.getMarkers();
        //serialize markers in the response
        savedInstanceState.putParcelableArrayList(MapsActivity.PARAMETERS.MARKERS,MarkerUtils.getMarkersDTO(markers));
        savedInstanceState.putBoolean(MARKERS_ENABLED_FLAG, mapView.getOverlays().contains(markerOverlay));
        savedInstanceState.putBoolean(DATA_ENABLED_FLAG, mapView.getOverlays().contains(markerOverlay));
        savedInstanceState.putBoolean(MAPSTORE_ENABLED_FLAG, mapView.getOverlays().contains(wmsOverlay));
        if(mapView.getOverlays().contains(wmsOverlay)){
        	savedInstanceState.putSerializable(MAPSTORE_CONFIG, mapStoreConfig);
        }
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
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.map, menu);

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
		if (itemId == R.id.data || itemId == R.id.markers) {
			toggleOverlayVisibility(itemId, item.isChecked());
			return true;
		} else if (itemId == R.id.menu_data) {
			return showDataList(item);
		} else if(itemId == R.id.preferences){
		    Intent pref = new Intent(this,EditPreferences.class);
		    startActivity(pref);
		    return true;
        }else if(itemId == R.id.menu_geostore){
		    Intent pref = new Intent(this,GeoStoreResourcesActivity.class);
//		    pref.putExtra(GeoStoreResourcesActivity.PARAMS.GEOSTORE_URL,"http://sit.comune.bolzano.it/geostore/rest/");
		    pref.putExtra(GeoStoreResourcesActivity.PARAMS.GEOSTORE_URL,"http://mapstore.geo-solutions.it/geostore/rest/");
		    startActivityForResult(pref, MAPSTORE_REQUEST_CODE);
		    return true;
	        
		}else{
            return super.onOptionsItemSelected(item);
		} 
	}

	/**
	 * Add/Remove the Overlay from the mapView if the
	 * 
	 * @param itemId
	 *            id of the menu item
	 * @param enabled
	 *            true to set the layer visible, false to make it not visible
	 */
	private void toggleOverlayVisibility(int itemId, boolean enable) {
		boolean present = false;
		Overlay overlay = null;
		Log.v("LAYERS", mapView.getOverlays().size() + " overays found");
		for (Overlay o : mapView.getOverlays()) {
			if (o.equals(spatialiteOverlay) && itemId == R.id.data) {
				present = true;
				Log.v("LAYERS", "data layer is visible");
				overlay = o;
				break;
			}
			if (o.equals(wmsOverlay) && itemId == R.id.mapstore) {
				present = true;
				Log.v("LAYERS", "marker layer is visible");
				overlay = o;
				break;
			}
			if (o.equals(markerOverlay) && itemId == R.id.markers) {
				present = true;
				Log.v("LAYERS", "marker layer is visible");
				overlay = o;
				break;
			}
		}
		if (present && !enable) {
			mapView.getOverlays().remove(overlay);
			mapView.redraw();
			Log.v("LAYERS", "removing layer");
		} else if (!present && enable) {
			if (itemId == R.id.data) {
			        //data layer is always at level 0
				mapView.getOverlays().add(0, spatialiteOverlay);
				Log.v("LAYERS", "add data layer");
			}else if(itemId == R.id.mapstore){
				if(mapView.getOverlays().size()>0){
					mapView.getOverlays().add(1,wmsOverlay );
				}else{
					mapView.getOverlays().add(0, wmsOverlay);
				}
				
			} else if (itemId == R.id.markers) {
			        //marker overlay goes always over the data and marker overlay
					int index = (mapView.getOverlays().contains(spatialiteOverlay) ? 1 :0 ) +
							(mapView.getOverlays().add(markerOverlay) ? 1 : 0 );
					mapView.getOverlays().add(index, markerOverlay);
			        Log.v("LAYERS", "add marker layer");

			}
			mapView.redraw();

		}

	}
	/**
	 * Create the markers and add them to the MarkerOverlay
	 * Gets it from the Intent or from the savedInstanceState
	 * Assign them the proper geopoint if missing
	 * @param savedInstanceState
	 */
	private void createMarkers(Bundle savedInstanceState) {

		// create an ItemizedOverlay with the default marker
		this.markerOverlay = new MarkerOverlay();
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
		
		
		markerOverlay.getOverlayItems().addAll(markers);
		if(savedInstanceState!=null){
		    if(savedInstanceState.getBoolean(MARKERS_ENABLED_FLAG,true)){
		        mapView.getOverlays().add(markerOverlay);
		    }
		}else{
		    mapView.getOverlays().add(markerOverlay);
		}
		
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
		getMenuInflater().inflate(R.menu.map, menu);

		return true;
	}

	/**
	 * Initialize the map with Controls and background
	 * @param savedInstanceState 
	 * 
	 * @return
	 */
	private boolean initMap(Bundle savedInstanceState) {
		setContentView(R.layout.activity_map);
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
	public void addWMSLayers(ArrayList<WMSLayer> layers){
		if(wmsOverlay == null){
			wmsOverlay=new WMSOverlay();
		}
		wmsOverlay.setLayers(layers);
		toggleOverlayVisibility(R.id.mapstore, true);
		
		Log.v("WMS","TOTAL LAYERS:"+wmsOverlay.getLayers().size());
	}
	
	/**
	 * Opena the Data List activity
	 * @param item
	 * @return
	 */
	public boolean showDataList(MenuItem item) {
		Intent datalistIntent = new Intent(this, DataListActivity.class);
		boolean spatialiteEnabled = false;
        boolean markerEnabled = false;
        boolean mapstoreEnabled = false;
        for (Overlay o : mapView.getOverlays()) {
                if (o.equals(spatialiteOverlay)) {
                    spatialiteEnabled = true;
                }
                if (o.equals(markerOverlay)) {
                    markerEnabled = true;
                }
                if (o.equals(wmsOverlay)) {
                	mapstoreEnabled = true;
            }
        }
        Bundle bundle = new Bundle();
        bundle.putBoolean(MARKERS_ENABLED_FLAG, markerEnabled);
        bundle.putBoolean(DATA_ENABLED_FLAG,spatialiteEnabled); 
        bundle.putBoolean(MAPSTORE_ENABLED_FLAG,mapstoreEnabled);
        if(mapStoreConfig!=null){
        	bundle.putSerializable(MAPSTORE_CONFIG, mapStoreConfig);
        }
        datalistIntent.putExtras(bundle);
		startActivityForResult(datalistIntent, DATAPROPERTIES_REQUEST_CODE);
		return true;
	}
	
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
		if(requestCode==DATAPROPERTIES_REQUEST_CODE){
		        Bundle b = data.getExtras();
		        if(b.containsKey(MAPSTORE_CONFIG)){
		        	loadMapStoreConfig((MapStoreConfiguration)b.getSerializable(MAPSTORE_CONFIG));
		        }
		        boolean m = b.getBoolean(MARKERS_ENABLED_FLAG,true);
		        this.markerActivated=m;
		        boolean d = b.getBoolean(DATA_ENABLED_FLAG,true);
		        boolean ms = b.getBoolean(MAPSTORE_ENABLED_FLAG,true);
		        this.mapstoreActivated= ms;
		        toggleOverlayVisibility(R.id.markers ,m);
		        toggleOverlayVisibility(R.id.mapstore,ms);
		        toggleOverlayVisibility(R.id.data, d);
		        
			mapView.redraw();
		}else if(requestCode==MAPSTORE_REQUEST_CODE){
			if(data ==null ) return;//TODO fix result code
			Resource resource = (Resource) data.getSerializableExtra(GeoStoreResourceDetailActivity.PARAMS.RESOURCE);
			String geoStoreUrl = data.getStringExtra(GeoStoreResourcesActivity.PARAMS.GEOSTORE_URL);
			 MapStoreUtils.loadMapStoreConfig(geoStoreUrl, resource, this);
			
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
                toggleOverlayVisibility(R.id.markers, true);
                m =  mapView.getMarkerOverlay();
            }
            
            //gets
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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
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
	public void setMapStoreConfig(MapStoreConfiguration result) {
		this.mapStoreConfig=result;
		
	}
	
	public void loadMapStoreConfig(MapStoreConfiguration result){

		addWMSLayers(MapStoreUtils.buildWMSLayers(result));
		Log.v("MapStore","LAYERS in WMS LAYER:"+  wmsOverlay.getLayers().size());
		setMapStoreConfig(result);
	}
	
}