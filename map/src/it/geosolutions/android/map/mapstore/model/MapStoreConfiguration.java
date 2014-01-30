/*
 * GeoSolutions Android map Library - Digital field mapping on Android based devices
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
package it.geosolutions.android.map.mapstore.model;

import it.geosolutions.android.map.wms.WMSSource;

import java.io.Serializable;
import java.util.HashMap;

import com.google.gson.JsonObject;
/**
 * Base container for a MapStore Configuration. Used to convert 
 * a JSON Configuration of mapstore into a Java object 
 * using gson library
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public class MapStoreConfiguration implements Serializable {
	public MapStoreMap map;
	public HashMap<String,MapStoreSource> sources;
	public String data;
	public String defaultSourceType;
}
