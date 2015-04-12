/*
 * GeoSolutions Android map Library - Digital field mapping on Android based devices
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
package it.geosolutions.android.map.database;

import it.geosolutions.android.map.database.spatialite.SpatialiteDataSourceHandler;
import it.geosolutions.android.map.style.StyleManager;
import it.geosolutions.android.map.utils.MapFilesProvider;
import it.geosolutions.android.map.utils.Coordinates.Coordinates_Query;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import jsqlite.Exception;
import android.content.Context;
import android.os.Bundle;
import eu.geopaparazzi.spatialite.database.spatial.core.ISpatialDatabaseHandler;
import eu.geopaparazzi.spatialite.database.spatial.core.MbtilesDatabaseHandler;
import eu.geopaparazzi.spatialite.database.spatial.core.OrderComparator;
import eu.geopaparazzi.spatialite.database.spatial.core.SpatialRasterTable;
import eu.geopaparazzi.spatialite.database.spatial.core.SpatialVectorTable;
import eu.geopaparazzi.spatialite.database.spatial.core.Style;

/**
 * Rewrite SpatialDatabaseManager to fix bugs and improve usage
 * 
 * @author Lorenzo Natali (www.geo-solutions.it)
 */
public class SpatialDataSourceManager {

    private List<ISpatialDatabaseHandler> sdbHandlers = null;
    private HashMap<SpatialVectorTable, SpatialDataSourceHandler> vectorTablesMap = new HashMap<SpatialVectorTable, SpatialDataSourceHandler>();
    private HashMap<SpatialRasterTable, ISpatialDatabaseHandler> rasterTablesMap = new HashMap<SpatialRasterTable, ISpatialDatabaseHandler>();

    private static SpatialDataSourceManager spatialDbManager = null;
    private SpatialDataSourceManager() {
    }

    public static SpatialDataSourceManager getInstance() {
        if (spatialDbManager == null) {
            spatialDbManager = new SpatialDataSourceManager();
        }
        return spatialDbManager;
    }

    public static void reset() {
        spatialDbManager = null;
    }
    
    /**
     * Resets the {@link ISpatialDatabaseHandler} list
     */
    public void clear(){
    	if(sdbHandlers != null){    		
    		sdbHandlers.clear();
    	}
    }
    //legacy --> update calling applications
    @Deprecated
    public void init(Context context, File mapsDir ) {
    	init(mapsDir);
    }
    
    public void init( File mapsDir ) {
    
    	sdbHandlers = new ArrayList<ISpatialDatabaseHandler>();
    	
    	if(mapsDir != null){
	        File[] sqliteFiles = mapsDir.listFiles(new FilenameFilter(){
	            public boolean accept( File dir, String filename ) {
	                return filename.endsWith(".sqlite") || filename.endsWith(".mbtiles");
	            }
	        });
	        
	        if(sqliteFiles == null){
	        	// No acceptable file found
	        	return;
	        }
	        
	        for( File sqliteFile : sqliteFiles ) {
	            ISpatialDatabaseHandler sdb = null;
	            if (sqliteFile.getName().endsWith("mbtiles")) {
	                sdb = new MbtilesDatabaseHandler(sqliteFile.getAbsolutePath());
	            } else {
	                sdb = new SpatialiteDataSourceHandler(sqliteFile.getAbsolutePath());
	            }
	            sdbHandlers.add(sdb);
	        }
    	}
        
    }
    /**
     * "lazy" instantiation of sdbHandlers -->
     * 	this will return always the available handlers
     * @return list of ISpatialDatabaseHandlers
     */
    public List<ISpatialDatabaseHandler> getSpatialDatabaseHandlers() {
    	if(sdbHandlers == null){
    		init(MapFilesProvider.getBaseDirectoryFile());
    	}
        return sdbHandlers;
    }

    public List<SpatialVectorTable> getSpatialVectorTables( boolean forceRead ) throws Exception {
        List<SpatialVectorTable> tables = new ArrayList<SpatialVectorTable>();
        for( ISpatialDatabaseHandler sdbHandler : getSpatialDatabaseHandlers() ) {
            List<SpatialVectorTable> spatialTables = sdbHandler.getSpatialVectorTables(forceRead);
            for( SpatialVectorTable spatialTable : spatialTables ) {
                tables.add(spatialTable);
                vectorTablesMap.put(spatialTable, (SpatialDataSourceHandler) sdbHandler);
            }
        }

        Collections.sort(tables, new OrderComparator());
        // set proper order index across tables
        for( int i = 0; i < tables.size(); i++ ) {
        	Style s = StyleManager.getInstance().getStyle(tables.get(i).getName());
        	if(s != null){
        		s.order = i;
        	}            
        }
        return tables;
    }

    public synchronized List<SpatialRasterTable> getSpatialRasterTables( boolean forceRead ) throws Exception {
        List<SpatialRasterTable> tables = new ArrayList<SpatialRasterTable>();
        for( ISpatialDatabaseHandler sdbHandler : getSpatialDatabaseHandlers() ) {
            try {
                List<SpatialRasterTable> spatialTables = sdbHandler.getSpatialRasterTables(forceRead);
                for( SpatialRasterTable spatialTable : spatialTables ) {
                    tables.add(spatialTable);
                    rasterTablesMap.put(spatialTable, sdbHandler);
                }
            } catch (java.lang.Exception e) {
                // ignore the handler and try to g on
            }
        }
        // Collections.sort(tables, new OrderComparator());
        return tables;
    }

    public ISpatialDatabaseHandler getVectorHandler( SpatialVectorTable spatialTable ) throws Exception {
        ISpatialDatabaseHandler spatialDatabaseHandler = vectorTablesMap.get(spatialTable);
        return spatialDatabaseHandler;
    }

    public ISpatialDatabaseHandler getRasterHandler( SpatialRasterTable spatialTable ) throws Exception {
        ISpatialDatabaseHandler spatialDatabaseHandler = rasterTablesMap.get(spatialTable);
        return spatialDatabaseHandler;
    }

    public SpatialVectorTable getVectorTableByName( String table ) throws Exception {
        List<SpatialVectorTable> spatialTables = getSpatialVectorTables(false);
        for( SpatialVectorTable spatialTable : spatialTables ) {
            if (spatialTable.getName().equals(table)) {
                return spatialTable;
            }
        }
        return null;
    }

    public SpatialRasterTable getRasterTableByName( String table ) throws Exception {
        List<SpatialRasterTable> spatialTables = getSpatialRasterTables(false);
        for( SpatialRasterTable spatialTable : spatialTables ) {
            if (spatialTable.getTableName().equals(table)) {
                return spatialTable;
            }
        }
        return null;
    }

    public void intersectionToString( String boundsSrid, SpatialVectorTable spatialTable, double n, double s, double e, double w,
            StringBuilder sb, String indentStr ) throws Exception {
        ISpatialDatabaseHandler spatialDatabaseHandler = vectorTablesMap.get(spatialTable);
        spatialDatabaseHandler.intersectionToStringBBOX(boundsSrid, spatialTable, n, s, e, w, sb, indentStr);
    }

    public void intersectionToString( String boundsSrid, SpatialVectorTable spatialTable, double n, double e, StringBuilder sb,
            String indentStr ) throws Exception {
        ISpatialDatabaseHandler spatialDatabaseHandler = vectorTablesMap.get(spatialTable);
        spatialDatabaseHandler.intersectionToString4Polygon(boundsSrid, spatialTable, n, e, sb, indentStr);
    }

    public void closeDatabases() throws Exception {
        for( ISpatialDatabaseHandler sdbHandler : getSpatialDatabaseHandlers() ) {
            sdbHandler.close();
        }
    }
    /**
     * Query a bbox and returns an array of <Bundle> mapped as attributeName->attributeValue
     * *NOTE*: max 10 results for now
     * @param boundsSrid
     * @param spatialTable
     * @param n
     * @param s
     * @param e
     * @param w
     * @return
     * @throws java.lang.Exception
     */
    public ArrayList<Bundle> intersectionToBundle( String boundsSrid, SpatialVectorTable spatialTable, double n, double s, double e, double w
         ) throws java.lang.Exception{
         return getSpatialDataSourceHandler(spatialTable).intersectionToBundleBBOX(boundsSrid, spatialTable, n, s, e, w);//TODO allow pass these parameters
    }
    
    /**
     * Query a circle and returns an array of <Bundle> mapped as attributeName->attributeValue
     * *NOTE*: max 10 results for now
     * @param boundsSrid
     * @param spatialTable
     * @param x
     * @param y
     * @param radius
     * @return
     * @throws java.lang.Exception
     */
    public ArrayList<Bundle> intersectionToCircleBox( String boundsSrid, SpatialVectorTable spatialTable, double x, double y, double radius) throws java.lang.Exception{
         return getSpatialDataSourceHandler(spatialTable).intersectionToCircleBOX(boundsSrid, spatialTable, x, y, radius);//TODO allow pass these parameters
    }
    
    /**
     * Get the Handler for the Table
     * @param table
     * @return
     */
    public SpatialDataSourceHandler getSpatialDataSourceHandler(SpatialVectorTable table){
   	 return vectorTablesMap.get(table);
    }
    
    /**
     * Get the Handler for the Table
     * @param table
     * @return
     */
    public ISpatialDatabaseHandler getSpatialDataSourceHandler(SpatialRasterTable table){
   	 	return rasterTablesMap.get(table);
    }
    /**
     * Query a polygon and returns an array of <Bundle> mapped as attributeName->attributeValue
     * *NOTE*: max 10 results for now
     * @param boundsSrid
     * @param spatialTable
     * @param polygon_points
     * @return
     * @throws java.lang.Exception
     */
    public ArrayList<Bundle> intersectionToPolygonBox( String boundsSrid, SpatialVectorTable spatialTable, ArrayList<Coordinates_Query> polygon_points) throws java.lang.Exception{
         return getSpatialDataSourceHandler(spatialTable).intersectionToPolygonBOX(boundsSrid, spatialTable, polygon_points);//TODO allow pass these parameters
    }
}