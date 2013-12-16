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
package it.geosolutions.android.map.renderer;

import it.geosolutions.android.map.database.SpatialDataSourceManager;
import it.geosolutions.android.map.style.AdvancedStyle;
import it.geosolutions.android.map.style.StyleManager;
import jsqlite.Exception;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import com.vividsolutions.jts.android.ShapeWriter;
import com.vividsolutions.jts.android.geom.DrawableShape;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import eu.geopaparazzi.spatialite.database.spatial.core.SpatialVectorTable;

/**
 * @author Lorenzo Natali (www.geo-solutions.it)
 * Draws on a canvas with the proper style
 */
public class LegendRenderer {
	private static final int width= 50;
	private static final int height=50;
	private static final StyleManager sm =StyleManager.getInstance();
	private static final SpatialDataSourceManager sdbm=SpatialDataSourceManager.getInstance();
	private static final WKTReader reader = new WKTReader();
	public static Bitmap getLegend(String layer){
		Bitmap bitmap = Bitmap.createBitmap((int) width, (int) height,Bitmap.Config.ARGB_8888);
		Canvas mCanvas = new Canvas(bitmap);
		SpatialVectorTable table=null;
		try {
			table = sdbm.getVectorTableByName(layer);
		} catch (Exception e) {
			Log.e("LEGEND","Unable to get table");
		}
		if(table==null){
			return null;
		}
		if(table.isLine()){
			 doLineLegend(layer,mCanvas);
			 return bitmap;
		}
		if(table.isPoint()){
			doPointLegend(layer,mCanvas);
			return bitmap;
			
		}
		if(table.isPolygon()){
			doPolygonLegend(layer,mCanvas);
			return bitmap;
		}
		return null;
	}
	

	/**
	 * @param layer
	 * @param mCanvas
	 * @return
	 */
	private static void doPolygonLegend(String layer, Canvas canvas) {
		AdvancedStyle style = sm.getStyle(layer);
		Paint s =sm.getStrokePaint4Style(sm.getStyle(layer));
		Geometry geom = getSamplePolygon();
		if(geom == null) return;
		ShapeWriter wr=new ShapeWriter(new LegendTransformation(width, height));
        DrawableShape shape = wr.toShape(geom);
        Paint fill = sm.getFillPaint4Style(style);
        Paint stroke = sm.getStrokePaint4Style(style);
        if (fill != null){
            shape.fill(canvas, fill);
        }
        if (stroke != null){
            shape.draw(canvas, stroke);
        }
		return;
	}


	/**
	 * @return 
	 * @return
	 */
	private static void doPointLegend(String layer,Canvas canvas) {
		AdvancedStyle style = sm.getStyle(layer);
		Paint s =sm.getStrokePaint4Style(sm.getStyle(layer));
		
		Geometry geom = getSampleGeometryPoint();
		if(geom == null) return;
		ShapeWriter wr=new ShapeWriter(new LegendTransformation(width, height),style.shape,style.size);        
        Paint fill = sm.getFillPaint4Style(style);
        Paint stroke = sm.getStrokePaint4Style(style);
        geom.getCoordinate();
        DrawableShape shape = wr.toShape(geom);
        
        if (fill != null){
            shape.fill(canvas, fill);
        }
        if (stroke != null){
            shape.draw(canvas, stroke);
        }
        
	}
	/**
	 * @param mCanvas 
	 * @return
	 */
	private static void doLineLegend(String layer, Canvas canvas) {
		
		AdvancedStyle style = sm.getStyle(layer);
		Paint s =sm.getStrokePaint4Style(sm.getStyle(layer));
		Geometry geom = getSampleLine();
		if(geom == null) return;
		ShapeWriter wr=new ShapeWriter(new LegendTransformation(width, height));
        DrawableShape shape = wr.toShape(geom);
        Paint fill = sm.getFillPaint4Style(style);
        Paint stroke = sm.getStrokePaint4Style(style);
        if (stroke != null){
            shape.draw(canvas, stroke);
        }
		return;
	}
	
	private static Point getSampleGeometryPoint(){
		try {
			return (Point) reader.read(
			        "POINT(50 50)");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			Log.v("LEGEND","Unable to generate legend for Point");
		}
		return null;
	}
	private static LineString getSampleLine(){
		try {
			return  (LineString) reader.read(
			        "LINESTRING(0 0,100 100)");
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			Log.v("LEGEND","Unable to generate legend for line");
		}
		return null;
        
	}
	private static Polygon getSamplePolygon(){
		try {
			return (Polygon) reader.read(
			        "POLYGON((0 0,0 100,100 100,100 0, 0 0))");
		} catch (ParseException e) {
			Log.v("LEGEND","Unable to generate legend for polygon");
			
		}
		return null;
	}
}
