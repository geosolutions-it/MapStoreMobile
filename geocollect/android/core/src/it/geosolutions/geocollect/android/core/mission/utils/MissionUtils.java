/*
 * GeoSolutions - GeoCollect
 * Copyright (C) 2014 - 2015  GeoSolutions (www.geo-solutions.it)
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

import it.geosolutions.android.map.utils.MapFilesProvider;
import it.geosolutions.android.map.wfs.WFSGeoJsonFeatureLoader;
import it.geosolutions.android.map.wfs.geojson.GeoJson;
import it.geosolutions.android.map.wfs.geojson.feature.Feature;
import it.geosolutions.geocollect.android.core.BuildConfig;
import it.geosolutions.geocollect.android.core.R;
import it.geosolutions.geocollect.android.core.login.LoginActivity;
import it.geosolutions.geocollect.android.core.mission.Mission;
import it.geosolutions.geocollect.android.core.mission.MissionFeature;
import it.geosolutions.geocollect.android.core.mission.PendingMissionListActivity;
import it.geosolutions.geocollect.model.config.MissionTemplate;
import it.geosolutions.geocollect.model.source.XDataType;
import it.geosolutions.geocollect.model.viewmodel.Field;
import it.geosolutions.geocollect.model.viewmodel.Form;
import it.geosolutions.geocollect.model.viewmodel.Page;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jsqlite.Database;
import jsqlite.Exception;
import jsqlite.Stmt;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.gson.Gson;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.WKBReader;

/**
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 * Utilities class for Mission
 */
public class MissionUtils {
	
	/**
	 * TAG for Logging
	 */
	private static String TAG = "MissionUtils";
	
	/**
	 * The regex to parse the tags in the json
	 */
	private static final String TAG_REGEX ="\\$\\{(.*?)\\}";
	private static final Pattern pattern = Pattern.compile(TAG_REGEX);
	
	/**
	 * Patterns to replace in various templates
	 */
	public static String HEX_COLOR_PATTERN = "#XXXXXX";
	public static String TABLE_NAME_PATTERN = "XXNAMEXX";
    
	/**
	 * Feature GCID string name 
	 */
	public static final String GCID_STRING = "GCID";
	
	/**
	 * Create a loader getting the source of the mission
	 * @param missionTemplate
	 * @param page
	 * @param pagesize
	 * @return
	 */
	public static Loader<List<MissionFeature>> createMissionLoader(
			MissionTemplate missionTemplate,SherlockFragmentActivity activity, int page, int pagesize, Database db) {
		
		// Retrieve saved credentials for BasicAuth
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
		String username = prefs.getString(LoginActivity.PREFS_USER_EMAIL, null);
		String password = prefs.getString(LoginActivity.PREFS_PASSWORD, null);

		WFSGeoJsonFeatureLoader wfsl = new WFSGeoJsonFeatureLoader(
				activity,
				missionTemplate.schema_seg.URL,
				missionTemplate.schema_seg.baseParams,
				missionTemplate.schema_seg.typeName,
				page*pagesize+1,
				pagesize,
				username,
				password);
	
        return new SQLiteCascadeFeatureLoader(
            activity, 
            wfsl, 
            db, 
            missionTemplate.schema_seg.localSourceStore, 
            missionTemplate.schema_sop.localFormStore, 
            missionTemplate.schema_seg.orderingField != null ? 
                    missionTemplate.schema_seg.orderingField : 
                    missionTemplate.orderingField,
            missionTemplate.priorityField,
            missionTemplate.priorityValuesColors);
	}
	
	/**
	 * Provide the default template as configured in the raw folder.
	 * @param c
	 * @return
	 */
	public static MissionTemplate getDefaultTemplate(Context c){
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
    	boolean usesDownloaded = prefs.getBoolean(PendingMissionListActivity.PREFS_USES_DOWNLOADED_TEMPLATE, false);
    	
    	if(usesDownloaded){
    		
    		int index = prefs.getInt(PendingMissionListActivity.PREFS_DOWNLOADED_TEMPLATE_INDEX, 0);
    		
    		ArrayList<MissionTemplate> templates = PersistenceUtils.loadSavedTemplates(c);
    		
            String selectedTemplateId = prefs.getString(PendingMissionListActivity.PREFS_SELECTED_TEMPLATE_ID, null);
            if(selectedTemplateId != null && !selectedTemplateId.isEmpty()){
                for(MissionTemplate t : templates){
                    if(t.id != null && t.id.equalsIgnoreCase(selectedTemplateId)){
                        return t;
                    }
                }
            }

    		if(index >= templates.size()){
    		    index = templates.size()-1;
    		}
    		return templates.get(index);
    		
    	}else{
    		
    		InputStream inputStream = c.getResources().openRawResource(R.raw.defaulttemplate);
    		if (inputStream != null) {
    			final Gson gson = new Gson();
    			final BufferedReader reader =
    					new BufferedReader(new InputStreamReader(inputStream));
    			// TODO: Catch JsonSyntaxException when template is malformed
    			return gson.fromJson(reader, MissionTemplate.class);
    		}
    	}
		
        
        return null;
	} 

	/**
	 * converts the Json string to an inputstream and parses it using gson
	 * @param json String
	 * @return the parsed template
	 */
	public static MissionTemplate getTemplateFromJSON(final String json){

		final Gson gson = new Gson();
		final BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(json.getBytes())));

		return gson.fromJson(reader, MissionTemplate.class);
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
	
	/**
	 * @param dataMapping
	 * @return
	 */
	public static String generateJsonString(Map<String, String> dataMapping, Mission m) {
		Feature f = PersistenceUtils.loadFeatureById(m);
		GeoJson gson = new GeoJson();
		String c = gson.toJson( f);
		try {
			return new String(c.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// return the original string
			return c;
		}
	}
	
	/**
	 * checks if mandatory fields were compiled using the mandatory field of the defaulttemplate
	 * 
	 * @param form the form to check
	 * @param id to refer to
	 * @param db to read from
	 * @param tableName to refer to
	 * @return ArrayList with the fields label which was not compiled or an empty list if all was done
	 */
	public static ArrayList<String> checkIfAllMandatoryFieldsAreSatisfied(final Form form,final String id,final Database db,final String tableName) {
		
		ArrayList<String> missingEntries = new ArrayList<String>();
				
		Stmt st = null;
		//find mandatory fields
		ArrayList<Pair<String,String>> missingFieldIDs = new ArrayList<Pair<String,String>>();
		for(Page page : form.pages){
			for(Field f : page.fields){
				if(f.mandatory){				
					missingFieldIDs.add(new Pair<String, String>(f.fieldId, f.label));
				}
			}
		}
		//if no mandatory fields no need to continue
		if(missingFieldIDs.isEmpty()){
			return missingEntries;
		}
		
		//create selection
		String selection = "";
		for(int i = 0; i < missingFieldIDs.size();i++){
			selection += missingFieldIDs.get(i).first;
			if(i < missingFieldIDs.size() -1 ){
				selection += ",";
			}
		}
		//create query
		final String s = "SELECT " + selection +" FROM '" + tableName + "' WHERE ORIGIN_ID = '" + id+"';";
		
		//do the query
		if(jsqlite.Database.complete(s)){
			try {
				st = db.prepare(s);
			if(st.step()){
				for(int j = 0; j < st.column_count(); j++){	
					
					//if mandatory field is null or empty, add it to the missing entries
					if(st.column_string(j) == null || st.column_string(j).equals("")){
						missingEntries.add(missingFieldIDs.get(j).second);
					}
				}
			}
			} catch (Exception e) {
				Log.d(MissionUtils.class.getSimpleName(), "Error checkIfAllMandatoryFieldsArsSatisfied",e);
			}
		}else{
			if(BuildConfig.DEBUG){
	    		Log.w(TAG, "Query is not complete: "+s);
			}
		}
		
		return missingEntries;
	}
	/**
	 * get "created" {@link MissionFeature} from the database
	 * @param tableName
	 * @param db
	 * @return a list of created {@link MissionFeature}
	 */
	public static ArrayList<MissionFeature> getMissionFeatures(final String tableName,final Database db){

		ArrayList<MissionFeature> missions = new ArrayList<MissionFeature>();

		// Reader for the Geometry field
		WKBReader wkbReader = new WKBReader();

		//create query
		////////////////////////////////////////////////////////////////
		// SQLite Geometry cannot be read with direct wkbreader
		// We must do a double conversion with ST_AsBinary and CastToXY 
		////////////////////////////////////////////////////////////////
		
		// Cycle all the columns to find the "Point" type one
		HashMap<String, String> columns = SpatialiteUtils.getPropertiesFields(db,tableName);

    	List<String> selectFields = new ArrayList<String>();
    	for(String columnName : columns.keySet()){
    		//Spatialite custom field point
    		if("point".equalsIgnoreCase(columns.get(columnName))){
    			selectFields.add("ST_AsBinary(CastToXY(" + columnName + ")) AS GEOMETRY");
    		}else{
    			selectFields.add(columnName);
    		}
    	}
    	
    	// Merge all the column names
    	String selectString = TextUtils.join(",",selectFields);
    	
    	// Build the query
    	StringWriter queryWriter = new StringWriter();
    	queryWriter.append("SELECT ")
    		.append(selectString)
    		.append(" FROM ")
    		.append(tableName)
    		.append(";");
    	
    	// The resulting query
    	String query = queryWriter.toString();
		
		Stmt stmt;
		//do the query
		if(jsqlite.Database.complete(query)){
			try {
				if(BuildConfig.DEBUG){
					Log.i("getCreatedMissionFeatures", "Loading from query: "+query);
				}
				stmt = db.prepare(query);
				MissionFeature f;
				while( stmt.step() ) {
					f = new MissionFeature();
					
					SpatialiteUtils.populateFeatureFromStmt(wkbReader, stmt, f);
					
		        	if(f.geometry == null){
		        		//workaround for a bug which does not read out the "Point" geometry in WKBreader
		        		//read single x and y coordinates instead and create the geometry by hand
		        		double[] xy = PersistenceUtils.getXYCoord(db, tableName, f.id);
		        		if(xy != null){
		        			GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(),4326);
		        			f.geometry = geometryFactory.createPoint(new Coordinate(xy[0], xy[1]));
		        		}
		        	}
		        	
					missions.add(f);
				}
				stmt.close();
			} catch (Exception e) {
				Log.d(MissionUtils.class.getSimpleName(), "Error getCreatedMissions",e);
			}
		}else{
			if(BuildConfig.DEBUG){
	    		Log.w(TAG, "Query is not complete: "+query);
			}
		}

		return missions;
	}
	
	/**
	 * Modifies the Feature aligning the field types with the give ones
	 * @param inputFeature
	 * @param fieldTypes
	 */
	public static void alignPropertiesTypes(Feature inputFeature, HashMap<String,XDataType> fieldTypes){
		
		if(inputFeature == null 
			|| fieldTypes == null
			|| inputFeature.properties == null
			|| inputFeature.properties.isEmpty()
			){
			// Nothing to do
			return;
		}
		
		ArrayList<String> propList = new ArrayList<String>();
		for(String s : inputFeature.properties.keySet()){
			propList.add(s);
		}
		
		// Cycle the properties
		for(String propertyString : propList){
			if(fieldTypes.containsKey(propertyString)){
				if(inputFeature.properties.get(propertyString) instanceof String){
					// Get property value
					String pValue = (String) inputFeature.properties.get(propertyString);
					
					if ( fieldTypes.get(propertyString) == XDataType.integer ){
						
						
						if(pValue == null || pValue.isEmpty()){
							// 
							inputFeature.properties.remove(propertyString);
						}
						
						try{
							inputFeature.properties.put(
									propertyString, 
									Integer.parseInt(pValue));
						}catch(NumberFormatException nfe){
							Log.w(TAG, "Wrong Integer String format, removing property " + propertyString  + "with value " + pValue);
							inputFeature.properties.remove(propertyString);
						}
						
					}else if ( fieldTypes.get(propertyString) == XDataType.decimal
							|| fieldTypes.get(propertyString) == XDataType.real){
							
						if(pValue == null || pValue.isEmpty()){
							// 
							inputFeature.properties.remove(propertyString);
						}
						
						try{
							inputFeature.properties.put(
									propertyString, 
									Double.parseDouble(pValue));
						}catch(NumberFormatException nfe){
							Log.w(TAG, "Wrong Double String format, removing property " + propertyString  + "with value " + pValue);
							inputFeature.properties.remove(propertyString);
						}
						
					}
				}
			}
		}
		
		
	}

    
    /**
     * Returns the "GCID" field of the origin feature of the given Mission
     * @param mMission
     * @return
     */
    public static String getMissionGCID(Mission mission){

        if(mission == null || mission.getOrigin() == null){
            if(BuildConfig.DEBUG){
                Log.w(TAG, "WARNING: cannot find origin Feature");
            }
            return null;
        }
        
        return getFeatureGCID(mission.getOrigin());
    }

	/**
	 * Returns the "GCID" or "gcid" field of the given Feature
	 * Returns null otherwise
	 * @param feature
	 * @return
	 */
	public static String getFeatureGCID(Feature feature){
		
		if(feature == null){
		    if(BuildConfig.DEBUG){
		        Log.w(TAG, "WARNING: cannot find feature GCID (feature null)");
		    }
			return null;
		}
		
		// Default ID
		String originIDString = feature.id;
		
		if(feature.properties != null){
		    
		    String localGCID = null;
		    if(feature.properties.containsKey(GCID_STRING)){
		        localGCID = GCID_STRING;
		    }else if(feature.properties.containsKey(GCID_STRING.toLowerCase(Locale.US))){
                localGCID = GCID_STRING.toLowerCase(Locale.US);
            }

			if(localGCID != null){
				try {
					Object objID = feature.properties.get(localGCID);
					if(objID == null){
					    if(BuildConfig.DEBUG){
					        Log.w(TAG, "WARNING: Feature has a null GCID using feature id: "+originIDString);
					    }  
						return originIDString;
					}
					originIDString = (String) objID;
					
				}catch(ClassCastException cce){
				    if(BuildConfig.DEBUG){
                        Log.w(TAG, "WARNING: Feature has a GCID but it cannot be converted to String");
				    }
					originIDString = null;
				}
			}
		}
		
		return originIDString;
	}
	
    /**
     * Check the existence of map styles relative to the given {@link MissionTemplate}
     * If they don't exist, it create them
     * @param missionTemplate
     */
    public static void checkMapStyles(Resources resources, MissionTemplate missionTemplate) {
        if(resources == null || missionTemplate == null){
            return;
        }

        File styleDir = new File(MapFilesProvider.getStyleDirIn());
        if(!styleDir.exists()){
            
            // Create the directory if not exists
            styleDir.mkdirs();
            
        }else if(!styleDir.isDirectory()){
            if(BuildConfig.DEBUG){
                Log.w(TAG, "Style directory is not a directory!");
            }
            return;
        }
        
        String baseString;
        try {
            baseString = PersistenceUtils.loadBaseStyleFile(resources);
        } catch (IOException e1) {
            Log.e(TAG, e1.getLocalizedMessage(), e1);
            return;
        }
        
        if(baseString == null){
            return;
        }
        
        if(missionTemplate.schema_seg != null && missionTemplate.schema_seg.localSourceStore != null){
            
            // Check for the main style
            File mainStyleFile = new File(styleDir, missionTemplate.schema_seg.localSourceStore+".style");
            if(!mainStyleFile.exists()){
    
                String noticeStyleString = baseString.replace(HEX_COLOR_PATTERN, resources.getString(R.color.default_notice_color));
                noticeStyleString = noticeStyleString.replace(TABLE_NAME_PATTERN, missionTemplate.schema_seg.localSourceStore);
                writeStyleFile(noticeStyleString, mainStyleFile);
                
            }
            
            // Check for the new notices style
            File newStyleFile = new File(styleDir, missionTemplate.schema_seg.localSourceStore + MissionTemplate.NEW_NOTICE_SUFFIX+ ".style");
            if(!newStyleFile.exists()){
    
                String newStyleString = baseString.replace(HEX_COLOR_PATTERN, resources.getString(R.color.default_new_notice_color));
                newStyleString = newStyleString.replace(TABLE_NAME_PATTERN, missionTemplate.schema_seg.localSourceStore + MissionTemplate.NEW_NOTICE_SUFFIX);
                writeStyleFile(newStyleString, newStyleFile);
                
            }
        }
        
        if(missionTemplate.schema_sop != null && missionTemplate.schema_sop.localFormStore != null){
                
            // Check for the Surveys style
            File surveyStyleFile = new File(styleDir, missionTemplate.schema_sop.localFormStore+".style");
            if(!surveyStyleFile.exists()){
    
                String surveyStyleString = baseString.replace(HEX_COLOR_PATTERN, resources.getString(R.color.default_survey_color));
                surveyStyleString = surveyStyleString.replace(TABLE_NAME_PATTERN, missionTemplate.schema_sop.localFormStore);
                writeStyleFile(surveyStyleString, surveyStyleFile);
                
            }
            
        }
        
        
    }

    /**
     * Write the String into the given File
     * @param baseString
     * @param mainStyleFile
     */
    public static void writeStyleFile(String baseString, File mainStyleFile) {
        FileWriter fw = null;
        try{
            fw= new FileWriter(mainStyleFile);
            fw.write(baseString);
            Log.i("STYLE","Style File updated:"+ mainStyleFile.getPath()); 
        } catch (FileNotFoundException e) {
            Log.e("STYLE", "unable to write open file:" + mainStyleFile.getPath());
        } catch (IOException e) {
            Log.e("STYLE", "error writing the file: " + mainStyleFile.getPath());
        }finally{
            try {
                if(fw != null){
                    fw.close();
                }
            } catch (IOException e) {
                // ignored
            }
        }
    }
    
    /**
     * checks if all files which are defined in the config section of
     * the template are present in the applications sdcard folder
     * @param t the template to check
     * @return true if all files are present - false otherwise
     */
    @SuppressWarnings("rawtypes")
	public static boolean checkTemplateForBackgroundData(final Context context, final MissionTemplate t) {
		
    	final String mount   = MapFilesProvider.getEnvironmentDirPath(context);
    	final String baseDir = MapFilesProvider.getBaseDir();
    	
    	final String appDir = mount + baseDir;
    	
    	final HashMap<String,Object> config = t.config;
    	
    	if(config.containsKey("bgFiles")){
    		ArrayList<Map> urls = (ArrayList<Map>) config.get("bgFiles");
    		if(urls != null){
    			for(int i = 0; i < urls.size(); i++){
					Map ltm = urls.get(i);
    				//String url = (String) ltm.get("url");
    				ArrayList<Map> content = (ArrayList<Map>) ltm.get("content");
    				
    				for(Map item : content){
    					
    					final String fileName = (String) item.get("file");
    					final File file = new File( appDir + "/" + fileName);
    					
    					if(!file.exists()){
    						return false;
    					}
    				}
    			}
    		}
    	}
    	
    	
		return true;
	}
    /**
     * creates and returns a HashMap containing entries consisting of url to zip files and the amount of files
     * these zips contain
     * @param t the template to parse
     * @return HashMap containing map of urls to fileAmounts
     */
    @SuppressWarnings("rawtypes")
    public static HashMap<String,Integer> getContentUrlsAndFileAmountForTemplate(final MissionTemplate t){
    	
    	final HashMap<String,Integer> urls = new HashMap<String,Integer>();
	
    	final HashMap<String,Object> config = t.config;

    	if(config.containsKey("bgFiles")){
			ArrayList<Map> files = (ArrayList<Map>) config.get("bgFiles");
    		if(files != null){
    			for(int i = 0; i < files.size(); i++){
					Map ltm = files.get(i);
    				String url = (String) ltm.get("url");

    				ArrayList<Map> content = (ArrayList<Map>) ltm.get("content");

    				urls.put(url, content.size());

    			}
    		}
			
    	}
    	return urls;
    }
    
    /**
     * Generates a new {@link MissionFeature} with the correct properties casing suitable for the upload.
     * Removes unnecessary properties
     * Takes feature schema as input
     */
    public static MissionFeature alignMissionFeatureProperties(MissionFeature inputMissionFeature, HashMap<String,XDataType> schema){
        
        if(inputMissionFeature == null || schema == null){
            if(BuildConfig.DEBUG){
                Log.w(TAG, "NULL MissionFeature or schema, cannot convert.");
            }
            return null;
        }
        
        MissionFeature output = new MissionFeature();
        
        output.id = inputMissionFeature.id;
        output.displayColor = inputMissionFeature.displayColor;
        output.editing = inputMissionFeature.editing;
        if(inputMissionFeature.geometry != null){
            output.geometry = (Geometry) inputMissionFeature.geometry.clone();
        }
        output.geometry_name = inputMissionFeature.geometry_name;
        output.type = inputMissionFeature.type;
        
        output.properties = new HashMap<String, Object>();
        
        if(inputMissionFeature.properties != null){
            for(String inputKey: inputMissionFeature.properties.keySet()){
                for(String schemaKey: schema.keySet()){
                    if(schemaKey != null && schemaKey.equalsIgnoreCase(inputKey)){
                        output.properties.put(schemaKey, inputMissionFeature.properties.get(inputKey));
                    }
                }
            }
        }
        
        return output;
    }
}
