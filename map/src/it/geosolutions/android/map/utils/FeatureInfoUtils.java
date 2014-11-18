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

import it.geosolutions.android.map.database.SpatialDataSourceManager;
import it.geosolutions.android.map.model.Layer;
import it.geosolutions.android.map.model.query.CircleQuery;
import it.geosolutions.android.map.model.query.PolygonQuery;
import it.geosolutions.android.map.model.query.CircleTaskQuery;
import it.geosolutions.android.map.model.query.BBoxQuery;
import it.geosolutions.android.map.model.query.BBoxTaskQuery;
import it.geosolutions.android.map.model.query.PolygonTaskQuery;

import java.util.ArrayList;
import eu.geopaparazzi.spatialite.database.spatial.core.SpatialVectorTable;

/**
 * @author Lorenzo Natali (www.geo-solutions.it)
 *
 */
public class FeatureInfoUtils {
	/**
	 * Creates a task query queue from the original query, adding start and limit and the proper layer handlers.
	 * @param sdbManager
	 * @param layers
	 * @param querySize
	 * @param query
	 * @param limit 
	 * @param start 
	 * @return
	 */
	public static BBoxTaskQuery[] createTaskQueryQueue(ArrayList<Layer> layers, BBoxQuery query, Integer start, Integer limit) {
		final SpatialDataSourceManager sdbManager = SpatialDataSourceManager
				.getInstance();
		int querySize = layers.size();
		BBoxTaskQuery[] queryQueue = new BBoxTaskQuery[querySize];
		int index = 0;
		for (Layer<?> layer : layers) {
			
			BBoxTaskQuery taskquery = new BBoxTaskQuery(query);
			taskquery.setLayer(layer);
			taskquery.setStart(start);
			taskquery.setLimit(limit);

			queryQueue[index] = taskquery;
			index++;
		}
		return queryQueue;
	}
	
	/**
	 * Creates a task query queue from the original query, adding start and limit and the  layer.
	 * @param sdbManager
	 * @param layers
	 * @param querySize
	 * @param query
	 * @param limit 
	 * @param start 
	 * @return
	 */
	public static CircleTaskQuery[] createTaskQueryQueue(ArrayList<Layer> layers, CircleQuery query, Integer start, Integer limit) {
		
		int querySize = layers.size();
		CircleTaskQuery[] queryQueue = new CircleTaskQuery[querySize];
		int index = 0;
		for (Layer layer : layers) {
			SpatialVectorTable table;
			CircleTaskQuery taskquery = new CircleTaskQuery(query);
			taskquery.setLayer(layer);
			taskquery.setStart(start);
			taskquery.setLimit(limit);

			queryQueue[index] = taskquery;
			index++;
		}
		return queryQueue;
	}
	
	/**
	 * Creates a task query queue from the original query, adding start and limit and the layer.
	 * @param sdbManager
	 * @param layers
	 * @param querySize
	 * @param query
	 * @param limit 
	 * @param start 
	 * @return
	 */
	public static PolygonTaskQuery[] createTaskQueryQueue(ArrayList<Layer> layers, PolygonQuery query, Integer start, Integer limit) {
		int querySize = layers.size();
		PolygonTaskQuery[] queryQueue = new PolygonTaskQuery[querySize];
		int index = 0;
		for (Layer layer : layers) {
			PolygonTaskQuery taskquery = new PolygonTaskQuery(query);
			taskquery.setLayer(layer);
			taskquery.setStart(start);
			taskquery.setLimit(limit);

			queryQueue[index] = taskquery;
			index++;
		}
		return queryQueue;
	}
	
}
