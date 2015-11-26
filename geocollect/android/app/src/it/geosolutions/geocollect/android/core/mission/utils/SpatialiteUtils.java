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

import it.geosolutions.geocollect.android.app.BuildConfig;
import it.geosolutions.geocollect.android.core.mission.Mission;
import it.geosolutions.geocollect.android.core.mission.MissionFeature;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Locale;

import jsqlite.Database;
import jsqlite.Exception;
import jsqlite.Stmt;
import android.content.Context;
import android.util.Log;

import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;

import eu.geopaparazzi.library.util.ResourcesManager;

/**
 * Utils for Spatialite database
 * @author Lorenzo Pini (lorenzo.pini@geo-solutions.it)
 */
public class SpatialiteUtils {
	
	public static String TAG = "SpatialiteUtils";
	
	/**
	 * Return the string associated with the given column_type
	 * @param jsqliteType
	 * @return
	 */
	public static String getMapping(int columnType){
		
		switch (columnType) {
		case jsqlite.Constants.SQLITE_INTEGER:  //1
			return "integer";
		case jsqlite.Constants.SQLITE_FLOAT:  //2
			
			return "float";
		case jsqlite.Constants.SQLITE3_TEXT:  //3
			
			return "text";
		case jsqlite.Constants.SQLITE_BLOB:  //4
			
			return "blob";
		case jsqlite.Constants.SQLITE_NULL:  //5
			
			return "null";
		case jsqlite.Constants.SQLITE_NUMERIC: //-1
			
			return "numeric";
		default:
			return null;
		}
		
	}
	
	/**
	 * Returns a valid SQLite Type from a given wfs type representation
	 * or null if given string is not a valid type
	 * Note that the string "null" is a valid SQLite type
	 */
	public static String getSQLiteTypeFromString(String inputTypeString){
		
		if(inputTypeString == null){
			return null;
		}
		
		String toCheck = inputTypeString.toLowerCase(Locale.US);
		if(	toCheck.equals("string")
			||	toCheck.equals("text")
			||	toCheck.equals("varchar")
			||	toCheck.equals("person")
			||	toCheck.equals("date")
			||	toCheck.equals("datetime")
			){
			return "text";
		}
		
		if(	toCheck.equals("double")
			||	toCheck.equals("real")
			||	toCheck.equals("float")
			||	toCheck.equals("decimal")
			){
			return "double";
		}
		
		if(	toCheck.equals("integer")
				||	toCheck.equals("int")
				){
			return "integer";
		}
		
		if(	toCheck.equals("blob")
			){
			return "blob";
		}
		
		if(	toCheck.equals("null")
			){
			return "null";
		}

		if(	toCheck.equals("numeric")
			|| toCheck.equals("long")
            	){
			return "numeric";
		}

		// Spatialite custom types
		if(	toCheck.equals("point")
				){
			return "point";
		}

		// unrecognized type
		return null;
	}
	
	/**
	 * Open the given database and returns a reference to it or null if invalid context or databasePath are passed
	 * If given filePath does not exists, it will be created
	 */
	public static Database importSpatialiteDBFromAssets(Context c, String sdcardFolder, String databaseFile){
		
		if(	c == null 
			|| databaseFile == null){
        	Log.v(TAG, "Cannot open Database, invalid parameters.");
        	return null;
			
		}
		
		Database spatialiteDatabase = null;
		
		
		try {
            
            final InputStream input = c.getAssets().open(databaseFile);

            File sdcardDir = ResourcesManager.getInstance(c).getSdcardDir();
            File spatialDbFile = new File(sdcardDir, sdcardFolder + "/" + databaseFile);
            
            final File geocollectDir = new File(sdcardDir, sdcardFolder);
            if(!geocollectDir.exists()){
            	if(!geocollectDir.mkdir()){
            		throw new  RuntimeException();
            	}
            }

			OutputStream myOutput = new FileOutputStream(spatialDbFile);

			//transfer bytes from the inputfile to the outputfile
			byte[] buffer = new byte[1024];
			int length;
			while ((length = input.read(buffer)) > 0){
				myOutput.write(buffer, 0, length);
			}

			myOutput.flush();
			myOutput.close();
			input.close();
            
			if (!spatialDbFile.getParentFile().exists()) {
				throw new RuntimeException();
			}
            spatialiteDatabase = new jsqlite.Database();           
            spatialiteDatabase.open(spatialDbFile.getAbsolutePath(), jsqlite.Constants.SQLITE_OPEN_READWRITE
                    | jsqlite.Constants.SQLITE_OPEN_CREATE);
            
            //Log.v("MISSION_DETAIL", SpatialiteUtils.queryVersions(spatialiteDatabase));
            Log.v(TAG, spatialiteDatabase.dbversion());
            
            if(BuildConfig.DEBUG){
            	Log.i(TAG, "Database imported from assets");
            }
            
        } catch (Exception e) {
            Log.v(TAG, Log.getStackTraceString(e));
        } catch (java.lang.Exception e) {
        	// Cannot find Android ResourceManager
            Log.e(TAG, Log.getStackTraceString(e));
		}
		
		return spatialiteDatabase;
	}
	
	/**
	 * Open the given database and returns a reference to it or null if invalid context or databasePath are passed
	 * If given filePath does not exists, it will be created
	 */
	public static Database openSpatialiteDB(Context c, String databasePath){
		
		if(	c == null 
			|| databasePath == null
			|| databasePath.isEmpty()){
        	Log.v(TAG, "Cannot open Database, invalid parameters.");
        	return null;
			
		}
		
		Database spatialiteDatabase = null;
		
		try {
            
            File sdcardDir = ResourcesManager.getInstance(c).getSdcardDir();
            File spatialDbFile = new File(sdcardDir, databasePath);

            if (!spatialDbFile.getParentFile().exists()) {
                return importSpatialiteDBFromAssets(c, "geocollect", "genova.sqlite");
            }
            
            spatialiteDatabase = new jsqlite.Database();
            spatialiteDatabase.open(spatialDbFile.getAbsolutePath(), jsqlite.Constants.SQLITE_OPEN_READWRITE
                    | jsqlite.Constants.SQLITE_OPEN_CREATE);
            
            //Log.v("MISSION_DETAIL", SpatialiteUtils.queryVersions(spatialiteDatabase));
            Log.v(TAG, spatialiteDatabase.dbversion());
            
        } catch (Exception e) {
            Log.v(TAG, Log.getStackTraceString(e));
        } catch (java.lang.Exception e) {
        	// Cannot find Android ResourceManager
            Log.e(TAG, Log.getStackTraceString(e));
		}
		
		return spatialiteDatabase;
	}
	
	
	/**
	 * Default getVersions method
	 * Based on Spatialite Examples by Alessandro Furieri (a.furieri@lqt.it)
	 * @param db
	 * @return
	 * @throws Exception
	 */
	public static String queryVersions(jsqlite.Database db) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("Check versions...\n");

        Stmt stmt01 = db.prepare("SELECT spatialite_version();");
        if (stmt01.step()) {
            sb.append("\t").append("SPATIALITE_VERSION: " + stmt01.column_string(0));
            sb.append("\n");
        }

        stmt01 = db.prepare("SELECT proj4_version();");
        if (stmt01.step()) {
            sb.append("\t").append("PROJ4_VERSION: " + stmt01.column_string(0));
            sb.append("\n");
        }

        stmt01 = db.prepare("SELECT geos_version();");
        if (stmt01.step()) {
            sb.append("\t").append("GEOS_VERSION: " + stmt01.column_string(0));
            sb.append("\n");
        }
        stmt01.close();

        sb.append("Done...\n");
        return sb.toString();
    }
	
	/**
	 * Check if the specified table exists in the specified database, if not exists, create it.
	 * 
	 * TODO:
	 * - Separate check and creation methods
	 * - Make table creation parametric based on Template
	 * 
	 * @param db
	 * @param tableName
	 * @return

	public static boolean checkOrCreateTable(jsqlite.Database db, String tableName){
		
		if (db != null){
			
	        String query = "SELECT name FROM sqlite_master WHERE type='table' AND name='"+tableName+"'";

	        boolean found = false;
	        try {
	            Stmt stmt = db.prepare(query);
	            if( stmt.step() ) {
	                String nomeStr = stmt.column_string(0);
	                found = true;
	                Log.v(TAG, "Found table: "+nomeStr);
	            }
	            stmt.close();
	        } catch (Exception e) {
	            Log.e(TAG, Log.getStackTraceString(e));
	            return false;
	        }

			if(found){
				return true;
			}else{
				// Table not found creating
                Log.v(TAG, "Table "+tableName+" not found, creating..");
				
                // TODO: refactor this
                if(tableName.equalsIgnoreCase("punti_accumulo_data")){

                	String create_stmt = "CREATE TABLE 'punti_accumulo_data' (" +
                			"'PK_UID' INTEGER PRIMARY KEY AUTOINCREMENT, " +
                			"'ORIGIN_ID' TEXT, " +
                			"'DATA_SCHEDA' TEXT, " +
                			"'DATA_AGG' TEXT, " +
                			"'NOME_RILEVATORE' TEXT, " +
                			"'COGNOME_RILEVATORE' TEXT, " +
                			"'ENTE_RILEVATORE' TEXT, " +
                			"'TIPOLOGIA_SEGNALAZIONE' TEXT, " +
                			"'PROVENIENZA_SEGNALAZIONE' TEXT, " +
                			"'CODICE_DISCARICA' TEXT, " +
                			"'TIPOLOGIA_RIFIUTO' TEXT, " +
                			"'COMUNE' TEXT, " +
                			"'LOCALITA' TEXT, " +
                			"'INDIRIZZO' TEXT, " +
                			"'CIVICO' TEXT, " +
                			"'PRESA_IN_CARICO' TEXT, " +
                			"'EMAIL' INTEGER, " +
                			"'RIMOZIONE' TEXT, " +
                			"'SEQUESTRO' TEXT, " +
                			"'RESPONSABILE_ABBANDONO' TEXT, " +
                			"'QUANTITA_PRESUNTA' FLOAT);";

                	String add_geom_stmt = "SELECT AddGeometryColumn('punti_accumulo_data', 'GEOMETRY', 4326, 'POINT', 'XY');";
                	String create_idx_stmt = "SELECT CreateSpatialIndex('punti_accumulo_data', 'GEOMETRY');";
                    
                	// TODO: check if all statements are complete
                	
                	try {                	
                		Stmt stmt01 = db.prepare(create_stmt);

						if (stmt01.step()) {
							//TODO This will never happen, CREATE statements return empty results
						    Log.v(TAG, "Table Created");
						}
						
						// TODO: Check if created, fail otherwise
						
						stmt01 = db.prepare(add_geom_stmt);
						if (stmt01.step()) {
						    Log.v(TAG, "Geometry Column Added "+stmt01.column_string(0));
						}
						
						stmt01 = db.prepare(create_idx_stmt);
						if (stmt01.step()) {
						    Log.v(TAG, "Index Created");
						}
						
						stmt01.close();
						
					} catch (jsqlite.Exception e) {
						Log.e(TAG, Log.getStackTraceString(e));
					}
                	return true;
                }
			}
		}else{
			Log.w(TAG, "No valid database received, aborting..");
		}
		
		return false;
	}
		 */
	
	/**
	 * Initializes the table if not exists
	 * @param db
	 * @param tableName
	 * @return

	public static boolean initializeTable(jsqlite.Database db, String tableName){
		
		
		return false;
		
	}
	 */
	
	/**
	 * Return an Hashmap of Strings, containing the list of the columns of the given table name in the given database.
	 * Returns null if wrong parameters are given or database is closed.
	 * @param db
	 * @param tableName
	 * @return HashMap<String, String>
	 */
	public static HashMap<String, String> getPropertiesFields (Database db, String tableName){
		
		if(db == null || tableName == null){
			Log.w(TAG, "Wrong parameters for getPropertiesFields()");
			return null;
		}
		
		if(db.dbversion().equals("unknown")){
			Log.w(TAG, "Database is closed, cannot access to it.");
			return null;
		}
		
		// get table columns
		// If table does not exists, an empty result set is returned.
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
                	
                	// This is a debug check
                	if(typeName == null){
                		Log.d(TAG, "Column name '"+columnName+"' has a NULL typeName");
                	}
                	
                	dbFieldValues.put(columnName, typeName);
                	
                }else{
                	// This should never happen
                	Log.d(TAG, "Found a NULL column name, this is strange.");
                }
            }
            stmt.close();
            			            
        } catch (Exception e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }
		return dbFieldValues;
	}
	/**
	 * Populate attributes and geometry fields of a feature from the stmt 
	 * reading attributes and setting geometry 
	 * @param wkbReader
	 * @param stmt
	 * @param f
	 * @throws Exception
	 */
	public static void populateFeatureFromStmt(WKBReader wkbReader, Stmt stmt,
			MissionFeature f) throws Exception {
		String columnName;
		int colcount = stmt.column_count();
		for(int colpos = 0; colpos < colcount; colpos++){
			
			columnName = stmt.column_name(colpos);
			if(columnName != null){
				
				if(columnName.equalsIgnoreCase(Mission.PK_UID_STRING)||columnName.equalsIgnoreCase(Mission.ORIGIN_ID_STRING)){
					// The output Feature ID should not be the same as the input Feature one
					f.id = stmt.column_string(colpos);
				}else if(columnName.equalsIgnoreCase(Mission.GEOMETRY_FIELD_STRING)){
					// At the moment, only Point is supported
					// Here, the "GEOMETRY" column contains the result of 
					//	ST_AsBinary(CastToXY("GEOMETRY"))
					byte[] geomBytes = stmt.column_bytes(colpos);
					if(geomBytes != null){						
						try {
							f.geometry = wkbReader.read(geomBytes);
						} catch (ParseException e) {
							Log.e(TAG,"Error reading geometry");
							//throw new Exception(e.getMessage());
						}
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
	}
	
	/**
	 * Escape a string for database input
	 */
	public static String escape(String s){
		if(s==null)
			return s;
		
		s = s.replaceAll("'", "''");
		return s;
	}
}
