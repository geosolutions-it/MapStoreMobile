package it.geosolutions.geocollect.android.core.test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import jsqlite.Database;

import it.geosolutions.android.map.dto.MarkerDTO;
import it.geosolutions.geocollect.android.core.mission.Mission;
import it.geosolutions.geocollect.android.core.mission.utils.PersistenceUtils;
import it.geosolutions.geocollect.android.core.mission.utils.SpatialiteUtils;
import it.geosolutions.geocollect.model.config.MissionTemplate;
import it.geosolutions.geocollect.model.source.XDataType;
import it.geosolutions.geocollect.model.viewmodel.Field;
import it.geosolutions.geocollect.model.viewmodel.Form;
import it.geosolutions.geocollect.model.viewmodel.Page;

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
public class LocalStorageTest extends android.test.AndroidTestCase {

	static String TAG = "LocalStorageTest";
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

	/**
	 * Load must fail if the database reference is valid but the database itself is closed
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
            
            if(SpatialiteUtils.checkOrCreateTable(spatialiteDatabase, "punti_accumulo_data")){
	            Log.v("MISSION_DETAIL", "Table Found");
            }else{
	            Log.w("MISSION_DETAIL", "Table could not be created, edits will not be saved");
            }
            
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

	/**
	 * Load must fail on a NULL database reference
	 */
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

	/// Metodo per l'estrazione della lista TIPATA dei campi del template ( "_data" )
	
	public void testGetTemplateFieldList(){
		
		MissionTemplate mt = new MissionTemplate();
		mt.form = new Form();
		mt.form.pages = new ArrayList<Page>();
		Page p = new Page();
		p.fields = new ArrayList<Field>();
		
		// Add date field
		Field f = new Field();
		f.type = XDataType.date;
		f.fieldId = "Data";
		p.fields.add(f);
		
		// Add decimal field
		f = new Field();
		f.type = XDataType.decimal;
		f.fieldId = "Decimalfield";
		p.fields.add(f);
		
		// Add decimal field
		f = new Field();
		f.type = XDataType.integer;
		f.fieldId = "IntField";
		p.fields.add(f);
	
		mt.form.pages.add(p);
		
		// Another page
		p = new Page();
		p.fields = new ArrayList<Field>();
		
		// Add decimal field
		f = new Field();
		f.type = XDataType.string;
		f.fieldId = "TextField";
		p.fields.add(f);
		
		mt.form.pages.add(p);

		HashMap<String,XDataType> templateDataTypes = PersistenceUtils.getTemplateFieldsList(mt);
		
		assertNotNull(templateDataTypes);
		
		assertTrue(templateDataTypes.get("Data") == XDataType.date);
		assertTrue(templateDataTypes.get("Decimalfield") == XDataType.decimal);
		assertTrue(templateDataTypes.get("IntField") == XDataType.integer);
		assertTrue(templateDataTypes.get("TextField") == XDataType.string);
		
		
	}
	
	
	public void testGetDataTypesFromTemplate(){
		
		
		
		
	}
	/// Metodo per il confronto dei campi esistenti nel DB con una lista di campi (vedi metodo sopra)
	
	/// Metodo per la creazione della tabella a partire da una lista di campi TIPATA
 	
	/// Creazione della tabella "ORIGIN" con ("PK_UID", "ID", fields, "Geometry")
	
	/// Creazione della tabella "TEMPLATE" con ("PK_UID", "ORIGIN_ID", fields, "Geometry")
	
		
	/// 
	
}
