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

import it.geosolutions.android.map.fragment.FeatureInfoLayerListFragment;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

/**
 * @author Lorenzo Natali (www.geo-solutions.it)
 *
 */
public class GetFeatureInfoLayerListActivity  extends SherlockFragmentActivity {
	public static final int BBOX_REQUEST = 10;
	public static final String RESULT_FEATURE_EXTRA = "FEATURE";
	public static final String LAYER_FEATURE_EXTRA = "LAYER";
	FeatureInfoLayerListFragment mTaskFragment;
	ArrayList<String> emptyLayers=new ArrayList<String>();
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        requestWindowFeature(Window.FEATURE_PROGRESS);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        
            // During initial setup, plug in the details fragment.
        	FragmentManager fm = getSupportFragmentManager();
        	mTaskFragment = (FeatureInfoLayerListFragment) fm.findFragmentByTag("featureInfoLayerList");
        	if(mTaskFragment == null){
        		mTaskFragment = new FeatureInfoLayerListFragment();
        		//TODO add empty layers to the view
        		mTaskFragment.setArguments(getIntent().getExtras());
        		//TODO add missing layers
        		fm.beginTransaction().add(android.R.id.content,mTaskFragment, "featureInfoLayerList").commit();
        	}
    }
	
	 @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	    	 switch (item.getItemId()) {
	    	    case android.R.id.home:
	    	    	setResult(RESULT_CANCELED);
	    	      finish();
	    	      break;
	    	 }
			return false;
	    }
    
	 /* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int request_code, int result_code, Intent result) {
		// TODO Auto-generated method stub
		super.onActivityResult(request_code, result_code, result);
		if(request_code==GetFeatureInfoAttributeActivity.GET_ITEM){
			if(result_code==RESULT_OK){
				setResult(Activity.RESULT_OK,result); 
				finish();
			}
		}
		//from this we can get the emptyLayers and provide them to the fragment view
		if(result_code==RESULT_CANCELED){
			if(result!=null && result.getExtras()!=null){
			String emptyLayer = result.getExtras().getString("emptyLayer");
				if(emptyLayer!=null){
					if(!this.emptyLayers.contains(emptyLayer)){
						this.emptyLayers.add(emptyLayer);
					}
				}
			}
		}
		
	}
}