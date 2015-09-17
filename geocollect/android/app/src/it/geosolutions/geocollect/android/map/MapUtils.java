package it.geosolutions.geocollect.android.map;

import it.geosolutions.android.map.geostore.model.Resource;
import it.geosolutions.android.map.mapstore.model.MapStoreConfiguration;
import it.geosolutions.android.map.mapstore.utils.MapStoreConfigTask;
import it.geosolutions.android.map.overlay.managers.OverlayManager;
import it.geosolutions.android.map.wms.WMSLayer;

import org.mapsforge.android.maps.MapView;
import org.mapsforge.core.model.GeoPoint;
import org.mapsforge.core.model.MapPosition;

import android.os.AsyncTask;
import android.util.Log;

public class MapUtils {

    /**
     * Creates an async task to get and read a mapstore configuration from the geostore url.
     * 
     * @param geoStoreUrl
     * @param resource
     * @param mapsActivity
     * @return
     */
    public static WMSLayer loadMapStoreConfig(final String geoStoreUrl,final Resource resource, final OverlayManager overlayManager, final MapView mapView) {
        
        if(resource == null || geoStoreUrl == null || overlayManager == null){
            return null;
        }
        
        AsyncTask<String, String, MapStoreConfiguration> task= new MapStoreConfigTask(resource.id, geoStoreUrl){

            @Override
            protected void onPostExecute(MapStoreConfiguration result) {
                Log.d(MapUtils.class.getSimpleName(),result.toString());
                //call the loadMapStore config on the Activity
                overlayManager.loadMapStoreConfig(result);
                if(mapView != null){
                    GeoPoint p = getPoint(result);
                    if(p!=null){
                        mapView.getMapViewPosition().setMapPosition(new MapPosition(p, (byte)result.map.zoom));
                    }
                }
            }
        };
        
        task.execute("");
        return null;
        
    }
    
}
