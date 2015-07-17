/*******************************************************************************
 * Copyright 2014-2015 GeoSolutions
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
 * 
 *******************************************************************************/
package it.geosolutions.geocollect.android.core;

import it.geosolutions.android.map.utils.MapFilesProvider;
import it.geosolutions.geocollect.model.config.MissionTemplate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.mapsforge.android.maps.BackgroundSourceType;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.content.res.AssetManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.newrelic.agent.android.NewRelic;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

/**
 * @author Lorenzo Pini (lorenzo.pini[at]geo-solutions[dot]it)
 */
public class GeoCollectApplication extends Application {

    /**
     * The template that provides the form and the endpoint to connect to
     */
    private MissionTemplate missionTemplate;

    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    @Override
    public void onCreate() {
        // TODO: remove this block on production
        // if (true && Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
        // StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyDialog().build());
        // StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyDeath().build());
        // }

        super.onCreate();

        initImageLoader(getApplicationContext());

        Log.d(GeoCollectApplication.class.getSimpleName(), "App onCreate");

        MapFilesProvider.setBaseDir("/geocollect");

        // Load the application properties file
        Properties properties = new Properties();
        try {
            // access to the folder ‘assets’
            AssetManager am = getAssets();
            // opening the file
            InputStream inputStream = am.open("geocollect.properties");
            // loading of the properties
            properties.load(inputStream);
            
        } catch (IOException e) {
            Log.e(GeoCollectApplication.class.getSimpleName(), e.toString());
        }

        // Get the NewRelic token, if found start the monitoring
        String newRelicToken = properties.getProperty("newRelicToken"); 
        if(newRelicToken != null){
            NewRelic.withApplicationToken(newRelicToken).start(this);
        }
        
        // setupMBTilesBackgroundConfiguration();
    }

    public void setupMBTilesBackgroundConfiguration() {

        final String defType = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString(
                "mapsforge_background_type", null);
        final String mbTilesPath = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString(
                "mapsforge_background_filepath", null);

        if (mbTilesPath == null && defType == null) { // only on first run, check files and set geocollect defaults

            // geocollect default not setup yet, check if mbtiles file is available
            final File dir = new File(MapFilesProvider.getEnvironmentDirPath(null) + MapFilesProvider.getBaseDir());
            File mbtileFile = null;
            File files[] = dir.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    final String path = files[i].getAbsolutePath();
                    if (path.substring(path.lastIndexOf(".") + 1).equals("mbtiles")) {
                        mbtileFile = files[i];
                        break;
                    }
                }
                final Editor ed = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();
                if (mbtileFile != null) {
                    ed.putString("mapsforge_background_filepath", mbtileFile.getAbsolutePath());
                }
                ed.putString("mapsforge_background_type", "1");
                ed.commit();
            }

        }

        final BackgroundSourceType type = BackgroundSourceType.values()[Integer.parseInt(defType == null ? "1"
                : defType)];
        MapFilesProvider.setBackgroundSourceType(type);

    }

    /*
     * Using default initializer for the first implementation
     * Based on the work of Sergey Tarasevich (nostra13[at]gmail[dot]com)
     * TODO: fine-tuning
     */
    public static void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        // ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .threadPriority(Thread.NORM_PRIORITY - 2).denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator()).tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
        Log.v("GeoCollectApplication", "ImageLoader initialized");
    }

    /**
     * Sets the current missionTemplate
     * 
     * @param pMissionTemplate the template to set
     */
    public void setTemplate(MissionTemplate pMissionTemplate) {

        missionTemplate = pMissionTemplate;

    }

    public MissionTemplate getTemplate() {
        return missionTemplate;
    }

}