/*
 * GeoSolutions GeoSolutions Android Map Library - Digital mapping on Android based devices
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

import it.geosolutions.android.map.R;
import it.geosolutions.android.map.activities.GetFeatureInfoLayerListActivity;
import it.geosolutions.android.map.common.Constants;
import it.geosolutions.android.map.model.Layer;
import it.geosolutions.android.map.model.query.CircleQuery;
import it.geosolutions.android.map.model.query.BBoxQuery;
import it.geosolutions.android.map.overlay.managers.MultiSourceOverlayManager;
import it.geosolutions.android.map.utils.ConversionUtilities;
import it.geosolutions.android.map.view.AdvancedMapView;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * Listener that implements OnTouch Event on map.
 * 
 * @author Lorenzo Natali (www.geo-solutions.it)
 */
public class MapInfoListener implements OnTouchListener {

	// MODES

	private int mode = Constants.Modes.MODE_EDIT;

	private boolean dragStarted;

	private float startX;

	private float startY;

	private float endX;

	private float endY;

	protected AdvancedMapView view;

	private boolean isPinching;

	private Activity activity;

	private SharedPreferences pref; // Used to check shape of selection desired

	/**
	 * Constructor for class MapInfoListener
	 * 
	 * @param mapView
	 * @param activity
	 * @param Shape_Selection
	 */
	public MapInfoListener(AdvancedMapView mapView, Activity activity) {
		view = mapView;
		this.setActivity(activity);
		pref = PreferenceManager.getDefaultSharedPreferences(activity
				.getApplicationContext());
	}

	/**
	 * Create a FeatureQuery for rectangular selection and pass it via intent.
	 * 
	 * @param n
	 * @param w
	 * @param s
	 * @param e
	 * @param zoomLevel
	 */
	protected void infoDialog(final double n, final double w, final double s,
			final double e, byte zoomLevel) {
		try {
			ArrayList<Layer> layerNames = getLayers();
			Intent i = new Intent(view.getContext(),
					GetFeatureInfoLayerListActivity.class);
			i.putExtra(Constants.ParamKeys.LAYERS, layerNames);
			BBoxQuery query = new BBoxQuery();
			query.setE(e);
			query.setN(n);
			query.setS(s);
			query.setW(w);
			query.setZoomLevel(zoomLevel);
			query.setSrid("4326");
			i.putExtra("query", query);
			if (mode == Constants.Modes.MODE_EDIT) {
				i.setAction(Intent.ACTION_PICK);
			} else {
				i.setAction(Intent.ACTION_VIEW);
			}
			getActivity().startActivityForResult(i,
					GetFeatureInfoLayerListActivity.BBOX_REQUEST);

		} catch (Exception ex) {
			Log.e("MapInfoListener", Log.getStackTraceString(ex));
		}
	}

	/**
	 * Get layers from the mapView
	 * 
	 * @return an arrayList of layers
	 */
	protected ArrayList<Layer> getLayers() {
		MultiSourceOverlayManager manager = view.getLayerManager();
		ArrayList<Layer> layers = manager.getLayers();
		ArrayList<Layer> result = new ArrayList<Layer>();
		for (Layer layer : layers) {
			if (layer.isVisibility()) {
				result.add(layer);
			}
		}
		return result;
	}

	/**
	 * Create a Feature Query for circular selection and pass it to an activity
	 * via intent.
	 * 
	 * @param x
	 * @param y
	 * @param radius
	 * @param zoomLevel
	 */
	private void infoDialogCircle(final double x, final double y,
			final double radius, byte zoomLevel) {
		try {
			ArrayList<Layer> layers = getLayers();
			Intent i = new Intent(view.getContext(),
					GetFeatureInfoLayerListActivity.class);
			i.putExtra(Constants.ParamKeys.LAYERS, layers);
			CircleQuery query = new CircleQuery();
			query.setX(x);
			query.setY(y);
			query.setRadius(radius);
			query.setSrid("4326");
			query.setZoomLevel(zoomLevel);
			i.putExtra("query", query);
			if (mode == Constants.Modes.MODE_EDIT) {
				i.setAction(Intent.ACTION_PICK);
			} else {
				i.setAction(Intent.ACTION_VIEW);
			}

			getActivity().startActivityForResult(i,
					GetFeatureInfoLayerListActivity.CIRCLE_REQUEST);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Method to handle touch event on view.
	 * 
	 * @param v
	 * @param event
	 *            class that handle touch.
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		String[] array = getActivity().getResources().getStringArray(
				R.array.preferences_selection_shape);

		int action = event.getAction();
		int pointerCount = event.getPointerCount(); // Number of pointer of
													// device

		// Try to skip pinch events
		if (Log.isLoggable("MAPINFOTOOL", Log.DEBUG)) {// Log check to avoid
														// string
														// creation
			Log.d("MAPINFOTOOL", "performed action:" + action + " on Info Tool");
		}

		if (action == MotionEvent.ACTION_DOWN) {
			if (dragStarted && pointerCount > 1) {
				setDragStarted(false);
				Log.d("MAPINFOTOOL", "drag stopped");
				isPinching = true;
			}
		}
		if (action == MotionEvent.ACTION_MOVE) {
			if (pointerCount > 1 || isPinching) {
				setDragStarted(false);
				Log.d("MAPINFOTOOL", "drag stopped");
				return false;
			}
			// START DRAGGING
			if (!dragStarted) {
				startX = event.getX();
				startY = event.getY();
			}

			setDragStarted(true);
			Log.d("MAPINFOTOOL", "dragging started");
			endX = event.getX();
			endY = event.getY();

			// Force redraw
			view.redraw(false);
			return true;
		} else if (dragStarted && action == MotionEvent.ACTION_UP) {
			if (pointerCount > 1) {
				isPinching = true;
				setDragStarted(false);
				Log.d("MAPINFOTOOL", "drag stopped");
				return false;
			} else if (isPinching) {
				isPinching = false;
				setDragStarted(false);
				Log.d("MAPINFOTOOL", "drag stopped");
				return false;
			}
			endX = event.getX();
			endY = event.getY();
			if (endX == startX || endY == startY) {
				setDragStarted(false);
				isPinching = false;
				return false;
			}
			// END DRAGGING EVENT
			setDragStarted(false);
			Log.d("MAPINFOTOOL", "drag stopped");
			Log.d("MAPINFOTOOL", "start query layer");

			// Use utility class to perform conversion from pixels to
			// latitude/longitude.
			if (pref.getString(
					"selectionShape",
					getActivity().getResources().getString(
							R.string.preferences_selection_shape_default))
					.equals(array[0])) {
				double n = ConversionUtilities.convertFromPixelsToLatitude(
						view, startY);
				double w = ConversionUtilities.convertFromPixelsToLongitude(
						view, startX);
				double s = ConversionUtilities.convertFromPixelsToLatitude(
						view, endY);
				double e = ConversionUtilities.convertFromPixelsToLongitude(
						view, endX);

				Log.v("MAPINFOTOOL", "bbox:" + w + "," + s + "," + e + "," + n);
				infoDialog(n, w, s, e, view.getMapViewPosition().getZoomLevel());
			} else {
				// Calculate radius and coordinates of circle
				double y = ConversionUtilities.convertFromPixelsToLatitude(
						view, startY);
				double x = ConversionUtilities.convertFromPixelsToLongitude(
						view, startX);
				double fin_y = ConversionUtilities.convertFromPixelsToLatitude(
						view, endY);
				double fin_x = ConversionUtilities
						.convertFromPixelsToLongitude(view, endX);
				double rad_x = Math.abs(x - fin_x);
				double rad_y = Math.abs(y - fin_y);
				double radius = Math.sqrt((rad_x * rad_x) + (rad_y * rad_y));

				Log.v("MAPINFOTOOL", "circle: center (" + x + "," + y
						+ ") radius " + radius);
				infoDialogCircle(x, y, radius, view.getMapViewPosition()
						.getZoomLevel());
			}
		}

		return false;
	}

	private void setDragStarted(boolean b) {
		// if(dragStarted ^ b){
		// if (dragStarted= true){
		// this.view.thawOverlays();
		// }else{
		// this.view.freezeOverlays();
		// }
		// }
		dragStarted = b;
	}

	/**
	 * Check if user start dragging
	 * 
	 * @return
	 */
	public boolean isDragStarted() {
		return dragStarted;
	}

	/**
	 * Return start x coordinate for selection
	 * 
	 * @return
	 */
	public float getStartX() {
		return startX;
	}

	/**
	 * Return start y coordinate for selection
	 * 
	 * @return
	 */
	public float getStartY() {
		return startY;
	}

	/**
	 * Return final x coordinate for selection
	 * 
	 * @return
	 */
	public float getEndX() {
		return endX;
	}

	/**
	 * Return final y coordinate for selection
	 * 
	 * @return
	 */
	public float getEndY() {
		return endY;
	}

	/**
	 * Return mode.
	 * 
	 * @return
	 */
	public int getMode() {
		return mode;
	}

	/**
	 * Set mode.
	 * 
	 * @param mode
	 */
	public void setMode(int mode) {
		this.mode = mode;
	}

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

}