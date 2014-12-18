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

import it.geosolutions.android.map.utils.MapFilesProvider;
import it.geosolutions.geocollect.android.core.R;
import it.geosolutions.geocollect.android.core.form.action.AndroidAction;
import it.geosolutions.geocollect.android.core.form.action.CameraAction;
import it.geosolutions.geocollect.android.core.form.action.CenterOnMarkerAction;
import it.geosolutions.geocollect.android.core.form.action.LocalizeAction;
import it.geosolutions.geocollect.android.core.form.action.SendAction;
import it.geosolutions.geocollect.model.viewmodel.FormAction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
	private static String ICON_CENTER_ON_MARKER="ic_center";
	private static String ICON_LOCALIZE="ic_localize";

	
	
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
		}else if(ICON_ACCEPT.equals(iconCls)){
			return R.drawable.ic_rating_good;
		}else if(ICON_CAMERA.equals(iconCls)){
			return R.drawable.ic_device_access_camera;
		}else if(ICON_CENTER_ON_MARKER.equals(iconCls)){
			return R.drawable.ic_center;
		}else if(ICON_LOCALIZE.equals(iconCls)){
			return R.drawable.ic_localize_marker;
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
			break;
		case localize:
			return new LocalizeAction(a);
		case center:
			return new CenterOnMarkerAction(a);
		}
		return null;
		
	}
	
	/**
	 * resize the images in the folder to the provided max size of the larger dimension
	 * @param feature_id
	 * @param maxSize
	 */
	public static void resizeFotosToMax(final Context context,final String feature_id,final int maxSize){
		
		if(feature_id == null || feature_id.isEmpty()){
			Log.w("FormUtils", "getPhotoUriStrings: Could not get feature_id");
			return;
		}
			
		File folder = new File(MapFilesProvider.getEnvironmentDirPath(context)+"/geocollect/media/"+feature_id);
		folder.mkdirs();
		File[] listOfFiles = folder.listFiles();

		if(listOfFiles == null){
			// Zero-length array as "not found"
			return;
		}
		
		//resize
		
		for (int i = 0; i < listOfFiles.length; i++) {

			Bitmap resizedBitmap = null;
			try {
				
				//Following http://developer.android.com/training/displaying-bitmaps/load-bitmap.html#load-bitmap
				//to avoid OOE decoding bitmaps, read out the bounds of the image, calculate the sample size
				//and only then decode the already resized image, which will need less memory
				
				BitmapFactory.Options options = new BitmapFactory.Options();
				//this won't allocate memory but read out only the bounds of the image
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(listOfFiles[i].getAbsolutePath(), options);

				// Calculate inSampleSize
				final int sampleSize = calculateInSampleSize(options,maxSize);
				if(sampleSize == 1){
					//no need to resample, this image is small enough
					continue;
				}
				options.inSampleSize = sampleSize;

				// Decode bitmap with inSampleSize set
				options.inJustDecodeBounds = false;
				resizedBitmap = BitmapFactory.decodeFile(listOfFiles[i].getAbsolutePath(), options);

			} catch (OutOfMemoryError e) {
				//according to http://stackoverflow.com/questions/7138645/catching-outofmemoryerror-in-decoding-bitmap
				//we could try to catch that OOE and do System.gc() but I think the above should avoid OOEs
				//so for now give up
				Log.e(FormUtils.class.getSimpleName(), "OOE decoding Bitmap ",e);
				continue;
			}


			try {
				final File origFile = listOfFiles[i];

				final String newfileName = origFile.getName().substring(0,origFile.getName().lastIndexOf("."))+"_resized";
				final String extension   = origFile.getName().substring(origFile.getName().lastIndexOf(".") + 1);

				final String newFile = origFile.getPath().substring(0,origFile.getPath().lastIndexOf("/") + 1) + newfileName+"."+extension;

				FileOutputStream fOut = new FileOutputStream(newFile);

				resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);

				fOut.flush();
				fOut.close();

				//all worked out, delete original
				origFile.delete();

			} catch (FileNotFoundException e) {
				Log.e(FormUtils.class.getSimpleName(), e.getClass().getSimpleName(),e);
			} catch (IOException e) {
				Log.e(FormUtils.class.getSimpleName(), e.getClass().getSimpleName(),e);
			}

		}
	}
	/**
	 * calculates the samplesize (the scale factor 1 / n) to subsample the image
	 * Note : A power of two value is calculated because the decoder uses a final value by rounding down
	 * to the nearest power of two, as per the inSampleSize documentation.
	 * @param options
	 * @param maxSize
	 * @return
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options, final int maxSize) {
		
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		
		int max = height > width ? height : width;

		float factor = (float) max / maxSize;

		int reqWidth  = (int) (width / factor);
		int reqHeight = (int) (height / factor);
		
		if (height > reqHeight || width > reqWidth) {

			// Calculate the  inSampleSize value that is a power of 2 and will keep both
			// height and width smaller than the required
			while (height / inSampleSize > reqHeight && width / inSampleSize > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}



	/**
	 * Return a list of Strings representing Uris for the media
	 * TODO: Filter media based on feature.id
	 * @return
	 */
	public static String[] getPhotoUriStrings(final Context context, final String feature_id){
		
		if(feature_id == null || feature_id.isEmpty()){
			Log.w("FormUtils", "getPhotoUriStrings: Could not get feature_id");
			return new String[0];
		}
			
		File folder = new File(MapFilesProvider.getEnvironmentDirPath(context)+"/geocollect/media/"+feature_id);//TODO Parametrize this
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


	/**
	 * Given a string encoded file URI, delete that file
	 * @param filePath
	 * @return boolean whether the file was deleted or not
	 */
	public static boolean deleteFile(String filePath){
		if(filePath == null)
			return false;
		
		URI fileURI = URI.create(filePath);
		
		File f = new File(fileURI);
		if(f == null || !f.exists() || !f.isFile()|| !f.canWrite() ){
			return false;
		}
		return f.delete();
		
	}
}
