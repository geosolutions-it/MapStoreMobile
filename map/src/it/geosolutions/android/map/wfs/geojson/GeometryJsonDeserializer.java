package it.geosolutions.android.map.wfs.geojson;

import static it.geosolutions.android.map.wfs.geojson.GeoJsonConstants.COORDINATES;
import static it.geosolutions.android.map.wfs.geojson.GeoJsonConstants.TYPE;
import static it.geosolutions.android.map.wfs.geojson.GeoJsonConstants.TYPE_GEOMETRYCOLLECTION;
import static it.geosolutions.android.map.wfs.geojson.GeoJsonConstants.TYPE_LINESTRING;
import static it.geosolutions.android.map.wfs.geojson.GeoJsonConstants.TYPE_MULTILINESTRING;
import static it.geosolutions.android.map.wfs.geojson.GeoJsonConstants.TYPE_MULTIPOINT;
import static it.geosolutions.android.map.wfs.geojson.GeoJsonConstants.TYPE_MULTIPOLYGON;
import static it.geosolutions.android.map.wfs.geojson.GeoJsonConstants.TYPE_POINT;
import static it.geosolutions.android.map.wfs.geojson.GeoJsonConstants.TYPE_POLYGON;

import java.lang.reflect.Type;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
/**
 * <JsonDeserializer> for GeoJson Geometry part
 * @author Lorenzo Natali (lorenzo.natali at geo-solutions.it)
 * Supports :
 * * POINT
 * * MULTIPOINT
 * * LINESTRING
 * * POLYGON
 * Does **not** support yet:
 * * MultiLineString
 *
 */
public class GeometryJsonDeserializer implements JsonDeserializer<Geometry> {
	
	
	@Override
	public Geometry deserialize(JsonElement json, Type type,
			JsonDeserializationContext context) throws JsonParseException {
		JsonObject obj = json.getAsJsonObject();
		String geometryType = obj.get(TYPE) != null  ?  obj.get(TYPE).getAsString() :null ;
		if(geometryType == null){
			return null;
		}
		GeometryFactory fact = new GeometryFactory();
		//case Point
		if (geometryType.equals(TYPE_POINT)) {
			 JsonElement jc = obj.get(COORDINATES);
			 if(jc != null){
			     return fact.createPoint(getCoordinates(jc.getAsJsonArray()));
			 }
        } else if (geometryType.equals(TYPE_MULTIPOINT)) {
        	JsonElement jc = obj.get(COORDINATES);
        	if(jc != null){
			     return fact.createMultiPoint(getCoordinatesArray(jc.getAsJsonArray()));
			 }
        } else if (geometryType.equals(TYPE_LINESTRING)) {
        	JsonElement jc = obj.get(COORDINATES);
        	if(jc != null){
			     return fact.createLineString(getCoordinatesArray(jc.getAsJsonArray()));
			 }
        } else if (geometryType.equals(TYPE_MULTILINESTRING)) {
        	JsonElement jc = obj.get(COORDINATES);
        	if(jc != null){
        		JsonArray ja = jc.getAsJsonArray();
        		if(ja != null){
        			int ja_size = ja.size();
        			LineString[] lineStringArray = new LineString[ja_size];
        			for(int i = 0; i < ja_size ; i++){
        				lineStringArray[i] = fact.createLineString(getCoordinatesArray(ja.get(i).getAsJsonArray()));
        			}
        			return fact.createMultiLineString(lineStringArray);
        		}
    		}
        } else if (geometryType.equals(TYPE_POLYGON)) {
        	JsonElement jc = obj.get(COORDINATES);
        	if(jc != null){
        		//check if this polygon contains inner polygons
        		if(jc.getAsJsonArray().size() > 1){
        			final int holesCount = jc.getAsJsonArray().size() - 1;
        			LinearRing[] holes = new LinearRing[holesCount];
        			for(int i = 0; i < holesCount;i++){
        				holes[i] = getLinearRingFromCoordinates(jc.getAsJsonArray().get(i + 1).getAsJsonArray(), fact);
        			}
        			return fact.createPolygon(getLinearRingFromCoordinates(jc.getAsJsonArray().get(0).getAsJsonArray(), fact), holes);
        			
        		}else if(jc.getAsJsonArray().size() == 1){//there is one outer polygon
        			
        			return fact.createPolygon(getCoordinatesArray(jc.getAsJsonArray().get(0).getAsJsonArray()));
        		}
			 }
        } else if (geometryType.equals(TYPE_MULTIPOLYGON)) {
        	// NOT SUPPORTED YET
        } else if (geometryType.equals(TYPE_GEOMETRYCOLLECTION)) {
        	// NOT SUPPORTED YET
        }
		return null;
	}
	/**
     * convert a <JsonArray> into a <LinearRing> 
     * @param coordinatesArray (array of arrays)
     * @param fact the GeometryFactory
     * @return
     */
	 private LinearRing getLinearRingFromCoordinates(JsonArray coordinatesArray, GeometryFactory fact){
		 
		 return new LinearRing(fact.getCoordinateSequenceFactory().create(getCoordinatesArray(coordinatesArray)), fact);
	 }
    /**
     * convert a <JsonArray> into a <Coordinate> array (case <LineString> ...)
     * @param coordinatesArray (array of arrays)
     * @return
     */
	 private Coordinate[] getCoordinatesArray(JsonArray coordinatesArray) {
		 final Coordinate[] result = new Coordinate[coordinatesArray.size()];
		 //iteration on coordinates elements
		 for (int i = 0; i < result.length; i++) {
			 final JsonArray jca = coordinatesArray.get(i).getAsJsonArray();
			 //create the single coordinates
			 result[i] = getCoordinates(jca);

		 }
		 return result;
	 }

    /**
     * convert a <JsonArray> into a <Coordinate> object (case <Point>)
     * @param coordinatesArray (array of latitude - longitude)
     * @return
     */
	private Coordinate getCoordinates(JsonArray jca) {
		if (jca!=null){
    	  if(jca.size() == 2 ){
    		  Coordinate cc = new Coordinate(jca.get(0).getAsDouble(),jca.get(1).getAsDouble());
    		  return cc;
    	  }else if(jca.size() == 3){
    		  Coordinate cc = new Coordinate(jca.get(0).getAsDouble(),jca.get(1).getAsDouble(),jca.get(2).getAsDouble());
    		  return cc;
    	  }
      }
		return null;
	   
	}
}

