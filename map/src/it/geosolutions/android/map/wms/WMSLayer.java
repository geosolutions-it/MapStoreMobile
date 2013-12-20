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

import java.util.HashMap;
/**
 * Abstraction of WMSLayer 
 * @author  Lorenzo Natali (lorenzo.natali@geo-solutions.it) 
 */
public class WMSLayer {
	/**
	 * The name of the layer
	 */
	private String name;
	
	/**
	 * The title of the layer
	 */
	
	private String title;
	
	private boolean tiled = false;
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public boolean isVisibility() {
		return visibility;
	}

	public void setVisibility(boolean visibility) {
		this.visibility = visibility;
	}

	public HashMap<String, String> getBaseParams() {
		return baseParams;
	}

	public void setBaseParams(HashMap<String, String> baseParams) {
		this.baseParams = baseParams;
	}
	/**
	 * The Source of the WMSLayer
	 */
	private WMSSource source;
	
	private String group;
	
	private boolean visibility =true;
	
	/**
	 * Parameters like style and cql_filter
	 */
	public HashMap<String,String> baseParams;
	
	
	
	public void setName(String name) {
		this.name = name;
	}

	public WMSSource getSource() {
		return source;
	}
	public void setSource(WMSSource source) {
		this.source = source;
	}
	public WMSLayer(WMSSource source, String name){
		this.source = source;
		this.name=name;
	}
	public String getName(){
		return name;
	}

	public boolean isTiled() {
		return tiled;
	}

	public void setTiled(boolean tiled) {
		this.tiled = tiled;
	}
	
	
}
