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
import it.geosolutions.android.map.listeners.MapInfoListener;
import it.geosolutions.android.map.listeners.OneTapListener;
import it.geosolutions.android.map.view.AdvancedMapView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View.OnTouchListener;

/**
 * A control for Informations about the map.
 * Wraps a listener and draw rectangle on the map.
 * @author Lorenzo Natali (www.geo-solutions.it)
 *
 */
@SuppressLint("WorldReadableFiles")
public class MapInfoControl extends MapControl{
	private static final String MODE_PRIVATE = null;
	
	protected MapInfoListener mapListener;
	protected OneTapListener oneTapListener;
	
	private static Paint paint = new Paint();
	private static int FILL_COLOR = Color.BLUE;
	private static int FILL_ALPHA = 50;
	private static int STROKE_COLOR = Color.BLACK;
	private static int STROKE_ALPHA = 100;
	private static int STROKE_WIDTH = 3;
	private static boolean STROKE_DASHED = false;
	private static float STROKE_SPACES = 10f;
	private static float STROKE_SHAPE_DIMENSION = 15f;
	private static Paint.Join STROKE_ANGLES = Paint.Join.ROUND;
	private String Shape_Selection;
	private static float RADIUS_PIXEL = 10f;
	
	private Activity activity; 
	private String[] array;
	
	private SharedPreferences pref;
	
	//Overrides the MapListener
	@Override
	public OnTouchListener getMapListener() {
		return this.mapListener;
	};
	
	//Override the OneTapListener
	@Override
	public OnTouchListener getOneTapListener() {
		return this.oneTapListener;
	};
	
	/**
	 * Creates a new MapInfoControl object and the associated listener.
	 * @param mapView
	 * @param activity 
	 */
	public MapInfoControl(AdvancedMapView mapView,Activity activity) {
		super(mapView);
		this.activity=activity;	
		array = activity.getResources().getStringArray(R.array.preferences_selection_shape);
		Shape_Selection = array[0]; //default selection rectangular
		this.mapListener = new MapInfoListener(mapView, activity, Shape_Selection);
		oneTapListener = new OneTapListener(mapView,activity);
	}
	
	/**
	 * Creates a new MapInfoControl object and the associated listener.
	 * @param mapView
	 * @param activity 
	 */
	public MapInfoControl(AdvancedMapView mapView,Activity activity,boolean enabled) {
		this(mapView,activity);
		this.setEnabled(enabled);
		this.activity=activity;
		array = activity.getResources().getStringArray(R.array.preferences_selection_shape);
		Shape_Selection = array[0]; //default selection rectangular
		this.mapListener = new MapInfoListener(mapView,activity,Shape_Selection);
		oneTapListener = new OneTapListener(mapView,activity);
	}
	
	/**
	 * Loads preferences about style of selection and checks if any of these has been
	 * changed from user. 
	 */
	public void loadStyleSelectorPreferences(){
		Context context = activity.getApplicationContext();
		pref  = PreferenceManager.getDefaultSharedPreferences(context);
		
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
			
		int ontime_radius = pref.getInt("OnTimeSelectionRadius", (int) RADIUS_PIXEL);
		if(ontime_radius != RADIUS_PIXEL) RADIUS_PIXEL = (float)ontime_radius;
		
		if(stroke_dashed != STROKE_DASHED || stroke_spaces != STROKE_SPACES || stroke_shape_dim != STROKE_SHAPE_DIMENSION || has_changed){
			STROKE_DASHED = stroke_dashed;
			STROKE_SPACES = stroke_spaces;
			STROKE_SHAPE_DIMENSION = stroke_shape_dim;
			
			//When user unchecks option for dashed stroke to reset paint is necessary because otherwise the stroke remains dashed. 
			paint.reset();	
		}	
	}

	/**
	 * Method used to draw on map, possible selections is: rectangular, circular, on time.
	 * @param canvas
	 */
	@Override
	public void draw(Canvas canvas) {
		
		if(Shape_Selection.equals(array[2])){
			if(!oneTapListener.pointsAcquired())
					return;
		}
		else{
			if(!mapListener.isDragStarted())return;
		}
		
		float radius = 0;
		RectF r = null;
		float x1 = 0, x2 = 0, y1 = 0, y2 = 0;
		
		if(!Shape_Selection.equals(array[2])){
			x1= mapListener.getStartX();
			y1= mapListener.getStartY();
			x2= mapListener.getEndX();
			y2= mapListener.getEndY();
		}	
		else{
			x1= oneTapListener.getStartX();
			y1= oneTapListener.getStartY();
		}
		// fill	
	    paint.setStyle(Paint.Style.FILL);
	    paint.setColor(FILL_COLOR);
	    paint.setAlpha(FILL_ALPHA);

		if(Shape_Selection.equals(array[0])){
			r= new RectF(
				x1<x2?x1:x2,
				y1<y2?y1:y2,
				x1>=x2?x1:x2,
				y1>=y2?y1:y2);
			
		    canvas.drawRect(r, paint);
		} else{
			if(Shape_Selection.equals(array[1])){
				float radius_y = Math.abs(x1-x2);
			    float radius_x = Math.abs(y1-y2);
			    radius = (float) Math.sqrt((radius_x*radius_x)+(radius_y*radius_y));
			}
			else
				radius = (float) pref.getInt("OnePointSelectionRadius", (int) this.RADIUS_PIXEL);
				   
		    canvas.drawCircle(x1, y1, radius, paint);
		}

	    // border
	    paint.setStyle(Paint.Style.STROKE);
	    paint.setColor(STROKE_COLOR);
	    paint.setAlpha(STROKE_ALPHA);
	    paint.setStrokeWidth(STROKE_WIDTH);
    	paint.setStrokeJoin(STROKE_ANGLES);

	    //Checks if user required dashed stroke
	    if(STROKE_DASHED==true)
	 	    paint.setPathEffect(new DashPathEffect(new float[]{STROKE_SHAPE_DIMENSION,STROKE_SPACES}, 0));
	   
	    if(this.Shape_Selection.equals(array[0]))
		    canvas.drawRect(r, paint);
	    else 	    		
	    	canvas.drawCircle(x1, y1, radius, paint);	    
	}

	@Override 
	public void setMode(int mode){
	    super.setMode(mode);
	    if(mode == MODE_VIEW){
	        mapListener.setMode(MapInfoListener.MODE_VIEW);
	        oneTapListener.setMode(OneTapListener.MODE_VIEW);
	    }
	    else{
	        mapListener.setMode(MapInfoListener.MODE_EDIT);	 
	        oneTapListener.setMode(OneTapListener.MODE_EDIT);
	    }
	}
	
	@Override
	public void refreshControl(int requestCode, int resultCode, Intent data) {

		Log.v("MapInfoControl", "requestCode:"+requestCode);
		Log.v("MapInfoControl", "resultCode:"+resultCode);

		disable();
		getActivationButton().setSelected(false);
		loadStyleSelectorPreferences();
	}
}