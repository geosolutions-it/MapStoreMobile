/*
 * GeoSolutions - MapstoreMobile - GeoSpatial Framework on Android based devices
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
package it.geosolutions.android.map.overlay.switcher;

import it.geosolutions.android.map.geostore.model.Resource;
import it.geosolutions.android.map.model.Layer;
import it.geosolutions.android.map.overlay.managers.MultiSourceOverlayManager;

import java.util.List;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;

/**
 * <AsyncTaskLoader> that load layers from a <LayerProvider>
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public class LayerListLoader extends AsyncTaskLoader<List<Layer>> {
	private LayerProvider layerProvider;
	public LayerListLoader(Context context, LayerProvider layerProvider) {
		super(context);
		this.layerProvider = layerProvider;
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public List<Layer> loadInBackground() {
		return layerProvider.getLayers();
	}
	@Override
	protected void onStopLoading() {
		cancelLoad();
	}

	
}