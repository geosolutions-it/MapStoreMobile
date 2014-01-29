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

import it.geosolutions.android.map.listeners.MapInfoListener;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Class to draw a rectangle on map for rectangular selection
 * @author Jacopo Pianigiani (jacopo.pianigiani85@gmail.com)
 */
public class Rectangle extends ObjectToDraw {
	private float x1, y1, x2, y2;
	private RectF r;
	
	/**
	 * Constructor of rectangle class.
	 * @param canvas
	 */
	public Rectangle(Canvas canvas) {
		super(canvas);
	}
	
	/**
	 * Construct a rectangle object by two points captured by touch.
	 * @param mapListener
	 */
	public void buildObject(MapInfoListener mapListener){
		x1 = mapListener.getStartX();
		y1 = mapListener.getStartY();
		x2 = mapListener.getEndX();
		y2 = mapListener.getEndY();
		r = new RectF(x1<x2?x1:x2, y1<y2?y1:y2, x1>=x2?x1:x2, y1>=y2?y1:y2);
	}


	/* (non-Javadoc)
	 * @see it.geosolutions.android.map.control.todraw.ObjectToDraw#draw(android.graphics.Paint)
	 */
	@Override
	public void draw(Paint paint) {
		canvas.drawRect(r, paint);
	}
}
