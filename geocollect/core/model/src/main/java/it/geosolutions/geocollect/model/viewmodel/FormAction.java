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
import java.util.HashMap;

/**
 * Configuration for an action in the ActionBar 
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public class FormAction implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * identifier. This is mandatory, to make the action unique on the system
	 */
	public int id;
	/**
	 * icon to use for action
	 */
	public String iconCls;
	/**
	 * Caption for the action
	 */
	public String text;
	/**
	 * A name to associate to the action
	 */
	public String name;
	/**
	 * Type of the action. The default value is confirm
	 */
	public FormActionType type=FormActionType.confirm;

	/**
	 * Attributes for this action
	 */
	public HashMap<String,Object> attributes;
	/**
	 * The data model to populate  and give to the service(can be different for each service)
	 */
	public Object dataModel;
	@Override
	public String toString() {
		return "Action [id=" + id + ", text=" + text + ", name=" + name
				+ ", type=" + type + "]";
	}
	
	
}
