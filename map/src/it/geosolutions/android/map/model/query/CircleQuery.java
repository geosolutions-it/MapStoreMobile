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
 * Class to represent a query model to perform a search by a circle designed on map.
 * @author Jacopo Pianigiani (jacopo.pianigiani85@gmail.com).
 */
public class CircleQuery extends BaseFeatureInfoQuery{
	
	private double x, y, radius;
	
	/**
	 * Method that return x coordinate of center
	 * @return double
	 */
	public double getX() {
		return x;
	}
	/**
	 * Method that set x coordinate of center
	 * @param double
	 */
	public void setX(double x) {
		this.x = x;
	}
	/**
	 * Method that return y coordinate of center
	 * @return double
	 */
	public double getY() {
		return y;
	}
	/**
	 * Method that set y coordinate of center
	 * @param double
	 */
	public void setY(double y) {
		this.y = y;
	}
	/**
	 * Method that return radius of circle
	 * @return double
	 */
	public double getRadius() {
		return radius;
	}
	/**
	 * Method that set radius of circle
	 * @param double
	 */
	public void setRadius(double radius) {
		this.radius = radius;
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
		super.writeToParcel(dest, flags);
		dest.writeDouble(x);
		dest.writeDouble(y);
		dest.writeDouble(radius);
	}
	
	/**
	 * Constructor for class FeatureCircleQuery.
	 * @param source
	 */
	public CircleQuery(Parcel source){
		super(source);
		x=source.readDouble();
		y=source.readDouble();
		radius=source.readDouble();
	}
	
	/**
	 * Default constructor for class.
	 */
	public CircleQuery(){}
	
	 /**
	 * @param q
	 */
	public CircleQuery(CircleQuery q) {
		super(q);
		setX(q.getX());
		setY(q.getY());
		setRadius(q.getRadius());
		setSrid(q.getSrid());
		setZoomLevel(q.getZoomLevel());
	}

	public static final Parcelable.Creator<CircleQuery> CREATOR
     = new Parcelable.Creator<CircleQuery>() {
	 public CircleQuery createFromParcel(Parcel in) {
	     return new CircleQuery(in);
	 }

	 public CircleQuery[] newArray(int size) {
	     return new CircleQuery[size];
	 }	
	};	
}