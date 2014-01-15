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
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import android.preference.PreferenceManager;
import android.util.AttributeSet;

/**
 * Preferences class to set the radius of on time selection.
 * @author Jacopo Pianigiani (jacopo.pianigiani85@gmail.com).
 */
public class SetRadiusPreference extends SeekBarPreference implements OnSharedPreferenceChangeListener{
	
	/**
	* The default value for value(In pixel) of radius of an on time selection.
	*/
	private static final double RADIUS_METERS_DEFAULT = 10.0;
	
	/**
	* The maximum value for radius of on time selection.
	*/
	private static final double RADIUS_METERS_MAX = 300.0;
	
	private String[] array;
	
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
		array = context.getResources().getStringArray(R.array.preferences_selection_shape);
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
		pref.registerOnSharedPreferenceChangeListener(this);
		
		//Check if one point selection has been selected otherwise disable this 
		if(pref.getString("selectionShape", "").equals(array[2]))
			this.setEnabled(true);
		else
			this.setEnabled(false);
		
		//Set message, min and max of the seek bar
		this.messageText = getContext().getString(R.string.preferences_radius_desc);
		this.setDefault(RADIUS_METERS_DEFAULT);
		this.setMax(RADIUS_METERS_MAX);
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
	 * Return value selected by user in String format.
	 * @param progress
	 * @return
	 */
	@Override
	String getCurrentValueText(int progress) {
		return String.valueOf(progress); //Return value selected
	}
	
	/**
	 * Listener which detect a changing on selectionShape preference, depending on it enable or
	 * disable this preference.
	 * @param sharedPref
	 * @param key 
	 */	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPref, String key) {
		if(key.equals("selectionShape") &&
			!sharedPref.getString("selectionShape", array[0]).equals(array[2]))
				this.setEnabled(false);		
		else 
			this.setEnabled(true);
	}
}