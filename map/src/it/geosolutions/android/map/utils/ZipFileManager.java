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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.*;

import it.geosolutions.android.map.MapsActivity;
import it.geosolutions.android.map.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;

/** Class to manage download and decompression of a sample data test archive from web.
 * This class manager uses two class that extend AsyncTask to perform computations in background.
 * @author Jacopo Pianigiani (jacopo.pianigiani85@gmail.com).
 */
public class ZipFileManager {
	private AlertDialog download_dialog;
	public Activity activity; //Necessary for the dialog
	private final static String file_name = "data_test_archive.zip"; //Archive that must be to download
	/**
	 * Constructor for class ZipFileManager 
	 * @param activity
	 * @param dir_path
	 */
	public ZipFileManager(final Activity activity, final String dir_path){
		this.activity = activity;
		
		//Check if folder of data already exists, otherwise it will be downloaded and unzipped.
		File f = new File(dir_path+"/maps");
		if(!f.isDirectory()){
			AlertDialog.Builder download_dialog_builder;
			download_dialog_builder = new AlertDialog.Builder(activity);
			download_dialog_builder.setTitle(R.string.dialog_title);
			download_dialog_builder.setMessage(R.string.dialog_message);
			download_dialog_builder.setCancelable(false);
			download_dialog_builder.setNegativeButton(R.string.button_download, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {	
					String url = activity.getResources().getString(R.string.url_data_test_archive); //Url data test archive
					new DownloadFileAsyncTask().execute(url,dir_path);
					download_dialog.dismiss();
				}
			});
			
			download_dialog_builder.setPositiveButton(R.string.button_undownload, new DialogInterface.OnClickListener() {		
				@Override
				public void onClick(DialogInterface dialog, int which) {
					download_dialog.dismiss();
				}
			});
			
			download_dialog = download_dialog_builder.create();
			
			//Show an alert dialog to ask user if wants to download data test archive from web
			download_dialog.show();
			}
		
		else
			launchMainActivity();
	}
	
	/**
	 * This method launch the main activity of MapStore Mobile(In View mode) once that archive is available,
	 * and center the map around the coordinate passed by intent.
	 */
	public void launchMainActivity(){
		Intent launch = new Intent(activity,MapsActivity.class);
		launch.setAction(Intent.ACTION_VIEW);
		launch.putExtra(MapsActivity.PARAMETERS.LAT, 43.68411);
		launch.putExtra(MapsActivity.PARAMETERS.LON, 10.84899);
		launch.putExtra(MapsActivity.PARAMETERS.ZOOM_LEVEL, (byte)13);
		//For the future, passing a marker
		/*ArrayList<MarkerDTO> markers = new ArrayList(1);
		MarkerDTO markerDTO1 = new MarkerDTO(43.7242359188,10.9463005959, MarkerDTO.MARKER_RED); 
		markerDTO1.setId("AB123456789");
		markerDTO1.setDescription("Segnalazione 1");
		boolean add = markers.add(markerDTO1);
		launch.putParcelableArrayListExtra(MapsActivity.PARAMETERS.MARKERS, markers);*/
		
		activity.startActivity(launch);
		activity.finish();
	}
	
	/**
	 * Class DownloadFileAsyncTask downloads a data test archive from web.
	 * @author Jacopo Pianigiani (jacopo.pianigiani85@gmail.com).
	 */
	class DownloadFileAsyncTask extends AsyncTask<String,Integer,String>{	
		private ProgressDialog barDialog; //Dialog to show progress of download
		private String dir_path; //Directory where the archive will be downloaded 
		private String path_file; //Complete path of archive downloaded
		
		/**
		 * This method will be executed before the task is executed: It creates a progress dialog
		 * to show to user progress of download.
		 */
		@Override
		protected void onPreExecute(){
			super.onPreExecute();
			barDialog = new ProgressDialog(activity);
			barDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			String message = (String)activity.getApplicationContext().getResources().getString(R.string.progress_dialog_title);
			CharSequence msg = (CharSequence)message;
			barDialog.setTitle(msg);
			message = (String)activity.getApplicationContext().getResources().getString(R.string.progress_dialog_message);
			msg = (CharSequence)message;
			barDialog.setMessage(msg);
			barDialog.setCancelable(false);
			barDialog.show();
		}
		
		/**
		 * Performs download of archive in background, this computation can requests long time depending by 
		 * the size of archive.
		 * @ param Data
		 */
		@Override
		protected String doInBackground(String ... Data) {
			dir_path = Data[1];
			int count;
			try {
				URL url = new URL(Data[0]);
				URLConnection conexion = url.openConnection();
				conexion.connect();
		
				int file_length = conexion.getContentLength();
		
				InputStream input = new BufferedInputStream(url.openStream());
				path_file = dir_path + "/" + file_name;
				OutputStream output = new FileOutputStream(path_file);
		
				byte data[] = new byte[1024];
		
				long total = 0;
				while ((count = input.read(data)) != -1) {	
						total += count;
						publishProgress((int)((total*100)/file_length));
						output.write(data, 0, count);
				}
				output.flush();
				output.close();
				input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			return null;		
		}
		
		/**
		 * Updates the progress of download showed on Progress Dialog while the computation continues 
		 * in background.
		 * @param progress
		 */
		protected void onProgressUpdate(Integer... progress) {
			barDialog.setProgress(progress[0]);
		}
		
		/**
		 * This method will be executed after the background computation finishes.
		 * @ param unused
		 */
		@Override
		protected void onPostExecute(String unused) {
			barDialog.dismiss();
			String[] data = {path_file, dir_path};
			new UnzipFileAsyncTask().execute(data);
		}	
	}
	
	/**
	 * UnzipFileAsyncTask class extracts all files contained in archive in the root directory.
	 * @author Jacopo Pianigiani (jacopo.pianigiani85@gmail.com).
	 */
	class UnzipFileAsyncTask extends AsyncTask<String,Integer,String>{	
		private ProgressDialog zipDialog; //Dialog to show progress of decompression
		private String dir_path; //path of directory where the archive has been downloaded
		
		/**
		 * This method will be executed before the task is executed, it creates a progress dialog
		 * to notify to user that the decompression is in progress.
		 */
		@Override
		protected void onPreExecute(){
			super.onPreExecute();
			zipDialog = new ProgressDialog(activity);
			String message = (String)activity.getApplicationContext().getResources().getString(R.string.progress_dialog_title);
			CharSequence msg = (CharSequence)message;
			zipDialog.setTitle(msg);
			message = (String)activity.getApplicationContext().getResources().getString(R.string.progress_dialog_unzipping);
			msg = (CharSequence)message;
			zipDialog.setMessage(msg);
			zipDialog.setCancelable(false);
			zipDialog.show();
		}
		
		/**
		 * Performs decompression of archive, already downloaded from web, in background.
		 * @ param path
		 */
		@Override
		protected String doInBackground(String ... path) {	
			this.dir_path = path[1];
			try {
		    	FileInputStream fis = new FileInputStream(path[0]);
		        ZipInputStream zis = new ZipInputStream(fis);
		        ZipEntry ze = null;
	        	while((ze = zis.getNextEntry())!=null){
	        		if(ze.isDirectory())
	        			dirChecker(path[1]+ "/" + ze.getName());
	        		else{
		                FileOutputStream fout = new FileOutputStream(path[1] + "/" + ze.getName(),true);
		                byte[] buffer = new byte[1024];
		                for (int lenght = zis.read(buffer); lenght != -1; lenght = zis.read(buffer))
		                    fout.write(buffer,0,lenght);
		                    
		                //zis.closeEntry();
		                fout.close();
	        			}	
	        	}
	            zis.close();
	        	fis.close();
		    }catch(Exception e ){
		    	e.printStackTrace();
		    }		
			
			return null;	
		}

		/** This method checks if a directory(location is her path) already exists, if not, creates it using
		 * the path passed by argument.
		 * @param location
		 */
		private void dirChecker(String location) {
			File f = new File(location);
			if(!f.exists())
				f.mkdirs();
		}
		
		/**
		 * This method will be executed after the background computation finishes.
		 * @ param unused
		 */
		@Override
		protected void onPostExecute(String unused) {
			//Delete downloaded archive
			File file = new File(dir_path+"/"+file_name);
			if(file.exists())
				file.delete();
			
			zipDialog.dismiss();
			launchMainActivity();
		}	
	}
}