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
package it.geosolutions.geocollect.model.source;

import java.io.Serializable;
import java.util.HashMap;

/**
 * The WFS source for the Mission Template
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public class WFSSource implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String type = "WFS";
	/**
	 * URL of the WFS Service
	 */
	public String URL;
	/**
	 * Base parameters for the service. A CQL_FILTER can be included
	 */
	public HashMap<String,String> baseParams;
	/**
	 * name of the FeatureType
	 */
	public String typeName;
	/**
	 * Can be used to have only the list without geometries
	 * and other attributes
	 */
	public String previewParamNames;
	/**
	 * Map with "propertyName":"propertyType". Can be used to keep in the database or save later
	 */
	public HashMap<String,XDataType> dataTypes = new HashMap<String,XDataType>();
	/**
	 * Defines the local database table name where the original mission data should be saved
	 */
	public String localSourceStore;
	/**
	 * Defines the local database table name where the form data should be saved
	 */
	public String localFormStore;

	
}
