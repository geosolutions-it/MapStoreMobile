/*
 * GeoSolutions Android map Library - Digital field mapping on Android based devices
 * Copyright (C) 2014  GeoSolutions (www.geo-solutions.it)
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
package it.geosolutions.android.map.spatialite;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import jsqlite.Exception;
import it.geosolutions.android.map.database.SpatialDataSourceHandler;
import it.geosolutions.android.map.database.SpatialDataSourceManager;
import it.geosolutions.android.map.database.spatialite.SpatialiteDataSourceHandler;
import it.geosolutions.android.map.model.Feature;
import it.geosolutions.android.map.model.Layer;
import it.geosolutions.android.map.model.Source;
import it.geosolutions.android.map.model.query.BaseFeatureInfoQuery;
import it.geosolutions.android.map.model.query.CircleTaskQuery;
import it.geosolutions.android.map.model.query.FeatureInfoQuery;
import it.geosolutions.android.map.model.query.FeatureInfoQueryResult;
import it.geosolutions.android.map.model.query.FeatureInfoTaskQuery;
import it.geosolutions.android.map.model.query.PolygonTaskQuery;
import it.geosolutions.android.map.model.query.BBoxTaskQuery;
import it.geosolutions.android.map.style.AdvancedStyle;
import it.geosolutions.android.map.style.StyleManager;
import it.geosolutions.android.map.utils.StyleUtils;
import it.geosolutions.android.map.utils.Coordinates.Coordinates_Query;
import eu.geopaparazzi.spatialite.database.spatial.core.ISpatialDatabaseHandler;
import eu.geopaparazzi.spatialite.database.spatial.core.SpatialVectorTable;

public class SpatialiteSource implements Source {
	private String title;
	private String dataSourceName;
	public SpatialiteSource(SpatialiteDataSourceHandler h) {
		title = h.getFileName();
		
	}

	public SpatialiteSource(ISpatialDatabaseHandler h) {
		title = h.toString();
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
		
	}

	public String getDataSourceName() {
		return dataSourceName;
	}

	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}

	/* (non-Javadoc)
	 * @see it.geosolutions.android.map.model.Source#performQuery(it.geosolutions.android.map.model.query.BaseFeatureInfoQuery, it.geosolutions.android.map.model.query.FeatureInfoQueryResult)
	 */
	@Override
	public int performQuery(FeatureInfoTaskQuery q, List<FeatureInfoQueryResult> r) {
		//TODO when multiple source available, use the handler of this source
		int count = 0;
		if(q instanceof BBoxTaskQuery){
			count = performQueryBBox((BBoxTaskQuery)q,r);
		}
		else if(q instanceof CircleTaskQuery){
			count = performQuery_circle((CircleTaskQuery)q,r);
		}else if(q instanceof PolygonTaskQuery){
			count = performQuery_poly((PolygonTaskQuery)q,r);
		}else{
			Log.w("Spatialite_Source","unrecognized query");
		}
		
		
		//TODO do other types
		return count;
	}

	/**
	 * Performs a query on A bbox
	 * @param query
	 * @param data
	 */
	public int performQueryBBox(BBoxTaskQuery query,
			List<FeatureInfoQueryResult> data){
		
	
	SpatialDataSourceHandler handler = null;
	SpatialVectorTable table = null;
	try {
		table = getTable(query);
		handler = getHandler(table);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		Log.v("Spatialite_Source", "Unable to parse query");
		return 0;
	}
	if(!checkisVisible(table,query.getZoomLevel())){
		return 0;
	}
    double north = query.getN();
    double south = query.getS();
    double east = query.getE();
    double west = query.getW();
    Integer start = query.getStart();
    Integer limit = query.getLimit();
   
    // this is empty to skip geometries that returns errors
    ArrayList<Feature> features = new ArrayList<Feature>();
    try {
        features = handler.intersectionToFeatureListBBOX("4326", table, north,
                south, east, west, start, limit);
    } catch (Exception e) {
        Log.e("Spatialite_Source", "unable to retrive data for table'"
                + table.getName() + "\'.Error:" + e.getLocalizedMessage());
        return 0;
        // TODO now simply skip, do better work
    }
    // add features
    FeatureInfoQueryResult result = new FeatureInfoQueryResult();
    result.setLayer( query.getLayer() );
    result.setFeatures(features);
    Log.v("Spatialite_Source", features.size() + " items found for table "
            +  table.getName() );
    // publishProgress(result);
    data.add(result);
    return features.size();
	}

	
	/**
	 * Returns the visibility of the table based on the zoom level
	 * @param table
	 * @param zoomLevel
	 * @return
	 */
	private boolean checkisVisible(SpatialVectorTable table, byte zoomLevel) {
		AdvancedStyle s = StyleManager.getInstance().getStyle(table.getName());
		//Log.v("Spatialite_Source","zoom level:"+zoomLevel);
	     if ( !StyleUtils.isInVisibilityRange(s, zoomLevel) ) {
	    	 return false;
	     }
		return true;
	}

	/**
	 * Perform a query on a circle.
	 * @param query
	 * @param data
	 * @return
	 */
	private int	performQuery_circle(CircleTaskQuery query,
	        List<FeatureInfoQueryResult> data) {
	    Layer<?> layer = query.getLayer();
	    SpatialDataSourceHandler handler = null;
		SpatialVectorTable table = null;
	    try {
			table = getTable(query);
			handler = getHandler(table);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.v("Spatialite_Source", "Unable to parse query");
			return 0;
		}
	    if(!checkisVisible(table,query.getZoomLevel())){
			return 0;
		}
	    double x = query.getX();
	    double y = query.getY();
	    double radius = query.getRadius();
	    
	    Integer start = query.getStart();
	    Integer limit = query.getLimit();
	    // this is empty to skip geometries that returns errors
	    ArrayList<Feature> features = new ArrayList<Feature>();
	    try {
	        features = handler.intersectionToCircle("4326", table, x, y, radius, start, limit);
	    } catch (Exception e) {
	        Log.e("FEATURE_Circle_TASK", "unable to retrive data for table'"
	                + table.getName() + "\'.Error:" + e.getLocalizedMessage());
	        // TODO now simply skip, do better work
	    }
	    // add features
	    FeatureInfoQueryResult result = new FeatureInfoQueryResult();
	    result.setLayer( query.getLayer() );
	    result.setFeatures(features);
	    
	    
	    // publishProgress(result);
	    data.add(result);
	    return features.size();
	}
	
	/**
	 * Perform a query on a polygon.
	 * @param query
	 * @param data
	 * @return
	 */
	private int performQuery_poly(PolygonTaskQuery query,
	        List<FeatureInfoQueryResult> data) {
		 Layer<?> layer = query.getLayer();
	    SpatialDataSourceHandler handler = null;
		SpatialVectorTable table = null;
	    try {
			table = getTable(query);
			handler = getHandler(table);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.v("Spatialite_Source", "Unable to parse query");
			return 0;
		}
	    if(!checkisVisible(table,query.getZoomLevel())){
			return 0;
		}
	    ArrayList<Coordinates_Query> polygon_points = query.getPolygonPoints(); //get points in format (long/lat).
	    
	    Integer start = query.getStart();
	    Integer limit = query.getLimit();
	    
	    // this is empty to skip geometries that returns errors
	    ArrayList<Feature> features = new ArrayList<Feature>();
	    try {
	        features = handler.intersectionToPolygon("4326", table, polygon_points, start, limit);
	    } catch (Exception e) {
	        Log.e("FEATURE_Polygon_TASK", "unable to retrive data for table'"
	                + table.getName() + "\'.Error:" + e.getLocalizedMessage());
	        // TODO now simply skip, do better work
	    }
	    // add features
	    FeatureInfoQueryResult result = new FeatureInfoQueryResult();
	    result.setLayer( query.getLayer() );
	    result.setFeatures(features);
	    Log.v("FEATURE_Polygon_TASK", features.size() + " items found for table "
	            + table.getName());
	    
	    // publishProgress(result);
	    data.add(result);

	    return features.size();

	}
	
	/**
	 * Provide <SpatialDataSourceHandler> for this table
	 * @param table
	 * @return
	 */
	private SpatialDataSourceHandler getHandler(SpatialVectorTable table) {
		SpatialDataSourceHandler handler;
		SpatialDataSourceManager manager = SpatialDataSourceManager.getInstance();
		handler = manager.getSpatialDataSourceHandler(table);
		return handler;
	}

	/**
	 * Provide the <SpatialVectorTable> of the layer in the query 
	 * @param query
	 * @return
	 * @throws Exception
	 */
	private SpatialVectorTable getTable(FeatureInfoTaskQuery query)
			throws Exception {
		SpatialVectorTable table;
		SpatialiteLayer l = (SpatialiteLayer) query.getLayer();
		SpatialDataSourceManager manager = SpatialDataSourceManager.getInstance();
		table = manager.getVectorTableByName(l.getTableName());
		return table;
	}
	
}
