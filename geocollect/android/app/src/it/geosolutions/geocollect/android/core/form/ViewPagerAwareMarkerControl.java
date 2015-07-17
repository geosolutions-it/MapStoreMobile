package it.geosolutions.geocollect.android.core.form;

import it.geosolutions.android.map.control.MarkerControl;
import it.geosolutions.android.map.view.AdvancedMapView;
import it.geosolutions.geocollect.android.core.widgets.EnableSwipeViewPager;
import android.view.MotionEvent;
import android.view.View;
/**
 * Custom Marker Control to disable the swipePager when the User is dragging the marker
 * This will stop the page to move and will enable a correct dragging of the marker.
 * 
 * @author Lorenzo Pini
 *
 */
public class ViewPagerAwareMarkerControl extends MarkerControl {

	public EnableSwipeViewPager enableSwipeViewPager;
	
	public ViewPagerAwareMarkerControl(AdvancedMapView view, boolean enabled, EnableSwipeViewPager enableSwipeViewPager) {
		super(view, enabled);
		this.enableSwipeViewPager = enableSwipeViewPager;
	}

	/**
	 * Override of onTouch to disable the SwipeViewPager if the user is dragging the marker
	 */
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		boolean superRes = super.onTouch(view, event);
		if(enableSwipeViewPager != null){
			enableSwipeViewPager.setPagingEnabled(!isDragging());
		}
		return superRes;
	}


}
