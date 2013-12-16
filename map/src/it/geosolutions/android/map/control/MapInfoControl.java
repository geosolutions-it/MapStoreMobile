package it.geosolutions.android.map.control;

import it.geosolutions.android.map.listeners.MapInfoListener;
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
 * A control for Infomations about the map.
 * Wraps a listener and draw rectangle on the map.
 * @author Lorenzo Natali (www.geo-solutions.it)
 *
 */
@SuppressLint("WorldReadableFiles")
public class MapInfoControl extends MapControl{
	private static final String MODE_PRIVATE = null;
	protected MapInfoListener mapListener;
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
	
	private Activity activity; //Useful to get the context of activity
		
	//Overrides the MapListener
	@Override
	public OnTouchListener getMapListener() {
		return this.mapListener;
	};
	/**
	 * Creates a new Control and the associated listener
	 * @param mapView
	 * @param activity the activity that will receive results
	 */
	public MapInfoControl(AdvancedMapView mapView,Activity activity) {
		super(mapView);
		this.mapListener = new MapInfoListener(mapView, activity);
		this.activity=activity;	
	}
	
	public MapInfoControl(AdvancedMapView mapView,Activity activity,boolean enabled) {
		this(mapView,activity);
		this.mapListener = new MapInfoListener(mapView,activity);
		this.setEnabled(enabled);
		this.activity=activity;
	}
	
	/**
	 * Loads preferences about style of selection and checks if any of these has been
	 * changed from user. 
	 */
	public void loadStyleSelectorPreferences(){
		Context context = activity.getApplicationContext();
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);//PreferenceManager.getSharedPreferences();
		
		int fill_color = pref.getInt("FillColor", FILL_COLOR);
		
		//Check if default color for selection has been selected, otherwise it changes the variable FILL_COLOR
		if(fill_color != FILL_COLOR) FILL_COLOR = fill_color;
		
		//Repeat same procedure for others properties
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
		
		if(stroke_dashed != STROKE_DASHED || stroke_spaces != STROKE_SPACES || stroke_shape_dim != STROKE_SHAPE_DIMENSION || has_changed){
			STROKE_DASHED = stroke_dashed;
			STROKE_SPACES = stroke_spaces;
			STROKE_SHAPE_DIMENSION = stroke_shape_dim;
			
			//When user unchecks option for dashed stroke to reset paint is necessary because otherwise the stroke remains dashed. 
			paint.reset();	
		}	
	}

	@Override
	public void draw(Canvas canvas) {
		if(!mapListener.isDragStarted()){
			return;
		}
		float x1= mapListener.getStartX();
		float y1= mapListener.getStartY();
		float x2= mapListener.getEndX();
		float y2= mapListener.getEndY();
		RectF r= new RectF(
				x1<x2?x1:x2,
				y1<y2?y1:y2,
				x1>=x2?x1:x2,
				y1>=y2?y1:y2);
		
		 // fill	
	    paint.setStyle(Paint.Style.FILL);
	    paint.setColor(FILL_COLOR);
	    paint.setAlpha(FILL_ALPHA);
	    canvas.drawRect(r, paint);

	    // border
	    paint.setStyle(Paint.Style.STROKE);
	    paint.setColor(STROKE_COLOR);
	    paint.setAlpha(STROKE_ALPHA);
	    paint.setStrokeWidth(STROKE_WIDTH);
    	paint.setStrokeJoin(STROKE_ANGLES);

	    //Checks if user required dashed stroke
	    if(STROKE_DASHED==true)
	 	    paint.setPathEffect(new DashPathEffect(new float[]{STROKE_SHAPE_DIMENSION,STROKE_SPACES}, 0));
	    
	    canvas.drawRect(r, paint);
	}
	
	@Override 
	public void setMode(int mode){
	    super.setMode(mode);
	    if(mode == MODE_VIEW){
	        mapListener.setMode(MapInfoListener.MODE_VIEW);
	    }else{
	        mapListener.setMode(MapInfoListener.MODE_EDIT);
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