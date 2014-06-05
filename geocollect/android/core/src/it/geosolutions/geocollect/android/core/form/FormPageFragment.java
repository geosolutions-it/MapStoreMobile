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
import it.geosolutions.geocollect.android.core.form.utils.FormBuilder;
import it.geosolutions.geocollect.android.core.mission.Mission;
import it.geosolutions.geocollect.android.core.mission.utils.MissionUtils;
import it.geosolutions.geocollect.model.config.MissionTemplate;
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
	
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		Log.d(TAG, "onAttach(): activity = " + activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
    
    
    private void buildForm() {
		// if the view hierarchy was already build, skip this
		if (mDone)
			return;

		FormBuilder.buildForm(getActivity(), this.mFormView,page.fields, mission);//TODO page is not enougth, some data should be accessible like constants and data

		// the view hierarchy is now complete
		mDone = true;
	}
    @Override 
    public void onSaveInstanceState(Bundle outState) {
    	outState.putSerializable(ARG_MISSION, mission);
    	
    	
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