package it.geosolutions.android.map.mapstore.model;

import it.geosolutions.android.map.wms.WMSSource;

import java.io.Serializable;
import java.util.HashMap;

import com.google.gson.JsonObject;

public class MapStoreConfiguration implements Serializable {
	public MapStoreMap map;
	public HashMap<String,MapStoreSource> sources;
	public String data;
	public String defaultSourceType;
}
