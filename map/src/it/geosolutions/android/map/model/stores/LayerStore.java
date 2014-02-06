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

import it.geosolutions.android.map.model.Layer;

import java.io.Serializable;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;

/**
 * Container for a service / file /library that provides layers
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public interface LayerStore extends Serializable {
	public ArrayList<Layer> getLayers();
	public String getName();
	public String getDescription();
	public void setName(String name);
	public void  setDescription(String description);
	public void openDetails(Activity ac);
	public void openEdit(Activity ac);
	public boolean canEdit();
}
