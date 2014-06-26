/*
 * GeoSolutions - MapstoreMobile - GeoSpatial Framework on Android based devices
 * Copyright (C) 2014  GeoSolutions (www.geo-solutions.it)
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
package it.geosolutions.geocollect.android.core.mission.utils;

import it.geosolutions.android.map.wfs.WFSGeoJsonFeatureLoader;
import it.geosolutions.android.map.wfs.geojson.feature.Feature;
import it.geosolutions.geocollect.android.core.R;
import it.geosolutions.geocollect.model.config.MissionTemplate;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jsqlite.Database;

import android.content.Context;
import android.support.v4.content.Loader;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.gson.Gson;

/**
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 * Utilities class for Mission
 */
public class MissionUtils {
	/**
	 * The regex to parse the tags in the json
	 */
	private static final String TAG_REGEX ="\\$\\{(.*?)\\}";
	private static final Pattern pattern = Pattern.compile(TAG_REGEX);
	/**
	 * Create a loader getting the source of the mission
	 * @param missionTemplate
	 * @param page
	 * @param pagesize
	 * @return
	 */
	public static Loader<List<Feature>> createMissionLoader(
			MissionTemplate missionTemplate,SherlockFragmentActivity activity, int page, int pagesize, Database db) {
		
		WFSGeoJsonFeatureLoader wfsl = new WFSGeoJsonFeatureLoader(activity,missionTemplate.source.URL,missionTemplate.source.baseParams, missionTemplate.source.typeName,page*pagesize+1,pagesize);
		
		if(db == null){
			// No database provided, load only online data
			return wfsl;
		}
		
		return new SQLiteCascadeFeatureLoader(
				activity, 
				wfsl, 
				db, 
				missionTemplate.source.localSourceStore, 
				missionTemplate.source.localFormStore, 
				missionTemplate.source.orderingField);
	}
	
	/**
	 * Provide the default template as configured in the raw folder.
	 * @param c
	 * @return
	 */
	public static MissionTemplate getDefaultTemplate(Context c){
		InputStream inputStream = c.getResources().openRawResource(R.raw.defaulttemplate);
        if (inputStream != null) {
            final Gson gson = new Gson();
            final BufferedReader reader =
                new BufferedReader(new InputStreamReader(inputStream));
            return gson.fromJson(reader, MissionTemplate.class);
        }
        
        return null;
	}
	
	/**
	 * Parse the string to get the tags between {} (brackets) 
	 * @param toParse the string to parse
	 * @return the list of brackets
	 */
	public static List<String> getTags(String toParse){
		if(toParse==null){
			return null;
		}
		Matcher matcher = pattern.matcher(toParse);
		//gets the 
		while(matcher.find()){
			List<String> tags = new ArrayList<String>();

		    int pos = -1;
		    while (matcher.find(pos+1)){
		        pos = matcher.start();
		        tags.add(matcher.group(1));
		    }
		    return tags;
		}
		return null;
	}
	
	
	
}
