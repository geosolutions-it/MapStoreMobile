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
import it.geosolutions.geocollect.android.app.R;
import it.geosolutions.geocollect.android.core.form.utils.FormUtils;
import it.geosolutions.geocollect.android.core.mission.MissionFeature;
import it.geosolutions.geocollect.android.core.mission.PendingMissionDetailFragment;
import it.geosolutions.geocollect.android.core.mission.PendingMissionListActivity;
import it.geosolutions.geocollect.android.core.mission.utils.MissionUtils;
import it.geosolutions.geocollect.android.core.mission.utils.PersistenceUtils;
import it.geosolutions.geocollect.android.core.mission.utils.SpatialiteUtils;
import it.geosolutions.geocollect.android.core.widgets.EnableSwipeViewPager;
import it.geosolutions.geocollect.android.core.widgets.UILImageAdapter;
import it.geosolutions.geocollect.model.config.MissionTemplate;
import it.geosolutions.geocollect.model.viewmodel.Field;
import it.geosolutions.geocollect.model.viewmodel.Page;
import it.geosolutions.geocollect.model.viewmodel.type.XType;

import java.util.HashMap;
import java.util.List;

import org.mapsforge.android.maps.MapActivity;
import org.mapsforge.android.maps.MapView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.GridView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

public class FormEditActivity extends SherlockFragmentActivity  implements MapActivity  {

	public static final int CONTEXT_IMAGE_ACTION_DELETE = 8001;
	
	public static final int FORM_CREATE_NEW_MISSIONFEATURE = 1234;

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
	public EnableSwipeViewPager mViewPager;
	/**
	 * the MapViewManager
	 */
	private MapViewManager mapViewManager =new MapViewManager();

	/**
	 * Spatialite Database for persistence
	 */
	public jsqlite.Database spatialiteDatabase;

	/**
	 * ListView for Photo Gallery
	 */
	AbsListView listView;
	
	/**
	 * Singleton of the ImageLoader, used by the Photo Gallery
	 */
	ImageLoader imageLoader = ImageLoader.getInstance();


	/**
	 * Stores the image urls to be shown
	 */
	//String[] imageUrls;

	/**
	 * Options of the Photo Gallery
	 */
	DisplayImageOptions options;
	
	
	MissionFeature mMission;
	String mMissionTableName;
	boolean mCreateMissionFeature;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.form_edit_pager);

        //flag to identify if this activity is for creating a sopralluogo (mission) or a segnalazione (mission) 
        mCreateMissionFeature = getIntent().getExtras().getBoolean(PendingMissionListActivity.ARG_CREATE_MISSIONFEATURE);
        
        Log.d(FormEditActivity.class.getSimpleName(), "will create a missionFeature "+ Boolean.toString(mCreateMissionFeature));
        
        // Initialize database
		if(spatialiteDatabase == null){
			
			spatialiteDatabase = SpatialiteUtils.openSpatialiteDB(this, "geocollect/genova.sqlite");
								
			// Database is correctly open
			if(spatialiteDatabase != null && !spatialiteDatabase.dbversion().equals("unknown")){
				
	            MissionTemplate t = MissionUtils.getDefaultTemplate(this);	            
	          

				if(!PersistenceUtils.createOrUpdateTable(spatialiteDatabase,t.schema_sop.localFormStore, t.schema_sop.fields)){
					Log.e(PendingMissionListActivity.class.getSimpleName(), "error creating "+t.schema_sop.localFormStore+" table ");
				}	            
			}
		}
		
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
                
                Page p = null;
                if(mCreateMissionFeature){
                	p = t.seg_form.pages.get(position);
                }else{
                	p = t.sop_form.pages.get(position);
                }
            	
            	if(p!=null && p.title != null){
            		List<Field> fields =  null;
            		if(mCreateMissionFeature){
            			fields = t.seg_form.pages.get(position).fields;
            		}else{            			
            			fields = t.sop_form.pages.get(position).fields;
            		}
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
				Page p = null;
				if(mCreateMissionFeature){
					p = t.seg_form.pages.get(nPage);
				}else{
					p = t.sop_form.pages.get(nPage);
				}
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
        
        if(mCreateMissionFeature){
        	
        	MissionTemplate t = MissionUtils.getDefaultTemplate(mViewPager.getContext());
        	mMissionTableName= t.schema_seg.localSourceStore+ MissionTemplate.NEW_NOTICE_SUFFIX;
        	
        	//when intent has missionFeature, edit it
        	if(getIntent().getExtras().containsKey(PendingMissionDetailFragment.ARG_ITEM_FEATURE)){
        		
        		mMission = (MissionFeature) getIntent().getExtras().get(PendingMissionDetailFragment.ARG_ITEM_FEATURE);
        		
        	}else{
        		//otherwise create a new one

        		mMission = new MissionFeature();
        		mMission.properties = new HashMap<String, Object>();
        		mMission.typeName = mMissionTableName;
        		
        		//insert empty row
        		Long id = PersistenceUtils.getIDforNewMissionFeatureEntry(spatialiteDatabase, mMissionTableName);
        		if(id == null){
        			Log.e(FormEditActivity.class.getSimpleName(), "could not retrieve max for "+mMissionTableName);
        		}
        		Log.d(FormEditActivity.class.getSimpleName(), "reference id for missionFeature " + String.valueOf(id));
        		mMission.id = String.valueOf(id);

        		PersistenceUtils.insertCreatedMissionFeature(spatialiteDatabase, mMissionTableName, id);
        	}
        }


    }

   

    /**
     * A {@link android.support.v4.app.FragmentStatePagerAdapter} that returns a fragment
     * representing an object in the collection.
     */
    public class FormCollectionPagerAdapter extends FragmentStatePagerAdapter {

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
            SherlockFragment fragment = null;
            if(!mCreateMissionFeature){
            	fragment = new FormPageFragment();
            }else{
            	fragment = new CreateMissionFeatureFormPageFragment();
            	
            }
            Bundle args = new Bundle();
            
            args.putInt(FormPageFragment.ARG_OBJECT,i);
            fragment.setArguments(args);
            return fragment;
        }

        
        @Override
        public int getCount() {
            return mCreateMissionFeature ? t.seg_form.pages.size() : t.sop_form.pages.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
        	Page p = mCreateMissionFeature ? t.seg_form.pages.get(position) : t.sop_form.pages.get(position);
        	
        	if(p!=null && p.title != null){
        		return mCreateMissionFeature ? t.seg_form.pages.get(position).title : t.sop_form.pages.get(position).title;
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
			Intent i = new Intent(this,PendingMissionListActivity.class);
			if(mCreateMissionFeature){
				i.putExtra(PendingMissionListActivity.ARG_CREATE_MISSIONFEATURE, true);
			}
			i.putExtra(PendingMissionListActivity.KEY_NAVIGATING_UP, true);
			NavUtils.navigateUpTo(this, i);
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
             /*
             if(listView != null){
            	 // TODO: add pauseOnScroll and pauseOnFling to the optionsMenu
            	 listView.setOnScrollListener(new PauseOnScrollListener(imageLoader, false, true));
             }
             */

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
	 
	/**
	 * 
	 * @author Lorenzo Pini

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
		super.onActivityResult(requestCode, resultCode, resultIntent);
		
		
		
	}
	 */	 
	 
	// Context menu for images

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// super.onContextItemSelected(item);
		if(item != null){
			switch (item.getItemId()) {
			case CONTEXT_IMAGE_ACTION_DELETE:
				Log.v("FEA", "Need to delete the image");
				if(	item.getMenuInfo() != null  &&  item.getMenuInfo() instanceof AdapterContextMenuInfo ){
					final AdapterContextMenuInfo aminfo = (AdapterContextMenuInfo) item.getMenuInfo();
					Log.v("FEA", "Target view type: "+aminfo.targetView.getClass().getName());
					String imagepath = (String) aminfo.targetView.getTag(R.id.tag_image_path);
					if(imagepath != null){
						Log.v("FEA", "ImagePath: "+imagepath);
						
						final String imagePath = imagepath;
				    	new AlertDialog.Builder(this)
					    .setTitle(R.string.button_confirm_image_delete_title)
					    .setMessage(R.string.button_confirm_image_delete)
					    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					        public void onClick(DialogInterface dialog, int which) { 
					        	
					        	FormUtils.deleteFile(imagePath);
					        	((UILImageAdapter)((GridView)aminfo.targetView.getParent()).getAdapter()).notifyDataSetChanged();
					        }
					     })
					    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
					        public void onClick(DialogInterface dialog, int which) { 
					            // do nothing
					        }
					     })
					     .show();
						
						
						return true;
					}
				}
				break;

			default:
				break;
			}
			
			return false;
		}
		return false;
	}
	
	
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		Log.v("FEA", "CreatingMenu for "+v.getClass().getName());
		super.onCreateContextMenu(menu, v, menuInfo);
		
		// a longPress on an Image is detected
		// Currently supported Actions are:
		// - Delete Image
		if (v instanceof GridView) {
			menu.setHeaderTitle(getString(R.string.gallery_context_menu_title));
			// Delete Option
			menu.add(Menu.NONE, CONTEXT_IMAGE_ACTION_DELETE, Menu.NONE, getString(R.string.gallery_context_menu_delete));
		}

	}
	
	/**
	 * Prompt the user before deleting the selected image
	 * @param imagePath

    public void confirmImageDelete(final String imagePath){
    	new AlertDialog.Builder(this)
	    .setTitle(R.string.button_confirm_image_delete_title)
	    .setMessage(R.string.button_confirm_image_delete)
	    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	        	FormUtils.deleteFile(imagePath);
	        }
	     })
	    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            // do nothing
	        }
	     })
	     .show();
    }
	 */		
}
