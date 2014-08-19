package it.geosolutions.android.map.utils;

import java.util.ArrayList;

import org.mapsforge.android.maps.Projection;
import org.mapsforge.core.model.BoundingBox;

import android.graphics.Canvas;
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
