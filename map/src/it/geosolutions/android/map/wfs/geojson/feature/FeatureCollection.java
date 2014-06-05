package it.geosolutions.android.map.wfs.geojson.feature;

import java.util.ArrayList;


/**
 * Class that wraps the FeatureCollection for geojson
 * @author Admin
 *
 */
public class FeatureCollection {
	public String type ="FeatureCollection";
	public Integer totalFeatures;
	public ArrayList<Feature> features;
}
