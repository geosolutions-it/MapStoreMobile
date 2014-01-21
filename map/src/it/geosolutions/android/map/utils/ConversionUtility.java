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
 * An utility class to perform conversion operations of point
 * @author Jacopo Pianigiani (jacopo.pianigiani85@gmail.com)
 */
public class ConversionUtility {

	/**
	 * Perform a conversion from pixels to longitude of a point on map.
	 * @param view
	 * @param longitude
	 * @return
	 */
	public static double convertFromPixelsToLongitude(AdvancedMapView view, double longitude){
		MapPosition mapPosition = view.getMapViewPosition()
                .getMapPosition();
        byte zoomLevel = view.getMapViewPosition().getZoomLevel();
        GeoPoint geoPoint = mapPosition.geoPoint;
        
        double pixelLeft = MercatorProjection.longitudeToPixelX(
                geoPoint.longitude, mapPosition.zoomLevel);
        
        pixelLeft -= view.getWidth() >> 1;
        
    	return MercatorProjection.pixelXToLongitude(pixelLeft + longitude, zoomLevel);
	}
	
	/**
	 * Perform a conversion from pixels to latitude of a point on map.
	 * @param view
	 * @param latitude
	 * @return
	 */
	public static double convertFromPixelsToLatitude(AdvancedMapView view, double latitude){
		MapPosition mapPosition = view.getMapViewPosition()
                .getMapPosition();
        byte zoomLevel = view.getMapViewPosition().getZoomLevel();
        GeoPoint geoPoint = mapPosition.geoPoint;
        
        double pixelTop = MercatorProjection.latitudeToPixelY(
                geoPoint.latitude, mapPosition.zoomLevel);
        
        pixelTop -= view.getHeight() >> 1;
        
    	return MercatorProjection.pixelYToLatitude(pixelTop + latitude, zoomLevel);
	}		
}