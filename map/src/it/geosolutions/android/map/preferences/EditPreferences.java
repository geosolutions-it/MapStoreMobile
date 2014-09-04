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


import it.geosolutions.android.map.R;
import it.geosolutions.android.map.dialog.FilePickerDialog;
import it.geosolutions.android.map.dialog.FilePickerDialog.FilePickCallback;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.WindowManager;

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
		
	}
	

	
	 @Override
	  public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,final Preference preference) {
		 if(preference.getKey().equals("UseMbTiles")){

			 final boolean mbTiles  = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("UseMbTiles", false);

			 if(mbTiles){
				 new FilePickerDialog(this,
						 "Select a MBTiles database file",
						 Environment.getExternalStorageDirectory()+"/mapstore/",
						 "mbtiles",
						 new FilePickCallback() {

					 @Override
					 public void filePicked(final String fileName) {

						 Log.d("MapsActivity", "Selected "+fileName);
						 final Editor ed = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();
						 ed.putString("MbTilesFile", fileName);
						 ed.commit();
					 }
				 });
			 }
		 }
		 return false;
    }
}
