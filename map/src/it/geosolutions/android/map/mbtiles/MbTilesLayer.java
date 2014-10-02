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

import org.mapsforge.android.maps.BackgroundSourceType;

import android.preference.PreferenceManager;
import android.util.Log;
import jsqlite.Exception;
import it.geosolutions.android.map.BuildConfig;
import it.geosolutions.android.map.database.SpatialDataSourceManager;
import it.geosolutions.android.map.model.Layer;
import it.geosolutions.android.map.model.LayerGroup;
import it.geosolutions.android.map.style.AdvancedStyle;
import it.geosolutions.android.map.style.StyleManager;
import it.geosolutions.android.map.utils.MapFilesProvider;
import eu.geopaparazzi.spatialite.database.spatial.core.ISpatialDatabaseHandler;
import eu.geopaparazzi.spatialite.database.spatial.core.SpatialRasterTable;

/**
 * Abstraction for an MBTiles Layer
 * @author Lorenzo Pini (lorenzo.pini@geo-solutions.it)
 */
public class MbTilesLayer implements Layer<MbTilesSource> {
	
	/**
	 * Tag for logging
	 */
	protected static final String TAG = "MbTilesLayer";
	
	protected String title;
	protected MbTilesSource source;
	protected String tableName;
	protected LayerGroup layerGroup;
	protected int opacity;
	
	public static int MAX_OPACITY = 255;	
	
	public MbTilesLayer(SpatialRasterTable t) {
		if(t != null){
			this.title = t.getTableName();
			this.tableName = t.getTableName();
		}

		this.opacity = MAX_OPACITY;
		
	}


	public AdvancedStyle  getStyle(){
		StyleManager styleManager =StyleManager.getInstance();
        return styleManager.getStyle(tableName);
	}
	
	boolean visibility =true;
	private int status;
	public MbTilesSource getSource() {
		return source;
	}


	public void setSource(MbTilesSource source) {
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
	
	/**
	 * Returns the renderer associated to this layer
	 * @return
	 */
	public ISpatialDatabaseHandler getSpatialDatabaseHandler(){
		SpatialDataSourceManager sdsm = SpatialDataSourceManager.getInstance();
		if(sdsm != null){
			try {
				if(sdsm.getRasterTableByName(tableName)!=null){
					return sdsm.getRasterHandler(sdsm.getRasterTableByName(tableName));
				}
			} catch (Exception e) {
				if(BuildConfig.DEBUG){
					Log.e("MbTilesLayer", "Exception while getting SpatialDatabaseHandler");
					Log.e("MbTilesLayer", e.getLocalizedMessage(), e);
				}
			}
		}
		return null;
	}


	/**
	 * Set {@link LayerGroup}
	 */
	@Override
	public void setLayerGroup(LayerGroup layerGroup) {
		this.layerGroup = layerGroup;
	}


	/**
	 * Get {@link LayerGroup}
	 */
	@Override
	public LayerGroup getLayerGroup() {
		return this.layerGroup;
	}


	@Override
	public void setOpacity(double opacityValue) {
		
		if(opacityValue < 0 || opacityValue > 255){
			
			// Fully visible
			this.opacity = MAX_OPACITY;
			
		}else{
			
			try{
				this.opacity = (int) Math.floor(opacityValue);
			}catch(ClassCastException cce){
				if(BuildConfig.DEBUG){
					Log.w(TAG, "Cannot cast opacity value to INT");
				}
				this.opacity = MAX_OPACITY;		
			}
			
		}
		
	}


	@Override
	public double getOpacity() {
		return this.opacity;
	}

}
