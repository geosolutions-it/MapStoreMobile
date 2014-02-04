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
package it.geosolutions.android.map.model.query;

import android.os.Parcel;
import eu.geopaparazzi.spatialite.database.spatial.core.SpatialVectorTable;
import it.geosolutions.android.map.database.SpatialDataSourceHandler;
import it.geosolutions.android.map.model.Layer;

/**
 * TaskQuery for circular selection.
 * @author Jacopo Pianigiani (jacopo.pianigiani85@gmail.com).
 */
public class CircleTaskQuery extends CircleQuery implements FeatureInfoTaskQuery {
	
	/**
	 * Constructor for class FeatureCircleTaskQuery.
	 * @param source
	 */
	public CircleTaskQuery(Parcel source) {
		super(source);
		
	}
	
	/**
	 * Constructor for class FeatureCircleTaskQuery.
	 * @param q
	 */
	public CircleTaskQuery(CircleQuery q){
		super(q);
	}
	
	private Layer layer;
	private Integer start;
	private Integer limit;

	/**
	 * Get start value.
	 * @return
	 */
	public Integer getStart() {
		return start;
	}

	/**
	 * Set start value.
	 * @param start
	 */
	public void setStart(Integer start) {
		this.start = start;
	}

	/**
	 * Get limit value.
	 * @return
	 */
	public Integer getLimit() {
		return limit;
	}

	/**
	 * set limit value.
	 * @param limit
	 */
	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	/**
	 * get the layer to query
	 * @return
	 */
	public Layer getLayer() {
		return layer;
	}

	/**
	 * set the layer to query
	 * @param layer
	 */
	public void setLayer(Layer layer) {
		this.layer = layer;
	}

}