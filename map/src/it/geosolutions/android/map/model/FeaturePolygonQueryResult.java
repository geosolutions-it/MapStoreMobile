/**
 * 
 */
package it.geosolutions.android.map.model;

import java.util.ArrayList;

/**
 * @author Jacopo Pianigiani (jacopo.pianigiani85@gmail.com)
 *
 */
public class FeaturePolygonQueryResult {
	private String layerName;
	ArrayList<Feature> features;
	public String getLayerName() {
		return layerName;
	}
	public void setLayerName(String layerName) {
		this.layerName = layerName;
	}
	public ArrayList<Feature> getFeatures() {
		return features;
	}
	public void setFeatures(ArrayList<Feature> features) {
		this.features = features;
	}
}
