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
package it.geosolutions.android.map.utils;

import org.mapsforge.android.maps.Projection;
import org.mapsforge.core.model.GeoPoint;
import org.mapsforge.core.util.MercatorProjection;

import android.graphics.Point;

/**
 * Utility Class to convert WebMercator and Geographic coordinates
 * Waiting for integration of a mor complex library to do that 
 * CHECK THIS CLASS results of toGeographicX and Y is incorrect!
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it
 *
 */
public class ProjectionUtils {
	
	/**
	 * Convert a EPSG:900913 longitude (meeters) into geographic WGS:84 (degrees)
	 * @param mercatorX_lon the longitude  in Mercator projection
	 * @return the longitude in WGS84
	 */
	public static double toGeographicX( double mercatorX_lon){ 
	    if ((Math.abs(mercatorX_lon) > MercatorProjection.EARTH_CIRCUMFERENCE/2) )
	    	throw new IllegalArgumentException();

	    double x = mercatorX_lon;
	    double num3 = x / 6378137.0;
	    double num4 = num3 * 57.295779513082323;
	    double num5 = Math.floor((double)((num4 + 180.0) / 360.0));
	    return num4 - (num5 * 360.0);
	 	    
	}
	
	/**
	 * Convert a EPSG:900913 latitude (meeters) into geographic EPSG:4326 (degrees)
	 * @param mercatorY_lat the latitude  in Mercator projection
	 * @return the latitude in WGS84
	 */
	public static double toGeographicY( double mercatorY_lat){
	    if ((Math.abs(mercatorY_lat) >  MercatorProjection.EARTH_CIRCUMFERENCE/2) ){
	    	 throw new IllegalArgumentException();
	    }
	    double y = mercatorY_lat;
	    double num7 = 1.5707963267948966 - (2.0 * Math.atan(Math.exp((-1.0 * y) / 6378137.0)));

	    return num7 * 57.295779513082323;
	}

	/**
	 * Convert a  geographic longitude WGS84 (degrees) into EPSG:900913 latitude (meeters)
	 * @param lon the longitude to convert
	 * @return longitude in meeters (EPSG:900913)
	 */
	public static double  toWebMercatorX( double lon){
		//TODO optimize 
	    if (Math.abs(lon) > 180 ){
	    	throw new IllegalArgumentException();
	    }
	    double num = lon * 0.017453292519943295;
	    return 6378137.0 * num;
	}
	
	/**
	 * Convert a  geographic latitude WGS84 (degrees) into EPSG:900913 latitude (meeters)
	 * @param lat latitude to convert
	 * @return latitude in meeters (EPSG:900913)
	 */
	public static double toWebMercatorY(double lat)	{
		//TODO optimize 
	    if ( Math.abs(lat) > 90){
	    	throw new IllegalArgumentException();
	    }
	    double a = lat * 0.017453292519943295;
	    return 3189068.5 * Math.log((1.0 + Math.sin(a)) / (1.0 - Math.sin(a)));
	}
	
	/**
	 * Gets the left-up corner and returns the point on the canvas to draw to
	 * @param dp the top left corner of the map
	 * @param projection the mapview projection
	 * @param drawZoomLevel the current zoom level
	 * @return array of pixels [x,y]
	 */
	public static long[] getDrawPoint(GeoPoint dp, Projection projection, byte drawZoomLevel){
		long drawX = (long) MercatorProjection.longitudeToPixelX(dp.longitude,
				drawZoomLevel);
		long drawY = (long) MercatorProjection.latitudeToPixelY(dp.latitude,
				drawZoomLevel);
		Point p = new Point(0,0);
        projection.toPixels(new GeoPoint(MercatorProjection.LATITUDE_MAX,-180), p);

        if(p.x >0){
        	drawX -=p.x;
        }
        if(p.y>0){
        	drawY-=p.y;
        }
    	return new long[]{drawX,drawY};
	}

	/**
	 * Returns the point on the mapView of the top left point of the Mercator Left top
	 * @param projection
	 * @param zoomLevel
	 * @return 
	 */
	public static long[] getMapLeftTopPoint(Projection projection) {
		Point p = new Point(0,0);
        projection.toPixels(new GeoPoint(MercatorProjection.LATITUDE_MAX,-180), p);
    	return new long[]{p.x,p.y};
	
		
	}
	
	/**
	 * Returns the point on the mapView of the bottom point of the Mercator bottom right
	 * @param projection
	 * @param zoomLevel
	 * @return 
	 */
	public static long[] getMapRightBottom(Projection projection){
		Point p = new Point(0,0);
		projection.toPixels(new GeoPoint(MercatorProjection.LATITUDE_MIN,180), p);
		return new long[]{p.x,p.y};
	}
	
	/**
	 * Calulate the size of a WMS request getting proper width/height from the projection
	 * @param width
	 * @param height
	 * @param projection
	 * @return
	 */
	public static long[] calculateMapSize(long width,long height,Projection projection){
		long[] lt = getMapLeftTopPoint(projection);
		long[] rb = getMapRightBottom(projection);

		long[] out =new long[2];
		if(rb[0] < width){
			width = rb[0]; 
		}
		if(lt[0] > 0){
			out[0] = width -lt[0];
		}else{
			out[0] = width;
		}
		if(rb[1] < height){
			height = rb[1]; 
		}
		if(lt[1] > 0){
			out[1] = height -lt[1];
		}else{
			out[1] = height;
		}
		return out;
		
		
		
	}
}
