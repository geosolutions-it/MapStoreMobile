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

import it.geosolutions.android.map.fragment.FeatureCircleAttributeListFragment;
import it.geosolutions.android.map.fragment.FeatureInfoAttributeListFragment;
import it.geosolutions.android.map.fragment.FeatureInfoLayerListFragment;
import it.geosolutions.android.map.fragment.FeaturePolygonAttributeListFragment;

import it.geosolutions.android.map.R;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

/**
 * This activity manages the FeatureInfoAttributeListFragment 
 * managing tasks. can return a feature from it fragment
 * @author Lorenzo Natali (www.geo-solutions.it)
 *
 */
public class GetFeatureInfoAttributeActivity  extends SherlockFragmentActivity {

	public static final int GET_ITEM = 0;
	
	//Fragment for rectangular selection
	FeatureInfoAttributeListFragment mTaskFragment;
	//Fragment for circular selection
	FeatureCircleAttributeListFragment cTaskFragment;
	//Fragment for polygonal selection
	FeaturePolygonAttributeListFragment pTaskFragment;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
		overridePendingTransition(R.anim.in_from_right,
                R.anim.out_to_left);
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    	FragmentManager fm = getSupportFragmentManager();
 	
    	String sel = "";
        if(getIntent()!=null)
        	sel = getIntent().getStringExtra("selection"); //Discover which selection user has selected
                
        if(sel!=null && sel.equals("Circular")){
        	// During initial setup, plug in the details fragment.
        	FeatureInfoLayerListFragment details = new FeatureInfoLayerListFragment();
        	//details.setArguments(getIntent().getExtras());
        	//fm.beginTransaction().add(android.R.id.content, details).commit(); 
        	cTaskFragment = (FeatureCircleAttributeListFragment) fm.findFragmentByTag("featureInfoLayerList");
        	if(cTaskFragment == null){
        		cTaskFragment = new FeatureCircleAttributeListFragment();
            	details.setArguments(getIntent().getExtras());
        		fm.beginTransaction().add(android.R.id.content,cTaskFragment, "featureInfoLayerList").commit();
        	}
        }
        else if(sel!=null && sel.equals("Rectangular")){
        	// During initial setup, plug in the details fragment.
        	FeatureInfoLayerListFragment details = new FeatureInfoLayerListFragment();
        	//fm.beginTransaction().add(android.R.id.content, details).commit();
        	mTaskFragment = (FeatureInfoAttributeListFragment) fm.findFragmentByTag("featureInfoLayerList");
        	if(mTaskFragment == null){
	        	mTaskFragment = new FeatureInfoAttributeListFragment();
	        	details.setArguments(getIntent().getExtras());
	        	//TODO add empty layers to the view
	            //TODO add missing layers
	    		fm.beginTransaction().add(android.R.id.content,mTaskFragment, "featureInfoLayerList").commit();
        	}
        }
        
       else if(sel!=null && sel.equals("Polygonal")){
	    	FeatureInfoLayerListFragment details = new FeatureInfoLayerListFragment();
	    	
	    	pTaskFragment = (FeaturePolygonAttributeListFragment) fm.findFragmentByTag("featureInfoLayerList");
	    	if(pTaskFragment == null){
	        	pTaskFragment = new FeaturePolygonAttributeListFragment();
	            details.setArguments(getIntent().getExtras());
	    		fm.beginTransaction().add(android.R.id.content,pTaskFragment, "featureInfoLayerList").commit();
	    	}
        }
    }
	
	 @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	    	 switch (item.getItemId()) {
	    	    case android.R.id.home:  	    
	    	      finish();
	    	      overridePendingTransition(R.anim.in_from_left,R.anim.out_to_right);
	    	      break;
	    	 }
			return false;
	    }
    
	 /* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragmentActivity#onStop()
	 */
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
}