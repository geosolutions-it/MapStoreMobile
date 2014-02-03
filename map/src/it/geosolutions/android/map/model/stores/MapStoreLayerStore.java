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
import it.geosolutions.android.map.geostore.activities.GeoStoreResourcesActivity;
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

	@Override
	public void openDetails(Activity ac) {
		 Intent pref = new Intent(ac,GeoStoreResourcesActivity.class);
		pref.putExtra(GeoStoreResourcesActivity.PARAMS.GEOSTORE_URL,url);
		pref.putExtra(GeoStoreResourcesActivity.PARAMS.LAYERSTORE_NAME,getName());
		ac.startActivityForResult(pref, MapsActivity.MAPSTORE_REQUEST_CODE);
		
	}

	@Override
	public void openEdit(Activity ac) {
		
	}
}
