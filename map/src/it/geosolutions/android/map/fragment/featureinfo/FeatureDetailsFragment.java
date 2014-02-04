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
package it.geosolutions.android.map.fragment.featureinfo;

import it.geosolutions.android.map.R;
import it.geosolutions.android.map.adapters.FeatureInfoAttributesAdapter;
import it.geosolutions.android.map.model.Attribute;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockListFragment;

/**
 * This fragment shows a view o the attributes of a single feature from a
 * feature passed as Extra
 * 
 * @author Lorenzo Natali (www.geo-solutions.it)
 */
public class FeatureDetailsFragment extends SherlockListFragment {
private FeatureInfoAttributesAdapter adapter;

/**
 * Called only once
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
    return inflater.inflate(R.layout.feature_info_attribute_list, container,
            false);
}

/*
 * (non-Javadoc)
 * @see android.support.v4.app.ListFragment#onViewCreated(android.view.View,
 * android.os.Bundle)
 */
@Override
public void onViewCreated(View view, Bundle savedInstanceState) {

    super.onViewCreated(view, savedInstanceState);
 // get data from the intent
    // TODO get them from arguments
    Bundle extras = getActivity().getIntent().getExtras();
    // TODO get already loaded data;
    ArrayList<Attribute> arr = extras.getParcelableArrayList("feature");
    
    adapter = new FeatureInfoAttributesAdapter(getSherlockActivity(),
            R.layout.feature_info_attribute_row,arr);
    setListAdapter(adapter);

}



@Override
public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

}

}
