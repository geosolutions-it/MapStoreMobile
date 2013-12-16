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


import org.mapsforge.android.maps.Projection;
import org.mapsforge.core.model.GeoPoint;
import org.mapsforge.core.util.MercatorProjection;

import android.graphics.Point;
import android.graphics.PointF;

import com.vividsolutions.jts.android.PointTransformation;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * Transformation that handles mapsforge transforms.
 */
public class MapsforgePointTransformation implements PointTransformation {
    private byte drawZoom;
    private Projection projection;
    private final Point tmpPoint = new Point();
    private long drawX,drawY;

    public MapsforgePointTransformation( Projection projection, long drawX,long drawY, byte drawZoom ) {
        this.projection = projection;
        this.drawX = drawX;
        this.drawY = drawY;
        this.drawZoom = drawZoom;
    }

    public void transform( Coordinate model, PointF view ) {
        double x =   MercatorProjection.longitudeToPixelX(model.x, drawZoom);
        double y =   MercatorProjection.latitudeToPixelY(model.y, drawZoom);
        //projection.toPoint(new GeoPoint(model.y, model.x), tmpPoint, drawZoom);
        view.set((int)(x - drawX),(int)( y - drawY));
    }
}