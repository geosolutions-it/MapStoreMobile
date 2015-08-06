package it.geosolutions.geocollect.android.core.test;

import it.geosolutions.geocollect.android.core.mission.MissionFeature;
import it.geosolutions.geocollect.android.core.mission.PendingMissionListActivity;
import it.geosolutions.geocollect.android.core.mission.utils.MissionUtils;
import it.geosolutions.geocollect.android.core.mission.utils.PersistenceUtils;
import it.geosolutions.geocollect.android.core.mission.utils.SpatialiteUtils;
import it.geosolutions.geocollect.android.core.mission.utils.UploadTask;
import it.geosolutions.geocollect.model.config.MissionTemplate;
import it.geosolutions.geocollect.model.http.CommitResponse;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import jsqlite.Database;
import android.content.Context;
import android.content.Intent;
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
	 * @throws InterruptedException 
	 */
	public void testUpload() throws InterruptedException{
		
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

		// load
		ArrayList<MissionFeature> results = MissionUtils.getMissionFeatures(tableName, db);
		
		// Check result
		assertTrue(results.size() > 0);
		
		//select one
		Random r = new Random();
		int random = r.nextInt(results.size() - 1);
		
		MissionFeature mf = results.get(random);
		assertNotNull(mf);
		
		Log.d(TAG, "uploading "+ MissionUtils.getFeatureGCID(mf));
		
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
	
		 new UploadTask(
		        targetContext,
				t,
				uploads){
			
			@Override
			public void hideMedia() {}
			
			@Override
			public void dataDone() {}

			@Override
			public void done(CommitResponse result) {
				Log.d(TAG, "upload done "+ Boolean.toString(result.isSuccess()));
				assertTrue(result.isSuccess());
			}
			
			
		}.execute();
		
		//wait for the result
		Thread.sleep(5000);

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
