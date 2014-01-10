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
 * Preferences class to set the property about style of stroke.
 * @author Jacopo Pianigiani (jacopo.pianigiani85@gmail.com)
 */
public class StyleStrokePreference extends SeekBarPreference {
	
   /**
    * The default value for transparency of stroke
    */
   private static final int STROKE_ALPHA_DEFAULT = 50;
    
   /**
    * The maximum value for transparency of stroke
    */
   private static final int STROKE_ALPHA_MAX = 100;
    
    /**
    * The default value for width of stroke
    */
   private static final int STROKE_WIDTH_DEFAULT = 3;
   
   /**
    * The maximum value for width of stroke
    */
   private static final int STROKE_WIDTH_MAX = 30;
   
   /**
    * The default value for spaces between every stamp component stroke
    */
   private static final float STROKE_SPACES_DEFAULT = 10f;
    
   /**
     * The default value for dimension of every shape of stroke
    */
   private static final float STROKE_SHAPE_DIMENSION_DEFAULT = 15f;
    
   /**
    * The maximum value for spaces between shapes and dimension of shapes.
    */
   private static final float STROKE_MAX = 100f; 
	
     /**
	 * Construct a new style stroke preference seek bar.
	 * 
	 * @param context
	 *            the context activity.
	 * @param attrs
	 *            A set of attributes (currently ignored).
	 */
	public StyleStrokePreference(Context context, AttributeSet attrs){
		super(context, attrs);		
		
		//Class distinguishes between preference on the basis of value returned by the getKey() method
		String s1 = "StrokeSpaces", s2 = "StrokeDim", s3 = "StrokeAlpha", s4="StrokeWidth";
		
		if(this.getKey().equals(s1)){
			this.messageText = getContext().getString(R.string.preferences_stroke_spaces_desc);
			this.setDefault(STROKE_SPACES_DEFAULT);
			this.setMax(STROKE_MAX);
		}
		else if(this.getKey().equals(s2)){
			this.messageText = getContext().getString(R.string.preferences_stroke_shape_dimension_desc);
			this.setDefault(STROKE_SHAPE_DIMENSION_DEFAULT);
			this.setMax(STROKE_MAX);
		}
		else if(this.getKey().equals(s3)){
			this.messageText = getContext().getString(R.string.preferences_stroke_alpha);
			this.setDefault(STROKE_ALPHA_DEFAULT);
			this.setMax(STROKE_ALPHA_MAX);		
		}
		else if(this.getKey().equals(s4)){
			this.messageText = getContext().getString(R.string.preferences_stroke_width);
			this.setDefault(STROKE_WIDTH_DEFAULT);
			this.setMax(STROKE_WIDTH_MAX);		
		}
	}
	
	/**
	 * Set default value for seek bar(For float value).
	 * 
	 * @param def
	 *            value that will be set as the default for seek bar.
	 */
	public void setDefault(float def){
		this.seekBarCurrentValue = this.preferencesDefault.getInt(this.getKey(),(int) def);
	}
	
	/**
	 * Set maximum value for seek bar(For float value).
	 * 
	 * @param def
	 *            value that will be set as the max for seek bar.
	 */
	public void setMax(float max){
		this.max = (int) max;
	}
	
	/**
	 * Set default value for seek bar(For integer value).
	 * 
	 * @param def
	 *            value that will be set as the default for seek bar.
	 */
	public void setDefault(int def){
		this.seekBarCurrentValue = this.preferencesDefault.getInt(this.getKey(),def);
	}
	
	/**
	 * Set maximum value for seek bar(For integer value).
	 * 
	 * @param def
	 *            value that will be set as the max for seek bar.
	 */
	public void setMax(int max){
		this.max = max;
	}
	
	/* (non-Javadoc)
	 * @see it.geosolutions.android.map.preferences.SeekBarPreference#getCurrentValueText(int)
	 */
	@Override
	String getCurrentValueText(int progress) {
		return String.valueOf(progress); //Return value selected
	}
}