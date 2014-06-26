/*
 * GeoSolutions map - Digital field mapping on Android based devices
 * Copyright (C) 2013 - 2014  GeoSolutions (www.geo-solutions.it)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.geosolutions.geocollect.android.core.test;

import java.io.File;
import java.util.List;

import com.google.gson.Gson;

import jsqlite.Database;
import jsqlite.Stmt;
import it.geosolutions.android.map.wfs.WFSGeoJsonFeatureLoader;
import it.geosolutions.android.map.wfs.geojson.feature.Feature;
import it.geosolutions.geocollect.android.core.mission.MissionFeature;
import it.geosolutions.geocollect.android.core.mission.utils.PersistenceUtils;
import it.geosolutions.geocollect.android.core.mission.utils.SQLiteCascadeFeatureLoader;
import it.geosolutions.geocollect.model.config.MissionTemplate;
import android.os.Environment;
import android.util.Log;

/**
 * Test for SQLiteCascadeFeatureLoader Class
 * In order to run this test, the device must have connectivity
 * 
 * 
 * @author Lorenzo Pini
 *
 */
public class SQLiteCascadeFeatureLoaderTest extends android.test.LoaderTestCase {

	static String TAG = "SQLiteCascadeFeatureLoaderTest";
	
	Database db;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		dbSetUp();
	}

	@Override
	protected void tearDown() throws Exception {
		dbTeardown();
		super.tearDown();
	}

	/**
	 * Closes the database and delete the file
	 * @throws Exception
	 */
	private void dbTeardown() throws Exception {
		db.close();
		deleteDatabase();
	}

	/**
	 * Recreates an empty database
	 * @throws Exception
	 */
	private void dbSetUp() throws Exception {
		deleteDatabase();

		db = new jsqlite.Database();
		db.open(Environment.getExternalStorageDirectory()
				+ "/geocollect/testdb.sqlite",
				jsqlite.Constants.SQLITE_OPEN_READWRITE
                | jsqlite.Constants.SQLITE_OPEN_CREATE);
		
		// Initialize spatial metadata
		try {
			Stmt stmt = db.prepare("SELECT InitSpatialMetaData();");
			stmt.step();
			stmt.close();
		} catch (jsqlite.Exception e) {
			Log.e(TAG, Log.getStackTraceString(e));
			fail(e.getLocalizedMessage());
		}
		

	}

	/**
	 * Delete the SQLite database file
	 */
	private void deleteDatabase() {
		File file = new File(Environment.getExternalStorageDirectory()
				+ "/geocollect/testdb.sqlite");
		if (file.exists()) {
			file.delete();
		}
	}

	/**
	 * Tests the Loader main function
	 * TODO: investigate if getLoaderResultSynchronously() can be used with this class 
	 * Create a WFSLoader to preload the data
	 * 
	 */
	public void testSQLiteLoaderWithWFSPreLoader(){
		
		// Create test table
		
		Gson gson = new Gson();
		String template1 = "{	" +
				"	\"id\":\"punti_accumulo\"," +
				"	\"title\": \"Punti Abbandono\"," +
				"	\"source\":{" +
				"		\"type\":\"WFS\"," +
				"		\"URL\":\"http://demo.geo-solutions.it/share/comunege/geocollect/punti_abbandono.geojson\"," +
				"		\"typeName\":\"geosolutions:punti_abbandono\"," +
				"		\"localSourceStore\":\"testTable\","+
				"		\"dataTypes\":{" +
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
		MissionTemplate mt1 = gson.fromJson( template1 , MissionTemplate.class);
		
		String tableName = mt1.source.localSourceStore; //"testTable"

		PersistenceUtils.createTableFromTemplate(db, tableName, mt1.source.dataTypes);
		
		/*
		try {
			Stmt stmt = db.prepare("CREATE TABLE '"+tableName+"' ('ORIGIN_ID' TEXT);");
			stmt.step();
			stmt = db.prepare("SELECT AddGeometryColumn('"+tableName+"', 'GEOMETRY', 4326, 'POINT', 'XY');");
			stmt.step();
			stmt = db.prepare("SELECT CreateSpatialIndex('"+tableName+"', 'GEOMETRY');");
			stmt.step();
			stmt.close();
		} catch (jsqlite.Exception e) {
			Log.e(TAG, Log.getStackTraceString(e));
			fail(e.getLocalizedMessage());
		}
		*/
		
		// Setup the preloader
		int page = 0;
		int pagesize = 100;
		
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
		
		// Check results
		assertEquals(90, results.size() );
		
		// Check the database records number
		int recordCount = 0;
		try {
			Stmt stmt = db.prepare("SELECT * FROM '"+tableName+"';");
			while(stmt.step()){
				recordCount++;
			}
			stmt.close();
		} catch (jsqlite.Exception e) {
			Log.e(TAG, Log.getStackTraceString(e));
			fail(e.getLocalizedMessage());
		}
		assertEquals(90, recordCount);
	}
	
	
}
