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

import yuku.ambilwarna.AmbilWarnaDialog;
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener;
import it.geosolutions.android.map.R;
import it.geosolutions.android.map.style.AdvancedStyle;
import it.geosolutions.android.map.style.StyleManager;
import jsqlite.Exception;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.Spinner;
import android.widget.TextView;
import it.geosolutions.android.map.database.SpatialDataSourceManager;
import eu.geopaparazzi.spatialite.database.spatial.core.SpatialVectorTable;
import eu.geopaparazzi.spatialite.util.SpatialiteLibraryConstants;

/**
 * Points Data properties activity.
 * 
 * @author Lorenzo Natali (www.geo-solutions.it)
 */

public class PointsDataPropertiesActivity extends BaseStyleActivity /*implements OnItemSelectedListener*/ {
    private SpatialVectorTable spatialTable;
    private Spinner shapesSpinner;
    private Spinner sizeSpinner;
    private Spinner widthSpinner;
    private Spinner alphaSpinner;
    private Spinner fillAlphaSpinner;
    private AdvancedStyle style;
    private Spinner dashSpinner;
    private TextView colorStroke;
    private TextView colorFill;
    private Integer colorSelFill;
    private Integer colorSelStroke;
    private String[] array;


    public void onCreate( Bundle icicle ) {
        super.onCreate(icicle);

        setContentView(R.layout.map_data_point_properties);
        style = getStyle();
        setBaseStyleProperties(style);
        
        //Set default color for fill and stroke color by attribute of style.        
		colorSelStroke = Color.parseColor(style.strokecolor); 
		colorSelFill = Color.parseColor(style.fillcolor); 
   
        shapesSpinner = (Spinner) findViewById(R.id.shape_spinner);
        String shape = style.shape;
        int count = shapesSpinner.getCount();
        for( int i = 0; i < count; i++ ) {
            if (shapesSpinner.getItemAtPosition(i).equals(shape)) {
                shapesSpinner.setSelection(i);
                break;
            }
        }
        String size = String.valueOf((int) style.size);
        sizeSpinner = (Spinner) findViewById(R.id.size_spinner);
        count = sizeSpinner.getCount();
        for( int i = 0; i < count; i++ ) {
            if (sizeSpinner.getItemAtPosition(i).equals(size)) {
                sizeSpinner.setSelection(i);
                break;
            }
        }
        
        colorStroke = (TextView) findViewById(R.id.textView1);
        colorStroke.setOnClickListener(new OnClickListener(){        

			@Override
			public void onClick(View v) {
				//Show a dialog with color picker
				colorPicker(colorSelStroke, who.Stroke);				
			}
		});
                
        String width = String.valueOf((int) style.width);
        widthSpinner = (Spinner) findViewById(R.id.width_spinner);
        count = widthSpinner.getCount();
        for( int i = 0; i < count; i++ ) {
            if (widthSpinner.getItemAtPosition(i).equals(width)) {
                widthSpinner.setSelection(i);
                break;
            }
        }
        String alpha = String.valueOf((int) (style.strokealpha * 100f));
        alphaSpinner = (Spinner) findViewById(R.id.alpha_spinner);
        count = alphaSpinner.getCount();
        for( int i = 0; i < count; i++ ) {
            if (alphaSpinner.getItemAtPosition(i).equals(alpha)) {
                alphaSpinner.setSelection(i);
                break;
            }
        }

        colorFill = (TextView) findViewById(R.id.textView4);
        colorFill.setOnClickListener(new OnClickListener(){        

			@Override
			public void onClick(View v) {
				//Show a dialog with color picker
				colorPicker(colorSelFill, who.Fill);				
			}
		});
        
        String fillAlpha = String.valueOf((int) (style.fillalpha * 100f));
        fillAlphaSpinner = (Spinner) findViewById(R.id.fill_alpha_spinner);
        count = fillAlphaSpinner.getCount();
        for( int i = 0; i < count; i++ ) {
            if (fillAlphaSpinner.getItemAtPosition(i).equals(fillAlpha)) {
                fillAlphaSpinner.setSelection(i);
                break;
            }
        }
        
        boolean dashed = style.dashed;
        array = this.getResources().getStringArray(R.array.array_dashes);
       
        dashSpinner = (Spinner) findViewById(R.id.dash_spinner);        
        if (dashSpinner.getItemAtPosition(0).equals(array[0]) && !dashed)
            dashSpinner.setSelection(0);
        else
        	dashSpinner.setSelection(1); 
        
    }

    public void onOkClick( View view ) {
    	AdvancedStyle style =getStyle(); 
    	String alphaString, sizeString; 
    	
    	//Shape of point
        String shape = (String) shapesSpinner.getSelectedItem();
        style.shape = shape;
        
        //Size of point
        sizeString = (String) sizeSpinner.getSelectedItem();
        try {
        	style.size = Float.parseFloat(sizeString);
        } catch (NumberFormatException e) {
        	Log.e("STYLE","input parse error");
        }     
        
        //Stroke color 
        String color = String.format("#%06X", (0xFFFFFF & colorSelStroke)); //Convert from hex to #RRGGBB format      
        style.strokecolor = color;
        
        //Width of stroke
        String widthString = (String) widthSpinner.getSelectedItem();
        try {
        	style.width = Float.parseFloat(widthString);
        } catch (NumberFormatException e) {
        	Log.e("STYLE","input parse error");
        }     
      
        
        //Stroke alpha
        alphaString = (String) alphaSpinner.getSelectedItem();
        try{
        	 style.strokealpha = (Float.parseFloat(alphaString))/100f;
        }catch (NumberFormatException e) {
        	Log.e("STYLE","input parse error");
        }            
        
        //Dash of stroke
        String dashed = (String) dashSpinner.getSelectedItem();
    	if(dashed.equals(array[1]))
    		style.dashed = true;
    	else
    		style.dashed = false;
    	
    	//Fill color
    	color = String.format("#%06X", (0xFFFFFF & colorSelFill)); //Convert from hex to #RRGGBB format      
        style.fillcolor = color;
        
        //alpha spinner
        alphaString = (String) fillAlphaSpinner.getSelectedItem();      
        try{
        	 style.fillalpha = (Float.parseFloat(alphaString))/100f;
        }catch (NumberFormatException e) {
        	Log.e("STYLE","input parse error");
        }       
        
    	updateStyle(style);
    }
    
    @Override
    protected AdvancedStyle getStyle(){
    	if(style!=null){
    		return style;
    	}
    	Bundle extras = getIntent().getExtras();
        String tableName = extras.getString(SpatialiteLibraryConstants.PREFS_KEY_TEXT);
        try {
            spatialTable = SpatialDataSourceManager.getInstance().getVectorTableByName(tableName);
        } catch (Exception e) {
            Log.e("STYLE","unable to retrive table" + tableName);
        }
        
        style =StyleManager.getInstance().getStyle(spatialTable.getName());
        
        return style;
    } 
    
    public void onCancelClick( View view ) {
        finish();
    }

    /**
     * Show a color picker dialog, permit to user to set fill and stroke color.
     * @param color_sel
     * @param sel
     */
    public void colorPicker(int color_sel, final who sel){
		AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, color_sel,new OnAmbilWarnaListener(){
			@Override
			public void onCancel(AmbilWarnaDialog dialog){}

			@Override
			public void onOk(AmbilWarnaDialog dialog, int color){ //Return color selected by user
				if(sel==who.Fill)
					colorSelFill = color;
				else
					colorSelStroke = color;
			} 		
    	});
    	
    	dialog.show();
    }
}
