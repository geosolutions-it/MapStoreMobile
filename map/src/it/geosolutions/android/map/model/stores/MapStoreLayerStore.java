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
package it.geosolutions.android.map.model.stores;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import it.geosolutions.android.map.DataListActivity;
import it.geosolutions.android.map.MapsActivity;
import it.geosolutions.android.map.activities.NewSourceActivity;
import it.geosolutions.android.map.common.Constants;
import it.geosolutions.android.map.geostore.activities.GeoStoreResourcesActivity;
import it.geosolutions.android.map.mapstore.fragment.NewMapStoreSourceFragment;
import it.geosolutions.android.map.model.Layer;

/**
 * A Store that contains Layers from MapSTore
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public class MapStoreLayerStore extends BaseLayerStore {

	String url;
	@Override
	public ArrayList<Layer> getLayers() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * @param string
	 */
	public void setUrl(String url) {
		this.url = url;
		
	}
	

	/**
	 * 
	 * @return the URL
	 */
	public String getUrl() {
		return this.url;
		
	}

	@Override
	public void openDetails(Activity ac) {
		 Intent pref = new Intent(ac,GeoStoreResourcesActivity.class);
		pref.putExtra(GeoStoreResourcesActivity.PARAMS.GEOSTORE_URL,url);
		pref.putExtra(GeoStoreResourcesActivity.PARAMS.LAYERSTORE_NAME,getName());
		ac.startActivityForResult(pref, MapsActivity.MAPSTORE_REQUEST_CODE);
		
	}

	@Override
	public void openEdit(Activity ac) {
		Intent i = new Intent(ac,NewSourceActivity.class);
		i.putExtra(NewMapStoreSourceFragment.PARAMS.STORE, this);
		ac.startActivityForResult(i,Constants.requestCodes.CREATE_SOURCE);
	}

	/* (non-Javadoc)
	 * @see it.geosolutions.android.map.model.stores.LayerStore#canEdit()
	 */
	@Override
	public boolean canEdit() {
		return true;
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(!super.equals(obj)){
			return false;
		}
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MapStoreLayerStore other = (MapStoreLayerStore) obj;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}
}
