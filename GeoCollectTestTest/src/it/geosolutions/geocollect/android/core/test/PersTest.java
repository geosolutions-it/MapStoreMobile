package it.geosolutions.geocollect.android.core.test;

import java.io.File;

import jsqlite.Database;

import it.geosolutions.android.map.dto.MarkerDTO;
import it.geosolutions.geocollect.android.core.mission.Mission;
import it.geosolutions.geocollect.android.core.mission.utils.PersistenceUtils;
import it.geosolutions.geocollect.android.core.mission.utils.SpatialiteUtils;
import it.geosolutions.geocollect.model.viewmodel.Page;

import org.mapsforge.android.maps.overlay.Marker;

import eu.geopaparazzi.library.util.ResourcesManager;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.LinearLayout;

/**
 * Test Class for PersistenceUtils Class
 * 
 * @author Lorenzo
 *
 */
public class PersTest extends android.test.AndroidTestCase {

	static String TAG = "PersistenceTest";
	protected void setUp() throws Exception {
		super.setUp();
		Log.v(TAG, "setUp()");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		Log.v(TAG, "tearDown()");
	}

	
	public void testPersistenceUtils() {
		Log.v(TAG, "YAY! I'm running!");
		assertTrue(true);
		
	}

	public void testMarkerColorCreation() {
		
		Drawable drawable=null;
        try{
            drawable = getContext().getResources().getDrawable(MarkerDTO.MARKER_BLUE);
            Log.v("MARKER","marker found!");
            
        }catch(Exception e){
            Log.w("MARKER","unable to find type of marker");
            drawable = getContext().getResources().getDrawable(MarkerDTO.MARKER_RED);
        }
        
        assertNotNull(drawable);
	}
/*
	public void testStorePageDataPageLinearLayoutMissionString() {
		Log.v(TAG, "YAY! I'm running!");
	}
*/
	public void testLoadPageDataPageLinearLayoutMissionContext() {
		
		// Mock Page
		Page p = new Page();
		// Mock LinearLayout
		LinearLayout l = new LinearLayout(getContext());
		// Mock Mission
		Mission m = new Mission();
		Database spatialiteDatabase;
		
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
            
            m.db = spatialiteDatabase;

        } catch (Exception e) {
            Log.v("MISSION_DETAIL", Log.getStackTraceString(e));
            fail("Cannot open database");
        }
        
        assertNotNull(m.db);
        assertFalse(m.db.dbversion().equals("unknown"));
        try {
			m.db.close();
		} catch (jsqlite.Exception e) {
			// ignore
		}
        assertTrue(m.db.dbversion().equals("unknown"));
        assertFalse(PersistenceUtils.loadPageData(p, l, m, getContext()));
	}

	public void testNullDatabase() {
		
		// Mock Page
		Page p = new Page();
		// Mock LinearLayout
		LinearLayout l = new LinearLayout(getContext());
		// Mock Mission
		Mission m = new Mission();
		
		assertNull(m.db);
        assertFalse(PersistenceUtils.loadPageData(p, l, m, getContext()));
	}

	public void testLoadPageDataPageLinearLayoutMissionContextString() {
		Log.v(TAG, "YAY! I'm running!");
		fail("Not yet implemented");
	}

}
