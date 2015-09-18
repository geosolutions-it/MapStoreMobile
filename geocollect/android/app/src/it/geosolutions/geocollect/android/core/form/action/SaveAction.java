package it.geosolutions.geocollect.android.core.form.action;

import it.geosolutions.geocollect.android.app.BuildConfig;
import it.geosolutions.geocollect.android.app.R;
import it.geosolutions.geocollect.android.core.mission.Mission;
import it.geosolutions.geocollect.android.core.mission.PendingMissionListActivity;
import it.geosolutions.geocollect.android.core.mission.utils.MissionUtils;
import it.geosolutions.geocollect.android.core.mission.utils.PersistenceUtils;
import it.geosolutions.geocollect.model.config.MissionTemplate;
import it.geosolutions.geocollect.model.viewmodel.FormAction;
import it.geosolutions.geocollect.model.viewmodel.Page;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

public class SaveAction extends AndroidAction {
	
	/**
	 * Tag for Logging
	 */
	private static String TAG = "SaveAction";
	
	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 1L;

	public SaveAction(FormAction a) {
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
		
		final String finalTableName = tableName;
		
		
		new AlertDialog.Builder(fragment.getActivity())
	    .setTitle(R.string.saving_data_title)
	    .setMessage(fragment.getString(R.string.saving_data_message, fragment.getString(R.string.survey)))
	    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 

	        	
	        	//save "uploadable" state
	        	HashMap<String,ArrayList<String>> uploadables = PersistenceUtils.loadUploadables(fragment.getSherlockActivity());
	        	
	        	
	        	if(uploadables.containsKey(finalTableName)){
	        		
	        		//list exists, add this entry
	        		uploadables.get(finalTableName).add(MissionUtils.getFeatureGCID( m.getOrigin()));
	        		
	        		
	        	}else{
	        		
	        		ArrayList<String> ids = new ArrayList<String>();
	        		ids.add(MissionUtils.getFeatureGCID(m.getOrigin()));
	        		
	        		uploadables.put(finalTableName, ids);
	        	}
	        	
	        	PersistenceUtils.saveUploadables(fragment.getSherlockActivity(), uploadables);
	        	
	        	
	        	//confirm save to user
	        	Toast.makeText(fragment.getSherlockActivity(), fragment.getResources().getString(R.string.entry_saved_success,fragment.getResources().getString(R.string.survey)), Toast.LENGTH_LONG).show();
	        	//done, navigate back
	        	Intent i = new Intent(fragment.getSherlockActivity(), PendingMissionListActivity.class);
	        	i.putExtra(PendingMissionListActivity.KEY_NAVIGATING_UP, true);
	        	NavUtils.navigateUpTo(fragment.getSherlockActivity(), i );
	        }
	     })
	    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int which) { 
	            // do nothing
	        }
	     })
	     .show();
		
	}

}
