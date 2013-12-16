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

import it.geosolutions.android.map.R;
import it.geosolutions.android.map.adapters.SectionAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * Shows a grouped list of feature attributes.
 * Expects a bundle with many <ArrayList<Bundle>> and each bundle contains attribute-value couple
 * @author Lorenzo Natali (www.geo-solutions.it)
 * 
 */
public class GetFeatureInfoFragment extends SherlockFragment {
	private SectionAdapter layerSection = new SectionAdapter() {
		protected View getHeaderView(String caption, int index,
				View convertView, ViewGroup parent) {
			TextView result = (TextView) convertView;

			if (convertView == null) {
				result = (TextView) getLayoutInflater(null).inflate(
						R.layout.feature_info_header, null);
			}

			result.setText(caption);

			return (result);
		}
	};
	class FeatureSectionAdapter extends SectionAdapter {
		protected View getHeaderView(String caption, int index,
				View convertView, ViewGroup parent) {
			TextView result = (TextView) convertView;

			if (convertView == null) {
				result = (TextView) getLayoutInflater(null).inflate(
						R.layout.feature_info_header, null);
			}

			result.setText(caption);

			return (result);
		}
	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.feature_info_attribute_list, container, false);

		return v;

	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onViewCreated(view, savedInstanceState);

		ListView list = (ListView) getActivity().findViewById(
				android.R.id.list);
		Bundle b = getArguments().getBundle("data");
		if(b==null){
			return;//TODO notify problem
		}
		String[] from = {"name","value"};
		int[] to = {R.id.attribute_name,R.id.attribute_value};
		Set<String> layerNameSet = b.keySet();
		for (String layerName : layerNameSet) {
			
			// Bundle layerBundle = b.getBundle(layerName);
			ArrayList<Bundle> layerBundleList = b.getParcelableArrayList(layerName);
			if (layerBundleList != null) {
				//if some data from the current section
				int featureListSize = layerBundleList.size();
				if ( featureListSize!= 0) {
					
					
					FeatureSectionAdapter fsa = new FeatureSectionAdapter();
					
					//create a section for every feature
					for(Bundle feature: layerBundleList){
						
						//Crete an array do display a list of strings...
						//TODO improve this with a table
						
						ArrayList<Map<String,String>> attributeList= new ArrayList<Map<String,String>>();
						for(String attributeName : feature.keySet()){
							HashMap<String,String> attribute = new HashMap<String,String>();
							attribute.put("name",attributeName);
							attribute.put("value",feature.getString(attributeName));
							attributeList.add(attribute);
						}
						
						//tableLayout.addView(adapter.getView(i, null, tableLayout))
						Adapter adapter = new SimpleAdapter(view.getContext(), attributeList, R.layout.feature_info_attribute_row,from,to);
						//new ArrayAdapter<String>(view.getContext(), R.layout.feature_info_header,attributes)
						fsa.addSection("",adapter );
						
					}
					;
					
					layerSection.addSection(layerName, fsa);
					
				}
			}
		}
		// TODO init adapter with headers and data
		// 
		list.setAdapter(layerSection);

	}
}
