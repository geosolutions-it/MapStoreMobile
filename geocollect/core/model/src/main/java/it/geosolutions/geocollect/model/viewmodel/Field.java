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
package it.geosolutions.geocollect.model.viewmodel;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import it.geosolutions.geocollect.model.source.XDataType;
import it.geosolutions.geocollect.model.viewmodel.type.XType;

/**
 * Base class for a field in the view model
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public class Field implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Identifier
	 */
	public String fieldId;
	/**
	 * The type of the data (int, double, date, datetime ...)
	 */
	public XDataType type;
	/**
	 * The type of the widget 
	 * (textarea, texfield, email,date selector,date time)
	 */
	public XType xtype; 
	/**
	 * The label for the field
	 */
	public String label;
	/**
	 * the InitialValue.
	 * It can come 
	 *  * from data using notation "data.datafieldname.
	 *  * a generic known datum like today, username using global.today 
	 */
	public String value;
	/**
	 * The format string (useful for date)
	 */
	public String format;
	/**
	 * lines, for multilines only
	 */
	public Integer lines;
	/**
	 * A list of options to use as suggestions(text) or options(spinner)
	 */
	public List<String> options;
	/**
	 * Mandatory value or not (allow empty values)
	 */
	public boolean mandatory;
	/**
	 * The generic attributes (type specific)
	 */
	public Map<String,Object> attributes;

	public Object getAttribute(String key){
		if(attributes==null){
			return null;
		}
		return attributes.get(key);
	}
}
