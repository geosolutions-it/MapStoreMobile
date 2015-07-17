package it.geosolutions.geocollect.android.core.mission.utils;

import it.geosolutions.geocollect.android.app.R;
import it.geosolutions.geocollect.model.http.CommitResponse;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import jsqlite.Database;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;

import eu.geopaparazzi.library.util.ResourcesManager;

/**
 * thread  that send data to the server
 * 
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 * 
 * separated from the UI by
 * 
 * @author Robert Oehler
 * 
 */
public abstract class UploadTask  extends AsyncTask<Void, Integer, CommitResponse> {

	private Context context;
	private HashMap<String, String> uploads;
	private HashMap<String, String[]> mediaUrls;
	private String dataUrl;
	private String mediaUrl;
	private String tableName;
	private String missionID;
	private String auth;
	private boolean deleteFromDisk;

	public UploadTask(
			Context pContext,
			HashMap<String, String> pUploads,
			HashMap<String, String[]> pMediaUrls,
			String pDataUrl,
			String pMediaUrl,
			String pTableName,
			String pMissionID,
			String pAuth,
			boolean pDeleteFromDisk
			){

		this.context   = pContext;
		this.uploads   = pUploads;
		this.mediaUrls = pMediaUrls;
		this.dataUrl   = pDataUrl;
		this.mediaUrl  = pMediaUrl;
		this.tableName = pTableName;
		this.missionID = pMissionID;
		this.auth      = pAuth;
		this.deleteFromDisk = pDeleteFromDisk;

	}

	public abstract void dataDone();
	public abstract void hideMedia();
	public abstract void done(CommitResponse response);

	@Override
	protected CommitResponse doInBackground(Void... params) {
		CommitResponse result = null ;

		//it these are null there is no upload necessary, break
		if(this.uploads  == null || this.mediaUrls == null){
			result = new CommitResponse();
			result.setMessage("no valid arguments provided");
			result.setStatus(it.geosolutions.geocollect.model.http.Status.ERROR);
			return result;
		}

		final int uploadAmount = uploads.size();
		for(String id : uploads.keySet()){

			hideMedia();

			try {
				//1.send data
				final String data = uploads.get(id);
				String resultString = sendJson(this.dataUrl, data);
				result  = getCommitResponse(resultString);
				
				if(result == null){ //most likely network error
					result = new CommitResponse();
					result.setMessage("network error transfering data");
					result.setStatus(it.geosolutions.geocollect.model.http.Status.ERROR);
					return result;
				}

				if(result.isSuccess()){ //data upload successful
					//2. send media
					//UI update -> media upload
					dataDone();

					final String[] photoUrls = this.mediaUrls.get(id);
					CommitResponse photosSent = sendPhotos(photoUrls, result.getId(), id, uploadAmount);
					if(photosSent.isSuccess()){
						//both successful, can delete this entry
						//1.delete table entry if desired
						if(this.deleteFromDisk){
							final Database db = getSpatialiteDatabase();
							PersistenceUtils.deleteMissionFeature(db, this.tableName, id);
							try {
								db.close();
							} catch (jsqlite.Exception e) {
								// ignore
							}	
						}
						//2.delete photos
						if(photoUrls != null){
							for(String file : photoUrls){
								File f = new File(file);
								if(f.exists()){
									f.delete();
								}
							}
						}
						//3.delete this entry as "uploadable"
						HashMap<String,ArrayList<String>> uploadables = PersistenceUtils.loadUploadables(this.context);
						ArrayList<String> uploadList = uploadables.get(tableName);
						if(uploadList.contains(id)){
							uploadList.remove(id);
						}
						uploadables.put(tableName, uploadList);
						PersistenceUtils.saveUploadables(this.context, uploadables);

					}else{
						//TODO data sent, photos failed, what to delete ?
					}

				}else{
					// data upload failed
					//TODO report this or continue with others ?
				}
			} catch (Exception e) {
				Log.e("SendData", "ErrorSending Data",e);
			}
		}
		
		done(result);

		return result;
	}

	private CommitResponse sendPhotos(String[] filePaths, String dataSentID, String featureID, final int uploadAmount){

		CommitResponse cr =null;
		//the output id

		int i = 0;
		//no media to send create a dummy commit response to return success
		if( filePaths == null || filePaths.length == 0 ){
			cr = new CommitResponse();
			cr.setStatus(it.geosolutions.geocollect.model.http.Status.SUCCESS);
			cr.setMessage(this.context.getString(R.string.no_media_to_send));
			return cr;
		}
		//<MEDIA_URL>/<MISSIONID>/ORIGINID/outID/upload
		String mediaUrl = this.mediaUrl + 
				"/" + this.missionID +
				"/" + featureID +
				"/" + dataSentID +
				"/upload";
		for(String file : filePaths){
			String res;

			try {
				File f =  new File(new URI(file)) ;
				if(!f.exists()){
					return cr;
				}
				res = sendMedia(f,mediaUrl );
			} catch (URISyntaxException e) {
				Log.e("SendMedia","error sending media, unable read URI:" + file,e);
				return cr;
			}
			publishProgress((int) (i / (float) uploadAmount));
			cr = getCommitResponse(res);
			if(cr==null || !cr.isSuccess()){
				return cr;
			}
		}
		return cr;
	}

	private String sendMedia(File file,String mediaUrl){
		HttpClient httpClient = new DefaultHttpClient();
		HttpContext localContext = new BasicHttpContext();
		try {
			mediaUrl +="?name=" + file.getName(); 
			URI url = new URI(mediaUrl);
			HttpPost httpPost = new HttpPost(url);

			Log.i("SendMedia","Sending data to:"+ mediaUrl);
			//			MultipartEntity multiEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);


			ContentBody cBody = new FileBody(file);
			MultipartEntity multipartContent = new MultipartEntity();
			multipartContent.addPart("file", cBody);
			httpPost.setEntity(multipartContent); 

			httpPost.addHeader(new BasicHeader(HttpHeaders.AUTHORIZATION, this.auth));

			HttpResponse response = httpClient.execute(httpPost, localContext);
			return EntityUtils.toString(response.getEntity());
		} catch (ClientProtocolException e) {
			Log.e("SendMedia","error sending media:",e);
		} catch (IOException e) {
			Log.e("SendMedia","error sending media:",e);
		} catch (URISyntaxException e) {
			Log.e("SendMedia", "error parsing media url",e);
			e.printStackTrace();
		}
		return null;
	}


	protected String sendJson(final String url, final String json) throws ClientProtocolException, IOException {
		HttpClient client = new DefaultHttpClient();
		HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000); //Timeout Limit
		HttpResponse response;

		HttpPost post = new HttpPost(url);

		StringEntity se = new StringEntity( json.toString(), "UTF-8");  
		se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
		se.setContentEncoding(new BasicHeader(HTTP.CHARSET_PARAM, "UTF-8"));
		post.setEntity(se);

		post.addHeader(new BasicHeader(HttpHeaders.AUTHORIZATION, this.auth));

		response = client.execute(post);

		/*Checking response */
		if(response!=null){
			return  EntityUtils.toString(response.getEntity());
		}
		return null;

	}

	/**
	 * Parse the result string to get a commit response
	 * @param resultString
	 * @return
	 */
	private CommitResponse getCommitResponse(String resultString) {
		Gson gson = new Gson();
		CommitResponse cr  = null;
		try{
			cr= gson.fromJson(resultString, CommitResponse.class);
		}catch(Exception e){
			Log.e("SendData","Error parsing commit response:"+ resultString,e);
		}
		return cr;

	}

	public Database getSpatialiteDatabase(){

		Database spatialiteDatabase = null;

		try {

			File sdcardDir = ResourcesManager.getInstance(this.context).getSdcardDir();
			File spatialDbFile = new File(sdcardDir, "geocollect/genova.sqlite");

			if (!spatialDbFile.getParentFile().exists()) {
				throw new RuntimeException();
			}
			spatialiteDatabase = new jsqlite.Database();
			spatialiteDatabase.open(spatialDbFile.getAbsolutePath(), jsqlite.Constants.SQLITE_OPEN_READWRITE
					| jsqlite.Constants.SQLITE_OPEN_CREATE);

			Log.v("MISSION_DETAIL", SpatialiteUtils.queryVersions(spatialiteDatabase));
			Log.v("MISSION_DETAIL", spatialiteDatabase.dbversion());

			return spatialiteDatabase;

		} catch (Exception e) {
			Log.v("MISSION_DETAIL", Log.getStackTraceString(e));
			return null;
		}

	}
}
