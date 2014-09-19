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
package it.geosolutions.android.map.spatialite.renderer;

import it.geosolutions.android.map.database.SpatialDataSourceManager;
import it.geosolutions.android.map.model.Layer;
import it.geosolutions.android.map.overlay.MapsforgePointTransformation;
import it.geosolutions.android.map.overlay.Shapes;
import it.geosolutions.android.map.renderer.OverlayRenderer;
import it.geosolutions.android.map.spatialite.SpatialiteLayer;
import it.geosolutions.android.map.style.AdvancedStyle;
import it.geosolutions.android.map.style.StyleManager;
import it.geosolutions.android.map.utils.ProjectionUtils;
import it.geosolutions.android.map.utils.StyleUtils;

import java.util.ArrayList;
import jsqlite.Exception;

import org.mapsforge.android.maps.Projection;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.GeoPoint;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import com.vividsolutions.jts.android.PointTransformation;

import eu.geopaparazzi.spatialite.database.spatial.core.GeometryIterator;
import eu.geopaparazzi.spatialite.database.spatial.core.ISpatialDatabaseHandler;
import eu.geopaparazzi.spatialite.database.spatial.core.SpatialVectorTable;

/**
 * A Renderer for Spatialite Layers
 * 
 * @author Lorenzo Natali(lorenzo.natali@geo-solutions.it)
 * 
 */

public class SpatialiteRenderer implements OverlayRenderer<SpatialiteLayer> {
	private ArrayList<SpatialiteLayer> layers;
	private Projection projection;

	public void render(Canvas c, BoundingBox boundingBox, byte zoomLevel) {
		drawFromSpatialite(c, boundingBox, zoomLevel);
	}

	@Override
	public void setLayers(ArrayList<SpatialiteLayer> pLayers) {
		
		this.layers = new ArrayList<SpatialiteLayer>();
		
		for(Layer l : pLayers){
			if(l instanceof SpatialiteLayer){
				this.layers.add((SpatialiteLayer) l);
			}
		}
	}

	public void refresh() {
		// nothing to update
	}

	@Override
	public ArrayList<SpatialiteLayer> getLayers() {
		return layers;
	}

	private void drawFromSpatialite(Canvas canvas, BoundingBox boundingBox,
			byte drawZoomLevel) {

		if( layers == null){
			// nothing to draw
			return;
		}
		double n = boundingBox.maxLatitude;
		double w = boundingBox.minLongitude;
		double s = boundingBox.minLatitude;
		double e = boundingBox.maxLongitude;

		// replaces the argument
		GeoPoint dp = new GeoPoint(n, w); // ULC
		long[] pxDp= ProjectionUtils.getDrawPoint(dp, projection, drawZoomLevel);
		long drawX = pxDp[0];
		long drawY= pxDp[1];
		try {
			// gets spatialite tables from the spatialite database manager
			SpatialDataSourceManager sdManager = SpatialDataSourceManager.getInstance();

			
			for (SpatialiteLayer l : layers) {
				
				//visibility checks
				if(! l.isVisibility() ) continue;
				if (isInterrupted() || sizeHasChanged()) {
					// stop working
					return;
				}
				
				//check visibility range in style
				AdvancedStyle style4Table = l.getStyle();
				if (!StyleUtils.isInVisibilityRange(style4Table, drawZoomLevel)){
					continue;
				}

				//retrieve the handler 
				SpatialVectorTable spatialTable = sdManager.getVectorTableByName(l.getTableName());
				ISpatialDatabaseHandler spatialDatabaseHandler = l.getSpatialDatabaseHandler();
				if(spatialDatabaseHandler != null){
					//ISpatialDatabaseHandler spatialDatabaseHandler = sdManager.getVectorHandler(spatialTable);
	
					GeometryIterator geometryIterator = null;
					try {
						geometryIterator = spatialDatabaseHandler
								.getGeometryIteratorInBounds("4326", spatialTable,
										n, s, e, w);
	
						Paint fill = null;
						Paint stroke = null;
						if (style4Table.fillcolor != null
								&& style4Table.fillcolor.trim().length() > 0)
							fill = StyleManager.getFillPaint4Style(style4Table);
						if (style4Table.strokecolor != null	&& style4Table.strokecolor.trim().length() > 0)
							stroke = StyleManager.getStrokePaint4Style(style4Table);
	
						PointTransformation pointTransformer = new MapsforgePointTransformation(
								projection, drawX, drawY, drawZoomLevel);
						Shapes shapes = new Shapes(pointTransformer, canvas,
								style4Table, geometryIterator);
						if (spatialTable.isPolygon()) {
							shapes.drawPolygon(fill, stroke);
							
						} else if (spatialTable.isLine()) {
							shapes.drawLine(stroke);
							
						} else if (spatialTable.isPoint()) {
							shapes.drawPoint(fill, stroke);
							
						}
						
						
					} finally {
						if (geometryIterator != null)
							geometryIterator.close();
					}
				}
			}
		} catch (Exception e1) {
			Log.e("SpatialiteRenderer","Exception while rendering spatialite data");
			Log.e("SpatialiteRenderer", e1.getLocalizedMessage(), e1);
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
}
