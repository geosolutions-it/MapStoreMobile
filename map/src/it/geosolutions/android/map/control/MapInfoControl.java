/*
 * GeoSolutions GeoSolutions Android Map Library - Digital mapping on Android based devices
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
import it.geosolutions.android.map.control.todraw.Circle;
import it.geosolutions.android.map.control.todraw.Polygon;
import it.geosolutions.android.map.control.todraw.Rectangle;
import it.geosolutions.android.map.listeners.MapInfoListener;
import it.geosolutions.android.map.listeners.OneTapListener;
import it.geosolutions.android.map.listeners.PolygonTapListener;

import it.geosolutions.android.map.view.AdvancedMapView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;

import android.preference.PreferenceManager;
import android.util.Log;

import android.view.View.OnTouchListener;

/**
 * A control for Informations about the map.
 * Wraps a listener and draw rectangle, circle or polygon on the map.
 * @author Lorenzo Natali (www.geo-solutions.it)
 */
@SuppressLint("WorldReadableFiles")
public class MapInfoControl extends MapControl{
	private static final String MODE_PRIVATE = null;
	
	//Listeners
	protected MapInfoListener mapListener;
	protected OneTapListener oneTapListener;
	protected PolygonTapListener polygonTapListener;
	
	private static Paint paint_fill = new Paint();
	private static Paint paint_stroke = new Paint();
	private static int FILL_COLOR = Color.BLUE;
	private static int FILL_ALPHA = 50;
	private static int STROKE_COLOR = Color.BLACK;
	private static int STROKE_ALPHA = 100;
	private static int STROKE_WIDTH = 3;
	private static boolean STROKE_DASHED = false;
	private static float STROKE_SPACES = 10f;
	private static float STROKE_SHAPE_DIMENSION = 15f;
	private static Paint.Join STROKE_ANGLES = Paint.Join.ROUND;
	private static String Shape_Selection;

	private Activity activity; 	
	private String[] array;
	private SharedPreferences pref;
	private AdvancedMapView mapView;
		
	//Overrides the MapListener
	@Override
	public OnTouchListener getMapListener() {
		return this.mapListener;
	};
	
	//Override the OneTapListener
	@Override
	public OneTapListener getOneTapListener() {
		return this.oneTapListener;
	};
	
	//Override the OneTapListener
	@Override
	public PolygonTapListener getPolygonTapListener() {
		return this.polygonTapListener;
	};

	/**
	 * Creates a new MapInfoControl object and the associated listener.
	 * @param mapView
	 * @param activity 
	 */
	public MapInfoControl(AdvancedMapView mapView,Activity activity) {
		super(mapView);
		this.mapView = mapView;
		this.activity=activity;	
		
		pref  = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
		array = activity.getResources().getStringArray(R.array.preferences_selection_shape);
		Shape_Selection = array[0]; //default selection rectangular
		
		instantiateListener();
	}
	
	/**
	 * Loads preferences about style of selection and checks if any of these has been
	 * changed from user. 
	 */
	public void loadStyleSelectorPreferences(){
		//Load preferences about style of fill
		int fill_color = pref.getInt("FillColor", FILL_COLOR);
		if(fill_color != FILL_COLOR) FILL_COLOR = fill_color; //Check if default color for selection has been selected, otherwise it changes the variable FILL_COLOR
		
		int fill_alpha = pref.getInt("FillAlpha", FILL_ALPHA);
		if(fill_alpha != FILL_ALPHA) FILL_ALPHA = fill_alpha;

		//Load preferences about style of stroke
		int stroke_color = pref.getInt("StrokeColor", STROKE_COLOR);
		if(stroke_color != STROKE_COLOR) STROKE_COLOR = stroke_color;

		int stroke_alpha = pref.getInt("StrokeAlpha", STROKE_ALPHA);
		if(stroke_alpha != STROKE_ALPHA) STROKE_ALPHA = stroke_alpha;

		int stroke_width = pref.getInt("StrokeWidth", STROKE_WIDTH);
		if(stroke_width != STROKE_WIDTH) STROKE_WIDTH = stroke_width;
		
		boolean stroke_dashed = pref.getBoolean("DashedStroke", STROKE_DASHED);
		
		int stroke_sp = pref.getInt("StrokeSpaces", (int) STROKE_SPACES);
		float stroke_spaces = (float)stroke_sp;
		
		int stroke_dim = pref.getInt("StrokeDim", (int) STROKE_SHAPE_DIMENSION);
		float stroke_shape_dim = (float)stroke_dim;
		
		//Load preference about shape of angles of stroke
		boolean has_changed = false;
		String angles_shape = pref.getString("StrokeAngles", "BEVEL");

		if(!angles_shape.equals(STROKE_ANGLES.toString())){
			has_changed = true;
			if(angles_shape.equals("BEVEL")) STROKE_ANGLES = Paint.Join.BEVEL;
			else if(angles_shape.equals("MITER")) STROKE_ANGLES = Paint.Join.MITER;
			else if(angles_shape.equals("ROUND")) STROKE_ANGLES = Paint.Join.ROUND;
		}
		
		String shape_sel = pref.getString("selectionShape", this.Shape_Selection);
		if(!shape_sel.equals(Shape_Selection))
			//Control if the user has choosed a new shape for selection
			Shape_Selection = shape_sel;
		
		if(stroke_dashed != STROKE_DASHED || stroke_spaces != STROKE_SPACES || stroke_shape_dim != STROKE_SHAPE_DIMENSION || has_changed){
			STROKE_DASHED = stroke_dashed;
			STROKE_SPACES = stroke_spaces;
			STROKE_SHAPE_DIMENSION = stroke_shape_dim;
			
			//When user unchecks option for dashed stroke to reset paint is necessary because otherwise the stroke remains dashed. 
			paint_stroke.reset();	
		}	
	}

	/**
	 * Method used to draw on map, possible selections is: rectangular, circular, one point.
	 * @param canvas
	 */
	@Override
	public void draw(Canvas canvas) {	
		// fill	properties
	    paint_fill.setStyle(Paint.Style.FILL);
	    paint_fill.setColor(FILL_COLOR);
	    paint_fill.setAlpha(FILL_ALPHA);
	    
	    // border properties
	    paint_stroke.setStyle(Paint.Style.STROKE);
	    paint_stroke.setColor(STROKE_COLOR);
	    paint_stroke.setAlpha(STROKE_ALPHA);
	    paint_stroke.setStrokeWidth(STROKE_WIDTH);
    	paint_stroke.setStrokeJoin(STROKE_ANGLES);
    	
    	//Checks if user required dashed stroke
	    if(STROKE_DASHED==true)
	 	    paint_stroke.setPathEffect(new DashPathEffect(new float[]{STROKE_SHAPE_DIMENSION,STROKE_SPACES}, 0));
		
		if(Shape_Selection.equals(array[0])){
			if(!mapListener.isDragStarted()) return;

			Rectangle r = new Rectangle(canvas);
			r.buildObject(mapListener);
			r.draw(paint_fill);
			r.draw(paint_stroke);
		}
		
		else if(Shape_Selection.equals(array[1])){
			if(!mapListener.isDragStarted()) return;

			Circle c = new Circle(canvas);
			c.buildObject(mapListener);
			c.draw(paint_fill);
			c.draw(paint_stroke);
			c.drawInfo(mapView, 0);
		}
		else if(Shape_Selection.equals(array[2])){
			if(!oneTapListener.pointsAcquired()) return;
			
			Circle c = new Circle(canvas);
			c.buildObject(oneTapListener);
			c.draw(paint_fill);
			c.draw(paint_stroke);
			c.drawInfo(mapView, 1);
		}
		else{
			if(!polygonTapListener.isAcquisitionStarted() || 
					polygonTapListener.getNumberOfPoints()< 1) return;
			
			Polygon p = new Polygon(canvas,view);
			p.buildPolygon(polygonTapListener);
			p.draw(paint_fill);
			p.draw(paint_stroke);
		}
	}

	@Override 
	public void setMode(int mode){
	    super.setMode(mode);
	    if(mode == MODE_VIEW){
	        if(mapListener != null) 
	        	mapListener.setMode(MapInfoListener.MODE_VIEW);
	        if(oneTapListener != null) 
	        	oneTapListener.setMode(OneTapListener.MODE_VIEW);
	        if(polygonTapListener !=null)
	        	polygonTapListener.setMode(PolygonTapListener.MODE_VIEW);
	    }
	    else{
	    	if(mapListener != null) 
	    		mapListener.setMode(MapInfoListener.MODE_EDIT);	 
	        if(oneTapListener != null) 
	        	oneTapListener.setMode(OneTapListener.MODE_EDIT);
	        if(polygonTapListener !=null)
	        	polygonTapListener.setMode(PolygonTapListener.MODE_EDIT);
	    }
	}
	
	@Override
	public void refreshControl(int requestCode, int resultCode, Intent data) {
		Log.v("MapInfoControl", "requestCode:"+requestCode);
		Log.v("MapInfoControl", "resultCode:"+resultCode);

		disable();
		getActivationButton().setSelected(false);
		loadStyleSelectorPreferences();
		instantiateListener();
	}
	
	/**
	 * Instantiate listener for selection choosed by user.
	 */
	private void instantiateListener(){
		if(pref.getString("selectionShape", Shape_Selection).equals(array[3]) 
				&& polygonTapListener == null)
			this.polygonTapListener = new PolygonTapListener(mapView,activity);
		else
			if(pref.getString("selectionShape", Shape_Selection).equals(array[2])
					&& oneTapListener == null)
				this.oneTapListener = new OneTapListener(mapView,activity);
			else
				if(this.mapListener == null)
					this.mapListener = new MapInfoListener(mapView,activity);
	}
	
	/**
	 * Override the method of MapControl to cancel polygonal selection when
	 * polygon is not closed and button info is not selected.
	 */
	@Override 
	public void setEnabled(boolean enabled){
		this.enabled =enabled;
		if(!enabled && polygonTapListener != null && 
				pref.getString("selectionShape","").equals(array[3]) )
        	polygonTapListener.reset();
	}
}