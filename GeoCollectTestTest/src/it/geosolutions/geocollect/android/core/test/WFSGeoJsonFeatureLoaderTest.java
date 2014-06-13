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

import java.util.List;

import it.geosolutions.android.map.wfs.WFSGeoJsonFeatureLoader;
import it.geosolutions.android.map.wfs.geojson.feature.Feature;

/**
 * Test for SQLiteCascadeFeatureLoader Class
 * In order to run this test, the device must have connectivity
 * 
 * @author Lorenzo Pini
 *
 */
public class WFSGeoJsonFeatureLoaderTest extends android.test.LoaderTestCase {

	static String TAG = "WFSFeatureLoaderTest";
	
	protected void setUp() throws Exception {
		super.setUp();
		// nothing to set up
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		// nothing to tear down
	}

	/**
	 * Tests the Loader main function
	 * TODO: investigate if getLoaderResultSynchronously() can be used with WFSGeoJsonFeatureLoader class 
	 */
	public void testWFSLoader(){
		
		int page = 0;
		int pagesize = 100;
		
		WFSGeoJsonFeatureLoader wfsl = new WFSGeoJsonFeatureLoader(
				getContext(),
				"http://demo.geo-solutions.it/share/comunege/geocollect/punti_abbandono.geojson",
				null, // baseparams
				"geosolutions:punti_abbandono",
				page*pagesize+1,
				pagesize);
		
		List<Feature> results = wfsl.loadInBackground();
		
		assertEquals(90, results.size() );
		
	}
	
}
