/*
 * GeoSolutions Android Map Library - Digital field mapping on Android based devices
 * Copyright (C) 2013 - 2014  GeoSolutions (www.geo-solutions.it)
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

import it.geosolutions.android.map.BuildConfig;
import it.geosolutions.android.map.overlay.SpatialiteOverlay;
import it.geosolutions.android.map.view.AdvancedMapView;
import android.os.SystemClock;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * Generic LongPress Listener
 * 
 * 
 * @author Lorenzo Pini (lorenzo.pini@geo-solutions.it)
 *
 */
public class LongPressListener implements OnTouchListener, OnGestureListener {

	/**
	 * TAG for logging
	 */
	private static final String TAG = "LongPressListener";
	
	private float x;
	private float y;
	private boolean startedDraggingSelection;
	private long time;
	private AdvancedMapView view;
	private SpatialiteOverlay overlay;
	final GestureDetector gestureDetector;
	public LongPressListener(AdvancedMapView mapView) {
		gestureDetector = new GestureDetector(mapView.getContext(),this);
		this.view = mapView;
		
	}

	@Override
	public boolean onTouch(View mapView, MotionEvent event) {
		return gestureDetector.onTouchEvent(event);
		//onTouchEvent(event);
		//return false;
	}

	public boolean onTouchEvent(MotionEvent event) {
		return gestureDetector.onTouchEvent(event);

	}
	
	/**
	 * Checks if the given {@link MotionEvent} is a single point long press
	 * If it is, enter in a "dragging" state
	 */
	@Override
	public void onLongPress(MotionEvent event) {
		if(BuildConfig.DEBUG){
			Log.v(TAG, "onLongPress");
		}
        
    	if(event.getPointerCount()==1){
    		float newX= event.getX(0);
	        float newY =event.getY(0);
	        
    		if(x==newX && y==newY && startedDraggingSelection){
    			startedDraggingSelection = false;
    			if(BuildConfig.DEBUG){
	    			Log.v(TAG,"LONG PRESS DETECTED!");
	    		}
    			//TODO check if marker under the mouse and allow drag and drop
    		}
    		
    	}
    }
    	
	@Override
    public boolean onDown(MotionEvent event){
    	x = event.getX(0);
		y = event.getY(0);
		time =SystemClock.uptimeMillis();
		startedDraggingSelection = true;
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		startedDraggingSelection = false;
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		//started = false;
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		startedDraggingSelection = false;
		return false;
	}
	

}
