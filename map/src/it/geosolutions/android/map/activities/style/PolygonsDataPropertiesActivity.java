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
import it.geosolutions.android.map.database.SpatialDataSourceManager;
import it.geosolutions.android.map.style.AdvancedStyle;
import it.geosolutions.android.map.style.StyleManager;
import jsqlite.Exception;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import eu.geopaparazzi.spatialite.database.spatial.core.SpatialVectorTable;
import eu.geopaparazzi.spatialite.util.SpatialiteLibraryConstants;

/**
 * Polygon Style properties activity.
 * 
 * @author Lorenzo Natali (www.geo-solutions.it)
 */

public class PolygonsDataPropertiesActivity extends BaseStyleActivity {
    private SpatialVectorTable spatialTable;
    private Spinner widthSpinner;
    private Spinner alphaSpinner;
    private Spinner fillAlphaSpinner;
    private EditText decimationText;
    private Spinner dashSpinner;
    private TextView colorStroke;
    private TextView colorFill;
    private Integer colorSelStroke;
    private Integer colorSelFill;
    private String[] array;

    public void onCreate( Bundle icicle ) {
        super.onCreate(icicle);
        setContentView(R.layout.data_polygon_properties);
        final AdvancedStyle style = getStyle();
        setBaseStyleProperties(style);
      
        //Set default color for fill and stroke by attribute of style.
		colorSelStroke = Color.parseColor(style.strokecolor); 
		colorSelFill = Color.parseColor(style.fillcolor); 
       
        colorStroke = (TextView) findViewById(R.id.textView1);
        colorStroke.setOnClickListener(new OnClickListener(){        

			@Override
			public void onClick(View v) {
				//Show a dialog with color picker
				colorPicker(colorSelStroke,who.Stroke);				
			}
		});
        
        String width = String.valueOf((int) style.width);
        widthSpinner = (Spinner) findViewById(R.id.width_spinner);
        int count = widthSpinner.getCount();
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
				colorPicker(colorSelFill,who.Fill);				
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

        String decimation = String.valueOf(style.decimationFactor);
        decimationText = (EditText) findViewById(R.id.decimation_text);
        decimationText.setText(decimation);
     
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
    	
    	String color = String.format("#%06X", (0xFFFFFF & colorSelStroke)); //Convert from hex to #RRGGBB format      
        style.strokecolor = color;

        String widthString = (String) widthSpinner.getSelectedItem();
        float width = Float.parseFloat(widthString);
        style.width = width;

        String alphaString = (String) alphaSpinner.getSelectedItem();
        float alpha100 = Float.parseFloat(alphaString);
        style.strokealpha = alpha100 / 100f;

        color = String.format("#%06X", (0xFFFFFF & colorSelFill)); //Convert from hex to #RRGGBB format      
        style.fillcolor = color;

        String fillAlphaString = (String) fillAlphaSpinner.getSelectedItem();
        float fillAlpha100 = Float.parseFloat(fillAlphaString);
        style.fillalpha = fillAlpha100 / 100f;

        String decimationString = decimationText.getText().toString();
        float decimation = 0.0f;
        try {
            decimation = Float.parseFloat(decimationString);
        } catch (java.lang.Exception e) {
        }
        style.decimationFactor = decimation;
        
        String dashed = (String) dashSpinner.getSelectedItem();
    	if(dashed.equals(array[1]))
    		style.dashed = true;
    	else
    		style.dashed = false;
        
        updateStyle(style);
    }

    public void onCancelClick( View view ) {
        finish();
    }

	@Override
    protected AdvancedStyle getStyle(){
    	Bundle extras = getIntent().getExtras();
        String tableName = extras.getString(SpatialiteLibraryConstants.PREFS_KEY_TEXT);
        try {
            spatialTable = SpatialDataSourceManager.getInstance().getVectorTableByName(tableName);
        } catch (Exception e) {
            Log.e("STYLE","unable to retrive table" + tableName);
        }
        AdvancedStyle style =StyleManager.getInstance().getStyle(spatialTable.getName());
        return style;
    } 
	
	public void colorPicker(int color_sel, final who sel){
		AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, color_sel,new OnAmbilWarnaListener(){
			@Override
			public void onCancel(AmbilWarnaDialog dialog){}

			@Override
			public void onOk(AmbilWarnaDialog dialog, int color){ //Return color selected by user
				if(sel==who.Fill) {
					colorSelFill = color;
				}
				else {
					colorSelStroke = color;
				}	
			}
    	});
    	
    	dialog.show();
    }
}