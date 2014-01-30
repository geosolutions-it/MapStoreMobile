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
package it.geosolutions.android.map.overlay.managers;

import it.geosolutions.android.map.mapstore.model.MapStoreConfiguration;
import it.geosolutions.android.map.overlay.MarkerOverlay;

import org.mapsforge.android.maps.overlay.MyLocationOverlay;
import org.mapsforge.android.maps.overlay.Overlay;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * The common interface for the object that manages Overlays.
 * They have a SwitcherFragment that manages layer switch events.
 * 
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public interface OverlayManager {
	/**
	 * The marker overlay in the Overlays
	 * @param markerOverlay
	 */
	void setMarkerOverlay(MarkerOverlay markerOverlay);
	
	/**
	 * restore the state on rotation/pause
	 * @param savedInstanceState
	 */
	void restoreInstanceState(Bundle savedInstanceState);

	/**
	 * default initialization operations if the overlaymanager
	 * is just started
	 */
	void defaultInit();

	/**
	 * @return the marker's overlay
	 */
	MarkerOverlay getMarkerOverlay();

	/**
	 * Saves the state on the bundle
	 * @param savedInstanceState
	 */
	void saveInstanceState(Bundle savedInstanceState);

	/**
	 * Complete load of a mapstore configuration
	 * @param serializable
	 */
	void loadMapStoreConfig(MapStoreConfiguration serializable);

	/**
	 * Switch buttons (have to be binded with its switcher) 
	 * @param markers the id 
	 * @param b
	 */
	void toggleOverlayVisibility(int id, boolean b);
	
	/**
	 * forced remove an overlay from the 
	 * @param overlay the overlay to remove 
	 */
	void removeOverlay(Overlay overlay);

	/**
	 * Add a Location Overlay to the Overlay list
	 * @param overlay
	 */
	void addLocationOverlay(MyLocationOverlay overlay);

}
