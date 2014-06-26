package it.geosolutions.android.map.wfs.geojson.feature;


import java.io.Serializable;
import java.util.HashMap;

import com.vividsolutions.jts.geom.Geometry;

public class Feature implements Serializable{
	public String type = "Feature";
	public String id;
	public Geometry geometry;
	public String geometry_name ="the_geom";
	public HashMap<String, Object> properties;
}
