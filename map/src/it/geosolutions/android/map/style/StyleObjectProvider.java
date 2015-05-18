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
package it.geosolutions.android.map.style;

import it.geosolutions.android.map.BuildConfig;
import it.geosolutions.android.map.utils.MapFilesProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

/**
 * Provides Style from a source (Json files in a directory)
 * 
 * @author Admin
 * 
 */
public class StyleObjectProvider {
	// TODO externalize it in config
	private static final String STYLE_DIR_IN = MapFilesProvider.getStyleDirIn();
	private static final String STYLE_DIR_OUT = MapFilesProvider.getStyleDirOut();

	public AdvancedStyle getStyle(String name) {
		
		String fileName = getFileName(name);
		try{
			AdvancedStyle s=readJsonFile(fileName);
			if(s!=null){
				return s;
			}else{
				s = new AdvancedStyle();
				s.name = name;
				return s;
			}
			
		}catch(IOException e){
			AdvancedStyle s = new AdvancedStyle();
			s.name = name;
			return s;
		}
		
		
	}

	/** 
	 * Reads the Json File with Style from the style directory
	 * @param fileName the name of the file 
	 * @return the style
	 * @throws IOException
	 */
	private AdvancedStyle readJsonFile(String fileName) throws IOException {
		Gson gson = new Gson();
		String path = STYLE_DIR_IN + fileName;
		FileReader fr = null;
		try {
			File f = new File(path);
			if(f.exists()){
				fr= new FileReader(f);
				
				AdvancedStyle s = gson.fromJson(fr,AdvancedStyle.class);
				return s;
			}else {
				return null;
			}
		}catch(FileNotFoundException e){
			Log.v("STYLE","file not found:"+path);
			throw e;
		}catch (JsonParseException jpe) {
			if(BuildConfig.DEBUG){
				Log.e("STYLE","Parsing failed:"+path);
			}
		}
		finally{
			if(fr!=null){
				fr.close();
			}
		}
		return null;
		
		
	}

	public void save(String name, AdvancedStyle style) throws IOException {
		String fileName = getFileName(name);
			writeJsonFile(fileName, style);
		

	}

	private void writeJsonFile(String fileName, AdvancedStyle style) throws IOException {
		Gson gson = new Gson();
		String s = gson.toJson(style,AdvancedStyle.class);
		String path = STYLE_DIR_OUT + fileName;
		File dir = new File(STYLE_DIR_OUT);//TODO do a better thing for this
		if(!dir.exists()){
			dir.mkdir();
		}
		FileWriter fw= new FileWriter(new File(path));
		try{
			fw= new FileWriter(new File(path));
			fw.write(s);
			Log.i("STYLE","Style File updated:"+ path);	
		} catch (FileNotFoundException e) {
			Log.e("STYLE", "unable to write open file:" + path);
			throw e;
		} catch (IOException e) {
			Log.e("STYLE", "error writing the file: " + path);
			throw e;
		}finally{
			fw.close();
		}
	}

	protected String getFileName(String name) {
		return name + ".style";
	}

	
	   

	   
}
