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

import it.geosolutions.android.map.wms.WMSLayer;
import it.geosolutions.android.map.wms.renderer.WMSRenderer;
import it.geosolutions.android.map.wms.renderer.WMSUntiledRenderer;

import java.util.ArrayList;

import org.mapsforge.android.maps.Projection;
import org.mapsforge.android.maps.overlay.Overlay;
import org.mapsforge.core.model.BoundingBox;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Log;


/** 
 * Implementation of the overlay to draw from WMS sources. 
 * @author Lorenzo Natali (www.geo-solutions.it)
 */
public  class WMSOverlay implements Overlay,FreezableOverlay {

    private Projection projection;
    private 
    Bitmap cacheBitmap;
    boolean isCaching=false;
    private WMSRenderer renderer =new WMSUntiledRenderer();
    
    public WMSRenderer getRenderer() {
		return renderer;
	}

	public void setRenderer(WMSRenderer renderer) {
		this.renderer = renderer;
	}

	ArrayList<WMSLayer> layers = new ArrayList<WMSLayer>();
    public Projection getProjection() {
		return projection;
	}
    
    public void addLayer(WMSLayer layer){
    		layers.add(layer);
    		renderer.setLayers(layers);
    }

	public void setProjection(Projection projection) {
		this.projection = projection;
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
		renderer.render(c, boundingBox, zoomLevel);
	}
	
	


	private boolean sizeHasChanged() {
		
		return false;
	}



	private boolean isInterrupted() {
		// TODO Control interrupt
		return false;
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
 
}