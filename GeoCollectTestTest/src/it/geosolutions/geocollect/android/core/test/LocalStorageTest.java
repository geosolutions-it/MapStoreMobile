package it.geosolutions.geocollect.android.core.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import com.google.gson.Gson;

import jsqlite.Database;
import jsqlite.Stmt;
import it.geosolutions.geocollect.android.core.mission.Mission;
import it.geosolutions.geocollect.android.core.mission.utils.PersistenceUtils;
import it.geosolutions.geocollect.android.core.mission.utils.SpatialiteUtils;
import it.geosolutions.geocollect.model.config.MissionTemplate;
import it.geosolutions.geocollect.model.source.SegSchema;
import it.geosolutions.geocollect.model.source.XDataType;
import it.geosolutions.geocollect.model.viewmodel.Field;
import it.geosolutions.geocollect.model.viewmodel.Form;
import it.geosolutions.geocollect.model.viewmodel.Page;
import android.util.Log;
import android.widget.LinearLayout;

/**
 * Test Class for PersistenceUtils Class
 * 
 * @author Lorenzo Pini
 *
 */
public class LocalStorageTest extends android.test.AndroidTestCase {

	static String TAG = "LocalStorageTest";
	
	Database db;
	
	protected void setUp() throws Exception {
		super.setUp();
		Log.v(TAG, "setUp()");
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		Log.v(TAG, "tearDown()");
		if(db != null){
			
			try {
				db.close();
			} catch (jsqlite.Exception e) {
	            Log.d(TAG, Log.getStackTraceString(e));
				// ignore
			}
			
		}
	}

	
	public void testPersistenceUtils() {
		Log.v(TAG, "YAY! I'm running!");
		assertTrue(true);
		
	}

	/**
	 * Load must fail if the database reference is valid but the database itself is closed
	 */
	public void testLoadPageDataPageLinearLayoutMissionContext() {
		Log.v(TAG, "testLoadPageDataPageLinearLayoutMissionContext()");
		
		// Mock Page
		Page p = new Page();
		// Mock LinearLayout
		LinearLayout l = new LinearLayout(getContext());
		// Mock Mission
		Mission m = new Mission();
		db =SpatialiteUtils.openSpatialiteDB(getContext(), "geocollect/test.sqlite");
		
        m.db = db;
        
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

	/**
	 * Load must fail on a NULL database reference
	 */
	public void testNullDatabase() {
		Log.v(TAG, "testNullDatabase()");
		
		// Mock Page
		Page p = new Page();
		// Mock LinearLayout
		LinearLayout l = new LinearLayout(getContext());
		// Mock Mission
		Mission m = new Mission();
		
		assertNull(m.db);
        assertFalse(PersistenceUtils.loadPageData(p, l, m, getContext(),false));
	}

	/// Metodo per l'estrazione della lista TIPATA dei campi del template ( "_data" )
	/**
	 * The getTemplateFieldsList method must cycle through all the template pages, listing the field types in them
	 */
	public void testGetTemplateFieldList(){
		Log.v(TAG, "testGetTemplateFieldList()");
		
		MissionTemplate mt = new MissionTemplate();
		mt.sop_form = new Form();
		mt.sop_form.pages = new ArrayList<Page>();
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
	
		mt.sop_form.pages.add(p);
		
		// Another page
		p = new Page();
		p.fields = new ArrayList<Field>();
		
		// Add decimal field
		f = new Field();
		f.type = XDataType.string;
		f.fieldId = "TextField";
		p.fields.add(f);
		
		mt.sop_form.pages.add(p);

		HashMap<String,XDataType> templateDataTypes = PersistenceUtils.getTemplateFieldsList(mt.sop_form);
		
		assertNotNull(templateDataTypes);
		
		assertTrue(templateDataTypes.get("Data") == XDataType.date);
		assertTrue(templateDataTypes.get("Decimalfield") == XDataType.decimal);
		assertTrue(templateDataTypes.get("IntField") == XDataType.integer);
		assertTrue(templateDataTypes.get("TextField") == XDataType.string);
		
		
	}
	
	/**
	 * The getDataTypesFromTemplate() method must return the MissionTemplate -> Source -> DataTypes HashMap
	 */
	// Is this test helpful?
	public void testGetDataTypesFromTemplate(){
		Log.v(TAG, "testGetDataTypesFromTemplate()");
		
		MissionTemplate mt = new MissionTemplate();
		mt.schema_seg= new SegSchema();
		//mt.source.dataTypes;
		db = SpatialiteUtils.openSpatialiteDB(getContext(), "geocollect/genova.sqlite");
		
	}
	
	
	/// Metodo per il confronto dei campi esistenti nel DB con una lista di campi (vedi metodo sopra)
	/**
	 * Must compare the given datatypes with the ones in the database
	 * If the database schema differs from the given datatypes: 
	 * - missing field must be created
	 * - additional field must be removed
	 */
	public void testAlignFieldsOnDatabase(){
		
		db = SpatialiteUtils.openSpatialiteDB(getContext(), "geocollect/test.sqlite");
		
		assertNotNull(db);
		Gson gson = new Gson();
		String template1 = "{	" +
				"	\"id\":\"punti_accumulo\"," +
				"	\"title\": \"Punti Abbandono\"," +
				"	\"schema_seg\":{" +
				"		\"type\":\"WFS\"," +
				"		\"URL\":\"http://demo.geo-solutions.it/share/comunege/geocollect/punti_abbandono.geojson\"," +
				"		\"typeName\":\"geosolutions:punti_abbandono\"," +
				"		\"fields\":{" +
				"			\"CODICE\":\"string\"," +
				"			\"DATA_RILEV\":\"string\"," +
				"			\"USO_AGRICO\":\"integer\"," +
				"			\"USO_PARCHE\":\"integer\"," +
				"			\"USO_COMMER\":\"integer\"," +
				"			\"AREA_PRIVA\":\"string\"," +
				"			\"AREA_PUBBL\":\"string\"," +
				"			\"ALTRE_CARA\":\"integer\"," +
				"			\"DISTANZA_U\":\"integer\"," +
				"			\"DIMENSIONI\":\"string\"," +
				"			\"RIFIUTI_NO\":\"string\"," +
				"			\"RIFIUTI_PE\":\"string\"," +
				"			\"QUANTITA_R\":\"integer\"," +
				"			\"STATO_FISI\":\"string\"," +
				"			\"ODORE\":\"string\"," +
				"			\"MODALITA_S\":\"string\"," +
				"			\"PERCOLATO\":\"string\"," +
				"			\"VEGETAZION\":\"string\"," +
				"			\"STABILITA\":\"integer\"," +
				"			\"INSEDIAMEN\":\"string\"," +
				"			\"AGRICOLO\":\"integer\"," +
				"			\"AGRICOLO_A\":\"string\"," +
				"			\"ID\":\"integer\"," +
				"			\"ID1\":\"integer\"," +
				"			\"VALORE_SOC\":\"integer\"," +
				"			\"GMROTATION\":\"real\"" +
				"		}" +
				"	}" +
				"}";
		
		String template2 = "{	" +
				"	\"id\":\"punti_accumulo\"," +
				"	\"title\": \"Punti Abbandono\"," +
				"	\"schema_seg\":{" +
				"		\"type\":\"WFS\"," +
				"		\"URL\":\"http://demo.geo-solutions.it/share/comunege/geocollect/punti_abbandono.geojson\"," +
				"		\"typeName\":\"geosolutions:punti_abbandono\"," +
				"		\"fields\":{" +
				"			\"CODICE\":\"string\"," +
				"			\"DATA_RILEV\":\"string\"," +
				"			\"USO_AGRICO\":\"integer\"," +
				"			\"USO_PARCHE\":\"integer\"," +
				"			\"USO_COMMER\":\"integer\"," +
				"			\"AREA_PRIVA\":\"string\"," +
				"			\"AREA_PUBBL\":\"string\"," +
				"			\"ALTRE_CARA\":\"integer\"," +
				"			\"DISTANZA_U\":\"integer\"," +
				"			\"DIMENSIONI\":\"string\"," +
				"			\"RIFIUTI_NO\":\"string\"," +
				"			\"RIFIUTI_PE\":\"string\"," +
							// removed QUANTITA_R
				"			\"STATO_FISI\":\"string\"," +
				"			\"ODORE\":\"string\"," +
				"			\"MODALITA_S\":\"string\"," +
				"			\"PERCOLATO\":\"string\"," +
				"			\"VEGETAZ\":\"string\"," +   // Renamed, must be dropped and recreated
				"			\"STABILITA\":\"integer\"," +
				"			\"INSEDIAMEN\":\"string\"," +
				"			\"AGRICOLO\":\"integer\"," +
				"			\"AGRICOLO_A\":\"string\"," +
				"			\"ID\":\"integer\"," +
				"			\"ID1\":\"integer\"," +
				"			\"VALORE_SOC\":\"text\"," + // Changed type, must be ignored and keep integer
				"			\"GMROTATION\":\"real\"" +
				"		}" +
				"	}" +
				"}";
		
		MissionTemplate mt1 = gson.fromJson( template1 , MissionTemplate.class);
		MissionTemplate mt2 = gson.fromJson( template2 , MissionTemplate.class);
		
		HashMap<String,XDataType> templateDataTypes1 = mt1.schema_seg.fields;
		HashMap<String,XDataType> templateDataTypes2 = mt2.schema_seg.fields;

		
		assertNotNull(templateDataTypes1);
		assertNotNull(templateDataTypes2);
		assertTrue(templateDataTypes1.size()>0);
		assertTrue(templateDataTypes2.size()>0);
		
		// TODO: write validator for these templateDataTypes
		// fieldNames must not clash with PK_UID, ORIGIN_ID, GEOMETRY
		try {
			db.prepare("DROP TABLE IF EXISTS test_table;").step();
		} catch (jsqlite.Exception e) {
			fail(e.getLocalizedMessage());
		}
		// Create the table as usual
		assertTrue(PersistenceUtils.createTableFromTemplate(db, "test_table", templateDataTypes1));
		
		// Method to test (EnableColumnDrop is FALSE)
		assertTrue(PersistenceUtils.updateTableFromTemplate(db, "test_table", templateDataTypes2));
		
		
		// Check results
        String query = "SELECT name FROM sqlite_master WHERE type='table' AND name='test_table'";

        try {
            Stmt stmt = db.prepare(query);
            if( stmt.step() ) {
                String nomeStr = stmt.column_string(0);
                assertTrue("Retrieved table name is incorrect", nomeStr.equalsIgnoreCase("test_table"));
            }else{
            	fail("Table not found");
            }
            stmt.close();
        } catch (Exception e) {
            fail( Log.getStackTraceString(e));
        }

        // check results
        query = "PRAGMA table_info('test_table');";
        int nameColumn = -1;
        int typeColumn = -1;
        String columnName, typeName;

        int count_columns = 0;
        int count_VEGETAZION = 0;
        try {
            Stmt stmt = db.prepare(query);
            while( stmt.step() ) {
                if(nameColumn<0 || typeColumn<0){
                	// I have to retrieve the position of the metadata fields
                	for(int i = 0; i<stmt.column_count(); i++){
                		Log.v(TAG, stmt.column_name(i));
                		if(stmt.column_name(i).equalsIgnoreCase("name")){
                			nameColumn = i;
                		}
                		if(stmt.column_name(i).equalsIgnoreCase("type")){
                			typeColumn = i;
                		}
                	}
                	
                }
            	assertTrue(nameColumn>=0);
            	assertTrue(typeColumn>=0);
            	
            	columnName = stmt.column_string(nameColumn);
            	typeName = stmt.column_string(typeColumn);
            	
            	// output values
            	Log.v(TAG, count_columns+" : "+columnName+" : "+typeName);
        		
            	if(!columnName.equals("ORIGIN_ID") && !columnName.equals("GEOMETRY")){
            		
            		
            		// If EnableColumnDrop, this fields should have been removed
            		// EnableColumnDrop is disabled in this test
            		//assertFalse(columnName.equalsIgnoreCase("QUANTITA_R"));
            		//assertFalse(columnName.equalsIgnoreCase("VEGETAZION"));
            		
            		// This field should be added (only once)
            		if(columnName.equals("VEGETAZ")){
            			count_VEGETAZION++;
            		}
            		
            		if(columnName.equals("VALORE_SOC")){
            			// this field should not be changed
            			assertTrue(typeName.equalsIgnoreCase(SpatialiteUtils.getSQLiteTypeFromString("integer")));
            		}else{
            			// If EnableDropColumn is TRUE , we must assertNotNull templateDataTypes2.get(columnName)
            			// In this test EnableDropColumn is false (by default)
            			if(templateDataTypes2.get(columnName)!=null){
            				assertTrue(typeName.equalsIgnoreCase(SpatialiteUtils.getSQLiteTypeFromString(templateDataTypes2.get(columnName).toString())));
            			}
            		}
            	}
            	count_columns++;
            }
            stmt.close();
        } catch (Exception e) {
            fail( Log.getStackTraceString(e));
        }
        
        // Check if the additional column was created
        assertEquals(1, count_VEGETAZION);
        
        // output values
        int j = 0;
        for(Entry<String, XDataType> e : templateDataTypes2.entrySet()){
        	Log.v(TAG, j++ +" : "+e.getKey()+" : "+e.getValue());
        }
        
        // Add 2 because the default CREATE statement creates "ORIGIN_ID" and "GEOMETRY" columns
        // all the other columns comes from the templateDataTypes
        // Add 2 again, because in this test there are 2 old columns that are not removed on purpose
        assertEquals(count_columns, templateDataTypes2.size()+2+2);
	}
	
	/**
	 * Given datatypes , tableName and a database, must create a table with the corresponding datatypes and tableName in the database
	 * if table already exists, do nothing.
	 */
	public void testCreateTableOnDatabase(){
		
		db = SpatialiteUtils.openSpatialiteDB(getContext(), "geocollect/test.sqlite");
		
		assertNotNull(db);
		Gson gson = new Gson();
		MissionTemplate mt = gson.fromJson(
"{	" +
"	\"id\":\"punti_accumulo\"," +
"	\"title\": \"Punti Abbandono\"," +
"	\"schema_seg\":{" +
"		\"type\":\"WFS\"," +
"		\"URL\":\"http://demo.geo-solutions.it/share/comunege/geocollect/punti_abbandono.geojson\"," +
"		\"typeName\":\"geosolutions:punti_abbandono\"," +
"		\"storeLocally\":\"localTable\"," +
"		\"fields\":{" +
"			\"CODICE\":\"string\"," +
"			\"DATA_RILEV\":\"string\"," +
"			\"MACROAREA\":\"string\"," +
"			\"MICROAREA\":\"string\"," +
"			\"CIRCOSCRIZ\":\"string\"," +
"			\"MORFOLOGIA\":\"string\"," +
"			\"INCLINAZIO\":\"string\"," +
"			\"MORFOLOGI1\":\"string\"," +
"			\"COPERTURA_\":\"string\"," +
"			\"COPERTURA1\":\"string\"," +
"			\"USO_AGRICO\":\"integer\"," +
"			\"USO_PARCHE\":\"integer\"," +
"			\"USO_COMMER\":\"integer\"," +
"			\"USO_STRADA\":\"integer\"," +
"			\"USO_ABBAND\":\"string\"," +
"			\"PRESUNZION\":\"string\"," +
"			\"AREA_PRIVA\":\"string\"," +
"			\"AREA_PUBBL\":\"string\"," +
"			\"ALTRE_CARA\":\"integer\"," +
"			\"DISTANZA_U\":\"integer\"," +
"			\"DIMENSIONI\":\"string\"," +
"			\"RIFIUTI_NO\":\"string\"," +
"			\"RIFIUTI_PE\":\"string\"," +
"			\"QUANTITA_R\":\"integer\"," +
"			\"STATO_FISI\":\"string\"," +
"			\"ODORE\":\"string\"," +
"			\"MODALITA_S\":\"string\"," +
"			\"PERCOLATO\":\"string\"," +
"			\"VEGETAZION\":\"string\"," +
"			\"STABILITA\":\"integer\"," +
"			\"INSEDIAMEN\":\"string\"," +
"			\"INSEDIAME1\":\"string\"," +
"			\"DISTANZA_C\":\"string\"," +
"			\"INSEDIAME2\":\"string\"," +
"			\"INSEDIAME3\":\"string\"," +
"			\"DISTANZA_P\":\"string\"," +
"			\"BOSCATE\":\"string\"," +
"			\"BOSCATE_AB\":\"string\"," +
"			\"AGRICOLO\":\"integer\"," +
"			\"AGRICOLO_A\":\"string\"," +
"			\"TORRENTI_R\":\"string\"," +
"			\"NOME_TORRE\":\"string\"," +
"			\"RISCHIO_ES\":\"string\"," +
"			\"RIFIUTI_IN\":\"string\"," +
"			\"PROBABILE_\":\"string\"," +
"			\"IMPATTO_ES\":\"string\"," +
"			\"POZZI_FALD\":\"string\"," +
"			\"CRITICITA\":\"string\"," +
"			\"IMPATTO_CO\":\"integer\"," +
"			\"NOTE\":\"string\"," +
"			\"PULIZIA\":\"string\"," +
"			\"DISSUASION\":\"string\"," +
"			\"VALORE_GRA\":\"integer\"," +
"			\"FATTIBILIT\":\"string\"," +
"			\"VALORE_FAT\":\"integer\"," +
"			\"LATITUDINE\":\"integer\"," +
"			\"LONGITUDIN\":\"integer\"," +
"			\"ID\":\"integer\"," +
"			\"ID1\":\"integer\"," +
"			\"GRAVITA\":\"string\"," +
"			\"RISCHIO\":\"string\"," +
"			\"VALORE_RIS\":\"integer\"," +
"			\"SOCIO_PAES\":\"string\"," +
"			\"VALORE_SOC\":\"integer\"," +
"			\"GMROTATION\":\"real\"" +
"		}" +
"	}" +
"}"    , MissionTemplate.class);
		
		HashMap<String,XDataType> templateDataTypes = mt.schema_seg.fields;

		assertNotNull(templateDataTypes);
		assertTrue(templateDataTypes.size()>0);
		
		
		
		// TODO: write validator for these templateDataTypes
		// fieldNames must not clash with PK_UID, ORIGIN_ID, GEOMETRY
		try {
			db.prepare("DROP TABLE IF EXISTS test_table;").step();
		} catch (jsqlite.Exception e) {
			fail(e.getLocalizedMessage());
		}
		
		// Method to test
		assertTrue(PersistenceUtils.createTableFromTemplate(db, "test_table", templateDataTypes));
		
		// Check results
        String query = "SELECT name FROM sqlite_master WHERE type='table' AND name='test_table'";

        try {
            Stmt stmt = db.prepare(query);
            if( stmt.step() ) {
                String nomeStr = stmt.column_string(0);
                assertTrue("Retrieved table name is incorrect", nomeStr.equalsIgnoreCase("test_table"));
            }else{
            	fail("Table not found");
            }
            stmt.close();
        } catch (Exception e) {
            fail( Log.getStackTraceString(e));
        }

        // check results
        query = "PRAGMA table_info(\"test_table\");";
        int nameColumn = -1;
        int typeColumn = -1;
        String columnName, typeName;

        int count_columns = 0;
        try {
            Stmt stmt = db.prepare(query);
            while( stmt.step() ) {
                if(nameColumn<0 || typeColumn<0){
                	// I have to retrieve the position of the metadata fields
                	for(int i = 0; i<stmt.column_count(); i++){
                		Log.v(TAG, stmt.column_name(i));
                		if(stmt.column_name(i).equalsIgnoreCase("name")){
                			nameColumn = i;
                		}
                		if(stmt.column_name(i).equalsIgnoreCase("type")){
                			typeColumn = i;
                		}
                	}
                	
                }
            	assertTrue(nameColumn>=0);
            	assertTrue(typeColumn>=0);
            	
            	columnName = stmt.column_string(nameColumn);
            	typeName = stmt.column_string(typeColumn);
            	
            	// output values
            	Log.v(TAG, count_columns+" : "+columnName+" : "+typeName);
        		
            	if(!columnName.equals("ORIGIN_ID") && !columnName.equals("GEOMETRY")){
            		assertNotNull(templateDataTypes.get(columnName));
            		assertTrue(typeName.equalsIgnoreCase(SpatialiteUtils.getSQLiteTypeFromString(templateDataTypes.get(columnName).toString())));
            	}
            	count_columns++;
            }
            stmt.close();
        } catch (Exception e) {
            fail( Log.getStackTraceString(e));
        }
        
        // output values
        int j = 0;
        for(Entry<String, XDataType> e : templateDataTypes.entrySet()){
        	Log.v(TAG, j++ +" : "+e.getKey()+" : "+e.getValue());
        }
        
        // Add 2 because the default create statement creates "ORIGIN_ID" and "GEOMETRY" columns
        // all the other columns comes from the templateDataTypes
        assertEquals(count_columns, templateDataTypes.size()+2);
	}
	
	// Validator for datatypes
		
	/**
	 * test openSpatialiteDB method
	 */
	public void testOpenSpatialiteDB(){
		
		db = SpatialiteUtils.openSpatialiteDB(getContext(), "geocollect/genova.sqlite");
		
		assertNotNull(db);
		assertTrue(!db.dbversion().equals("unknown"));

		try {
			db.close();
		} catch (jsqlite.Exception e) {
			//
		}
		assertTrue(db.dbversion().equals("unknown"));
		
		
		// test with wrong parameters
		db = SpatialiteUtils.openSpatialiteDB(null, "geocollect/genova.sqlite");
		assertNull(db);
		
		db = SpatialiteUtils.openSpatialiteDB(getContext(), null);
		assertNull(db);
		
		db = SpatialiteUtils.openSpatialiteDB(getContext(), "");
		assertNull(db);

		// TODO what is an illegal filename in android?
		/*
		db = SpatialiteUtils.openSpatialiteDB(getContext(), "()");
		assertNull(db);
		*/
	}

	
}
