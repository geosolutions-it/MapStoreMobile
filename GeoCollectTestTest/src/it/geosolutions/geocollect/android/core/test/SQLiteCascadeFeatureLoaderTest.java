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
import jsqlite.Database;
import jsqlite.Stmt;
import it.geosolutions.android.map.wfs.WFSGeoJsonFeatureLoader;
import it.geosolutions.android.map.wfs.geojson.feature.Feature;
import it.geosolutions.geocollect.android.core.mission.utils.SQLiteCascadeFeatureLoader;
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
		String tableName = "testTable";
		
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
		List<Feature> results = loaderToTest.loadInBackground();
		
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
