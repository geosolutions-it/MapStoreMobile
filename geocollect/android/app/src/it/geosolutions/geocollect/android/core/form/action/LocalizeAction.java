package it.geosolutions.geocollect.android.core.form.action;

import it.geosolutions.android.map.overlay.items.DescribedMarker;
import it.geosolutions.android.map.view.AdvancedMapView;
import it.geosolutions.geocollect.android.app.R;
import it.geosolutions.geocollect.android.core.form.FormPageFragment;
import it.geosolutions.geocollect.android.core.mission.Mission;
import it.geosolutions.geocollect.android.core.mission.utils.LocationProvider;
import it.geosolutions.geocollect.android.core.mission.utils.LocationProvider.LocationResultCallback;
import it.geosolutions.geocollect.model.viewmodel.FormAction;
import it.geosolutions.geocollect.model.viewmodel.Page;

import org.mapsforge.core.model.GeoPoint;

import android.app.ProgressDialog;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.actionbarsherlock.app.SherlockFragment;

public class LocalizeAction  extends AndroidAction  {

	private static final long serialVersionUID = 496269294596203933L;

	private ProgressDialog progressDialog;
	
	public LocalizeAction(FormAction a) {
		super(a);
	}

	/**
	 * acquires the current position, finds the mapview and centers the marker and the mapview on the acquired position 
	 */
	@Override
	public void performAction(final SherlockFragment fragment, FormAction action, Mission m, Page p) {
		
		if(fragment instanceof FormPageFragment){

			LocationResultCallback locationResult = new LocationResultCallback(){
				@Override
				public void gotLocation(final Location location){
					
					hideProgress();
					
					if(location == null){
						Log.e(LocalizeAction.class.getSimpleName(), "no location acquired, cannot continue");
						return;
					}
					
					final GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
					
					final LinearLayout formView = ((FormPageFragment) fragment).getFormView();
									
					final AdvancedMapView mapView = findMapView(formView);
					
					if(mapView == null){
						Log.e(LocalizeAction.class.getSimpleName(), "no mapView found, cannot continue");
						return;
					}

					if(location != null){
 
						DescribedMarker setMarker = null;
						
						if(mapView.getOverlayManger().getMarkerOverlay().getMarkers() != null && mapView.getOverlayManger().getMarkerOverlay().getMarkers().size() > 0){
							
							setMarker = mapView.getOverlayManger().getMarkerOverlay().getMarkers().get(0);
							
							setMarker.setGeoPoint(geoPoint);
						}
						
						mapView.getMapViewPosition().setCenter(geoPoint);
						
						mapView.getOverlayController().redrawOverlays();

					}
				}
			};
			
			showProgressDialog(fragment);
			new LocationProvider().getLocation(fragment.getActivity(), locationResult);

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
				Log.d(LocalizeAction.class.getSimpleName(), "mapView found");
				return (AdvancedMapView) child;
			}

			if (child instanceof ViewGroup) {
				return findMapView((ViewGroup) child);
			}
		}
		return null;
	}
	/**
	 * shows a progressview
	 * @param fragment the context to show in
	 */
	public void showProgressDialog(SherlockFragment fragment) {
		try{
			LayoutInflater pinflater = LayoutInflater.from(fragment.getActivity());
			View progress = pinflater.inflate(R.layout.progress_spinner, null);
			progressDialog = new ProgressDialog(fragment.getActivity());
			progressDialog.setView(progress);
			progressDialog.setMessage(fragment.getString(R.string.location_searching));
			progressDialog.show();
		}catch(Exception e){
			Log.e(LocalizeAction.class.getSimpleName(), "error showing progress",e);
		}
	}
	/**
	 * hides the progress
	 */
	public void hideProgress(){

		if(progressDialog != null && progressDialog.isShowing()){
			progressDialog.dismiss();
		}	

	}
}
