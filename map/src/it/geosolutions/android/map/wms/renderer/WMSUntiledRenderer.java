/*
 * GeoSolutions map - Digital field mapping on Android based devices
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
package it.geosolutions.android.map.wms.renderer;

import it.geosolutions.android.map.R;
import it.geosolutions.android.map.renderer.RenderingException;
import it.geosolutions.android.map.utils.ProjectionUtils;
import it.geosolutions.android.map.wms.WMSLayer;
import it.geosolutions.android.map.wms.WMSLayerChunker;
import it.geosolutions.android.map.wms.WMSRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import org.mapsforge.android.maps.Projection;
import org.mapsforge.core.model.BoundingBox;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;

/**
 * A Simple renderer that draws maps  
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public  class WMSUntiledRenderer implements WMSRenderer{
	ArrayList<WMSRequest> requests;
	private ArrayList<WMSLayer> layers;
	private Projection projection;
	private int status =0;
	private HttpURLConnection connection;
	public void render(Canvas c, BoundingBox boundingBox, byte zoomLevel) throws RenderingException{
		if(requests ==null){
			Log.d("WMS","request is missing, draw skipped");
			return;
		}
	
		for(WMSRequest r :requests){
			URL url = r.getURL(createParameters(c,boundingBox,zoomLevel));
			draw(c,url,boundingBox,zoomLevel);
		}
	    
	}

	/**
	 * Draws the layers on the canvas from a WMS url
	 * @param c
	 * @param url
	 * @param zoomLevel 
	 * @param boundingBox 
	 * @throws RenderingException 
	 */
	private void draw(Canvas c, URL url, BoundingBox boundingBox, byte zoomLevel) throws RenderingException {
		if(url == null) return; //TODO notify
	    if(connection !=null){
	    	connection.disconnect();
	    }
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(1000);//TODO Make this configurable
		} catch (IOException e2) {
			Log.e("WMS","error opening connection");
			notifyError(e2);
			return;
		}
	    InputStream is;
		try {
			is = connection.getInputStream();
			Bitmap img = BitmapFactory.decodeStream(is); 
			if(img!=null){
				long[] pxDp= ProjectionUtils.getMapLeftTopPoint(projection);
				c.drawBitmap(img, pxDp[0] >0 ?  pxDp[0] : 0 , pxDp[1] >0 ?  pxDp[1] : 0, null);
				if(Log.isLoggable("WMS", Log.VERBOSE)){
					Log.v("WMS","Draw downloaded bitmap for "+ layers.size() + "layers from" +url.getHost());
				}
				notifySuccess();
			}else {
				Log.e("WMS","null image from the request");
			}
		} catch (IOException e1) {
			notifyError(e1);
			Log.e("WMS","unable to read from the wms service for url"+url);
		}finally{
			//if the status has changed during draw. the exception is raised
			if(this.status != 0){
				throw new RenderingException(this.status);
			}
		}
	}
	
	/**
	 * Create the parameters for the current location and the default ones too
	 * @param c the Canvas 
	 * @param boundingBox
	 * @param zoomLevel 
	 * @return
	 */
	private HashMap<String,String> createParameters(Canvas c, BoundingBox boundingBox, byte zoomLevel){
		double n = boundingBox.maxLatitude;
        double w = boundingBox.minLongitude;
        double s = boundingBox.minLatitude;
        double e = boundingBox.maxLongitude;
		double nm = ProjectionUtils.toWebMercatorY(n);
		double wm = ProjectionUtils.toWebMercatorX(w);
        double sm =ProjectionUtils.toWebMercatorY(s);
	    double em = ProjectionUtils.toWebMercatorX(e);
	    HashMap<String,String> params = new HashMap<String,String>();
	    //picture size
	    
	    synchronized (projection) {
		    long[] pictureSize = ProjectionUtils.calculateMapSize(c.getWidth(), c.getHeight(), projection);
		    params.put("width",(pictureSize[0])+"");
		    params.put("height",(pictureSize[1])+"");
	    };
	    //Log.v("WMSRenderer","request bbox:"+w+","+s+""+e +","+n);
	    
	    params.put("bbox", wm + "," + sm + "," + em + "," + nm);
	    params.put("service","WMS");
	    params.put("srs","EPSG:900913");
	    params.put("request","GetMap");
	    params.put("version","1.1.1");

	    return params;
	    
	}

	@Override
	public void setLayers(ArrayList layers) {
		this.layers = layers;
		refresh();
	}

	@Override
	public void refresh() {
		requests = WMSLayerChunker.createChunkedRequests(this.layers);
	}

	@Override
	public ArrayList<WMSLayer> getLayers() {
		// TODO Auto-generated method stub
		return layers;
	}

	@Override
	public void setProjection(Projection projection) {
		this.projection = projection;
	}


	/* (non-Javadoc)
	 * @see it.geosolutions.android.map.wms.renderer.WMSRenderer#notifyError(java.lang.Exception)
	 */
	@Override
	public void notifyError(Exception e) {
		if(e instanceof UnknownHostException){
			this.status = R.string.error_connectivity_problem;
		}else{
			this.status = R.string.error_rendering;
		}
		for(WMSLayer l : layers){
			l.setStatus(this.status);
		}	
		
	}

	/* (non-Javadoc)
	 * @see it.geosolutions.android.map.wms.renderer.WMSRenderer#notifySuccess()
	 */
	@Override
	public void notifySuccess() {
		if(status!=0){
			status = 0;
			for(WMSLayer l : layers){
				l.setStatus(0);
			}
		}
		
	}
	
}
