package it.geosolutions.geocollect.android.core.test;

import it.geosolutions.android.map.wfs.geojson.GeoJson;
import it.geosolutions.geocollect.android.core.mission.MissionFeature;
import it.geosolutions.geocollect.android.core.mission.utils.MissionUtils;
import it.geosolutions.geocollect.model.source.XDataType;

import java.util.HashMap;
import android.util.Log;

public class MissionUtilsTest extends android.test.AndroidTestCase{

	static String TAG = "MissionUtilsTest";
	
	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	/**
	 * Test 
	 * Creates a mission feature with all String values in properties
	 * Changes the type of one to Numeric
	 * the resulting JSON should not have quotes around the value
	 */
	public void testAlignFeaturePropertiesTypes(){
		
		// SETUP
		
		HashMap<String,XDataType> fields = new HashMap<String,XDataType>();
		fields.put("field1", XDataType.string);
		fields.put("field2", XDataType.integer);
		fields.put("field3", XDataType.decimal);
		fields.put("field4", XDataType.real);
		
		MissionFeature inputFeature = new MissionFeature();
		
		inputFeature.properties = new HashMap<String, Object>();
		
		// Setting the fields
		// field2 and field3 should be converted
		inputFeature.properties.put("field1", "123");
		inputFeature.properties.put("field2", "456");
		inputFeature.properties.put("field3", "789");
		inputFeature.properties.put("field3", "0100");

		// EXECUTE
		//MissionUtils.alignPropertiesTypes(Feature inputFeature, HashMap<String,XDataType> fields);
		MissionUtils.alignPropertiesTypes(inputFeature, fields);
		
		// TEST
		GeoJson gson = new GeoJson();
		String c = gson.toJson( inputFeature);
		
		Log.v(TAG, c);
		
		assertTrue(c.contains("\"123\""));
		assertFalse(c.contains("\"456\""));
		assertFalse(c.contains("\"789\""));
		assertFalse(c.contains("\"0100\""));

	}
	
	
	
}
