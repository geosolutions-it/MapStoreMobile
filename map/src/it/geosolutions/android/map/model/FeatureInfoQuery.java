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
package it.geosolutions.android.map.model;

import it.geosolutions.android.map.dto.MarkerDTO;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Lorenzo Natali (www.geo-solutions.it)
 *
 */
public class FeatureInfoQuery implements Parcelable{
	private double n;
	private double s;
	private double e;
	private double w;
	private byte zoomLevel;
	private String srid;
	public double getN() {
		return n;
	}
	public void setN(double n) {
		this.n = n;
	}
	public double getS() {
		return s;
	}
	public void setS(double s) {
		this.s = s;
	}
	public double getE() {
		return e;
	}
	public void setE(double e) {
		this.e = e;
	}
	public double getW() {
		return w;
	}
	public void setW(double w) {
		this.w = w;
	}
	public byte getZoomLevel() {
		return zoomLevel;
	}
	public void setZoomLevel(byte zoomLevel) {
		this.zoomLevel = zoomLevel;
	}
	public String getSrid() {
		return srid;
	}
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
		dest.writeDouble(n);
		dest.writeDouble(s);
		dest.writeDouble(e);
		dest.writeDouble(w);
		dest.writeByte(zoomLevel);
		dest.writeString(srid);
		
	}
	public FeatureInfoQuery(Parcel source){
		n=source.readDouble();
		s=source.readDouble();
		e=source.readDouble();
		w=source.readDouble();
		zoomLevel=source.readByte();
		srid=source.readString();
	}
	public FeatureInfoQuery(){
		
	}
	
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
