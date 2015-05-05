/*
 * GeoSolutions - MapstoreMobile - GeoSpatial Framework on Android based devices
 * Copyright (C) 2014  GeoSolutions (www.geo-solutions.it)
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

import it.geosolutions.android.map.model.stores.LayerStore;
import it.geosolutions.android.map.model.stores.MapStoreLayerStore;
import it.geosolutions.android.map.model.stores.SpatialiteStore;
import java.io.File;
import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

/**
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public class StorageUtils {

	/**
	 * @param mapsActivity
	 */
	public static void setupSources(Context context) {
		
		ArrayList<LayerStore> sources = (ArrayList<LayerStore>) LocalPersistence.readObjectFromFile(context, LocalPersistence.SOURCES);
		if(sources == null){
			reloadDefaultSources(context);
		}else{
			Log.v("Storage","sources available");
		}
	}

	private static void reloadDefaultSources(Context context) {
		ArrayList<LayerStore> sources;
		sources = new ArrayList<LayerStore>();
		MapStoreLayerStore mapStoreDemo = new MapStoreLayerStore();
		mapStoreDemo.setUrl("http://mapstore.geo-solutions.it/geostore/rest/");
		mapStoreDemo.setName("MapStore Demo");
		LayerStore defaultDb = new SpatialiteStore();
		defaultDb.setName("Local Database");
		sources.add(mapStoreDemo); sources.add(defaultDb);
		LocalPersistence.writeObjectToFile(context, sources, LocalPersistence.SOURCES);
		Log.v("Storage","saved sources to local file");
	}
	
	
    /**
     * @return the available memory for an external mount
     */
    @SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
    public static long getAvailableMemoryInBytesForPath(final String path) {

    	final StatFs stat = new StatFs(path);
    	
    	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){

    		return stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
    		
    	} else{

    		return (long) stat.getBlockSize() * (long) stat.getAvailableBlocks(); 
    	}
    }
    /**
     * 
     * @return the available memory on the default SD-Card
     */
    public static long getAvailableInternalMemoryInBytes() {
    	
    	return getAvailableMemoryInBytesForPath(Environment.getExternalStorageDirectory().getAbsolutePath());
    }
	
}
