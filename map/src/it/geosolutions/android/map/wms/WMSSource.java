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
import it.geosolutions.android.map.model.query.BaseFeatureInfoQuery;
import it.geosolutions.android.map.model.query.FeatureInfoQueryResult;
import it.geosolutions.android.map.model.query.FeatureInfoTaskQuery;
import it.geosolutions.android.map.renderer.OverlayRenderer;

import java.util.HashMap;
import java.util.List;

/**
 * Represents a WMS Source
 * @author Lorenzo Natali(lorenzo.natali@geo-solutions.it)
 *
 */
public class WMSSource implements Source{
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((baseParams == null) ? 0 : baseParams.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WMSSource other = (WMSSource) obj;
		if (baseParams == null) {
			if (other.baseParams != null)
				return false;
		} else if (!baseParams.equals(other.baseParams))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

	public HashMap<String,String> baseParams= new HashMap<String,String>();
	private String url;
	private String title;
	
	/**
	 * Create a WMSSource using its URL
	 * @param url
	 */
	public WMSSource(String url){
		setDefaultParameters();
		this.url=url;
	}
	
	/**
	 * @return the URL of the WMS Service
	 */
	public String getUrl(){
		return url;
	}
	
	/**
	 * set default parameters, to be applied to all the layers
	 */
	private void setDefaultParameters(){
		baseParams.put("format","image/png8");
		baseParams.put("transparent","true");
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	/* (non-Javadoc)
	 * @see it.geosolutions.android.map.model.Source#performQuery(it.geosolutions.android.map.model.query.FeatureInfoQuery, it.geosolutions.android.map.model.query.FeatureInfoQueryResult)
	 */
	@Override
	public int performQuery(FeatureInfoTaskQuery q, List<FeatureInfoQueryResult> r) {
		//TODO implement it;
		return 0;
		
	}
	
}
