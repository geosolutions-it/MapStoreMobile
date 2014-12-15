package it.geosolutions.geocollect.android.core.test;

import org.mapsforge.core.model.GeoPoint;

import it.geosolutions.android.map.overlay.items.DescribedMarker;
import it.geosolutions.geocollect.android.core.mission.SimpleMapActivity;
import android.content.Intent;
import android.test.ActivityUnitTestCase;

public class SimpleMapActivityTest  extends ActivityUnitTestCase<SimpleMapActivity> {

	static String TAG =SimpleMapActivityTest.class.getSimpleName();

	public SimpleMapActivityTest() {
		super(SimpleMapActivity.class);
	}

	public SimpleMapActivityTest(Class<SimpleMapActivity> activityClass) {
		super(activityClass);
	}
	
	public void testSimpleMapActivity(){
		
		final GeoPoint one = new GeoPoint(42.0, 10.0);
		
		final GeoPoint two = new GeoPoint(10.0, 42.0);
		
		Intent mapIntent = new Intent(Intent.ACTION_MAIN);
		
		final byte zoom = 18;
		
		mapIntent.putExtra(SimpleMapActivity.ARG_FIRST_POINT_LAT, one.latitude);
		mapIntent.putExtra(SimpleMapActivity.ARG_FIRST_POINT_LON, one.longitude);
		mapIntent.putExtra(SimpleMapActivity.ARG_ZOOM, zoom);	

		mapIntent.putExtra(SimpleMapActivity.ARG_SECOND_POINT_LAT, two.latitude);
		mapIntent.putExtra(SimpleMapActivity.ARG_SECOND_POINT_LON, two.longitude);
		
		SimpleMapActivity sma = startActivity(mapIntent, null, null);
		
		DescribedMarker origin = sma.getOriginMarker();
		DescribedMarker update = sma.getUpdatedMarker();
		
		assertNotNull(origin);
		assertNotNull(update);
		
		assertEquals(one, origin.getGeoPoint());
		assertEquals(two,update.getGeoPoint());
		
		assertTrue(sma.getMapView().getOverlayManger().getMarkerOverlay().getMarkers().size() == 2);

		assertEquals(sma.getMapView().getMapViewPosition().getZoomLevel(), zoom);
	}

}
