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
import it.geosolutions.android.map.fragment.sources.LayerStoreProvider;
import it.geosolutions.android.map.fragment.sources.SourcesFragment;
import it.geosolutions.android.map.listeners.LayerChangeListener;
import it.geosolutions.android.map.model.Layer;
import it.geosolutions.android.map.model.stores.LayerStore;
import it.geosolutions.android.map.renderer.LegendRenderer;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;

/**
 * Adapter to show Layers in the <LayerSwitcherFragment>
 * @author Lorenzo Natali (www.geo-solutions.it)
 */
public class LayerStoreAdapter extends ArrayAdapter<LayerStore> {


int resourceId = R.layout.sources_row;
private LayerStoreProvider listener;

/**
 * Create the Adapter
 * @param context
 * @param resource
 */
public LayerStoreAdapter(Context context, int resource,LayerStoreProvider sourcesFragment) {
    super(context, resource);
    this.resourceId = resource;
    this.listener = sourcesFragment;
}


/**
 * Create the Adapter
 * @param context the context
 * @param resource the resource number
 * @param layers the list of layers
 * @param listener the LayerChangeListener to use for event on layers
 */
public LayerStoreAdapter(SherlockFragmentActivity context, int resource,
        ArrayList<LayerStore> layers,LayerStoreProvider listener) {
    super(context, resource, layers);
    this.listener = listener;
    this.resourceId = resource;
}

public View getView(int position, View convertView, ViewGroup parent) {

    // assign the view we are converting to a local variable
    View v = convertView;

    // first check to see if the view is null. if so, we have to inflate it.
    // to inflate it basically means to render, or show, the view.
    final LayerStore store = getItem(position);
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
    if (store != null) {
        TextView name = (TextView) v.findViewById(R.id.name);
        if (name != null) {
            name.setText(store.getName());
        }
        ImageButton details = (ImageButton) v.findViewById(R.id.details);
        if(details != null){
	        details.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					store.openDetails(listener.getActivity());
					
				}
			});
        }
        ImageButton edit = (ImageButton) v.findViewById(R.id.edit);
        if(edit != null){
	        edit.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					store.openEdit(listener.getActivity());
					
				}
			});
        }
    }

    // the view must be returned to our activity
    return v;

}

}
