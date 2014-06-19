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
package it.geosolutions.geocollect.android.core.form.utils;

import java.io.File;
import java.util.ArrayList;

import it.geosolutions.geocollect.android.core.R;
import it.geosolutions.geocollect.android.core.form.action.AndroidAction;
import it.geosolutions.geocollect.android.core.form.action.CameraAction;
import it.geosolutions.geocollect.android.core.form.action.SendAction;
import it.geosolutions.geocollect.android.core.mission.Mission;
import it.geosolutions.geocollect.model.viewmodel.FormAction;
import it.geosolutions.geocollect.model.viewmodel.Page;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;


/**
 * A static class that provide utility methods
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public class FormUtils {
	private static String ICON_SEND ="ic_send";
	private static String ICON_ACCEPT="ic_accept";
	private static String ICON_CAMERA="ic_camera";

	
	
	/**
	 * Provides the drawable for the icon class in <MissionTemplate>
	 * @param iconCls
	 * @return
	 */
	public static Integer getDrawable(String iconCls) {
		if(iconCls == null){
			return null;
		}
		if(ICON_SEND.equals(iconCls)){
			return R.drawable.ic_social_send_now;
		}
		if(ICON_ACCEPT.equals(iconCls)){
			return R.drawable.ic_rating_good;
		}if(ICON_CAMERA.equals(iconCls)){
			return R.drawable.ic_device_access_camera;
		}else{
			return null;
		}
	}



	/**
	 * @param a
	 */
	public static AndroidAction getAndroidAction(FormAction a) {
		switch(a.type){
		case confirm:
		case send:
			return new SendAction(a);
		case photo:
			return new CameraAction(a);
		case video:
		}
		return null;
		
	}
	
	/**
	 * Return a list of Strings representing Uris for the media
	 * TODO: Filter media based on feature.id
	 * @return
	 */
	public static String[] getPhotoUriStrings(String feature_id){
		
		if(feature_id == null || feature_id.isEmpty()){
			Log.w("FormUtils", "getPhotoUriStrings: Could not get feature_id");
			return new String[0];
		}
			
		File folder = new File(Environment.getExternalStorageDirectory().getPath()+"/geocollect/media/"+feature_id);
		folder.mkdirs();
		File[] listOfFiles = folder.listFiles();

		if(listOfFiles == null){
			// Zero-length array as "not found"
			return new String[0];
		}
		
		ArrayList<String> newFileListUri = new ArrayList<String>();
	    for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				newFileListUri.add(Uri.fromFile(listOfFiles[i]).toString());
			} 
	    }
		
	    return newFileListUri.toArray(new String[newFileListUri.size()]);
	}

}
