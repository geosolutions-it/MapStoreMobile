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

import it.geosolutions.android.map.database.SpatialDataSourceManager;
import it.geosolutions.android.map.model.Layer;
import it.geosolutions.android.map.style.AdvancedStyle;
import it.geosolutions.android.map.style.StyleManager;
import eu.geopaparazzi.spatialite.database.spatial.core.ISpatialDatabaseHandler;
import eu.geopaparazzi.spatialite.database.spatial.core.SpatialVectorTable;

public class SpatialiteLayer implements Layer<SpatialiteSource> {
	private String title;
	SpatialiteSource source;
	private String tableName;
	
	public SpatialiteLayer(SpatialVectorTable t) {
		this.title = t.getName();
		this.tableName = t.getName();
		ISpatialDatabaseHandler h= SpatialDataSourceManager.getInstance().getSpatialDataSourceHandler(t);
	}


	public AdvancedStyle  getStyle(){
		StyleManager styleManager =StyleManager.getInstance();
        return styleManager.getStyle(tableName);
	}
	
	boolean visibility =true;
	private int status;
	public SpatialiteSource getSource() {
		return source;
	}


	public void setSource(SpatialiteSource source) {
		this.source = source;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	@Override
	public String getTitle() {
		return title;
	}

	
	@Override
	public boolean isVisibility() {
		return visibility;
	}

	@Override
	public void setVisibility(boolean visibility) {
		this.visibility = visibility;
		
	}


	public String getTableName() {
		return tableName;
	}


	public void setTableName(String tableName) {
		this.tableName = tableName;
	}


	/* (non-Javadoc)
	 * @see it.geosolutions.android.map.model.Layer#setStatus(int)
	 */
	@Override
	public void setStatus(int status) {
		this.status = status;
		
	}


	/* (non-Javadoc)
	 * @see it.geosolutions.android.map.model.Layer#getStatus()
	 */
	@Override
	public int getStatus() {
		return status;
	}

}
