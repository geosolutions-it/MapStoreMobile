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
package it.geosolutions.android.map.utils;

/**
 * A class containing the coordinates of a point about a polygonal selection
 * @author Jacopo Pianigiani (jacopo.pianigiani85@gmail.com).
 */
public class Coordinates{
	
	private float x = 0f, y=0f;
	/**
	 * Default constructor of the class
	 * @param x
	 * @param y
	 */
	public Coordinates(){
		this.x = 0f;
		this.y = 0f;
	}
	
	/**
	 * Constructor of the class for float parameter
	 * @param x
	 * @param y
	 */
	public Coordinates(float x, float y){
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Return x value for coordinate of point
	 * @return 
	 */
	public float getX(){
		return x;
	}
	
	/**
	 * Return y value for coordinate of point
	 * @return 
	 */
	public float getY(){
		return y;
	}
}