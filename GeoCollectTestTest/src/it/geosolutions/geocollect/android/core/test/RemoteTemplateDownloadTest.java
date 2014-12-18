package it.geosolutions.geocollect.android.core.test;

import it.geosolutions.android.map.wfs.WFSGeoJsonFeatureLoader;
import it.geosolutions.geocollect.android.core.login.LoginActivity;
import it.geosolutions.geocollect.android.core.login.utils.NetworkUtil;
import it.geosolutions.geocollect.android.core.mission.MissionFeature;
import it.geosolutions.geocollect.android.core.mission.PendingMissionListActivity;
import it.geosolutions.geocollect.android.core.mission.utils.PersistenceUtils;
import it.geosolutions.geocollect.android.core.mission.utils.SQLiteCascadeFeatureLoader;
import it.geosolutions.geocollect.android.core.mission.utils.SpatialiteUtils;
import it.geosolutions.geocollect.android.template.TemplateDownloadTask;
import it.geosolutions.geocollect.model.config.MissionTemplate;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jsqlite.Database;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import eu.geopaparazzi.library.util.ResourcesManager;

public class RemoteTemplateDownloadTest extends android.test.AndroidTestCase {
	
	final static String TAG = RemoteTemplateDownloadTest.class.getSimpleName();
	
	public void testDownload(){
		
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		
		final String authKey = prefs.getString(LoginActivity.PREFS_AUTH_KEY, null);
		
		if(authKey != null && NetworkUtil.isOnline(getContext())){
			
			final TemplateDownloadTask task = new TemplateDownloadTask(){
				@Override
				public void complete(final ArrayList<MissionTemplate> downloadedTemplates){
					
					assertTrue(downloadedTemplates != null);
					
					//1insert into databse
					ArrayList<MissionTemplate> validTemplates = new ArrayList<MissionTemplate>();
					if(downloadedTemplates != null && downloadedTemplates.size() > 0){
						
						Database db = getSpatialiteDatabase();
						
						for(MissionTemplate t : downloadedTemplates){
							if(!PersistenceUtils.createOrUpdateTablesForTemplate(t, db)){
								Log.e(PendingMissionListActivity.class.getSimpleName(), "error creating/updating tables for "+ t.nameField);
							}else{
								//if insert succesfull add to list of valid templates
								validTemplates.add(t);
							}
						}
						
						//2. save valid templates 
						PersistenceUtils.saveDownloadedTemplates(getContext(), validTemplates);
						
						ArrayList<MissionTemplate> savedTemplates = PersistenceUtils.loadSavedTemplates(getContext());
						
						assertNotNull(savedTemplates);
						
						assertEquals(validTemplates.size(), savedTemplates.size());
						
						try {
							db.close();
						} catch (jsqlite.Exception e) {
							// ignore
						}
					}
				}
			};
			
			task.execute(authKey);
		}else if(authKey ==  null){
			Log.e(TAG, "no authKey, cannot test template download");
		}else {
			Log.e(TAG, "not online, cannot test template download");			
		}
		
	}
	
	public void testLoader(){
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		String username = prefs.getString(LoginActivity.PREFS_USER_EMAIL, null);
		String password = prefs.getString(LoginActivity.PREFS_PASSWORD, null);
		
		int page = 0;
		int pagesize = 100;
		
		ArrayList<MissionTemplate> savedTemplates = PersistenceUtils.loadSavedTemplates(getContext());
		
		MissionTemplate t = savedTemplates.get(0);
		
		Database db = getSpatialiteDatabase();
		
		String tableName = t.schema_seg.localSourceStore;
		
		WFSGeoJsonFeatureLoader wfsl = new WFSGeoJsonFeatureLoader(
				getContext(),
				t.schema_seg.URL,
				t.schema_seg.baseParams,
				t.schema_seg.typeName,
				page*pagesize+1,
				pagesize,
				username,
				password);
		
		// Actual loader to test
		SQLiteCascadeFeatureLoader loaderToTest = new SQLiteCascadeFeatureLoader(getContext(), wfsl, db, tableName);
		
		// Start the test
		List<MissionFeature> results = loaderToTest.loadInBackground();
		
		//this is a quite specific test for the from90 test template, adjust in future 
		assertEquals(results.size(), 4);
		
		if(savedTemplates.size() >= 2){
			
			MissionTemplate t2 = savedTemplates.get(1);
			
			String tableName2 = t2.schema_seg.localSourceStore;
			
			WFSGeoJsonFeatureLoader wfsl2 = new WFSGeoJsonFeatureLoader(
					getContext(),
					t2.schema_seg.URL,
					t2.schema_seg.baseParams,
					t2.schema_seg.typeName,
					page*pagesize+1,
					pagesize,
					username,
					password);
			
			// Actual loader to test
			SQLiteCascadeFeatureLoader loaderToTest2 = new SQLiteCascadeFeatureLoader(getContext(), wfsl2, db, tableName2);
			
			// Start the test
			List<MissionFeature> results2 = loaderToTest2.loadInBackground();
			
			//this is a quite specific test for the until5 template, adjust in future 
			assertEquals(results2.size(), 5);
			
		}
		
		try {
			db.close();
		} catch (jsqlite.Exception e) {
			// ignore
		}
	}
	
	
	public Database getSpatialiteDatabase(){
		
		Database spatialiteDatabase = null;
		
		  try {
	            
	            File sdcardDir = ResourcesManager.getInstance(getContext()).getSdcardDir();
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
