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

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import jsqlite.Exception;
import jsqlite.Stmt;
import it.geosolutions.android.map.fragment.MapFragment;
import it.geosolutions.geocollect.android.core.R;
import it.geosolutions.geocollect.android.core.form.action.AndroidAction;
import it.geosolutions.geocollect.android.core.form.utils.FormBuilder;
import it.geosolutions.geocollect.android.core.form.utils.FormUtils;
import it.geosolutions.geocollect.android.core.mission.Mission;
import it.geosolutions.geocollect.android.core.mission.utils.MissionUtils;
import it.geosolutions.geocollect.android.core.widgets.DatePicker;
import it.geosolutions.geocollect.model.config.MissionTemplate;
import it.geosolutions.geocollect.model.viewmodel.FormAction;
import it.geosolutions.geocollect.model.viewmodel.Field;
import it.geosolutions.geocollect.model.viewmodel.Page;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

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
	/**
	 * Reference to the Activity Database
	 */
	jsqlite.Database db;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.d(TAG, "onAttach(): activity = " + activity);
		if(activity instanceof FormEditActivity){
			Log.d(TAG, "Connecting to Activity database");
			db = ((FormEditActivity)activity).spatialiteDatabase;
		}else{
			Log.w(TAG, "Could not connect to Activity database");
		}
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
			page = t.form.pages.get(pageNumber);
			
		}
		if(savedInstanceState !=null){
			
			mission = (Mission)savedInstanceState.getSerializable(ARG_MISSION);
		}
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
		Log.v("ACTION","performing action"+a);
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
		// TODO: move this block on database utils?
		if(db != null){
			String s;
			Stmt st = null;
			for(Field f : page.fields){
				if(f == null )continue;
				try {
					// TODO: load all the fields in one query
					s = "SELECT "+ f.fieldId +" FROM 'punti_accumulo_data' WHERE ORIGIN_ID = '"+mission.getOrigin().id+"';";
					if(jsqlite.Database.complete(s)){
						st = db.prepare(s);
						if(st.step()){
							View v = this.mFormView.findViewWithTag(f.fieldId);
//////////////////////////
							if (f.xtype == null) {
								//textfield as default
								((TextView)v).setText(st.column_string(0));
							} else {
								// switch witch widget create
								switch (f.xtype) {
								case textfield:
									((TextView)v).setText(st.column_string(0));
									break;
								case textarea:
									((TextView)v).setText(st.column_string(0));
									break;
								case datefield:
									if(st.column_string(0) != null){
										((DatePicker)v).setDate(st.column_string(0));
									}
									break;
								case checkbox:
									// TODO
									break;
								case spinner:
									// TODO
									break;
								case label:
									// skip
									break;
								case separator:
									// skip
									break;
								case mapViewPoint:
									// TODO
									//addMapViewPoint(f,mFormView,context,mission);
									break;
								default:
									//textfield as default
									((TextView)v).setText(st.column_string(0));
								}
							}							
////////////////////////
						}else{
							// no record found, creating..
							Log.v(TAG, "No record found, creating..");
							s = "INSERT INTO 'punti_accumulo_data' ( ORIGIN_ID ) VALUES ( '"+mission.getOrigin().id+"');";
							st = db.prepare(s);
							if(st.step()){
								// nothing will be returned anyway
							}
							Log.v(TAG, "Record created");

						}
						st.close();
					}
				} catch (Exception e) {
					Log.e(TAG, Log.getStackTraceString(e));
				}
			}
			if(st!=null){
				try {
					st.close();
				} catch (Exception e) {
					//Log.e(TAG, Log.getStackTraceString(e));
					// ignore
				}
			}
		} // if db
		
		
		// the view hierarchy is now complete
		mDone = true;
	}
    
    
    @Override 
    public void onSaveInstanceState(Bundle outState) {
    	outState.putSerializable(ARG_MISSION, mission);
    	
		// TODO: move this block on database utils?
		if(db != null){
			String s;
			String value;
			Stmt st = null;
			for(Field f : page.fields){
				if(f == null )continue;

				View v = this.mFormView.findViewWithTag(f.fieldId);

				if(v == null){
					Log.w(TAG, "Tag not found : "+f.fieldId);
					continue;
				}
				
				if (f.xtype == null) {
					// TODO: load all the fields in one query
					value = ((TextView)v).getText().toString();
				} else {
					// switch witch widget create
					switch (f.xtype) {
					case textfield:
						value = ((TextView)v).getText().toString();
						break;
					case textarea:
						value = ((TextView)v).getText().toString();
						break;
					case datefield:
						value = ((DatePicker)v).getText().toString();
						continue;
						//break;
					case checkbox:
						// TODO
						continue;
						//break;
					case spinner:
						// TODO
						continue;
						//break;
					case label:
						// skip
						continue;
						//break;
					case separator:
						// skip
						continue;
						//break;
					case mapViewPoint:
						// TODO
						continue;
						//addMapViewPoint(f,mFormView,context,mission);
						//break;
					default:
						//textfield as default
						value = ((TextView)v).getText().toString();
					}
				}
				try {	
					
					s = "UPDATE 'punti_accumulo_data' SET "+ f.fieldId +" = '"+ value +"' WHERE ORIGIN_ID = '"+mission.getOrigin().id+"';";
					Log.v(TAG, "Query :\n"+s);
					
					st = db.prepare(s);
					if(st.step()){
						Log.v(TAG, "Updated");
					}else{
						Log.v(TAG, "Update failed");
					}
					
				} catch (Exception e) {
					Log.e(TAG, Log.getStackTraceString(e));
				}
			}
			if(st!=null){
				try {
					st.close();
				} catch (Exception e) {
					//Log.e(TAG, Log.getStackTraceString(e));
					// ignore
				}
			}
		} // if db
    	
    }
    
    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "onActivityCreated(): savedInstanceState = "
				+ savedInstanceState);

		toggleLoading(true);
		getLoaderManager().initLoader(0, null, this);
	}
    
    public Loader<Void> onCreateLoader(int id, Bundle args) {
		Log.d(TAG, "onCreateLoader(): id=" + id);
		Loader<Void> loader = new AsyncTaskLoader<Void>(getActivity()) {

			@Override
			public Void loadInBackground() {
				Mission m =  (Mission) getActivity().getIntent().getExtras().getSerializable(ARG_MISSION);
				m.setTemplate(MissionUtils.getDefaultTemplate(getSherlockActivity()));
				mission =m;
				return null;
			}
		};
		//TODO create loader;
		loader.forceLoad();
		return loader;
	}

	public void onLoadFinished(Loader<Void> id, Void result) {
		Log.d(TAG, "onLoadFinished(): id=" + id);
		toggleLoading(false);
		buildForm();
	}

	public void onLoaderReset(Loader<Void> loader) {
		Log.d(TAG, "onLoaderReset(): id=" + loader.getId());
	}

	private void toggleLoading(boolean show) {
		mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
		mFormView.setGravity(show ? Gravity.CENTER : Gravity.TOP);
	}
	
	@Override
	public void onDestroyView() {
		super.onDestroyView();
		Log.d(TAG, "onDestroyView()");
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

}