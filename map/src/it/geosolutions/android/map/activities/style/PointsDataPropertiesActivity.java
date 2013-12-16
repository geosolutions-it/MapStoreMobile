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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
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

public class PointsDataPropertiesActivity extends BaseStyleActivity implements OnItemSelectedListener {
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


    public void onCreate( Bundle icicle ) {
        super.onCreate(icicle);

        setContentView(R.layout.data_point_properties);
        style = getStyle();
        setBaseStyleProperties(style);
        
        //Set default color for fill and stroke color by attribute of style.        
		colorSelStroke = Color.parseColor(style.strokecolor); 
		colorSelFill = Color.parseColor(style.fillcolor); 
   
        shapesSpinner = (Spinner) findViewById(R.id.shape_spinner);
        shapesSpinner.setOnItemSelectedListener(this);
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
        sizeSpinner.setOnItemSelectedListener(this);
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
        widthSpinner.setOnItemSelectedListener(this);
        count = widthSpinner.getCount();
        for( int i = 0; i < count; i++ ) {
            if (widthSpinner.getItemAtPosition(i).equals(width)) {
                widthSpinner.setSelection(i);
                break;
            }
        }
        String alpha = String.valueOf((int) (style.strokealpha * 100f));
        alphaSpinner = (Spinner) findViewById(R.id.alpha_spinner);
        alphaSpinner.setOnItemSelectedListener(this);
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
        fillAlphaSpinner.setOnItemSelectedListener(this);
        count = fillAlphaSpinner.getCount();
        for( int i = 0; i < count; i++ ) {
            if (fillAlphaSpinner.getItemAtPosition(i).equals(fillAlpha)) {
                fillAlphaSpinner.setSelection(i);
                break;
            }
        }
        
       String dashed = String.valueOf(style.dashed);
       dashSpinner = (Spinner)findViewById(R.id.dash_spinner);
       dashSpinner.setOnItemSelectedListener(this);
       count = dashSpinner.getCount();
       for( int i = 0; i < count; i++ ) {
           if (dashSpinner.getItemAtPosition(i).equals(dashed)) {
               dashSpinner.setSelection(i);
               break;
           }
       }
    }

    public void onOkClick( View view ) {
    	
        updateStyle(getStyle());
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

    @Override
    public void onItemSelected( AdapterView< ? > callingView, View view, int arg2, long arg3 ) {
    	style = getStyle();
        if(callingView.equals(colorStroke)){
            String color = String.format("#%06X", (0xFFFFFF & colorSelStroke)); //Convert from hex to #RRGGBB format      
            style.strokecolor = color;
        } else if (callingView.equals(sizeSpinner)) {
            String sizeString = (String) sizeSpinner.getSelectedItem();
            float size = Float.parseFloat(sizeString);
            style.size = size;
        } else if (callingView.equals(widthSpinner)) {
            String widthString = (String) widthSpinner.getSelectedItem();
            float width = Float.parseFloat(widthString);
            style.width = width;
        } else if (callingView.equals(alphaSpinner)) {
            String alphaString = (String) alphaSpinner.getSelectedItem();
            float alpha100 = Float.parseFloat(alphaString);
            style.strokealpha = alpha100 / 100f;
        } else if (callingView.equals(colorFill)){
        	String color = String.format("#%06X", (0xFFFFFF & colorSelFill)); //Convert from hex to #RRGGBB format      
            style.fillcolor = color;
        } else if (callingView.equals(fillAlphaSpinner)) {
            String alphaString = (String) fillAlphaSpinner.getSelectedItem();
            float alpha100 = Float.parseFloat(alphaString);
            style.fillalpha = alpha100 / 100f;
        } else if (callingView.equals(shapesSpinner)) {
            String color = (String) shapesSpinner.getSelectedItem();
            style.shape = color;
        } else if(callingView.equals(dashSpinner)){
        	String dash = (String) dashSpinner.getSelectedItem();
        	if(dash.equals("Ok"))
        		style.dashed = true;
        	else
        		style.dashed = false;
        }
    }

    @Override
    public void onNothingSelected( AdapterView< ? > arg0 ) {
    }

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
