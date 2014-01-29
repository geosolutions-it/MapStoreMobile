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
import it.geosolutions.android.map.listeners.PolygonTapListener;
import it.geosolutions.android.map.view.AdvancedMapView;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;

/**
 * A controller for the AdvancedMapView class.
 * Implements methods for the draw on the map.
 * Allows to be enabled or disabled.
 * @author Lorenzo Natali (www.geo-solutions.it)
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
	
	private SharedPreferences pref;
	private String[] array;
	
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
	
	// Set OnClickListener for Image buttons.
	protected OnClickListener activationListener=new OnClickListener(){

		@Override
		public void onClick(View button) {
			if (button.isSelected()){
				if(button == (ImageButton) view.findViewById(R.id.ButtonInfo)){
					if(pref.getString("selectionShape", "").equals(array[3]))
		        		ptl.reset();
				}
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
	
	//Listener for touch event on map.
	protected OnTouchListener mapListener;
	protected OnTouchListener oneTapListener;
	protected OnTouchListener polygonTapListener;
	private PolygonTapListener ptl; //Used when user wants to cancel polygonal selection
	
	/**
	 * Creates the control.
	 * @param view
	 */
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
	 * Create the control.
	 * @param view
	 * @param activity will be used to access to shared preferences.
	 */
	public MapControl(AdvancedMapView view,Activity activity){
		this(view);
		pref = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
		array = activity.getResources().getStringArray(R.array.preferences_selection_shape);
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
	
	/**
	 * Return listener for Image Buttons.
	 * @return
	 */
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
	
	/**
	 * Get ImageButton identifier.
	 * @return
	 */
	public ImageButton getActivationButton() {
		return activationButton;
	}
	
	/**
	 * Set listener for click event on ImageButton.
	 * @param imageButton on then to set listener.
	 */
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
    
    public void restoreState(Bundle savedInstanceState){    }
    
    /**
     * Return control Identifier.
     * @return
     */
    public String getControlId() {
        return controlId;
    }

    /**
     * Set control identifier.
     * @param controlId
     */
    public void setControlId(String controlId) {
        this.controlId = controlId;
    }
    
    /**
     * Set identifier for PolygonTapListener, used when user wants to cancel polygonal selection.
     * @param ptl
     */
    protected void setToCancel(PolygonTapListener ptl){
    	this.ptl = ptl;
    }
}