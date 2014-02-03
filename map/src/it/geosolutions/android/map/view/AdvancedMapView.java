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
package it.geosolutions.android.map.view;

import it.geosolutions.android.map.MapsActivity;
import it.geosolutions.android.map.R;
import it.geosolutions.android.map.control.MapControl;
import it.geosolutions.android.map.overlay.FreezableOverlay;
import it.geosolutions.android.map.overlay.MarkerOverlay;
import it.geosolutions.android.map.overlay.managers.OverlayManager;

import java.util.ArrayList;
import java.util.List;

import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.overlay.Overlay;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * This class extends the <MapView> adding the management of the <MapControl> objects.
 * @author  Lorenzo Natali.
 */
public class AdvancedMapView extends MapView {
	protected List<MapControl> controls =  new ArrayList<MapControl>();
	protected MapsActivity activity;
	public OverlayManager overlayManger;
	
	public OverlayManager getOverlayManger() {
		return overlayManger;
	}

	public void setOverlayManger(OverlayManager overlayManger) {
		this.overlayManger = overlayManger;
	}

	public AdvancedMapView(Context context) {
		super(context);
		//get reference to mapsActivity for actionbar support
		if(context instanceof MapsActivity){
			activity = (MapsActivity) context; 
		}

	}
	
	/**
	 * Constructor
	 * @param context the Activity (must implement <MapActivity> interface)
	 * @param attributeSet the attributeSet
	 */
	public  AdvancedMapView(Context context, AttributeSet attributeSet){
		super(context,attributeSet);
		if(context instanceof MapsActivity){
			activity = (MapsActivity) context; 
		}

	}
	
	/**
	 * Add a <MapControl> object to the controls of the map
	 * @param m the <MapControl> object
	 */
	public void addControl(MapControl m){
		controls.add(m);
		Log.v("CONTROL","total controls:"+controls.size());
	}
	
	/**
	 * remove the passed <MapControl> from the controls 
	 * @param m the control to remove
	 */
	public void removeControl(MapControl m){
		if(controls.contains(m)){
			controls.remove(m);
		}
	}
	
	/**
	 * Get the list of <MapControl> binded to the map.
	 * @return
	 */
	public List<MapControl> getControls(){
		return controls;
	}
	
	/**
	 * Draws the map and the controls optional functions
	 */
	@Override
	public  void onDraw(Canvas canvas){
		super.onDraw(canvas);
		for(MapControl c : controls){
			if(c.isEnabled()){
				c.draw(canvas);
			}
		}
	}
	
	/**
	 * Extend the touch event with other events from the controllers.
	 * The controls have a associated MapListener.
	 * If this listener catch the event (onTouch method returns true) the event is not propagated 
	 * to the map.
	 */
	@Override
	public boolean onTouchEvent(MotionEvent motionEvent) {
		boolean catched = false;
		boolean touchResult =true;
		for(MapControl cl : controls){
			if(cl==null) continue;
			//get the OnTouchListener from the controller
			OnTouchListener tl = cl.getMapListener();
			//call the event
			if(cl.isEnabled() && tl !=null){
				catched = tl.onTouch(this, motionEvent) || catched;
			}
		}
		//if the event was catched the result is not propagated to the map
		if(!catched)
			 touchResult = super.onTouchEvent(motionEvent);
		
		return touchResult || catched;
	}
	
	/**
	 * Workaround for getting proper overlay
	 */
	public MarkerOverlay getMarkerOverlay(){
		return overlayManger.getMarkerOverlay();
		
	}
	
	/**
	 * Freeze all the <FreezableOverlay> overlays 
	 */
	public void freezeOverlays(){
	    for(Overlay o:getOverlays()){
	        if (o instanceof FreezableOverlay) {
	            ((FreezableOverlay) o).freeze();
                    
                }
	    }
	}
	
	/**
	 * Thaws all the <FreezableOverlay> overlays
	 */
	public void thawOverlays(){
	    for(Overlay o:getOverlays()){
                if (o instanceof FreezableOverlay) {
                    ((FreezableOverlay) o).thaw();
                    
                }
            }
	}

	@Override
	protected void loadStart() {
		if(activity!=null){
			activity.setSupportProgressBarIndeterminateVisibility(true);
		}
	}
	@Override
	protected void loadStop() {
		if(activity!=null){
			
			activity.setSupportProgressBarIndeterminateVisibility(false);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.mapsforge.android.maps.MapView#redraw()
	 */
	@Override
	public void redraw() {
		// TODO Auto-generated method stub
		super.redraw();
	}
}
