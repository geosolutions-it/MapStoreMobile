package it.geosolutions.android.map.listeners;

import it.geosolutions.android.map.overlay.SpatialiteOverlay;
import it.geosolutions.android.map.view.AdvancedMapView;
import android.os.SystemClock;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class LongPressListener implements OnTouchListener, OnGestureListener {

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
	@Override
	public void onLongPress(MotionEvent event) {
        Log.v("EVENT", "onLongPress");	 
        
    	if(event.getPointerCount()==1){
    		float newX= event.getX(0);
	        float newY =event.getY(0);
	        
    		if(x==newX && y==newY && startedDraggingSelection){
    			startedDraggingSelection = false;
	    		Log.v("EVENT","LONG PRESS DETECTEED!");
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
