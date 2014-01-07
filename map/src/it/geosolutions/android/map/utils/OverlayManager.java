package it.geosolutions.android.map.utils;

import java.util.ArrayList;

import it.geosolutions.android.map.MapsActivity;
import it.geosolutions.android.map.R;
import it.geosolutions.android.map.listeners.OverlayChangeListener;
import it.geosolutions.android.map.mapstore.model.MapStoreConfiguration;
import it.geosolutions.android.map.mapstore.utils.MapStoreUtils;
import it.geosolutions.android.map.overlay.MarkerOverlay;
import it.geosolutions.android.map.overlay.SpatialiteOverlay;
import it.geosolutions.android.map.overlay.WMSOverlay;
import it.geosolutions.android.map.overlay.items.DescribedMarker;
import it.geosolutions.android.map.view.AdvancedMapView;
import it.geosolutions.android.map.wms.WMSLayer;

import org.mapsforge.android.maps.overlay.MyLocationOverlay;
import org.mapsforge.android.maps.overlay.Overlay;

import android.os.Bundle;
import android.util.Log;
/**
 * Manages overlays of the AdvancedMapView
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public class OverlayManager {
	 /** MARKERS_ENABLED_FLAG */
    private static final String MARKERS_ENABLED_FLAG = "markers";
    
    /** DATA_ENABLED_FLAG */
    private static final String DATA_ENABLED_FLAG = "data";
    /** MAPSTORE_ENABLED_FLAG */
	private static final String MAPSTORE_ENABLED_FLAG = "mapstore";
	public static final String MAPSTORE_CONFIG = "MAPSTORE_CONFIG";

	//------------------------------------------------------
    //PUBLIC VARIABLES
    //------------------------------------------------------
    public MarkerOverlay markerOverlay;
    public SpatialiteOverlay spatialiteOverlay;
    public AdvancedMapView mapView;
	
	public boolean markerActivated;
	public boolean spatialActivated;
	public boolean mapstoreActivated;
	public WMSOverlay wmsOverlay;
	private MapStoreConfiguration mapStoreConfig;
	
	/**
	 * returns the <MapStoreConfiguration> for the WMSLayer
	 * @return
	 */
	public MapStoreConfiguration getMapStoreConfig() {
		return mapStoreConfig;
	}
	
	/**
	 * set the 
	 * @param mapStoreConfig for the WMSLayer
	 */
	public void setMapStoreConfig(MapStoreConfiguration mapStoreConfig) {
		this.mapStoreConfig = mapStoreConfig;
		overlayChangeListener.onOverlayVisibilityChange(R.id.mapstore, mapstoreActivated);
	}
	
	/**
	 * the <OverlayChangeListener> that notifies the changes in the overlay
	 * visibility and events of add of a <MapStoreConfiguration>
	 */
	public OverlayChangeListener overlayChangeListener =new OverlayChangeListener(){
		public void onOverlayVisibilityChange(int id, boolean visibility) {}
		
	};
	
	/**
	 * get the current <OverlayChangeListener>
	 * @return
	 */
	public OverlayChangeListener getOverlayChangeListener() {
		return overlayChangeListener;
	}
	
	/**
	 * set the <OverlayChangeListener> object to monitor the overlay changes
	 * @param overlayChangeListener
	 */
	public void setOverlayChangeListener(OverlayChangeListener overlayChangeListener) {
		this.overlayChangeListener = overlayChangeListener;
	}
	
	/**
	 * Create the <OverlayManager>
	 * it automatically binds to the mapView and initialize overlays
	 * @param mapView
	 */
	public OverlayManager(AdvancedMapView mapView) {
		//add spatialite overlay
		this.mapView = mapView;
		mapView.setOverlayManger(this);
		this.spatialiteOverlay = new SpatialiteOverlay();
		spatialiteOverlay.setProjection(mapView.getProjection());
		wmsOverlay = new WMSOverlay();
	}
	/**
	 * Add/Remove the Overlay from the mapView if the
	 * 
	 * @param itemId
	 *            id of the menu item
	 * @param enabled
	 *            true to set the layer visible, false to make it not visible
	 */
	public void toggleOverlayVisibility(int itemId, boolean enable) {
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
			overlayChangeListener.onOverlayVisibilityChange(itemId, enable);
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
			overlayChangeListener.onOverlayVisibilityChange(itemId, enable);
			mapView.getOverlayController().redrawOverlays();

		}

	}
	
	/**
	 * sets the data overlay visible
	 */
	public void setDataVisible() {
		spatialActivated = true;
		mapView.getOverlays().add(spatialiteOverlay);
		overlayChangeListener.onOverlayVisibilityChange(R.id.data, true);

		
	}
	
	/**
	 * get the markerOverlay
	 * @return the <MarkerOverlay>
	 */
	public MarkerOverlay getMarkerOverlay() {
		return markerOverlay;
	}
	public SpatialiteOverlay getDataOverlay() {
		return spatialiteOverlay;
	}
	public void setMarkerOverlay(MarkerOverlay markerOverlay) {
		this.markerOverlay= markerOverlay;
	}
	public void setMarkerVisible() {
		markerActivated =true;
		overlayChangeListener.onOverlayVisibilityChange(R.id.markers, true);
		mapView.getOverlays().add(markerOverlay);
		
	}
	public void addLocationOverlay(MyLocationOverlay overlay) {
		mapView.getOverlays().add(mapView.getOverlays().size(), overlay);
		
	}
	public void removeOverlay(MyLocationOverlay overlay) {
		mapView.getOverlays().remove(overlay);	
	}
	public void addWMSLayers(ArrayList<WMSLayer> layers){
		if(wmsOverlay == null){
			wmsOverlay=new WMSOverlay();
		}
		wmsOverlay.setLayers(layers);
		toggleOverlayVisibility(R.id.mapstore, true);
		Log.v("WMS","TOTAL LAYERS:" + wmsOverlay.getLayers().size());
	}
	public WMSOverlay getWMSOverlay() {
		// TODO Auto-generated method stub
		return wmsOverlay;
	}
	
	/**
	 * load a MapStore configuration into the WMSOverlay
	 * @param the <MapStoreConfiguration> object to load
	 */
	public void loadMapStoreConfig(MapStoreConfiguration result){
		if(result == null) return ;
		addWMSLayers(MapStoreUtils.buildWMSLayers(result));
		setMapStoreConfig(result);
	}
	
	/**
	 * save the current status of the layers in the provided bundle
	 * @param savedInstanceState
	 */
	public void saveInstanceState(Bundle savedInstanceState) {
		ArrayList<DescribedMarker> markers = getMarkerOverlay().getMarkers();
		savedInstanceState.putParcelableArrayList(MapsActivity.PARAMETERS.MARKERS,MarkerUtils.getMarkersDTO(markers));
        savedInstanceState.putBoolean(MARKERS_ENABLED_FLAG, mapView.getOverlays().contains(getMarkerOverlay()));//TODO change for visibility!!!
        savedInstanceState.putBoolean(DATA_ENABLED_FLAG, mapView.getOverlays().contains(getDataOverlay()));
        savedInstanceState.putBoolean(MAPSTORE_ENABLED_FLAG, mapView.getOverlays().contains(getWMSOverlay()));
    	savedInstanceState.putSerializable(MAPSTORE_CONFIG, getMapStoreConfig());
        
		
	}
	
	/**
	 * restore the state of the overlays from a bundle
	 * @param savedInstanceState
	 */
	public void restoreInstanceState(Bundle savedInstanceState) {
		 if(savedInstanceState.getBoolean(MARKERS_ENABLED_FLAG,true)){
		        setMarkerVisible();
	        if(savedInstanceState.getBoolean(DATA_ENABLED_FLAG,true)){
	            setDataVisible();
	        }
		 }
     	loadMapStoreConfig((MapStoreConfiguration) savedInstanceState.getSerializable(MAPSTORE_CONFIG));
		toggleOverlayVisibility(R.id.mapstore,savedInstanceState.getBoolean(MAPSTORE_ENABLED_FLAG,false) );
	}
	
	
}
