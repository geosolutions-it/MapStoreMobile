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

import it.geosolutions.android.map.view.AdvancedMapView;

import java.util.List;

import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
/**
 * A controller for the AdvancedMapView class.
 * Implements methods for the draw on the map.
 * Allows to be enabled or disabled.
 * @author Lorenzo Natali
 *
 */
public abstract class MapControl {
        protected String controlId;
        public static final int MODE_EDIT=0;
        public static final int MODE_VIEW=1;
	protected AdvancedMapView view = null;
	boolean enabled = false;
	List<MapControl> group;
	protected ImageButton activationButton;
	protected int mode=MODE_EDIT;
	
	public void setMode(int mode){
            this.mode = mode;
        }
	
	public int getMode(){
	    return mode;
	}
	public List<MapControl> getGroup() {
		return group;
	}
	public void setGroup(List<MapControl> group) {
		this.group = group;
	}
	protected OnClickListener activationListener=new OnClickListener(){

		@Override
		public void onClick(View button) {
			if (button.isSelected()){
	            button.setSelected(false);
	            disable();
	        } else {
	            if(group != null){
	            	for(MapControl c:group){
	            			c.disable();
	            			c.getActivationButton().setSelected(false);
	            	}
	            }
	            button.setSelected(true);
	            enable();
	        }
			
		}
		 
	};
	
	//Listener for on touch event
	protected OnTouchListener mapListener;
	protected OnTouchListener oneTapListener;
	protected OnTouchListener polygonTapListener;
	
	//Listener for polygonal selection
	protected OnTouchListener singleTapListener;
	protected OnDoubleTapListener doubleTapListener; 
	
	public MapControl(AdvancedMapView view){
		this.view = view;
	}
	/**
	 * Creates the control with the flag enabled
	 * @param view the mapView 
	 * @param enabled start as enabled or not
	 */
	public MapControl(AdvancedMapView view,boolean enabled){
		this(view);
		setEnabled(enabled);
	}
	/**
	 * Draw on the canvas
	 * @param canvas
	 */
	public abstract void draw(Canvas canvas);
	
	
	/**
	 * return the status of the control
	 * @return
	 */
	public final boolean isEnabled(){
		return enabled;
	}
	/**
	 * enable the control
	 */
	public final void enable(){
		setEnabled(true);
	}
	/**
	 * disable the control
	 */
	public final void disable(){
		setEnabled(false);
	}
	
	/**
	 * set the control enabled or disabled
	 * @param enabled if true the control is enabled, disabled if false.
	 */
	public void setEnabled(boolean enabled){
		this.enabled =enabled;
	}
	public OnClickListener getActivationListener() {
		return activationListener;
	}
	public void setActivationListener(OnClickListener activationListener) {
		this.activationListener = activationListener;
		if(this.activationButton!=null){
			this.activationButton.setOnClickListener(activationListener);
		}
	}
	public OnTouchListener getMapListener() {
		return mapListener;
	}
	
	public void setMapListener(OnTouchListener mapListener) {
		this.mapListener = mapListener;		
	}
	
	public OnTouchListener getOneTapListener() {
		return oneTapListener;
	}
	
	public void setOneTapListener(OnTouchListener oneTapListener) {
		this.oneTapListener = oneTapListener;		
	}
	
	public OnTouchListener getPolygonTapListener() {
		return polygonTapListener;
	}
	
	public void setPolygonTapListener(OnTouchListener polygonTapListener) {
		this.polygonTapListener = polygonTapListener;		
	}
	
	public OnDoubleTapListener getDoubleTapListener(){
		return doubleTapListener;
	}
	
	public void setDoubleTapListener(OnDoubleTapListener doubleTapListener){
		this.doubleTapListener = doubleTapListener;
	}
	
	public ImageButton getActivationButton() {
		return activationButton;
	}
	public void setActivationButton(ImageButton imageButton) {
		imageButton.setOnClickListener(this.getActivationListener());
		this.activationButton = imageButton;
	}
	/**
	 * Interface to allow control refreshing from resultFromIntent
	 * @param data 
	 * @param resultCode 
	 * @param requestCode 
	 */
	public abstract void refreshControl(int requestCode, int resultCode, Intent data);

    /**
     * @param savedInstanceState
     */
    public void saveState(Bundle savedInstanceState) {}
    
    public void restoreState(Bundle savedInstanceState ){    }
    
    public String getControlId() {
        return controlId;
    }

    public void setControlId(String controlId) {
        this.controlId = controlId;
    }
}