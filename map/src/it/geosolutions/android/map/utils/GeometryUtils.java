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
package it.geosolutions.android.map.utils;

import java.util.ArrayList;

import it.geosolutions.android.map.database.SpatialDataSourceHandler;
import it.geosolutions.android.map.database.SpatialDataSourceManager;
import it.geosolutions.android.map.model.Feature;

import org.mapsforge.core.model.GeoPoint;

import android.util.Log;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;

import eu.geopaparazzi.spatialite.database.spatial.core.SpatialVectorTable;

/**
 * @author Lorenzo Natali (www.geo-solutions.it)
 *
 */
public class GeometryUtils {
        private static SpatialDataSourceManager sdsm = SpatialDataSourceManager.getInstance();
	/**
	 * 
	 * @param layer
	 * @param featureFound
	 */
	public static GeoPoint getGeoPointFromGeometry(String layer,Geometry geom) {
	        if(geom!=null){
        		Point p = geom.getInteriorPoint();
        		return new GeoPoint(p.getY(),p.getX());
	        }else{
	            return null;
	        }
		
	}
	}
