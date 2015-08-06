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
import static it.geosolutions.geocollect.android.core.mission.utils.SpatialiteUtils.populateFeatureFromStmt;
import it.geosolutions.geocollect.android.app.BuildConfig;
import it.geosolutions.geocollect.android.core.mission.MissionFeature;
import it.geosolutions.geocollect.model.config.MissionTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.vividsolutions.jts.geom.Point;
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
	public static String REVERSE_ORDER_PREF = "ReverseOrdering";
	public static String ORDER_BY_DISTANCE = "OrderByDistance";
	
	/**
	 * Preferences Strings for the Spatial Filter
	 */
	public static String FILTER_N = "FilterN";
	public static String FILTER_S = "FilterS";
	public static String FILTER_E = "FilterE";
	public static String FILTER_W = "FilterW";
	public static String FILTER_SRID = "FilterSRID";
	
	public static String LOCATION_X = "LocationX";
    public static String LOCATION_Y = "LocationY";
    
	
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
	@Override
	protected void onForceLoad() {
		super.onForceLoad();
		
		Log.d(TAG, "onForceLoad , mSourceTable "+sourceTableName +" mId "+this.getId());
		
	}
	/*
	 * When is this called?
	 */
	@Override
	protected void onStartLoading() {
	    if (mData != null) {
	        // Deliver any previously loaded data immediately.
	        //deliverResult(mData);
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
			// this must now specify a table, otherwise it blocks loading data for other tables
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
						
						long startTime = System.nanoTime();
 
						StringBuilder columnNames = new StringBuilder(300);
						StringBuilder columnValues = new StringBuilder(300);
                        
                        columnNames.append(" ( ").append( "ORIGIN_ID");
                        columnValues.append(" ( ").append( "'");
                        
                        int namesToTruncate = columnNames.length();
                        int valuesToTruncate = columnValues.length();
                        long featureStartTime;
                        long queryTime;
                        long prefStartTime;
                        long stopTime;
                        
                        StringBuilder loggerBuilder = new StringBuilder(300);
                        
						for(Feature f : fromPreLoader){
							
						  featureStartTime = System.nanoTime();

						  columnValues.append(f.id).append( "'");
                          
                          for(Entry<String, String> e : dbFieldValues.entrySet()){
                              //Log.v(TAG, "Got -> "+e.getKey()+" : "+e.getValue());
                              
                              converted = SpatialiteUtils.getSQLiteTypeFromString(e.getValue());
                              if (converted.equals("point") && f.geometry != null){
                              
                                  columnNames .append( ", GEOMETRY");
                                  // In JTS Point getX = longitude, getY = latitude
                                  columnValues .append( ", MakePoint(").append(((Point)f.geometry).getX()).append(",").append(((Point)f.geometry).getY()).append(", 4326)");
                                  
                              }else if(f.properties.containsKey(e.getKey()) && converted != null && f.properties.get(e.getKey())!= null ){
                                  
                                  columnNames .append( ", " ).append( e.getKey() );
                                  
                                  if(converted.equals("text")||converted.equals("blob")){
                                      columnValues .append( ", '" ).append( SpatialiteUtils.escape((String) f.properties.get(e.getKey())) ).append("' " );
                                  }else{
                                      columnValues .append( ", " ).append( f.properties.get(e.getKey())) ;
                                  }
                                  
                              }
                              
                          }
                          
                          // add the geometry
                          columnNames .append( " )");
                          columnValues .append( " )");
						    
							// TODO: Use prepared statements and group all the insert queries into a transaction for insertion speedup
							//       This will need to get the schema beforehand, based on the JSON features
							String insertQuery = "INSERT INTO '"+sourceTableName+"' "+columnNames.toString()+" VALUES "+columnValues.toString()+";";
							queryTime = System.nanoTime();
                            if(BuildConfig.DEBUG){
                                loggerBuilder.append("Query created in: " ).append( (queryTime - featureStartTime)/1000000 ).append( "ms\n");
                            }
							
                            columnNames .setLength(namesToTruncate);
                            columnValues .setLength(valuesToTruncate);
                            
                            try {
								stmt = db.prepare(insertQuery);
								long prepareTime = System.nanoTime();
                                if(BuildConfig.DEBUG){
	                                loggerBuilder.append("Query prepared in: " ).append( (prepareTime - queryTime)/1000000 ).append( "ms\n");
	                            }
								
								stmt.step();
								stmt.close();
								
								long featureStopTime = System.nanoTime();
								if(BuildConfig.DEBUG){
								    loggerBuilder.append("Feature inserted in: " ).append( (featureStopTime - prepareTime)/1000000 ).append( "ms\n");
								}
								
							} catch (Exception e1) {
							    if(BuildConfig.DEBUG){
                                    Log.e(TAG, Log.getStackTraceString(e1));
							    }
							} finally {
							    if(BuildConfig.DEBUG){
                                    Log.d(TAG, loggerBuilder.toString());
                                    loggerBuilder.setLength(0);
                                }
							}
						}
						
                        prefStartTime = System.nanoTime();

                        // Update the "last update" time to prevent unnecessary downloads
                        if(mPrefs != null){
                            SharedPreferences.Editor editor = mPrefs.edit();
                            Date currentDate = new Date();
                            editor.putLong(LAST_UPDATE_PREF, currentDate.getTime());
                            editor.commit();
                        }
                        
						stopTime = System.nanoTime();
						if(BuildConfig.DEBUG){
                            Log.d(TAG, "Pref updated in: " + (stopTime - prefStartTime)/1000000 + "ms");
                            Log.d(TAG, "Database updated in: " + (stopTime - startTime)/1000000 + "ms");
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
		
		// Reader for the Geometry field
        WKBReader wkbReader = new WKBReader();
        
		if(dbFieldValues!=null){
			
			String converted ;
			boolean hasGeometry = false;
			
			String columnNames = "SELECT ROWID "; // This is an SQLite standard column
			String filterString = "";
			String orderString = "";
			
			for(Entry<String, String> e : dbFieldValues.entrySet()){
				//Log.v(TAG, "Got -> "+e.getKey()+" : "+e.getValue());
				
				converted = SpatialiteUtils.getSQLiteTypeFromString(e.getValue());
				
				if(converted != null ){
					
					if(converted.equals("point")){
						// Only Points are supported
						columnNames = columnNames + ", ST_AsBinary(CastToXY("+e.getKey()+")) AS 'GEOMETRY'";
						hasGeometry = true;
					}else{
						columnNames = columnNames + ", " + e.getKey() ;
					}
					
				}
				
			}
			
			//Add Spatial filtering
			if(hasGeometry){
				int filterSrid = mPrefs.getInt(FILTER_SRID, -1);
				// If the SRID is not defined, skip the filter
				if(filterSrid != -1){
					double filterN = Double.longBitsToDouble( mPrefs.getLong(FILTER_N, Double.doubleToLongBits(0)));
					double filterS = Double.longBitsToDouble( mPrefs.getLong(FILTER_S, Double.doubleToLongBits(0)));
					double filterW = Double.longBitsToDouble( mPrefs.getLong(FILTER_W, Double.doubleToLongBits(0)));
					double filterE = Double.longBitsToDouble( mPrefs.getLong(FILTER_E, Double.doubleToLongBits(0)));
					
					filterString = " WHERE MbrIntersects(GEOMETRY, BuildMbr("+filterW+", "+filterN+", "+filterE+", "+filterS+")) ";
				}
				//WHERE MbrIntersects(GEOMETRY, BuildMbr(8.75269101373853, 44.505790969141614, 9.039467060007173, 44.35415617743291))
			}
			
			// TODO: Should the orderingField be mandatory to have the ordering?
			if(orderingField != null && !orderingField.isEmpty()){
			    boolean reverse = mPrefs.getBoolean(REVERSE_ORDER_PREF, false);
			    boolean useDistance = mPrefs.getBoolean(ORDER_BY_DISTANCE, false);
			    double posX = Double.longBitsToDouble( mPrefs.getLong(LOCATION_X, Double.doubleToLongBits(0)));
                double posY = Double.longBitsToDouble( mPrefs.getLong(LOCATION_Y, Double.doubleToLongBits(0)));
                
			    if(useDistance){
				    columnNames = columnNames + ", Distance(ST_Transform(GEOMETRY,4326), MakePoint("+posX+","+posY+", 4326)) * 111195 AS '"+MissionFeature.DISTANCE_VALUE_ALIAS+"'" ;
				    orderString = "ORDER BY "+MissionFeature.DISTANCE_VALUE_ALIAS;
				}else{
					orderString = "ORDER BY "+orderingField;
				}
				
				if(reverse){
				    orderString = orderString + " DESC";
				}
			}
			
			String finalQuery = columnNames + " FROM '"+sourceTableName+"' "+filterString+" "+orderString+";";
					
			Log.v(TAG, finalQuery);
			loadMissionFeature(wkbReader, sourceTableName, finalQuery);
			
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
			}else{
				if(BuildConfig.DEBUG){
		    		Log.w(TAG, "Query is not complete: "+query);
				}
			}
			
			if(!editingIds.isEmpty()){
				for(MissionFeature f : mData){
					if ( editingIds.contains(MissionUtils.getFeatureGCID(f))){
						f.editing = true;
					}
				}
			}
		}
		
		///////////////////////
		// Add the NEW items
		///////////////////////
		
		Log.v(TAG, "Loading created missions from " + sourceTableName + MissionTemplate.NEW_NOTICE_SUFFIX);
        final ArrayList<MissionFeature> missions = MissionUtils.getMissionFeatures(sourceTableName + MissionTemplate.NEW_NOTICE_SUFFIX, db);

        mData.addAll(missions);
        /*
        if(orderingField != null && !orderingField.isEmpty()){
            boolean reverse = mPrefs.getBoolean(REVERSE_ORDER_PREF, false);
            boolean useDistance = mPrefs.getBoolean(ORDER_BY_DISTANCE, false);
            
            if(useDistance){
                Collections.sort(mData, new Comparator<MissionFeature>() {
        
                    @Override
                    public int compare(MissionFeature lhs, MissionFeature rhs) {
                        if(lhs.properties == null || !lhs.properties.containsKey(MissionFeature.DISTANCE_VALUE_ALIAS)){
                            return 1;
                        }
                        if(rhs.properties == null || !rhs.properties.containsKey(MissionFeature.DISTANCE_VALUE_ALIAS)){
                            return -1;
                        }
                        
                        try{
                            long ldistance = Math.round(Double.parseDouble(lhs.properties.get(MissionFeature.DISTANCE_VALUE_ALIAS).toString()));
                            long rdistance = Math.round(Double.parseDouble(rhs.properties.get(MissionFeature.DISTANCE_VALUE_ALIAS).toString()));
                            return (int) (rdistance-ldistance);
                        }catch (NumberFormatException nfe){
                            return 0;
                        }
                    }
                } );
            }else{
                
            }
            
            if(reverse){
                Collections.reverse(mData);
            }
        }
        */
		///////////////////////
		
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

	private void loadMissionFeature(WKBReader wkbReader, String tableName, String query) {
		Stmt stmt;
		if(Database.complete(query)){
			
		    try {
		    	if(BuildConfig.DEBUG){
		    		Log.i(TAG, "Loading from query: "+query);
		    	}
		    	stmt = db.prepare(query);
		        MissionFeature f;
		        while( stmt.step() ) {
		            f = new MissionFeature();
		        	populateFeatureFromStmt(wkbReader, stmt, f);
		        	f.typeName = tableName;
		            mData.add(f);
		        }
		        stmt.close();
		        			            
		    } catch (Exception e) {
		        Log.e(TAG, Log.getStackTraceString(e));
		    }
		    
		}else{
			if(BuildConfig.DEBUG){
	    		Log.w(TAG, "Query is not complete: "+query);
			}
		}
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
