/*
 * GeoSolutions map - Digital field mapping on Android based devices
 * Copyright (C) 2014  GeoSolutions (www.geo-solutions.it)
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
package it.geosolutions.android.map.mbtiles;

import it.geosolutions.android.map.renderer.OverlayRenderer;
import it.geosolutions.android.map.style.AdvancedStyle;
import it.geosolutions.android.map.utils.StyleUtils;

import java.util.ArrayList;
import org.mapsforge.android.maps.Projection;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.GeoPoint;
import org.mapsforge.core.model.Tile;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.TimingLogger;

import eu.geopaparazzi.spatialite.database.spatial.core.ISpatialDatabaseHandler;

/**
 * A Renderer for MBTiles Layers
 * 
 * @author Lorenzo Pini (lorenzo.pini@geo-solutions.it)
 * 
 */
public class MbTilesRenderer implements OverlayRenderer<MbTilesLayer> {

	// -----------------------------------------------
	// copied from
	// - geopaparazzilibrary/src/eu/geopaparazzi/library/util/Utilities.java
	// -----------------------------------------------
	public static double originShift = 2 * Math.PI * 6378137 / 2.0;
	
	private ArrayList<MbTilesLayer> layers;
	private Projection projection;

	/**
	 * Tag for Logging
	 */
	private static String TAG = "MbTilesRenderer";
	
	/**
	 * Draws the tiles of the requested bounding box at the requested zoom level
	 * to the given {@link Canvas}
	 */
	public void render(Canvas c, BoundingBox boundingBox, byte zoomLevel) {
		// Time execution
		TimingLogger timings = new TimingLogger(TAG, "Rendering MBTiles");
		
		// actual rendering
		drawFromMbTile(c, boundingBox, zoomLevel);
		
		// measure and log execution time
		timings.addSplit("Render Done");
		timings.dumpToLog();
	}

	@Override
	public void setLayers(ArrayList<MbTilesLayer> layers) {
		this.layers = layers;
	}

	public void refresh() {
		// nothing to update
	}

	@Override
	public ArrayList<MbTilesLayer> getLayers() {
		return layers;
	}

	/**
	 * Draws the tiles of the given {@link BoundingBox} in the given {@link Canvas}
	 * @param canvas
	 * @param boundingBox
	 * @param drawZoomLevel
	 */
	private void drawFromMbTile (Canvas canvas, BoundingBox boundingBox, byte drawZoomLevel) {

		double n = boundingBox.maxLatitude;
		double w = boundingBox.minLongitude;
		double s = boundingBox.minLatitude;
		double e = boundingBox.maxLongitude;

		if(projection == null){
			// cannot continue
			return;
		}

		// Get the point of the canvas where to start drawing
		GeoPoint dp = new GeoPoint(n, w); // UpperLeftCorner
		//long[] pxDp = ProjectionUtils.getDrawPoint(dp, projection, drawZoomLevel);
		//long drawX = pxDp[0];
		//long drawY = pxDp[1];
		android.graphics.Point bboxPixelPoint = projection.toPixels(dp, null);
		
		
		// Get the row/col values of the tiles covering the area to draw
		int[] tile_bounds =	LatLonBounds_to_TileBounds(
								new double[]{w,n,e,s},
								drawZoomLevel);
		
		int i_min_x = tile_bounds[1]; 
		int i_min_y_osm = tile_bounds[2]; 
		int i_max_x = tile_bounds[3]; 
		int i_max_y_osm = tile_bounds[4];
		
		// get tileLatLonBounds of the upper left tile
		double[] tb = tileLatLonBounds(i_min_x, i_min_y_osm, drawZoomLevel, Tile.TILE_SIZE);

		// Check Lon/Lat bounds
//		if( -180 > tb[0] || tb[0] > 180
//			|| -90 > tb[1] || tb[1] > 90){
//			// Computations gone wrong, skip
//			return;
//		}
		
		GeoPoint mbtileUlc = new GeoPoint(tb[1], tb[0]); // UpperLeftCorner
		//long[] pxMbtile = ProjectionUtils.getDrawPoint(mbtileUlc, projection, drawZoomLevel);
		//long tileX = pxMbtile[0];
		//long tileY = pxMbtile[1];

		// get the screen pixel position
		android.graphics.Point tilePixelPoint = projection.toPixels(mbtileUlc, null);
		
		/*
		if(BuildConfig.DEBUG){
			Log.v(TAG, "BBox Point [ "+bboxPixelPoint.x+" , "+bboxPixelPoint.y+" ]");
			Log.v(TAG, "Tile Point [ "+tilePixelPoint.x+" , "+tilePixelPoint.y+" ]");
		}
		 */
		
		long offsetX = (long) bboxPixelPoint.x - tilePixelPoint.x; 
		long offsetY = (long) bboxPixelPoint.y - tilePixelPoint.y; 
		
			StringBuilder sb = new StringBuilder();
			
			for (MbTilesLayer l : layers) {

				// visibility checks
				if (!l.isVisibility())
					continue;
				if (isInterrupted() || sizeHasChanged()) {
					// stop working
					return;
				}

				// check visibility range in style
				AdvancedStyle style4Table = l.getStyle();
				if (!StyleUtils.isInVisibilityRange(style4Table, drawZoomLevel)) {
					continue;
				}

				// retrieve the handler
				//SpatialRasterTable spatialRasterTable = sdManager.getRasterTableByName(l.getTableName());
				
				ISpatialDatabaseHandler spatialDatabaseHandler = l.getSpatialDatabaseHandler();
				
				if (spatialDatabaseHandler != null) {
										
					////////
					// Prepare the pixel matrix
					int[] pixels = new int[Tile.TILE_SIZE * Tile.TILE_SIZE];
					
					Bitmap decodedBitmap = null;
					Bitmap bitmap = null;		
					

					for(int tile_y = i_min_y_osm; tile_y<=i_max_y_osm; tile_y++ ){
						
						for(int tile_x = i_min_x; tile_x<=i_max_x; tile_x++ ){
						
							// clear all
							sb.delete(0, sb.length());
							
							byte[] rasterBytes = null;
							sb.append(drawZoomLevel).append(",").append(tile_x).append(",").append(tile_y);
							
							String tileQuery = sb.toString();

							// get the raster tile
							rasterBytes = spatialDatabaseHandler.getRasterTile(tileQuery);
							if( rasterBytes == null){
								// got nothing
								/*
								 *  TODO: use a StringBuilder to log inside critical blocks
								if(BuildConfig.DEBUG){
									Log.v(TAG, "Could not find the correct tile");
								}
								*/
								continue;
							}
							
							decodedBitmap = BitmapFactory.decodeByteArray(rasterBytes, 0, rasterBytes.length);
							
							// check if the input stream could be decoded into a bitmap
							if (decodedBitmap != null) {
								// copy all pixels from the decoded bitmap to the color array
								decodedBitmap.getPixels(pixels, 0, Tile.TILE_SIZE, 0, 0, Tile.TILE_SIZE, Tile.TILE_SIZE);
								decodedBitmap.recycle();
							} else {
								for (int i = 0; i < pixels.length; i++) {
										pixels[i] = Color.WHITE;
								}
							}
							
							if(bitmap == null){
								Bitmap.Config conf = Bitmap.Config.ARGB_8888;
								bitmap = Bitmap.createBitmap(Tile.TILE_SIZE, Tile.TILE_SIZE, conf);
							}
							
							// copy all pixels from the color array to the tile bitmap
							bitmap.setPixels(pixels, 0, Tile.TILE_SIZE, 0, 0, Tile.TILE_SIZE, Tile.TILE_SIZE);
							
							// do the actual drawing on canvas
							// TODO: make this Paint configurable
							Paint paint = new Paint();    
							paint.setAlpha(60); // set transparent value here  
							/* 
							 * TODO: investigate the why the tiles must be drawn with an additional Y offset of (-tileSize)
							 * ((tile_y - i_min_y_osm -1 )*tileSize) -offsetY
							 * instead of
							 * ((tile_y - i_min_y_osm )*tileSize) -offsetY
							 * 
							 */
							canvas.drawBitmap(bitmap, ((tile_x-i_min_x)*Tile.TILE_SIZE)-offsetX, ((tile_y - i_min_y_osm - 1)*Tile.TILE_SIZE)-offsetY, paint);
		
							/*
							if(BuildConfig.DEBUG){
								Log.v(TAG, sb.toString());
							}
							*/
						}
					}
					
					if(bitmap != null){
						bitmap.recycle();
					}
					////////
						

				}
			}
		/*
		} catch (Exception e1) {
			if (BuildConfig.DEBUG) {
				Log.e(TAG, "Exception while rendering spatialite data");
				Log.e(TAG, e1.getLocalizedMessage(), e1);
			}
		}
		*/
	}

	private boolean sizeHasChanged() {
		return false;
	}

	private boolean isInterrupted() {
		return false;
	}

	public void setProjection(Projection p) {
		this.projection = p;
	}

	/**
	 * <p>
	 * Code copied from:
	 * http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames#Lon..2Flat._to_tile_numbers
	 * </p>
	 * 20131128: corrections added to correct going over or under max/min extent
	 * - was causing http 400 Bad Requests - updated openstreetmap wiki
	 * 
	 * @param latlong_bounds [position_y,position_x]
	 * @param zoom
	 * @return [zoom,xtile,ytile_osm]
	 */
	public static int[] getTileNumber(final double lat, final double lon,
			final int zoom) {
		int xtile = (int) Math.floor((lon + 180) / 360 * (1 << zoom));
		int ytile_osm = (int) Math.floor((1 - Math.log(Math.tan(Math
				.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat)))
				/ Math.PI)
				/ 2 * (1 << zoom));
		if (xtile < 0)
			xtile = 0;
		if (xtile >= (1 << zoom))
			xtile = ((1 << zoom) - 1);
		if (ytile_osm < 0)
			ytile_osm = 0;
		if (ytile_osm >= (1 << zoom))
			ytile_osm = ((1 << zoom) - 1);
		return new int[] { zoom, xtile, ytile_osm };
	}

	/**
	 * <p>
	 * Code copied from: http://code.google.com/p/gmap-tile-generator/
	 * </p>
	 * 
	 * @param latlong_bounds
	 *            [minx,miny,maxx,minx]
	 * @param i_zoom
	 * @return [zoom,minx, miny, maxx, maxy of tile_bounds]
	 */
	public static int[] LatLonBounds_to_TileBounds(double[] latlong_bounds, int i_zoom) {
		int[] min_tile_bounds = getTileNumber(latlong_bounds[1],
				latlong_bounds[0], i_zoom);
		int[] max_tile_bounds = getTileNumber(latlong_bounds[3],
				latlong_bounds[2], i_zoom);
		return new int[] { i_zoom, min_tile_bounds[1], min_tile_bounds[2],
				max_tile_bounds[1], max_tile_bounds[2] };
	}
	
	/**
	 * Returns bounds of the given tile in EPSG:900913 coordinates
	 * 
	 * <p>
	 * Code copied from: http://code.google.com/p/gmap-tile-generator/
	 * </p>
	 * 
	 * @param tx
	 * @param ty
	 * @param zoom
	 * @return [minx, miny, maxx, maxy]
	 */
	public static double[] tileBounds(int tx, int ty, int zoom, int tileSize) {
		//cast to long needed to go over the 24th zoom level (integer limit overflow)
		double[] min = pixelsToMeters((long)tx * (long)tileSize, (long)ty * (long)tileSize, zoom, tileSize);
		double minx = min[0], miny = min[1];
		double[] max = pixelsToMeters(((long)tx + (long)1) * (long)tileSize, ((long)ty + (long)1) * (long)tileSize, zoom, tileSize);
		double maxx = max[0], maxy = max[1];
		return new double[] { minx, miny, maxx, maxy };
	}
	
	/**
	 * Converts pixel coordinates in given zoom level of pyramid to EPSG:900913
	 * 
	 * <p>
	 * Code copied from: http://code.google.com/p/gmap-tile-generator/
	 * </p>
	 * 
	 * @return
	 */
	public static double[] pixelsToMeters(double px, double py, int zoom, int tileSize) {
		double res = getResolution(zoom, tileSize);
		double mx = px * res - originShift;
		double my = py * res - originShift;
		return new double[] { mx, my };
	}

	/**
	 * Resolution (meters/pixel) for given zoom level (measured at Equator)
	 * 
	 * <p>
	 * Code copied from: http://code.google.com/p/gmap-tile-generator/
	 * </p>
	 * 
	 * @return
	 */
	public static double getResolution(int zoom, int tileSize) {
		// return (2 * Math.PI * 6378137) / (this.tileSize * 2**zoom)
		double initialResolution = 2 * Math.PI * 6378137 / tileSize;
		return initialResolution / Math.pow(2, zoom);
	}
	
	/**
	 * <p>
	 * Code copied from: http://code.google.com/p/gmap-tile-generator/
	 * </p>
	 * 
	 * @param tx
	 * @param ty [osm notation]
	 * @param zoom
	 * @param tileSize
	 * @return [minx, miny, maxx, maxy]
	 */
	public static double[] tileLatLonBounds(int tx, int ty, int zoom, int tileSize) {
		double[] bounds = tileBounds(tx, ty, zoom, tileSize);
		double[] mins = metersToLatLon(bounds[0], bounds[1]);
		double[] maxs = metersToLatLon(bounds[2], bounds[3]);
		return new double[] { mins[1], maxs[0], maxs[1], mins[0] };
	}

	/**
	 * Converts XY point from Spherical Mercator EPSG:900913 to lat/lon in WGS84 Datum
	 * 
	 * <p>
	 * Code copied from: http://code.google.com/p/gmap-tile-generator/
	 * </p>
	 * 
	 * @return
	 */
	public static double[] metersToLatLon(double mx, double my) { 
		// double originShift = 2 * Math.PI * 6378137 / 2.0;
		double lon = (mx / originShift) * 180.0;
		double lat = (my / originShift) * 180.0;
		lat = 180 / Math.PI * (2 * Math.atan(Math.exp(lat * Math.PI / 180.0)) - Math.PI / 2.0);
		return new double[] { -lat, lon };
	}
}
