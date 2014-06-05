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

/**
 * The Base model for a Form. Contains a submit URL and a data URL.
 * The form contains a list of pages with the fields
 * 
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public class Form implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * A unique identifier for the form.
	 * This allow to store it and keep sync with the remote db.
	 */
	public int id;
	/**
	 * The name of the form.
	 */
	public String name;
	/**
	 * The list of pages to compile
	 */
	public ArrayList<Page> pages;
	/**
	 * The URL of the service that gets this form 
	 */
	public String submitURL;
}
