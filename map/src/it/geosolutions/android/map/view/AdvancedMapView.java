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

import it.geosolutions.android.map.control.MapControl;
import it.geosolutions.android.map.overlay.FreezableOverlay;
import it.geosolutions.android.map.overlay.MarkerOverlay;

import java.util.ArrayList;
import java.util.List;

import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.overlay.Overlay;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.MotionEvent;

public class AdvancedMapView extends MapView {
	protected List<MapControl> controls =  new ArrayList<MapControl>();
	
	public AdvancedMapView(Context context) {
		super(context);		
	}
	public  AdvancedMapView(Context context, AttributeSet attributeSet){
		super(context,attributeSet);
	}
	public void addControl(MapControl m){
		controls.add(m);
		Log.v("CONTROL","total controls:"+controls.size());
	}
	public void removeControl(MapControl m){
		if(controls.contains(m)){
			controls.remove(m);
		}
		
	}
	
	public List<MapControl> getControls(){
		return controls;
	}
	/**
	 * Drows the map and the controls optional functions
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
	 * Extend the touch event with other events from the controllers
	 */
	@Override
	public boolean onTouchEvent(MotionEvent motionEvent) {
		boolean catched = false;
		boolean touchResult =true;
		for(MapControl cl : controls){
			OnTouchListener tl = cl.getMapListener();
			if(cl.isEnabled() && tl !=null){
				//if one controller returns true the event is not propagated to the map
				catched = tl.onTouch(this, motionEvent) || catched;
			}
		}
		
		if(!catched){
			 touchResult = super.onTouchEvent(motionEvent);
		}
		

		return touchResult || catched;
	}
	
	/**
	 * Method to propagate ti map double tap event
	 * @param event
	 * @return
	 */
	/*public boolean onDoubleTapEvent(MotionEvent event){
		boolean catched = false;
		boolean doubleTapResult =true;
		for(MapControl cl : controls){
			OnDoubleTapListener gl = cl.getDoubleTapListener();
			if(cl.isEnabled() &&  gl!=null){
				//if one controller returns true the event is not propagated to the map
				catched = gl.onDoubleTap(event) || catched;
			}
		}
		
		return doubleTapResult || catched; //Check su ritorno solo di catched
	}*/
	
	/**
	 * Workaround for getting proper overlay
	 */
	public MarkerOverlay getMarkerOverlay(){
		for(Overlay ov : getOverlays()){
			if(ov instanceof MarkerOverlay){
				return (MarkerOverlay) ov;
			}
		}
		return null;
		
	}
	public void freezeOverlays(){
	    for(Overlay o:getOverlays()){
	        if (o instanceof FreezableOverlay) {
	            ((FreezableOverlay) o).freeze();
                    
                }
	    }
	}
	public void thawOverlays(){
	    for(Overlay o:getOverlays()){
                if (o instanceof FreezableOverlay) {
                    ((FreezableOverlay) o).thaw();
                    
                }
            }
	}
}