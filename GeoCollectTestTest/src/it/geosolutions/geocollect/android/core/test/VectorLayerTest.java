package it.geosolutions.geocollect.android.core.test;

import it.geosolutions.geocollect.android.core.login.utils.NetworkUtil;
import it.geosolutions.geocollect.android.core.mission.utils.VectorLayerLoader;
import it.geosolutions.geocollect.android.core.mission.utils.VectorLayerLoader.VectorLayerLoaderListener;
import it.geosolutions.geocollect.model.config.MissionTemplate;
import it.geosolutions.geocollect.model.http.VectorLayer;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jsqlite.Database;
import jsqlite.Exception;
import jsqlite.Stmt;
import android.content.Context;
import android.os.Environment;
import android.test.InstrumentationTestCase;
import android.util.Log;

import com.google.gson.Gson;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;

public class VectorLayerTest extends InstrumentationTestCase {
	
	/**
	 * tests
	 * 
	 * -reading vector layers from the mission template
	 * -downloading multiple layers
	 * -and their insert into the spatialite database
	 * 
	 */
	public void testVectorLayers(){
		
		final Context context = getInstrumentation().getTargetContext();
		assertNotNull(context);
		
		//get a database
		final Database db = createTestDatabase();
		assertNotNull(db);
		
		final VectorLayerLoader vectorLayerLoader = new VectorLayerLoader(db);
	
		//we can use either the mission template to load layers - contains one layer currently
		
		//get the default template
		MissionTemplate missionTemplate = null;
		InputStream inputStream = context.getResources().openRawResource(it.geosolutions.geocollect.android.app.R.raw.defaulttemplate);
		if (inputStream != null) {
			final Gson gson = new Gson();
			final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
			missionTemplate = gson.fromJson(reader, MissionTemplate.class);
		}
		assertNotNull(missionTemplate);
		
		ArrayList<VectorLayer> layersFromTemplate = vectorLayerLoader.checkIfVectorLayersAreAvailable(missionTemplate);
		
		assertNotNull(layersFromTemplate);
		assertTrue(layersFromTemplate.size() > 0);
		
		//or just create more than one layer to test the download of multiple layers
		final String url = "http://geocollect.geo-solutions.it/geoserver/it.geosolutions/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=it.geosolutions:muro&maxFeatures=5&outputFormat=application%2Fjson&srsName=EPSG:4326";
		final String style = "http://bc3.antares.uberspace.de/maps/test.style";
		final int version = 1;
		
		final ArrayList<VectorLayer> layers = new ArrayList<VectorLayer>();
		
		layers.add(new VectorLayer("a_muri", url, version, style));
		layers.add(new VectorLayer("b_muri", url, version, style));
		layers.add(new VectorLayer("c_muri", url, version, style));
		
		//online ?
		if (!NetworkUtil.isOnline(context)) {

			Log.w("VectorLayerDownloadTest", "No internet connection cannot test vector layer download");
			return;
		}
		final CountDownLatch latch = new CountDownLatch(1);
		
		//this runs in background - set a listener for the result
		vectorLayerLoader.setListener(new VectorLayerLoaderListener() {
			
			@Override
			public void error(String errorMessage) {
				
				fail(errorMessage);
				latch.countDown();
			}
			
			@Override
			public void didLoadLayers() {
				
				//done - test
				final WKBReader wkbReader = new WKBReader();
				
				for (VectorLayer layer : layers) {
					try {
						int recordCount = 0;
						final String tableName = layer.name;
						// test table exists and has entries
						Stmt stmt = db.prepare("SELECT * FROM '" + tableName+ "';");
						while (stmt.step()) {
							recordCount++;
						}
						stmt.close();

						assertTrue(recordCount > 0);
						// when using the self created vector layers there should be 5 entries
						assertTrue("count for "+tableName+" is unexpected : " + recordCount, recordCount == 5);

						// test columns / geometry
						stmt = db.prepare("SELECT ORIGIN_ID, VERSION, STYLE, URL, ST_AsBinary(GEOMETRY) FROM '"+ tableName + "';");
						while (stmt.step()) {

							String origin = stmt.column_string(0);
							assertNotNull(origin);
							int version = stmt.column_int(1);
							assertTrue(version > 0);
							String style = stmt.column_string(2);
							assertNotNull(style);
							String url = stmt.column_string(3);
							assertNotNull(url);
							byte[] geomBytes = stmt.column_bytes(4);
							assertNotNull(geomBytes);

							Geometry geometry = wkbReader.read(geomBytes);
							assertNotNull(geometry);
						}
						stmt.close();

					} catch (jsqlite.Exception e) {
						fail("Database access error");
					} catch (ParseException e) {
						fail("WKBReader failed to parse geometry");
					}
				}
				
				latch.countDown();
			}
		});
		
		//load layers and await result in listener
		vectorLayerLoader.loadLayers(layers);
		
		try {
			//this should complete in 10 seconds
			assertTrue(latch.await(10, TimeUnit.SECONDS));
		} catch (InterruptedException e) {
			fail("exception while awaiting execution");
		}
		
		//clean up
		try {
			db.close();
		} catch (Exception e) {
			Log.e("VectorLayerTest", "error closing test database");
		}
		
		deleteDatabase();
	}
	
	/**
	 * creates an empty database
	 */
	private Database createTestDatabase(){
		
		deleteDatabase();
		Database db = null;
		try {
			db = new jsqlite.Database();
			db.open(Environment.getExternalStorageDirectory()+ "/geocollect/testdb.sqlite", jsqlite.Constants.SQLITE_OPEN_READWRITE | jsqlite.Constants.SQLITE_OPEN_CREATE);
			// Initialize spatial metadata
			Stmt stmt = db.prepare("SELECT InitSpatialMetaData();");
			stmt.step();
			stmt.close();
		} catch (jsqlite.Exception e) {
			fail(e.getLocalizedMessage());
		}
		
		return db;	
	}

	/**
	 * Delete the SQLite database file
	 */
	private void deleteDatabase() {
		File file = new File(Environment.getExternalStorageDirectory()+ "/geocollect/testdb.sqlite");
		if (file.exists()) {
			file.delete();
		}
	}

}
