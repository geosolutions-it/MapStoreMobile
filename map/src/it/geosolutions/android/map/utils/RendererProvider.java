package it.geosolutions.android.map.utils;

import java.util.ArrayList;

import org.mapsforge.android.maps.Projection;
import org.mapsforge.core.model.BoundingBox;

import android.graphics.Canvas;
import it.geosolutions.android.map.model.Source;
import it.geosolutions.android.map.renderer.OverlayRenderer;
import it.geosolutions.android.map.spatialite.SpatialiteSource;
import it.geosolutions.android.map.spatialite.renderer.SpatialiteRenederer;
import it.geosolutions.android.map.wms.WMSSource;
import it.geosolutions.android.map.wms.renderer.WMSUntiledRenderer;

public class RendererProvider {

	public static OverlayRenderer getRenderer(Source s) {
		if(s instanceof WMSSource){
			return new WMSUntiledRenderer();
		}else if (s instanceof SpatialiteSource){
			return new SpatialiteRenederer();
		}
		return null;
	}

}
