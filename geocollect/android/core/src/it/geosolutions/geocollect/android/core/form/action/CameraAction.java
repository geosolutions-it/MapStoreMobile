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
package it.geosolutions.geocollect.android.core.form.action;

import java.io.File;
import com.actionbarsherlock.app.SherlockFragment;

import it.geosolutions.geocollect.android.core.mission.Mission;
import it.geosolutions.geocollect.model.viewmodel.FormAction;
import it.geosolutions.geocollect.model.viewmodel.Page;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

/**
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public class CameraAction extends AndroidAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * Constructor from an <Action>
	 * @param a
	 */
	public CameraAction(FormAction a) {
		super(a);
		
	}
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 123;

	
	/**
	 * Provides the image file URI
	 * @param mediaTypeImage
	 * @param feature_id 
	 * @return
	 */
	private static Uri getOutputMediaFileUri(int mediaTypeImage, String feature_id) {
		File baseDir = new File(Environment.getExternalStorageDirectory().getPath()+"/geocollect/media/"+feature_id);
		
		baseDir.mkdirs();
		
		File f = new File (baseDir, String.valueOf(System.currentTimeMillis()) + ".jpg");
		
		return Uri.fromFile(f);

	}


	/* (non-Javadoc)
	 * @see it.geosolutions.geocollect.android.core.form.action.AndroidAction#performAction(android.support.v4.app.Fragment, it.geosolutions.geocollect.model.viewmodel.Action, it.geosolutions.geocollect.android.core.mission.Mission, it.geosolutions.geocollect.model.viewmodel.Page)
	 */
	@Override
	public void performAction(SherlockFragment fragment, FormAction action, Mission m,
			Page p) {
		
		if(m == null || m.getOrigin() == null || m.getOrigin().id.isEmpty()){
			
	    	Log.w("CameraAction", "Could not start intent, feature id not found");
			return;
		}
		
		// create Intent to take a picture and return control to the calling application
	    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    Uri fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE, m.getOrigin().id); // create a file to save the image
	    if(fileUri != null){
		    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
		    // start the image capture Intent
		    fragment.startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	    }else{
	    	Log.w("CameraAction", "Could not start intent, bad Uri");
	    }
	}
	
}
