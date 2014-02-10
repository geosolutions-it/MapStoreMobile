/*
 * GeoSolutions Android Map Library - Digital field mapping on Android based devices
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
package it.geosolutions.android.map.overlay;

import it.geosolutions.android.map.model.Layer;
import it.geosolutions.android.map.overlay.managers.MultiSourceOverlayManager;
import it.geosolutions.android.map.renderer.MultiSourceRenderer;
import it.geosolutions.android.map.renderer.RenderingException;

import java.util.ArrayList;

import org.mapsforge.android.maps.Projection;
import org.mapsforge.android.maps.overlay.Overlay;
import org.mapsforge.core.model.BoundingBox;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;


/** 
 * Implementation of Overlay that draws <Layer> objects from different sources. 
 * Chunks uses a <MultiSourceRenderer> to render the layers associated
 * @author Lorenzo Natali (www.geo-solutions.it)
 */
public  class MultiSourceOverlay implements Overlay,FreezableOverlay {

    private Projection projection;
    private Bitmap cacheBitmap;
    boolean isCaching=false;
    private MultiSourceRenderer renderer =new MultiSourceRenderer();
    
    ArrayList<Layer> layers = new ArrayList<Layer>();
	private MultiSourceOverlayManager manager;
	private boolean problemsNotified=false;

	/**
	 * @param multiSourceOverlayManager
	 */
	public MultiSourceOverlay(
			MultiSourceOverlayManager multiSourceOverlayManager) {
			manager = multiSourceOverlayManager;
	}

	public ArrayList<Layer> getLayers() {
		return layers;
	}

	public void setLayers(ArrayList<Layer> layers) {
		this.layers = layers;
		this.renderer.setLayers(layers);
	}

	public Projection getProjection() {
		return projection;
	}
    
    public void addLayer(Layer layer){
    		layers.add(layer);
    		renderer.setLayers(layers);
    }

	public void setProjection(Projection projection) {
		this.projection = projection;
		this.renderer.setProjection(projection);
	}

    @Override
	public void draw(BoundingBox bbox, byte zoomLevel, Canvas canvas) {
            if(isCaching){
                //cache a bitmap and draw the features on it
                if(cacheBitmap==null){
                    Canvas c=  new Canvas();
                    cacheBitmap=Bitmap.createBitmap(canvas.getWidth(),
                            canvas.getHeight(),
                            Bitmap.Config.ARGB_8888);
                    c.setBitmap(cacheBitmap);
                    drawLayers(c, bbox,  zoomLevel);
                }
                //draw the cached bitmap on the canvas
                canvas.drawBitmap(cacheBitmap, 0, 0, null);
            }else{
                //normal behiviour
            	Log.v("WMS","draw action");
                drawLayers(canvas, bbox,  zoomLevel);
            }
		
	}
   
    /** Draw the WMS layers from WMS services.
     * Pack send a request for each set of contiguous layers from the same source 
     *  group 
     * @param c
     * @param boundingBox
     * @param zoomLevel
     */
	private void drawLayers(Canvas c, BoundingBox boundingBox, byte zoomLevel) {
		try {
			renderer.render(c, boundingBox, zoomLevel);
			if(problemsNotified == true){
				problemsNotified = false;
				//TODO change status of the layers.
				//manager.getLayerChangeListener().onLayerStatusChange();
			}
			
		} catch (RenderingException e) {
			if(problemsNotified == false){
				problemsNotified = true;
				manager.notifyRenederingException(e);
			}
		}
	}

    @Override
    public void freeze() {
        isCaching=true;
        
    }

    @Override
    public void thaw() {
       isCaching=false;
       if(cacheBitmap!=null){
           cacheBitmap.recycle();
           cacheBitmap=null;
       }
        
    }

    /**
     * Refresh the layers associated to this Overlay
     * @param layer
     */
	public void refreshLayer(Layer layer) {
		renderer.refreshLayer(layer);
		
	}
	
	public void refresh(){
		renderer.refresh();
	}
	

}