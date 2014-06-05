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
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Base class for a Page in the viewmodel
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public class Page implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * the title of the page
	 */
	public String title; 
	/**
	 * iconCls: the icon associated to this form
	 */
	public String iconCls;
	/**
	 * The actions available for this page.
	 */
	public HashMap<String,String> actions;
	/**
	 * The fields in the page
	 */
	public ArrayList<Field> fields;
	/**
	 * swipe 
	 */
	public boolean swipe = true;
	/**
	 * page attributes
	 */
	public HashMap<String,Object> attributes;
	
}
