/*
 * GeoSolutions Android map Library - Digital field mapping on Android based devices
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
package it.geosolutions.android.map.model;

import it.geosolutions.android.map.model.query.FeatureInfoQueryResult;
import it.geosolutions.android.map.model.query.FeatureInfoTaskQuery;

import java.io.Serializable;
import java.util.List;

/**
 * Abstract Layer Source interface
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public interface Source extends Serializable {
	/**
	 * @return the title of the source
	 */
	public String getTitle();
	
	/**
	 * Set the title of the source
	 * @param title 
	 */
	public void setTitle(String title);
	
	/**
	 * Perform a query on the source
	 * @param q the query to perform
	 * @param r the result to provide
	 * @return the number of items found
	 */
	public int performQuery(FeatureInfoTaskQuery q,List<FeatureInfoQueryResult> r);

}
