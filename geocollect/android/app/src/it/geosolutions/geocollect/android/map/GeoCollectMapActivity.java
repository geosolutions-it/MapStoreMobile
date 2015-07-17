/*******************************************************************************
 * Copyright 2014-2015 GeoSolutions
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
 * 
 *******************************************************************************/
package it.geosolutions.geocollect.android.map;

import java.util.List;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import it.geosolutions.android.map.MapsActivity;
import it.geosolutions.android.map.control.MapControl;
import it.geosolutions.android.map.control.MapInfoControl;
import it.geosolutions.geocollect.android.app.R;
/**
 * Custom Map to use with GeoCollect application
 * This version adds a button to zoom to the initial BBOX
 * There are also a custom layout on the resources 
 * layout-land/activity_map.xml
 * to place the button bar on the bottom, as in the other maps
 * 
 * @author Lorenzo Pini (lorenzo.pini@geo-solutions.it)
 */
public class GeoCollectMapActivity extends MapsActivity {

    protected MapInfoControl mic;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inf = getSupportMenuInflater();
        inf.inflate(R.menu.simple_map_menu, (Menu) menu);
        
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        
        MenuInflater inf = getSupportMenuInflater();
        inf.inflate(R.menu.simple_map_menu, (Menu) menu);
        
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        
        //center on the marker, preferably the updated
        if(item.getItemId() == R.id.center){

            centerMapFile();
            
        }
        
        // Activate the filter control
        if(item.getItemId() == R.id.filter){

            if(mic != null && mic.getActivationListener() != null){
                mic.getActivationListener().onClick(item.getActionView());
            }
            
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mic = new ReturningMapInfoControl();
        mic.activity = this;
        mic.mapView = mapView;
        
        // if the mapView had some controls, get the group of them
        // this check is needed until the map library will have named groups
        List<MapControl> mcList = mapView.getControls();
        if(mcList != null && !mcList.isEmpty()){
            MapControl mc;
            boolean found = false;
            for(int i = 0; i < mcList.size() && !found; i++){
                mc = mcList.get(i);
                if(mc.getGroup() != null){
                    mic.setGroup(mc.getGroup());
                    mic.getGroup().add(mic);
                    found = true;
                }
            }
            
        }
        
        mapView.addControl(mic);
        mic.instantiateListener();
        
    }
}
