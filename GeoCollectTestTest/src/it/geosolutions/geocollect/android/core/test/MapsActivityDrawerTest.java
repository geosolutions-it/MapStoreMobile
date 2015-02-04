package it.geosolutions.geocollect.android.core.test;

import it.geosolutions.android.map.MapsActivity;
import it.geosolutions.android.map.MapsActivity.DrawerMode;
import android.content.Intent;
import android.test.ActivityUnitTestCase;

public class MapsActivityDrawerTest   extends ActivityUnitTestCase<MapsActivity> {

	static String TAG = MapsActivityDrawerTest.class.getSimpleName();

	public MapsActivityDrawerTest() {
		super(MapsActivity.class);
	}

	public MapsActivityDrawerTest(Class<MapsActivity> activityClass) {
		super(activityClass);
	}

	/**
	 * only one of them will be launched correctly
	 * 
	 * sometimes it will throw an exception due some Sherlock method, try again, don't change
	 */

//	public void testBoth(){
//
//		Intent both = new Intent(Intent.ACTION_MAIN);
//
//		both.putExtra(MapsActivity.PARAMETERS.DRAWER_MODE, DrawerMode.BOTH.ordinal());
//
//		MapsActivity mapsActivityWithBoth = startActivity(both, null, null);
//
//		assertTrue(mapsActivityWithBoth.getLayerMenu() != null);
//
//		assertTrue(mapsActivityWithBoth.getDrawerList() != null);
//	}
//	
//	public void testLeft(){
//
//		Intent left = new Intent(Intent.ACTION_MAIN);
//
//		left.putExtra(MapsActivity.PARAMETERS.DRAWER_MODE, DrawerMode.ONLY_LEFT.ordinal());
//
//		MapsActivity mapsActivityWithLeft = startActivity(left, null, null);
//
//		assertFalse(mapsActivityWithLeft.getLayerMenu() != null);
//
//		assertTrue(mapsActivityWithLeft.getDrawerList() != null);
//
//	}
//
//
	public void testNone(){

		Intent none= new Intent(Intent.ACTION_MAIN);

		none.putExtra(MapsActivity.PARAMETERS.DRAWER_MODE, DrawerMode.NONE.ordinal());

		MapsActivity mapsActivityWithNone = startActivity(none, null, null);

		assertFalse(mapsActivityWithNone.getLayerMenu() != null);

		assertFalse(mapsActivityWithNone.getDrawerList() != null);
	}

}
