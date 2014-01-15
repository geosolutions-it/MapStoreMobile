package it.geosolutions.android.map.utils;

import org.mapsforge.android.maps.MapScaleBar;
import org.mapsforge.core.util.MercatorProjection;

/**
 * Utility Class to convert WebMercator and Geographic coordinates
 * Waiting for integration of a mor complex library to do that 
 * @author Admin
 *
 */
public class ProjectionUtils {
	public static double toGeographicX( double mercatorX_lon){
		//TODO optimize 
	    if (Math.abs(mercatorX_lon) > 180)
	        throw new IllegalArgumentException();

	    if ((Math.abs(mercatorX_lon) > MercatorProjection.EARTH_CIRCUMFERENCE/2) )
	    	throw new IllegalArgumentException();

	    double x = mercatorX_lon;
	    double num3 = x / 6378137.0;
	    double num4 = num3 * 57.295779513082323;
	    double num5 = Math.floor((double)((num4 + 180.0) / 360.0));
	    return num4 - (num5 * 360.0);
	 	    
	}
	public static double toGeographicY( double mercatorY_lat)
	{
		//TODO optimize 
	    if (Math.abs(mercatorY_lat) > 90){
	    	 throw new IllegalArgumentException();
	    }
	    if ((Math.abs(mercatorY_lat) >  MercatorProjection.EARTH_CIRCUMFERENCE/2) ){
	    	 throw new IllegalArgumentException();
	    }
	    double y = mercatorY_lat;
	    double num7 = 1.5707963267948966 - (2.0 * Math.atan(Math.exp((-1.0 * y) / 6378137.0)));

	    return num7 * 57.295779513082323;
	}

	public static double  toWebMercatorX( double mercatorX_lon)
	{
		//TODO optimize 
	    if (Math.abs(mercatorX_lon) > 180){
	    	throw new IllegalArgumentException();
	    }
	    double num = mercatorX_lon * 0.017453292519943295;
	    return 6378137.0 * num;
	}
	
	public static double toWebMercatorY(   double mercatorY_lat)
	{
		//TODO optimize 
	    if ( Math.abs(mercatorY_lat) > 90){
	    	throw new IllegalArgumentException();
	    }
	    double a = mercatorY_lat * 0.017453292519943295;
	    return 3189068.5 * Math.log((1.0 + Math.sin(a)) / (1.0 - Math.sin(a)));
	}
}
