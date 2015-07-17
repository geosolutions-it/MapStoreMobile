package it.geosolutions.geocollect.android.core.form.action;

import org.mapsforge.core.model.GeoPoint;

import it.geosolutions.android.map.overlay.items.DescribedMarker;
import it.geosolutions.android.map.view.AdvancedMapView;
import it.geosolutions.geocollect.android.core.form.FormPageFragment;
import it.geosolutions.geocollect.android.core.mission.Mission;
import it.geosolutions.geocollect.model.viewmodel.FormAction;
import it.geosolutions.geocollect.model.viewmodel.Page;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.SherlockFragment;

public class CenterOnMarkerAction extends AndroidAction {

	private static final long serialVersionUID = -2858057391209301056L;

	public CenterOnMarkerAction(FormAction a) {
		super(a);
	}

	/**
	 * finds the mapview, looks for the first marker and centers the map on its position
	 */
	@Override
	public void performAction(SherlockFragment fragment, FormAction action,	Mission m, Page p) {

		if(fragment instanceof FormPageFragment){

			final LinearLayout formView = ((FormPageFragment) fragment).getFormView();
			//find mapview
			final AdvancedMapView mapView = findMapView(formView);

			if(mapView == null){
				Log.e(LocalizeAction.class.getSimpleName(), "no mapView found, cannot continue");
				return;
			}

			DescribedMarker setMarker = null;
			//find the marker
			if(mapView.getOverlayManger().getMarkerOverlay().getMarkers() != null && mapView.getOverlayManger().getMarkerOverlay().getMarkers().size() > 0){

				setMarker = mapView.getOverlayManger().getMarkerOverlay().getMarkers().get(0);
				
				GeoPoint geoPoint = setMarker.getGeoPoint();
				//center map on markers position
				mapView.getMapViewPosition().setCenter(geoPoint);
			}
		}
	}

	
	/**
	 * traverses recursively the view hierarchy of the
	 * @param root until it finds a AdvancedMapView instance
	 * @return the instance found or null if none was found
	 */
	public AdvancedMapView findMapView(ViewGroup root) {
		final int childCount = root.getChildCount();

		for (int i = 0; i < childCount; ++i) {
			final View child = root.getChildAt(i);

			if(child instanceof AdvancedMapView){
				return (AdvancedMapView) child;
			}

			if (child instanceof ViewGroup) {
				return findMapView((ViewGroup) child);
			}
		}
		return null;
	}
}
