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
package it.geosolutions.android.map.listeners;

import java.util.ArrayList;
import java.util.List;
import org.mapsforge.core.model.GeoPoint;
import org.mapsforge.core.model.MapPosition;
import org.mapsforge.core.util.MercatorProjection;
import eu.geopaparazzi.spatialite.database.spatial.core.SpatialVectorTable;
import it.geosolutions.android.map.utils.Coordinates_Query;
import it.geosolutions.android.map.activities.GetFeatureInfoLayerListActivity;
import it.geosolutions.android.map.database.SpatialDataSourceManager;
import it.geosolutions.android.map.model.FeaturePolygonQuery;
import it.geosolutions.android.map.style.AdvancedStyle;
import it.geosolutions.android.map.style.StyleManager;
import it.geosolutions.android.map.utils.Coordinates;
import it.geosolutions.android.map.utils.Singleton_Polygon_Points;
import it.geosolutions.android.map.utils.StyleUtils;
import it.geosolutions.android.map.view.AdvancedMapView;
import android.app.Activity;
import android.content.Intent;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.MotionEvent;
import android.view.View;

/**
 * Listener to implements double tap event on map.
 * @author Jacopo Pianigiani (jacopo.pianigiani85@gmail.com).
 */
public class DoubleTapListener implements OnDoubleTapListener{	
	
	// MODES
	public static final int MODE_VIEW = 0;

	public static final int MODE_EDIT = 1;
	
	private AdvancedMapView view;
	private Activity activity;
	private GestureDetector gd;
	private ArrayList<Coordinates_Query> points;
	private boolean pointsAcquired; 
	
	private int mode = MODE_EDIT;

	/**
	 * Constructor for class DoubleTapListener
	 * @param view
	 */
	public DoubleTapListener(AdvancedMapView view, Activity activity){
		this.view = view;
		this.activity = activity;
		pointsAcquired = false;
	
		final GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener(){		
			/**
			 * Called when a double tap event has occured.
			 * @param e
			 * @return 
			 */
			@Override
		    public boolean onDoubleTap(MotionEvent e) {
				pointsAcquired = true;
				preparePoints();
				infoDialogPolygon(points);
		        return false;
		    }

		    @Override
		    public void onLongPress(MotionEvent e){}
		};
		
		gd = new GestureDetector(view.getContext(),listener);
		gd.setOnDoubleTapListener(listener);
		gd.setIsLongpressEnabled(true);

		view.setOnTouchListener(new View.OnTouchListener() {
		    @Override
		    public boolean onTouch(View view, MotionEvent event) {
		        return gd.onTouchEvent(event);
		    }
		});
	}
		
	/**
	 * Create a new ArrayList with points captured converted from pixel to latitude/longitude, ready for query
	 */
	public void preparePoints(){
		MapPosition mapPosition = this.view.getMapViewPosition()
                .getMapPosition();
        byte zoomLevel = view.getMapViewPosition().getZoomLevel();
        GeoPoint geoPoint = mapPosition.geoPoint;
        
        double pixelLeft = MercatorProjection.longitudeToPixelX(
                geoPoint.longitude, mapPosition.zoomLevel);
        double pixelTop = MercatorProjection.latitudeToPixelY(
                geoPoint.latitude, mapPosition.zoomLevel);
        pixelLeft -= view.getWidth() >> 1;
        pixelTop -= view.getHeight() >> 1;
        
        points = new ArrayList<Coordinates_Query>();
        ArrayList<Coordinates> polygon_points = Singleton_Polygon_Points.getInstance().getPoints();
        for(int i = 0; i<polygon_points.size(); i++){ //Exclude last point beacuse with double tap it will be captured two times
        	double x = MercatorProjection.pixelXToLongitude(pixelLeft + polygon_points.get(i).getX(), zoomLevel);
        	double y = MercatorProjection.pixelYToLatitude(pixelTop + polygon_points.get(i).getY(), zoomLevel);
        	Coordinates_Query to_add = new Coordinates_Query(x,y);
        	points.add(to_add);
        }
        
        //Singleton_Polygon_Points.getInstance().reset(); //Clear points captured
	}
	
	/**
	 * Create a Feature Query for polygonal selection and passes to an GetFeatureInfoLayerListActivity via intent.
	 * @param polygon_points
	 */
	private void infoDialogPolygon(final ArrayList<Coordinates_Query> polygon_points){
		try {
	        final SpatialDataSourceManager sdbManager = SpatialDataSourceManager
	                .getInstance();
	        final List<SpatialVectorTable> spatialTables = sdbManager
	                .getSpatialVectorTables(false);
	        final StyleManager styleManager = StyleManager.getInstance();
	        final byte zoomLevel = view.getMapViewPosition().getZoomLevel();
	        ArrayList<String> layerNames = new ArrayList<String>();
	        for (SpatialVectorTable table : spatialTables) {
	            String tableName = table.getName();
	            AdvancedStyle style = styleManager.getStyle(tableName);

	            // skip this table if not visible
	            if (StyleUtils.isVisible(style, zoomLevel)) {
	                layerNames.add(table.getName());

	            }
	        }
	        Intent i = new Intent(view.getContext(),
	                GetFeatureInfoLayerListActivity.class); 
	        i.putExtra("layers", layerNames);
	        FeaturePolygonQuery query = new FeaturePolygonQuery();
	        query.setPolygonPoints(polygon_points);
	        query.setSrid("4326");
	        i.putExtra("query", query);
	        i.putExtra("selection","Polygonal"); //Indicate that user has choosed polygonal selection
	        if (mode == MODE_EDIT) {
	            i.setAction(Intent.ACTION_PICK);
	        } else {
	            i.setAction(Intent.ACTION_VIEW);
	        }
	        activity.startActivityForResult(i,
	                GetFeatureInfoLayerListActivity.POLYGON_REQUEST);

	    } catch (Exception ex) {
	        ex.printStackTrace();
	    }
		
		points.removeAll(points); //TOGLI E FAI IL CLEAR DALL'ESTERNO!
		//pointsAcquired = false;
	}

	/**
	 * Return mode value
	 * @return
	 */
	public int getMode() {
	    return mode;
	}

	/**
	 * Set mode
	 * @param mode
	 */
	public void setMode(int mode) {
	    this.mode = mode;
	}
	
	/**
	 * Return polygon points of selection to MapInfoControl for drawing.
	 * @return
	 */
	public ArrayList<Coordinates_Query> getPolygonPoints(){
		return points;
	}
	
	/**
	 * Return true if a double tap event has been captured and all points of a polygonal selection
	 * are available.
	 * @return
	 */
	public boolean pointsAcquired(){
		return pointsAcquired;
	}

	@Override
	public boolean onDoubleTap(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}
}