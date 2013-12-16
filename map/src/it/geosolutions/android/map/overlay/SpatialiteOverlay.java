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

import it.geosolutions.android.map.style.AdvancedStyle;
import it.geosolutions.android.map.style.StyleManager;
import it.geosolutions.android.map.utils.StyleUtils;

import java.util.List;

import jsqlite.Exception;

import org.mapsforge.android.maps.Projection;
import org.mapsforge.android.maps.overlay.Overlay;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.GeoPoint;
import org.mapsforge.core.util.MercatorProjection;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

import com.vividsolutions.jts.android.PointTransformation;
import com.vividsolutions.jts.android.ShapeWriter;
import com.vividsolutions.jts.android.geom.DrawableShape;
import com.vividsolutions.jts.geom.Geometry;
import it.geosolutions.android.map.database.SpatialDataSourceManager;
import eu.geopaparazzi.spatialite.database.spatial.core.GeometryIterator;
import eu.geopaparazzi.spatialite.database.spatial.core.ISpatialDatabaseHandler;
import eu.geopaparazzi.spatialite.database.spatial.core.SpatialVectorTable;



/**
 * 
 * @author Lorenzo Natali (www.geo-solutions.it)
 * Implementation of the overlay for spatialite database. Gets Overlays from the 
 * DatabaseManager and drows geometries getting style from the StyleManager
 * 
 *
 */
public  class SpatialiteOverlay implements Overlay,FreezableOverlay {

    private static final int ITEM_INITIAL_CAPACITY = 8;
    
    private Projection projection;
    Bitmap cacheBitmap;
    boolean isCaching=false;
    public Projection getProjection() {
		return projection;
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
                    drawFromSpatialite(c, bbox,  zoomLevel);
                }
                //draw the cached bitmap on the canvas
                canvas.drawBitmap(cacheBitmap, 0, 0, null);
            }else{
                //normal behiviour
                drawFromSpatialite(canvas, bbox,  zoomLevel);
            }
		
	}
   
    
    
    private void drawFromSpatialite( Canvas canvas, BoundingBox boundingBox, byte drawZoomLevel ) {		

        double n = boundingBox.maxLatitude;
        double w = boundingBox.minLongitude;
        double s = boundingBox.minLatitude;
        double e = boundingBox.maxLongitude;
        
	//replaces the argument
        GeoPoint dp = new GeoPoint(n, w); //ULC

	        long drawX =  (long) MercatorProjection.longitudeToPixelX(dp.longitude, drawZoomLevel);
	        long drawY =  (long) MercatorProjection.latitudeToPixelY(dp.latitude, drawZoomLevel);
		//projection.toPoint(dp, drawPosition,drawZoomLevel);

		

        try {
        	//gets spatialite tables from the spatialite database manager
            SpatialDataSourceManager sdManager = SpatialDataSourceManager.getInstance();
            StyleManager styleManager =StyleManager.getInstance();
            List<SpatialVectorTable> spatialTables = sdManager.getSpatialVectorTables(false);
            for( int i = 0; i < spatialTables.size(); i++ ) {
            	if (isInterrupted() || sizeHasChanged()) {
                    // stop working
                    return;
                }
                SpatialVectorTable spatialTable = spatialTables.get(i);
                AdvancedStyle style4Table = styleManager.getStyle(spatialTable.getName());

               if (!StyleUtils.isVisible(style4Table, drawZoomLevel)) { 
                    continue;
               }
                
                ISpatialDatabaseHandler spatialDatabaseHandler = sdManager.getVectorHandler(spatialTable);

                GeometryIterator geometryIterator = null;
	                            try {
                    geometryIterator = spatialDatabaseHandler.getGeometryIteratorInBounds("4326", spatialTable, n, s, e, w);

                    Paint fill = null;
                    Paint stroke = null;
                    if (style4Table.fillcolor != null && style4Table.fillcolor.trim().length() > 0)
                        fill = styleManager.getFillPaint4Style(style4Table);
                    if (style4Table.strokecolor != null && style4Table.strokecolor.trim().length() > 0)
                        stroke = styleManager.getStrokePaint4Style(style4Table);
                    
                    PointTransformation pointTransformer = new MapsforgePointTransformation(projection, drawX,drawY,
                    drawZoomLevel);
                    Shapes shapes = new Shapes(pointTransformer, canvas, style4Table, geometryIterator);
                    if (spatialTable.isPolygon()) {
                    	shapes.drawPolygon(fill, stroke);                      
                        if (isInterrupted() || sizeHasChanged()) {
                            // stop working
                            return;
                        }
                    } else if (spatialTable.isLine()) {
                    	shapes.drawLine(stroke);
                         if (isInterrupted() || sizeHasChanged()) {
                                // stop working
                                return;
                            }
                    } else if (spatialTable.isPoint()) {
                        	shapes.drawPoint(fill, stroke);
                            if (isInterrupted() || sizeHasChanged()) {
                                // stop working
                                return;
                            }
                        }
                } finally {
                    if (geometryIterator != null)
                        geometryIterator.close();
                }
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
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