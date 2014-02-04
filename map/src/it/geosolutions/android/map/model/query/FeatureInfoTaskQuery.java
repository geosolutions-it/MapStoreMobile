/*
 * GeoSolutions - MapstoreMobile - GeoSpatial Framework on Android based devices
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
package it.geosolutions.android.map.model.query;

import it.geosolutions.android.map.model.Layer;

/**
 * Interface for a layer to a query. 
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public interface FeatureInfoTaskQuery extends FeatureInfoQuery {
	public Integer getStart();
	

	/**
	 * Set start value.
	 * @param start
	 */
	public void setStart(Integer start);

	/**
	 * Get limit value.
	 * @return
	 */
	public Integer getLimit() ;

	/**
	 * set limit value.
	 * @param limit
	 */
	public void setLimit(Integer limit);

	/**
	 * get the layer to query
	 * @return
	 */
	public Layer getLayer() ;

	/**
	 * set the layer to query
	 * @param layer
	 */
	public void setLayer(Layer layer) ;
}
