/*
 * GeoSolutions map - Digital field mapping on Android based devices
 * Copyright (C) 2013 - 2014  GeoSolutions (www.geo-solutions.it)
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

import java.io.Serializable;
import java.util.ArrayList;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author Lorenzo Natali (www.geo-solutions.it)
 *
 */
public class Feature extends ArrayList<Attribute> implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Geometry geometry;

	/**
	 * @param arrayList
	 */
	public Feature(ArrayList<Attribute> arrayList) {
		addAll(arrayList);
	}

	/**
	 * 
	 */
	public Feature() {
		// TODO Auto-generated constructor stub
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public Attribute getAttribute(String name){
		for(Attribute a : this){
			if(a.getName().equals(name)){
				return a;
			}
		}
		return null;
	}

	/**
	 * @param geometry
	 */
	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
		
	}
	public String getDefaultGeometryProperty(){
		return "the_geom";
	}
}
