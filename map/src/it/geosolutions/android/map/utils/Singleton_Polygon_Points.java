/*
 * GeoSolutions GeoSolutions Android Map Library - Digital mapping on Android based devices
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

import java.util.ArrayList;

/**
 * Singleton class to share data points between class for polygonal selecton on map.
 * @author Jacopo Pianigiani (jacopo.pianigiani85@gmail.com).
 *
 */
public class Singleton_Polygon_Points {
	private static Singleton_Polygon_Points istance;
	private ArrayList<Coordinates> polygon_points;

	  /**
	   * Constructor for class.
	   */
	  private Singleton_Polygon_Points(){
		  polygon_points = new ArrayList<Coordinates>();
	  }

	  /**
	   * Static method to allow access to only instance of the class.
	   * @return
	   */
	  public static Singleton_Polygon_Points getInstance(){
	    if (istance == null)
	      istance = new Singleton_Polygon_Points();

	    return istance; 
	  } 
	  
	  /**
	   * Return collection of coordinates of points about polygonal selection.
	   * @param index
	   * @return
	   */
	  public ArrayList<Coordinates> getPoints(){
		  return polygon_points;
	  }
	  
	  /**
	   * Return Coordinates of a specific point of polygonal selection.
	   * @param index
	   * @return
	   */
	  public Coordinates getPoint(int index){
		  return polygon_points.get(index);
	  }
	  
	  /**
	   * Add a point to polygon_points
	   * @param new_point
	   */
	  public void setPoint(Coordinates new_point){
		  polygon_points.add(new_point);
	  }
	  
	  /**
	   * Remove collection of polygonal selection points.
	   */
	  public void reset(){
		  for(int i = 0; i<polygon_points.size(); i++)
			  polygon_points.remove(i);
	  }
}