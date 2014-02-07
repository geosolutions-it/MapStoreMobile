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

import eu.geopaparazzi.spatialite.database.spatial.core.SpatialVectorTable;

import it.geosolutions.android.map.activities.GetFeatureInfoLayerListActivity;
import it.geosolutions.android.map.common.Constants;
import it.geosolutions.android.map.common.Constants.Modes;
import it.geosolutions.android.map.database.SpatialDataSourceManager;
import it.geosolutions.android.map.model.Layer;
import it.geosolutions.android.map.model.query.PolygonQuery;
import it.geosolutions.android.map.overlay.managers.MultiSourceOverlayManager;
import it.geosolutions.android.map.style.AdvancedStyle;
import it.geosolutions.android.map.style.StyleManager;
import it.geosolutions.android.map.utils.ConversionUtilities;
import it.geosolutions.android.map.utils.StyleUtils;
import it.geosolutions.android.map.utils.Coordinates.Coordinates;
import it.geosolutions.android.map.utils.Coordinates.Coordinates_Query;
import it.geosolutions.android.map.view.AdvancedMapView;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * Listener to implements double tap event on map.
 * @author Jacopo Pianigiani (jacopo.pianigiani85@gmail.com).
 */
public class PolygonTapListener implements OnGestureListener, OnDoubleTapListener, OnTouchListener{		
	private int mode = Modes.MODE_EDIT;

	private AdvancedMapView view;
	private Activity activity;
	private GestureDetector gd;
	
	private ArrayList<Coordinates> points; //Store coordinates of points touched on map.	
	private boolean pointsAcquired, acquisitionStarted; 	
	private Coordinates new_point;
	private float startX, startY;
	
	/**
	 * Constructor for class PolygonTapListener
	 * @param view
	 * @param activity
	 */
	public PolygonTapListener(AdvancedMapView view, Activity activity){
		this.view = view;
		this.activity = activity;
		pointsAcquired = false;
    	acquisitionStarted = true;
		points = new ArrayList<Coordinates>();		
		gd = new GestureDetector(view.getContext(),this);
	}
		
	/**
	 * Create a new ArrayList with points captured converted from pixel to latitude/longitude, ready for query
	 */
	public void preparePoints(){
		Coordinates point;
		Coordinates_Query to_add;
		
		//It will contain long/lat of the points that will be used to query on spatialite database
		ArrayList<Coordinates_Query> polygon_points = new ArrayList<Coordinates_Query>();
        for(int i = 0; i<points.size()-1; i++){ 
        	//Exclude last point because with double tap it will be captured twice
        	point = points.get(i);
        	to_add = new Coordinates_Query(point.getX(),point.getY());
        	polygon_points.add(to_add);
        }
        
        infoDialogPolygon(polygon_points,view.getMapViewPosition().getZoomLevel());
	}
	
	/**
	 * Create a Feature Query for polygonal selection and passes to an GetFeatureInfoLayerListActivity via intent.
	 * @param polygon_points
	 * @param zoomLevel
	 */
	private void infoDialogPolygon(final ArrayList<Coordinates_Query> polygon_points,byte zoomLevel){
		try {
			ArrayList<Layer> layers = getLayers();
	        Intent i = new Intent(view.getContext(),
	                GetFeatureInfoLayerListActivity.class); 
	        i.putExtra(Constants.ParamKeys.LAYERS, layers);
	        PolygonQuery query = new PolygonQuery();
	        query.setPolygonPoints(polygon_points);
	        query.setSrid("4326");
	        query.setZoomLevel(zoomLevel);
	        i.putExtra("query", query);
	        if (mode == Modes.MODE_EDIT) {
	            i.setAction(Intent.ACTION_PICK);
	        } else {
	            i.setAction(Intent.ACTION_VIEW);
	        }
	        activity.startActivityForResult(i,
	                GetFeatureInfoLayerListActivity.POLYGON_REQUEST);
	    } catch (Exception ex) {
	        Log.e("Exception launched", ex.getMessage());
	    }
		
		reset();
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
	
	/**
	 * Return mode value
	 * @return
	 */
	public int getMode() {
	    return mode;
	}

	/**
	 * Set mode
	 * @param mode
	 */
	public void setMode(int mode) {
	    this.mode = mode;
	}
		
	/**
	 * Return true if a double tap event has been captured and all points of a polygonal selection
	 * are available.
	 * @return
	 */
	public boolean isPointsAcquired(){
		return pointsAcquired;
	}
	
	/**
	 * Return true if acquisition of points is started, otherwise return false.
	 * @return
	 */
	public boolean isAcquisitionStarted(){
		return acquisitionStarted;
	}

	@Override
	public boolean onDoubleTap(MotionEvent arg0) {
		pointsAcquired = true;
		preparePoints();
        return false;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onDown(MotionEvent event) {
		//Use this to capture points for polygonal selection
		startX = event.getX();
		startY = event.getY();
		
		//Convert coordinates of point in longitude/latitude.
		new_point = new Coordinates(ConversionUtilities.convertFromPixelsToLongitude(view, startX),
				ConversionUtilities.convertFromPixelsToLatitude(view, startY));
    	return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e){}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e){}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		points.add(new_point);
		acquisitionStarted = true;
		view.redraw(false);
		return true;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event){
		return gd.onTouchEvent(event);
	}
	
	/**
	 * Return number of points that are currently acquired
	 * @return
	 */
	public int getNumberOfPoints(){
		return points.size();
	}
	
	/**
	 * Return x coordinate(longitude) for a point of selection.
	 * @param index
	 * @return
	 */
	public double getXPoint(int index){
		return points.get(index).getX();
	}
	
	/**
	 * Return y coordinate(latitude) for a point of selection.
	 * @param index
	 * @return
	 */
	public double getYPoint(int index){
		return points.get(index).getY();
	}
	
	/**
	 * Clear collection of point and restore initial configuration by selection
	 */
	public void reset(){
		points.clear();     
		pointsAcquired = false;
		acquisitionStarted = false;
		view.redraw(false);
	}
}