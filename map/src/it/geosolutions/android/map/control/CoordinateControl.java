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

import java.text.DecimalFormat;

import org.mapsforge.core.model.GeoPoint;
import org.mapsforge.core.model.MapPosition;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
/**
 * Draw the current coordinates on the map
 * @author Lorenzo Natali
 *
 */
public class CoordinateControl extends MapControl{
	private static final Paint COORDINATE_TEXT = new Paint(Paint.ANTI_ALIAS_FLAG);
	private static final Paint COORDINATE_TEXT_STROKE = new Paint(Paint.ANTI_ALIAS_FLAG);
	private String format ="##.00000";
	private int x=0;
	private int y=0;
	private static int textSize = 16;
	
	private static float dpiFactor;
	
	private static void configurePaints() {

		COORDINATE_TEXT.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		COORDINATE_TEXT.setTextSize(textSize * dpiFactor);
		COORDINATE_TEXT.setColor(Color.BLACK);
		COORDINATE_TEXT_STROKE.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		COORDINATE_TEXT_STROKE.setStyle(Paint.Style.STROKE);
		COORDINATE_TEXT_STROKE.setColor(Color.WHITE);
		COORDINATE_TEXT_STROKE.setStrokeWidth(2);
		COORDINATE_TEXT_STROKE.setTextSize(textSize * dpiFactor);
	}
	
	public CoordinateControl(AdvancedMapView m){
		super(m);
		dpiFactor = m.getContext().getResources().getDisplayMetrics().density;
		configurePaints();
	}
	public CoordinateControl(AdvancedMapView m,boolean enabled){
		super(m,enabled);
		dpiFactor = m.getContext().getResources().getDisplayMetrics().density;
		configurePaints();
	}
	@Override
	public void draw(Canvas canvas) {
		MapPosition currentMapPosition = this.view.getMapViewPosition().getMapPosition();
		
		GeoPoint gp = currentMapPosition.geoPoint;
		DecimalFormat f = new DecimalFormat(format);  // this will helps you to always keeps in two decimal places
		f.format(gp.latitude);
		f.format(gp.longitude);
		String message = (f.format(gp.latitude) +","+f.format(gp.longitude));
	    canvas.drawText(message,x , y+(textSize * dpiFactor), COORDINATE_TEXT_STROKE);
		canvas.drawText(message,x , y+(textSize * dpiFactor), COORDINATE_TEXT);
		
	}
	public void setPosition( int x, int y){
		this.x=x;
		this.y=y;
	}

	@Override
	public void refreshControl(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		
	}
	
}
