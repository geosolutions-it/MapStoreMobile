package it.geosolutions.geocollect.android.core.form.action;

import it.geosolutions.android.map.wfs.geojson.GeoJson;
import it.geosolutions.geocollect.android.core.R;
import it.geosolutions.geocollect.android.core.form.FormEditActivity;
import it.geosolutions.geocollect.android.core.login.LoginActivity;
import it.geosolutions.geocollect.android.core.login.utils.LoginRequestInterceptor;
import it.geosolutions.geocollect.android.core.mission.MissionFeature;
import it.geosolutions.geocollect.android.core.mission.PendingMissionListActivity;
import it.geosolutions.geocollect.android.core.mission.utils.MissionUtils;
import it.geosolutions.geocollect.android.core.mission.utils.PersistenceUtils;
import it.geosolutions.geocollect.android.core.widgets.dialog.UploadDialog;
import it.geosolutions.geocollect.model.config.MissionTemplate;
import it.geosolutions.geocollect.model.http.CommitResponse;
import it.geosolutions.geocollect.model.viewmodel.FormAction;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import jsqlite.Database;
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
 * class to start the upload process for a created missionFeature
 * 
 * @author robertoehler
 *
 */
public class SendMissionFeatureAction extends FormAction {
	
	private static final long serialVersionUID = 6464461286516024625L;

	final String TAG = SendMissionFeatureAction.class.getSimpleName();
	
	private static final String ATTRIBUTE_CONFIRM_MESSAGE="confirmMessage";
	
	/**
	 * Fragment upload
	 */
	private static final String FRAGMENT_UPLOAD_DIALOG="FRAGMENT_UPLOAD_DIALOG";
	
	public SendMissionFeatureAction(FormAction a){
		this.attributes = a.attributes;
		this.dataModel = a.dataModel;
		this.iconCls = a.iconCls;
		this.id = a.id;
		this.type = a.type; 
		this.text = a.text;
	}
	/**
	 * check if all mandatory fields are filled, confirm the upload 
	 * @param fragment
	 * @param action
	 * @param missionFeature
	 */
	public void performAction(final SherlockFragment fragment, final FormAction action, final MissionFeature missionFeature) {
	
		
		Log.d(SendMissionFeatureAction.class.getSimpleName(), "perform sendaction");
		
		MissionTemplate t = MissionUtils.getDefaultTemplate(fragment.getActivity());
		
		Database db = ((FormEditActivity) fragment.getSherlockActivity()).spatialiteDatabase;
		
		// check database for mandatory fields
		ArrayList<String> notFilledMandatoryEntries = MissionUtils.checkIfAllMandatoryFieldsAreSatisfied(t.seg_form,"ORIGIN_ID",db,t.schema_seg.localSourceStore+ "_new");
		
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
					sendData( fragment,  action,  missionFeature);

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
			sendData( fragment,  action,  missionFeature);
		}

	}

	/**
	 * Send data to the server
	 * @param fragment
	 * @param action
	 * @param missionFeature to send
	 */
	private void sendData(final SherlockFragment fragment, FormAction action,final MissionFeature missionFeature) {
		//createProgressAlert();
		//uploadData();
		android.support.v4.app.FragmentManager fm = fragment.getSherlockActivity().getSupportFragmentManager();
		Fragment mTaskFragment = (Fragment)fm.findFragmentByTag(FRAGMENT_UPLOAD_DIALOG);
		if(mTaskFragment==null){
			FragmentTransaction ft = fm.beginTransaction();
			String url = (String) action.attributes.get("url");
			String murl = (String) action.attributes.get("mediaurl");

			mTaskFragment = new UploadDialog(){
				/**
				 * Delete this entry if uploading was successful
				 * 
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
							
							//delete this mission							
							final MissionTemplate t = MissionUtils.getDefaultTemplate(fragment.getSherlockActivity());
				        	final Database db = ((FormEditActivity)fragment.getSherlockActivity()).spatialiteDatabase;				        	
				        	PersistenceUtils.deleteCreatedMissionFeature(db, t.schema_seg.localSourceStore+ "_new", missionFeature);
				        	
				        	//back to list
				        	Intent i = new Intent(ctx,PendingMissionListActivity.class);
				        	i.putExtra(PendingMissionListActivity.ARG_CREATE_MISSIONFEATURE, true);
				        	NavUtils.navigateUpTo(ctx, i);
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
			
			GeoJson gson = new GeoJson();
			String c = gson.toJson( missionFeature);
			String data = null;
			try {
				data = new String(c.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				Log.e(TAG, "error transforming missionfeature to gson",e);
			}
			
			arguments.putString(UploadDialog.PARAMS.DATA, data);
			arguments.putString(UploadDialog.PARAMS.ORIGIN_ID, missionFeature.id);
			arguments.putBoolean(UploadDialog.PARAMS.MISSION_FEATURE_UPLOAD, true);
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(fragment.getSherlockActivity());
			
			//String authKey = prefs.getString(LoginActivity.PREFS_AUTH_KEY, null);
			String email = prefs.getString(LoginActivity.PREFS_USER_EMAIL, null);
			String pass = prefs.getString(LoginActivity.PREFS_PASSWORD, null);
						
			arguments.putString(UploadDialog.PARAMS.BASIC_AUTH, LoginRequestInterceptor.getB64Auth(email, pass));
			
			mTaskFragment.setArguments(arguments);
			
			((DialogFragment)mTaskFragment).setCancelable(false);
		    ft.add(mTaskFragment, FRAGMENT_UPLOAD_DIALOG);
			//ft.add(R.id.embedded, newFragment);
			ft.commit();
			
		}
		
		
	}
}
