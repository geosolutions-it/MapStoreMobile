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
package it.geosolutions.android.map.listeners;

import java.util.ArrayList;
import java.util.List;

import org.mapsforge.core.model.GeoPoint;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.core.util.MercatorProjection;

import eu.geopaparazzi.spatialite.database.spatial.core.SpatialVectorTable;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import it.geosolutions.android.map.activities.GetFeatureInfoLayerListActivity;
import it.geosolutions.android.map.common.Constants;
import it.geosolutions.android.map.database.SpatialDataSourceManager;
import it.geosolutions.android.map.model.Layer;
import it.geosolutions.android.map.model.query.CircleQuery;
import it.geosolutions.android.map.overlay.managers.MultiSourceOverlayManager;
import it.geosolutions.android.map.style.AdvancedStyle;
import it.geosolutions.android.map.style.StyleManager;
import it.geosolutions.android.map.utils.StyleUtils;
import it.geosolutions.android.map.view.AdvancedMapView;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

import android.preference.PreferenceManager;
import android.util.Log;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * Listener with a gesture detector for one point selection on map.
 * @author Jacopo Pianigiani (jacopo.pianigiani85@gmail.com).
 */
public class OneTapListener implements OnTouchListener, OnGestureListener {	
	// MODES

	private int mode = Constants.Modes.MODE_EDIT;
	
	private AdvancedMapView view;
	private Activity activity;
	private boolean pointsAcquired;
	private SharedPreferences pref;
	private float startX, startY, radius_pixel;
	
	private GestureDetector gd;
	
	/**
	 * Constructor for class OneTapListener.
	 * @param view
	 * @param activity
	 */
	public OneTapListener(AdvancedMapView view, Activity activity){
		this.view = view;
		this.activity = activity;
		pointsAcquired = false;		
		pref = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
		gd = new GestureDetector(view.getContext(),this);
	}
	
	/**
	 * Return x coordinate on map to drawing.
	 * @return
	 */
	public float getStartX(){
		return startX;
	}
	
	/**
	 * Return y coordinate on map to drawing.
	 * @return
	 */
	public float getStartY(){
		return startY;
	}
	
	/**
	 * Return radius of selection to draw a circle on map.
	 * @return
	 */
	public float getRadius(){
		return radius_pixel;
	}
	
	/**
	 * Calculate radius of one point selection and convert coordinates from pixel to long/lat.
	 */
	public void query_layer(){
		if(!pointsAcquired) return;
		
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
        
        double x = 0.0 , y = 0.0, radius = 0.0, fin_x = 0.0;      
    	x = MercatorProjection.pixelXToLongitude(pixelLeft + startX, zoomLevel);
        y = MercatorProjection.pixelYToLatitude(pixelTop + startY, zoomLevel);
        
        //Calculate radius of one point selection
        radius_pixel = (float)pref.getInt("OnePointSelectionRadius", 10);
        fin_x = MercatorProjection.pixelXToLongitude(pixelLeft + startX + radius_pixel, zoomLevel);
        radius = Math.abs(fin_x - x);
    	
        Log.v("MAPINFOTOOL", "circle: center (" + x + "," + y + ") radius " + radius);
    	infoDialogCircle(x,y,radius);
	}
	
	/**
	 * Create a Feature Query for one point selection and pass it to an activity via intent.
	 * @param x
	 * @param y
	 * @param radius
	 */
	private void infoDialogCircle(final double x, final double y, final double radius){
	       try{
    	    ArrayList<Layer> layerNames = getLayers();
	        Intent i = new Intent(view.getContext(),
	                GetFeatureInfoLayerListActivity.class);
	        i.putExtra(Constants.ParamKeys.LAYERS, layerNames);
	        CircleQuery query = new CircleQuery();
	        query.setX(x);
	        query.setY(y);
	        query.setRadius(radius);
	        query.setSrid("4326");
	        i.putExtra("query", query);
	        i.putExtra("selection","Circular");
	        if (mode == Constants.Modes.MODE_EDIT) {
	            i.setAction(Intent.ACTION_PICK);
	        } else {
	            i.setAction(Intent.ACTION_VIEW);
	        }
	        activity.startActivityForResult(i,
	                GetFeatureInfoLayerListActivity.CIRCLE_REQUEST);
	        }catch(Exception ex){
	        	Log.e("Exception launched ", ex.getMessage());
	        }
	}

	/**
	 * Get layers from the mapView
	 * @return an arrayList of layers
	 */
	private ArrayList<Layer> getLayers() {
		MultiSourceOverlayManager manager =  view.getLayerManager();
		ArrayList<Layer> layers = manager.getLayers();
		ArrayList<Layer> result =new ArrayList<Layer>();
		for(Layer layer:layers){
			if(layer.isVisibility()){
				result.add(layer);
			}
		}
		return result;
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		radius_pixel = (float)pref.getInt("OnePointSelectionRadius", 10);
		return gd.onTouchEvent(event);
	}

	/**
	 * Return mode.
	 * @return
	 */
	public int getMode() {
	    return mode;
	}

	/**
	 * Set mode.
	 * @param mode
	 */
	public void setMode(int mode) {
	    this.mode = mode;
	}
	
	/**
	 * Return true if points have been acquired, false otherwise
	 * @return
	 */
	public boolean pointsAcquired(){
		return pointsAcquired;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		startX = e.getX();
        startY = e.getY();
       	view.redraw(false);
       	pointsAcquired = true;		
       	return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		query_layer();
		pointsAcquired = false;
		view.redraw(false);
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
	    pointsAcquired = false;
	    
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		query_layer();
		pointsAcquired = false;
		view.redraw(false);
		
		return true;
	}
}