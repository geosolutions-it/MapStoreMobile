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
package it.geosolutions.android.map.utils;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A class containing the coordinates of a point about a polygonal selection, already converted and projected
 * ready to pass to a query.
 * @author Jacopo Pianigiani (jacopo.pianigiani85@gmail.com).
 */
public class Coordinates_Query implements Parcelable{
	
	private double x, y;
	
	/**
	 * Default constructor of the class
	 * @param x
	 * @param y
	 */
	public Coordinates_Query(){
		super();
		this.x = 0;
		this.y = 0;
	}
	
	public Coordinates_Query(Parcel in){
		super();
		this.x = in.readDouble();
		this.y = in.readDouble();
	}
	
	/**
	 * Constructor of the class for float parameter
	 * @param x
	 * @param y
	 */
	public Coordinates_Query(double x, double y){
		super(); //
		this.x = x;
		this.y = y;
	}	
	/**
	 * Return x value for coordinate of point
	 * @return 
	 */
	public double getX(){
		return x;
	}	
	/**
	 * Return y value for coordinate of point
	 * @return 
	 */
	public double getY(){
		return y;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeDouble(x);
		dest.writeDouble(y);
	}

	public static Parcelable.Creator<Coordinates_Query> CREATOR = new Parcelable.Creator<Coordinates_Query>() 
	{	 
          public Coordinates_Query createFromParcel(Parcel s){
              return new Coordinates_Query(s);
          }

          public Coordinates_Query[] newArray(int size) {
                return new Coordinates_Query[size];
          }
		};
}