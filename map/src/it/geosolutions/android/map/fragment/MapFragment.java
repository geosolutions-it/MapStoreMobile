package it.geosolutions.android.map.fragment;

import java.util.ArrayList;
import java.util.List;

import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.core.model.GeoPoint;
import org.mapsforge.core.model.MapPosition;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.actionbarsherlock.app.SherlockFragment;
//import android.app.Fragment;

/**
 * Experimental fragment for this version of MapsActivity
 * @author Lorenzo Natali (lorenzo.natali at geo-solutions.it)
 *
 */
public class MapFragment extends SherlockFragment {
        private static final String KEY_LATITUDE = "latitude";
        private static final String KEY_LONGITUDE = "longitude";
        private static final String KEY_MAP_FILE = "mapFile";
        private static final String KEY_ZOOM_LEVEL = "zoomLevel";
        private static final String PREFERENCES_FILE = "MapActivity";
    	private static final String PREFERENCES_VERSION_KEY = "version";
    	private static final int PREFERENCES_VERSION_NUMBER = 2;

        /**
         * Counter to store the last ID given to a MapView.
         */
        private int lastMapViewId;

        /**
         * Internal list which contains references to all running MapView objects.
         */
        private final List<MapView> mapViews = new ArrayList<MapView>(2);
        private static boolean isCompatible(SharedPreferences sharedPreferences) {
    		return sharedPreferences.getInt(PREFERENCES_VERSION_KEY, -1) == PREFERENCES_VERSION_NUMBER;
    	}

        private void destroyMapViews() {
                while (!this.mapViews.isEmpty()) {
                        MapView mapView = this.mapViews.remove(0);
                        mapView.destroy();
                }
        }

        @Override
        public void onDestroy() {
                super.onDestroy();
                destroyMapViews();
        }

        @Override
        public void onPause() {
                super.onPause();
                //pause map views
                for (MapView currentMapView : this.mapViews){
                        currentMapView.onPause();
                }
               //save the status of the mapView
                MapView mapView = null;
                if(this.mapViews.size()>0){
                	mapView = this.mapViews.get(0);
                }
                // save the map position and zoom level
                if(mapView !=null){
            	 Editor editor = getActivity().getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE).edit();
                 editor.clear();
                MapPosition mapPosition = mapView.getMapViewPosition().getMapPosition();
	                if (mapPosition != null) {
	                        GeoPoint geoPoint = mapPosition.geoPoint;
	                        editor.putFloat(KEY_LATITUDE,(float) geoPoint.latitude);
	                        editor.putFloat(KEY_LONGITUDE,(float) geoPoint.longitude);
	                        editor.putInt(KEY_ZOOM_LEVEL, mapPosition.zoomLevel);
	                }
	
	                if (mapView.getMapFile() != null) {
	        			// save the map file
	        			editor.putString(KEY_MAP_FILE, mapView.getMapFile().getAbsolutePath());
	        		}
	                editor.commit();

	                if (isRemoving()) {
	                        destroyMapViews();
	                }
                }
               
}

        @Override
        public void onResume() {
                super.onResume();
                for (MapView currentMapView : this.mapViews) {
                        currentMapView.onResume();
                }
        }

        public MapFragmentContext getMapContext() {
                return new MapFragmentContext(getActivity().getApplicationContext());
        }

        private class MapFragmentContext extends ContextWrapper implements MapActivity {
                public MapFragmentContext(Context base) {
                        super(base);
                }

                /**
                 * Returns a unique MapView ID on each call.
                 * 
                 * @return the new MapView ID.
                 */
                public int getMapViewId() {
                        return ++MapFragment.this.lastMapViewId;
                }

                /**
                 * This method is called once by each MapView during its setup process.
                 * 
                 * @param mapView
                 *            the calling MapView.
                 */
                @Override
                public void registerMapView(MapView mapView) {
                        MapFragment.this.mapViews.add(mapView);
                        restoreMapView(mapView);
                }

                private boolean containsMapViewPosition(SharedPreferences sharedPreferences) {
                        return sharedPreferences.contains(KEY_LATITUDE) && sharedPreferences.contains(KEY_LONGITUDE)
                                        && sharedPreferences.contains(KEY_ZOOM_LEVEL);
                }

                private void restoreMapView(MapView mapView) {
                        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES_FILE, MODE_PRIVATE);
                        if (isCompatible(sharedPreferences) && containsMapViewPosition(sharedPreferences)) {
                                

	                        	// get and set the map position and zoom level
	                			float latitude = sharedPreferences.getFloat(KEY_LATITUDE, 0);
	                			float longitude = sharedPreferences.getFloat(KEY_LONGITUDE, 0);
	                			int zoomLevel = sharedPreferences.getInt(KEY_ZOOM_LEVEL, -1);

	                			GeoPoint geoPoint = new GeoPoint(latitude, longitude);
	                			MapPosition mapPosition = new MapPosition(geoPoint, (byte) zoomLevel);
	                			mapView.getMapViewPosition().setMapPosition(mapPosition);
                        }
                }

				@Override
				public Context getActivityContext() {
					return getActivity();
					
				}
				
        }
        
}