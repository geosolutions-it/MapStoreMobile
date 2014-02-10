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
package it.geosolutions.android.map.wms;

import it.geosolutions.android.map.model.Layer;

import java.util.HashMap;
/**
 * Abstraction of WMSLayer 
 * @author  Lorenzo Natali (lorenzo.natali@geo-solutions.it) 
 */
public class WMSLayer implements Layer<WMSSource>{
	/**
	 * The name of the layer
	 */
	private String name;
	/**
	 * The Source of the WMSLayer
	 */
	private WMSSource source;
	/**
	 * The group of the layer
	 */
	private String group;
	/**
	 * the visibility
	 */
	private boolean visibility =true;
	/**
	 * The title of the layer
	 */
	
	private String title;
	
	private boolean tiled = false;
	
	/**
	 * Create a WMS layer getting the source and the layer name
	 * @param source the <WMSSource> object that represents the WMS service
	 * @param name the name of the layer in the WMS service
	 */
	public WMSLayer(WMSSource source, String name){
		this.source = source;
		this.name=name;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return the group of the layer
	 */
	public String getGroup() {
		return group;
	}

	/**
	 * Set the group of the layer
	 * @param group
	 */
	public void setGroup(String group) {
		this.group = group;
	}


	public boolean isVisibility() {
		return visibility;
	}

	public void setVisibility(boolean visibility) {
		this.visibility = visibility;
	}

	/**
	 * @return a map of the base parameters for this layer
	 */
	public HashMap<String, String> getBaseParams() {
		return baseParams;
	}

	/**
	 * set the base parameters for this layer
	 * @param baseParams
	 */
	public void setBaseParams(HashMap<String, String> baseParams) {
		this.baseParams = baseParams;
	}
	
	
	/**
	 * Parameters like style and cql_filter
	 */
	public HashMap<String,String> baseParams;
	private int status;
	
	
	/**
	 * set the layer name (to use in WMS services)
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * get the Name of the layer
	 * @return
	 */
	public String getName(){
		return name;
	}

	public WMSSource getSource() {
		return source;
	}
	
	public void setSource(WMSSource source) {
		this.source = source;
	}
	
	/**
	 * @return if the layer is tiled or not
	 */
	public boolean isTiled() {
		return tiled;
	}

	/**
	 * set the "tiled" parameter
	 * @param tiled
	 */
	public void setTiled(boolean tiled) {
		this.tiled = tiled;
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
