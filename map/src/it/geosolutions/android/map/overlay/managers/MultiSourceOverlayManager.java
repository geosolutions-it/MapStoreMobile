/*
 * GeoSolutions - MapstoreMobile - GeoSpatial Framework on Android based devices
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
package it.geosolutions.android.map.overlay.managers;

import it.geosolutions.android.map.MapsActivity;
import it.geosolutions.android.map.R;
import it.geosolutions.android.map.listeners.LayerChangeListener;
import it.geosolutions.android.map.mapstore.model.MapStoreConfiguration;
import it.geosolutions.android.map.mapstore.utils.MapStoreUtils;
import it.geosolutions.android.map.model.Layer;
import it.geosolutions.android.map.model.MSMMap;
import it.geosolutions.android.map.overlay.MarkerOverlay;
import it.geosolutions.android.map.overlay.MultiSourceOverlay;
import it.geosolutions.android.map.overlay.SpatialiteOverlay;
import it.geosolutions.android.map.overlay.items.DescribedMarker;
import it.geosolutions.android.map.utils.MarkerUtils;
import it.geosolutions.android.map.view.AdvancedMapView;

import java.util.ArrayList;

import org.mapsforge.android.maps.overlay.MyLocationOverlay;
import org.mapsforge.android.maps.overlay.Overlay;

import android.os.Bundle;
import android.util.Log;
/**
 * Manages overlays of the AdvancedMapView
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public class MultiSourceOverlayManager implements OverlayManager {
	 /** MARKERS_ENABLED_FLAG */
    private static final String MARKERS_ENABLED_FLAG = "markers";
    
    /** DATA_ENABLED_FLAG */
    private static final String DATA_ENABLED_FLAG = "data";
    /** MAPSTORE_ENABLED_FLAG */
	private static final String MAPSTORE_ENABLED_FLAG = "mapstore";
	public static final String MAPSTORE_CONFIG = "MAPSTORE_CONFIG";

	private static final String LAYERS = "MAPSTORE_LAYERS";
	
	//------------------------------------------------------
    //PUBLIC VARIABLES
    //------------------------------------------------------
    public MarkerOverlay markerOverlay;
    public SpatialiteOverlay spatialiteOverlay;
	
	public boolean markerActivated;
	public boolean spatialActivated;
	public boolean mapstoreActivated;
	public MultiSourceOverlay layerOverlay;
	private MapStoreConfiguration mapStoreConfig;
	private AdvancedMapView mapView;

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
	}
	
	/**
	 * the <OverlayChangeListener> that notifies the changes in the overlay
	 * visibility and events of add of a <MapStoreConfiguration>
	 */
	public LayerChangeListener overlayChangeListener;

	private LayerChangeListener listener;
	
	/**
	 * Create the <OverlayManager>
	 * it automatically binds to the mapView and initialize overlays
	 * @param mapView
	 */
	public MultiSourceOverlayManager(AdvancedMapView mapView) {
		this.mapView =mapView;
		//add spatialite overlay
		mapView.setOverlayManger(this);
		this.spatialiteOverlay = new SpatialiteOverlay();
		spatialiteOverlay.setProjection(mapView.getProjection());
		layerOverlay = new MultiSourceOverlay();
		layerOverlay.setProjection(mapView.getProjection());
		setMarkerOverlay(new MarkerOverlay());
	}

	
	public void redrawLayer(Layer layer) {
		layerOverlay.refreshLayer(layer);
		mapView.getOverlayController().redrawOverlays();
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
			if (o.equals(layerOverlay) && itemId == R.id.mapstore) {
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
				//mapView.getOverlays().add(0, spatialiteOverlay);
				Log.v("LAYERS", "add data layer");
			}else if(itemId == R.id.mapstore){
				if(mapView.getOverlays().size()>0){
					mapView.getOverlays().add(1,layerOverlay );
				}else{
					mapView.getOverlays().add(0, layerOverlay);
				}
				
			} else if (itemId == R.id.markers) {
			        //marker overlay goes always over the data and marker overlay
					int index = (mapView.getOverlays().contains(spatialiteOverlay) ? 1 :0 ) +
							(mapView.getOverlays().add(markerOverlay) ? 1 : 0 );
					mapView.getOverlays().add(index, markerOverlay);
			        Log.v("LAYERS", "add marker layer");

			}
			mapView.getOverlayController().redrawOverlays();

		}

	}
	
	/**
	 * sets the data overlay visible
	 */
	public void setDataVisible() {
		spatialActivated = true;
		//mapView.getOverlays().add(spatialiteOverlay);
		
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
		mapView.getOverlays().add(markerOverlay);
		
	}
	public void addLocationOverlay(MyLocationOverlay overlay) {
		mapView.getOverlays().add(mapView.getOverlays().size(), overlay);
		
	}
	public void removeOverlay(Overlay overlay) {
		mapView.getOverlays().remove(overlay);	
	}
	public void setLayers(ArrayList<Layer> layers){
		if(layerOverlay == null){
			layerOverlay=new MultiSourceOverlay();
		}
		layerOverlay.setLayers(layers);
		toggleOverlayVisibility(R.id.mapstore, true);
		onSetLayers(layers);
		Log.v("LAYERS","TOTAL LAYERS:" + layerOverlay.getLayers().size());
	}
	
	private void onSetLayers(ArrayList<Layer> layers) {
		LayerChangeListener lcl = getLayerChangeListener();
		if(lcl!= null){
			lcl.onSetLayers(layers);
		}else{
			Log.w("Overlay Manager","the change listener is not intialized yet");
		}
		
	}

	public MultiSourceOverlay getOverlay() {
		return layerOverlay;
	}
	
	public ArrayList<Layer> getLayers(){
		return layerOverlay.getLayers();
	}
	/**
	 * load a MapStore configuration into the WMSOverlay
	 * @param the <MapStoreConfiguration> object to load
	 */
	public void loadMapStoreConfig(MapStoreConfiguration result){
		if(result == null) return ;
		ArrayList<Layer> l = MapStoreUtils.buildWMSLayers(result); //TODO used for test REMOVE IT
				l.addAll(getLayers());
				
		setLayers(l);
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
        //savedInstanceState.putBoolean(MAPSTORE_ENABLED_FLAG, mapView.getOverlays().contains(getViewOverlay()));
    	savedInstanceState.putSerializable(MAPSTORE_CONFIG, getMapStoreConfig());
    	savedInstanceState.putSerializable(LAYERS, getLayers());
        
		
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
     	//loadMapStoreConfig((MapStoreConfiguration) savedInstanceState.getSerializable(MAPSTORE_CONFIG));
		setLayers((ArrayList<Layer>)savedInstanceState.getSerializable(LAYERS));
		toggleOverlayVisibility(R.id.mapstore,true);
		
	}

	public void defaultInit() {
		
		setMarkerVisible();
		
	}
	
	public LayerChangeListener getLayerChangeListener() {
		return listener;
	}

	public void setLayerChangeListener(LayerChangeListener l){
		listener = l;
	}
	
	public void loadMap(MSMMap m){
		setLayers(m.layers);
	}
}
