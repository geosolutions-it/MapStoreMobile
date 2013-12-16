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
package it.geosolutions.android.map.activities;

import it.geosolutions.android.map.fragment.GetFeatureInfoFragment;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

/**
 * @author Lorenzo Natali (www.geo-solutions.it)
 *
 */
public class GetFeatureInfoActivity extends SherlockFragmentActivity{
	

	    @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	        if (savedInstanceState == null) {
	            // During initial setup, plug in the details fragment.
	        	GetFeatureInfoFragment details = new GetFeatureInfoFragment();
	            details.setArguments(getIntent().getExtras());
	            getSupportFragmentManager().beginTransaction().add(android.R.id.content, details).commit();
	            
	            
	        }
	    
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
}
