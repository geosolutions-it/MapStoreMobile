package org.mapsforge.android.maps.mapgenerator.mbtiles;

import org.mapsforge.android.maps.mapgenerator.MapGeneratorJob;
import org.mapsforge.android.maps.mapgenerator.MapRenderer;
import org.mapsforge.core.model.GeoPoint;
import org.mapsforge.core.model.Tile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

/**
 * @author Robert Oehler
 */

public class MbTilesDatabaseRenderer implements MapRenderer {

	final static String TAG = MbTilesDatabaseRenderer.class.getSimpleName();

	MbTilesDatabase db;

	public MbTilesDatabaseRenderer(final Context pContext) {

		this.db = new MbTilesDatabase(pContext);

	}

	@Override
	public boolean executeJob(MapGeneratorJob mapGeneratorJob, Bitmap bitmap) {

		this.db.openDataBase();

		final Tile tile = mapGeneratorJob.tile;

		final int[] tmsTileXY = googleTile2TmsTile(tile.tileX, tile.tileY, tile.zoomLevel);
		// Log.d(TAG,String.format("Tile requested %d %d is now %d %d", tile.tileX, tile.tileY, tmsTileXY[0],
		// tmsTileXY[1]));

		byte[] rasterBytes = null;
		Bitmap decodedBitmap = null;
		int[] pixels = new int[Tile.TILE_SIZE * Tile.TILE_SIZE];

		rasterBytes = this.db.getTileAsBytes(String.valueOf(tmsTileXY[0]), String.valueOf(tmsTileXY[1]),
				Byte.toString(tile.zoomLevel));

		if (rasterBytes == null) {
			// got nothing
			this.db.close();
			return false;
		}

		decodedBitmap = BitmapFactory.decodeByteArray(rasterBytes, 0, rasterBytes.length);

		// check if the input stream could be decoded into a bitmap
		if (decodedBitmap != null) {
			// copy all pixels from the decoded bitmap to the color array
			decodedBitmap.getPixels(pixels, 0, Tile.TILE_SIZE, 0, 0, Tile.TILE_SIZE, Tile.TILE_SIZE);
			decodedBitmap.recycle();
		} else {
			for (int i = 0; i < pixels.length; i++) {
				pixels[i] = Color.WHITE;
			}
		}

		if (bitmap == null) {
			Bitmap.Config conf = Bitmap.Config.ARGB_8888;
			bitmap = Bitmap.createBitmap(Tile.TILE_SIZE, Tile.TILE_SIZE, conf);
		}

		// copy all pixels from the color array to the tile bitmap
		bitmap.setPixels(pixels, 0, Tile.TILE_SIZE, 0, 0, Tile.TILE_SIZE, Tile.TILE_SIZE);

		this.db.close();
		return true;
	}

	@Override
	public GeoPoint getStartPoint() {

		return new GeoPoint(10.799223, 46.31746);
	}

	@Override
	public Byte getStartZoomLevel() {

		return Byte.valueOf((byte) 8);
	}

	@Override
	public byte getZoomLevelMax() {

		return 11;
	}

	@Override
	public void destroy() {

		if (this.db != null) {
			this.db = null;
		}

	}

	public static int[] googleTile2TmsTile(long tx, long ty, byte zoom) {
		return new int[] { (int) tx, (int) ((Math.pow(2, zoom) - 1) - ty) };
	}
}
