package it.geosolutions.geocollect.android.core.test;

import it.geosolutions.android.map.wfs.WFSGeoJsonFeatureLoader;
import it.geosolutions.android.map.wfs.geojson.GeoJson;
import it.geosolutions.geocollect.android.core.form.utils.FormUtils;
import it.geosolutions.geocollect.android.core.login.LoginActivity;
import it.geosolutions.geocollect.android.core.login.utils.LoginRequestInterceptor;
import it.geosolutions.geocollect.android.core.mission.MissionFeature;
import it.geosolutions.geocollect.android.core.mission.PendingMissionListActivity;
import it.geosolutions.geocollect.android.core.mission.utils.MissionUtils;
import it.geosolutions.geocollect.android.core.mission.utils.PersistenceUtils;
import it.geosolutions.geocollect.android.core.mission.utils.SQLiteCascadeFeatureLoader;
import it.geosolutions.geocollect.android.core.mission.utils.SpatialiteUtils;
import it.geosolutions.geocollect.android.core.widgets.dialog.UploadDialog;
import it.geosolutions.geocollect.model.config.MissionTemplate;
import it.geosolutions.geocollect.model.http.CommitResponse;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import jsqlite.Database;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.test.ActivityUnitTestCase;
import android.util.Log;
import eu.geopaparazzi.library.util.ResourcesManager;

public class UploadTest extends ActivityUnitTestCase<PendingMissionListActivity> {
	
	static String TAG = "UploadTest";
	
	public UploadTest(){
		super(PendingMissionListActivity.class);
	}
	public UploadTest(Class<PendingMissionListActivity> activityClass) {
		super(activityClass);
	}
	
	
	/**
	 * Tests the upload function
	 */
	public void testUpload(){
		
		final Context targetContext = getInstrumentation().getTargetContext();
		
		//setup the activity
		Intent intent = new Intent(getInstrumentation().getTargetContext(),PendingMissionListActivity.class);
		startActivity(intent, null, null);
		
		//get template and db
		final MissionTemplate t = MissionUtils.getDefaultTemplate(targetContext);
		assertNotNull(t);
		
		final Database db = getSpatialiteDatabase(targetContext);
		assertNotNull(db);
		
		String tableName = t.schema_seg.localSourceStore; 

		// Setup the preloader
		int page = 0;
		int pagesize = 100;
		
		WFSGeoJsonFeatureLoader wfsl = new WFSGeoJsonFeatureLoader(
				targetContext,
				"http://demo.geo-solutions.it/share/comunege/geocollect/punti_abbandono.geojson",
				null, // baseparams
				"geosolutions:punti_abbandono",
				page*pagesize+1,
				pagesize);
		
		assertNotNull(wfsl);
		
		// loader
		SQLiteCascadeFeatureLoader loader = new SQLiteCascadeFeatureLoader(targetContext, wfsl, db, tableName);
		
		// load
		List<MissionFeature> results = loader.loadInBackground();
		
		// Check result
		assertTrue(results.size() > 80);
		
		//select one
		Random r = new Random();
		int random = r.nextInt(results.size() - 1);
		
		MissionFeature mf = results.get(random);
		assertNotNull(mf);
		
		//add to uploadables
		
		HashMap<String,ArrayList<String>> uploadables = PersistenceUtils.loadUploadables(targetContext);
		
		if(uploadables.containsKey(tableName)){
			
			//list exists, add this entry
			uploadables.get(tableName).add(mf.id);
			
		}else{
			
			ArrayList<String> ids = new ArrayList<String>();
			ids.add(mf.id);
			
			uploadables.put(tableName, ids);
		}
		
		PersistenceUtils.saveUploadables(targetContext, uploadables);
				
		//upload this one
		ArrayList<MissionFeature> uploads = new ArrayList<>();
		uploads.add(mf);
		
		//check the activity context
		assertNotNull(getActivity());
		assertTrue(getActivity() instanceof PendingMissionListActivity);
		
		android.support.v4.app.FragmentManager fm = getActivity().getSupportFragmentManager();
		Fragment mTaskFragment = fm.findFragmentByTag("FRAGMENT_UPLOAD_DIALOG");
		if(mTaskFragment==null){
			FragmentTransaction ft = fm.beginTransaction();

			mTaskFragment = new UploadDialog(){
				@Override
				public void onFinish(Activity ctx, CommitResponse result) {
					
					Log.i(TAG, "uploadtest result " + Boolean.toString(result.isSuccess()));
					
					assertTrue(result.isSuccess());			
				}
			};
			
			final String url = t.sop_form.url;
			final String mediaUrl = t.sop_form.mediaurl;

			// fill up the args for the upload dialog
			Bundle arguments = new Bundle();
			arguments.putString(UploadDialog.PARAMS.DATAURL, url);
			arguments.putString(UploadDialog.PARAMS.MEDIAURL, mediaUrl);
			arguments.putString(UploadDialog.PARAMS.TABLENAME, tableName);

			//parse the max imagesize
			int defaultImageSize = 1000;
			try{
				defaultImageSize = Integer.parseInt((String) t.config.get("maxImageSize"));	
			}catch( NumberFormatException e ){
				Log.e(UploadDialog.class.getSimpleName(), e.getClass().getSimpleName(),e);
			}catch( NullPointerException e){
				Log.e(UploadDialog.class.getSimpleName(), e.getClass().getSimpleName(),e);
			}
			
			HashMap<String,String> id_json_map = new HashMap<>();					
			HashMap<String,String[]> id_mediaurls_map = new HashMap<>();
			
			//create entries <featureID,  String   data      > for each missionfeature
			//create entries <featureID , String[] uploadUrls> for each missionfeature
			for(MissionFeature missionFeature : uploads){
				

				String featureIDString = MissionUtils.getFeatureGCID(missionFeature);
				
				// Set the "MY_ORIG_ID" to link this feature to its photos
				if(missionFeature.properties == null){
					missionFeature.properties = new HashMap<String, Object>();
				}
				missionFeature.properties.put("MY_ORIG_ID", featureIDString);

				GeoJson gson = new GeoJson();
				String c = gson.toJson( missionFeature);
				String data = null;
				try {
					data = new String(c.getBytes("UTF-8"));
				} catch (UnsupportedEncodingException e) {
					Log.e(UploadDialog.class.getSimpleName(), "error transforming missionfeature to gson",e);
				}
				id_json_map.put(featureIDString, data);

				//photos
				FormUtils.resizeFotosToMax(targetContext, featureIDString, defaultImageSize);
				String[] urls = FormUtils.getPhotoUriStrings(targetContext,featureIDString);						
				id_mediaurls_map.put(featureIDString, urls);

			}
			//add the populated maps
			arguments.putSerializable(UploadDialog.PARAMS.MISSIONS, id_json_map);
			arguments.putSerializable(UploadDialog.PARAMS.MISSION_MEDIA_URLS, id_mediaurls_map);
			
			/*
			 *  TODO: Change this line into 
			 *  arguments.putString(UploadDialog.PARAMS.MISSION_ID, <mission_id_here>);
			 *  when the MapStore GetFeatureInfoMenu will handle parametric missions id
			 */
			arguments.putString(UploadDialog.PARAMS.MISSION_ID, "punti_abbandono");
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(targetContext);
			
			String email = prefs.getString(LoginActivity.PREFS_USER_EMAIL, null);
			String pass = prefs.getString(LoginActivity.PREFS_PASSWORD, null);
						
			arguments.putString(UploadDialog.PARAMS.BASIC_AUTH, LoginRequestInterceptor.getB64Auth(email, pass));
			
			mTaskFragment.setArguments(arguments);
			
			((DialogFragment)mTaskFragment).setCancelable(false);
		    ft.add(mTaskFragment, "FRAGMENT_UPLOAD_DIALOG");
			ft.commit();
			
		}
		
	}
	public Database getSpatialiteDatabase(Context context){
		
		Database spatialiteDatabase = null;
		
		  try {
	            
	            File sdcardDir = ResourcesManager.getInstance(context).getSdcardDir();
	            File spatialDbFile = new File(sdcardDir, "geocollect/genova.sqlite");

	            if (!spatialDbFile.getParentFile().exists()) {
	                throw new RuntimeException();
	            }
	            spatialiteDatabase = new jsqlite.Database();
	            spatialiteDatabase.open(spatialDbFile.getAbsolutePath(), jsqlite.Constants.SQLITE_OPEN_READWRITE
	                    | jsqlite.Constants.SQLITE_OPEN_CREATE);
	            
	            Log.v("MISSION_DETAIL", SpatialiteUtils.queryVersions(spatialiteDatabase));
	            Log.v("MISSION_DETAIL", spatialiteDatabase.dbversion());

	            return spatialiteDatabase;

	        } catch (Exception e) {
	            Log.v("MISSION_DETAIL", Log.getStackTraceString(e));
	            return null;
	        }
		
	}

}
