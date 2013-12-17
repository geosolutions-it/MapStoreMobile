package it.geosolutions.android.map.wms.renderer;

import it.geosolutions.android.map.wms.WMSLayer;

import java.util.ArrayList;

import org.mapsforge.core.model.BoundingBox;

import android.graphics.Canvas;

public interface WMSRenderer {
	public void setLayers(ArrayList<WMSLayer> layers);
	public void render(Canvas c, BoundingBox boundingBox, byte zoomLevel);
}
