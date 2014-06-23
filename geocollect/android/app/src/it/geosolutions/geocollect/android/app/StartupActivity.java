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
package it.geosolutions.geocollect.android.app;


import it.geosolutions.android.map.MapsActivity;
import it.geosolutions.android.map.utils.MapFilesProvider;
import it.geosolutions.android.map.utils.ZipFileManager;
import it.geosolutions.geocollect.android.core.mission.PendingMissionListActivity;
import it.geosolutions.geocollect.android.core.mission.PendingMissionListFragment;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.TaskStackBuilder;

/**
 * Startup activity loaded when launch.
 * @author Lorenzo Natali (lorenzo.natali geo-solutions.it)
 */
public class StartupActivity extends Activity {

	/**
	 * onCreate method for startup activity.
	 * @ param savedInstanceState.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		//id really needed the set content vie for a starter
		//setContentView(R.layout.activity_startup);	
		//TODO place from a click on the item
		 
		 MapFilesProvider.setBaseDir("/geocollect");
		 String dir_path = Environment.getExternalStorageDirectory().getPath();
         ZipFileManager zfm = new ZipFileManager(this,dir_path,"/geocollect",getResources().getString(R.string.url_data_test_archive)){
 			@Override
 			public void launchMainActivity(){
 				
 				Intent launch = new Intent(activity, PendingMissionListActivity.class);
 				//TODO remove it when using a WFS or a database
 				launch.putExtra(PendingMissionListFragment.INFINITE_SCROLL,false);
 				launch.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
 				
 				TaskStackBuilder sBuilder = TaskStackBuilder.create( activity );
 				sBuilder.addNextIntent(launch);
 				
 				Intent launchMap = new Intent(activity,MapsActivity.class);
 				launchMap.setAction(Intent.ACTION_VIEW);
 				launchMap.putExtra(MapsActivity.PARAMETERS.LAT, 44.40565);
 				launchMap.putExtra(MapsActivity.PARAMETERS.LON, 8.946256);
 				launchMap.putExtra(MapsActivity.PARAMETERS.ZOOM_LEVEL, (byte)11);
 				launchMap.putExtra(MapsActivity.PARAMETERS.CONFIRM_ON_EXIT, false);
 				// disable the animation
 				launchMap.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
 				sBuilder.addNextIntent(launchMap);
 				sBuilder.startActivities();
 				//activity.startActivity(pIntent);
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
		
		
	}
}