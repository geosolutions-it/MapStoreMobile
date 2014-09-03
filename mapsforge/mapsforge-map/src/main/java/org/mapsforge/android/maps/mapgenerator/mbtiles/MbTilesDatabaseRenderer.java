package org.mapsforge.android.maps.mapgenerator.mbtiles;

import org.mapsforge.android.maps.mapgenerator.MapGeneratorJob;
import org.mapsforge.android.maps.mapgenerator.MapRenderer;
import org.mapsforge.core.model.GeoPoint;

import android.graphics.Bitmap;

/**
 * @author Robert Oehler
 */

public class MbTilesDatabaseRenderer implements MapRenderer {

	MbTilesDatabaseHelper mMbTilesDatabaseHelper;

	public MbTilesDatabaseRenderer() {

		mMbTilesDatabaseHelper = new MbTilesDatabaseHelper();
	}

	@Override
	public boolean executeJob(MapGeneratorJob mapGeneratorJob, Bitmap bitmap) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public GeoPoint getStartPoint() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Byte getStartZoomLevel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte getZoomLevelMax() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
