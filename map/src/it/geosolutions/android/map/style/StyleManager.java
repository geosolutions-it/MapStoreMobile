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
 */package it.geosolutions.android.map.style;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import eu.geopaparazzi.spatialite.database.spatial.core.Style;

/**
 * 
 * @author Lorenzo Natali
 * Manages Styles Manager with cache. 
 * Singleton Object that provides style from a StyleObjectProvider and keep styles in cache in write-through mode.
 *
 */
public class StyleManager{
	private static StyleManager styleManager = null;
	private StyleObjectProvider styles = new StyleObjectProvider();

	private  Map<String,AdvancedStyle> styleCache;
	private static HashMap<String, Paint> fillPaints = new HashMap<String, Paint>();
    private static HashMap<String, Paint> strokePaints = new HashMap<String, Paint>();
	private StyleManager() {
		styleCache = new HashMap<String,AdvancedStyle>();
    }

    public static StyleManager getInstance() {
        if (styleManager == null) {
        	styleManager = new StyleManager();
        }
        return styleManager;
    }
    
    /**
     * Resets the style cache
     */     
    public void resetCache(){
    	styleCache = new HashMap<String,AdvancedStyle>();
    }
    
    /**
     * Updates a style 
     * @param style the style to update
     * @throws IOException
     */
    public void updateStyle(AdvancedStyle style) throws IOException{
    	styleCache.put(style.name, style);
    	styles.save(style.name,style);
    }
    
    /**
     * For future use 
     * @param context
     * @param styleDir
     */
    public void init( Context context, File styleDir ) {
        //this.context =context;
        

        
    }
    
    /**
     * Provides a style with a name
     * @param name the name of the style
     * @return
     */
    public  AdvancedStyle getStyle(String name){
 
    	if(styleCache.containsKey(name)){
//    		Log.d("STYLE","style "+name+" in the cache");
    		return styleCache.get(name);
    		
    	}
		AdvancedStyle s = styles.getStyle(name);
		styleCache.put(name, s);
		return s;
		
    	
    }
    
    /**
     * Translate the AdvancedStyleObject in a Paint for FILL
     * @param style
     * @return
     */
    public static Paint getFillPaint4Style( Style style ) {
        Paint paint = fillPaints.get(style.name);
        if (paint == null) {
            paint = new Paint();
            fillPaints.put(style.name, paint);
        }
        paint.reset();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor(style.fillcolor));
        float alpha = style.fillalpha * 255f;
        paint.setAlpha((int) alpha);
        
        AdvancedStyle fill_style = StyleManager.getInstance().getStyle(style.name);
        if(fill_style.dashed)
 	    	paint.setPathEffect(new DashPathEffect(new float[]{15f,10f}, 0));
        
        return paint;
    }

    /**
     * Translate the AdvancedStyleObject in a Paint for STROKE
     * @param style
     * @return
     */
    public static Paint getStrokePaint4Style( Style style ) {
        Paint paint = strokePaints.get(style.name);
        if (paint == null) {
            paint = new Paint();
            strokePaints.put(style.name, paint);
        }
        paint.reset();
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeCap(Cap.ROUND);
        paint.setStrokeJoin(Join.ROUND);
        paint.setColor(Color.parseColor(style.strokecolor));
        float alpha = style.strokealpha * 255f;
        paint.setAlpha((int) alpha);
        paint.setStrokeWidth(style.width);
        
        AdvancedStyle stroke_style = StyleManager.getInstance().getStyle(style.name);
        if(stroke_style.dashed) //Check if the dash is enabled
        	paint.setPathEffect(new DashPathEffect(new float[]{15f,10f}, 0));

        return paint;
    }  
}