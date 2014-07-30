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
package it.geosolutions.geocollect.android.core.form.action;

import java.util.Map;

import com.actionbarsherlock.app.SherlockFragment;

import it.geosolutions.geocollect.android.core.R;
import it.geosolutions.geocollect.android.core.form.utils.FormUtils;
import it.geosolutions.geocollect.android.core.mission.Mission;
import it.geosolutions.geocollect.android.core.mission.PendingMissionListActivity;
import it.geosolutions.geocollect.android.core.mission.utils.MissionUtils;
import it.geosolutions.geocollect.android.core.widgets.dialog.TaskFragment;
import it.geosolutions.geocollect.android.core.widgets.dialog.UploadDialog;
import it.geosolutions.geocollect.model.http.CommitResponse;
import it.geosolutions.geocollect.model.viewmodel.FormAction;
import it.geosolutions.geocollect.model.viewmodel.Page;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public class SendAction extends AndroidAction {
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * ATTRIBUTE_CONFIRM_MESSAGE: if present, this attribute let show a confirm message before saving
	 */
	private static final String ATTRIBUTE_CONFIRM_MESSAGE="confirmMessage";
	
	/**
	 * Fragment upload
	 */
	private static final String FRAGMENT_UPLOAD_DIALOG="FRAGMENT_UPLOAD_DIALOG";
	/**
	 * Constructor for the
	 * @param a the original action
	 */
	public SendAction(FormAction a) {
		super(a);
	}

	/* (non-Javadoc)
	 * @see it.geosolutions.geocollect.android.core.form.action.AndroidAction#performAction(android.support.v4.app.Fragment, it.geosolutions.geocollect.model.viewmodel.Action, it.geosolutions.geocollect.android.core.mission.Mission, it.geosolutions.geocollect.model.viewmodel.Page)
	 */
	@Override
	public void performAction(SherlockFragment fragment, FormAction action, Mission m, Page p) {
			String confirm =null;
			if(attributes!=null){
				confirm= (String) attributes.get(ATTRIBUTE_CONFIRM_MESSAGE);
			}
			
			if(confirm !=null){
				sendData( fragment,  action,  m,  p);
			}else{
				//missing confirm message will send without any confirmation
				sendData( fragment,  action,  m,  p);
			}
		
	}

	/**
	 * Send data to the server
	 * @param fragment
	 * @param action
	 * @param m
	 * @param p
	 */
	private void sendData(SherlockFragment fragment, FormAction action, Mission m, Page pi) {
		//createProgressAlert();
		//uploadData();
		android.support.v4.app.FragmentManager fm = fragment.getSherlockActivity().getSupportFragmentManager();
		Fragment mTaskFragment = (Fragment)fm.findFragmentByTag(FRAGMENT_UPLOAD_DIALOG);
		if(mTaskFragment==null){
			FragmentTransaction ft = fm.beginTransaction();
			String url = (String) action.attributes.get("url");//TODO extenalize
			String murl = (String) action.attributes.get("mediaurl");
			FormUtils.getPhotoUriStrings(m.getOrigin().id);
			mTaskFragment = new UploadDialog(){
				/**
				 * Navigate up to the list
				 */
				/* (non-Javadoc)
				 * @see it.geosolutions.geocollect.android.core.widgets.dialog.UploadDialog#onFinish(android.app.Activity, it.geosolutions.geocollect.model.http.CommitResponse)
				 */
				@Override
				public void onFinish(Activity ctx, CommitResponse result) {
					if(result !=null && result.isSuccess()){
						if(ctx != null){
							Toast.makeText(ctx, getResources().getString(R.string.data_send_success), Toast.LENGTH_LONG).show();
							NavUtils.navigateUpTo(ctx, new Intent(ctx,
									PendingMissionListActivity.class));
						}
						super.onFinish(ctx,result);
					}else{
						Toast.makeText(ctx, R.string.error_sending_data, Toast.LENGTH_LONG).show();
						super.onFinish(ctx,result);
					}
					
				}
			};
			
			Bundle arguments = new Bundle();
			arguments.putString(UploadDialog.PARAMS.DATAURL, url);
			arguments.putString(UploadDialog.PARAMS.MEDIAURL, murl);
			arguments.putString(UploadDialog.PARAMS.DATA, MissionUtils.generateJsonString(null,m));
			arguments.putString(UploadDialog.PARAMS.ORIGIN_ID, m.getOrigin().id);
			arguments.putString(UploadDialog.PARAMS.MISSION_ID, m.getTemplate().id);
			arguments.putStringArray(UploadDialog.PARAMS.MEDIA, FormUtils.getPhotoUriStrings(m.getOrigin().id));
			
			mTaskFragment.setArguments(arguments);
			
			((DialogFragment)mTaskFragment).setCancelable(false);
		    ft.add(mTaskFragment, FRAGMENT_UPLOAD_DIALOG);
			//ft.add(R.id.embedded, newFragment);
			ft.commit();
			
		}
		
		
	}

	
	
}



		