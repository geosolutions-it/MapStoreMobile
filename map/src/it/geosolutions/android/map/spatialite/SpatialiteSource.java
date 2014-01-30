/*
 * GeoSolutions Android map Library - Digital field mapping on Android based devices
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
package it.geosolutions.android.map.spatialite;

import it.geosolutions.android.map.database.spatialite.SpatialiteDataSourceHandler;
import it.geosolutions.android.map.model.Source;
import eu.geopaparazzi.spatialite.database.spatial.core.ISpatialDatabaseHandler;

public class SpatialiteSource implements Source {
	private String title;
	private String dataSourceName;
	public SpatialiteSource(SpatialiteDataSourceHandler h) {
		title = h.getFileName();
		
	}

	public SpatialiteSource(ISpatialDatabaseHandler h) {
		title = h.toString();
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
		
	}

	public String getDataSourceName() {
		return dataSourceName;
	}

	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}
	
}
