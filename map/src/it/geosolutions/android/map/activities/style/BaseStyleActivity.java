/*
 * GeoSolutions Android Map Library - Digital field mapping on Android based devices
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
package it.geosolutions.android.map.activities.style;

import it.geosolutions.android.map.R;
import it.geosolutions.android.map.style.AdvancedStyle;
import it.geosolutions.android.map.style.StyleManager;

import java.io.IOException;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Spinner;
/**
 * 
 * @author Admin
 * Base Activity for Styles. Contains common methods and attributes
 * comon to all the styles;
 * The Layout MUST jave a the Components used in this class 
 *  * minzoom_spinner
 *  * maxzoom_spinner
 */

enum who {Fill, Stroke}; //Used by color picker to distinguish between color for stroke or fill.

public abstract class BaseStyleActivity extends Activity {
	protected Spinner minZoom;
	protected Spinner maxZoom;
	
	public void onCreate(Bundle icicle){
		super.onCreate(icicle);
		super.setTitle(getStyle().name);
		
       
	}
	protected void setBaseStyleProperties(AdvancedStyle style) {
		int count=0;
		//min zoom
        String min_zoom = String.valueOf((int) (style.minZoom ));
        minZoom = (Spinner) findViewById(R.id.minzoom_spinner);
        count = minZoom.getCount();
        for( int i = 0; i < count; i++ ) {
            if (minZoom.getItemAtPosition(i).equals(min_zoom)) {
            	minZoom.setSelection(i);
                break;
            }
        }
      //max zoom
        String max_zoom = String.valueOf((int) (style.maxZoom ));
        maxZoom = (Spinner) findViewById(R.id.maxzoom_spinner);
        count = maxZoom.getCount();
        for( int i = 0; i < count; i++ ) {
            if (maxZoom.getItemAtPosition(i).equals(max_zoom)) {
            	maxZoom.setSelection(i);
                break;
            }
        }
		
	}
	
	protected void updateBaseStyleProperties(AdvancedStyle style){
		
        byte minZoom=0;
        try {
        	minZoom = Byte.parseByte( this.minZoom.getSelectedItem().toString() );
        } catch (NumberFormatException e) {
        	Log.e("STYLE","input parse error");	
        }
        style.minZoom=minZoom;
        
      //max zoom
        byte maxZoom=127;
        try {
        	maxZoom = Byte.parseByte( this.maxZoom.getSelectedItem().toString() );
        } catch (NumberFormatException e) {
        	Log.e("STYLE","input parse error");
        }
        style.maxZoom=maxZoom;
	}
	
	protected void updateStyle(AdvancedStyle style){
		updateBaseStyleProperties(style);
		try {
			StyleManager.getInstance().updateStyle(style);
			finish();
		} catch (IOException e) {
			Log.v("STYLE","error saving the style");
		}
	}
	protected abstract AdvancedStyle getStyle();
}
