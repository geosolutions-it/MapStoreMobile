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
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import jsqlite.Database;
import jsqlite.Exception;
import jsqlite.Stmt;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
/**
 * AsyncTaskLoader to load Features from SQLite database.
 * It queries another loader to fetch data (can be a WFSLoader or any other one) before loading it
 * Provide a List of Resources <Feature> object on Load finish
 *  
 * @author Lorenzo Pini (www.geo-solutions.it)
 *
 */
public class SQLiteCascadeFeatureLoader extends AsyncTaskLoader<List<Feature>> {

	public String TAG = "SQLiteCascadeFeatureLoader";
	private AsyncTaskLoader<List<Feature>> pre_loader;
	private Database db;
	private String tableName;
	private List<Feature> mData;
	public int start;
	public int limit;
	HashMap<String,String> parameters;
	public Integer totalCount;//This hack allow infinite scrolling without total count limits.

	public SQLiteCascadeFeatureLoader(Context context,
			AsyncTaskLoader<List<Feature>> pre_loader,
			Database db,
			String tableName) {
		
		super(context);
		this.pre_loader = pre_loader;

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
	public List<Feature> loadInBackground() {
		
		if(this.db == null || this.db.dbversion().equals("unknown")){
			Log.w(TAG, "Cannot open DB");
			return null;
		}
		
		if(this.tableName == null || this.tableName.isEmpty()){
			Log.w(TAG, "Table Name is empty");
			return null;
		}
		
		if(this.pre_loader!= null){
			
			List<Feature> fromPreLoader = this.pre_loader.loadInBackground();
			if(fromPreLoader!= null){
				
				// TODO: load only paginated rows
				
				// TODO: truncate only paginated rows
				try {
					Stmt stmt = db.prepare("DELETE FROM '"+tableName+";");
					stmt.step();
					stmt.close();
				} catch (jsqlite.Exception e) {
					Log.e(TAG, Log.getStackTraceString(e));
				}
				
				
				if(!fromPreLoader.isEmpty()){
					
					// get table columns
					// TODO: extract in a separate Utility Class
			        String table_info_query = "PRAGMA table_info('"+tableName+"');";
			        int nameColumn = -1;
			        int typeColumn = -1;
			        String columnName, typeName;
			        HashMap<String, String> dbFieldValues = new HashMap<String, String>();
			        
			        try {
			            Stmt stmt = db.prepare(table_info_query);
			            while( stmt.step() ) {

			                if(nameColumn<0 || typeColumn<0){
			                	// I have to retrieve the position of the metadata fields
			                	for(int i = 0; i<stmt.column_count(); i++){
			                		Log.v(TAG, stmt.column_name(i));
			                		if(stmt.column_name(i).equalsIgnoreCase("name")){
			                			nameColumn = i;
			                		}
			                		if(stmt.column_name(i).equalsIgnoreCase("type")){
			                			typeColumn = i;
			                		}
			                	}
			                }
			                
			                columnName = stmt.column_string(nameColumn);
			                typeName = stmt.column_string(typeColumn);
			                if(columnName != null){
			                	
			                	dbFieldValues.put(columnName, typeName);
			                	
			                }else{
			                	// This should never happen
			                	Log.v(TAG, "Found a NULL column name, this is strange.");
			                }
			            }
			            stmt.close();
			            			            
			        } catch (Exception e) {
			            Log.e(TAG, Log.getStackTraceString(e));
			        }
					
					////////////////////////////////////////
					Stmt stmt;
					String converted ;
					for(Feature f : fromPreLoader){
						
						String columnNames = " (  ";
						String columnValues = " ( ";
						columnNames = columnNames + "ORIGIN_ID";
						columnValues = columnValues + f.id;
						
						for(Entry<String, String> e : dbFieldValues.entrySet()){
							Log.v(TAG, "Got -> "+e.getKey()+" : "+e.getValue());
							
							converted = SpatialiteUtils.getSQLiteTypeFromString(e.getValue());
							
							if(f.properties.containsKey(e.getKey()) && converted != null){
								
								columnNames = columnNames + ", " + e.getKey() ;
								
								// TODO: escape values
								if(converted.equals("text")||converted.equals("blob")){
									columnValues = columnValues + ", '" + e.getValue()+"' " ;
								}else{
									columnValues = columnValues + ", " + e.getValue() ;
								}
								
							}
							
						}
						
						columnNames = columnNames + ")";
						columnValues = columnValues + ")";
						
						String insertQuery = "INSERT INTO '"+tableName+"' "+columnNames+" VALUES "+columnValues+";";
						try {
							stmt = db.prepare(insertQuery);
							stmt.step();
							stmt.close();
						} catch (Exception e1) {
							Log.e(TAG, Log.getStackTraceString(e1));
						}
					}
				
				}

				mData = fromPreLoader;
			}else{
				// error in the loader (no connectivity)
			}
		}
		
		// TODO: Load data into mData
		
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
	public void onCanceled(List<Feature> data) {
		super.onCanceled(data);
		if(this.pre_loader != null){
			this.pre_loader.onCanceled(data);
		}
		releaseResources(data);
	}

	/**
	 * @param data
	 */
	private void releaseResources(List<Feature> data) {
		// release resource if needed

	}

	

}
