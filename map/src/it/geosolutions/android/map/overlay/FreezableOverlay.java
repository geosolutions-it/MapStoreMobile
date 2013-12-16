/*
 *    GeoSolutions Android Map Library
 *    http://www.geo-solutions.it
 *
 *    (C) 2012-2013, GeoSolutions S.A.S
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.android.map.overlay;

import org.mapsforge.android.maps.overlay.Overlay;

/**
 * Interface for <Overlay> that requires too much time to be
 * drawn (e.g. <SpatialiteOverlay>). If the map doesn't move
 * you can notify the <Overlay> this condition doing freeze
 * the layer, if possible, will try to optimize his draw()
 * algorithm to show always the same image (i.e. caching his 
 * bitmap).
 * @author Lorenzo Natali
 *
 */
public interface FreezableOverlay extends Overlay {
   /**
    * Notify the overlay to freeze.
    * This assure that the map will not be moved
    */
   public void freeze();
   /**
    * Notify the <Overlay> that the map
    * can be moved again
    */
   public void  thaw();

}
