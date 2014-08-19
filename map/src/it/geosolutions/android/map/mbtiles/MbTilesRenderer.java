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

import it.geosolutions.android.map.BuildConfig;
import it.geosolutions.android.map.database.SpatialDataSourceManager;
import it.geosolutions.android.map.renderer.OverlayRenderer;
import it.geosolutions.android.map.style.AdvancedStyle;
import it.geosolutions.android.map.utils.ProjectionUtils;
import it.geosolutions.android.map.utils.StyleUtils;

import java.util.ArrayList;
import java.util.Arrays;

import jsqlite.Exception;

import org.mapsforge.android.maps.Projection;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.GeoPoint;
import org.mapsforge.core.model.Tile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import eu.geopaparazzi.spatialite.database.spatial.core.ISpatialDatabaseHandler;
import eu.geopaparazzi.spatialite.database.spatial.core.SpatialRasterTable;

/**
 * A Renderer for MBTiles Layers
 * 
 * @author Lorenzo Pini(lorenzo.pini@geo-solutions.it)
 * 
 */
public class MbTilesRenderer implements OverlayRenderer<MbTilesLayer> {

	private ArrayList<MbTilesLayer> layers;
	private Projection projection;

	/**
	 * Tag for Logging
	 */
	private static String TAG = "MbTilesRenderer";
	
	/**
	 * Draws the tiles of the requested bounding box at the requested zoomlevel
	 * to the given {@link Canvas}
	 */
	public void render(Canvas c, BoundingBox boundingBox, byte zoomLevel) {
		drawFromMbTile(c, boundingBox, zoomLevel);
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

	private void drawFromMbTile (Canvas canvas, BoundingBox boundingBox, byte drawZoomLevel) {

		double n = boundingBox.maxLatitude;
		double w = boundingBox.minLongitude;
		double s = boundingBox.minLatitude;
		double e = boundingBox.maxLongitude;

		// Get the point of the canvas where to start drawing
		GeoPoint dp = new GeoPoint(n, w); // UpperLeftCorner
		long[] pxDp = ProjectionUtils.getDrawPoint(dp, projection, drawZoomLevel);
		long drawX = pxDp[0];
		long drawY = pxDp[1];
		
		int[] tile_bounds =	LatLonBounds_to_TileBounds(
								new double[]{w,s,e,n},
								drawZoomLevel);
		
		Log.v(TAG, "Tilebounds "+Arrays.toString(tile_bounds));
		
		int i_min_x = tile_bounds[1]; 
		int i_min_y_osm = tile_bounds[4]; 
		int i_max_x = tile_bounds[3]; 
		int i_max_y_osm = tile_bounds[2];
		

		try {
			// gets mbtiles layers from the spatialite database manager
			SpatialDataSourceManager sdManager = SpatialDataSourceManager.getInstance();
			
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
				SpatialRasterTable spatialRasterTable = sdManager.getRasterTableByName(l.getTableName());
				
				ISpatialDatabaseHandler spatialDatabaseHandler = l.getSpatialDatabaseHandler();
				
				if (spatialDatabaseHandler != null) {
										
					////////
					int tileSize = Tile.TILE_SIZE;
					// Prepare the pixel matrix
					int[] pixels = new int[Tile.TILE_SIZE * Tile.TILE_SIZE];
					
					Bitmap decodedBitmap = null;
					Bitmap bitmap = null;		
					
					for(int tile_min_y = i_min_y_osm; tile_min_y<=i_max_y_osm; tile_min_y++ ){
						for(int tile_min_x = i_min_x; tile_min_x<=i_max_x; tile_min_x++ ){
						
							// clear all
							sb.delete(0, sb.length());
							
							byte[] rasterBytes = null;
							sb.append(drawZoomLevel).append(",").append(tile_min_x).append(",").append(tile_min_y);
							
							String tileQuery = sb.toString();
							if(BuildConfig.DEBUG){
								Log.v(TAG, tileQuery);
							}
							// get the raster tile
							rasterBytes = spatialDatabaseHandler.getRasterTile(tileQuery);
							if( rasterBytes == null){
								// got nothing
								if(BuildConfig.DEBUG){
									Log.v(TAG, "Could not find the correct tile");
								}
								continue;
							}
							
							decodedBitmap = BitmapFactory.decodeByteArray(rasterBytes, 0, rasterBytes.length);
							
							// check if the input stream could be decoded into a bitmap
							if (decodedBitmap != null) {
								// copy all pixels from the decoded bitmap to the color array
								decodedBitmap.getPixels(pixels, 0, tileSize, 0, 0, tileSize, tileSize);
								decodedBitmap.recycle();
							} else {
								for (int i = 0; i < pixels.length; i++) {
										pixels[i] = Color.WHITE;
								}
							}
							
							if(bitmap == null){
								Bitmap.Config conf = Bitmap.Config.ARGB_8888;
								bitmap = Bitmap.createBitmap(tileSize, tileSize, conf);
							}
							
							// copy all pixels from the color array to the tile bitmap
							bitmap.setPixels(pixels, 0, tileSize, 0, 0, tileSize, tileSize);
							
							// do the actual drawing on canvas
							Paint paint = new Paint();    
							paint.setAlpha(60); //you can set your transparent value here    
							canvas.drawBitmap(bitmap, (tile_min_x-i_min_x)*tileSize, (tile_min_y-i_min_y_osm)*tileSize, paint);
		
						}
					}
					
					if(bitmap != null){
						bitmap.recycle();
					}
					////////
						

				}
			}
		} catch (Exception e1) {
			if (BuildConfig.DEBUG) {
				Log.e(TAG, "Exception while rendering spatialite data");
				Log.e(TAG, e1.getLocalizedMessage(), e1);
			}
		}
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
	public static int[] LatLonBounds_to_TileBounds(double[] latlong_bounds,
			int i_zoom) {
		int[] min_tile_bounds = getTileNumber(latlong_bounds[1],
				latlong_bounds[0], i_zoom);
		int[] max_tile_bounds = getTileNumber(latlong_bounds[3],
				latlong_bounds[2], i_zoom);
		return new int[] { i_zoom, min_tile_bounds[1], min_tile_bounds[2],
				max_tile_bounds[1], max_tile_bounds[2] };
	}
}
