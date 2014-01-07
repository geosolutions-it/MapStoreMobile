/*
 * GeoSolutions GeoSolutions Android Map Library - Digital mapping on Android based devices
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
package it.geosolutions.android.map.listeners;

import it.geosolutions.android.map.R;
import it.geosolutions.android.map.activities.GetFeatureInfoLayerListActivity;
import it.geosolutions.android.map.database.SpatialDataSourceManager;
import it.geosolutions.android.map.model.FeatureCircleQuery;
import it.geosolutions.android.map.model.FeatureInfoQuery;
import it.geosolutions.android.map.style.AdvancedStyle;
import it.geosolutions.android.map.style.StyleManager;
import it.geosolutions.android.map.utils.Singleton_Polygon_Points;
import it.geosolutions.android.map.utils.StyleUtils;
import it.geosolutions.android.map.view.AdvancedMapView;
import it.geosolutions.android.map.utils.Coordinates;
import java.util.ArrayList;
import java.util.List;
import org.mapsforge.core.model.GeoPoint;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.core.util.MercatorProjection;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import eu.geopaparazzi.spatialite.database.spatial.core.SpatialVectorTable;

/**
 * Listener that implements OnTouch Event on map.
 * @author Lorenzo Natali (www.geo-solutions.it.
 */
public class MapInfoListener implements OnTouchListener{

// MODES
public static final int MODE_VIEW = 0;

public static final int MODE_EDIT = 1;

private int mode = MODE_EDIT;

private boolean dragStarted;

private float startX;

private float startY;

private float endX;

private float endY;

private boolean infoTaskLaunched;

protected AdvancedMapView view;

private boolean isPinching;

private Activity activity;

private String Shape_Selection;

private SharedPreferences pref; //Used to check shape of selection desired

/**
 * Constructor for class MapInfoListener
 * @param mapView
 * @param activity
 * @param Shape_Selection
 */
public MapInfoListener(AdvancedMapView mapView, Activity activity, String Shape_Selection) {
    view = mapView;
    this.activity = activity;
    this.Shape_Selection = Shape_Selection;
    pref = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
}

/**
 * Create a FeatureQuery for rectangular selection and passes it via intent.
 * @param n
 * @param w
 * @param s
 * @param e
 */
private void infoDialog(final double n, final double w, final double s,
        final double e) {
    try {
        final SpatialDataSourceManager sdbManager = SpatialDataSourceManager
                .getInstance();
        final List<SpatialVectorTable> spatialTables = sdbManager
                .getSpatialVectorTables(false);
        final StyleManager styleManager = StyleManager.getInstance();
        final byte zoomLevel = view.getMapViewPosition().getZoomLevel();
        ArrayList<String> layerNames = new ArrayList<String>();
        for (SpatialVectorTable table : spatialTables) {
            String tableName = table.getName();
            AdvancedStyle style = styleManager.getStyle(tableName);

            // skip this table if not visible
            if (StyleUtils.isVisible(style, zoomLevel)) {
                layerNames.add(table.getName());

            }
        }
        Intent i = new Intent(view.getContext(),
                GetFeatureInfoLayerListActivity.class);
        i.putExtra("layers", layerNames);
        FeatureInfoQuery query = new FeatureInfoQuery();
        query.setE(e);
        query.setN(n);
        query.setS(s);
        query.setW(w);
        query.setSrid("4326");
        i.putExtra("query", query);
        i.putExtra("selection","Rectangular");
        if (mode == MODE_EDIT) {
            i.setAction(Intent.ACTION_PICK);
        } else {
            i.setAction(Intent.ACTION_VIEW);
        }
        activity.startActivityForResult(i,
                GetFeatureInfoLayerListActivity.BBOX_REQUEST);

    } catch (Exception ex) {
        ex.printStackTrace();
    }
}

/**
 * Create a Feature Query for circular selection and passes it to an activity via intent.
 * @param x
 * @param y
 * @param radius
 */
private void infoDialogCircle(final double x, final double y, final double radius /*,final double stroke*/){
	try {
        final SpatialDataSourceManager sdbManager = SpatialDataSourceManager
                .getInstance();
        final List<SpatialVectorTable> spatialTables = sdbManager
                .getSpatialVectorTables(false);
        final StyleManager styleManager = StyleManager.getInstance();
        final byte zoomLevel = view.getMapViewPosition().getZoomLevel();
        ArrayList<String> layerNames = new ArrayList<String>();
        for (SpatialVectorTable table : spatialTables) {
            String tableName = table.getName();
            AdvancedStyle style = styleManager.getStyle(tableName);

            // skip this table if not visible
            if (StyleUtils.isVisible(style, zoomLevel)) {
                layerNames.add(table.getName());

            }
        }
        Intent i = new Intent(view.getContext(),
                GetFeatureInfoLayerListActivity.class);
        i.putExtra("layers", layerNames);
        FeatureCircleQuery query = new FeatureCircleQuery();
        query.setX(x);
        query.setY(y);
        query.setRadius(radius);
        query.setSrid("4326");
        i.putExtra("query", query);
        i.putExtra("selection","Circular"); //Indicate that user has choosed circular selection
        if (mode == MODE_EDIT) {
            i.setAction(Intent.ACTION_PICK);
        } else {
            i.setAction(Intent.ACTION_VIEW);
        }
        activity.startActivityForResult(i,
                GetFeatureInfoLayerListActivity.CIRCLE_REQUEST);

    } catch (Exception ex) {
        ex.printStackTrace();
    }
}

/**
 * Method to handle touch event on view.
 * @param v
 * @param event class that handle touch.
 */
@Override
public boolean onTouch(View v, MotionEvent event){
    String[] array = activity.getResources().getStringArray(R.array.preferences_selection_shape);
    
    int action = event.getAction();
	int pointerCount = event.getPointerCount(); //Number of pointer of device
    // Try to skip pinch events
    if (Log.isLoggable("MAPINFOTOOL", Log.DEBUG)) {// Log check to avoid string
                                                   // creation
        Log.d("MAPINFOTOOL", "performed action:" + action + " on Info Tool");
    }
    if (action == MotionEvent.ACTION_DOWN) {
        if (dragStarted && pointerCount > 1) {
            dragStarted = false;
            Log.d("MAPINFOTOOL", "drag stopped");
            isPinching = true;
        }
        else if(this.Shape_Selection.equals(array[2])){
        	//Use this to capture points for polygonal selection
    		endX = event.getX();
    		endY = event.getY();
    		Coordinates new_point = new Coordinates(endX,endY);
        	Singleton_Polygon_Points.getInstance().setPoint(new_point);
        	Log.d("MAPINFOTOOL", "Point " + endX + " " + endY); 
        	dragStarted = false;
        	return false;
        }
    }
    if (action == MotionEvent.ACTION_MOVE) {
        if (pointerCount > 1 || isPinching) {
            dragStarted = false;
            Log.d("MAPINFOTOOL", "drag stopped");
            return false;
        }
        // START DRAGGING
        if (!dragStarted) {
            startX = event.getX();
            startY = event.getY();
        }
        dragStarted = true;
        Log.d("MAPINFOTOOL", "dragging started");
        endX = event.getX();
        endY = event.getY();

        // Force redraw
        view.redraw();
        return true;

    } else if (dragStarted && action == MotionEvent.ACTION_UP) {
        if (pointerCount > 1 /*&& !Shape_Selection.equals(array[2])*/) {
            isPinching = true;
            dragStarted = false;
            Log.d("MAPINFOTOOL", "drag stopped");
            return false;
        } else if (isPinching && !Shape_Selection.equals(array[2])) {
            isPinching = false;
            dragStarted = false;
            Log.d("MAPINFOTOOL", "drag stopped");
            return false;
        }
 
        endX = event.getX();
        endY = event.getY();
        if (endX == startX || endY == startY) {
            dragStarted = false;
            isPinching = false;
            return false;
        }
        // END DRAGGING EVENT
        dragStarted = false;
        Log.d("MAPINFOTOOL", "drag stopped");
        Log.d("MAPINFOTOOL", "start query layer");

        // TODO use an utility class for
        // this operations
        MapPosition mapPosition = this.view.getMapViewPosition()
                .getMapPosition();
        byte zoomLevel = view.getMapViewPosition().getZoomLevel();
        GeoPoint geoPoint = mapPosition.geoPoint;
        
        double pixelLeft = MercatorProjection.longitudeToPixelX(
                geoPoint.longitude, mapPosition.zoomLevel);
        double pixelTop = MercatorProjection.latitudeToPixelY(
                geoPoint.latitude, mapPosition.zoomLevel);
        pixelLeft -= view.getWidth() >> 1;
        pixelTop -= view.getHeight() >> 1;
        
        double x = 0, y = 0, radius = 0, n = 0, s = 0, e = 0, w = 0;
        boolean circle_choosed = false;
        
        if(pref.getString("selectionShape", "").equals(array[1])){  	
            //Calculate radius and coordinates of circle
        	x = MercatorProjection.pixelXToLongitude(pixelLeft + startX, zoomLevel);
            y = MercatorProjection.pixelYToLatitude(pixelTop + startY, zoomLevel);
            double fin_x = MercatorProjection.pixelXToLongitude(pixelLeft + endX, zoomLevel);
            double fin_y = MercatorProjection.pixelYToLatitude(pixelTop + endY, zoomLevel);
            double rad_x = Math.abs(x-fin_x); 
            double rad_y = Math.abs(y-fin_y);
            radius = Math.sqrt( (rad_x*rad_x) + (rad_y*rad_y));
            circle_choosed = true;
        }
        
        else if(pref.getString("selectionShape", "").equals(array[0])){
        	n = MercatorProjection.pixelYToLatitude(pixelTop + startY,
                    zoomLevel);
            w = MercatorProjection.pixelXToLongitude(pixelLeft + startX,
                    zoomLevel);
            s = MercatorProjection.pixelYToLatitude(pixelTop + endY,
                    zoomLevel);
            e = MercatorProjection.pixelXToLongitude(pixelLeft + endX,
                    zoomLevel);
            Log.v("MAPINFOTOOL", "bbox:" + w + "," + s + "," + e + "," + n);
        }
        
        // TODO Allow to draw a rectangle
        if (!infoTaskLaunched && circle_choosed) {
            // infoTaskLaunched=true;
        	infoDialogCircle(x,y,radius);
        }
        else
            infoDialog(n, w, s, e);
        
    }
    return false;
}

public boolean isDragStarted(){
    return dragStarted;
}

public float getStartX() {
    return startX;
}

public float getStartY() {
    return startY;
}

public float getEndX() {
    return endX;
}

public float getEndY() {
    return endY;
}

public int getMode() {
    return mode;
}

public void setMode(int mode) {
    this.mode = mode;
}

/** ATTENZIONE PUO' ESSERE ELIMINATO, USA LE PREFERENZE! ANZICHE' QST METODO.
 * Method used by extern to update shape of selection when user chooses one different from current choice
 * @param shape
 */
public void updateShapeSelection(String shape){
	this.Shape_Selection = shape;
}

/**
 * Return a point of polygonal selection stored by singleton class.
 * @param index
 * @return
 */
public Coordinates getPolygonPoint(int index){
	return Singleton_Polygon_Points.getInstance().getPoint(index);
}

}