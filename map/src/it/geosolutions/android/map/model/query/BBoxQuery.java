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
 * Class to represent a query model to perform a search by a rectangle designed on map.
 * @author Lorenzo Natali (www.geo-solutions.it)
 */
public class BBoxQuery extends BaseFeatureInfoQuery{
	private double n;
	private double s;
	private double e;
	private double w;

	/**
	 * Method to get n coordinate of rectangle
	 * @return
	 */
	public double getN() {
		return n;
	}
	
	/**
	 * Method to set n coordinate of rectangle
	 * @param n
	 */
	public void setN(double n) {
		this.n = n;
	}
	/**
	 * Method to get s coordinate of rectangle
	 * @return
	 */
	public double getS() {
		return s;
	}
	
	/**
	 * Method to set s coordinate of rectangle
	 * @param s
	 */
	public void setS(double s) {
		this.s = s;
	}
	
	/**
	 * Method to get e coordinate of rectangle
	 * @return
	 */
	public double getE() {
		return e;
	}
	
	/**
	 * Method to set e coordinate of rectangle
	 * @param e
	 */
	public void setE(double e) {
		this.e = e;
	}
	
	/**
	 * Method to get w coordinate of rectangle
	 * @return
	 */
	public double getW() {
		return w;
	}
	
	/**
	 * Method to set w coordinate of rectangle
	 * @param w
	 */
	public void setW(double w) {
		this.w = w;
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
		dest.writeDouble(n);
		dest.writeDouble(s);
		dest.writeDouble(e);
		dest.writeDouble(w);
	}
	
	/**
	 * Constructor for class FeatureRectangularQuery.
	 * @param source
	 */
	public BBoxQuery(Parcel source){
		super(source);
		n=source.readDouble();
		s=source.readDouble();
		e=source.readDouble();
		w=source.readDouble();
	}
	
	/**
	 * Default constructor for class.
	 */
	public BBoxQuery(){}
		
	/**
	 * Constructor for class FeatureRectangularTaskQuery.
	 * @param q
	 */
	public BBoxQuery(BBoxQuery q){
		super(q);
		setE(q.getE());
		setS(q.getS());
		setW(q.getW());
		setN(q.getN());
	}

	public static final Parcelable.Creator<BBoxQuery> CREATOR
     = new Parcelable.Creator<BBoxQuery>() {
	 public BBoxQuery createFromParcel(Parcel in) {
	     return new BBoxQuery(in);
	 }

	 public BBoxQuery[] newArray(int size) {
	     return new BBoxQuery[size];
	 }	
	};	
}