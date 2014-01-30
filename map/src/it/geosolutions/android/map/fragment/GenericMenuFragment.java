/*
 * GeoSolutions map - Digital field mapping on Android based devices
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
package it.geosolutions.android.map.fragment;

import it.geosolutions.android.map.DataListActivity;
import it.geosolutions.android.map.MapsActivity;
import it.geosolutions.android.map.R;
import it.geosolutions.android.map.geostore.activities.GeoStoreResourcesActivity;
import it.geosolutions.android.map.listeners.OverlayChangeListener;
import it.geosolutions.android.map.mapstore.activities.MapStoreLayerListActivity;
import it.geosolutions.android.map.overlay.managers.SimpleOverlayManager;
import it.geosolutions.android.map.preferences.EditPreferences;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * This fragment shows a some menu options
 * For now it contains only the Settings view
 * feature passed as Extra
 * 
 * @author Lorenzo Natali (www.geo-solutions.it)
 */
public class GenericMenuFragment extends SherlockFragment{



/**
 * Called only once. The instance is automatically retained
 */
@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);

}

@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    return inflater.inflate(R.layout.other_left_drawer_options_fragment, container,
            false);
}

/*
 * (non-Javadoc)
 * @see android.support.v4.app.ListFragment#onViewCreated(android.view.View,
 * android.os.Bundle)
 */
@Override
public void onViewCreated(View view, Bundle savedInstanceState) {
    //add the setting view click listener to open the settings activity
	View t = (View)view.findViewById(R.id.settings);
	t.setOnClickListener(new OnClickListener() {
	
		@Override
		public void onClick(View v) {
			 Intent pref = new Intent(getSherlockActivity(),EditPreferences.class);
			 getSherlockActivity().startActivity(pref);
			
		}
   });

}





}
