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
package it.geosolutions.android.map.control;

import it.geosolutions.android.map.R;
import it.geosolutions.android.map.activities.FeatureDetailsActivity;
import it.geosolutions.android.map.model.Feature;
import it.geosolutions.android.map.overlay.MarkerOverlay;
import it.geosolutions.android.map.overlay.items.DescribedMarker;
import it.geosolutions.android.map.view.AdvancedMapView;

import org.mapsforge.core.model.Point;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Vibrator;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A control to implement the marker operations.
 * This control manages Selection and dragging funtionalities
 * @author Lorenzo Natali (www.geo-solutions.it)
 *
 */
public class MarkerControl extends MapControl implements OnTouchListener, OnGestureListener,OnClickListener {
	
	private DescribedMarker selectedMarker;
	private DescribedMarker originalMarker;
	final GestureDetector gestureDetector;
	private double x;
	private double y;
	private double endX;
	private double endY;
	private Point originalMarkerPoint;
	private boolean startedDraggingSelection;
	private boolean isDragging;
	private ImageButton infoButton;
	//CONSTANTS
	public static final int MODE_VIEW=0;
	public static final int MODE_EDIT=1;

	/**
	 * @param view
	 */
	public MarkerControl(AdvancedMapView view) {
		super(view);
		gestureDetector = new GestureDetector(view.getContext(),this);
		mapListener=this;
		
	}
	
	public MarkerControl(AdvancedMapView view,boolean enabled){
		super(view,enabled);
		
		gestureDetector = new GestureDetector(view.getContext(),this);
		mapListener=this;
	}

	
	@Override
	public void draw(Canvas canvas) {
		if(isDragging){
		    //this code is useful if we want to cache also 
		    //the marker layer
//		    MapViewPosition mp= view.getMapViewPosition();
//		    BoundingBox boundingBox = mp.getBoundingBox();
//		    byte zoomLevel = mp.getZoomLevel();
//		    double canvasPixelLeft = MercatorProjection.longitudeToPixelX(boundingBox.minLongitude, zoomLevel);
//		    double canvasPixelTop = MercatorProjection.latitudeToPixelY(boundingBox.maxLatitude, zoomLevel);
//		    Point canvasPosition = new Point(canvasPixelLeft, canvasPixelTop);
//		    selectedMarker.draw( boundingBox ,zoomLevel, canvas,canvasPosition);
		}

	}

	
	@Override
	public boolean onTouch(View view, MotionEvent event ) {
		if(!isDragging){
			return gestureDetector.onTouchEvent(event);
		}else{
			return manageDragEvent(view, event);
		}
		
		
	}

	/**
	 * This method is used when the drag event is activated on LongPress
	 * event detected on a marker.
	 * @param view
	 * @param event
	 */
	private boolean manageDragEvent(View view, MotionEvent event) {
		int action =event.getAction();
		switch(action){
			case MotionEvent.ACTION_UP:
				isDragging=false;
				this.view.thawOverlays();
				promptChangeFeature();
				return false;
			case MotionEvent.ACTION_MOVE:
			        this.view.freezeOverlays();
				move(event);
				return true;
			
		}
		return false;
		
	}

        /**
         * prompt a dialog to change the associated feature
         */
        private void promptChangeFeature() {
            // TODO prompt a dialog asking in you want to change
            // the feature
            //if yes open the getFeatureInfoLayerListActivty
            //with a proper bounding box for result
            
        
        }

    /**
	 * @param event
	 */
	private void move(MotionEvent event) {
		endX=event.getX();
		endY=event.getY();
		double dx = endX-x;
		double dy = endY-y;
		/*TODO improve this:
		 * this operation is expensive (many coordinate conversion,create a new Geopoint on every
		 * move event. Evaluate the possibility of:
		 * * remove the marker from the marker overlay
		 * * get the drowable and draw it using the draw method
		 * * put the marker on the Marker overlay on ACTION_UP
		 */
		selectedMarker.setXY(originalMarkerPoint.x+dx, originalMarkerPoint.y+dy, view.getMapViewPosition(), null);
		view.redraw();
		
		
	}

	/**
	 * 
	 */
	private void onMarkerUnselected() {
		if(infoButton!=null){
			((LinearLayout) infoButton.getParent()).setVisibility(View.GONE);
		}
	}

	/**
	 * 
	 */
	private void onMarkerSelected(DescribedMarker dm) {
		selectMarker(dm);
		if(infoButton!=null){
			((LinearLayout) infoButton.getParent()).setVisibility(View.VISIBLE);
		}
		
		
	}
	
	 
	
	/**
	 * wrap selecting one marker at time
	 * @param dm
	 */
	public void selectMarker(DescribedMarker dm) {
		if(selectedMarker !=null){
			selectedMarker.highlightOff();
		}
		selectedMarker=dm;
		dm.highlightOn();
		//show the button
		// <include layout="@layout/marker_info_button"></include>		
		if(infoButton!=null){
			((LinearLayout) infoButton.getParent()).setVisibility(View.VISIBLE);
		}
	}

	@Override
	public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX,
			float velocityY) {
		// Not managed now
		return false;
	}

	@Override
	public boolean onDown(MotionEvent event) {
		x = event.getX(0);
		y = event.getY(0);
		startedDraggingSelection = true;
		return false;
	}
	
	@Override
	public void onLongPress(MotionEvent event) {
	    if(mode!=MODE_EDIT){
	        return;
	    }
		Log.v("EVENT", "onLongPress");	 
        
    	    if(event.getPointerCount()==1){
    		float newX= event.getX(0);
	        float newY =event.getY(0);
	        
    		if(x==newX && y==newY && startedDraggingSelection){
    			startedDraggingSelection = false;
	    		Log.v("EVENT","LONG PRESS DETECTEED!");
	    		
	    		    startDragging(newX,newY);
	    		
	    		
    		}
    		
    	}
		
	}

	
	/**
	 * Operations to do when the dragging operation start
	 * if a marker is under the longpress event, the marker is copied
	 * and the listener goes in dragging mode. A Vibration notify 
	 * the user the dragging operation is started.
	 */
	private void startDragging(float x, float y) {
		MarkerOverlay mo = view.getMarkerOverlay();
		if(mo==null){
		    return;
		}
		DescribedMarker dm = mo.queryPixel(view.getMapViewPosition(),view.getProjection(), x, y);
		if(dm !=null){
			selectMarker(dm);
			isDragging=true;
			//TODO make the marker clonable instead of copying every element
			originalMarker = new DescribedMarker(dm.getGeoPoint(),dm.getDrawable());
			originalMarker.setId(dm.getDescription());
			originalMarker.setDescription(dm.getDescription());
			originalMarker.setDrawable(dm.getDrawable());
			originalMarkerPoint = selectedMarker.getXY(view.getMapViewPosition(), null);
			//notify selection with vibration
			Vibrator vibe = (Vibrator) view.getContext().getSystemService(Context.VIBRATOR_SERVICE) ;
			vibe.vibrate(50); 
			Toast.makeText(view.getContext(), R.string.map_marker_drag_suggestion, Toast.LENGTH_SHORT).show();
		}
		
		
	}

	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// not managed now
		return false;
	}

	
	@Override
	public void onShowPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Manages the single tap for select markers
	 */
	@Override
	public boolean onSingleTapUp(MotionEvent event) {
	        MarkerOverlay mo = view.getMarkerOverlay();
	        if(mo==null){
	            return false;
	        }
		DescribedMarker dm = view.getMarkerOverlay().queryPixel(view.getMapViewPosition(),view.getProjection(), event.getX(), event.getY());
		if(dm !=null){
			if(!dm.isHighlight()){
				dm.highlightOn();
				view.redraw();
				onMarkerSelected(dm);
			}else{
				dm.highlightOff();
				onMarkerUnselected();
				view.redraw();
			}
		}else{
		    return false;
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see it.geosolutions.android.map.control.MapControl#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(boolean enabled) {
		// TODO Auto-generated method stub
		super.setEnabled(enabled);
		if(enabled){
			//this message is on creation, it should not be shown when create, but when display the map
			//Toast.makeText(view.getContext(), R.string.map_marker_enable_suggestion, Toast.LENGTH_LONG).show();
		}else{
			if(view.getMarkerOverlay()!=null){
			    view.getMarkerOverlay().resetHighlight();
			}
			view.redraw();
			if(infoButton!=null){
				((LinearLayout) infoButton.getParent()).setVisibility(View.GONE);
			}
		}
		
	}
	
	// Getters and Setters
	
	public ImageButton getInfoButton() {
		return infoButton;
	}

	public void setInfoButton(ImageButton infoButton) {
		this.infoButton = infoButton;
		if(infoButton!=null){
			infoButton.setOnClickListener(this);
		}
	}

	/**
	 * Used for click on info button
	 */
	@Override
	public void onClick(View v) {
		if(this.selectedMarker!=null){
			final Dialog dialog=new Dialog(view.getContext());
			dialog.setContentView(R.layout.marker_info);
			dialog.setTitle(selectedMarker.getId());
			TextView text = (TextView) dialog.findViewById(R.id.marker_info_text);
			text.setText(selectedMarker.getDescription());
			ImageView image = (ImageView) dialog.findViewById(R.id.marker_info_image);
			//clone the drawable to avoid bound change
			image.setImageDrawable(selectedMarker.getDrawable().getConstantState().newDrawable());
			image.setOnClickListener(new OnClickListener() {
                            
                            @Override
                            public void onClick(View v) {
                                showFeatureDetails(selectedMarker.getFeature());
                                
                            }
                        });
			
			dialog.setCancelable(true);
			dialog.setCanceledOnTouchOutside(true);
			dialog.show();
		}
		
	}

    /**
     * @param feature
     */
    protected void showFeatureDetails(Feature feature) {
        Intent i = new Intent(view.getContext(), FeatureDetailsActivity.class);
        i.putParcelableArrayListExtra("feature", feature);
        view.getContext().startActivity(i);
        
    }

	@Override
	public void refreshControl(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		
	}
	


}
