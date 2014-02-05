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
package it.geosolutions.android.map.common;

/**
 * This class provides constants for common interface between activities and other useful constants
 * like request codes and result codes used in the application
 * We don't use string parameters because we don't want to go in conflict with internationalization
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public class Constants {
	public class ParamKeys{
		/**
		 * Used to pass layers to the feature info query
		 */
		public static final String LAYERS = "layers";
	}

	public static class Modes{

		// MODES
		public static final int MODE_VIEW = 0;
		public static final int MODE_EDIT = 1;
		
	}
	public static class requestCodes{
		public static final int CREATE_SOURCE =193;
	}
}
