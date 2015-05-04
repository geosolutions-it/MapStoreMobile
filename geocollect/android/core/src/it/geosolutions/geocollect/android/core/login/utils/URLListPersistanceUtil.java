package it.geosolutions.geocollect.android.core.login.utils;

import it.geosolutions.geocollect.android.core.BuildConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
/**
 * Helper class to save and load a list of Strings which is not possible using the
 * PreferenceManager prior API level 11
 * 
 * @author Robert Oehler
 *
 */
public class URLListPersistanceUtil {

	final static String URLS_FILE = "geocollect_server_urls";

	/**
	 * Saves the urls to file.
	 */
	public static void save(final Context pContext,final ArrayList<String> pUrls) {

		FileOutputStream fo = null;
		try {
			fo = pContext.openFileOutput(URLS_FILE, Context.MODE_PRIVATE);
			ObjectOutputStream out = new ObjectOutputStream(fo);
			out.writeObject(pUrls);
			out.flush();
			out.close();
			fo.close();

		} catch (IOException e) {
            if(BuildConfig.DEBUG){
                Log.e(URLListPersistanceUtil.class.getSimpleName(), "save failed",e);
            }
		}
	}

	/**
	 * Loads urls.
	 */
	@SuppressWarnings("unchecked")
	public static ArrayList<String> load(final Context pContext) {
		FileInputStream fi = null;
		ObjectInputStream in = null;
		ArrayList<String> urls = null;
		try {
            
            File file = pContext.getFileStreamPath(URLS_FILE);
            if(file == null || !file.exists()) {
                // File does not exists yet
                return null;
            }
		    
			fi = pContext.openFileInput(URLS_FILE);
			in = new ObjectInputStream(fi);		
			
			urls = (ArrayList<String>) in.readObject();

            if(BuildConfig.DEBUG){
                Log.e(URLListPersistanceUtil.class.getSimpleName(), "urls loaded");
            }
			in.close();
			fi.close();

		}catch (Exception e) {
            if(BuildConfig.DEBUG){
                Log.e(URLListPersistanceUtil.class.getSimpleName(), "load failed",e);
            }
			return null;
		}
		return urls;
	}

}
