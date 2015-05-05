/*
 * GeoSolutions GeoCollect - Digital field mapping on Android based devices
 * Copyright (C) 2014 - 2015  GeoSolutions (www.geo-solutions.it)
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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import it.geosolutions.android.map.utils.MapFilesProvider;
import it.geosolutions.android.map.utils.ZipFileManager;
import it.geosolutions.geocollect.android.core.GeoCollectApplication;
import it.geosolutions.geocollect.android.core.mission.PendingMissionListActivity;
import it.geosolutions.geocollect.android.core.mission.PendingMissionListFragment;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.newrelic.agent.android.NewRelic;

/**
 * Startup activity loaded when launch.
 * 
 * @author Lorenzo Natali (lorenzo.natali geo-solutions.it)
 */
public class StartupActivity extends Activity {

    /**
     * onCreate method for startup activity.
     * 
     * @param savedInstanceState.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // id really needed the set content vie for a starter
        // setContentView(R.layout.activity_startup);
        // TODO place from a click on the item

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
            Log.e("StartupActivity", e.toString());
        }

        // Get the NewRelic token, if found start the monitoring
        String newRelicToken = properties.getProperty("newRelicToken"); 
        if(newRelicToken != null){
            NewRelic.withApplicationToken(newRelicToken).start(this.getApplication());
        }
        
        String dir_path = Environment.getExternalStorageDirectory().getPath();
        ZipFileManager zfm = new ZipFileManager(this, dir_path, MapFilesProvider.getBaseDir(), getResources()
                .getString(R.string.url_data_test_archive)) {
            @Override
            public void launchMainActivity(final boolean success) {

                if (getApplication() instanceof GeoCollectApplication) {
                    ((GeoCollectApplication) getApplication()).setupMBTilesBackgroundConfiguration();
                }

                Intent launch = new Intent(activity, PendingMissionListActivity.class);
                // TODO remove it when using a WFS or a database
                launch.putExtra(PendingMissionListFragment.INFINITE_SCROLL, false);

                activity.startActivity(launch);

                activity.finish();
            }
        };

    }

    /**
     * onDestroy method for StartupActivity
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}