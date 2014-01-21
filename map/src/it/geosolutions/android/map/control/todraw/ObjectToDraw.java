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

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Template class for object to draw on map for selection.
 * @author Jacopo Pianigiani(jacopo.pianigiani85@gmail.com)
 */
public abstract class ObjectToDraw {
	
	protected Canvas canvas;
	
	/**
	 * Constructor of class;
	 * @param canvas
	 */
	public ObjectToDraw(Canvas canvas){
		this.canvas = canvas;
	}

	/**
	 * Draw fill or stroke of selection on the canvas
	 * @param paint
	 */
	public abstract void draw(Paint paint);
}
