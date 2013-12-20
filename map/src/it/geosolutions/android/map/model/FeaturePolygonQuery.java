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

import java.util.ArrayList;
import android.os.Parcel;
import android.os.Parcelable;
import it.geosolutions.android.map.utils.Coordinates_Query;

/**
 * Class to represent a query model to perform a search by polygon drawed on map.
 * @author Jacopo Pianigiani (jacopo.pianigiani85@gmail.com).
 */
public class FeaturePolygonQuery implements Parcelable{

	private ArrayList<Coordinates_Query> polygon_points;  //Store coordinates of polygon points
	private byte zoomLevel;
	private String srid;
	
	public FeaturePolygonQuery(){
		polygon_points = new ArrayList<Coordinates_Query>();
	}
	/**
	 * Method that return arraylist of points
	 * @return double
	 */
	public ArrayList<Coordinates_Query> getPolygonPoints() {
		return polygon_points;
	}
	/**
	 * Method that set an arraylist storing points of polygon
	 * @param double
	 */
	public void setPolygonPoints(ArrayList<Coordinates_Query> polygon_points) {
		this.polygon_points = polygon_points;
	}	
	/**
	 * Method that return zoom level
	 * @return byte
	 */
	public byte getZoomLevel() {
		return zoomLevel;
	}
	/**
	 * Method that set requested zoom level
	 * @param byte
	 */
	public void setZoomLevel(byte zoomLevel) {
		this.zoomLevel = zoomLevel;
	}
	/**
	 * Method that return current reference system
	 * @return String
	 */
	public String getSrid() {
		return srid;
	}
	/**
	 * Method that set the reference system
	 * @param String
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
		dest.writeTypedList(polygon_points);
		dest.writeByte(zoomLevel);
		dest.writeString(srid);	
	}
	
	public FeaturePolygonQuery(Parcel source){
		polygon_points = new ArrayList<Coordinates_Query>();
		source.readTypedList(polygon_points,Coordinates_Query.CREATOR);
		zoomLevel=source.readByte();
		srid=source.readString();
	}
		
	 public static final Parcelable.Creator<FeaturePolygonQuery> CREATOR
     = new Parcelable.Creator<FeaturePolygonQuery>() {
	 public FeaturePolygonQuery createFromParcel(Parcel in) {
	     return new FeaturePolygonQuery(in);
	 }

	 public FeaturePolygonQuery[] newArray(int size) {
	     return new FeaturePolygonQuery[size];
	 }	
	};
}