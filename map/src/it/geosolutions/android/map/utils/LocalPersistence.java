/*
 * GeoSolutions Android Map Library - Digital field mapping on Android based devices
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

/**
 *
 * Writes/reads an object to/from a private local file
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public class LocalPersistence {
	/**
	 * The name of the file that contains the maps
	 */
	public static final String MAPS="MAPS";
	/**
	 * The name of the file that contains the sources
	 */
	public static final String SOURCES="SOURCES";
	/**
	 * the file that contains current map
	 */
	public static final String CURRENT_MAP="CURRENT_MAP";
    /**
     * Write an object to a file
     * @param context
     * @param object
     * @param filename
     */
    public static void writeObjectToFile(Context context, Object object, String filename) {
    	
        ObjectOutputStream objectOut = null;
        try {

            FileOutputStream fileOut = context.openFileOutput(filename, Activity.MODE_PRIVATE);
            objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(object);
            fileOut.getFD().sync();

        } catch (IOException e) {
           Log.e("LocalPersistence",  e.getStackTrace().toString());
        } finally {
            if (objectOut != null) {
                try {
                    objectOut.close();
                } catch (IOException e) {
                    Log.e("LocalPersistence",  e.getStackTrace().toString());

                }
            }
        }
    }


    /**
     * 
     * @param context
     * @param filename
     * @return
     */
    public static Object readObjectFromFile(Context context, String filename) {

        ObjectInputStream objectIn = null;
        Object object = null;
        try {

            FileInputStream fileIn = context.getApplicationContext().openFileInput(filename);
            objectIn = new ObjectInputStream(fileIn);
            object = objectIn.readObject();

        } catch (FileNotFoundException e) {
            // Do nothing
        } catch (IOException e) {
            Log.e("LocalPersistence",  e.getStackTrace().toString());

        } catch (ClassNotFoundException e) {
            Log.e("LocalPersistence",  e.getStackTrace().toString().toString());

        } finally {
            if (objectIn != null) {
                try {
                    objectIn.close();
                } catch (IOException e) {
                	 Log.e("LocalPersistence",  e.getStackTrace().toString());
                }
            }
        }

        return object;
    }

}