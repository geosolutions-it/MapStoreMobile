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
import it.geosolutions.android.map.model.FeatureInfoQuery;
import it.geosolutions.android.map.model.FeatureInfoTaskQuery;

import java.util.ArrayList;

import jsqlite.Exception;
import android.util.Log;
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
	public static FeatureInfoTaskQuery[] createTaskQueryQueue(ArrayList<String> layers, FeatureInfoQuery query, Integer start, Integer limit) {
		final SpatialDataSourceManager sdbManager = SpatialDataSourceManager
				.getInstance();
		int querySize = layers.size();
		FeatureInfoTaskQuery[] queryQueue = new FeatureInfoTaskQuery[querySize];
		int index = 0;
		for (String layer : layers) {
			SpatialVectorTable table;
			try {
				table = sdbManager.getVectorTableByName(layer);
			} catch (Exception e1) {
				Log.e("FEATUREINFO", "unable to get table:" + layer);
				continue;
			}
			FeatureInfoTaskQuery taskquery = new FeatureInfoTaskQuery(query);
			taskquery.setTable(table);
			taskquery.setHandler(sdbManager.getSpatialDataSourceHandler(table));
			taskquery.setStart(start);
			taskquery.setLimit(limit);

			queryQueue[index] = taskquery;
			index++;
		}
		return queryQueue;
	}
}
