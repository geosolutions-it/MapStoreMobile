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

/**
 * An utility class to calculate geodesic distance between two point on earth surface.
 * source: http://www.spadamar.com/2007/12/calcolo-della-distanza-geodetica-tra-due-punti-della-superficie-terrestre/
 * @author Jacopo Pianigiani (jacopo.pianigiani85@gmail.com)
 */
public class GeodesicDistance {
	
	/**
	 * Method to calculate distance in kilometers between two points on earth surface,whose coordinates are 
	 * passed through arguments.
	 * @param lon_startX longitude of first point.
	 * @param lat_startY latitude of first point.
	 * @param lon_endX longitude of second point.
	 * @param lat_endY latitude of second point.
	 * @return
	 */
	public static double getDistance(double lon_startX,  double lat_startY, double lon_endX, double lat_endY){
		double R = 6371; //Radius of earth(expressed in kilometers).
		
		double lat_alfa, lat_beta, lon_alfa, lon_beta;
		//Convert latitude and longitude from degrees to radians 
		lat_alfa = (Math.PI * lat_startY) / 180;
		lat_beta = (Math.PI * lat_endY) / 180;
		lon_alfa = (Math.PI * lon_startX) / 180;
		lon_beta = (Math.PI * lon_endX) / 180;
		
		double fi = Math.abs(lon_alfa - lon_beta);
		double p = Math.acos(Math.sin(lat_beta) * Math.sin(lat_alfa) + 
				Math.cos(lat_beta) * Math.cos(lat_alfa) * Math.cos(fi));

		return p * R;	
	}
}