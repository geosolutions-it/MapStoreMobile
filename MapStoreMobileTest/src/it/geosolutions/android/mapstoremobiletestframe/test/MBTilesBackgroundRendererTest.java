package it.geosolutions.android.mapstoremobiletestframe.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.mapsforge.android.maps.mapgenerator.MapGeneratorJob;
import org.mapsforge.android.maps.mapgenerator.mbtiles.MbTilesDatabaseRenderer;
import org.mapsforge.core.model.BoundingBox;
import org.mapsforge.core.model.GeoPoint;
import org.mapsforge.core.model.Tile;

import android.content.Context;
import android.graphics.Bitmap;
import android.test.InstrumentationTestCase;
import android.util.Log;
/**
 * class to test the functionalities of the MBTiles renderer integrated into Mapsforge
 * 
 * @author Robert Oehler
 *
 */
public class MBTilesBackgroundRendererTest extends InstrumentationTestCase {


	private Context  mContext;

	private final static String TAG = MBTilesBackgroundRendererTest.class.getSimpleName();

	private MbTilesDatabaseRenderer mbTilesDatabaseRenderer;


	@Override
	public void setUp() throws Exception {
		super.setUp();

		mContext = getInstrumentation().getTargetContext();

		assertNotNull(mContext);
		
		//it is necessary to copy the test mbtiles file from assets to the internal memory as otherwise it cannot be read
		final String targetPath = getInstrumentation().getTargetContext().getApplicationInfo().dataDir+ "/test.mbtiles";
		File file = new File(targetPath);
		
		//check if already done on earlier launch, when not -> copy
		if(!file.exists()){
	
			try {

				InputStream is = getInstrumentation().getContext().getAssets().open("premium-snowdepth.mbtiles");
				
				copyFile(is, targetPath);

				file = new File(targetPath);
			} catch (IOException e) {
				Log.e(TAG, "error testMapView",e);
			}
		}				
		
		//now the file should be available always
		assertTrue(file.exists());

		mbTilesDatabaseRenderer = new MbTilesDatabaseRenderer(mContext, targetPath);

	}
	public void tearDown() throws Exception {
		
		mbTilesDatabaseRenderer.destroy();
		
		super.tearDown();
	}
/**
 * tests the extraction of tiles from the database
 */
	public void testExecuteJob(){


		//open its database
		mbTilesDatabaseRenderer.start();
		//it should be working
		boolean isWorking = mbTilesDatabaseRenderer.isWorking();

		assertTrue(isWorking);


		//now test the database, these are valid coordinates for the file premium-snowdepth.mbtiles provided in assets
		final int[] validDBCoords = {540,658,10};
		//these instead should always be invalid
		final int[] invalidDBCoords = {5400,363,10};

		//as the renderer converts from Google coords to TMS coords, convert here the tms coords to google coords
		final int[] validGoogleCoords = tmsTile2GoogleTile(validDBCoords[0], validDBCoords[1], validDBCoords[2]);
		final int[] invalidGoogleCoords = tmsTile2GoogleTile(invalidDBCoords[0], invalidDBCoords[1], invalidDBCoords[2]);
		
		//create tiles
		final Tile validTile = new Tile(validGoogleCoords[0], validGoogleCoords[1],(byte) validDBCoords[2]);
		final Tile invalidTile = new Tile(invalidGoogleCoords[0], invalidGoogleCoords[1],(byte) invalidDBCoords[2]);

		//jobs
		final MapGeneratorJob validJob = new MapGeneratorJob(validTile, null, null, null);
		final MapGeneratorJob invalidJob = new MapGeneratorJob(invalidTile, null, null, null);

		//empty bitmaps
		Bitmap validBitmap = Bitmap.createBitmap(Tile.TILE_SIZE, Tile.TILE_SIZE, Bitmap.Config.RGB_565);
		Bitmap invalidBitmap = Bitmap.createBitmap(Tile.TILE_SIZE, Tile.TILE_SIZE, Bitmap.Config.RGB_565); 

		//start a valid query, only in severe error scenarios this will fail e.g. for invalid tiles with zoom > 11
		assertTrue(mbTilesDatabaseRenderer.executeJob(validJob, validBitmap));
		//invalid query
		assertTrue(mbTilesDatabaseRenderer.executeJob(invalidJob,invalidBitmap));

		//prepare pixel arrays for evaluation
		int[] validPixels = new int[Tile.TILE_SIZE * Tile.TILE_SIZE];
		int[] invalidPixels = new int[Tile.TILE_SIZE * Tile.TILE_SIZE];

		//fill them with the bitmap pixels
		validBitmap.getPixels(validPixels, 0, Tile.TILE_SIZE, 0, 0, Tile.TILE_SIZE, Tile.TILE_SIZE);
		invalidBitmap.getPixels(invalidPixels, 0, Tile.TILE_SIZE, 0, 0, Tile.TILE_SIZE, Tile.TILE_SIZE);

		//check the valid pixels, they should not all be white for a non white tile
		boolean allValidPixelsAreWhite = true;

		for(int i = 0; i < validPixels.length; i++){
			//get value
			int validPixel = validPixels[i];
			//extract r,g and b
			int valid_r = (validPixel >> 16) & 0xff;
			int valid_g = (validPixel >> 8) & 0xff;
			int valid_b = (validPixel) & 0xff;

			//if one component is not white, this will turn to false
			allValidPixelsAreWhite = valid_r == 255 && valid_g == 255 && valid_b == 255;

			if(!allValidPixelsAreWhite){
				//not white, no need to go on
				break;
			}
		}
		//check at least on component of one pixel was not white
		assertFalse(allValidPixelsAreWhite);


		//instead for invalid tiles, all pixels should be white
		boolean allInvalidPixelsAreWhite = false;

		for(int j = 0; j < invalidPixels.length; j++){

			int invalidPixel = invalidPixels[j];

			int invalid_r = (invalidPixel >> 16) & 0xff;
			int invalid_g = (invalidPixel >> 8) & 0xff;
			int invalid_b = (invalidPixel) & 0xff;

			//this will turn to false if one component is not white
			allInvalidPixelsAreWhite = (invalid_r == 255 && invalid_g == 255 && invalid_b == 255);

			if(!allInvalidPixelsAreWhite){
				//failure, something was not white
				break;
			}
		}
		//all should have been white
		assertTrue(allInvalidPixelsAreWhite);


		//clean up
		
		validBitmap.recycle();
		invalidBitmap.recycle();
		
		mbTilesDatabaseRenderer.stop();
		

	}
	
	public void testMbTilesBoundingBox(){
		
		mbTilesDatabaseRenderer.start();
		
		final BoundingBox bb = mbTilesDatabaseRenderer.getBoundingBox();
		
		assertNotNull(bb);
		
		//premium-snowdepth.mbtiles dependent values
		final BoundingBox pSDBB = new BoundingBox(45.3308271267531,8.55646534342289,47.34943607332579,13.787875316072201);
		
		assertEquals(bb.maxLatitude, pSDBB.maxLatitude, 0.000001);
		assertEquals(bb.minLatitude, pSDBB.minLatitude, 0.000001);
		assertEquals(bb.maxLongitude, pSDBB.maxLongitude, 0.000001);
		assertEquals(bb.minLongitude, pSDBB.minLongitude, 0.000001);
		
		mbTilesDatabaseRenderer.stop();
		
	}
	/**
	 * tests the database metadata boundingbox and its resulting start (center) point
	 */
	public void testStartPoint(){
		
		mbTilesDatabaseRenderer.start();
		
		final GeoPoint center = mbTilesDatabaseRenderer.getStartPoint();
		
		assertNotNull(center);
		
		//this returns in case of error a default center, check that this did not happen
		assertNotSame(center, new GeoPoint(43.7242359188, 10.9463005959));
		
		//premium-snowdepth.mbtiles dependent values
		final BoundingBox pSDBB = new BoundingBox(45.3308271267531,8.55646534342289,47.34943607332579,13.787875316072201);
		final GeoPoint pSDCenter = pSDBB.getCenterPoint();
		
		assertEquals(center, pSDCenter);
		
		
		mbTilesDatabaseRenderer.stop();
		
	}

	public void copyFile(InputStream is, String target){

		OutputStream outputStream = null;

		try {
			// write the inputStream to a FileOutputStream
			outputStream =   new FileOutputStream(new File(target));

			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = is.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}

			Log.d(TAG, "done copying");

		} catch (IOException e) {
			Log.e(TAG, "error saveFile",e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					Log.e(TAG, "error saveFile",e);
				}
			}
			if (outputStream != null) {
				try {
					outputStream.flush();
					outputStream.close();
				} catch (IOException e) {
					Log.e(TAG, "error saveFile",e);
				}

			}
		}

	}

	public static int[] tmsTile2GoogleTile( int tx, int ty, int zoom ) {
		return new int[]{tx, (int) ((Math.pow(2, zoom) - 1) - ty)};
	}
}
