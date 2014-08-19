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
package it.geosolutions.android.map.mbtiles;

import java.util.List;

import it.geosolutions.android.map.database.SpatialDataSourceHandler;
import it.geosolutions.android.map.database.SpatialDataSourceManager;
import it.geosolutions.android.map.database.spatialite.SpatialiteDataSourceHandler;
import it.geosolutions.android.map.model.Source;
import it.geosolutions.android.map.model.query.FeatureInfoQueryResult;
import it.geosolutions.android.map.model.query.FeatureInfoTaskQuery;
import eu.geopaparazzi.spatialite.database.spatial.core.ISpatialDatabaseHandler;
import eu.geopaparazzi.spatialite.database.spatial.core.SpatialVectorTable;

/**
 * Source for MBTiles files
 * @author Lorenzo Pini
 */
public class MbTilesSource implements Source {
	private String title;
	private String dataSourceName;
	public MbTilesSource(SpatialiteDataSourceHandler h) {
		title = h.getFileName();
		
	}

	public MbTilesSource(ISpatialDatabaseHandler h) {
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

	/* (non-Javadoc)
	 * @see it.geosolutions.android.map.model.Source#performQuery(it.geosolutions.android.map.model.query.BaseFeatureInfoQuery, it.geosolutions.android.map.model.query.FeatureInfoQueryResult)
	 */
	@Override
	public int performQuery(FeatureInfoTaskQuery q, List<FeatureInfoQueryResult> r) {
		// MBTiles cannot be queried
		return 0;
	}
	
	/**
	 * Provide <SpatialDataSourceHandler> for this table
	 * @param table
	 * @return
	 */
	private SpatialDataSourceHandler getHandler(SpatialVectorTable table) {
		SpatialDataSourceHandler handler;
		SpatialDataSourceManager manager = SpatialDataSourceManager.getInstance();
		handler = manager.getSpatialDataSourceHandler(table);
		return handler;
	}

	
}
