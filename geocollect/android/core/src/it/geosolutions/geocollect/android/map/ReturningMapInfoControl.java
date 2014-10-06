package it.geosolutions.geocollect.android.map;

import android.app.Activity;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import it.geosolutions.android.map.control.MapControl;
import it.geosolutions.android.map.control.MapInfoControl;
import it.geosolutions.android.map.view.AdvancedMapView;
import it.geosolutions.geocollect.android.core.R;

public class ReturningMapInfoControl extends MapInfoControl implements Parcelable {

	/**
	 * 
	 * @param mapView
	 * @param activity
	 */
	public ReturningMapInfoControl(AdvancedMapView mapView, Activity activity) {
		super(mapView, activity);
	}

	/**
	 * Empty Constructor to create a MapInfoControl without an attached
	 * {@link AdvancedMapView} and {@link Activity} The receiving Activity MUST
	 * attach an {@link AdvancedMapView} and an {@link Activity} to this control
	 * before use.
	 */
	public ReturningMapInfoControl() {
		super(null, null);
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
	}

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<ReturningMapInfoControl> CREATOR = new Parcelable.Creator<ReturningMapInfoControl>() {
        public ReturningMapInfoControl createFromParcel(Parcel in) {
            return new ReturningMapInfoControl();
        }

        public ReturningMapInfoControl[] newArray(int size) {
            return new ReturningMapInfoControl[size];
        }
    };

    @Override
    public void instantiateListener() {
    	if(activity != null){
    		this.mapListener = new ReturningMapInfoListener(mapView,activity);
			pref  = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
			array = activity.getResources().getStringArray(R.array.preferences_selection_shape);
			defaultShapeSelection = activity.getResources().getString(R.string.preferences_selection_shape_default); //default selection rectangular
		}
    };

	@Override
	public OnClickListener getActivationListener() {

		return new OnClickListener(){

			@Override
			public void onClick(View button) {

				if (button.isSelected()){
					button.setSelected(false);
					disable();	            
				} else {
					if(getGroup() != null){
						for(MapControl c : getGroup()){
							c.disable();
							c.getActivationButton().setSelected(false);
						}
					}
					button.setSelected(true);
					enable();				
					
					Toast.makeText(button.getContext(),button.getContext().getString(R.string.create_rectangle), Toast.LENGTH_SHORT).show();
				}			
			}

		};
	}
}
