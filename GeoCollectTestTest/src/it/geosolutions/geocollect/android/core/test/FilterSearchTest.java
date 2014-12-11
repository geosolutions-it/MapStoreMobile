package it.geosolutions.geocollect.android.core.test;

import it.geosolutions.android.map.wfs.WFSGeoJsonFeatureLoader;
import it.geosolutions.geocollect.android.core.mission.FeatureAdapter;
import it.geosolutions.geocollect.android.core.mission.MissionFeature;
import it.geosolutions.geocollect.android.core.mission.utils.MissionUtils;
import it.geosolutions.geocollect.android.core.mission.utils.SQLiteCascadeFeatureLoader;
import it.geosolutions.geocollect.android.core.mission.utils.SpatialiteUtils;
import it.geosolutions.geocollect.model.config.MissionTemplate;

import java.io.File;
import java.util.List;

import jsqlite.Database;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import eu.geopaparazzi.library.util.ResourcesManager;

public class FilterSearchTest extends android.test.AndroidTestCase{

	static String TAG = "FilterSearchTest";
	
	protected void setUp() throws Exception {
		super.setUp();
		Log.v(TAG, "setUp()");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		Log.v(TAG, "tearDown()");
	}
	
	public void testFilterSearchFunctionality(){
		
		assertTrue(true);
		
		final MissionTemplate t = MissionUtils.getDefaultTemplate(getContext());
		
		final FeatureAdapter df = new FeatureAdapter(getContext(),R.layout.mission_resource_row,t);
		
		int page = 0;
		int pagesize = 100;
		
		final Database db = getSpatialiteDatabase();
		
		String tableName = t.source.localSourceStore; //"testTable"
		
		WFSGeoJsonFeatureLoader wfsl = new WFSGeoJsonFeatureLoader(
				getContext(),
				"http://demo.geo-solutions.it/share/comunege/geocollect/punti_abbandono.geojson",
				null, // baseparams
				"geosolutions:punti_abbandono",
				page*pagesize+1,
				pagesize);
		
		// Actual loader to test
		SQLiteCascadeFeatureLoader loaderToTest = new SQLiteCascadeFeatureLoader(getContext(), wfsl, db, tableName);
		
		// Start the test
		List<MissionFeature> results = loaderToTest.loadInBackground();
		
		Log.d(TAG, "found "+results.size()+" missionfeatures");
		
		final int absoluteCount = results.size();

		if(Build.VERSION.SDK_INT > 10){
			df.addAll(results);
		}else{        		  
			for(MissionFeature f :results){
				df.add(f);
			}
		}

		final String toFilter = "materassi";
		
		//this filters asynchronously
		df.getFilter().filter(toFilter);
		
		// --> wait 500 ms for the result
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
								
				Log.d(TAG, "filtered : "+df.getCount()+" missionfeatures");
				
				assertTrue(df.getCount() < absoluteCount);
			}
		}, 500);
		
		

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
