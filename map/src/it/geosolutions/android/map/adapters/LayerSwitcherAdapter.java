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

import it.geosolutions.android.map.R;
import it.geosolutions.android.map.listeners.LayerChangeListener;
import it.geosolutions.android.map.model.Layer;
import it.geosolutions.android.map.renderer.LegendRenderer;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;

/**
 * Adapter to show Layers in the <LayerSwitcherFragment>
 * @author Lorenzo Natali (www.geo-solutions.it)
 */
public class LayerSwitcherAdapter extends ArrayAdapter<Layer> {


int resourceId = R.layout.feature_info_layer_list_row;
private LayerChangeListener listener;

/**
 * Create the Adapter
 * @param context
 * @param resource
 */
public LayerSwitcherAdapter(Context context, int resource,LayerChangeListener listener) {
    super(context, resource);
    this.resourceId = resource;
    this.listener = listener;
}


/**
 * Create the Adapter
 * @param context the context
 * @param resource the resource number
 * @param layers the list of layers
 * @param listener the LayerChangeListener to use for event on layers
 */
public LayerSwitcherAdapter(SherlockFragmentActivity context, int resource,
        ArrayList<Layer> layers,LayerChangeListener listener) {
    super(context, resource, layers);
    this.listener = listener;
    this.resourceId = resource;
}

public View getView(int position, View convertView, ViewGroup parent) {

    // assign the view we are converting to a local variable
    View v = convertView;

    // first check to see if the view is null. if so, we have to inflate it.
    // to inflate it basically means to render, or show, the view.
    final Layer layer = getItem(position);
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
    if (layer != null) {

        // This is how you obtain a reference to the TextViews.
        // These TextViews are created in the XML files we defined.
        TextView name = (TextView) v.findViewById(R.id.name);
        if (name != null) {
            name.setText(layer.getTitle());
        }
        CheckBox visib = (CheckBox) v.findViewById(R.id.visible);
        visib.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton c,
					boolean isChecked) {
				//XOR means that they are different
				if(isChecked ^ layer.isVisibility()){
					layer.setVisibility(isChecked);
					listener.onLayerVisibilityChange(layer);
				}
				
			}
        	
        });
        visib.setChecked(layer.isVisibility());
        ImageView legend = (ImageView) v.findViewById(R.id.legend);
    }

    // the view must be returned to our activity
    return v;

}

}
