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

import it.geosolutions.android.map.style.AdvancedStyle;

/**
 * Utility Class for style common methods.
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public class StyleUtils {
	/**
	 * Keept for compatiblity reasons. returns the visibility using the enable flag inside the style and the zoom level.
	 * @param s
	 * @param zoomLevel
	 * @return
	 */
	public static boolean isVisible(AdvancedStyle s,byte zoomLevel){
		return s.enabled !=0 && s.maxZoom >= zoomLevel && zoomLevel >= s.minZoom;
	}
	
	/**
	 * Returns true if the zoom level is in the right range
	 * @param s the style 
	 * @param zoomLevel the current zoom level 
	 * @return
	 */
	public static boolean isInVisibilityRange(AdvancedStyle s,byte zoomLevel){
		return (s.maxZoom >= zoomLevel && zoomLevel >= s.minZoom);
	}
}
