/*
 * GeoSolutions map - Digital field mapping on Android based devices
 * Copyright (C) 2013 - 2014  GeoSolutions (www.geo-solutions.it)
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


import it.geosolutions.android.map.wfs.geojson.feature.Feature;
import it.geosolutions.geocollect.android.core.mission.MissionFeature;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;

import jsqlite.Database;
import jsqlite.Exception;
import jsqlite.Stmt;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
/**
 * AsyncTaskLoader to load Features from SQLite database.
 * It queries another loader to fetch data (can be a WFSLoader or any other one) before loading it
 * Provide a List of Resources <MissionFeature> object on Load finish
 *  
 * @author Lorenzo Pini (www.geo-solutions.it)
 *
 */
public class SQLiteCascadeFeatureLoader extends AsyncTaskLoader<List<MissionFeature>> {

	public static String TAG = "SQLiteCascadeFeatureLoader";
	public static String PREF_NAME = "SQLiteCascadeFeatureLoader";
	public static String LAST_UPDATE_PREF = "LastUpdate";
	public static String REVERSE_ORDER_PREF = "OrderByDesc";
	// 1 Hour between automatic reloading
	public long UPDATE_THRESHOLD = (3600)*1000;

	private AsyncTaskLoader<List<Feature>> pre_loader;
	private Database db;
	private String sourceTableName;
	private String formTableName;
	private String orderingField;
	private List<MissionFeature> mData;
	public int start;
	public int limit;
	HashMap<String,String> parameters;

	// Hold information about the priority to be shown
	private String priorityField;
	HashMap<String,String> priorityColours;

	public Integer totalCount;//This hack allow infinite scrolling without total count limits.

	// Place where to store the last load time
	private final SharedPreferences mPrefs;

	public SQLiteCascadeFeatureLoader(Context context,
			AsyncTaskLoader<List<Feature>> pre_loader,
			Database db,
			String localSourceStore,
			String localFormStore,
			String orderingField, 
			String priorityField,
			HashMap<String, String> priorityValuesColors) {
		
		super(context);
		this.mPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		this.pre_loader = pre_loader;
		this.db = db;
		this.sourceTableName = localSourceStore;
		this.formTableName = localFormStore;
		this.orderingField = orderingField;
		this.priorityField = priorityField;
		this.priorityColours = priorityValuesColors;
		
	}
	
	/**
	 * Constructor without form data checking
	 * Features will not be checked for existence in the form data table
	 * @param context
	 * @param pre_loader
	 * @param db
	 * @param localSourceStore
	 */
	public SQLiteCascadeFeatureLoader(Context context,
			AsyncTaskLoader<List<Feature>> pre_loader, 
			Database db, 
			String localSourceStore) {
		this(context, pre_loader, db, localSourceStore, null, null);
	}

	/**
	 * Constructor without color field info
	 * @param context
	 * @param pre_loader
	 * @param db
	 * @param localSourceStore
	 * @param localFormStore
	 * @param orderingField
	 */
	public SQLiteCascadeFeatureLoader(Context context,
			AsyncTaskLoader<List<Feature>> pre_loader,
			Database db,
			String localSourceStore,
			String localFormStore,
			String orderingField) {
		this(context, pre_loader, db, localSourceStore, localFormStore, orderingField, null, null);
	}

	/*
	 * When is this called?
	 */
	@Override
	protected void onStartLoading() {
	    if (mData != null) {
	        // Deliver any previously loaded data immediately.
	        deliverResult(mData);
	    }

	    // Begin monitoring the underlying data source.
	    // if (mObserver == null) {
	    // mObserver = new SampleObserver();
	    // // TODO: register the observer
	    // }
	        
	    if (takeContentChanged() || mData == null) {
	        // When the observer detects a change, it should call onContentChanged()
	        // on the Loader, which will cause the next call to takeContentChanged()
	        // to return true. If this is ever the case (or if the current data is
	        // null), we force a new load.
	        forceLoad();
	    }
	}
	
	@Override
	public List<MissionFeature> loadInBackground() {
		
		if(this.db == null || this.db.dbversion().equals("unknown")){
			Log.w(TAG, "Cannot open DB");
			return null;
		}
		
		if(this.sourceTableName == null || this.sourceTableName.isEmpty()){
			Log.w(TAG, "Table Name is empty");
			return null;
		}
		
		// Default, reload anyway
		boolean reload = true;

		if (mPrefs != null){
			long millis = mPrefs.getLong(LAST_UPDATE_PREF, 0L);
			if(millis >0){
				Date currentDate = new Date();
				if( currentDate.getTime() - millis < UPDATE_THRESHOLD){
					// Disable reload when too early
					Log.v(TAG, "Data is already updated");
					reload = false;
				}
			}
		
		}
		
		if(this.pre_loader!= null && reload){

			List<Feature> fromPreLoader = this.pre_loader.loadInBackground();
			if(fromPreLoader!= null){
				
				// TODO: load only paginated rows
				
				// TODO: truncate only paginated rows
				try {
					Stmt stmt = db.prepare("DELETE FROM '"+sourceTableName+"';");
					stmt.step();
					stmt.close();
				} catch (jsqlite.Exception e) {
					Log.e(TAG, Log.getStackTraceString(e));
				}
				
				
				if(!fromPreLoader.isEmpty()){
					
					// Get the list of the fields
					HashMap<String, String> dbFieldValues = SpatialiteUtils.getPropertiesFields(db, sourceTableName);
					if(dbFieldValues != null){

						Stmt stmt;
						String converted ;
						for(Feature f : fromPreLoader){
							
							String columnNames = " ( ";
							String columnValues = " ( ";
							columnNames = columnNames + "ORIGIN_ID";
							columnValues = columnValues + f.id;
							
							for(Entry<String, String> e : dbFieldValues.entrySet()){
								//Log.v(TAG, "Got -> "+e.getKey()+" : "+e.getValue());
								
								converted = SpatialiteUtils.getSQLiteTypeFromString(e.getValue());
								if (converted.equals("point") && f.geometry != null){
								
									columnNames = columnNames + ", GEOMETRY";
									// In JTS Point getX = longitude, getY = latitude
									columnValues = columnValues + ", MakePoint("+((Point)f.geometry).getX()+","+((Point)f.geometry).getY()+", 4326)";
									
								}else if(f.properties.containsKey(e.getKey()) && converted != null && f.properties.get(e.getKey())!= null ){
									
									columnNames = columnNames + ", " + e.getKey() ;
									
									if(converted.equals("text")||converted.equals("blob")){
										columnValues = columnValues + ", '" + SpatialiteUtils.escape((String) f.properties.get(e.getKey())) +"' " ;
									}else{
										columnValues = columnValues + ", " + f.properties.get(e.getKey()) ;
									}
									
								}
								
							}
							
							// add the geometry
							
							columnNames = columnNames + " )";
							columnValues = columnValues + " )";
							
							// TODO: group all the insert queries into a transaction for insertion speedup
							String insertQuery = "INSERT INTO '"+sourceTableName+"' "+columnNames+" VALUES "+columnValues+";";
							try {
								stmt = db.prepare(insertQuery);
								stmt.step();
								stmt.close();
								
								if(mPrefs != null){
									SharedPreferences.Editor editor = mPrefs.edit();
									Date currentDate = new Date();
									editor.putLong(LAST_UPDATE_PREF, currentDate.getTime());
									editor.commit();
								}
								
							} catch (Exception e1) {
								Log.e(TAG, Log.getStackTraceString(e1));
							}
							
							
						}
					}
				}

				//mData = fromPreLoader;
			}else{
				// error in the loader (no connectivity)
			}
		}
		
		// TODO: Load data into mData
		// Get the list of the fields
		// TODO: Can this call be done before the pre_loader block? 
		// Are there any concurrent events that can modify the table schema before we read the data?
		HashMap<String, String> dbFieldValues = SpatialiteUtils.getPropertiesFields(db, sourceTableName);
		
		mData = new ArrayList<MissionFeature>();
		
		// Reade for the Geometry field
        WKBReader wkbReader = new WKBReader();
        
		if(dbFieldValues!=null){
			
			Stmt stmt;
			String converted ;
			
			String columnNames = "SELECT ROWID "; // This is an SQLite standard column
			//String columnValues = " ( ";

			for(Entry<String, String> e : dbFieldValues.entrySet()){
				//Log.v(TAG, "Got -> "+e.getKey()+" : "+e.getValue());
				
				converted = SpatialiteUtils.getSQLiteTypeFromString(e.getValue());
				
				if(converted != null ){
					
					if(converted.equals("point")){
						// Only Points are supported
						columnNames = columnNames + ", ST_AsBinary(CastToXY("+e.getKey()+")) AS 'GEOMETRY'";
					}else{
						columnNames = columnNames + ", " + e.getKey() ;
					}
					
					/* TODO: escape values, is it necessary? they are columnames
					if(converted.equals("text")||converted.equals("blob")){
						columnValues = columnValues + ", '" + e.getValue()+"' " ;
					}else{
						columnValues = columnValues + ", " + e.getValue() ;
					}
					*/
				}
				
			}

			

			if(orderingField != null && !orderingField.isEmpty()){
				boolean reverse = mPrefs.getBoolean(REVERSE_ORDER_PREF, false);
				if(reverse){
					columnNames = columnNames + " FROM '"+sourceTableName+"' ORDER BY "+orderingField+" DESC;";
				}else{
					columnNames = columnNames + " FROM '"+sourceTableName+"' ORDER BY "+orderingField+";";
				}
			}else{
				columnNames = columnNames + " FROM '"+sourceTableName+"';";
			}
			
			Log.v(TAG, columnNames);
			if(Database.complete(columnNames)){
				
		        try {
		        	stmt = db.prepare(columnNames);
		            String columnName;
		            MissionFeature f;
		            while( stmt.step() ) {
		                f = new MissionFeature();
		            	int colcount = stmt.column_count();
		            	for(int colpos = 0; colpos < colcount; colpos++){
	            			
		            		columnName = stmt.column_name(colpos);
			            	if(columnName != null){
			            		
			            		if(columnName.equalsIgnoreCase("PK_UID")||columnName.equalsIgnoreCase("ORIGIN_ID")){
			            			f.id = stmt.column_string(colpos);
			            		}else if(columnName.equalsIgnoreCase("GEOMETRY")){
			            			// At the moment, only Point is supported
			            			// Here, the "GEOMETRY" column contains the result of 
			            			//	ST_AsBinary(CastToXY("GEOMETRY"))
			            			byte[] geomBytes = stmt.column_bytes(colpos);
				   					try {
				   						f.geometry = wkbReader.read(geomBytes);
				   					} catch (ParseException e) {
				   						Log.e(TAG,"Error reading geometry");
				   						//throw new Exception(e.getMessage());
				   					}

			            		}else{
			            			if(f.properties == null){
			            				f.properties = new HashMap<String, Object>();
			            			}
			            			f.properties.put(columnName, stmt.column_string(colpos));
			            		}
			            		
			                	
			                }else{
			                	// This should never happen
			                	Log.d(TAG, "Found a NULL column name, this is strange.");
			                }
		            	
		            	}
		                mData.add(f);
		            }
		            stmt.close();
		            			            
		        } catch (Exception e) {
		            Log.e(TAG, Log.getStackTraceString(e));
		        }
		        
			}else{
				Log.w(TAG, "Query is not complete:\n"+columnNames);
			}
			
		}

		// Set the "Editing flag"
		
		if(formTableName != null && !formTableName.isEmpty()){
			ArrayList<String> editingIds = new ArrayList<String>();
			String query = "SELECT ORIGIN_ID FROM '"+formTableName+"';";
						
			if(Database.complete(query)){
				try {
					Stmt stmt = db.prepare(query);
					while( stmt.step() ) {
						editingIds.add(stmt.column_string(0));
					}
					stmt.close();
				} catch (jsqlite.Exception e) {
					Log.e(TAG, Log.getStackTraceString(e));
				}
			}
			if(!editingIds.isEmpty()){
				for(MissionFeature f : mData){
					if ( editingIds.contains(f.id)){
						f.editing = true;
					}
				}
			}
		}
		
		
		// Icon Color
		if ( priorityField != null
    			&& !priorityField.isEmpty()
    			&& priorityColours != null
    			&& !priorityColours.isEmpty()
				){
			
			for(MissionFeature f : mData){
				if ( f.properties.containsKey(priorityField)){
					f.displayColor = priorityColours.get(f.properties.get(priorityField));
				}
			}
			
		}
		
		return mData;
	}

	@Override
	protected void onStopLoading() {
		cancelLoad();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.content.Loader#onReset()
	 */
	@Override
	protected void onReset() {
		
		if(this.pre_loader != null){
			this.pre_loader.reset();
		}

		onStopLoading();
		if (mData != null) {
			releaseResources(mData);
			mData = null;
		}

		// if(mObserver !=null){
		// //TODO unregister the observer
		// moObserver=null;
		//
		// }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.content.AsyncTaskLoader#onCanceled(java.lang.Object)
	 */
	@Override
	public void onCanceled(List<MissionFeature> data) {
		super.onCanceled(data);
		/*
		if(this.pre_loader != null){
			this.pre_loader.onCanceled(data);
		}
		*/
		releaseResources(data);
	}

	/**
	 * @param mData
	 */
	private void releaseResources(List<MissionFeature> mData) {
		// release resource if needed

	}

	

}
