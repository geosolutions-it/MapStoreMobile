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
import android.content.Intent;

import it.geosolutions.android.map.DataListActivity;
import it.geosolutions.android.map.MapsActivity;
import it.geosolutions.android.map.model.Layer;
import it.geosolutions.android.map.spatialite.activities.SpatialiteLayerListActivity;

/**
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public class SpatialiteStore extends BaseLayerStore{

	/* (non-Javadoc)
	 * @see it.geosolutions.android.map.model.stores.LayerStore#getLayers()
	 */
	@Override
	public ArrayList<Layer> getLayers() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see it.geosolutions.android.map.model.stores.LayerStore#openDetails(android.app.Activity)
	 */
	@Override
	public void openDetails(Activity ac) {
		Intent datalistIntent = new Intent(ac, SpatialiteLayerListActivity.class);
		ac.startActivityForResult(datalistIntent, MapsActivity.DATAPROPERTIES_REQUEST_CODE);
		
	}

	/* (non-Javadoc)
	 * @see it.geosolutions.android.map.model.stores.LayerStore#openEdit(android.app.Activity)
	 */
	@Override
	public void openEdit(Activity ac) {
		// TODO Auto-generated method stub
		
	}
	
}
