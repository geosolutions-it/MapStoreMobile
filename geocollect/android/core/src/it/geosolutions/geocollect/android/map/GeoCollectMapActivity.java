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

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import android.view.ContextMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import it.geosolutions.android.map.MapsActivity;
import it.geosolutions.geocollect.android.core.R;
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
        
        return super.onOptionsItemSelected(item);
    }
    
}
