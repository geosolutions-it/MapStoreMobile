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

import it.geosolutions.android.map.utils.ProjectionUtils;
import it.geosolutions.android.map.wms.WMSLayer;
import it.geosolutions.android.map.wms.WMSLayerChunker;
import it.geosolutions.android.map.wms.WMSRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.mapsforge.core.model.BoundingBox;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.Log;

/**
 * A Renderer for wms services that renders 
 * @author Admin
 *
 */

public  class WMSUntiledRenderer implements WMSRenderer {
	ArrayList<WMSRequest> requests;
	
	//public ArrayList<WMSLayer> layers =new ArrayList<WMSLayer>();
	public void render(Canvas c, BoundingBox boundingBox, byte zoomLevel){
		if(requests ==null){
			Log.d("WMS","request is missing, draw skipped");
			return;
		}
	
		for(WMSRequest r :requests){
			draw(c, r.getURL(createParameters(c,boundingBox)));
		}
	    
	}

	private void draw(Canvas c, URL url) {
		if(url == null) return; //TODO notify
	    HttpURLConnection connection;
		try {
			connection = (HttpURLConnection) url.openConnection();
		} catch (IOException e2) {
			Log.e("WMS","error opening connection");
			return;
		}
	    InputStream is;
		try {
			is = connection.getInputStream();
			Bitmap img = BitmapFactory.decodeStream(is); 
			if(img!=null){
				Log.v("WMS","Map Updated");
				c.drawBitmap(img, 0, 0, null);
			}else {
				Log.e("WMS","null image from the request");
			}
		} catch (IOException e1) {
			Log.e("WMS","unable to read from the wms service");
		}
	}
	
	private HashMap<String,String> createParameters(Canvas c, BoundingBox boundingBox){
		double n = boundingBox.maxLatitude;
        double w = boundingBox.minLongitude;
        double s = boundingBox.minLatitude;
        double e = boundingBox.maxLongitude;
		double nm = ProjectionUtils.toWebMercatorY(n);
		double wm = ProjectionUtils.toWebMercatorX(w);
        double sm =ProjectionUtils.toWebMercatorY(s);
	    double em = ProjectionUtils.toWebMercatorX(e);
	    HashMap<String,String> params = new HashMap<String,String>();
	    params.put("bbox", wm + "," + sm + "," + em + "," + nm);
	    params.put("service","WMS");
	    params.put("srs","EPSG:900913");
	    params.put("request","GetMap");
	    params.put("version","1.1.1");
	    params.put("width",c.getWidth()+"");
	    params.put("height",c.getHeight()+"");
	    
	    return params;
	    
	}


	@Override
	public void setLayers(ArrayList<WMSLayer> layers) {
		requests = WMSLayerChunker.createChunkedRequests(layers);
		Log.v("WMS","request models created:"+ requests.size());
		
	}
}
