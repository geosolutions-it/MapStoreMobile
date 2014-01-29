/*
 * GeoSolutions map - Digital field mapping on Android based devices
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
package it.geosolutions.android.map.model.query;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Common model for query used by query rectangular, circular, one point and polygonal.
 * @author Jacopo Pianigiani (jacopo.pianigiani85@gmail.com)
 */
public class FeatureInfoQuery implements Parcelable{
	private byte zoomLevel;
	private String srid;
	
	/**
	 * Get zoom level on map.
	 * @return
	 */
	public byte getZoomLevel() {
		return zoomLevel;
	}
	
	/**
	 * Set level of zoom on map.
	 * @param zoomLevel
	 */
	public void setZoomLevel(byte zoomLevel) {
		this.zoomLevel = zoomLevel;
	}
	
	/**
	 * Return the coordinate reference system.
	 * @return
	 */
	public String getSrid() {
		return srid;
	}
	
	/**
	 * Set the coordinate reference system.
	 * @param srid
	 */
	public void setSrid(String srid) {
		this.srid = srid;
	}
	/* (non-Javadoc)
	 * @see android.os.Parcelable#describeContents()
	 */
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	/* (non-Javadoc)
	 * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
	 */
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeByte(zoomLevel);
		dest.writeString(srid);
	}
	
	/**
	 * Constructor for class FeatureInfoQuery.
	 * @param source
	 */
	public FeatureInfoQuery(Parcel source){
		zoomLevel=source.readByte();
		srid=source.readString();
	}
	
	/**
	 * Default constructor for class.
	 */
	public FeatureInfoQuery(){}
	
	 public static final Parcelable.Creator<FeatureInfoQuery> CREATOR
     = new Parcelable.Creator<FeatureInfoQuery>() {
	 public FeatureInfoQuery createFromParcel(Parcel in) {
	     return new FeatureInfoQuery(in);
	 }

	 public FeatureInfoQuery[] newArray(int size) {
	     return new FeatureInfoQuery[size];
	 }
	};
}