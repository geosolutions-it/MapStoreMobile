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

import it.geosolutions.geocollect.android.app.BuildConfig;
import it.geosolutions.geocollect.android.app.R;
import it.geosolutions.geocollect.android.core.form.utils.FormUtils;
import it.geosolutions.geocollect.android.core.login.LoginActivity;
import it.geosolutions.geocollect.android.core.mission.Mission;
import it.geosolutions.geocollect.android.core.mission.PendingMissionListActivity;
import it.geosolutions.geocollect.android.core.mission.utils.MissionUtils;
import it.geosolutions.geocollect.android.core.widgets.dialog.UploadDialog;
import it.geosolutions.geocollect.model.config.MissionTemplate;
import it.geosolutions.geocollect.model.http.CommitResponse;
import it.geosolutions.geocollect.model.viewmodel.FormAction;
import it.geosolutions.geocollect.model.viewmodel.Page;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public class SendAction extends AndroidAction {
	/**
	 * Tag for Logging
	 */
	private static String TAG = "SendAction";
	
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
	public void performAction(final SherlockFragment fragment, final FormAction action, final Mission m, final Page p) {
			
		if(fragment == null){
			if(BuildConfig.DEBUG){
				Log.w(TAG, "Given fragment is NULL, cannot perform action");
			}
			return;
		}
		if(fragment.getActivity() == null){
			if(BuildConfig.DEBUG){
				Log.w(TAG, "Given fragment Activity is NULL, cannot perform action");
			}
			return;
		}

		
		String tableName = m.getTemplate().id+"_data";
		if(m.getTemplate().schema_sop != null && m.getTemplate().schema_sop.localFormStore != null && !m.getTemplate().schema_sop.localFormStore.isEmpty()){
    		tableName = m.getTemplate().schema_sop.localFormStore;
    	}
		
		MissionTemplate t = MissionUtils.getDefaultTemplate(fragment.getActivity());

		String originIDString = MissionUtils.getMissionGCID(m);
		
		// check database for mandatory fields
		ArrayList<String> notFilledMandatoryEntries = MissionUtils.checkIfAllMandatoryFieldsAreSatisfied(t.sop_form, originIDString, m.db, tableName);	
		
		if(notFilledMandatoryEntries.size() > 0){
			String missing = fragment.getString(R.string.mandatory_fields_not_filled)+"\n\n";
			for(String string : notFilledMandatoryEntries){
				missing+=" \u2022 "+ string+"\n";
			}
			missing += "\n"+fragment.getString(R.string.mandatory_fields_please_fill);
			Log.d(TAG, "missing "+Html.fromHtml(missing));
			
			new AlertDialog.Builder(fragment.getActivity())
		    .setTitle(R.string.missing_data)
		    .setMessage(missing)
		    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int which) { 
		        	//nothing		        	
		        }
		     })
		     .show();
			
			return;
		}
		
		
		
		String confirm =null;
			if(attributes!=null){
				confirm= (String) attributes.get(ATTRIBUTE_CONFIRM_MESSAGE);
			}
			
			if(confirm !=null){
				
				new AlertDialog.Builder(fragment.getActivity())
			    .setTitle(R.string.sending_data)
			    .setMessage(confirm)
			    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) { 
			        	sendData( fragment,  action,  m,  p);
			        	
			        }
			     })
			    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
			        public void onClick(DialogInterface dialog, int which) { 
			            // do nothing
			        }
			     })
			     .show();
				
				
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
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(fragment.getSherlockActivity());
			
			//String authKey = prefs.getString(LoginActivity.PREFS_AUTH_KEY, null);
			String email = prefs.getString(LoginActivity.PREFS_USER_EMAIL, null);
			String pass = prefs.getString(LoginActivity.PREFS_PASSWORD, null);
			
			int defaultImageSize = 1000;
			try{
				defaultImageSize = Integer.parseInt((String) m.getValueByTag(fragment.getSherlockActivity(), "config.maxImageSize"));	
			}catch( NumberFormatException e ){
				Log.e(SendAction.class.getSimpleName(), e.getClass().getSimpleName(),e);
			}
			catch( NullPointerException e){
				Log.e(SendAction.class.getSimpleName(), e.getClass().getSimpleName(),e);
			}
			
			String originIDString = MissionUtils.getMissionGCID(m);
			
			FormUtils.resizeImagesToMax(fragment.getActivity().getBaseContext(), originIDString, defaultImageSize);
			
			Bundle arguments = new Bundle();
//			arguments.putString(UploadDialog.PARAMS.DATAURL, url);
//			arguments.putString(UploadDialog.PARAMS.MEDIAURL, murl);
//			arguments.putString(UploadDialog.PARAMS.DATA, MissionUtils.generateJsonString(null,m));
//			arguments.putString(UploadDialog.PARAMS.ORIGIN_ID, originIDString);
//			arguments.putString(UploadDialog.PARAMS.MISSION_ID, m.getTemplate().id);
//			arguments.putString(UploadDialog.PARAMS.BASIC_AUTH, LoginRequestInterceptor.getB64Auth(email, pass));
//			arguments.putStringArray(UploadDialog.PARAMS.MEDIA, FormUtils.getPhotoUriStrings(fragment.getActivity().getBaseContext(),originIDString));
//			arguments.putBoolean(UploadDialog.PARAMS.MISSION_FEATURE_UPLOAD, false);
			
			mTaskFragment.setArguments(arguments);
			
			((DialogFragment)mTaskFragment).setCancelable(false);
		    ft.add(mTaskFragment, FRAGMENT_UPLOAD_DIALOG);
			//ft.add(R.id.embedded, newFragment);
			ft.commit();
			
		}
		
		
	}

	
	
}



		