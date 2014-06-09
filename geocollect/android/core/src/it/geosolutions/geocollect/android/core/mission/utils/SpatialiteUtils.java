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

import android.util.Log;
import jsqlite.Stmt;

/**
 * Utils for Spatialite database
 * @author Lorenzo Pini (lorenzo.pini@geo-solutions.it)
 */
public class SpatialiteUtils {
	
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
	 */
	public static boolean checkOrCreateTable(jsqlite.Database db, String tableName){
		
		if (db != null){
			
	        String query = "SELECT name FROM sqlite_master WHERE type='table' AND name='"+tableName+"'";

	        boolean found = false;
	        try {
	            Stmt stmt = db.prepare(query);
	            if( stmt.step() ) {
	                String nomeStr = stmt.column_string(0);
	                found = true;
	                Log.v("SPATIALITE_UTILS", "Found table: "+nomeStr);
	            }
	            stmt.close();
	        } catch (Exception e) {
	            Log.e("SPATIALITE_UTILS", Log.getStackTraceString(e));
	            return false;
	        }

			if(found){
				return true;
			}else{
				// Table not found creating
                Log.v("SPATIALITE_UTILS", "Table "+tableName+" not found, creating..");
				
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
                			"'QUANTITA_PRESUNTA' NUMERIC);";

                	String add_geom_stmt = "SELECT AddGeometryColumn('punti_accumulo_data', 'GEOMETRY', 4326, 'POINT', 'XY');";
                	String create_idx_stmt = "SELECT CreateSpatialIndex('punti_accumulo_data', 'GEOMETRY');";
                    
                	// TODO: check if all statements are complete
                	
                	try {                	
                		Stmt stmt01 = db.prepare(create_stmt);

						if (stmt01.step()) {
							//TODO This will never happen, CREATE statements return empty results
						    Log.v("UTILS", "Table Created");
						}
						
						// TODO: Check if created, fail otherwise
						
						stmt01 = db.prepare(add_geom_stmt);
						if (stmt01.step()) {
						    Log.v("UTILS", "Geometry Column Added "+stmt01.column_string(0));
						}
						
						stmt01 = db.prepare(create_idx_stmt);
						if (stmt01.step()) {
						    Log.v("UTILS", "Index Created");
						}
						
						stmt01.close();
						
					} catch (jsqlite.Exception e) {
						Log.e("UTILS", Log.getStackTraceString(e));
					}
                	return true;
                }
			}
		}else{
			Log.w("UTILS", "No valid database received, aborting..");
		}
		
		return false;
	}
	
	
	/**
	 * Initializes the table if not exists
	 * @param db
	 * @param tableName
	 * @return

	public static boolean initializeTable(jsqlite.Database db, String tableName){
		
		
		return false;
		
	}
	 */
}
