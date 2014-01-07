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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import it.geosolutions.android.map.database.SpatialDataSourceManager;
import eu.geopaparazzi.spatialite.database.spatial.core.SpatialVectorTable;
import eu.geopaparazzi.spatialite.util.SpatialiteLibraryConstants;

/**
 * Line Style properties activity.
 * 
 * @author Lorenzo Natali (www.geo-solutions.it)
 */
public class LinesDataPropertiesActivity extends BaseStyleActivity {
	
	protected SpatialVectorTable spatialTable;
	protected Spinner widthSpinner;
	protected Spinner alphaSpinner;
    private EditText decimationText;
    protected Spinner dashSpinner;
    private TextView colorStroke;
    private Integer colorSelStroke;

    public void onCreate( Bundle icicle ) {
        super.onCreate(icicle);

        setContentView(R.layout.data_line_properties);
        final AdvancedStyle style = getStyle();
        setBaseStyleProperties(style);
		colorSelStroke = Color.parseColor(style.strokecolor); //Set default color by the strokecolor attribute of style.
        
        //color
        colorStroke = (TextView) findViewById(R.id.textView1);
        colorStroke.setOnClickListener(new OnClickListener(){        

			@Override
			public void onClick(View v) {
				new Color();
				//Show a dialog with color picker
				colorPicker(colorSelStroke);				
			}
		});
				
        //width
        String width = String.valueOf((int) style.width);
        widthSpinner = (Spinner) findViewById(R.id.width_spinner);
        int count = widthSpinner.getCount();
        for( int i = 0; i < count; i++ ) {
            if (widthSpinner.getItemAtPosition(i).equals(width)) {
                widthSpinner.setSelection(i);
                break;
            }
        }
        //alpha
        String alpha = String.valueOf((int) (style.strokealpha * 100f));
        alphaSpinner = (Spinner) findViewById(R.id.alpha_spinner);
        count = alphaSpinner.getCount();
        for( int i = 0; i < count; i++ ) {
            if (alphaSpinner.getItemAtPosition(i).equals(alpha)) {
                alphaSpinner.setSelection(i);
                break;
            }
        }
        
        //decimation
        String decimation = String.valueOf(style.decimationFactor);
        decimationText = (EditText) findViewById(R.id.decimation_text);
        decimationText.setText(decimation);
        
        String dash = String.valueOf(style.dashed);
        if(dash.equals("true")) dash = "Ok";
        else dash = "No";
        dashSpinner = (Spinner) findViewById(R.id.dash_spinner);
        count = dashSpinner.getCount();
        for( int i = 0; i < count; i++ ) {
            if (dashSpinner.getItemAtPosition(i).equals(dash)) {
                dashSpinner.setSelection(i);
                break;
            }
        }
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
    
    public void onOkClick( View view ) {
    	
        AdvancedStyle style =getStyle();       

    	//stroke color
        String color = String.format("#%06X", (0xFFFFFF & colorSelStroke)); //Convert from hex to #RRGGBB format      
        style.strokecolor = color;
        
    	//width
        String widthString = (String) widthSpinner.getSelectedItem();
        float width = 1f;
        try {
            width = Float.parseFloat(widthString);
        } catch (NumberFormatException e) {
        	Log.e("STYLE","input parse error");
        }
        style.width = width;
        
        //alpha
        String alphaString = (String) alphaSpinner.getSelectedItem();
        float alpha100 = Float.parseFloat(alphaString);
        style.strokealpha = alpha100 / 100f;
        
        //decimation
        String decimationString = decimationText.getText().toString();
        float decimation = 0.0f;
        try {
            decimation = Float.parseFloat(decimationString);
        } catch (NumberFormatException e) {
        	
        	Log.e("STYLE","input parse error");
        }
        style.decimationFactor = decimation;
        
        String dash = (String) dashSpinner.getSelectedItem();
        String[] array = this.getResources().getStringArray(R.array.array_dashes);
    	if(dash.equals(array[1]))
    		style.dashed = true;
    	else
    		style.dashed = false;
        
        updateStyle(style);
    }

    public void onCancelClick( View view ) {
        finish();
    }
    
    public void colorPicker(int color_sel){
    	AmbilWarnaDialog dialog = new AmbilWarnaDialog(this, color_sel, new OnAmbilWarnaListener(){
			@Override
			public void onCancel(AmbilWarnaDialog dialog){}

			@Override
			public void onOk(AmbilWarnaDialog dialog, int color){ //Return color selected by user
				colorSelStroke = color;
			} 		
    	});
    	
    	dialog.show();
    }
}
