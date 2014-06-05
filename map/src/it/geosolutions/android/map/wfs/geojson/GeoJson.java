package it.geosolutions.android.map.wfs.geojson;

import it.geosolutions.android.map.wfs.geojson.feature.FeatureCollection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vividsolutions.jts.geom.Geometry;

public class GeoJson {
	private final Gson gson;

	public GeoJson() {
		gson = new GsonBuilder()
				// .serializeNulls()
				.disableHtmlEscaping()
				.registerTypeHierarchyAdapter(Geometry.class,
						new GeometryJsonSerializer())
				.registerTypeHierarchyAdapter(Geometry.class,
						new GeometryJsonDeserializer()).create();
	}

	public String toJson(Object src) {
		return gson.toJson(src);
	}

	public void toJson(Object src, Appendable writer) {
		gson.toJson(src, writer);
	}

	public FeatureCollection fromJson(String responseText,
			Class<FeatureCollection> class1) {
		// TODO Auto-generated method stub

		return gson.fromJson(responseText, FeatureCollection.class);

	}
}