/*
 * GeoSolutions map - Digital field mapping on Android based devices
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
package it.geosolutions.android.map.renderer;

import android.graphics.PointF;

import com.vividsolutions.jts.android.PointTransformation;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * Wraps a false coordinate system that transforms coordinates
 * in percent of the width/height passed in constructor.
 * 
 * Used to draw legend
 * @author Lorenzo Natali (www.geo-solutions.it)
 *
 */
public class LegendTransformation  implements PointTransformation {
	int width;
	int height;
	/* (non-Javadoc)
	 * @see com.vividsolutions.jts.android.PointTransformation#transform(com.vividsolutions.jts.geom.Coordinate, android.graphics.PointF)
	 */
	public LegendTransformation(int width,int height){
		this.width=width;
		this.height=height;
	}
	@Override
	public void transform(Coordinate src, PointF view) {
		view.set((float)src.x*width/100,(float)src.y*height/100);
		
	}
	
}
