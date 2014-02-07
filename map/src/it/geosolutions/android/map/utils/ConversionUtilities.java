/*
 * GeoSolutions Android Map Library - Digital field mapping on Android based devices
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

import it.geosolutions.android.map.view.AdvancedMapView;

import org.mapsforge.core.model.GeoPoint;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.core.util.MercatorProjection;

/**
 * An utility class to perform conversion operations of point between pixels and long/lat.
 * @author Jacopo Pianigiani (jacopo.pianigiani85@gmail.com)
 */
public class ConversionUtilities {

	/**
	 * Perform a conversion from pixels to longitude of a point on map.
	 * @param view
	 * @param pixel_x pixels of the point.
	 * @return longitude value for point.
	 */
	public static double convertFromPixelsToLongitude(AdvancedMapView view, double pixel_x){
		MapPosition mapPosition = view.getMapViewPosition()
                .getMapPosition();
        byte zoomLevel = view.getMapViewPosition().getZoomLevel();
        GeoPoint geoPoint = mapPosition.geoPoint;
        
        double pixelLeft = MercatorProjection.longitudeToPixelX(
                geoPoint.longitude, mapPosition.zoomLevel);      
        pixelLeft -= view.getWidth() >> 1;
        double ret = 0;
        try{
        	ret = MercatorProjection.pixelXToLongitude(pixelLeft + pixel_x, zoomLevel);
        }catch(IllegalArgumentException e){
        	ret = 180;
        }
    	return ret;
	}
	
	/**
	 * Perform a conversion from pixels to latitude of a point on map.
	 * @param view
	 * @param pixel_y pixels of the point.
	 * @return latitude value for point.
	 */
	public static double convertFromPixelsToLatitude(AdvancedMapView view, double pixel_y){
		MapPosition mapPosition = view.getMapViewPosition()
                .getMapPosition();
        byte zoomLevel = view.getMapViewPosition().getZoomLevel();
        GeoPoint geoPoint = mapPosition.geoPoint;
        
        double pixelTop = MercatorProjection.latitudeToPixelY(
                geoPoint.latitude, mapPosition.zoomLevel);        
        pixelTop -= view.getHeight() >> 1;
        double ret=0;
        try{
           ret  = MercatorProjection.pixelYToLatitude(pixelTop + pixel_y, zoomLevel);
        }catch(IllegalArgumentException e){
        	ret = MercatorProjection.LATITUDE_MAX;
        }
    	return  ret;
	}	
	
	/**
	 * Perform a conversion from latitude to pixels of a point on map.
	 * @param view
	 * @param latitude latitude of the point.
	 * @return pixels value for point.
	 */
	public static double convertFromLatitudeToPixels(AdvancedMapView view, double latitude){
        byte zoomLevel = view.getMapViewPosition().getZoomLevel();
        GeoPoint mapCenter = view.getMapViewPosition().getCenter(); //MapCenter
     
        // calculate the pixel coordinates of the top left corner
        double pixelY = MercatorProjection.latitudeToPixelY(mapCenter.latitude, zoomLevel)
                - (view.getHeight() >> 1);
   
        return ( MercatorProjection.latitudeToPixelY(latitude, zoomLevel) - pixelY);
	}
	
	/**
	 * Perform a conversion from longitude to pixels of a point on map.
	 * @param view
	 * @param longitude longitude of the point.
	 * @return pixels value for point.
	 */
	public static double convertFromLongitudeToPixels(AdvancedMapView view, double longitude){
        byte zoomLevel = view.getMapViewPosition().getZoomLevel();
        GeoPoint mapCenter = view.getMapViewPosition().getCenter(); //MapCenter
        
        // calculate the pixel coordinates of the top left corner
        double pixelX = MercatorProjection.longitudeToPixelX(mapCenter.longitude, zoomLevel)
        - (view.getWidth() >> 1);
        
        return (MercatorProjection.longitudeToPixelX(longitude, zoomLevel) - pixelX);
	}
}