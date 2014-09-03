/*
 * Copyright 2010, 2011, 2012 mapsforge.org
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.mapsforge.android.maps.mapgenerator.mbtiles;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author Robert Oehler
 */

public class MbTilesDatabase extends SQLiteOpenHelper {

	// private static String DB_PATH = "/data/data/org.mapsforge.android/databases/";

	private static String DB_PATH;

	private static String DB_NAME = "premium-slope.mbtiles";

	private SQLiteDatabase mDataBase;

	private final Context mContext;

	/**
	 * Constructor takes and keeps a reference of the passed context in order to access to the application assets and
	 * resources.
	 * 
	 * @param context
	 */
	public MbTilesDatabase(Context context) {

		super(context, DB_NAME, null, 1);

		this.mContext = context;

		DB_PATH = "/data/data/" + this.mContext.getApplicationContext().getPackageName() + "/databases/";

		try {
			createDataBase();
		} catch (IOException e) {
			Log.e("MbTilesDatabase", "IOE accessing the mbtiles db", e);
		}
	}

	/**
	 * Creates a empty database on the system and rewrites it with the mbtiles database.
	 */
	public void createDataBase() throws IOException {

		boolean dbExist = checkDataBase();

		if (dbExist) {
			// do nothing - database already exist
		} else {

			// By calling this method and empty database will be created into the default system path
			// of your application so we are gonna be able to overwrite that database with our database.
			this.getReadableDatabase();

			try {

				this.close();
				copyDataBase();
				this.openDataBase();
				this.close();

			} catch (IOException e) {

				throw new Error("Error copying database");

			}
		}
	}

	/**
	 * Check if the database already exist to avoid re-copying the file each time you open the application.
	 * 
	 * @return true if it exists, false if it doesn't
	 */
	private boolean checkDataBase() {

		SQLiteDatabase checkDB = null;

		try {

			checkDB = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.OPEN_READONLY);

		} catch (SQLiteException e) {

			// database does't exist yet.

		}

		if (checkDB != null) {

			checkDB.close();

		}

		return checkDB != null ? true : false;
	}

	/**
	 * Copies the database from local assets-folder to the just created empty database in the system folder, from where
	 * it can be accessed and handled. This is done by transferring bytestream.
	 */
	private void copyDataBase() throws IOException {

		// Open your local db as the input stream
		InputStream myInput = this.mContext.getAssets().open(DB_NAME);

		// Path to the just created empty db
		String outFileName = DB_PATH + DB_NAME;

		// Open the empty db as the output stream
		OutputStream myOutput = new FileOutputStream(outFileName);

		// transfer bytes from the inputfile to the outputfile
		byte[] buffer = new byte[1024];
		int length;
		while ((length = myInput.read(buffer)) > 0) {
			myOutput.write(buffer, 0, length);
		}

		// Close the streams
		myOutput.flush();
		myOutput.close();
		myInput.close();

	}

	public void openDataBase() throws SQLException {

		// Open the database
		String myPath = DB_PATH + DB_NAME;
		this.mDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

	}

	@Override
	public synchronized void close() {

		if (this.mDataBase != null)
			this.mDataBase.close();

		super.close();

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

	// //***Actual DB ACCESS****/////////

	/**
	 * from MBTilesDroidSplitter
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public byte[] getTileAsBytes(String x, String y, String z) {

		final Cursor c = this.mDataBase.rawQuery(
				"select tile_data from tiles where tile_column=? and tile_row=? and zoom_level=?", new String[] { x, y,
						z });
		if (!c.moveToFirst()) {
			c.close();
			return null;
		}
		byte[] bb = c.getBlob(c.getColumnIndex("tile_data"));
		c.close();
		return bb;
	}

}
