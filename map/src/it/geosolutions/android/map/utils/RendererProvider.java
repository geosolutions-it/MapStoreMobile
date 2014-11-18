/*
 *    GeoSolutions Android Map Library
 *    http://www.geo-solutions.it
 *
 *    (C) 2012-2014, GeoSolutions S.A.S
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
package it.geosolutions.android.map.utils;

import it.geosolutions.android.map.mbtiles.MbTilesRenderer;
import it.geosolutions.android.map.mbtiles.MbTilesSource;
import it.geosolutions.android.map.model.Source;
import it.geosolutions.android.map.renderer.OverlayRenderer;
import it.geosolutions.android.map.spatialite.SpatialiteSource;
import it.geosolutions.android.map.spatialite.renderer.SpatialiteRenderer;
import it.geosolutions.android.map.wms.WMSSource;
import it.geosolutions.android.map.wms.renderer.WMSUntiledRenderer;

public class RendererProvider {

	public static OverlayRenderer getRenderer(Source s) {
		if(s instanceof WMSSource){
			return new WMSUntiledRenderer();
		}else if (s instanceof SpatialiteSource){
			return new SpatialiteRenderer();
		}else if (s instanceof MbTilesSource){
			return new MbTilesRenderer();
		}
		return null;
	}

}
