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


import it.geosolutions.android.map.fragment.MapFragment;
import it.geosolutions.geocollect.android.core.R;
import it.geosolutions.geocollect.android.core.form.action.AndroidAction;
import it.geosolutions.geocollect.android.core.form.action.CameraAction;
import it.geosolutions.geocollect.android.core.form.utils.FormBuilder;
import it.geosolutions.geocollect.android.core.form.utils.FormUtils;
import it.geosolutions.geocollect.android.core.mission.Mission;
import it.geosolutions.geocollect.android.core.mission.utils.MissionUtils;
import it.geosolutions.geocollect.android.core.mission.utils.PersistenceUtils;
import it.geosolutions.geocollect.android.core.widgets.UILImageAdapter;
import it.geosolutions.geocollect.model.config.MissionTemplate;
import it.geosolutions.geocollect.model.viewmodel.Field;
import it.geosolutions.geocollect.model.viewmodel.FormAction;
import it.geosolutions.geocollect.model.viewmodel.Page;
import it.geosolutions.geocollect.model.viewmodel.type.XType;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager.LayoutParams;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * This Fragment contains form field for a page of the <Form>.
 * 
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public class FormPageFragment extends MapFragment  implements LoaderCallbacks<Void>{
	/**
	 * Tag for logs
	 */
	public static final String TAG = "FormPage";
	
	/**
	 * The argument for the page
	 */
    public static final String ARG_OBJECT = "Page";
    public static final String ARG_MISSION = "MISSION";
    private ScrollView mScrollView;
    private Page page;
	private LinearLayout mFormView;
	private ProgressBar mProgressView;
	private boolean mDone;
	private Mission mission;
	private boolean visibleToUser;
	
	/**
	 * Reference to the ImageLoader instance
	 * Has value only if this page contains a Photo gallery
	 */
	ImageLoader imageLoader;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.d(TAG, "onAttach(): activity = " + activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Mission m =  (Mission) getActivity().getIntent().getExtras().getSerializable(ARG_MISSION);
		mission = m;
		Log.d(TAG, "onCreate(): savedInstanceState = " + savedInstanceState);
		Integer pageNumber = (Integer) getArguments().get(ARG_OBJECT);
		if(pageNumber!=null){
			MissionTemplate t = MissionUtils.getDefaultTemplate(getActivity());
			//if page number exists i suppose pages is not empty
			page = t.sop_form.pages.get(pageNumber);
			
		}
		if(savedInstanceState !=null){
			
			mission = (Mission)savedInstanceState.getSerializable(ARG_MISSION);
		}
		//This allow form fragments to 
		// display actions in the action bar
		setHasOptionsMenu(true);
		
	}
	
	@Override
	public void onCreateOptionsMenu(
	      Menu menu, MenuInflater inflater) {
		// add actions from the page configuration
		if(this.page.actions == null) return;
	   //add actions associated to the page
	   for(int i = 0;i< this.page.actions.size();i++){
		   FormAction a =this.page.actions.get(i);
		   
		   MenuItem item = menu.add(Menu.NONE, a.id , Menu.NONE, a.text);
		   item.setIcon(FormUtils.getDrawable(a.iconCls));
		   item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		   Log.v("ACTION","added action"+ a.name);
	   }
	   super.onCreateOptionsMenu(menu,inflater);
	}
	/* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragment#onOptionsItemSelected(com.actionbarsherlock.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		//search for the action using the id (the index should not be supported
		if(this.page.actions !=null){
			for(FormAction a : this.page.actions){
				if(id == a.id){
					performAction(a);
				}
			}
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Performs and <Action> in the current page.
	 * This function is call when the user tap an action
	 * @param a
	 */
	private void performAction(FormAction a) {
		Log.v("ACTION","performing action "+a);
		AndroidAction aa = FormUtils.getAndroidAction(a);
		if(aa!=null){
			aa.performAction(this, a, mission, page);
		}
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.d(TAG, "onCreateView(): container = " + container
				+ "savedInstanceState = " + savedInstanceState);

		if (mScrollView == null) {
			// normally inflate the view hierarchy
			mScrollView = (ScrollView) inflater.inflate(R.layout.form_page_fragment,
					container, false);
			mFormView = (LinearLayout) mScrollView.findViewById(R.id.formcontent);
			mProgressView = (ProgressBar) mScrollView
					.findViewById(R.id.loading);
		} else {
			// mScrollView is still attached to the previous view hierarchy
			// we need to remove it and re-attach it to the current one
			ViewGroup parent = (ViewGroup) mScrollView.getParent();
			parent.removeView(mScrollView);
		}
		return mScrollView;
	}
    
    /**
     * Creates the page content cycling the page fields
     */
    private void buildForm() {
		// if the view hierarchy was already build, skip this
		if (mDone)
			return;

		FormBuilder.buildForm(getActivity(), this.mFormView, page.fields, mission);//TODO page is not enough, some data should be accessible like constants and data
		
		// It is safe to initialize field here because buildForm is a callback of the onActivityCreated();
		PersistenceUtils.loadPageData(page, mFormView, mission, getActivity(),true);

		// TODO: add a timer and start an animation for the tutorial
		if(page.attributes != null && page.attributes.containsKey("tutorial")){
			if ( page.attributes.get("tutorial") != null &&
				 page.attributes.get("tutorial") instanceof Boolean &&
				 (Boolean) page.attributes.get("tutorial")){
				
				Log.v(TAG, "Showing tutorial");
				
				// create new Layout
				FrameLayout overlayFramelayout = new FrameLayout(getSherlockActivity());
				
				View view = getSherlockActivity().getLayoutInflater().inflate(
						R.layout.swipe_overlay,
						overlayFramelayout,
						false
						);
				
				
				overlayFramelayout.addView(view);

				// Viewpager layoutparams
				LayoutParams layoutParams = new LayoutParams();
				layoutParams.gravity = Gravity.BOTTOM;
				//getSherlockActivity().addContentView(overlayFramelayout, layoutParams);
				this.mFormView.addView(overlayFramelayout, layoutParams);
				
			}
		}
		// the view hierarchy is now complete
		mDone = true;
	
		// TODO: merge this code block with the previous "tutorial"
		if(visibleToUser && page.attributes != null && page.attributes.containsKey("message")){
			Toast.makeText(getSherlockActivity(), (String) page.attributes.get("message"), Toast.LENGTH_LONG).show();
		}

	}
    
    @Override
    public void setMenuVisibility(boolean menuVisible) {
    	super.setMenuVisibility(menuVisible);
    	visibleToUser = menuVisible;
    	if(imageLoader != null && imageLoader.isInited()){
			if(menuVisible)
				imageLoader.resume();
			else
				imageLoader.pause();
		}
    }
    
    @Override 
    public void onSaveInstanceState(Bundle outState) {
    	outState.putSerializable(ARG_MISSION, mission);
    	
    	PersistenceUtils.storePageData(page, mFormView, mission);
    	    	
    }
    
    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "onActivityCreated(): savedInstanceState = "
				+ savedInstanceState);

		toggleLoading(true);
		getLoaderManager().restartLoader(0, null, this);
	}
    
    public Loader<Void> onCreateLoader(int id, Bundle args) {
		Log.d(TAG, "onCreateLoader(): id=" + id);
		Loader<Void> loader = new AsyncTaskLoader<Void>(getActivity()) {

			@Override
			public Void loadInBackground() {
				Activity activity = getSherlockActivity();
				
				Mission m =  (Mission) getActivity().getIntent().getExtras().getSerializable(ARG_MISSION);//TODO investigate sometimes m is null
				if(m!=null){
					m.setTemplate(MissionUtils.getDefaultTemplate(activity));
					if(activity instanceof FormEditActivity){
						Log.d(TAG, "Loader: Connecting to Activity database");
						m.db = ((FormEditActivity)activity).spatialiteDatabase;
					}else{
						Log.w(TAG, "Loader: Could not connect to Activity database");
					}

					
					mission =m;
					return null;
				}else{
					//TODO notify error
					Toast.makeText(getActivity(), R.string.error_getting_data_from_database, Toast.LENGTH_LONG).show();
					getActivity().finish();
					return null;
				}
				
			}
		};
		//TODO create loader;
		loader.forceLoad();
		return loader;
	}

    
	/**
	 * Handle the results of the CameraAction
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult(): "+requestCode);
	    super.onActivityResult(requestCode, resultCode, data);
	    
	    if(requestCode == CameraAction.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK){
	    	if( this.page.fields != null &&
	    			this.mission != null &&
	    			this.mission.getOrigin() != null &&
	    			this.mission.getOrigin().id != null &&
	    			!this.mission.getOrigin().id.isEmpty()){
	    		
	    		
				for(Field f : page.fields){
					if(f == null || f.xtype != XType.photo)
						continue;
					
					// TODO: use a non-string tag
					GridView grid = (GridView) this.mFormView.findViewWithTag("__photo__");
					
			    	if( grid != null && ImageLoader.getInstance().isInited()){
			    		
		    		    //((UILImageAdapter)grid.getAdapter()).setImageUrls(FormUtils.getPhotoUriStrings(this.mission.getOrigin().id));
		    		    ((UILImageAdapter)grid.getAdapter()).notifyDataSetChanged();
			    	}
			    	break;
		    	}
	    		
	    	}
	    }
	    
	}
	
	
    /**
     * 
     */
	public void onLoadFinished(Loader<Void> id, Void result) {
		Log.d(TAG, "onLoadFinished(): id=" + id);
		toggleLoading(false);
		buildForm();
		
		for(Field f : page.fields){
			if(f == null || f.xtype != XType.photo)
				continue;
			imageLoader = ImageLoader.getInstance();
	    	break;
    	}
	}

	public void onLoaderReset(Loader<Void> loader) {
		Log.d(TAG, "onLoaderReset(): id=" + loader.getId());
	}

	private void toggleLoading(boolean show) {
		mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
		mFormView.setGravity(show ? Gravity.CENTER : Gravity.TOP);
	}
	
	@Override
	public void onStop() {
		super.onStop();
		Log.d(TAG, "onStop()");
		//PersistenceUtils.storePageData(page, mFormView, mission);
		if(imageLoader != null){
			imageLoader.stop();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.d(TAG, "onPause()");

		if(imageLoader != null && imageLoader.isInited()){
			imageLoader.pause();
		}

		PersistenceUtils.storePageData(page, mFormView, mission);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Log.d(TAG, "onDestroyView()");
		if(imageLoader != null){
			imageLoader.destroy();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy()");
	}

	@Override
	public void onDetach() {
		super.onDetach();
		Log.d(TAG, "onDetach()");
	}

	@Override
	public void onResume() {
		super.onResume();
		if(imageLoader != null && imageLoader.isInited()){
			imageLoader.resume();
		}
	}
	
}