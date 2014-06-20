/*
 * GeoSolutions MapStore Mobile - Digital field mapping on Android based devices
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
package it.geosolutions.geocollect.android.core.mission;


import it.geosolutions.android.map.wfs.geojson.feature.Feature;
import it.geosolutions.geocollect.android.core.R;
import it.geosolutions.geocollect.model.config.MissionTemplate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;

/**
 * Adapter for a row of layers from a <ArrayList> of <FeatureInfoQueryResult>
 * The implementation show legend and name.
 * 
 * @author Lorenzo Natali (www.geo-solutions.it)
 */
public class FeatureAdapter extends
        ArrayAdapter<Feature> {

int resourceId = R.layout.mission_resource_row;

private MissionTemplate template;

/**
 * The constructor gets the resource (id of the layout for the element)
 * 
 * @param context
 * @param resource
 */
public FeatureAdapter(Context context, int resource) {
    super(context, resource);
    this.resourceId = resource;
}

/**
 * Create the adapter and populate it with a list of <FeatureInfoQueryResult>
 * 
 * @param context
 * @param feature_info_layer_list_row
 * @param feature_layer_name
 */
public FeatureAdapter(SherlockFragmentActivity context, int resource,MissionTemplate template) {
    super(context, resource);
    this.resourceId = resource;
    this.template = template;
}

public View getView(int position, View convertView, ViewGroup parent) {

    // assign the view we are converting to a local variable
    View v = convertView;

    // first check to see if the view is null. if so, we have to inflate it.
    // to inflate it basically means to render, or show, the view.
    if (v == null) {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(resourceId, null);
    }

    /*
     * Recall that the variable position is sent in as an argument to this
     * method. The variable simply refers to the position of the current object
     * in the list. (The ArrayAdapter iterates through the list we sent it)
     * Therefore, i refers to the current Item object.
     */
    Feature result = getItem(position);

    if (result != null) {

        // This is how you obtain a reference to the TextViews.
        // These TextViews are created in the XML files we defined.
        // TODO use ViewHolder
        // display name
    	if(this.template !=null && this.template.nameField != null){
	        TextView name = (TextView) v.findViewById(R.id.mission_resource_name);
	        if (name != null && result.properties != null && result.properties.containsKey(this.template.nameField)) {
	        	Object prop =result.properties.get(this.template.nameField);
	        	if(prop!=null){
	        		name.setText(prop.toString());//TODO: format options
	        	}else{
	        		name.setText("");
	        	}
	            
	        }
    	}
    	if(this.template !=null && this.template.descriptionField != null){
	        TextView desc = (TextView) v.findViewById(R.id.mission_resource_description);
	        if (desc != null && result.properties != null && result.properties.containsKey(this.template.descriptionField)) {
	        	Object prop =result.properties.get(this.template.descriptionField);
	        	if(prop!=null){
	        		desc.setText(prop.toString());//TODO: format options
	        	}else{
	        		desc.setText("");
	        	}
	            
	        }
    	}
    	
		ImageView desc = (ImageView) v.findViewById(R.id.mission_resource_edit_icon);
		if(desc != null){
			if(result.editing){
				desc.setVisibility(View.VISIBLE);
			}else{
				desc.setVisibility(View.GONE);
			}
    	}
    }

    // the view must be returned to our activity
    return v;

}
}

