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
package it.geosolutions.android.map;

import it.geosolutions.android.map.activities.style.LinesDataPropertiesActivity;
import it.geosolutions.android.map.activities.style.PointsDataPropertiesActivity;
import it.geosolutions.android.map.activities.style.PolygonsDataPropertiesActivity;
import it.geosolutions.android.map.database.SpatialDataSourceManager;
import it.geosolutions.android.map.mapstore.activities.MapStoreLayerListActivity;
import it.geosolutions.android.map.mapstore.model.MapStoreConfiguration;
import it.geosolutions.android.map.renderer.LegendRenderer;
import it.geosolutions.android.map.style.AdvancedStyle;
import it.geosolutions.android.map.style.StyleManager;

import java.io.IOException;
import java.util.ArrayList;
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
import android.widget.ImageView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.MenuItem;

import eu.geopaparazzi.spatialite.database.spatial.core.SpatialVectorTable;
import eu.geopaparazzi.spatialite.util.SpatialiteLibraryConstants;


/**
 * Class that keep fields of the view to improve performances
 * @author Lorenzo Natali
 *
 */
class ViewHolder {
TextView nameView ;
CheckBox visibleView;
ImageButton propertiesButton;
ImageView legend ;
}


public class DataListActivity extends SherlockListActivity {
	private MapStoreConfiguration mapStoreConfig;
    private List<SpatialVectorTable> spatialTables = new ArrayList<SpatialVectorTable>();;

    public void onCreate( Bundle icicle ) {
        super.onCreate(icicle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
        setContentView(R.layout.data_list);
        
       
        
    }

    protected MapStoreConfiguration getMapStoreConfig() {
		// TODO Auto-generated method stub
		return mapStoreConfig;
	}

	@Override
    protected void onResume() {
        super.onResume();
        refreshList(true);
    }

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
                /*ImageButton listUpButton = (ImageButton) rowView.findViewById(R.id.upButton);
                listUpButton.setOnClickListener(new View.OnClickListener(){
                    public void onClick( View v ) {
                        if (position > 0) {
                            SpatialVectorTable before = spatialTables.get(position - 1);
                            int tmp1 = before.style.order;
                            int tmp2 = item.style.order;
                            item.getStyle().order = tmp1;
                            before.getStyle().order = tmp2;
                            Collections.sort(spatialTables, new OrderComparator());
                            refreshList(false);
                        }
                    }
                });

                ImageButton listDownButton = (ImageButton) rowView.findViewById(R.id.downButton);
                listDownButton.setOnClickListener(new View.OnClickListener(){
                    public void onClick( View v ) {
                        if (position < spatialTables.size() - 1) {
                            SpatialVectorTable after = spatialTables.get(position + 1);
                            int tmp1 = after.style).order;
                            int tmp2 = item.style.order;
                            item.getStyle().order = tmp1;
                            after.getStyle().order = tmp2;
                            Collections.sort(spatialTables, new OrderComparator());
                            refreshList(false);
                        }
                    }
                });
				*/
                if(holder.propertiesButton!=null){
                    holder.propertiesButton.setOnClickListener(new View.OnClickListener(){
                        public void onClick( View v ) {
                            Intent intent = null;
                            if (item.isLine()) {
                                intent = new Intent(DataListActivity.this, LinesDataPropertiesActivity.class);
                            } else if (item.isPolygon()) {
                                intent = new Intent(DataListActivity.this, PolygonsDataPropertiesActivity.class);
                            } else if (item.isPoint()) {
                                intent = new Intent(DataListActivity.this, PointsDataPropertiesActivity.class);
                            }
                            intent.putExtra(SpatialiteLibraryConstants.PREFS_KEY_TEXT, item.getName());
                            startActivity(intent);
    
                        }
                    });
                }
                /*
                ImageButton zoomtoButton = (ImageButton) rowView.findViewById(R.id.zoomtoButton);
                zoomtoButton.setOnClickListener(new View.OnClickListener(){
                    public void onClick( View v ) {
                        try {
                            float[] tableBounds = SpatialDatabaseManager.getInstance().getVectorHandler(item)
                                    .getTableBounds(item, "4326");
                            double lat = tableBounds[1] + (tableBounds[0] - tableBounds[1]) / 2.0;
                            double lon = tableBounds[3] + (tableBounds[2] - tableBounds[3]) / 2.0;

                            Intent intent = getIntent();
                            intent.putExtra(SpatialiteLibraryConstants.LATITUDE, lat);
                            intent.putExtra(SpatialiteLibraryConstants.LONGITUDE, lon);
                            setResult(Activity.RESULT_OK, intent);
                            finish();
                        } catch (jsqlite.Exception e) {
                            e.printStackTrace();
                        }
                    }
                });*/

                // rowView.setBackgroundColor(Color.parseColor(item.getColor()));
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
    	      returnData();
    	      finish();
    	      break;
    	 }
		return false;
    }
    
    @Override
    public void onBackPressed() {
        Bundle bundle = new Bundle();
        //bundle.putString(FIELD_A, mA.getText().toString());
        
        
        returnData();

        super.onBackPressed();
    }

    /**
     * Set the proper return data
     */
    private void returnData() {
        Intent mIntent = new Intent();
        setResult(RESULT_OK, mIntent);
        
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	// TODO Auto-generated method stub
    	super.onActivityResult(requestCode, resultCode, data);
    	mapStoreConfig = (MapStoreConfiguration) data.getSerializableExtra(MapsActivity.MAPSTORE_CONFIG);
    	
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	// TODO Auto-generated method stub
    	super.onSaveInstanceState(outState);
    	outState.putSerializable(MapsActivity.MAPSTORE_CONFIG	, mapStoreConfig) ;
    	
    }
}





