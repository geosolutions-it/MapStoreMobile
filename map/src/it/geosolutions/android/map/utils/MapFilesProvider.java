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

import java.io.File;
import java.io.FilenameFilter;

import android.os.Environment;
import android.util.Log;

/**
 * @author Lorenzo Natali (www.geo-solutions.it)
 *
 */
public class MapFilesProvider {
	private static String baseDir ="/mapstore";
	private static String baseStyle = baseDir + "/styles/";
	private static String backgroundFileName =  "bg.map";
	public static void setBaseDir(String baseDir){
		MapFilesProvider.baseDir = baseDir;
	}
	public static File getBaseDirectoryFile(){
		File f = new File(Environment
				.getExternalStorageDirectory().getPath(),baseDir);
		if(f.exists()){
			if(validFilesFound(f)){
				Log.v("FILES","base directory found at:"+f.getAbsolutePath());
				return f;
			}else{
				Log.v("FILES","no sqlite found at:"+f.getAbsolutePath());
			}
		}
		
		Log.w("FILES","base directory not found at: "+Environment
				.getExternalStorageDirectory().getPath() );
		return null;
		
	}
	/**
	 * @return
	 */
	private static boolean validFilesFound(File mapsDir) {
		File[] sqliteFiles = mapsDir.listFiles(new FilenameFilter(){
            public boolean accept( File dir, String filename ) {
                return filename.endsWith(".sqlite") || filename.endsWith(".mbtiles");
            }
        });
		if (sqliteFiles.length>0){
			return true;
		}
		return false;
	}
	
	/**
	 * Provides background map file configured in this object
	 * @return the .map file configured
	 */
	public static File getBackgroundMapFile(){
		File f =new File(Environment
				.getExternalStorageDirectory().getPath(),getBackgroundFilePath() );
		if(f.exists()){
			return f;
		}
		
		Log.w("FILES","background file not found");
		return null;
	}
	/**
	 * Returns the path to the background .map file
	 * @return
	 */
	private static String getBackgroundFilePath() {
		return baseDir + "/" + backgroundFileName;
	}
	/**
	 * @return
	 */
	public static String getStyleDirIn() {
		String in = Environment.getExternalStorageDirectory().getPath() + baseStyle;
		return in;
	}
	/**
	 * @return
	 */
	public static String getStyleDirOut() {
		String in = Environment.getExternalStorageDirectory().getPath() + baseStyle;
		return in;
	}
}
