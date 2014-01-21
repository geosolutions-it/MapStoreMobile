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
package it.geosolutions.android.map.utils.Coordinates;

/**
 * A class containing the coordinates(lon/lat) of a point on earth surface
 * @author Jacopo Pianigiani (jacopo.pianigiani85@gmail.com).
 */
public class Coordinates{
	
	private double x, y;
	
	/**
	 * Default constructor of the class
	 * @param x
	 * @param y
	 */
	public Coordinates(){
		this.x = 0;
		this.y = 0;
	}
	
	/**
	 * Constructor of the class for double parameter
	 * @param x
	 * @param y
	 */
	public Coordinates(double x, double y){
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Return x value for coordinate of point.
	 * @return 
	 */
	public double getX(){
		return x;
	}
	
	/**
	 * Return y value for coordinate of point.
	 * @return 
	 */
	public double getY(){
		return y;
	}
}