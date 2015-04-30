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

import it.geosolutions.android.map.MapsActivity;
import it.geosolutions.android.map.utils.MapFilesProvider;
import it.geosolutions.android.map.utils.ZipFileManager;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

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
		dir_path = MapFilesProvider.getEnvironmentDirPath(this);
		
		ZipFileManager zfm = new ZipFileManager(
				this,
				dir_path,
				"/mapstore",
				getResources().getString(R.string.url_data_test_archive)
				){
			
			@Override
			public void launchMainActivity(final boolean success){
				Intent launch = new Intent(activity,MapsActivity.class);
				launch.setAction(Intent.ACTION_VIEW);
				launch.putExtra(MapsActivity.PARAMETERS.LAT, 43.68411);
				launch.putExtra(MapsActivity.PARAMETERS.LON, 10.84899);
				launch.putExtra(MapsActivity.PARAMETERS.ZOOM_LEVEL, (byte)13);
				//For the future, passing a marker
				/*ArrayList<MarkerDTO> markers = new ArrayList(1);
				MarkerDTO markerDTO1 = new MarkerDTO(43.7242359188,10.9463005959, MarkerDTO.MARKER_RED); 
				markerDTO1.setId("AB123456789");
				markerDTO1.setDescription("Segnalazione 1");
				boolean add = markers.add(markerDTO1);
				launch.putParcelableArrayListExtra(MapsActivity.PARAMETERS.MARKERS, markers);*/
				
				activity.startActivity(launch);
				activity.finish();
			}
		};
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