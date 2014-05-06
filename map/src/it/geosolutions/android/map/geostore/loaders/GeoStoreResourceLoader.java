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
package it.geosolutions.android.map.geostore.loaders;


import it.geosolutions.android.map.geostore.model.Resource;
import it.geosolutions.android.map.geostore.utils.GeoStoreClient;

import java.util.List;

import android.support.v4.content.AsyncTaskLoader;

import com.actionbarsherlock.app.SherlockFragmentActivity;
/**
 * AsyncTaskLoader to load Resources from GeoStore.
 * Uses JSON format and gson Library to convert GeoStore stuff
 * into a local model replication of the GeoStore Model.
 * Can not use GeoStoreClient because of this issue (https://github.com/geosolutions-it/geostore/issues/46)
 * Provide a List of Resources <Resource> object on Load finish
 *  
 * @author Lorenzo Natali (www.geo-solutions.it)
 *
 */
public class GeoStoreResourceLoader extends AsyncTaskLoader<List<Resource>> {

	private String geostore_url;
	private List<Resource> mData;
	private String textFilter;
	public int page;
	public int limit;
	public int totalCount;

	public GeoStoreResourceLoader(SherlockFragmentActivity context,
			String url,String parameters,int page,int limit) {
		super(context);
		geostore_url = url;
		textFilter = parameters;
		this.page=page;
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
	public List<Resource> loadInBackground() {
		GeoStoreClient gsc= new GeoStoreClient();
		
		gsc.setUrl(geostore_url);
		if(textFilter!=null){
			mData = gsc.searchResources(textFilter,limit*page,limit);
			totalCount = gsc.totalCount;
		}else{
			mData = gsc.searchResources("*",limit*page,limit);
			totalCount = gsc.totalCount;
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
	public void onCanceled(List<Resource> data) {
		// TODO Auto-generated method stub
		super.onCanceled(data);
		releaseResources(data);
	}

	/**
	 * @param data
	 */
	private void releaseResources(List<Resource> data) {
		// release resource if needed

	}

	

}
