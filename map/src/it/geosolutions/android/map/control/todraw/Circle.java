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
package it.geosolutions.android.map.control.todraw;

import java.text.DecimalFormat;

import it.geosolutions.android.map.R;
import it.geosolutions.android.map.listeners.MapInfoListener;
import it.geosolutions.android.map.listeners.OneTapListener;
import it.geosolutions.android.map.utils.ConversionUtilities;
import it.geosolutions.android.map.utils.GeodesicDistance;
import it.geosolutions.android.map.view.AdvancedMapView;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.Align;

/**
 * Class to draw a circle for circular selection on map.
 * @author Jacopo Pianigiani (jacopo.pianigiani85@gmail.com)
 */
public class Circle extends ObjectToDraw {	
	
	private float x1, y1, x2, y2, radius;
	private final static Paint COORDINATE_TEXT = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final static Paint COORDINATE_TEXT_STROKE = new Paint(Paint.ANTI_ALIAS_FLAG);
	
	
	/**
	 * Constructor for class circle.
	 * @param canvas
	 */
	public Circle(Canvas canvas){
		super(canvas);	
	}
	
	/**
	 * Build circle object by two points captured by touch.
	 * @param mapListener
	 */
	public void buildObject(MapInfoListener mapListener){
		x1= mapListener.getStartX();
		y1= mapListener.getStartY();
		x2= mapListener.getEndX();
		y2= mapListener.getEndY();
		float radius_x = Math.abs(x1-x2);
	    float radius_y = Math.abs(y1-y2);
	    radius = (float) Math.sqrt((radius_x*radius_x)+(radius_y*radius_y));
	}
	
	/**
	 * Build circle object by center and radius set by user.
	 * @param oneTapListener
	 */
	public void buildObject(OneTapListener oneTapListener){
		x1= oneTapListener.getStartX();
		y1= oneTapListener.getStartY();
		radius = oneTapListener.getRadius();
	}

	/* (non-Javadoc)
	 * @see it.geosolutions.android.map.control.todraw.ObjectToDraw#draw(android.graphics.Paint)
	 */
	@Override
	public void draw(Paint paint) {
		canvas.drawCircle(x1, y1, radius, paint);
	}
	
	/**
	 * Draw informations about circular and one point selection(center long/lat, pixels of radius)
	 * on the top right corner of the screen.
	 * @param mapView
	 * @param which
	 */
	public void drawInfo(AdvancedMapView mapView, int which){
		int textSize = 20;
		configurePaints(textSize);
		String format ="##.00000";
		String format_radius = "##";
		DecimalFormat f = new DecimalFormat(format); 
		
		double x_long = ConversionUtilities.convertFromPixelsToLongitude(mapView, x1);
		double y_lat = ConversionUtilities.convertFromPixelsToLatitude(mapView, y1);	
		
		String center = mapView.getResources().getString(R.string.center);
		String rad = mapView.getResources().getString(R.string.radius);
		String message_center = center + " ("+ f.format(x_long) +" , "+f.format(y_lat)+") ";
		
		double radius_km;
		int radius_to_show;
		if(which == 1)
			radius_km = GeodesicDistance.getDistance(x_long, y_lat, ConversionUtilities.convertFromPixelsToLongitude(mapView, x1+radius), y_lat);
		else
			radius_km = GeodesicDistance.getDistance(x_long, y_lat, ConversionUtilities.convertFromPixelsToLongitude(mapView, x2), ConversionUtilities.convertFromPixelsToLatitude(mapView, y2));
		
		if(radius_km < 1){
			radius_to_show = (int) (radius_km * 1000);
			format_radius += " m ";
		}
		else{
			radius_to_show = (int)radius_km;
			format_radius += " km ";
		}
		
		DecimalFormat rad_format = new DecimalFormat(format_radius);
		String message_radius = rad + " " + rad_format.format(radius_to_show);
		
		canvas.drawText(message_center, (float) canvas.getWidth(), (float) textSize, COORDINATE_TEXT);		
		canvas.drawText(message_center, (float) canvas.getWidth(), (float) textSize, COORDINATE_TEXT_STROKE);
		
		canvas.drawText(message_radius, (float) canvas.getWidth(), (float) textSize*2, COORDINATE_TEXT);		
		canvas.drawText(message_radius, (float) canvas.getWidth(), (float) textSize*2, COORDINATE_TEXT_STROKE);
	}
	
	/**
	 * Basic style configurations of paint.
	 * @param textSize
	 */
	private static void configurePaints(int textSize) {
		COORDINATE_TEXT.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		COORDINATE_TEXT.setTextSize(textSize);
		COORDINATE_TEXT.setColor(Color.BLACK);
		COORDINATE_TEXT.setTextAlign(Align.RIGHT); 
		COORDINATE_TEXT_STROKE.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		COORDINATE_TEXT_STROKE.setStyle(Paint.Style.STROKE);
		COORDINATE_TEXT_STROKE.setColor(Color.WHITE);
		COORDINATE_TEXT_STROKE.setStrokeWidth(2);
		COORDINATE_TEXT_STROKE.setTextSize(textSize);
	}
}