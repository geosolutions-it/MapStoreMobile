package it.geosolutions.android.map.mapstore.model;

import java.io.Serializable;
import java.util.ArrayList;

public class MapStoreMap implements Serializable{
	public ArrayList<MapStoreLayer> layers;
	public short zoom;
	public double[] center;
	public String projection;

	
}
