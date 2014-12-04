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
package it.geosolutions.android.map.wfs;


import it.geosolutions.android.map.wfs.geojson.feature.Feature;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
/**
 * AsyncTaskLoader to load Features from WFS Service in GeoJson format.
 * Uses JSON format and gson Library to convert WFS stuff
 * into a local model replication of the GeoStore Model.
 * Can not use GeoStoreClient because of this issue (https://github.com/geosolutions-it/geostore/issues/46)
 * Provide a List of Resources <Feature> object on Load finish
 *  
 * @author Lorenzo Natali (www.geo-solutions.it)
 *
 */
public class WFSGeoJsonFeatureLoader extends AsyncTaskLoader<List<Feature>> {

	private String wfs_url;
	private List<Feature> mData;
	private String typeName;
	public int start;
	public int limit;
	HashMap<String,String> parameters;
	public Integer totalCount;//This hack allow infinite scrolling without total count limits.

	// BasicAuth parameters
	private String userName;
	private String password;
	
	/**
	 * This constructor handles username and password for basicauth
	 * @param context
	 * @param url
	 * @param parameters
	 * @param typeName
	 * @param start
	 * @param limit
	 * @param username
	 * @param password
	 */
	public WFSGeoJsonFeatureLoader(Context context,
			String url,HashMap<String,String> parameters,String typeName,int start,int limit, String username, String password) {
		this(context, url, parameters, typeName, start, limit);
		this.userName = username;
		this.password = password;
	}
	
	/**
	 * Constructor, does not handle BasicAuth params for backward compatibility
	 * @param context
	 * @param url
	 * @param parameters
	 * @param typeName
	 * @param start
	 * @param limit
	 */
	public WFSGeoJsonFeatureLoader(Context context,
			String url,HashMap<String,String> parameters,String typeName,int start,int limit) {
		super(context);
		wfs_url = url;
		this.parameters =parameters;
		this.typeName = typeName;
		this.start=start;
		this.limit=limit;
	}
	
	@Override
	protected void onStartLoading() {
	    if (mData != null) {
	        // Deliver any previously loaded data immediately.
	        deliverResult(mData);
	    }

	    // Begin monitoring the underlying data source.
	    // if (mObserver == null) {
	    // mObserver = new SampleObserver();
	    // // TODO: register the observer
	    // }

	    if (takeContentChanged() || mData == null) {
	        // When the observer detects a change, it should call onContentChanged()
	        // on the Loader, which will cause the next call to takeContentChanged()
	        // to return true. If this is ever the case (or if the current data is
	        // null), we force a new load.
	        forceLoad();
	    }
	}
	
	@Override
	public List<Feature> loadInBackground() {
		WFSGeoJsonClient gsc= new WFSGeoJsonClient();
		
		// If set, use BasicAuth parameters
		if(this.userName != null && this.password != null){
			gsc.setUsername(this.userName);
			gsc.setPassword(this.password);
		}
		
		gsc.setUrl(wfs_url);
		if(typeName!=null){
			mData = gsc.getFeature(typeName, parameters, start, limit);
			totalCount = gsc.totalCount;
		}else{

		}
		return mData;
	}

	@Override
	protected void onStopLoading() {
		cancelLoad();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.content.Loader#onReset()
	 */
	@Override
	protected void onReset() {

		onStopLoading();
		if (mData != null) {
			releaseResources(mData);
			mData = null;
		}

		// if(mObserver !=null){
		// //TODO unregister the observer
		// moObserver=null;
		//
		// }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.content.AsyncTaskLoader#onCanceled(java.lang.Object)
	 */
	@Override
	public void onCanceled(List<Feature> data) {
		// TODO Auto-generated method stub
		super.onCanceled(data);
		releaseResources(data);
	}

	/**
	 * @param data
	 */
	private void releaseResources(List<Feature> data) {
		// release resource if needed

	}

	

}
