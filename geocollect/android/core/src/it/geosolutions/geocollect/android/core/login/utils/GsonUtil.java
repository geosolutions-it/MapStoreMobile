package it.geosolutions.geocollect.android.core.login.utils;

import it.geosolutions.android.map.wfs.geojson.GeometryJsonDeserializer;
import it.geosolutions.android.map.wfs.geojson.GeometryJsonSerializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vividsolutions.jts.geom.Geometry;
/**
 * Utility classes for serialization
 * @author Lorenzo Natali,GeoSolutions
 *
 */
public class GsonUtil {
	public static Gson createFeatureGson(){
		return  new GsonBuilder()
		// .serializeNulls()
		.disableHtmlEscaping()
		.registerTypeHierarchyAdapter(Geometry.class,
				new GeometryJsonSerializer())
		.registerTypeHierarchyAdapter(Geometry.class,
				new GeometryJsonDeserializer()).create();
	} 
}
