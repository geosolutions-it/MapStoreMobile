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

	public GeoStoreResourceLoader(SherlockFragmentActivity context,
			String url,String parameters) {
		super(context);
		geostore_url = url;
		 
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
		//String testString = "{\"ResourceList\":{\"Resource\":[{\"canDelete\":false,\"canEdit\":false,\"creation\":\"2013-08-08T11:57:40.319+02:00\",\"description\":\"Agit GWC\",\"id\":341,\"lastUpdate\":\"2013-10-23T09:56:50.105+02:00\",\"name\":\"Agit GWC\"},{\"canDelete\":false,\"canEdit\":false,\"creation\":\"2013-10-15T12:05:56.637+02:00\",\"description\":\"DB_Servizi\",\"id\":441,\"lastUpdate\":\"2013-10-23T09:56:51.790+02:00\",\"name\":\"DB_Servizi\"},{\"canDelete\":false,\"canEdit\":false,\"creation\":\"2013-09-26T15:20:58.713+02:00\",\"description\":\"Elettrosmog\",\"id\":401,\"lastUpdate\":\"2013-10-23T09:56:50.448+02:00\",\"name\":\"Elettrosmog\"},{\"canDelete\":false,\"canEdit\":false,\"creation\":\"2013-07-18T09:12:56.718+02:00\",\"description\":\"Mappatura Acustica\",\"id\":301,\"lastUpdate\":\"2013-11-15T09:03:40.882+01:00\",\"name\":\"Mappatura Acustica\"},{\"canDelete\":false,\"canEdit\":false,\"creation\":\"2013-08-22T11:56:10.021+02:00\",\"description\":\"Mobilità\",\"id\":361,\"lastUpdate\":\"2013-11-15T08:27:51.114+01:00\",\"name\":\"Mobilità\"},{\"canDelete\":false,\"canEdit\":false,\"creation\":\"2013-10-22T14:42:07.262+02:00\",\"description\":\"PUC\",\"id\":481,\"lastUpdate\":\"2013-12-18T09:01:03.973+01:00\",\"name\":\"PUC\"},{\"canDelete\":false,\"canEdit\":false,\"creation\":\"2013-08-07T13:38:57.515+02:00\",\"description\":\"Mappa dello Stradario - Integrazione diretta con GeoWebCache\",\"id\":321,\"lastUpdate\":\"2013-11-14T10:05:48.109+01:00\",\"name\":\"Stradario GWC\"},{\"canDelete\":false,\"canEdit\":false,\"creation\":\"2013-09-27T14:47:19.492+02:00\",\"description\":\"New stradario test Comune BZ\",\"id\":424,\"lastUpdate\":\"2013-10-23T09:56:48.841+02:00\",\"name\":\"StradarioTestLatest\"},{\"canDelete\":false,\"canEdit\":false,\"creation\":\"2013-07-08T17:34:03.597+02:00\",\"description\":\"WiFi\",\"id\":287,\"lastUpdate\":\"2013-11-15T09:06:13.170+01:00\",\"name\":\"WiFi New\"}]}}";
		GeoStoreClient gsc= new GeoStoreClient();
		gsc.setUrl(geostore_url);
		mData = gsc.getResources();
		
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
