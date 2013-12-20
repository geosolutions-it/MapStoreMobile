package it.geosolutions.android.map.mapstore.model;

import java.io.Serializable;
import java.util.HashMap;

public class MapStoreSource implements Serializable {
	public HashMap<String,Object >layerBaseParams;
	public String title;
	public String url;
	public String version;
	public Double[] layersCachedExtent;
	public String ptype;
}
