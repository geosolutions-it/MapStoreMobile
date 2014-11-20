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
package it.geosolutions.android.map.overlay.items;

import it.geosolutions.android.map.model.Feature;

import org.mapsforge.android.maps.MapViewPosition;
import org.mapsforge.android.maps.Projection;
import org.mapsforge.android.maps.overlay.Marker;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.GeoPoint;
import org.mapsforge.core.model.Point;
import org.mapsforge.core.util.MercatorProjection;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
/**
 * Described Highlighted Marker
 * @author Lorenzo Natali (www.geo-solutions.it)
 *
 */
public class DescribedMarker extends Marker {
        /**
         * The id of the marker
         */
	private String id;
	private String description;
	private boolean highlight;
	private int type;
	private String source;
	private String featureId;
	private Feature feature;
	
	private boolean textVisible = false;
	
	public String getFeatureId() {
        return featureId;
    }
    public void setFeatureId(String featureId) {
        this.featureId = featureId;
    }
    
    //STROKE
	private static final int FILL_COLOR = Color.BLUE;
	private static final int FILL_ALPHA = 100;
	private static final int STROKE_COLOR = Color.BLACK;
	private static final int STROKE_ALPHA = 100;
	private static final int STROKE_WIDTH = 3;
	private static final Paint PAINT_STROKE=new Paint();
	private static final Paint PAINT_FILL=new Paint();
	private static final Paint PAINT_TEXT=new Paint();
	
	/**
	 * Create a marker
	 * @param point
	 * @param boundCenterBottom
	 */
	public DescribedMarker(GeoPoint point, Drawable boundCenterBottom) {
		super(point,boundCenterBottom);
		createPaint();
	}
	/**
	 * 
	 */
	private static void createPaint() {
		 // fill
		PAINT_FILL.setStyle(Paint.Style.FILL);
		PAINT_FILL.setColor(FILL_COLOR); 
		PAINT_FILL.setAlpha(FILL_ALPHA);

	    // border
		PAINT_STROKE.setAntiAlias(true);
		PAINT_STROKE.setColor(STROKE_COLOR);
		PAINT_STROKE.setAlpha(STROKE_ALPHA);
		PAINT_STROKE.setStrokeWidth(STROKE_WIDTH);
		
	    // text
		PAINT_TEXT.setAntiAlias(true);
		PAINT_TEXT.setColor(STROKE_COLOR);
		PAINT_TEXT.setAlpha(STROKE_ALPHA);
		PAINT_TEXT.setStrokeWidth(STROKE_WIDTH);
		PAINT_TEXT.setTextSize(40);
		PAINT_TEXT.setUnderlineText(true);
		
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public void highlightOn(){
		highlight(true);
	}
	public void highlightOff(){
		highlight(false);
	}
	public void highlight(boolean h){
		highlight=h;
	}
	public boolean isHighlight(){
		return highlight;
		
	}
	
	/**
	 * @return the textVisible
	 */
	public boolean isTextVisible() {
		return textVisible;
	}
	/**
	 * @param textVisible the textVisible to set
	 */
	public void setTextVisible(boolean textVisible) {
		this.textVisible = textVisible;
	}
	@Override
	/* (non-Javadoc)
	 * @see org.mapsforge.android.maps.overlay.Marker#draw(org.mapsforge.core.model.BoundingBox, byte, android.graphics.Canvas, org.mapsforge.core.model.Point)
	 */
	public synchronized boolean draw(BoundingBox boundingBox, byte zoomLevel,
			Canvas canvas, Point canvasPosition) {
		GeoPoint geoPoint = this.getGeoPoint();
		Drawable drawable = this.getDrawable();
		
		if (geoPoint == null || drawable == null) {
			return false;
		}

		double latitude = geoPoint.latitude;
		double longitude = geoPoint.longitude;
		int pixelX = (int) (MercatorProjection.longitudeToPixelX(longitude, zoomLevel) - canvasPosition.x);
		int pixelY = (int) (MercatorProjection.latitudeToPixelY(latitude, zoomLevel) - canvasPosition.y);

		Rect drawableBounds = drawable.copyBounds();
		int left = pixelX + drawableBounds.left;
		int top = pixelY + drawableBounds.top;
		int right = pixelX + drawableBounds.right;
		int bottom = pixelY + drawableBounds.bottom;

		if (!intersect(canvas, left, top, right, bottom)) {
			return false;
		}
		//TODO HighLight
		if(isHighlight()){
			canvas.drawCircle(pixelX, pixelY, drawableBounds.width()/2,PAINT_FILL) ;
			canvas.drawCircle(pixelX, pixelY, drawableBounds.width()/2,PAINT_STROKE) ;
		}
		
		if(textVisible){
			canvas.drawText(description, pixelX-40, pixelY+30, PAINT_TEXT);
		}
		
		drawable.setBounds(left, top, right, bottom);
		drawable.draw(canvas);
		drawable.setBounds(drawableBounds);
		return true;
	}
	
	public boolean matchPoint(MapViewPosition mapViewPosition,Projection projection ,float x,float y) {
		GeoPoint geoPoint = this.getGeoPoint();
		Drawable drawable = this.getDrawable();
		BoundingBox boundingBox = mapViewPosition.getBoundingBox();
		byte zoomLevel =mapViewPosition.getZoomLevel();
		double canvasPixelLeft = MercatorProjection.longitudeToPixelX(boundingBox.minLongitude, zoomLevel);
		double canvasPixelTop = MercatorProjection.latitudeToPixelY(boundingBox.maxLatitude, zoomLevel);
		Point canvasPosition = new Point(canvasPixelLeft, canvasPixelTop);
		if (geoPoint == null || drawable == null) {
			return false;
		}

		double latitude = geoPoint.latitude;
		double longitude = geoPoint.longitude;
		int pixelX = (int) (MercatorProjection.longitudeToPixelX(longitude, zoomLevel) - canvasPosition.x);
		int pixelY = (int) (MercatorProjection.latitudeToPixelY(latitude, zoomLevel) - canvasPosition.y);

		Rect drawableBounds = drawable.copyBounds();
		int left = pixelX + drawableBounds.left;
		int top = pixelY + drawableBounds.top;
		int right = pixelX + drawableBounds.right;
		int bottom = pixelY + drawableBounds.bottom;
		
		if(intersect(x,y,left,top,right,bottom)){
			return true;
		}
		
		return false;
		//TODO translate x and y in pixels 

	}
	
	/**
	 * return the x y position of the marker relative to the point
	 * @param canvasPosition
	 * @param zoomLevel
	 * @return
	 */
	public Point getXY(MapViewPosition mapViewPosition,Projection projection){
		GeoPoint geoPoint = this.getGeoPoint();
		Drawable drawable = this.getDrawable();
		
		if (geoPoint == null || drawable == null) {
			return null;
		}
		BoundingBox boundingBox = mapViewPosition.getBoundingBox();
		byte zoomLevel =mapViewPosition.getZoomLevel();
		double canvasPixelLeft = MercatorProjection.longitudeToPixelX(boundingBox.minLongitude, zoomLevel);
		double canvasPixelTop = MercatorProjection.latitudeToPixelY(boundingBox.maxLatitude, zoomLevel);
		Point canvasPosition = new Point(canvasPixelLeft, canvasPixelTop);

		double latitude = geoPoint.latitude;
		double longitude = geoPoint.longitude;
		int pixelX = (int) (MercatorProjection.longitudeToPixelX(longitude, zoomLevel) - canvasPosition.x);
		int pixelY = (int) (MercatorProjection.latitudeToPixelY(latitude, zoomLevel) - canvasPosition.y);
		return  new Point(pixelX, pixelY);
	}
	
	
	/**
	 * Move the GeoPoint to a certain point related to the mapViewPosition
	 * @param x
	 * @param y
	 * @param mapViewPosition
	 * @param projectionl
	 */
	public void setXY(double x, double y, MapViewPosition mapViewPosition,Projection projectionl){
		BoundingBox boundingBox = mapViewPosition.getBoundingBox();
		byte zoomLevel =mapViewPosition.getZoomLevel();
		double canvasPixelLeft = MercatorProjection.longitudeToPixelX(boundingBox.minLongitude, zoomLevel);
		double canvasPixelTop = MercatorProjection.latitudeToPixelY(boundingBox.maxLatitude, zoomLevel);
		double lon = MercatorProjection.pixelXToLongitude(x+canvasPixelLeft, zoomLevel);
		double lat = MercatorProjection.pixelYToLatitude(y+canvasPixelTop, zoomLevel);
		setGeoPoint(new GeoPoint(lat, lon));
	}
	/**
	 * @param x
	 * @param y
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 * @return
	 */
	private boolean intersect(float x, float y, int left, int top, int right,
			int bottom) {
		return x >=left && x<=right && y >=top && y <=bottom;
	}
	/**
	 * Recreated because is not visible
	 * @param canvas
	 * @param left
	 * @param top
	 * @param right
	 * @param bottom
	 * @return
	 */
	private static boolean intersect(Canvas canvas, float left, float top, float right, float bottom) {
		return right >= 0 && left <= canvas.getWidth() && bottom >= 0 && top <= canvas.getHeight();
	}
	/**
	 * @param type
	 */
	public void setType(int type) {
		this.type=type;
		
	}
	
	public int getType(){
		return type;
	}
        public String getSource() {
            return source;
        }
        public void setSource(String source) {
                this.source = source;
        }
        public Feature getFeature() {
            return feature;
        }
        public void setFeature(Feature feature) {
            this.feature = feature;
        }
}
