package it.geosolutions.android.map.view;

import java.util.ArrayList;

import org.mapsforge.android.maps.MapView;

import android.util.Log;
/**
 * A <MapView> manager that provide methods to register and destroy map Views.
 * Wraps actions that should be done by the old <MapActivityBase> when not possible to
 * Extend this class
 * @author Lorenzo Natali <lorenzo.natali@geo-solutions.it
 *
 */
public class MapViewManager {
	private ArrayList<MapView> mapViews = new ArrayList<MapView>(2);
	private int lastMapViewId=0;
	
	/**
	 * destroys the map views
	 */
	public void destroyMapViews() {
         while (!this.mapViews.isEmpty()) {
                 MapView mapView = this.mapViews.remove(0);
                 mapView.destroy();
                 Log.v("MAPVIEWMANAGER","destroy mapview"+ 0);
         }
	 }
	
	public void resumeMapViews(){
		for (int i = 0, n = this.mapViews.size(); i < n; ++i) {
	            this.mapViews.get(i).onResume();
	            Log.v("MAPVIEWMANAGER","resume mapview"+ i);
	    }
	}
	
	/**
	 * register a <MapView> to the manager
	 * @param mapView
	 */
	 public final void registerMapView(MapView mapView) {
			this.mapViews.add(mapView);
	 }
	 
	 /**
      * @return a unique MapView ID on each call.
      */
	 public final int getMapViewId() {
    	 	 
             return ++this.lastMapViewId;
     }

}
