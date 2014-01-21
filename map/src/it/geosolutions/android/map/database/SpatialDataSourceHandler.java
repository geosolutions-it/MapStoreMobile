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
package it.geosolutions.android.map.database;

import it.geosolutions.android.map.model.Feature;
import it.geosolutions.android.map.utils.Coordinates.Coordinates_Query;

import java.util.ArrayList;
import java.util.Map;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;

import jsqlite.Exception;

import android.os.Bundle;
import eu.geopaparazzi.spatialite.database.spatial.core.ISpatialDatabaseHandler;
import eu.geopaparazzi.spatialite.database.spatial.core.SpatialVectorTable;

/**
 * Interface for spatialdatasourcehandler
 * @author Lorenzo Natali (www.geo-solutions.it)
 *
 */
public interface SpatialDataSourceHandler extends ISpatialDatabaseHandler {

	public Map<String,String>  queryBBox();
	public ArrayList<Bundle> intersectionToBundleBBOX( String boundsSrid, SpatialVectorTable spatialTable, double n, double s, double e,
            double w) throws Exception;
	/**
	 * @param boundsSrid
	 * @param spatialTable
	 * @param n
	 * @param s
	 * @param e
	 * @param w
	 * @param start
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	ArrayList<Bundle> intersectionToBundleBBOX(String boundsSrid,
			SpatialVectorTable spatialTable, double n, double s, double e,
			double w, Integer start, Integer limit) throws Exception;
	/**
	 * 
	 * @param boundsSrid
	 * @param spatialTable
	 * @param x
	 * @param y
	 * @param radius
	 * @return
	 * @throws Exception
	 */
	ArrayList<Bundle> intersectionToCircleBOX(String boundsSrid,
			SpatialVectorTable spatialTable, double x, double y, double radius) throws Exception;
	
	/**
	 * @param boundsSrid
	 * @param spatialTable
	 * @param x	 
	 * @param y
	 * @param radius
	 * @param start
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	ArrayList<Bundle> intersectionToCircleBOX(String boundsSrid,
			SpatialVectorTable spatialTable, double x, double y, double radius,
			Integer start, Integer limit) throws Exception;
	/**
	 * @param boundsSrid
	 * @param spatialTable
	 * @param n
	 * @param s
	 * @param e
	 * @param w
	 * @param start
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	ArrayList<Map<String, String>> intersectionToMapBBOX(String boundsSrid,
			SpatialVectorTable spatialTable, double n, double s, double e,
			double w, Integer start, Integer limit) throws Exception;
	/**
	 * @param boundsSrid
	 * @param spatialTable
	 * @param n
	 * @param s
	 * @param e
	 * @param w
	 * @param start
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	ArrayList<Feature> intersectionToFeatureListBBOX(String boundsSrid,
			SpatialVectorTable spatialTable, double n, double s, double e,
			double w, Integer start, Integer limit) throws Exception;
	/**
	 * @param boundsSrid
	 * @param spatialTable
	 * @param x
	 * @param y
	 * @param radius
	 * @param stroke_width
	 * @param start
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	ArrayList<Feature> intersectionToCircle(String boundsSrid,
			SpatialVectorTable spatialTable, double x, double y, double radius,
			Integer start, Integer limit) throws Exception;
	/**
	 * @param srid 
	 * @param layer
	 * @param attributeName
	 * @param attributeValue
	 * @param object
	 * @param i
	 * @param b
	 * @return 
	 * @throws Exception 
	 * @throws ParseException 
	 */
	public Geometry getGeometryByAttribute(String srid, SpatialVectorTable layer, String attributeName,
			String attributeValue, Integer start, Integer limit, boolean includeGeometry) throws Exception, ParseException;
	/**
	 * @param srid
	 * @param table
	 * @param attributeName
	 * @param attributeValue
	 * @param start
	 * @param limit
	 * @param getGeometry
	 * @return
	 * @throws Exception
	 */
	ArrayList<Feature> getFeaturesByAttribute(String srid,
			SpatialVectorTable table, String attributeName,
			String attributeValue, Integer start, Integer limit,
			boolean getGeometry) throws Exception;
	
	/**
	 * 
	 * @param boundsSrid
	 * @param spatialTable
	 * @param polygon_points
	 * @return
	 * @throws Exception
	 */
	ArrayList<Bundle> intersectionToPolygonBOX(String boundsSrid,
			SpatialVectorTable spatialTable, ArrayList<Coordinates_Query> polygon_points) throws Exception;

	/**
	 * @param boundsSrid
	 * @param spatialTable
	 * @param polygon_points
	 * @param start
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	ArrayList<Bundle> intersectionToPolygonBOX(String boundsSrid,
			SpatialVectorTable spatialTable, ArrayList<Coordinates_Query> polygon_points,
			Integer start, Integer limit) throws Exception;
	
	/**
	 * @param boundsSrid
	 * @param spatialTable
	 * @param polygon_points
	 * @param start
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	ArrayList<Feature> intersectionToPolygon(String boundsSrid,
			SpatialVectorTable spatialTable, ArrayList<Coordinates_Query> polygon_points,
			Integer start, Integer limit) throws Exception;
}