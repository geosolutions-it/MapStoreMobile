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

import it.geosolutions.android.map.model.Source;

import java.util.HashMap;

public class WMSSource implements Source{
	public HashMap<String,String> baseParams= new HashMap<String,String>();
	private String url;
	private String title;
	
	public WMSSource(String url){
		setDefaultParameters();
		this.url=url;
	}
	
	public String getUrl(){
		return url;
	}
	private void setDefaultParameters(){
		baseParams.put("format","image/png8");
		baseParams.put("transparent","true");
	}
	
}
