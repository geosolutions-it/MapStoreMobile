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
package it.geosolutions.geocollect.model.config;

import java.io.Serializable;
import java.util.HashMap;

import it.geosolutions.geocollect.model.source.WFSSource;
import it.geosolutions.geocollect.model.viewmodel.Form;
import it.geosolutions.geocollect.model.viewmodel.Page;

/**
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 * The base template for the collection retrieving and saving
 */
public class MissionTemplate implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * name to display for this Template
	 */
	public String title;
	/**
	 * Source for data to edit
	 */
	public WFSSource source;
	/**
	 * Page to show data in the preview
	 */
	public Page preview;
	/**
	 * name field
	 */
	public String nameField;
	/**
	 * description Field 
	 */
	public String descriptionField;
	/**
	 * The form to compile
	 */
	public Form form;
	/**
	 * Generic data to get from the server to configure objects.
	 * Can be used to bind form editing values.
	 * Example: 
	 * my_allowed_values:["value1","value2"]
	 * 
	 */
	public HashMap<String,Object> config = new HashMap<String,Object>();
}
