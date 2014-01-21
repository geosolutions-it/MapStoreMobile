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

/**
 * Task query for rectangular selection
 * @author Lorenzo Natali (www.geo-solutions.it)
 */
public class FeatureRectangularTaskQuery extends FeatureRectangularQuery {
	
	/**
	 * Constructor for class FeatureRectangularTaskQuery.
	 * @param source
	 */
	public FeatureRectangularTaskQuery(Parcel source) {
		super(source);	
	}
	
	/**
	 * Constructor for class FeatureRectangularTaskQuery.
	 * @param q
	 */
	public FeatureRectangularTaskQuery(FeatureRectangularQuery q){
		setE(q.getE());
		setS(q.getS());
		setW(q.getW());
		setN(q.getN());
	}
	
	private SpatialDataSourceHandler handler;
	private SpatialVectorTable table;
	private Integer start;
	private Integer limit;

	/**
	 * Return start value
	 * @return
	 */
	public Integer getStart() {
		return start;
	}

	/**
	 * Set start value
	 * @param start
	 */
	public void setStart(Integer start) {
		this.start = start;
	}

	/**
	 * Return limit value
	 * @return
	 */
	public Integer getLimit() {
		return limit;
	}

	/**
	 * Set limit value
	 * @param limit
	 */
	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public SpatialDataSourceHandler getHandler() {
		return handler;
	}

	public void setHandler(SpatialDataSourceHandler handler) {
		this.handler = handler;
	}

	public SpatialVectorTable getTable() {
		return table;
	}

	public void setTable(SpatialVectorTable table) {
		this.table = table;
	}
}
