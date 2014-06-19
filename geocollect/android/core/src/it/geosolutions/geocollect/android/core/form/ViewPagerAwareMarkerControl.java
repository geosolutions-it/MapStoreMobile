package it.geosolutions.geocollect.android.core.form;

import android.view.MotionEvent;
import android.view.View;
import it.geosolutions.android.map.control.MarkerControl;
import it.geosolutions.android.map.view.AdvancedMapView;
import it.geosolutions.geocollect.android.core.widgets.EnableSwipeViewPager;

public class ViewPagerAwareMarkerControl extends MarkerControl {

	public EnableSwipeViewPager enableSwipeViewPager;
	
	public ViewPagerAwareMarkerControl(AdvancedMapView view, boolean enabled, EnableSwipeViewPager enableSwipeViewPager) {
		super(view, enabled);
		this.enableSwipeViewPager = enableSwipeViewPager;
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		boolean superRes = super.onTouch(view, event);
		if(enableSwipeViewPager != null){
			enableSwipeViewPager.setPagingEnabled(!isDragging());
		}
		return superRes;
	}


}
