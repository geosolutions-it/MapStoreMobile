/*
 * GeoSolutions Android Map Library - Digital field mapping on Android based devices
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
package it.geosolutions.android.map.mapstore.activities;

import it.geosolutions.android.map.MapsActivity;
import it.geosolutions.android.map.R;
import it.geosolutions.android.map.mapstore.model.MapStoreConfiguration;
import it.geosolutions.android.map.mapstore.model.MapStoreLayer;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;


/**
 * Class that keep fields of the view to improve performances
 * @author Lorenzo Natali
 *
 */
class ViewHolder {
	TextView nameView ;
	CheckBox visibleView;
}

/**
 * This activity shows a list of layers from a mapstore configuration json file
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public class MapStoreLayerListActivity extends SherlockListActivity {
	
	private MapStoreConfiguration mapStoreConfig;

    public void onCreate( Bundle icicle ) {
        super.onCreate(icicle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        setContentView(R.layout.mapstore_layer_list);
        
        //set checkboxes
        Bundle bundle = getIntent().getExtras();
        if(icicle == null){
        	mapStoreConfig = (MapStoreConfiguration) bundle.getSerializable(MapsActivity.MAPSTORE_CONFIG);
        	Collections.reverse(mapStoreConfig.map.layers);
        }else{
        	mapStoreConfig =(MapStoreConfiguration) icicle.getSerializable(MapsActivity.MAPSTORE_CONFIG);
        	
        }
        //start intent on click on 
        
        
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList(true);
    }

    private void refreshList( boolean doReread ) {
        
        final MapStoreLayerListActivity act =this;

    	ArrayAdapter<MapStoreLayer> arrayAdapter = new ArrayAdapter<MapStoreLayer>(this, R.layout.data_row,mapStoreConfig.map.layers){
            @Override
            public View getView( final int position, View cView, ViewGroup parent ) {
                View rowView = cView;
                final ViewHolder holder; // to reference the child views for later actions
                if (rowView == null){
                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    rowView = inflater.inflate(R.layout.mapstore_layer_row, null);
                    holder = new ViewHolder();
                    holder.nameView = (TextView) rowView.findViewById(R.id.name);
                    holder.visibleView = (CheckBox) rowView.findViewById(R.id.visible);
                    rowView.setTag(holder);
                }else{
                    holder  = (ViewHolder) rowView.getTag();
                   
                }
                final MapStoreLayer item = act.getLayers().get(position);
                holder.nameView.setText(item.title);
                holder.visibleView.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(
							CompoundButton buttonView, boolean isChecked) {
						item.visibility = isChecked;
					}
            	});
                holder.visibleView.setChecked(item.visibility);
				
				return rowView;
			}
		};
        setListAdapter(arrayAdapter);

    }
    
    /**
     * returns the layers  from the mapstore configurations
     * @return the list of <MapStoreLayers> loaded
     */
    protected ArrayList<MapStoreLayer> getLayers() {
		return mapStoreConfig.map.layers;
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	 if (item.getItemId() == android.R.id.home) {
 			finish();
 		} else if (item.getItemId() == R.id.action_use) {
 			 returnData();
   	      finish();
 		}
		return false;
    }
    
    @Override
    public void onBackPressed() {
    	//return data also if back button is pressed
        super.onBackPressed();
    }

    /**
     * Set the proper return data
     */
    private void returnData() {
        Intent mIntent = new Intent();
        Bundle bundle = new Bundle();
        //reverse the list to return the layers in the correct order
        Collections.reverse(mapStoreConfig.map.layers);
        bundle.putSerializable(MapsActivity.MAPSTORE_CONFIG	, mapStoreConfig) ;
        mIntent.putExtras(bundle);
        setResult(RESULT_OK, mIntent);        
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	// TODO Auto-generated method stub
    	super.onSaveInstanceState(outState);
    	outState.putSerializable(MapsActivity.MAPSTORE_CONFIG	, mapStoreConfig) ;
    	
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.mapstore_resource_select, menu);
		return true;
	}
}





