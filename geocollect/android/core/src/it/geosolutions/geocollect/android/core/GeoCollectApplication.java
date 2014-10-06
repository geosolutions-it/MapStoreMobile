/*******************************************************************************
 * Copyright 2011-2013 Sergey Tarasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Edited by Lorenzo Pini (lorenzo.pini@geo-solutions.it) to adapt on GeoCollect
 * Copyright 2014  GeoSolutions
 *******************************************************************************/
package it.geosolutions.geocollect.android.core;

import it.geosolutions.android.map.utils.MapFilesProvider;

import java.io.File;

import org.mapsforge.android.maps.BackgroundSourceType;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

/**
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @author Lorenzo Pini (lorenzo.pini[at]geo-solutions[dot]it)
 */
public class GeoCollectApplication extends Application {
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	@Override
	public void onCreate() {
		// TODO: remove this block on production
//		if (true && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
//			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyDialog().build());
//			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyDeath().build());
//		}

		super.onCreate();
		
		initImageLoader(getApplicationContext());
		
		Log.d(GeoCollectApplication.class.getSimpleName(), "App onCreate");
		
		MapFilesProvider.setBaseDir("/geocollect");
		
		setupMBTilesBackgroundConfiguration();
	}

	private void setupMBTilesBackgroundConfiguration() {
    		
    		final String defType = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("mapsforge_background_type", null);
    		final String mbTilesPath = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("mapsforge_background_filepath", null);

    		if(mbTilesPath == null && defType == null){ //only on first run, check files and set geocollect defaults

    			//geocollect default not setup yet, check if mbtiles file is available
    			final File dir = new File(MapFilesProvider.getEnvironmentDirPath(null)+MapFilesProvider.getBaseDir());
    			File mbtileFile = null;
    			File files[] = dir.listFiles();
    			if(files != null){
    				for(int i = 0; i < files.length;i++){
    					final String path = files[i].getAbsolutePath();
    					if(path.substring(path.lastIndexOf(".") + 1).equals("mbtiles")){
    						mbtileFile = files[i];
    						break;
    					}
    				}
    				final Editor ed = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();
    				if(mbtileFile != null){
    					ed.putString("mapsforge_background_filepath", mbtileFile.getAbsolutePath());
    				}
    				ed.putString("mapsforge_background_type", "1");
    				ed.commit();
    			}
    			
    		}
    		
    		final BackgroundSourceType type = BackgroundSourceType.values()[Integer.parseInt(defType == null ? "1" : defType)];
    		MapFilesProvider.setBackgroundSourceType(type);

		
	}

	/*
	 * Using default initializer for the first implementation
	 * TODO: fine-tuning
	 */
	public static void initImageLoader(Context context) {
		// This configuration tuning is custom. You can tune every option, you may tune some of them,
		// or you can create default configuration by
		//  ImageLoaderConfiguration.createDefault(this);
		// method.
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.denyCacheImageMultipleSizesInMemory()
				.diskCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.LIFO)
				.writeDebugLogs() //  TODO: Remove for release app
				.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
		Log.v("GeoCollectApplication", "ImageLoader initialized");
	}
}