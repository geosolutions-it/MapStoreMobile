/*
 *    GeoSolutions Android Map Library
 *    http://www.geo-solutions.it
 *
 *    (C) 2012-2013, GeoSolutions S.A.S
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.android.map.utils;

import it.geosolutions.android.map.database.SpatialDataSourceHandler;
import it.geosolutions.android.map.database.SpatialDataSourceManager;
import it.geosolutions.android.map.model.Feature;

import java.util.ArrayList;
import java.util.List;

import jsqlite.Exception;

import org.mapsforge.core.model.GeoPoint;

import android.util.Log;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;

import eu.geopaparazzi.spatialite.database.spatial.core.SpatialVectorTable;

/**
 * Utility class for common operations on the Spatial DB
 * @author Lorenzo Natali
 */
public class SpatialDbUtils {

private static final SpatialDataSourceManager sdbm = SpatialDataSourceManager
        .getInstance();

/**
 * Provide a <Feature> object searching in a table that match a particular value
 * The passed attribute should be an id
 * @param attributeValue the value searched
 * @param attributeName the name of the column to check
 * @param tableName the name of the table
 * @return a <Feature> creating getting the feature that match attributeName -> attributeValue
 * @throws Exception
 */
public static Feature getFeatureById(String attributeValue,
        String attributeName, String tableName) {
    ArrayList<Feature> result = null;
    try {
        SpatialVectorTable table = sdbm.getVectorTableByName(tableName);
        SpatialDataSourceHandler handler = sdbm
                .getSpatialDataSourceHandler(table);

        if (handler != null) {
            result = handler.getFeaturesByAttribute("4326", table,
                    attributeName, attributeValue, null, 1, true);
        } else {
            Log.e("DATABASE", "unable to retrive table:" + tableName);
        }
    } catch (Exception e) {
        Log.e("DATABASE", "unable to retrive feature:" + attributeName + "->"
                + attributeValue + "from table: " + tableName);
    }
    if (result != null && result.size() > 0) {
        return result.get(0);
    }
    return null;

}
/**
 * Provide the <Feature> object iterating over all the tabled in the DataBase.
 * @param attributeValue value of the column to check
 * @param attributeName name of the column to check 
 * @return reating getting the feature that match attributeName -> attributeValue
 */
public static Feature getFeatureById(String attributeValue, String attributeName) {

    ArrayList<Feature> result = null;
    List<SpatialVectorTable> tables = null;
    try {
        tables = sdbm.getSpatialVectorTables(false);
    } catch (Exception e) {
        Log.e("DATABASE", "unable to get tables from the database:");
        return null;
    }
    for (SpatialVectorTable table : tables) {
        try {

            SpatialDataSourceHandler handler = sdbm
                    .getSpatialDataSourceHandler(table);

            if (handler != null) {
                result = handler.getFeaturesByAttribute("4326", table,
                        attributeName, attributeValue, null, 1, true);
            } else {
                Log.e("DATABASE", "unatable.getName()table:" + table.getName());
            }
        } catch (Exception e) {
            Log.e("DATABASE", "unable to retrive feature:" + attributeName
                    + "->" + table.getName());
        }
        if (result != null && result.size() > 0) {
            return result.get(0);
        }
    }
    return null;

}

/**
 * Gets a <Geopoint> for the layer with the attributeName = attributeValue.
 * 
 * @param layer
 * @param attributeName
 * @param attributeValue
 * @param wantGeometry to get also the geometry
 * @return
 */
public static GeoPoint getGeopointFromLayer(String layer, String attributeName,String attributeValue) {

    SpatialDataSourceHandler handler = null;
    try {
        SpatialVectorTable table = sdbm.getVectorTableByName(layer);
        handler = sdbm.getSpatialDataSourceHandler(table);
        if (handler != null && attributeValue != null) {
            Log.v("MARKER_SUBSTITUTION",
                    "start getting Feature from the database");
            Geometry geom = handler.getGeometryByAttribute("4326", table,
                    attributeName, attributeValue, null, 1, true);
            GeoPoint gp = GeometryUtils.getGeoPointFromGeometry(layer, geom);
            if (gp != null) {
                Log.v("MARKER_SUBSTITUTION", "new Coordinates:" + gp.latitude
                        + "," + gp.longitude);
                return gp;
            } else {
                Log.v("MARKER_SUBSTITUTION", "unable to retrive:"
                        + attributeName + "->" + attributeValue);

                // TODO request download

            }
        } else {
            Log.e("MARKER_SUBSTITUTION", "error retriving table for layer:"
                    + layer
                    + ". The attribute value for search or the handler is null");
        }
    } catch (jsqlite.Exception e) {
        Log.e("MARKER_SUBSTITUTION", "error retriving table for layer:" + layer
                + "error:" + e.getMessage());

    } catch (ParseException e) {
        Log.e("MARKER_SUBSTITUTION", "error retriving geometry for layer:"
                + layer + "error:" + e.getMessage());

    }
    return null;
}

/**
 * Iterates on the database to get the <GeoPoint> from the object that match
 * @param attributeName the column name
 * @param attributeValue the value that should match
 * @return a <GeoPoint> or null
 */
public static GeoPoint getGeopointFromLayer( String attributeName, String attributeValue) {
    List<SpatialVectorTable> tables;
    try{
        tables= sdbm.getSpatialVectorTables(false);
    }catch(Exception e){
        Log.v("DATABASE","unable to retrive table list from data source manager");
        return null;
    }
    for (SpatialVectorTable t :tables){
        GeoPoint p = getGeopointFromLayer(t.getName(),attributeName,attributeValue);
        if(p!=null){
           return p; 
        }
    }
    return null;
}
}
