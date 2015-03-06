package it.geosolutions.geocollect.android.core.form.action;

import it.geosolutions.geocollect.android.core.R;
import it.geosolutions.geocollect.android.core.form.FormEditActivity;
import it.geosolutions.geocollect.android.core.mission.Mission;
import it.geosolutions.geocollect.android.core.mission.MissionFeature;
import it.geosolutions.geocollect.android.core.mission.PendingMissionListActivity;
import it.geosolutions.geocollect.android.core.mission.utils.MissionUtils;
import it.geosolutions.geocollect.android.core.mission.utils.PersistenceUtils;
import it.geosolutions.geocollect.model.config.MissionTemplate;
import it.geosolutions.geocollect.model.viewmodel.FormAction;
import it.geosolutions.geocollect.model.viewmodel.Page;

import java.util.ArrayList;
import java.util.HashMap;

import jsqlite.Database;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

public class SaveMissionFeatureAction extends AndroidAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SaveMissionFeatureAction(FormAction a){
		super(a);
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
		
		final MissionTemplate t = MissionUtils.getDefaultTemplate(fragment.getActivity());
		
		Database db = ((FormEditActivity) fragment.getSherlockActivity()).spatialiteDatabase;
		
		final String tableName = t.schema_seg.localSourceStore+ MissionTemplate.NEW_NOTICE_SUFFIX;
		
		// check database for mandatory fields
		ArrayList<String> notFilledMandatoryEntries = MissionUtils.checkIfAllMandatoryFieldsAreSatisfied(t.seg_form,"ORIGIN_ID",db,tableName);
		
		if(notFilledMandatoryEntries.size() > 0){
			String missing = fragment.getString(R.string.mandatory_fields_not_filled)+"\n\n";
			for(String string : notFilledMandatoryEntries){
				missing+=" \u2022 "+ string+"\n";
			}
			missing += "\n"+fragment.getString(R.string.mandatory_fields_please_fill);
			Log.d(SaveMissionFeatureAction.class.getSimpleName(), "missing "+Html.fromHtml(missing));
			
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
		
		new AlertDialog.Builder(fragment.getActivity())
	    .setTitle(R.string.saving_data_title)
	    .setMessage(fragment.getString(R.string.saving_data_message,fragment.getString(R.string.new_entry)))
	    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 

	        	// Edit the MissionFeature for a better JSON compliance
	        	MissionUtils.alignPropertiesTypes(missionFeature, t.schema_seg.fields );
	        		        	
	        	//edit uploadable state
	        	HashMap<String,ArrayList<String>> uploadables = PersistenceUtils.loadUploadables(fragment.getSherlockActivity());
	        	
	        	if(uploadables.containsKey(tableName)){
	        		
	        		//list exists, add this entry
	        		uploadables.get(tableName).add(missionFeature.id);
	        		
	        	}else{
	        		
	        		ArrayList<String> ids = new ArrayList<String>();
	        		ids.add(missionFeature.id);
	        		
	        		uploadables.put(tableName, ids);
	        	}
	        	
	        	PersistenceUtils.saveUploadables(fragment.getSherlockActivity(), uploadables);
	        		        	
	        	//confirm save to user
	        	Toast.makeText(fragment.getSherlockActivity(), fragment.getResources().getString(R.string.entry_saved_success,fragment.getResources().getString(R.string.new_entry)), Toast.LENGTH_LONG).show();
	        	//done, navigate back to list of "new" surveys
	        	Intent i = new Intent(fragment.getSherlockActivity(),PendingMissionListActivity.class);
	        	i.putExtra(PendingMissionListActivity.ARG_CREATE_MISSIONFEATURE, true);
	        	NavUtils.navigateUpTo(fragment.getSherlockActivity(), i);
	        	
	        }
	     })
	    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            // do nothing
	        }
	     })
	     .show();
		
	}

	@Override
	public void performAction(SherlockFragment fragment, FormAction action,
			Mission m, Page p) {
		// TODO Auto-generated method stub
		
	}

}
