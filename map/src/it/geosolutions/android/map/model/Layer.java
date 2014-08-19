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
package it.geosolutions.android.map.model;

import java.io.Serializable;

/**
 * Interface for a generic <Layer>
 * @author Lorenzo Natali(lorenzo.natali@geo-solutions.it)
 *
 * @param <T> the Source type
 */
public interface Layer <T extends Source> extends Serializable{
	/**
	 * @return the title of the layer
	 */
	public String getTitle();
	
	/**
	 * set the title of the layer
	 * @param title the title
	 */
	public void  setTitle(String title);
	/**
	 * @return get the source of the layer
	 */
	public Source getSource();
	
	/**
	 * set the source for this layer
	 * @param source the source
	 */
	public void setSource(T source);
	
	/**
	 * @return true if the layer is visible, false otherwise
	 */
	public boolean isVisibility();
	
	/**
	 * set the visibility for this layer
	 * @param visibility
	 */
	public void setVisibility(boolean visibility);
	
	/**
	 * The status is ok if status is 0
	 * if different, it contains the message string
	 * @param status
	 */
	public void setStatus(int status);
	
	public int getStatus();
	
	
	/**
	 * The {@link LayerGroup} of this layer, 
	 * can be null if the layer isn't into a group
	 * @param layerGroup
	 */
	public void setLayerGroup(LayerGroup layerGroup);
	
	public LayerGroup getLayerGroup();
	
	/**
	 * The Opacity value of this layer
	 */
	public void setOpacity(double opacityValue);
	
	public double getOpacity();
	
	
}
