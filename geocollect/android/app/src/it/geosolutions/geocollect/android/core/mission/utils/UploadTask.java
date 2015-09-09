package it.geosolutions.geocollect.android.core.mission.utils;

import it.geosolutions.android.map.wfs.geojson.GeoJson;
import it.geosolutions.geocollect.android.app.BuildConfig;
import it.geosolutions.geocollect.android.app.R;
import it.geosolutions.geocollect.android.core.Config;
import it.geosolutions.geocollect.android.core.form.utils.FormUtils;
import it.geosolutions.geocollect.android.core.login.LoginActivity;
import it.geosolutions.geocollect.android.core.login.utils.LoginRequestInterceptor;
import it.geosolutions.geocollect.android.core.mission.Mission;
import it.geosolutions.geocollect.android.core.mission.MissionFeature;
import it.geosolutions.geocollect.model.config.MissionTemplate;
import it.geosolutions.geocollect.model.http.CommitResponse;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import org.apache.http.message.AbstractHttpMessage;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;

import eu.geopaparazzi.library.util.ResourcesManager;

/**
 * thread that send data to the server
 * separated from the UI
 * 
 * @author Robert Oehler
 * 
 */
public abstract class UploadTask  extends AsyncTask<Void, Integer, CommitResponse> {

    /**
     * Tag for logging
     */
    public static String TAG = "UploadTask";
    
	private Context context;
	private String mediaUrl;
	private String missionID;
	private boolean deleteFromDisk;
	
	private MissionTemplate missionTemplate;
	private List<MissionFeature> featuresList;

	private String authorizationString;
	
	public UploadTask(
			Context pContext,
			MissionTemplate mMissionTemplate,
            List<MissionFeature> mFeaturesList
			){

		this.context   = pContext;
		this.deleteFromDisk = true;
		
		this.missionTemplate = mMissionTemplate;
		this.featuresList = mFeaturesList;
		
		if ( missionTemplate !=null
		  && missionTemplate.schema_seg != null){
		    this.missionID = missionTemplate.schema_seg.localSourceStore;
		}
		
		this.mediaUrl = Config.MAIN_SERVER_BASE_URL+Config.OPENSDI_PATH+Config.UPLOAD_MEDIA_PATH;

	}

	public abstract void dataDone();
	public abstract void hideMedia();
	public abstract void done(CommitResponse response);

	@Override
	protected CommitResponse doInBackground(Void... params) {

	    CommitResponse result = null ;

	    int defaultImageSize = Config.DEFAULT_MAX_PHOTO_SIZE;
        try {
            if ( missionTemplate != null
              && missionTemplate.config != null
              && missionTemplate.config.containsKey("maxImageSize") ){
                
                defaultImageSize = Integer.parseInt((String) missionTemplate.config.get("maxImageSize"));
            }
            
        } catch (NumberFormatException e) {
            if(BuildConfig.DEBUG){
                Log.e(TAG, e.getLocalizedMessage(), e);
            }
        } catch (NullPointerException e) {
            if(BuildConfig.DEBUG){
                Log.e(TAG, e.getLocalizedMessage(), e);
            }
        }
	    
		//it these are null there is no upload necessary, break
		if ( this.missionTemplate  == null
	      || this.featuresList == null){
		    
			result = new CommitResponse();
			result.setMessage("no valid arguments provided");
			result.setStatus(it.geosolutions.geocollect.model.http.Status.ERROR);
			return result;
		}

		//This upload task handles MissionFeature objects and do the JSON conversion only before upload
		////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
        GeoJson geogson = new GeoJson();
        Gson gson = new Gson();
        final int uploadAmount = featuresList.size();
        for(MissionFeature featureToUpload : featuresList){
            
            // Validate
            if(featureToUpload.typeName == null){
                if(BuildConfig.DEBUG){
                    Log.e(TAG, "Cannot upload feature: "+featureToUpload.id);
                }
                continue;
            }

            String tableName = featureToUpload.typeName.endsWith(MissionTemplate.NEW_NOTICE_SUFFIX)
                    ? missionTemplate.schema_seg.localSourceStore + MissionTemplate.NEW_NOTICE_SUFFIX
                    : missionTemplate.schema_sop.localFormStore;
            
            hideMedia();

            try {
                //1.send data
                
                // Convert to JSON
                
                // Align Feature properties
                if (featureToUpload.typeName.endsWith(MissionTemplate.NEW_NOTICE_SUFFIX)) {// new entry
                    // Edit the MissionFeature for a better JSON compliance
                    MissionUtils.alignPropertiesTypes(featureToUpload, missionTemplate.schema_seg.fields);
                }

                String featureIDString = MissionUtils.getFeatureGCID(featureToUpload);

                // Set the "MY_ORIG_ID" to link this feature to its photos
                if (featureToUpload.properties == null) {
                    featureToUpload.properties = new HashMap<String, Object>();
                }
                
                // Look for a "my_orig_id" property and populate it
                boolean hasMyOrigID = false;
                for(String inputKey: featureToUpload.properties.keySet()){
                    if(inputKey.equalsIgnoreCase(Mission.MY_ORIG_ID_STRING)){
                        featureToUpload.properties.put(inputKey, featureIDString);
                        hasMyOrigID = true;
                    }
                }
                // If not found, add it
                if(!hasMyOrigID){
                    featureToUpload.properties.put(Mission.MY_ORIG_ID_STRING, featureIDString);
                }

                MissionFeature toUpload;
                if (featureToUpload.typeName.endsWith(MissionTemplate.NEW_NOTICE_SUFFIX)) {
                    toUpload = MissionUtils.alignMissionFeatureProperties(featureToUpload,
                            missionTemplate.schema_seg.fields);
                } else {
                    toUpload = MissionUtils.alignMissionFeatureProperties(featureToUpload,
                            missionTemplate.schema_sop.fields);
                }

                String c = geogson.toJson(toUpload);
                String data = null;
                // Encode
                try {
                    
                    data = new String(c.getBytes("UTF-8"));
                    
                } catch (UnsupportedEncodingException e) {
                    if(BuildConfig.DEBUG){
                        Log.e(TAG, "error transforming missionfeature to gson", e);
                    }
                }
                
                // send
                
                String resultString = sendJson(
                        featureToUpload.typeName.endsWith(MissionTemplate.NEW_NOTICE_SUFFIX)
                        ? missionTemplate.seg_form.url
                        : missionTemplate.sop_form.url
                        , data);
                
                // Get result
                result  = getCommitResponse(gson, resultString);
                
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

                    // Resize images
                    FormUtils.resizeImagesToMax(context, featureIDString, defaultImageSize);
                    final String[] photoUrls = FormUtils.getPhotoUriStrings(context, featureIDString);
                    
                    // Send images
                    CommitResponse photosSent = sendPhotos(photoUrls, result.getId(), featureIDString, uploadAmount);
                    if(photosSent.isSuccess()){
                        
                        //both successful, can delete this entry
                        //1.delete table entry if desired
                        if(this.deleteFromDisk){
                            final Database db = getSpatialiteDatabase();

                            PersistenceUtils.deleteMissionFeature(
                                    db ,
                                    tableName ,
                                    featureIDString);
                            
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
                        
                        if(uploadList != null){
                            uploadList.remove(featureIDString);
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

		////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
		
		Gson gson = new Gson();
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
			cr = getCommitResponse(gson, res);
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

			// If authentication info exists, add it to the request
	        addAuthHeaders(httpPost);

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

		// If authentication info exists, add it to the request
		addAuthHeaders(post);

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
	public static CommitResponse getCommitResponse(Gson gson, String resultString) {
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
	
	/**
	 * Checks preferences for login informations.
	 * If found, add the Authorization header to the input request
	 * @param request
	 */
	protected void addAuthHeaders(AbstractHttpMessage request){
	    
	    // Get the Authorization from the Preferences
	    if(authorizationString == null){
	        
	        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            String email = prefs.getString(LoginActivity.PREFS_USER_EMAIL, null);
            String pass = prefs.getString(LoginActivity.PREFS_PASSWORD, null);
            if(email != null && pass != null){
                authorizationString = LoginRequestInterceptor.getB64Auth(email, pass);
            }
	    
	    }
	    
	    // If there is an authorizationString, add the header
	    if(authorizationString != null){
	        request.addHeader(new BasicHeader(HttpHeaders.AUTHORIZATION, authorizationString));
	    }
            
	}
}
