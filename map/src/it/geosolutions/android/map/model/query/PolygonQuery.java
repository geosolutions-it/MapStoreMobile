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

import java.util.ArrayList;
import android.os.Parcel;
import android.os.Parcelable;
import it.geosolutions.android.map.utils.Coordinates.Coordinates_Query;

/**
 * Class to represent a query model to perform a search by polygon designed on map.
 * @author Jacopo Pianigiani (jacopo.pianigiani85@gmail.com).
 */
public class PolygonQuery extends BaseFeatureInfoQuery{
	private ArrayList<Coordinates_Query> polygon_points;  //Store coordinates of polygon points
	
	/**
	 * Default constructor for class
	 */
	public PolygonQuery(){
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
	 * Method that set an array storing points of polygonal selection.
	 * @param polygon_points points captured by tap events on map.
	 */
	public void setPolygonPoints(ArrayList<Coordinates_Query> polygon_points) {
		this.polygon_points = polygon_points;
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
		dest.writeTypedList(polygon_points);
	}
	
	/**
	 * Constructor for class FeaturePolygonQuery
	 * @param source
	 */
	public PolygonQuery(Parcel source){
		super(source);
		polygon_points = new ArrayList<Coordinates_Query>();
		source.readTypedList(polygon_points,Coordinates_Query.CREATOR);
	}
		
	 /**
	 * @param q
	 */
	public PolygonQuery(PolygonQuery q) {
		super(q);
		setPolygonPoints(q.getPolygonPoints());
		setSrid(q.getSrid());
		setZoomLevel(q.getZoomLevel());
	}

	public static final Parcelable.Creator<PolygonQuery> CREATOR
     = new Parcelable.Creator<PolygonQuery>() {
	 public PolygonQuery createFromParcel(Parcel in) {
	     return new PolygonQuery(in);
	 }

	 public PolygonQuery[] newArray(int size) {
	     return new PolygonQuery[size];
	 }	
	};
}