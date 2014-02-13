/*
 * GeoSolutions map - Digital field mapping on Android based devices
 * Copyright (C) 2013  GeoSolutions (www.geo-solutions.it)
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
package it.geosolutions.android.mapstoremobile;

import it.geosolutions.android.map.utils.ZipFileManager;

import java.io.File;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;

/**
 * Startup activity loaded when launch mapstore mobile, download a data sample
 * archive from web and decompress it using ZipFileManager class
 * @author Jacopo Pianigiani (jacopo.pianigiani85@gmail.com)
 */
public class StartupActivity extends Activity {
	private String dir_path;

	/**
	 * onCreate method for startup activity.
	 * @ param savedInstanceState.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_startup);	
	
		dir_path = Environment.getExternalStorageDirectory().getPath();
		ZipFileManager zfm = new ZipFileManager(this,dir_path);
	}
	
	/**
	 * onDestroy method for StartupActivity
	 */
	@Override
	protected void onDestroy(){
		super.onDestroy();
		
		//If an incomplete archive file exists it will be deleted
		File archive = new File(dir_path + "/data_test_archive.zip");
		if(archive.exists())
			archive.delete();
	}
}