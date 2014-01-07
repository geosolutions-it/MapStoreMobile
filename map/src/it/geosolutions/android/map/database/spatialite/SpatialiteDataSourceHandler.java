/*
 * GeoSolutions map - Digital field mapping on Android based devices
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
package it.geosolutions.android.map.database.spatialite;

import it.geosolutions.android.map.database.SpatialDataSourceHandler;
import it.geosolutions.android.map.model.Attribute;
import it.geosolutions.android.map.model.Feature;
import it.geosolutions.android.map.utils.Coordinates_Query;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jsqlite.Database;
import jsqlite.Exception;
import jsqlite.Stmt;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.os.Bundle;
import android.util.Log;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKBReader;

import eu.geopaparazzi.spatialite.database.spatial.core.GeometryIterator;
import eu.geopaparazzi.spatialite.database.spatial.core.GeometryType;
import eu.geopaparazzi.spatialite.database.spatial.core.OrderComparator;
import eu.geopaparazzi.spatialite.database.spatial.core.SpatialRasterTable;
import eu.geopaparazzi.spatialite.database.spatial.core.SpatialVectorTable;
import eu.geopaparazzi.spatialite.database.spatial.core.Style;

/**
 * @author Lorenzo Natali (www.geo-solutions.it)
 *
 */
public class SpatialiteDataSourceHandler implements SpatialDataSourceHandler{

	private static final String METADATA_TABLE_GEOPACKAGE_CONTENTS = "geopackage_contents";
    private static final String METADATA_TABLE_TILE_MATRIX = "tile_matrix_metadata";
    private static final String METADATA_TABLE_RASTER_COLUMNS = "raster_columns";
    private static final String METADATA_TABLE_GEOMETRY_COLUMNS = "geometry_columns";

    private static final String METADATA_GEOPACKAGECONTENT_TABLE_NAME = "table_name";
    private static final String METADATA_TILE_TABLE_NAME = "t_table_name";
    private static final String METADATA_ZOOM_LEVEL = "zoom_level";
    private static final String METADATA_RASTER_COLUMN = "r_raster_column";
    private static final String METADATA_RASTER_TABLE_NAME = "r_table_name";
    private static final String METADATA_SRID = "srid";
    private static final String METADATA_GEOMETRY_TYPE4 = "geometry_type";
    private static final String METADATA_GEOMETRY_TYPE3 = "type";
    private static final String METADATA_GEOMETRY_COLUMN = "f_geometry_column";
    private static final String METADATA_TABLE_NAME = "f_table_name";
    private static final String ORDER_BY_DEFAULT_FIELD = "ROWID";
    private static final String DEFAULT_GEOMETRY_NAME ="MAPSTORE_9384_GEOMETRY";

    private Database db;

    private HashMap<String, Paint> fillPaints = new HashMap<String, Paint>();
    private HashMap<String, Paint> strokePaints = new HashMap<String, Paint>();

    private List<SpatialVectorTable> vectorTableList;
    private List<SpatialRasterTable> rasterTableList;
    private String fileName;
    
    public SpatialiteDataSourceHandler( String dbPath ) {
        try {
            File spatialDbFile = new File(dbPath);
            if (!spatialDbFile.getParentFile().exists()) {
                throw new RuntimeException();
            }
            db = new jsqlite.Database();
            db.open(spatialDbFile.getAbsolutePath(), jsqlite.Constants.SQLITE_OPEN_READWRITE
                    | jsqlite.Constants.SQLITE_OPEN_CREATE);
            fileName = spatialDbFile.getName();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getFileName() {
        return fileName;
    }

    /**
     * Get the version of Spatialite.
     * 
     * @return the version of Spatialite.
     * @throws Exception
     */
    public String getSpatialiteVersion() throws Exception {
        Stmt stmt = db.prepare("SELECT spatialite_version();");
        try {
            if (stmt.step()) {
                String value = stmt.column_string(0);
                return value;
            }
        } finally {
            stmt.close();
        }
        return "-";
    }

    /**
     * Get the version of proj.
     * 
     * @return the version of proj.
     * @throws Exception
     */
    public String getProj4Version() throws Exception {
        Stmt stmt = db.prepare("SELECT proj4_version();");
        try {
            if (stmt.step()) {
                String value = stmt.column_string(0);
                return value;
            }
        } finally {
            stmt.close();
        }
        return "-";
    }

    /**
     * Get the version of geos.
     * 
     * @return the version of geos.
     * @throws Exception
     */
    public String getGeosVersion() throws Exception {
        Stmt stmt = db.prepare("SELECT geos_version();");
        try {
            if (stmt.step()) {
                String value = stmt.column_string(0);
                return value;
            }
        } finally {
            stmt.close();
        }
        return "-";
    }
	@Override
    public List<SpatialVectorTable> getSpatialVectorTables( boolean forceRead ) throws Exception {
        if (vectorTableList == null || forceRead) {
            vectorTableList = new ArrayList<SpatialVectorTable>();

            StringBuilder sb3 = new StringBuilder();
            sb3.append("select ");
            sb3.append(METADATA_TABLE_NAME);
            sb3.append(", ");
            sb3.append(METADATA_GEOMETRY_COLUMN);
            sb3.append(", ");
            sb3.append(METADATA_GEOMETRY_TYPE3);
            sb3.append(",");
            sb3.append(METADATA_SRID);
            sb3.append(" from ");
            sb3.append(METADATA_TABLE_GEOMETRY_COLUMNS);
            sb3.append(";");
            String query3 = sb3.toString();

            boolean is3 = true;
            Stmt stmt = null;
            try {
                stmt = db.prepare(query3);
            } catch (java.lang.Exception e) {
                // try with spatialite 4 syntax
                StringBuilder sb4 = new StringBuilder();
                sb4.append("select ");
                sb4.append(METADATA_TABLE_NAME);
                sb4.append(", ");
                sb4.append(METADATA_GEOMETRY_COLUMN);
                sb4.append(", ");
                sb4.append(METADATA_GEOMETRY_TYPE4);
                sb4.append(",");
                sb4.append(METADATA_SRID);
                sb4.append(" from ");
                sb4.append(METADATA_TABLE_GEOMETRY_COLUMNS);
                sb4.append(";");
                String query4 = sb4.toString();
                stmt = db.prepare(query4);
                is3 = false;
            }
            try {
                while( stmt.step() ) {
                    String name = stmt.column_string(0);
                    String geomName = stmt.column_string(1);

                    int geomType = 0;
                    if (is3) {
                        String type = stmt.column_string(2);
                        geomType = GeometryType.forValue(type);
                    } else {
                        geomType = stmt.column_int(2);
                    }

                    String srid = String.valueOf(stmt.column_int(3));
                    SpatialVectorTable table = new SpatialVectorTable(name, geomName, geomType, srid);
                    vectorTableList.add(table);
                }
            } finally {
                stmt.close();
            }    
        }
        OrderComparator orderComparator = new OrderComparator();
        Collections.sort(vectorTableList, orderComparator);

        return vectorTableList;
    }

    @Override
    public List<SpatialRasterTable> getSpatialRasterTables( boolean forceRead ) throws Exception {
        if (rasterTableList == null || forceRead) {
            rasterTableList = new ArrayList<SpatialRasterTable>();

            StringBuilder sb = new StringBuilder();
            sb.append("select ");
            sb.append(METADATA_RASTER_TABLE_NAME);
            sb.append(", ");
            sb.append(METADATA_RASTER_COLUMN);
            sb.append(", srid from ");
            sb.append(METADATA_TABLE_RASTER_COLUMNS);
            sb.append(";");
            String query = sb.toString();
            Stmt stmt = db.prepare(query);
            try {
                while( stmt.step() ) {
                    String tableName = stmt.column_string(0);
                    String columnName = stmt.column_string(1);
                    String srid = String.valueOf(stmt.column_int(2));

                    if (tableName != null) {
                        int[] zoomLevels = {0, 18};
                        getZoomLevels(tableName, zoomLevels);

                        double[] centerCoordinate = {0.0, 0.0};
                        getCenterCoordinate4326(tableName, centerCoordinate);

                        SpatialRasterTable table = new SpatialRasterTable(tableName, columnName, srid, zoomLevels[0],
                                zoomLevels[1], centerCoordinate[0], centerCoordinate[1], null);
                        rasterTableList.add(table);
                    }

                }
            } finally {
                stmt.close();
            }
        }
        // OrderComparator orderComparator = new OrderComparator();
        // Collections.sort(rasterTableList, orderComparator);

        return rasterTableList;
    }

    /**
     * Extract the center coordinate of a raster tileset.
     * 
     * @param tableName the raster table name.
     * @param centerCoordinate teh coordinate array to update with the extracted values.
     */
    private void getCenterCoordinate4326( String tableName, double[] centerCoordinate ) {
        try {
            Stmt centerStmt = null;
            try {
                WKBReader wkbReader = new WKBReader();

                StringBuilder centerBuilder = new StringBuilder();
                centerBuilder.append("select ST_AsBinary(CastToXY(ST_Transform(MakePoint(");
                // centerBuilder.append("select AsText(ST_Transform(MakePoint(");
                centerBuilder.append("(min_x + (max_x-min_x)/2), ");
                centerBuilder.append("(min_y + (max_y-min_y)/2), ");
                centerBuilder.append(METADATA_SRID);
                centerBuilder.append("), 4326))) from ");
                centerBuilder.append(METADATA_TABLE_GEOPACKAGE_CONTENTS);
                centerBuilder.append(" where ");
                centerBuilder.append(METADATA_GEOPACKAGECONTENT_TABLE_NAME);
                centerBuilder.append("='");
                centerBuilder.append(tableName);
                centerBuilder.append("';");
                String centerQuery = centerBuilder.toString();

                centerStmt = db.prepare(centerQuery);
                if (centerStmt.step()) {
                    // String geomBytes = centerStmt.column_string(0);
                    // System.out.println();
                    byte[] geomBytes = centerStmt.column_bytes(0);
                    Geometry geometry = wkbReader.read(geomBytes);
                    Coordinate coordinate = geometry.getCoordinate();
                    centerCoordinate[0] = coordinate.x;
                    centerCoordinate[1] = coordinate.y;
                }
            } finally {
                if (centerStmt != null)
                    centerStmt.close();
            }
        } catch (java.lang.Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the available zoomlevels for a raster table.
     * 
     * @param tableName the raster table name.
     * @param zoomLevels the zoomlevels array to update with the min and max levels available.
     * @throws Exception
     */
    private void getZoomLevels( String tableName, int[] zoomLevels ) throws Exception {
        Stmt zoomStmt = null;
        try {
            StringBuilder zoomBuilder = new StringBuilder();
            zoomBuilder.append("SELECT min(");
            zoomBuilder.append(METADATA_ZOOM_LEVEL);
            zoomBuilder.append("),max(");
            zoomBuilder.append(METADATA_ZOOM_LEVEL);
            zoomBuilder.append(") FROM ");
            zoomBuilder.append(METADATA_TABLE_TILE_MATRIX);
            zoomBuilder.append(" WHERE ");
            zoomBuilder.append(METADATA_TILE_TABLE_NAME);
            zoomBuilder.append("='");
            zoomBuilder.append(tableName);
            zoomBuilder.append("';");
            String zoomQuery = zoomBuilder.toString();
            zoomStmt = db.prepare(zoomQuery);
            if (zoomStmt.step()) {
                zoomLevels[0] = zoomStmt.column_int(0);
                zoomLevels[1] = zoomStmt.column_int(1);
            }
        } finally {
            if (zoomStmt != null)
                zoomStmt.close();
        }
    }

    public float[] getTableBounds( SpatialVectorTable spatialTable, String destSrid ) throws Exception {
        boolean doTransform = false;
        if (!spatialTable.getSrid().equals(destSrid)) {
            doTransform = true;
        }

        StringBuilder geomSb = new StringBuilder();
        if (doTransform)
            geomSb.append("ST_Transform(");
        geomSb.append(spatialTable.getGeomName());
        if (doTransform) {
            geomSb.append(", ");
            geomSb.append(destSrid);
            geomSb.append(")");
        }
        String geom = geomSb.toString();

        StringBuilder qSb = new StringBuilder();
        qSb.append("SELECT Min(MbrMinX(");
        qSb.append(geom);
        qSb.append(")) AS min_x, Min(MbrMinY(");
        qSb.append(geom);
        qSb.append(")) AS min_y,");
        qSb.append("Max(MbrMaxX(");
        qSb.append(geom);
        qSb.append(")) AS max_x, Max(MbrMaxY(");
        qSb.append(geom);
        qSb.append(")) AS max_y");
        qSb.append(" FROM \"");
        qSb.append(spatialTable.getName());
        qSb.append("\";");

        String selectQuery = qSb.toString();
        Stmt stmt = db.prepare(selectQuery);
        try {
            if (stmt.step()) {
                float w = (float) stmt.column_double(0);
                float s = (float) stmt.column_double(1);
                float e = (float) stmt.column_double(2);
                float n = (float) stmt.column_double(3);

                return new float[]{n, s, e, w};
            }
        } finally {
            stmt.close();
        }
        return null;
    }

    @Override
    public Paint getFillPaint4Style( Style style ) {
        Paint paint = fillPaints.get(style.name);
        if (paint == null) {
            paint = new Paint();
            fillPaints.put(style.name, paint);
        }
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor(style.fillcolor));
        float alpha = style.fillalpha * 255f;
        paint.setAlpha((int) alpha);
        return paint;
    }

    @Override
    public Paint getStrokePaint4Style( Style style ) {
        Paint paint = strokePaints.get(style.name);
        if (paint == null) {
            paint = new Paint();
            strokePaints.put(style.name, paint);
        }
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeCap(Cap.ROUND);
        paint.setStrokeJoin(Join.ROUND);
        paint.setColor(Color.parseColor(style.strokecolor));
        float alpha = style.strokealpha * 255f;
        paint.setAlpha((int) alpha);
        paint.setStrokeWidth(style.width);
        return paint;
    }

    public List<byte[]> getWKBFromTableInBounds( String destSrid, SpatialVectorTable table, double n, double s, double e, double w ) {
        List<byte[]> list = new ArrayList<byte[]>();
        String query = buildGeometriesInBoundsQuery(destSrid, table, n, s, e, w);
        try {
            Stmt stmt = db.prepare(query);
            try {
                while( stmt.step() ) {
                    list.add(stmt.column_bytes(0));
                }
            } finally {
                stmt.close();
            }
            return list;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public byte[] getRasterTile( String query ) {
        try {
            Stmt stmt = db.prepare(query);
            try {
                if (stmt.step()) {
                    byte[] bytes = stmt.column_bytes(0);
                    return bytes;
                }
            } finally {
                stmt.close();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public GeometryIterator getGeometryIteratorInBounds( String destSrid, SpatialVectorTable table, double n, double s, double e,
            double w ) {
        String query = buildGeometriesInBoundsQuery(destSrid, table, n, s, e, w);
        return new GeometryIterator(db, query);
    }

    private String buildGeometriesInBoundsQuery( String destSrid, SpatialVectorTable table, double n, double s, double e, double w ) {
        boolean doTransform = false;
        if (!table.getSrid().equals(destSrid)) {
            doTransform = true;
        }

        StringBuilder mbrSb = new StringBuilder();
        if (doTransform)
            mbrSb.append("ST_Transform(");
        mbrSb.append("BuildMBR(");
        mbrSb.append(w);
        mbrSb.append(", ");
        mbrSb.append(n);
        mbrSb.append(", ");
        mbrSb.append(e);
        mbrSb.append(", ");
        mbrSb.append(s);
        if (doTransform) {
            mbrSb.append(", ");
            mbrSb.append(destSrid);
            mbrSb.append("), ");
            mbrSb.append(table.getSrid());
        }
        mbrSb.append(")");
        String mbr = mbrSb.toString(); 

        StringBuilder qSb = new StringBuilder();
        qSb.append("SELECT ST_AsBinary(CastToXY(");
        if (doTransform)
            qSb.append("ST_Transform(");
        qSb.append(table.getGeomName());
        if (doTransform) {
            qSb.append(", ");
            qSb.append(destSrid);
            qSb.append(")");
        }
        qSb.append("))");
        // qSb.append(", AsText(");
        // if (doTransform)
        // qSb.append("ST_Transform(");
        // qSb.append(table.geomName);
        // if (doTransform) {
        // qSb.append(", ");
        // qSb.append(destSrid);
        // qSb.append(")");
        // }
        // qSb.append(")");
        qSb.append(" FROM \"");
        qSb.append(table.getName());
        qSb.append("\" WHERE ST_Intersects(");
        qSb.append(table.getGeomName());
        qSb.append(", ");
        qSb.append(mbr);
        qSb.append(") = 1");
        qSb.append("   AND ROWID IN (");
        qSb.append("     SELECT ROWID FROM Spatialindex WHERE f_table_name ='");
        qSb.append(table.getName());
        qSb.append("'");
        qSb.append("     AND search_frame = ");
        qSb.append(mbr);
        qSb.append(" );");
        String q = qSb.toString();

        return q;
    }

    public void close() throws Exception {
        if (db != null) {
            db.close();
        }
    }

    public void intersectionToStringBBOX( String boundsSrid, SpatialVectorTable spatialTable, double n, double s, double e,
            double w, StringBuilder sb, String indentStr ) throws Exception {
        boolean doTransform = false;
        if (!spatialTable.getSrid().equals(boundsSrid)) {
            doTransform = true;
        }

        String query = null;


        {
            StringBuilder sbQ = new StringBuilder();
            sbQ.append("SELECT ");
            sbQ.append("*");
            sbQ.append(" from \"").append(spatialTable.getName());
            sbQ.append("\" WHERE ST_Intersects(");
            if (doTransform)
                sbQ.append("ST_Transform(");
            sbQ.append("BuildMBR(");
            sbQ.append(w);
            sbQ.append(", ");
            sbQ.append(s);
            sbQ.append(", ");
            sbQ.append(e);
            sbQ.append(", ");
            sbQ.append(n);
            if (doTransform) {
                sbQ.append(", ");
                sbQ.append(boundsSrid);
                sbQ.append("),");
                sbQ.append(spatialTable.getSrid());
            }
            sbQ.append("),");
            sbQ.append(spatialTable.getGeomName());
            sbQ.append(");");

            query = sbQ.toString();

            // Logger.i(this, query);
        }

        Stmt stmt = db.prepare(query);
        try {
            while( stmt.step() ) {
                int column_count = stmt.column_count();
                for( int i = 0; i < column_count; i++ ) {
                    String cName = stmt.column_name(i);
                    if (cName.equalsIgnoreCase(spatialTable.getGeomName())) {
                        continue;
                    }

                    String value = stmt.column_string(i);
                    sb.append(indentStr).append(cName).append(": ").append(value).append("\n");
                }
                sb.append("\n");
            }
        } finally {
            stmt.close();
        }
    }
    
    /**
     * TODO needs to manage start, sorting...
     */
    @Override
    public ArrayList<Bundle> intersectionToBundleBBOX( String boundsSrid, SpatialVectorTable spatialTable, double n, double s, double e,
            double w,Integer start,Integer limit ) throws Exception  {
        Stmt stmt = generateBBoxQuery(boundsSrid, spatialTable, n, s, e, w,
				start, limit);
        ArrayList<Bundle> features = new ArrayList<Bundle>();
        try {
        	//every row of the table (feature)
            generateBundle(spatialTable, stmt, features);
            
        }catch(Exception ee){
        	Log.e("DATABASE","Error in database query:\nException:"+ee.getMessage());
        	throw ee;
        } finally {
            stmt.close();
        }
        return features;
    }

	/**
	 * @param spatialTable
	 * @param stmt
	 * @param features
	 * @throws Exception
	 */
	private void generateBundle(SpatialVectorTable spatialTable, Stmt stmt,
			ArrayList<Bundle> features) throws Exception {
		while( stmt.step() ) {
		    int column_count = stmt.column_count();
		    Bundle feature= new Bundle();
		    
		    for( int i = 0; i < column_count; i++ ) {
		        String cName = stmt.column_name(i);
		        //skip geometry
		        if (cName.equalsIgnoreCase(spatialTable.getGeomName())) {
		            continue;
		        }
		        feature.putString(cName, stmt.column_string(i));
				//add name->value pairs to the attribute ArrayList
				
		    }
		    features.add(feature);
		    //add the ArrayList
		}
	}

	/**
	 * @param boundsSrid
	 * @param spatialTable
	 * @param n
	 * @param s
	 * @param e
	 * @param w
	 * @param start
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	private Stmt generateBBoxQuery(String boundsSrid,
			SpatialVectorTable spatialTable, double n, double s, double e,
			double w, Integer start, Integer limit) throws Exception {
		boolean doTransform = false;
        if (!spatialTable.getSrid().equals(boundsSrid)) {
            doTransform = true;
        }
    	String query = null;

        {
            StringBuilder sbQ = new StringBuilder();
            sbQ.append("SELECT ");
            sbQ.append("*");
            sbQ.append(" from \"").append(spatialTable.getName());
            sbQ.append("\" WHERE ST_Intersects(");
            if (doTransform)
                sbQ.append("ST_Transform(");
            sbQ.append("BuildMBR(");
            sbQ.append(w);
            sbQ.append(", ");
            sbQ.append(s);
            sbQ.append(", ");
            sbQ.append(e);
            sbQ.append(", ");
            sbQ.append(n);
            if (doTransform) {
                sbQ.append(", ");
                sbQ.append(boundsSrid);
                sbQ.append("),");
                sbQ.append(spatialTable.getSrid());
            }
            sbQ.append("),");
            sbQ.append(spatialTable.getGeomName());
            sbQ.append(")");
            if(limit != null){
	            if(start !=null){
	            	sbQ.append(" ORDER BY  ");
	            	sbQ.append(ORDER_BY_DEFAULT_FIELD);
	            	sbQ.append(" ");
	            }
	            sbQ.append(" LIMIT ");
	            if(start != null){
	            	sbQ.append(start);
		            sbQ.append(",");
		            sbQ.append(start + limit);
	            }else{
	            	sbQ.append(limit);
	            }
            }
            sbQ.append(";");

            query = sbQ.toString();

            // Logger.i(this, query);
        }

        Stmt stmt = db.prepare(query);
		return stmt;
	}
	
	/**
	 * Generates a query for a layer where the 
	 * @param layer
	 * @param attributeName
	 * @param attributeValue
	 * @param start
	 * @param limit
	 * @return
	 * @throws Exception 
	 */
	private Stmt generateQueryByAttributeForGeometry(String destSrid,SpatialVectorTable table, String attributeName,
			String attributeValue, Integer start, Integer limit) throws Exception {
			boolean doTransform = false;
	        if (!table.getSrid().equals(destSrid)) {
	            doTransform = true;
	        }
	    	String query = null;
	
	       
	        StringBuilder sbQ = new StringBuilder();
	        //SELECT
	        sbQ.append("SELECT ST_AsBinary(CastToXY(");
	        if (doTransform)
	        	sbQ.append("ST_Transform(");
	        sbQ.append(table.getGeomName());
	        if (doTransform) {
	        	sbQ.append(", ");
	        	sbQ.append(destSrid);
	        	sbQ.append(")");
	        }
	        sbQ.append(")) AS ");
	        sbQ.append(DEFAULT_GEOMETRY_NAME);
	        //FROM
	        sbQ.append(" FROM \"").append(table.getName());
	        sbQ.append("\" ");
	        //WHERE
	        sbQ.append(" WHERE \"");
	        sbQ.append(attributeName);
	        sbQ.append("\"='"); //TODO now works only for strings
	        sbQ.append(attributeValue);
	        sbQ.append("' ");            //LIMIT & ORDER
	        if(limit != null){
	            if(start !=null){
	            	sbQ.append(" ORDER BY  ");
	            	sbQ.append(ORDER_BY_DEFAULT_FIELD);
	            	sbQ.append(" ");
	            }
	            sbQ.append(" LIMIT ");
	            if(start != null){
	            	sbQ.append(start);
		            sbQ.append(",");
		            sbQ.append(start + limit);
	            }else{
	            	sbQ.append(limit);
	            }
	        }
	        sbQ.append(";");
	
	        query = sbQ.toString();
	        return  db.prepare(query);
       
	}
	private Stmt generateQueryByAttributeForFeature(String destSrid,SpatialVectorTable table, String attributeName,
			String attributeValue, Integer start, Integer limit) throws Exception {
			boolean doTransform = false;
	        if (!table.getSrid().equals(destSrid)) {
	            doTransform = true;
	        }
	    	String query = null;
	
	       
	        StringBuilder sbQ = new StringBuilder();
	        //SELECT
	        sbQ.append("SELECT *");
	        //FROM
	        sbQ.append(" FROM \"").append(table.getName());
	        sbQ.append("\" ");
	        //WHERE
	        sbQ.append(" WHERE \"");
	        sbQ.append(attributeName);
	        sbQ.append("\"='"); //TODO now works only for strings
	        sbQ.append(attributeValue);
	        sbQ.append("' ");            //LIMIT & ORDER
	        if(limit != null){
	            if(start !=null){
	            	sbQ.append(" ORDER BY  ");
	            	sbQ.append(ORDER_BY_DEFAULT_FIELD);
	            	sbQ.append(" ");
	            }
	            sbQ.append(" LIMIT ");
	            if(start != null){
	            	sbQ.append(start);
		            sbQ.append(",");
		            sbQ.append(start + limit);
	            }else{
	            	sbQ.append(limit);
	            }
	        }
	        sbQ.append(";");
	
	        query = sbQ.toString();
	        return  db.prepare(query);
       
	}
    public void intersectionToString4Polygon( String queryPointSrid, SpatialVectorTable spatialTable, double n, double e,
            StringBuilder sb, String indentStr ) throws Exception {
        boolean doTransform = false;
        if (!spatialTable.getSrid().equals(queryPointSrid)) {
            doTransform = true;
        }

        StringBuilder sbQ = new StringBuilder();
        sbQ.append("SELECT * FROM \"");
        sbQ.append(spatialTable.getName());
        sbQ.append("\" WHERE ST_Intersects(");
        sbQ.append(spatialTable.getGeomName());
        sbQ.append(", ");
        if (doTransform)
            sbQ.append("ST_Transform(");
        sbQ.append("MakePoint(");
        sbQ.append(e);
        sbQ.append(",");
        sbQ.append(n);
        if (doTransform) {
            sbQ.append(", ");
            sbQ.append(queryPointSrid);
            sbQ.append("), ");
            sbQ.append(spatialTable.getSrid());
        }
        sbQ.append(")) = 1 ");
        sbQ.append("AND ROWID IN (");
        sbQ.append("SELECT ROWID FROM Spatialindex WHERE f_table_name ='");
        sbQ.append(spatialTable.getName());
        sbQ.append("' AND search_frame = ");
        if (doTransform)
            sbQ.append("ST_Transform(");
        sbQ.append("MakePoint(");
        sbQ.append(e);
        sbQ.append(",");
        sbQ.append(n);
        if (doTransform) {
            sbQ.append(", ");
            sbQ.append(queryPointSrid);
            sbQ.append("), ");
            sbQ.append(spatialTable.getSrid());
        }
        sbQ.append("));");
        String query = sbQ.toString();

        Stmt stmt = db.prepare(query);
        try {
            while( stmt.step() ) {
                int column_count = stmt.column_count();
                for( int i = 0; i < column_count; i++ ) {
                    String cName = stmt.column_name(i);
                    if (cName.equalsIgnoreCase(spatialTable.getGeomName())) {
                        continue;
                    }

                    String value = stmt.column_string(i);
                    sb.append(indentStr).append(cName).append(": ").append(value).append("\n");
                }
                sb.append("\n");
            }
        } finally {
            stmt.close();
        }
    }
    
	/* (non-Javadoc)
	 * @see it.geosolutions.android.map.database.SpatialDataSourceHandler#queryBBox()
	 */
	@Override
	public Map<String, String> queryBBox() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * IMPORTANT Implemented for interface implementation but NOT MANAGED
	 */
	@Override
	public void updateStyle(Style style) throws Exception {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see it.geosolutions.android.map.database.SpatialDataSourceHandler#intersectionToBundleBBOX(java.lang.String, eu.geopaparazzi.spatialite.database.spatial.core.SpatialVectorTable, double, double, double, double)
	 */
	@Override
	public ArrayList<Bundle> intersectionToBundleBBOX(String boundsSrid,
			SpatialVectorTable spatialTable, double n, double s, double e,
			double w) throws Exception {
		return this.intersectionToBundleBBOX(boundsSrid, spatialTable, n, s, e, w,null,null);
		
	}
	
	@Override
	public ArrayList<Map<String,String>> intersectionToMapBBOX(String boundsSrid,
			SpatialVectorTable spatialTable, double n, double s, double e,
			double w,Integer start,Integer limit) throws Exception{
		 Stmt stmt = generateBBoxQuery(boundsSrid, spatialTable, n, s, e, w,
					start, limit);
	        ArrayList<Map<String,String>> features = new ArrayList<Map<String,String>>();
	        try {
	        	//every row of the table (feature)
	            generateMap(spatialTable, stmt, features);
	            
	        }catch(Exception ee){
	        	Log.e("DATABASE","Error in database query:\nException:"+ee.getMessage());
	        	throw ee;
	        } finally {
	            stmt.close();
	        }
	        return features;
	}
	
	@Override
	public ArrayList<Feature> intersectionToFeatureListBBOX(String boundsSrid,
			SpatialVectorTable spatialTable, double n, double s, double e,
			double w,Integer start,Integer limit) throws Exception{
		 Stmt stmt = buildFeatureBBoxQuery(boundsSrid, spatialTable, n, s, e, w,
					start, limit);
	        ArrayList<Feature> features = new ArrayList<Feature>();
	        try {
	        	//every row of the table (feature)
	        	generateAttributes(spatialTable, stmt, features,false);//TODO put out this
	            
	        }catch(Exception ee){
	        	Log.e("DATABASE","Error in database query:\nException:"+ee.getMessage());
	        	throw ee;
	        } finally {
	            stmt.close();
	        }
	        return features;
	}
	
	@Override
	public ArrayList<Feature> intersectionToCircle(String boundsSrid,
			SpatialVectorTable spatialTable, double x, double y, double radius,
			Integer start, Integer limit) throws Exception {
		
		Stmt stmt = buildFeatureCircleQuery(boundsSrid, spatialTable, x, y, radius, start, limit);
        ArrayList<Feature> features = new ArrayList<Feature>();
        try {
        	//every row of the table (feature)
        	generateAttributes(spatialTable, stmt, features, false);//TODO put out this
            
        }catch(Exception ee){
        	Log.e("DATABASE","Error in database query:\nException:"+ee.getMessage());
        	throw ee;
        } finally {
            stmt.close();
        }
        return features;
	}
	
	/**
	 * @param spatialTable
	 * @param stmt
	 * @param features
	 * @throws Exception
	 */
	private void generateMap(SpatialVectorTable spatialTable, Stmt stmt,
			ArrayList<Map<String,String>> features) throws Exception {
		while( stmt.step() ) {
		    int column_count = stmt.column_count();
		    Map<String,String> feature= new HashMap<String,String>();
		    
		    for( int i = 0; i < column_count; i++ ) {
		        String cName = stmt.column_name(i);
		        //skip geometry
		        if (cName.equalsIgnoreCase(spatialTable.getGeomName())) {
		            continue;
		        }
		        feature.put(cName, stmt.column_string(i));
				//add name->value pairs to the attribute ArrayList
				
		    }
		    features.add(feature);
		    //add the ArrayList
		}
	}
	/**
	 * @param spatialTable
	 * @param stmt
	 * @param features
	 * @throws Exception
	 */
	public void generateAttributes(SpatialVectorTable spatialTable, Stmt stmt,
			ArrayList<Feature> features,boolean includeGeometry) throws Exception {
		try{
		while( stmt.step() ) {
		    int column_count = stmt.column_count();
		    Feature feature = new Feature();
		    for( int i = 0; i < column_count; i++ ) {
		    	Attribute attribute= new Attribute();
		        String cName = stmt.column_name(i);
		        //skip geometry
		        if (cName.equalsIgnoreCase(spatialTable.getGeomName() )|| cName.equalsIgnoreCase(DEFAULT_GEOMETRY_NAME)){
		            continue;
		        }
		        attribute.setName(cName);
		        attribute.setValue(stmt.column_string(i));
				//add name->value pairs to the attribute ArrayList
		        feature.add(attribute);
		        
		    }
		    features.add(feature);
		    //add the ArrayList
		}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	private Geometry generateGeometry(SpatialVectorTable spatialTable, Stmt stmt,
			ArrayList<Feature> features,boolean includeGeometry) throws Exception{
        WKBReader wkbReader = new WKBReader();
		while( stmt.step() ) {
		    int column_count = stmt.column_count();
		    for( int i = 0; i < column_count; i++ ) {
		    	String cName = stmt.column_name(i);
		    	
		        if (!cName.equalsIgnoreCase(DEFAULT_GEOMETRY_NAME)) {
		            continue;
		        }else{
		        	 byte[] geomBytes = stmt.column_bytes(0);
		             Geometry geometry;
					try {
						geometry = wkbReader.read(geomBytes);
					} catch (ParseException e) {
						Log.e("DATABASE","Error reading geometry");
						throw new Exception(e.getMessage());
					}
		             return geometry;
		        } 
		    }
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see it.geosolutions.android.map.database.SpatialDataSourceHandler#getFeatureByAttribute(java.lang.String, java.lang.String, java.lang.String, java.lang.Object, int, boolean)
	 */
	@Override
	public Geometry getGeometryByAttribute(String srid,SpatialVectorTable table, String attributeName,
			String attributeValue, Integer start, Integer limit, boolean getGeometry) throws Exception {
		Stmt stmt = generateQueryByAttributeForGeometry(srid, table, attributeName, attributeValue, start,limit);
		ArrayList<Feature> features = new ArrayList<Feature>();
		try {
			
        	return generateGeometry(table, stmt, features,true);
            
        }catch(Exception ee){
        	Log.e("DATABASE","Error in database query:\nException:"+ee.getMessage());
        	throw ee;
        } finally {
            stmt.close();
        }
		
	}
	@Override
	public ArrayList<Feature> getFeaturesByAttribute(String srid,SpatialVectorTable table, String attributeName,
			String attributeValue, Integer start, Integer limit, boolean getGeometry) throws Exception {
		Stmt stmt = generateQueryByAttributeForFeature(srid, table, attributeName, attributeValue, start,limit);
        ArrayList<Feature> features = new ArrayList<Feature>();
        try {
        	//every row of the table (feature)
        	generateAttributes(table, stmt, features,false);
            
        }catch(Exception ee){
        	Log.e("DATABASE","Error in database query:\nException:"+ee.getMessage());
        	throw ee;
        } finally {
            stmt.close();
        }
        return features;
	}

	/**
	 * Intersects a bounding box using spatial index
	 * @param destSrid
	 * @param table
	 * @param n
	 * @param s
	 * @param e
	 * @param w
	 * @param start
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	private Stmt buildFeatureBBoxQuery( String destSrid, SpatialVectorTable table, double n, double s, double e, double w,Integer start,Integer limit ) throws Exception {
		boolean doTransform = false;
        if (!table.getSrid().equals(destSrid)) {
            doTransform = true;
        }

        StringBuilder mbrSb = new StringBuilder();
        if (doTransform)
            mbrSb.append("ST_Transform(");
        mbrSb.append("BuildMBR(");
        mbrSb.append(w);
        mbrSb.append(", ");
        mbrSb.append(n);
        mbrSb.append(", ");
        mbrSb.append(e);
        mbrSb.append(", ");
        mbrSb.append(s);
        if (doTransform) {
            mbrSb.append(", ");
            mbrSb.append(destSrid);
            mbrSb.append("), ");
            mbrSb.append(table.getSrid());
        }
        mbrSb.append(")");
        String mbr = mbrSb.toString(); 

        StringBuilder qSb = new StringBuilder();
        qSb.append("SELECT *,ST_AsBinary(CastToXY(");
        if (doTransform)
            qSb.append("ST_Transform(");
        qSb.append(table.getGeomName());
        if (doTransform) {
            qSb.append(", ");
            qSb.append(destSrid);
            qSb.append(")");
        }
        qSb.append("))AS ");
        qSb.append(DEFAULT_GEOMETRY_NAME);
        // qSb.append(", AsText(");
        // if (doTransform)
        // qSb.append("ST_Transform(");
        // qSb.append(table.geomName);
        // if (doTransform) {
        // qSb.append(", ");
        // qSb.append(destSrid);
        // qSb.append(")");
        // }
        // qSb.append(")");
        qSb.append(" FROM \"");
        qSb.append(table.getName());
        qSb.append("\" WHERE ST_Intersects(");
        qSb.append(table.getGeomName());
        qSb.append(", ");
        qSb.append(mbr);
        qSb.append(") = 1");
        qSb.append("   AND ROWID IN (");
        qSb.append("     SELECT ROWID FROM Spatialindex WHERE f_table_name ='");
        qSb.append(table.getName());
        qSb.append("'");
        qSb.append("     AND search_frame = ");
        qSb.append(mbr);
        qSb.append(" )");
        if(limit != null){
            if(start !=null){
            	qSb.append(" ORDER BY  ");
            	qSb.append(ORDER_BY_DEFAULT_FIELD);
            	qSb.append(" ");
            }
            qSb.append(" LIMIT ");
            if(start != null){
            	qSb.append(start);
            	qSb.append(",");
            	qSb.append(start + limit);
            }else{
            	qSb.append(limit);
            }
        }
        qSb.append(";");
        String q = qSb.toString();
        Log.v("ID",q);
        Stmt stmt = db.prepare(q);
		return stmt;
    }
	
	/**
	 * Intersect a circle using spatial index
	 * @param destSrid
	 * @param table
	 * @param x
	 * @param y
	 * @param radius
	 * @param start
	 * @param limit
	 * @return
	 * @throws Exception
	 */
	public Stmt buildFeatureCircleQuery(String destSrid, SpatialVectorTable table, double x,
			double y, double radius, Integer start, Integer limit) throws Exception {
		boolean doTransform = false;
        if (!table.getSrid().equals(destSrid)) {
            doTransform = true;
        }

        StringBuilder mbrSb = new StringBuilder();
        if (doTransform)
            mbrSb.append("ST_Transform(");
        
        mbrSb.append("MakePoint(");
        mbrSb.append(x);
        mbrSb.append(" , ");
        mbrSb.append(y);
        if (doTransform) {
            mbrSb.append(", ");
            mbrSb.append(destSrid);
            mbrSb.append("), ");
            mbrSb.append(table.getSrid());
        }
        mbrSb.append(")");
        String mbr = mbrSb.toString(); 

        StringBuilder qSb = new StringBuilder();
        qSb.append("SELECT *,ST_AsBinary(CastToXY(");
        if (doTransform)
            qSb.append("ST_Transform(");
        qSb.append(table.getGeomName());
        if (doTransform) {
            qSb.append(", ");
            qSb.append(destSrid);
            qSb.append(")");
        }
        qSb.append("))AS ");
        qSb.append(DEFAULT_GEOMETRY_NAME);
        // qSb.append(", AsText(");
        // if (doTransform)
        // qSb.append("ST_Transform(");
        // qSb.append(table.geomName);
        // if (doTransform) {
        // qSb.append(", ");
        // qSb.append(destSrid);
        // qSb.append(")");
        // }
        // qSb.append(")");
        qSb.append(" FROM \"");
        qSb.append(table.getName());
        qSb.append("\" WHERE ST_Distance(");
        qSb.append(table.getGeomName());
        qSb.append(", ");
        qSb.append(mbr);
        qSb.append(") <= ");
        //double distance = radius + stroke_width;
        qSb.append(Double.toString(radius));
        qSb.append(" ");
        /*qSb.append("   AND ROWID IN (");
        qSb.append("     SELECT ROWID FROM Spatialindex WHERE f_table_name ='");
        qSb.append(table.getName());
        qSb.append("'");
        qSb.append("     AND search_frame = ");
        qSb.append(mbr);
        qSb.append(" )");*/
        if(limit != null){
            if(start !=null){
            	qSb.append(" ORDER BY  ");
            	qSb.append(ORDER_BY_DEFAULT_FIELD);
            	qSb.append(" ");
            }
            qSb.append(" LIMIT ");
            if(start != null){
            	qSb.append(start);
            	qSb.append(",");
            	qSb.append(start + limit);
            }else{
            	qSb.append(limit);
            }
        }
        qSb.append(";");
        String q = qSb.toString();
        Log.v("Query circular",""+q);

        Stmt stmt = db.prepare(q);
		return stmt;		
	}

	@Override
	public ArrayList<Bundle> intersectionToCircleBOX(String boundsSrid,
			SpatialVectorTable spatialTable, double x, double y, double radius)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Bundle> intersectionToCircleBOX(String boundsSrid,
			SpatialVectorTable spatialTable, double x, double y, double radius,
			Integer start, Integer limit) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	/*
	@Override
	public ArrayList<Bundle> intersectionToPolygonBOX(String boundsSrid,
			SpatialVectorTable spatialTable,
			ArrayList<Coordinates_Query> polygon_points) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Bundle> intersectionToPolygonBOX(String boundsSrid,
			SpatialVectorTable spatialTable,
			ArrayList<Coordinates_Query> polygon_points, Integer start,
			Integer limit) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<Feature> intersectionToPolygon(String boundsSrid,
			SpatialVectorTable spatialTable, ArrayList<Coordinates_Query> polygon_points,
			Integer start, Integer limit) throws Exception {
		
		Stmt stmt = buildFeaturePolygonQuery(boundsSrid, spatialTable, polygon_points, start, limit);
        ArrayList<Feature> features = new ArrayList<Feature>();
        try {
        	//every row of the table (feature)
        	generateAttributes(spatialTable, stmt, features, false);//TODO put out this
            
        }catch(Exception ee){
        	Log.e("DATABASE","Error in database query:\nException:"+ee.getMessage());
        	throw ee;
        } finally {
            stmt.close();
        }
        return features;
	}

	public Stmt buildFeaturePolygonQuery(String destSrid,
			SpatialVectorTable table,
			ArrayList<Coordinates_Query> polygon_points, Integer start,
			Integer limit) throws Exception {
		
		boolean doTransform = false;
        if (!table.getSrid().equals(destSrid)) {
            doTransform = true;
        }

        StringBuilder mbrSb = new StringBuilder();
        if (doTransform)
            mbrSb.append("ST_Transform(");
        
        mbrSb.append("PolygonFromText('POLYGON((");
        for(int i = 0; i<polygon_points.size(); i++){
            mbrSb.append(polygon_points.get(i).getX());
            mbrSb.append(" ");
            mbrSb.append(polygon_points.get(i).getY());
            if(i<polygon_points.size()-1) mbrSb.append(" , ");
        }
        
        if (doTransform) {
        	mbrSb.append(" , ");
            mbrSb.append(destSrid);
            mbrSb.append("))', ");
            mbrSb.append(table.getSrid());
        }
        
        else
        	mbrSb.append("))'");
        
        mbrSb.append(")");

        String mbr = mbrSb.toString(); 

        StringBuilder qSb = new StringBuilder();
        qSb.append("SELECT *,ST_AsBinary(CastToXY(");
        if (doTransform)
            qSb.append("ST_Transform(");
        qSb.append(table.getGeomName());
        if (doTransform) {
            qSb.append(", ");
            qSb.append(destSrid);
            qSb.append(")");
        }
        qSb.append("))AS ");
        qSb.append(DEFAULT_GEOMETRY_NAME);
        // qSb.append(", AsText(");
        // if (doTransform)
        // qSb.append("ST_Transform(");
        // qSb.append(table.geomName);
        // if (doTransform) {
        // qSb.append(", ");
        // qSb.append(destSrid);
        // qSb.append(")");
        // }
        // qSb.append(")");
        qSb.append(" FROM \"");
        qSb.append(table.getName());
        qSb.append("\" WHERE ST_Intersects(");
        qSb.append(table.getGeomName());
        qSb.append(" , ");
        qSb.append(mbr);
        qSb.append(") = 1 ");
        qSb.append(" AND ROWID IN (");
        qSb.append("     SELECT ROWID FROM Spatialindex WHERE f_table_name ='");
        qSb.append(table.getName());
        qSb.append("'");
        qSb.append("     AND search_frame = ");
        qSb.append(mbr);
        qSb.append(" )");
        if(limit != null){
            /*if(start !=null){
            	qSb.append(" ORDER BY  ");
            	qSb.append(ORDER_BY_DEFAULT_FIELD);
            	qSb.append(" ");
            }*/
            /*qSb.append(" LIMIT ");
            if(start != null){
            	qSb.append(start);
            	qSb.append(",");
            	qSb.append(start + limit);
            }else{
            	qSb.append(limit);
            }
        }
        qSb.append(";");
        String q = qSb.toString();
        Log.v("Query polygon",""+q);
        Stmt stmt = db.prepare(q);
		return stmt;		
	}*/
}