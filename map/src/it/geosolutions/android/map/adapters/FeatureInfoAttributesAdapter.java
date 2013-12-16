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
package it.geosolutions.android.map.adapters;

import java.util.ArrayList;

import it.geosolutions.android.map.R;
import it.geosolutions.android.map.model.Attribute;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * @author Lorenzo Natali (www.geo-solutions.it)
 *
 */
public class FeatureInfoAttributesAdapter  extends ArrayAdapter<Attribute> {


	/*
	 * here we must override the constructor for ArrayAdapter the only variable
	 * we care about now is ArrayList<Item> objects, because it is the list of
	 * objects we want to display.
	 */
	
	/*
	 * we are overriding the getView method here - this is what defines how each
	 * list item will look.
	 */
	int resourceId=R.layout.feature_info_attribute_row;
	

	public FeatureInfoAttributesAdapter(Context context, int resource) {
		super(context, resource);
		this.resourceId=resource;
	}

        public FeatureInfoAttributesAdapter(Context context, int resource,ArrayList<Attribute> list) {
                super(context, resource,list);
                this.resourceId=resource;
        }

	public View getView(int position, View convertView, ViewGroup parent){

		// assign the view we are converting to a local variable
		View v = convertView;

		// first check to see if the view is null. if so, we have to inflate it.
		// to inflate it basically means to render, or show, the view.
		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(resourceId, null);
		}

		/*
		 * Recall that the variable position is sent in as an argument to this method.
		 * The variable simply refers to the position of the current object in the list. (The ArrayAdapter
		 * iterates through the list we sent it)
		 * 
		 * Therefore, i refers to the current Item object.
		 */
		Attribute attribute = getItem(position);

		if (attribute != null) {
			
			// This is how you obtain a reference to the TextViews.
			// These TextViews are created in the XML files we defined.			
			
				TextView name = (TextView) v.findViewById(R.id.attribute_name);
				TextView value = (TextView)v.findViewById(R.id.attribute_value);
				name.setText(attribute.getName());
				value.setText(attribute.getValue());
				
			
				
			
		}

			
		// the view must be returned to our activity
		return v;

	

	}
	
	
}
