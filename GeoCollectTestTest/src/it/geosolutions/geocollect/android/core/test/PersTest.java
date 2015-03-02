package it.geosolutions.geocollect.android.core.test;

import it.geosolutions.android.map.dto.MarkerDTO;
import it.geosolutions.geocollect.android.core.mission.Mission;
import it.geosolutions.geocollect.android.core.mission.MissionFeature;
import it.geosolutions.geocollect.android.core.mission.utils.MissionUtils;
import it.geosolutions.geocollect.android.core.mission.utils.PersistenceUtils;
import it.geosolutions.geocollect.android.core.mission.utils.SpatialiteUtils;
import it.geosolutions.geocollect.model.config.MissionTemplate;
import it.geosolutions.geocollect.model.viewmodel.Field;
import it.geosolutions.geocollect.model.viewmodel.Page;
import it.geosolutions.geocollect.model.viewmodel.type.XType;

import java.io.File;
import java.util.ArrayList;

import jsqlite.Database;

import org.mapsforge.core.model.GeoPoint;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.LinearLayout;
import eu.geopaparazzi.library.util.ResourcesManager;

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
		
		Database spatialiteDatabase = getSpatialiteDatabase();
		
		m.db = spatialiteDatabase;
        
        assertNotNull(m.db);
        assertFalse(m.db.dbversion().equals("unknown"));
        try {
			m.db.close();
		} catch (jsqlite.Exception e) {
			// ignore
		}
        assertTrue(m.db.dbversion().equals("unknown"));
        assertFalse(PersistenceUtils.loadPageData(p, l, m, getContext(),false));
	}

	public void testNullDatabase() {
		
		// Mock Page
		Page p = new Page();
		// Mock LinearLayout
		LinearLayout l = new LinearLayout(getContext());
		// Mock Mission
		Mission m = new Mission();
		
		assertNull(m.db);
        assertFalse(PersistenceUtils.loadPageData(p, l, m, getContext(),false));
	}

	public void testLoadPageDataPageLinearLayoutMissionContextString() {
		Log.v(TAG, "YAY! I'm running!");
		fail("Not yet implemented");
	}
	
	
	public void testTableCreation(){
		
		final MissionTemplate t = MissionUtils.getDefaultTemplate(getContext());
		
		final Database spatialiteDatabase = getSpatialiteDatabase();
		
		assertTrue(PersistenceUtils.createTableFromTemplate(spatialiteDatabase, t.schema_seg.localSourceStore+ "_new", t.schema_seg.fields));
		
		assertTrue(PersistenceUtils.createOrUpdateTable(spatialiteDatabase,t.schema_seg.localSourceStore, t.schema_seg.fields));

		assertTrue(PersistenceUtils.createOrUpdateTable(spatialiteDatabase,t.schema_sop.localFormStore, t.schema_sop.fields));

		try {
			spatialiteDatabase.close();
		} catch (jsqlite.Exception e) {
			// ignore
		}
	}
	

	public void testMissionFeatureCreation(){
		
	    final MissionTemplate t = MissionUtils.getDefaultTemplate(getContext());
		
		final Database spatialiteDatabase = getSpatialiteDatabase();
		
		final String tableName =  t.schema_seg.localSourceStore+ "_new";
		
		final Long id = PersistenceUtils.getIDforNewMissionFeatureEntry(spatialiteDatabase, tableName);
		
		final String id_ = Long.toString(id);
		
		assertNotNull(id);
		
		assertTrue(PersistenceUtils.insertCreatedMissionFeature(spatialiteDatabase, tableName, id));
		
		Field field = t.seg_form.pages.get(0).fields.get(1);
		
		assertNotNull(field);
		
		assertEquals("CODICE", field.fieldId);
		
		final String value = "Great Code";
				
		PersistenceUtils.updateCreatedMissionFeatureRow(spatialiteDatabase, tableName, field, value, id_);
		
		ArrayList<MissionFeature> features = MissionUtils.getCreatedMissionFeatures(tableName, spatialiteDatabase);
		
		MissionFeature inserted = null;
		
		for(MissionFeature f : features){
			if(f.id.equals(id_)){
				assertEquals(f.properties.get("CODICE"),value);
				inserted = f;
				break;
			}
		}
		
		PersistenceUtils.deleteMissionFeature(spatialiteDatabase, tableName, inserted.id);
		
		ArrayList<MissionFeature> newFeatures = MissionUtils.getCreatedMissionFeatures(tableName, spatialiteDatabase);
		
		assertFalse(newFeatures.contains(inserted));
		
		try {
			spatialiteDatabase.close();
		} catch (jsqlite.Exception e) {
			// ignore
		}
		
	}

	public void testMissionFeatureGeometry(){

		final MissionTemplate t = MissionUtils.getDefaultTemplate(getContext());

		final Database spatialiteDatabase = getSpatialiteDatabase();

		final String tableName =  t.schema_seg.localSourceStore+ "_new";

		final Long id = PersistenceUtils.getIDforNewMissionFeatureEntry(spatialiteDatabase, tableName);

		final String id_ = Long.toString(id);

		assertNotNull(id);

		assertTrue(PersistenceUtils.insertCreatedMissionFeature(spatialiteDatabase, tableName, id));

		final double lat = 43.4244351;
		final double lon = 11.53254524;

		final GeoPoint g = new GeoPoint(lat,lon);

		final String insertPoint = "MakePoint("+g.longitude+","+g.latitude+", 4326)";

		Field mapfield = t.seg_form.pages.get(5).fields.get(0);

		assertEquals(XType.mapViewPoint, mapfield.xtype);

		PersistenceUtils.updateCreatedMissionFeatureRow(spatialiteDatabase, tableName, mapfield, insertPoint, id_);

		double[] xy = PersistenceUtils.getXYCoord(spatialiteDatabase, tableName, id_);

		assertEquals(lat, xy[1]);
		assertEquals(lon, xy[0]);
		
		try {
			spatialiteDatabase.close();
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
