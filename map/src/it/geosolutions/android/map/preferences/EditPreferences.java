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
package it.geosolutions.android.map.preferences;


import java.io.File;

import org.mapsforge.android.maps.MapView;

import it.geosolutions.android.map.R;
import it.geosolutions.android.map.dialog.FilePickerDialog;
import it.geosolutions.android.map.dialog.FilePickerDialog.FilePickCallback;
import it.geosolutions.android.map.utils.MapFilesProvider;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;

/**
 * Activity to edit the application preferences.
 */
public class EditPreferences extends SherlockPreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// check if the full screen mode should be activated
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("fullscreen", false)) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		} else {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
		}
		
		//sets the currently selected filename if available
		Preference source = findPreference("mapsforge_background_type");
		final String filePath = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString(MapView.MAPSFORGE_BACKGROUND_FILEPATH, null);
		if(filePath != null){
			if(filePath.contains("/")){			
				source.setSummary(filePath.substring(filePath.lastIndexOf("/") + 1));
			}else{
				source.setSummary(filePath);		
			}
		}
		
	}
	

	
	 @Override
	  public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,final Preference preference) {
		 if(preference.getKey().equals("mapsforge_background_type")){

			 //listens to changes of this preference and launches a file selection dialog according to the select source
			 preference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
		            @Override
		            public boolean onPreferenceChange(final Preference preference, Object newValue) {
		            	
		            	final int type = Integer.parseInt(newValue.toString());
		            	final String oldType = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString(MapView.MAPSFORGE_BACKGROUND_RENDERER_TYPE, "0");
		            	
		            	//TODO implement geopackage
		            	if(type == 2){
		            		Toast.makeText(getBaseContext(), "not implemented yet", Toast.LENGTH_SHORT).show();
		            		return false;
		            	}
		            	
		            	String extension = null;
		            	
		            	switch(type){
		            	case 0:
		            		extension = "map";
		            		break;
		            	case 1:
		            		extension = "mbtiles";
		            		break;
		            	case 2:
		            		extension = "gpkg";
		            		break;
		            	}
		            	
		            	new FilePickerDialog(EditPreferences.this,
		            			getString(R.string.preferences_background_source_file),
		            			MapFilesProvider.getEnvironmentDirPath(null)+MapFilesProvider.getBaseDir(),
		            			extension,
		            			new FilePickCallback() {
		            		
		            		@Override
		            		public void filePicked(final File file) {
		            			
		            			final Editor ed = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();
		            			ed.putBoolean(MapView.MAPSFORGE_BACKGROUND_FILEPATH_CHANGED, true);
		            			ed.putString(MapView.MAPSFORGE_BACKGROUND_FILEPATH, file.getAbsolutePath());
		            			ed.commit();
		            			MapFilesProvider.setBackgroundFileName(file.getName());
		            			preference.setSummary(file.getName());
		            		}

							@Override
							public void noFilePicked() {
								
								//no file was selected, reset to previous type
								Editor ed = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();
								ed.putString(MapView.MAPSFORGE_BACKGROUND_RENDERER_TYPE, oldType);
								ed.commit();
								
							}
		            	});
		            	

		                return true;
		            }
		        });
			 
		 }
		 return true;
    }
}
