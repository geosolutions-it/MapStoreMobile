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

import it.geosolutions.android.map.activities.GetFeatureInfoLayerListActivity;
import it.geosolutions.android.map.database.SpatialDataSourceManager;
import it.geosolutions.android.map.model.FeatureInfoQuery;
import it.geosolutions.android.map.style.AdvancedStyle;
import it.geosolutions.android.map.style.StyleManager;
import it.geosolutions.android.map.utils.StyleUtils;
import it.geosolutions.android.map.view.AdvancedMapView;

import java.util.ArrayList;
import java.util.List;

import org.mapsforge.core.model.GeoPoint;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.core.util.MercatorProjection;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import eu.geopaparazzi.spatialite.database.spatial.core.SpatialVectorTable;

/**
 * TODO
 * 
 * @author Lorenzo Natali
 */
public class MapInfoListener implements OnTouchListener {
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

public MapInfoListener(AdvancedMapView mapView, Activity activity) {
    view = mapView;
    this.activity = activity;
}

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

@Override
public boolean onTouch(View v, MotionEvent event) {
    int action = event.getAction();
    int pointerCount = event.getPointerCount();
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
        if (pointerCount > 1) {
            isPinching = true;
            dragStarted = false;
            Log.d("MAPINFOTOOL", "drag stopped");
            return false;
        } else if (isPinching) {
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
        double n = MercatorProjection.pixelYToLatitude(pixelTop + startY,
                zoomLevel);
        double w = MercatorProjection.pixelXToLongitude(pixelLeft + startX,
                zoomLevel);
        double s = MercatorProjection.pixelYToLatitude(pixelTop + endY,
                zoomLevel);
        double e = MercatorProjection.pixelXToLongitude(pixelLeft + endX,
                zoomLevel);
        Log.v("MAPINFOTOOL", "bbox:" + w + "," + s + "," + e + "," + n);
        // TODO Allow to draw a rectangle
        if (!infoTaskLaunched) {
            // infoTaskLaunched=true;
            infoDialog(n, w, s, e);
        }

    }
    return false;
}

public boolean isDragStarted() {
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

}
