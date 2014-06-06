/*
 * GeoSolutions - MapstoreMobile - GeoSpatial Framework on Android based devices
 * Copyright (C) 2014  GeoSolutions (www.geo-solutions.it)
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

package it.geosolutions.geocollect.android.core.form;

import it.geosolutions.android.map.view.MapViewManager;
import it.geosolutions.geocollect.android.core.R;
import it.geosolutions.geocollect.android.core.mission.PendingMissionListActivity;
import it.geosolutions.geocollect.android.core.mission.utils.MissionUtils;
import it.geosolutions.geocollect.android.core.mission.utils.SpatialiteUtils;
import it.geosolutions.geocollect.android.core.widgets.EnableSwipeViewPager;
import it.geosolutions.geocollect.model.config.MissionTemplate;
import it.geosolutions.geocollect.model.viewmodel.Field;
import it.geosolutions.geocollect.model.viewmodel.Page;
import it.geosolutions.geocollect.model.viewmodel.type.XType;

import java.io.File;
import java.util.List;

import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.MapView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import eu.geopaparazzi.library.util.ResourcesManager;

public class FormEditActivity extends SherlockFragmentActivity  implements MapActivity  {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments representing
     * each object in a collection. We use a {@link android.support.v4.app.FragmentStatePagerAdapter}
     * derivative, which will destroy and re-create fragments as needed, saving and restoring their
     * state in the process. This is important to conserve memory and is a best practice when
     * allowing navigation between objects in a potentially large collection.
     */
	FormCollectionPagerAdapter formPagerAdapter;

    /**
     * The {@link android.support.v4.view.ViewPager} that will display the object collection.
     */
	EnableSwipeViewPager mViewPager;
	/**
	 * the MapViewManager
	 */
	private MapViewManager mapViewManager =new MapViewManager();

	/**
	 * Spatialite Database for persistence
	 */
	jsqlite.Database spatialiteDatabase;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.form_edit_pager);
        /* create the adapter for the pages. it contains a reference to the 
         * mapViewManager to delete the mapViews when destroy items
         * */
        
        formPagerAdapter = new FormCollectionPagerAdapter(getSupportFragmentManager(),this){
        	MapViewManager mapManager = mapViewManager;
        	@Override 
            public Object instantiateItem(ViewGroup viewGroup, int position) {

                Object obj = super.instantiateItem(viewGroup, position);
                
                return obj;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                super.destroyItem(container, position, object);
                //get the page and check if map is present
                //I assume only one map in the form
                //TODO manage multiple maps
                MissionTemplate t = MissionUtils.getDefaultTemplate(getActivityContext());
                Page p =t.form.pages.get(position);
            	
            	if(p!=null && p.title != null){
            		List<Field> fields =  t.form.pages.get(position).fields;
            		for(Field f: fields){
            			if(XType.mapViewPoint.equals(f.xtype)){
            				mapManager.destroyMapViews();
            			}
            		}
            	}
            	
                
            }

        };

        // Set up action bar.
        final ActionBar actionBar = getSupportActionBar();

        // Specify that the Home button should show an "Up" caret, indicating that touching the
        // button will take the user one step up in the application's hierarchy.
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Set up the ViewPager, attaching the adapter.
        mViewPager = (EnableSwipeViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(formPagerAdapter);
        //set a listener for page change events
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int nPage) {
				MissionTemplate t =MissionUtils.getDefaultTemplate(mViewPager.getContext());
				Page p = t.form.pages.get(nPage);
				if(p.attributes != null && p.attributes.containsKey("message")){
					Toast.makeText(mViewPager.getContext(), (String) p.attributes.get("message"), Toast.LENGTH_LONG).show();
				}
				//TODO disable scroll if needed
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				// TODO save
				
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				//nothing to do
				
			}
		});

        // Initialize database
		if(spatialiteDatabase == null){
	        try {
	            
	            File sdcardDir = ResourcesManager.getInstance(this).getSdcardDir();
	            File spatialDbFile = new File(sdcardDir, "geocollect/genova.sqlite");
	
	            if (!spatialDbFile.getParentFile().exists()) {
	                throw new RuntimeException();
	            }
	            spatialiteDatabase = new jsqlite.Database();
	            spatialiteDatabase.open(spatialDbFile.getAbsolutePath(), jsqlite.Constants.SQLITE_OPEN_READWRITE
	                    | jsqlite.Constants.SQLITE_OPEN_CREATE);
	            
	            Log.v("FORM_EDIT", SpatialiteUtils.queryVersions(spatialiteDatabase));
	            
	            //TODO: remove hardcoded tableName
	            if(SpatialiteUtils.checkOrCreateTable(spatialiteDatabase, "punti_accumulo_data")){
		            Log.v("FORM_EDIT", "Table Found");
	            }else{
		            Log.w("FORM_EDIT", "Table could not be created, edits will not be saved");
	            }
	            
	        } catch (Exception e) {
	            Log.v("FORM_EDIT", Log.getStackTraceString(e));
	        }
		}
    }

   

    /**
     * A {@link android.support.v4.app.FragmentStatePagerAdapter} that returns a fragment
     * representing an object in the collection.
     */
    public static class FormCollectionPagerAdapter extends FragmentStatePagerAdapter {

    	MissionTemplate t;
    	/**
    	 * Constructor for <FormCollectionPageAdapter>
    	 * To avoid memory problems, on page creation, the mapViewManager will be passed to the created
    	 * fragment. The reference to the mapViewManager will allow the fragment to dispose the MapView
    	 * when removed.
    	 * @param fm the <FragmentManager>
    	 * @param c the context
    	 * @param mapViewManager the MapViewManager
    	 */
        public FormCollectionPagerAdapter(FragmentManager fm,Context c) {
            super(fm);
            t = MissionUtils.getDefaultTemplate(c);
        }

        @Override
        public SherlockFragment getItem(int i) {
            SherlockFragment fragment = new FormPageFragment();
            Bundle args = new Bundle();
            
            args.putInt(FormPageFragment.ARG_OBJECT,i);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return t.form.pages.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
        	Page p =t.form.pages.get(position);
        	
        	if(p!=null && p.title != null){
        		return t.form.pages.get(position).title;
        	}
        	
            return  "PAGE " + ( position + 1);//TODO i18n?
        }
    }
    @Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		int id = item.getItemId();
		if (id == android.R.id.home) {
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpTo(this, new Intent(this,
					PendingMissionListActivity.class));
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * @return a unique MapView ID on each call.
	 */
	@Override
	public final int getMapViewId() {
		if(this.mapViewManager==null){
			//registration auto creates mapViewManager
			this.mapViewManager =new MapViewManager();
		}
		int i = this.mapViewManager.getMapViewId();
		Log.v("MAPVIEW","created mapview with id:"+i);
		return i;
	}

	/**
	 * This method is called once by each MapView during its setup process.
	 * 
	 * @param mapView
	 *            the calling MapView.
	 */
	@Override
	public final void registerMapView(MapView mapView) {
		if(this.mapViewManager==null){
			//registration auto creates mapViewManager
			this.mapViewManager =new MapViewManager();
		}
		this.mapViewManager.registerMapView(mapView);
	}
	
	@Override
	public Context getActivityContext(){
		return this;
	}
	
	 @Override
     protected void onResume() {
             super.onResume();
             if(this.mapViewManager !=null){
            	 this.mapViewManager.resumeMapViews();
             }
     }
	 @Override
     protected void onDestroy() {
             super.onDestroy();
             if(this.mapViewManager!=null){
            	 this.mapViewManager.destroyMapViews();
             }
             if(this.spatialiteDatabase!=null){
            	 try {
					this.spatialiteDatabase.close();
 		            Log.v("FORM_EDIT", "Spatialite Database Closed");
				} catch (jsqlite.Exception e) {
		            Log.e("FORM_EDIT", Log.getStackTraceString(e));
				}
             }
     }
    
}
