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
import it.geosolutions.android.map.utils.OverlayManager;

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
	private SharedPreferences pref;
	private String[] array;
	
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
		pref = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
		array = activity.getResources().getStringArray(R.array.preferences_selection_shape);
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
		pref = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
		array = activity.getResources().getStringArray(R.array.preferences_selection_shape);
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
			//If user chooses one point selection select listener for one tap event
			OnTouchListener tl = null;
			if(pref.getString("selectionShape", "").equals(array[2])){
				tl = cl.getOneTapListener();
			}
			else 
				if(pref.getString("selectionShape", "").equals(array[3])){
					tl = cl.getPolygonTapListener();
				}
				else 
					tl = cl.getMapListener();
			
			if(cl.isEnabled() && tl !=null){
				//if one controller returns true the event is not propagated to the map
				catched = tl.onTouch(this, motionEvent) || catched;
			}
		}
		if(!catched)
			 touchResult = super.onTouchEvent(motionEvent);
		
		return touchResult || catched;
	}
	
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
		Log.v("MapView","Redraw start");
		if(activity!=null){
			activity.setSupportProgressBarIndeterminateVisibility(true);
		}
	}
	@Override
	protected void loadStop() {
		Log.v("MapView","Redraw stop");
		if(activity!=null){
			
			activity.setSupportProgressBarIndeterminateVisibility(false);
		}
	}
}