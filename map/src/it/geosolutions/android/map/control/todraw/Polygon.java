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

import it.geosolutions.android.map.listeners.PolygonTapListener;
import it.geosolutions.android.map.utils.ConversionUtilities;
import it.geosolutions.android.map.view.AdvancedMapView;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Path.Direction;

/**
 * Class to draw a polygon for polygonal selection on map.
 * @author Jacopo Pianigiani (jacopo.pianigiani85@gmail.com)
 */
public class Polygon extends ObjectToDraw {
	private Path polygon;
	private AdvancedMapView view;

	/**
	 * Constructor of class polygon.
	 * @param canvas
	 */
	public Polygon(Canvas canvas, AdvancedMapView view) {
		super(canvas);
		polygon = new Path();
		this.view = view;
	}
	
	/**
	 * Build a polygon object by points stored by listener.
	 * @param polygonTapListener
	 * @param paint used for style of circle put over polygon points.
	 */
	public void buildPolygon(PolygonTapListener polygonTapListener,Paint paint){
		float x, y;
		int n_points = polygonTapListener.getNumberOfPoints();
		for(int i = 0;i <n_points;i++){
			if(i==0){
				x = (float) ConversionUtilities.convertFromLongitudeToPixels(view, polygonTapListener.getXPoint(i));
				y = (float) ConversionUtilities.convertFromLatitudeToPixels(view, polygonTapListener.getYPoint(i));
				polygon.moveTo(x, y);
			} 
				
			else{
				x = (float) ConversionUtilities.convertFromLongitudeToPixels(view, polygonTapListener.getXPoint(i));
				y = (float) ConversionUtilities.convertFromLatitudeToPixels(view, polygonTapListener.getYPoint(i));
				polygon.lineTo(x,y);
				if(i==n_points-1)
					break;
			}
				
			canvas.drawCircle(x, y, 8 , paint);
		}
		
		//Close perimeter of polygon
		if(polygonTapListener.isPointsAcquired()){
			polygon.lineTo(
					(float) ConversionUtilities.convertFromLongitudeToPixels(view, polygonTapListener.getXPoint(0)),
					(float) ConversionUtilities.convertFromLatitudeToPixels(view, polygonTapListener.getYPoint(0)));			
		}
	}

	/* (non-Javadoc)
	 * @see it.geosolutions.android.map.control.todraw.ObjectToDraw#draw(android.graphics.Paint)
	 */
	@Override
	public void draw(Paint paint) {
		canvas.drawPath(polygon, paint);
	}
}