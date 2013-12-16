/*
 * GeoSolutions Android Map Library - Digital field mapping on Android based devices
 * Copyright (C) 2013  GeoSolutions (www.geo-solutions.it)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.geosolutions.android.map.dto;

import java.io.Serializable;

import it.geosolutions.android.map.R;
import it.geosolutions.android.map.model.Attribute;
import it.geosolutions.android.map.model.Feature;
import it.geosolutions.android.map.overlay.items.DescribedMarker;

import org.mapsforge.android.maps.overlay.Marker;
import org.mapsforge.core.model.GeoPoint;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
/**
 * Data Transfer Object for a marker.
 * Can be transferred among 
 * @author Lorenzo Natali (www.geo-solutions.it)
 *
 */
public class MarkerDTO implements Serializable,Parcelable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private GeoPoint point;
	private int type;
	private String id="";
	private String featureId="";
	private Feature feature=null;
	public String getFeatureId() {
        return featureId;
    }
    public void setFeatureId(String featureId) {
        this.featureId = featureId;
    }
    public Feature getFeature() {
        return feature;
    }
    public void setFeature(Feature feature) {
        this.feature = feature;
    }
    private String description="";
	private String source="";
	
        public static final int MARKER_RED =R.drawable.marker_red;
	public static final int MARKER_GREEN = R.drawable.marker_green;
	public static final int MARKER_YELLOW = R.drawable.marker_yellow;
	public static final int MARKER_BLUE = R.drawable.marker_blue;
	public MarkerDTO(){
		
	}
	/**
	 * Creates a DTO from longitude latitude and type
	 * @param latitude latitude using coordinates in EPSG:4326
	 * @param longitude longitude using coordinates in EPSG:4326
	 * @param type can be MarkerDTO.MARKER_RED, MARKERDTO.MARKER_GREEN etc...
	 */
	public MarkerDTO(double latitude,double longitude,int type){
		setPoint(new GeoPoint(latitude,longitude));
		this.setType(type);
		
	}
	
	public MarkerDTO(DescribedMarker d){
    	        this();
	        GeoPoint gp =d.getGeoPoint();
	        this.setPoint(gp);
	        this.setType(d.getType());
	        
		this.setId(d.getId());
		this.setDescription(d.getDescription());
		this.setSource(d.getSource());
		this.setFeatureId(d.getFeatureId());
		this.setFeature(d.getFeature());
	}
	/**
	 * Creates a marker getting the drawable object from the passed context
	 * @param c
	 * @return
	 */
	public DescribedMarker createMarker(Context c){
	    Drawable drawable=null;
        try{
            drawable = c.getResources().getDrawable(type);
        }catch(Exception e){
            Log.w("MARKER","unable to find type of marker");
            drawable = c.getResources().getDrawable(MARKER_RED);
            type=MARKER_RED;
        }
		DescribedMarker dm =new DescribedMarker(point, Marker.boundCenterBottom(drawable));
		dm.setType(type);
		dm.setId(id);
		dm.setDescription(description);
		dm.setSource(source);
		dm.setFeatureId(featureId);
		dm.setFeature(feature);
		
		return dm;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	 public static final Parcelable.Creator<MarkerDTO> CREATOR
     = new Parcelable.Creator<MarkerDTO>() {
	 public MarkerDTO createFromParcel(Parcel in) {
	     return new MarkerDTO(in);
	 }

	 public MarkerDTO[] newArray(int size) {
	     return new MarkerDTO[size];
	 }
	};
	@Override
	public void writeToParcel(Parcel dest, int flags) {
	        if(point!=null){
	            dest.writeDouble(point.latitude);
	            dest.writeDouble(point.longitude);
	        }else{
	            dest.writeDouble(Double.NaN);
	            dest.writeDouble(Double.NaN);
	        }
		dest.writeInt(type);
		dest.writeString(id);
		dest.writeString(description);
		dest.writeString(source);
		dest.writeString(featureId);
		dest.writeTypedList(feature);
	}
	public MarkerDTO(Parcel in) {
		Double lat = in.readDouble();
		Double lon = in.readDouble();
		if(lat.isNaN()&& lon.isNaN()){
		    point =null;
		}else{
		    point = new GeoPoint(lat, lon);
		}
		type = in.readInt();
		id = in.readString();
		description = in.readString();
		source = in.readString();
		featureId=in.readString();
		feature = new Feature();
		in.readTypedList(feature,Attribute.CREATOR);
	}

	// getters and Setters
	/**
	 * 
	 * @return the id of the related feature
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * The id of the marker
	 * @param id
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * 
	 * @return a string that describes the marker
	 */
	public String getDescription() {
		return description;
	}
	
	/**
	 * A description about the marker
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * 
	 * @return the coordinate of the marker
	 */
	public GeoPoint getPoint() {
		return point;
	}
	
	/**
	 * 
	 * @param the coordinates of the marker
	 */
	public void setPoint(GeoPoint point) {
		this.point = point;
	}
	
	/**
	 * The type of the marker
	 * @return
	 */
	public int getType() {
		return type;
	}
	
	/**
	 * The type of the marker, one of constant MARKER_RED ...
	 * @param type
	 */
	public void setType(int type) {
		this.type = type;
	}
	
	
	/**
	 * 
	 * @return the table name of the related feature
	 */
	public String getSource() {
	        return source;
	}
	
	/**
	 * the source is the name of the table.
	 * If present, the initial query for objects wich GeoPoint is missing
	 * will be much more fast
	 * @param source
	 */
	public void setSource(String source) {
	        this.source = source;
	}
}
