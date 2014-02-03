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
package it.geosolutions.android.map.spatialite.activities;

import it.geosolutions.android.map.MapsActivity;
import it.geosolutions.android.map.R;
import it.geosolutions.android.map.activities.style.LinesDataPropertiesActivity;
import it.geosolutions.android.map.activities.style.PointsDataPropertiesActivity;
import it.geosolutions.android.map.activities.style.PolygonsDataPropertiesActivity;
import it.geosolutions.android.map.database.SpatialDataSourceHandler;
import it.geosolutions.android.map.database.SpatialDataSourceManager;
import it.geosolutions.android.map.mapstore.model.MapStoreConfiguration;
import it.geosolutions.android.map.model.Layer;
import it.geosolutions.android.map.model.MSMMap;
import it.geosolutions.android.map.renderer.LegendRenderer;
import it.geosolutions.android.map.spatialite.SpatialiteLayer;
import it.geosolutions.android.map.spatialite.SpatialiteSource;
import it.geosolutions.android.map.style.AdvancedStyle;
import it.geosolutions.android.map.style.StyleManager;
import it.geosolutions.android.map.utils.SpatialDbUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;

import eu.geopaparazzi.spatialite.database.spatial.core.SpatialVectorTable;
import eu.geopaparazzi.spatialite.util.SpatialiteLibraryConstants;


/**
 * Class that shows a list of layers from a spatialite source
 * @author Lorenzo Natali
 *
 */
class ViewHolder {
TextView nameView ;
CheckBox visibleView;
ImageButton propertiesButton;
ImageView legend ;
}


public class SpatialiteLayerListActivity extends SherlockListActivity {
    private List<SpatialVectorTable> spatialTables = new ArrayList<SpatialVectorTable>();;

    public void onCreate( Bundle icicle ) {
        super.onCreate(icicle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        setContentView(R.layout.spatialite_layer_list);
        //set the handler for the select layers button 
        Button selectLayers = (Button) findViewById(R.id.select_layers);
        selectLayers.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				returnSelected();
				
			}
		});
      //set the handler for the select layers button 
        Button loadMap = (Button) findViewById(R.id.load_map);
        loadMap.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				returnAll();
				
			}
		});
    }

	@Override
    protected void onResume() {
        super.onResume();
        refreshList(true);
    }

	/**
	 * refresh the list of layers 
	 * @param doReread if true, reload the spatial vector tables
	 */
    private void refreshList( boolean doReread ) {
        try {
            if (doReread)
                spatialTables = SpatialDataSourceManager.getInstance().getSpatialVectorTables(doReread);
        } catch (Exception e) {
            // Logger.e(this, e.getLocalizedMessage(), e);
            e.printStackTrace();
        }

        ArrayAdapter<SpatialVectorTable> arrayAdapter = new ArrayAdapter<SpatialVectorTable>(this, R.layout.data_row, spatialTables){
            @Override
            public View getView( final int position, View cView, ViewGroup parent ) {
                View rowView = cView;
                ViewHolder holder; // to reference the child views for later actions
                if (rowView == null){
                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    rowView = inflater.inflate(R.layout.data_row, null);
                    holder = new ViewHolder();
                    holder.legend = (ImageView)rowView.findViewById(R.id.legend);
                    holder.nameView = (TextView) rowView.findViewById(R.id.name);
                    holder.visibleView = (CheckBox) rowView.findViewById(R.id.visible);
                    holder.propertiesButton = (ImageButton) rowView.findViewById(R.id.propertiesButton);
                    rowView.setTag(holder);
                }else{
                    holder  = (ViewHolder) rowView.getTag();
                }
                final SpatialVectorTable item = spatialTables.get(position);
                
                if(holder.propertiesButton!=null){
                    holder.propertiesButton.setOnClickListener(new View.OnClickListener(){
                        public void onClick( View v ) {
                            Intent intent = null;
                            if (item.isLine()) {
                                intent = new Intent(SpatialiteLayerListActivity.this, LinesDataPropertiesActivity.class);
                            } else if (item.isPolygon()) {
                                intent = new Intent(SpatialiteLayerListActivity.this, PolygonsDataPropertiesActivity.class);
                            } else if (item.isPoint()) {
                                intent = new Intent(SpatialiteLayerListActivity.this, PointsDataPropertiesActivity.class);
                            }
                            intent.putExtra(SpatialiteLibraryConstants.PREFS_KEY_TEXT, item.getName());
                            startActivity(intent);
    
                        }
                    });
                }
                
                if(holder.nameView!=null){
                    holder.nameView.setText(item.getName());
                }
                AdvancedStyle style =StyleManager.getInstance().getStyle(item.getName());
                if(holder.visibleView!=null){
                    holder.visibleView.setOnCheckedChangeListener(new OnCheckedChangeListener(){
                        public void onCheckedChanged( CompoundButton buttonView, boolean isChecked ) {
                        	StyleManager sm = StyleManager.getInstance();
                        	AdvancedStyle s =sm.getStyle(item.getName());
                        	s.enabled = isChecked ? 1 : 0;
                        	try {
    							sm.updateStyle(s);
    						} catch (IOException e) {
    							Toast.makeText(getBaseContext(),R.string.error_saving_style,Toast.LENGTH_LONG).show();
    						}
                        }
                    });
                    holder.visibleView.setChecked(style.enabled != 0);
                }
            if (holder.legend != null) {
                holder.legend.setImageDrawable(new BitmapDrawable(getContext()
                        .getResources(), LegendRenderer.getLegend(item
                        .getName())));
            }
                
                return rowView;
            }

        };
        setListAdapter(arrayAdapter);

    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	 switch (item.getItemId()) {
    	    case android.R.id.home:
    	      finish();
    	      break;
    	 }
		return false;
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    /**
     * Returns to the caller activity a list of layers to add from the selected ones.
     */
    private void returnSelected() {
        Intent mIntent = new Intent();
        
        ArrayList<Layer> layers = new ArrayList<Layer>();
        //look for the source
        SpatialiteSource s=null;
        for(SpatialVectorTable t : spatialTables){
        	AdvancedStyle style = StyleManager.getInstance().getStyle(t.getName());
        	if(style.enabled>0){
				SpatialDataSourceHandler dsm = SpatialDataSourceManager.getInstance().getSpatialDataSourceHandler(t);
				if(dsm != null){
					s = new SpatialiteSource(dsm);
				}
	    		
	        	SpatialiteLayer l = new SpatialiteLayer(t);
	        	
				l.setSource(s);
				layers.add(l);
        	}
        }
        //reverse the list because the order is the opposite
        Collections.reverse(layers);
        mIntent.putExtra(MapsActivity.LAYERS_TO_ADD, layers);
        setResult(RESULT_OK, mIntent);
        finish();
        
        
    }
    
    /**
     * returns all the a <MSMMap> from the source
     */
    private void returnAll() {
        Intent mIntent = new Intent();
        MSMMap m = SpatialDbUtils.mapFromDb();
        mIntent.putExtra(MapsActivity.MSM_MAP, m);
        setResult(RESULT_OK, mIntent);
        finish();
        
    }
}





