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
package it.geosolutions.android.map.loaders;

import it.geosolutions.android.map.database.SpatialDataSourceHandler;
import it.geosolutions.android.map.model.Feature;
import it.geosolutions.android.map.model.FeatureInfoQueryResult;
import it.geosolutions.android.map.model.FeatureInfoTaskQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jsqlite.Exception;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import android.widget.ArrayAdapter;
import eu.geopaparazzi.spatialite.database.spatial.core.SpatialVectorTable;

/**
 * Async query task to query layers. Updates an adapter with the results from a
 * query A query to the task is a "List of Lists of Maps", implemented with a
 * bundle of bundles The main bundle contains is name->list of features feature
 * contains name->value bundles
 * 
 * @author Lorenzo Natali (www.geo-solutions.it)
 */
public class FeatureInfoLoader extends
        AsyncTaskLoader<List<FeatureInfoQueryResult>> {
int features_loaded = 0;

private List<FeatureInfoQueryResult> mData;

private FeatureInfoTaskQuery[] queryQueue;

// private FeatureInfoObserver mObserver;
private static int MAX_FEATURES = 10;

public FeatureInfoLoader(Context ctx, FeatureInfoTaskQuery[] queryQueue) {
    // Loaders may be used across multiple Activities (assuming they aren't
    // bound to the LoaderManager), so NEVER hold a reference to the context
    // directly. Doing so will cause you to leak an entire Activity's context.
    // The superclass constructor will store a reference to the Application
    // Context instead, and can be retrieved with a call to getContext().
    super(ctx);
    this.queryQueue = queryQueue;

}

protected void doInBackground(FeatureInfoTaskQuery[] queryQueue,
        List<FeatureInfoQueryResult> data) {
    Log.d("FEATURE_INFO_TASK", "Info Task Launched");
    //process all queries
    for (FeatureInfoTaskQuery query : queryQueue) {
        if (!processQuery(query, data)) {
            return;
        }
    }
    return;

}

/**
 * Process a single <FeatureInfoQuery>
 * @param query
 * @param data the result will be added to this array
 */
private boolean processQuery(FeatureInfoTaskQuery query,
        List<FeatureInfoQueryResult> data) {
    SpatialDataSourceHandler handler = query.getHandler();
    SpatialVectorTable table = query.getTable();
    String tableName = query.getTable().getName();
    // check visibility before adding the table
    // AdvancedStyle s = sm.getStyle(tableName);
    // if ( !StyleUtils.isVisible(s, zoomLevel) ) {
    // onProgressUpdate(0);
    // continue;
    // }
    double north = query.getN();
    double south = query.getS();
    double east = query.getE();
    double west = query.getW();
    Integer start = query.getStart();
    Integer limit = query.getLimit();
    if (Log.isLoggable("FEATURE_INFO_TASK", Log.DEBUG)) { // Log check to avoid
                                                          // string creation
        Log.d("FEATURE_INFO_TASK", "starting query for table " + tableName);
    }
    // this is empty to skip geometries that returns errors
    ArrayList<Feature> features = new ArrayList<Feature>();
    try {
        features = handler.intersectionToFeatureListBBOX("4326", table, north,
                south, east, west, start, limit);
    } catch (Exception e) {
        Log.e("FEATURE_INFO_TASK", "unable to retrive data for table'"
                + tableName + "\'.Error:" + e.getLocalizedMessage());
        // TODO now simply skip, do better work
    }
    // add features
    FeatureInfoQueryResult result = new FeatureInfoQueryResult();
    result.setLayerName(tableName);
    result.setFeatures(features);
    Log.v("FEATURE_INFO_TASK", features.size() + " items found for table "
            + tableName);
    features_loaded += features.size();
    // publishProgress(result);
    data.add(result);

    return true;

}

/*
 * (non-Javadoc)
 * @see android.support.v4.content.AsyncTaskLoader#loadInBackground()
 */
@Override
public List<FeatureInfoQueryResult> loadInBackground() {
    List<FeatureInfoQueryResult> data = new ArrayList<FeatureInfoQueryResult>();

    // TODO: Perform the query here and add the results to 'data'.
    doInBackground(queryQueue, data);

    return data;
}

// ********************************************************/
// ** Deliver the results to the registered listener **/
// ********************************************************/
@Override
public void deliverResult(List<FeatureInfoQueryResult> data) {
    if (isReset()) {
        // The Loader has been reset; ignore the result and invalidate the data.
        releaseResources(data);
        return;
    }

    // Hold a reference to the old data so it doesn't get garbage collected.
    // We must protect it until the new data has been delivered.
    List<FeatureInfoQueryResult> oldData = mData;
    mData = data;
    if (isStarted()) {
        // If the Loader is in a started state, deliver the results to the
        // client. The superclass method does this for us.
        super.deliverResult(data);
    }

    // Invalidate the old data as we don't need it any more.
    if (oldData != null && oldData != data) {
        releaseResources(oldData);
    }
}

// ********************************************************/
// ** Implement the Loader’s state-dependent behavior **/
// ********************************************************/
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

/*
 * (non-Javadoc)
 * @see android.support.v4.content.Loader#onStopLoading()
 */
@Override
protected void onStopLoading() {
    cancelLoad();
}

/*
 * (non-Javadoc)
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
 * @see android.support.v4.content.AsyncTaskLoader#onCanceled(java.lang.Object)
 */
@Override
public void onCanceled(List<FeatureInfoQueryResult> data) {
    // TODO Auto-generated method stub
    super.onCanceled(data);
    releaseResources(data);
}

/**
 * @param data
 */
private void releaseResources(List<FeatureInfoQueryResult> data) {
    // release resource if needed

}

}
