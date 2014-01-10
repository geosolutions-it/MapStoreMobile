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
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.preference.ListPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Preferences class to set the radius of on time selection.
 * @author Jacopo Pianigiani (jacopo.pianigiani85@gmail.com).
 */
public class SetRadiusPreference extends SeekBarPreference implements OnSharedPreferenceChangeListener{

	/**
	* The default value for value(In pixel) of radius of an on time selection.
	*/
	private static final double RADIUS_PIXEL_DEFAULT = 10.0;
	
	/**
	* The maximum value for radius of on time selection.
	*/
	private static final double RADIUS_PIXEL_MAX = 100.0;
	    	
	/**
	 * Construct a new set radius preference seek bar.
	 * 
	 * @param context
	 *            the context activity.
	 * @param attrs
	 *            A set of attributes (currently ignored).
	 */
	public SetRadiusPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		//Find preference about shape of selection and on time selection radius.
		/*final Preference ontime_radius = this; //findPreferenceInHierarchy("OnTimeSelectionRadius");
		/*Disable preference to set radius of on time selection if user wants to use a different
		 * selection(Rectangular or Circular).
		 */
		
		this.messageText = getContext().getString(R.string.preferences_radius_desc);
		this.setDefault(RADIUS_PIXEL_DEFAULT);
		this.setMax(RADIUS_PIXEL_MAX);
	}

	/**
	 * Set maximum value for seek bar.
	 * 
	 * @param def
	 *            value that will be set as the max for seek bar.
	 */
	private void setMax(double radiusPixelMax){
		this.max = (int) radiusPixelMax;		
	}

	/**
	 * Set default value for seek bar.
	 * 
	 * @param def
	 *            value that will be set as the default for seek bar.
	 */
	private void setDefault(double radiusPixelDefault) {
		this.seekBarCurrentValue = this.preferencesDefault.getInt(this.getKey(),(int) radiusPixelDefault);
	}

	/**
	 * Return value choosed by user in String format.
	 * @param progress
	 * @return
	 */
	@Override
	String getCurrentValueText(int progress) {
		return String.valueOf(progress); //Return value selected
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPref, String key) {
		//if(key.equals("selectionShape"))
		final ListPreference shapesList = (ListPreference)findPreferenceInHierarchy("selectionShape");		
		final boolean enabled = false;
		/*shapesList.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener(){		
		  public boolean onPreferenceChange(Preference preference, Object newValue) {
		    final String val = newValue.toString();
		    int index = shapesList.findIndexOfValue(val);
		    if(index==3)
		      enabled = true;
		    else
		      enabled = false;
		    return true;
		  }
		});*/
		
		if(enabled)
			this.setEnabled(true);
		else 
			this.setEnabled(false);
	}
}