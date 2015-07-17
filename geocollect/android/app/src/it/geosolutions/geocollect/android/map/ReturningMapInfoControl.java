/*******************************************************************************
 * Copyright 2014-2015 GeoSolutions
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
 * 
 *******************************************************************************/
package it.geosolutions.geocollect.android.map;

import android.app.Activity;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import it.geosolutions.android.map.control.MapControl;
import it.geosolutions.android.map.control.MapInfoControl;
import it.geosolutions.android.map.view.AdvancedMapView;
import it.geosolutions.geocollect.android.app.BuildConfig;
import it.geosolutions.geocollect.android.app.R;

public class ReturningMapInfoControl extends MapInfoControl implements Parcelable {

    /**
     * @param mapView
     * @param activity
     */
    public ReturningMapInfoControl(AdvancedMapView mapView, Activity activity) {
        super(mapView, activity);
    }

    /**
     * Empty Constructor to create a MapInfoControl without an attached
     * {@link AdvancedMapView} and {@link Activity}
     * The receiving Activity MUST attach an {@link AdvancedMapView} 
     * and an {@link Activity} to this control before use.
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
        if (activity != null) {
            this.mapListener = new ReturningMapInfoListener(mapView, activity);
            pref = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
            array = activity.getResources().getStringArray(R.array.preferences_selection_shape);
            defaultShapeSelection = activity.getResources().getString(R.string.preferences_selection_shape_default); // default selection rectangular
        }
    };

    @Override
    public OnClickListener getActivationListener() {

        return new OnClickListener() {

            @Override
            public void onClick(View button) {

                if (isEnabled()) {
                    disable();
                    if (button != null) {
                        button.setSelected(false);
                    }
                } else {
                    if (getGroup() != null) {
                        for (MapControl c : getGroup()) {
                            c.disable();
                            if (c.getActivationButton() != null) {
                                c.getActivationButton().setSelected(false);
                            }
                        }
                    }
                    enable();
                    if (button != null) {
                        button.setSelected(true);
                    }
                    if (activity != null) {
                        Toast.makeText(activity, activity.getString(R.string.create_rectangle), Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            }

        };
    }

    @Override
    public void refreshControl(int requestCode, int resultCode, Intent data) {
        if (BuildConfig.DEBUG) {
            Log.v("ReturningMapInfoControl", "requestCode:" + requestCode);
            Log.v("ReturningMapInfoControl", "resultCode:" + resultCode);
        }

        disable();
        if (getActivationButton() != null) {
            getActivationButton().setSelected(false);
        }
        loadStyleSelectorPreferences();
        instantiateListener();
        setMode(mode);
    }
}
