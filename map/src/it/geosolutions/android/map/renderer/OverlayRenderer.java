package it.geosolutions.android.map.renderer;

import it.geosolutions.android.map.model.Layer;

import java.util.ArrayList;

import org.mapsforge.android.maps.Projection;
import org.mapsforge.core.model.BoundingBox;

import android.graphics.Canvas;

public interface OverlayRenderer <T extends Layer>{
	public void render(Canvas c, BoundingBox boundingBox, byte zoomLevel) throws RenderingException;
	public void setLayers(ArrayList<T> layers);
	public ArrayList<T> getLayers();
	public void setProjection(Projection projection);
	public void refresh();
}
