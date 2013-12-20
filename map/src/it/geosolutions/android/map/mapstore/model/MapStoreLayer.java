package it.geosolutions.android.map.mapstore.model;

import java.io.Serializable;

public class MapStoreLayer implements Serializable{
	public String source;
	public String title;
	public String group;
	public String name;
	public String format;
	public String styles;
	public String transparent;
	public Boolean tiled ;
	public Double ratio;
	public Boolean visibility;
	public Integer buffer;
}
