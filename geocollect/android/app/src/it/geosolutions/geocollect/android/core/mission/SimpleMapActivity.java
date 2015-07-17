package it.geosolutions.geocollect.android.core.mission;

import it.geosolutions.android.map.MapsActivity;
import it.geosolutions.android.map.activities.MapActivityBase;
import it.geosolutions.android.map.control.CoordinateControl;
import it.geosolutions.android.map.control.LocationControl;
import it.geosolutions.android.map.dto.MarkerDTO;
import it.geosolutions.android.map.model.MSMMap;
import it.geosolutions.android.map.overlay.MarkerOverlay;
import it.geosolutions.android.map.overlay.items.DescribedMarker;
import it.geosolutions.android.map.overlay.managers.MultiSourceOverlayManager;
import it.geosolutions.android.map.utils.MapFilesProvider;
import it.geosolutions.android.map.view.AdvancedMapView;
import it.geosolutions.geocollect.android.app.R;
import java.io.File;

import org.mapsforge.android.maps.MapView;
import org.mapsforge.core.model.GeoPoint;
import org.mapsforge.core.model.MapPosition;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * A SimpleMapView shows a full screen map according to the current map settings
 * 
 * Add arguments to the intent to if the intent it started provided coordinates it will show markers
 * 
 * 
 * @author Robert Oehler
 *
 */
public class SimpleMapActivity extends MapActivityBase {
	
	private AdvancedMapView mapView;
	
    private static final File MAP_FILE = MapFilesProvider.getBackgroundMapFile();
	
    public final static String ARG_PRIORITY_COLOR = "it.geosolutions.geocollect.simplemapactivity.arg_color";
    public final static String ARG_ZOOM = "it.geosolutions.geocollect.simplemapactivity.arg_zoom";
    public final static String ARG_ZOOM_MIN = "it.geosolutions.geocollect.simplemapactivity.arg_zoom_min";
    public final static String ARG_ZOOM_MAX = "it.geosolutions.geocollect.simplemapactivity.arg_zoom_max";
    
    public final static String ARG_FIRST_POINT_LAT = "it.geosolutions.geocollect.simplemapactivity.fir_lat";
    public final static String ARG_FIRST_POINT_LON = "it.geosolutions.geocollect.simplemapactivity.fir_lon";
    
    public final static String ARG_SECOND_POINT_LAT = "it.geosolutions.geocollect.simplemapactivity.sec_lat";
    public final static String ARG_SECOND_POINT_LON = "it.geosolutions.geocollect.simplemapactivity.sec_lon";
    
    private DescribedMarker mOriginMarker;
    private DescribedMarker mUpdatedMarker;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.form_mapview);
		
		mapView = (AdvancedMapView) findViewById(R.id.advancedMapView);
		
		ImageButton buttonLocation = (ImageButton) findViewById(R.id.ButtonLocation);
		 
		//mapView = new AdvancedMapView(this);
		
		initMap(savedInstanceState);
		
		MultiSourceOverlayManager o = new MultiSourceOverlayManager(mapView);
		o.setMarkerOverlay(new MarkerOverlay());
		o.setMarkerVisible();
		mapView.setOverlayManger(o);
        
		if(getIntent() != null && getIntent().getExtras() != null && getIntent().getExtras().containsKey(MapsActivity.MSM_MAP)){
		    o.loadMap((MSMMap)getIntent().getExtras().getSerializable(MapsActivity.MSM_MAP));
		}
		
		mapView.setLayoutParams(new LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
		
		
		mapView.setClickable(true);
		mapView.setBuiltInZoomControls(true);
		
		//add coordinates control
		mapView.addControl(new CoordinateControl(mapView, true));
		
		//add "location" control connected to the button
		LocationControl lc  =new LocationControl(mapView);
		lc.setActivationButton(buttonLocation);
		mapView.addControl(lc);
		
		centerMapFileAndAddMarkers();
	}
	
	/**
	 * center the map and add markers if information available
	 */
	public void centerMapFileAndAddMarkers() {
		
		MarkerOverlay mo = mapView.getMarkerOverlay();
		MapPosition mp = mapView.getMapViewPosition().getMapPosition();
		
		Intent intent = getIntent();
		
		GeoPoint mainPoint = null;
		
		if(intent.hasExtra(ARG_FIRST_POINT_LAT) && intent.hasExtra(ARG_FIRST_POINT_LON) ){

			double lat = intent.getDoubleExtra(ARG_FIRST_POINT_LAT, 43.68411);
			double lon = intent.getDoubleExtra(ARG_FIRST_POINT_LON, 10.84899);

			mOriginMarker = new MarkerDTO(lat, lon, MarkerDTO.PIN_BLUE).createMarker(this);
			
			//you could apply a color filter here, according to the missions "gravità"
			//but with the new pin markers, color filters won't work very well
//			int priorityColor = intent.getIntExtra(ARG_COLOR, -1);	
//			if(priorityColor != -1){
//				applyColorFilter(mOriginMarker.getDrawable(), priorityColor);
//			}
			
			mo.getOverlayItems().add(mOriginMarker);
			
			mainPoint = new GeoPoint(lat,lon);			
		}	
		
		if(intent.hasExtra(ARG_SECOND_POINT_LAT) && intent.hasExtra(ARG_SECOND_POINT_LON)){
				
				final GeoPoint updatedPoint = new GeoPoint(intent.getDoubleExtra(ARG_SECOND_POINT_LAT, 0.0), intent.getDoubleExtra(ARG_SECOND_POINT_LON, 0.0));
				
				mUpdatedMarker = new MarkerDTO(updatedPoint.latitude, updatedPoint.longitude, MarkerDTO.PIN_RED).createMarker(this);

				mo.getOverlayItems().add(mUpdatedMarker);
				
				mainPoint = updatedPoint;
		}
		
		if(mainPoint != null){

			byte zoom_level = intent.getByteExtra(ARG_ZOOM, (byte) 16);
			byte zoom_level_min = intent.getByteExtra(ARG_ZOOM_MIN, (byte) 0);
			byte zoom_level_max = intent.getByteExtra(ARG_ZOOM_MAX, (byte) 30);

			mp = new MapPosition(mainPoint, zoom_level);

			mapView.getMapViewPosition().setMapPosition(mp);
			mapView.getMapZoomControls().setZoomLevelMin(zoom_level_min);        	 
			mapView.getMapZoomControls().setZoomLevelMax(zoom_level_max);

			mapView.getOverlayController().redrawOverlays();

		} else {
			
			Log.e(SimpleMapActivity.class.getSimpleName(), "no lat/lon provided, cannot center/set marker");	       
		}
	}
	/**
	 * applies a ColorFilter to a Drawable
	 * @param d the drawable to apply to
	 * @param color the color to apply
	 */
	public void applyColorFilter(Drawable d, int color){


		if(color != Integer.MIN_VALUE){
			try{
				d.mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
			}catch(IllegalArgumentException iae){
				Log.e(SimpleMapActivity.class.getSimpleName(), "A feature has an incorrect color value" );
			}
		}else{
			d.mutate().clearColorFilter();

		}
	}
	
	/**
	 * Initialize the map
	 * @param savedInstanceState 
	 * @return
	 */
	private boolean initMap(Bundle savedInstanceState) {

		mapView.setClickable(true);
		mapView.setBuiltInZoomControls(true);

    	final String filePath = PreferenceManager.getDefaultSharedPreferences(this).getString(MapView.MAPSFORGE_BACKGROUND_FILEPATH, null);
    	final int type = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString(MapView.MAPSFORGE_BACKGROUND_RENDERER_TYPE, "0"));
    	
    	//if the map file was edited in the preferences
		if(filePath != null && type == 0){
			//use it
			mapView.setMapFile(new File(filePath));
			
		}else if (MAP_FILE!=null) {
			
			Log.i(SimpleMapActivity.class.getSimpleName(),"setting background file");
			mapView.setMapFile(MAP_FILE);
			
		} else {
			Log.i(SimpleMapActivity.class.getSimpleName(),"unable to set background file");
		}
		
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inf = getSupportMenuInflater();
		inf.inflate(R.menu.simple_map_menu, menu);

		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		//center on the marker, preferably the updated
		if(item.getItemId() == R.id.center){

			GeoPoint geoPoint = null;
			
			if(mUpdatedMarker != null){
				geoPoint = mUpdatedMarker.getGeoPoint();
			}else if(mOriginMarker != null){
				geoPoint = mOriginMarker.getGeoPoint();
			}
			
			if(geoPoint != null){					
				//center map on markers position
				mapView.getMapViewPosition().setCenter(geoPoint);
			}
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	public DescribedMarker getOriginMarker(){
		return mOriginMarker;
	}
	public DescribedMarker getUpdatedMarker(){
		return mUpdatedMarker;
	}
	public AdvancedMapView getMapView(){
		return mapView;
	}
	
}
