package it.geosolutions.geocollect.android.map;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import it.geosolutions.android.map.common.Constants;
import it.geosolutions.android.map.listeners.MapInfoListener;
import it.geosolutions.android.map.model.Layer;
import it.geosolutions.android.map.model.query.BBoxQuery;
import it.geosolutions.android.map.view.AdvancedMapView;

public class ReturningMapInfoListener extends MapInfoListener {

	public ReturningMapInfoListener(AdvancedMapView mapView, Activity activity) {
		super(mapView, activity);
		// TODO Auto-generated constructor stub
	}

	/**
	 * The {@link ReturningMapInfoListener} behavior is to set the selected BBox
	 * and close the activity instead opening the {@link GetFeatureInfoLayerListActivity}
	 */
	@Override
	protected void infoDialog(double n, double w, double s, double e, byte zoomLevel) {
		
		if(getActivity() == null)
			return;
		
		try {
			ArrayList<Layer> layerNames = getLayers();
			Intent data = new Intent();
			data.putExtra(Constants.ParamKeys.LAYERS, layerNames);
			BBoxQuery query = new BBoxQuery();
			query.setE(e);
			query.setN(n);
			query.setS(s);
			query.setW(w);
			query.setZoomLevel(zoomLevel);
			query.setSrid("4326");
			data.putExtra("query", query);
			
			if (getActivity().getParent() == null) {
				getActivity().setResult(Activity.RESULT_OK, data);
			} else {
				getActivity().getParent().setResult(Activity.RESULT_OK, data);
			}
			getActivity().finish();

		} catch (Exception ex) {
			Log.e("MapInfoListener", Log.getStackTraceString(ex));
		}
	}
}
