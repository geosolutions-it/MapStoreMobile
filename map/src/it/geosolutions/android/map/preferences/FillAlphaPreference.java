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
import android.util.AttributeSet;

/**
 * Preference class to setting value of transparency of selection on map.
 * @author Jacopo Pianigiani (jacopo.pianigiani85@gmail.com)
 */
public class FillAlphaPreference extends SeekBarPreference {
	
	 /**
     * The default value for transparency of fill
     */
    private static final int FILL_ALPHA_DEFAULT = 50;
    
    /**
     * The maximum value for transparency of fill
     */
    private static final int FILL_ALPHA_MAX = 100;
    
	/**
	 * Construct a new fill alpha preference seek bar.
	 * 
	 * @param context
	 *            the context activity.
	 * @param attrs
	 *            A set of attributes (currently ignored).
	 */
	public FillAlphaPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		// define the text message
		this.messageText = getContext().getString(R.string.preferences_fill_alpha);
		
		// define the current and maximum value of the seek bar
		this.seekBarCurrentValue = this.preferencesDefault.getInt(this.getKey(),FILL_ALPHA_DEFAULT);
		this.max = FILL_ALPHA_MAX;
	}

	@Override
	String getCurrentValueText(int progress) {
		return String.valueOf(progress);
	}
}