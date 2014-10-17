package it.geosolutions.geocollect.android.core.widgets.dialog;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
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
import org.json.JSONObject;

import com.google.gson.Gson;

import it.geosolutions.geocollect.android.core.R;
import it.geosolutions.geocollect.model.http.CommitResponse;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
/**
 * Fragment that shows uplaod status
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public class UploadDialog extends RetainedDialogFragment {

	private ProgressBar dataProgress;
	private ProgressBar progressMedia;
	private TextView txtDataSend;
	private TextView txtMediaSend;
	private ImageView imgOKData;
	private ImageView imgOKMedia;
	private ImageView imgBadData;
	private ImageView imgBadMedia;
	private boolean skipData = false;
	private Activity activity;
	private static boolean sending = false;
	private String[] photoURIs;
	private String missionId;
	public static class PARAMS {
		public static final String DATAURL="URL";
		public static final String MEDIAURL="MEDIAURL";
		public static final String DATA="DATA";
		public static final String ORIGIN_ID = "ORIGIN_ID";
		public static final String MISSION_ID="MISSION_ID";
		public static final String MEDIA="MEDIA";
	}

	public UploadDialog() {
		// Empty constructor required for DialogFragment
		super();

	}
	/**
	 * Create the view that display current upload progress 
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			//TODO the retained fragment call this method.
			if(getView()==null){;
				View view = inflater.inflate(R.layout.progress_send, container);
				getDialog().setTitle(getString(R.string.sending_data));
				getDialog().setCancelable(false);
				//if rotation continue to have problems, block orientation change.
				//don't forget to remove the requested orientation after finish
				//getActivity().setRequestedOrientation(getActivity().getResources().getConfiguration().orientation);
				return view;
			} else return getView();
			
		
			//return super.onCreateView( inflater,  container,  savedInstanceState);
		

		
	}
	/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onViewCreated(android.view.View, android.os.Bundle)
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		if( sending == false ){
			setupControls();
		}
	}

	
	/**
	 * setup view components (progress elements and textviews for data and progress)
	 */
	protected void setupControls() {
		dataProgress = (ProgressBar) getView().findViewById(
				R.id.progress_data_send);

		progressMedia = (ProgressBar) getView().findViewById(
				R.id.progress_media_send);

		txtDataSend = (TextView) getView().findViewById(
				R.id.txt_data_send);

		txtMediaSend = (TextView) getView().findViewById(
				R.id.txt_media_send);

		imgOKData = (ImageView) getView().findViewById(
				R.id.img_data_send_ok);
		imgBadData = (ImageView) getView().findViewById(
				R.id.img_data_send_bad);

		imgOKMedia = (ImageView) getView()
				.findViewById(R.id.img_media_send_ok);
		imgBadMedia = (ImageView) getView().findViewById(
				R.id.img_media_send_bad);

	}

	@Override
	public void onActivityCreated(Bundle arg0) {
		super.onActivityCreated(arg0);
		
		if (isAdded() && (!sending)) {
			new SendDataThread().execute();
		}
	}
	public class Result{
		
		
	}

	/**
	 * thread  that send data to the server
	 * 
	 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
	 * 
	 */
	private class SendDataThread extends
			AsyncTask<Void, Void, CommitResponse> {
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			sending = true;
	
				setupDataControls(true);
			

		}

		@Override
		protected CommitResponse doInBackground(Void... params) {
			CommitResponse result = null ;

			if (!skipData) {
				
				try {
					String resultString = sendJson(getArguments().getString("URL"),getArguments().getString("DATA"));
					result = getCommitResponse(resultString);
				} catch (Exception e) {
					Log.e("SendData", "ErrorSending Data",e);
				}

			}

			return result;
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
                 response = client.execute(post);

                 /*Checking response */
                 if(response!=null){
                     return  EntityUtils.toString(response.getEntity());
                 }
                 return null;
                 

                
		    }

		@Override
		protected void onPostExecute(CommitResponse result) {
			super.onPostExecute(result);
			if(result == null || !result.isSuccess()){
				setDataSendResultUI(false);
				closeDialog(false);
				Toast.makeText(activity, R.string.error_sending_data, Toast.LENGTH_LONG).show();
				Activity c = activity != null ? activity : getActivity();
				onFinish(c, result);
			}else{
				setupDataControls(false);
				setDataSendResultUI(true);
				new MediaSenderThread().execute(result.getId());
			}
		}
	}

	/**
	 * Send Media data dummy Thread 
	 * 
	 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
	 * 
	 */
	private class MediaSenderThread extends AsyncTask<String, Integer, CommitResponse> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();

				setupMediaControl(true);
			

		}

		@Override
		protected CommitResponse doInBackground(String... params) {
			//Dummy 
			//Thread.sleep(5000)
			CommitResponse cr =null;
			//the output id
			String idOut = params[0];
			String[] filePaths = getArguments().getStringArray(PARAMS.MEDIA);
			int i = 0;
			//no media to send create a dummy commit response to return success
			if( filePaths == null || filePaths.length == 0 ){
				cr = new CommitResponse();
				cr.setStatus(it.geosolutions.geocollect.model.http.Status.SUCCESS);
				cr.setMessage(getString(R.string.no_media_to_send));
				return cr;
			}
			//<MEDIA_URL>/<MISSIONID>/ORIGINID/outID/upload
			String mediaUrl = getArguments().getString(PARAMS.MEDIAURL) + 
					"/" + getArguments().getString(PARAMS.MISSION_ID) +
					"/" + getArguments().getString(PARAMS.ORIGIN_ID) +
					"/" + idOut +
					"/upload";
			for(String file : filePaths){
				String result;
				
				try {
					File f =  new File(new URI(file)) ;
					if(!f.exists()){
						return cr;
					}
					result = sendMedia(f,mediaUrl );
				} catch (URISyntaxException e) {
					Log.e("SendMedia","error sending media, unable read URI:" + file,e);
					return cr;
				}
				publishProgress(i);
				 cr = getCommitResponse(result);
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
			MultipartEntity multiEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

			
			ContentBody cBody = new FileBody(file);
			MultipartEntity multipartContent = new MultipartEntity();
			multipartContent.addPart("file", cBody);
			httpPost.setEntity(multipartContent); 
			
			
				
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

		@Override
		protected void onPostExecute(CommitResponse result) {
			if(result == null || !result.isSuccess()){
				setDataSendResultUI(false);
				setMediaSendResultUI(false);
				closeDialog(true);
				Activity c = activity != null ? activity : getActivity();
				onFinish(c, result);
			}else{
				//SUCCESS
				setupMediaControl(false);
				setMediaSendResultUI(true);
				closeDialog(true);
				Activity c = activity != null ? activity : getActivity();
				onFinish(c,result);
			}
			
		}
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
	/**
	 * CloseDialog
	 * 
	 * @param closeActivity
	 */
	private void closeDialog(boolean closeActivity) {

		//getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
		this.dismiss();
		sending = false;
		
	}

	

	/**
	 * Method to override called when application finish
	 * @param activity the current running activity
	 * @param result
	 */
	public void onFinish(Activity ctx, CommitResponse result){
		
	}
	/**
	 * setup the controls for data upload
	 * 
	 * @param started
	 */
	private void setupDataControls(boolean started) {
		
		if (started) {
			//display progress
			dataProgress.setVisibility(View.VISIBLE);
			txtDataSend.setTypeface(null, Typeface.BOLD_ITALIC);
		} else {
			//hide progress
			dataProgress.setVisibility(View.GONE);
			txtDataSend.setTypeface(null, Typeface.NORMAL);
		}
	}

	/**
	 * setup the controls for media upload
	 * 
	 * @param started
	 */
	private void setupMediaControl(boolean started) {
		if (started) {
			//display progress
			progressMedia.setVisibility(View.VISIBLE);
			txtMediaSend.setTypeface(null, Typeface.BOLD_ITALIC);
		} else {
			//hide progress
			progressMedia.setVisibility(View.GONE);
			txtMediaSend.setTypeface(null, Typeface.NORMAL);
		}
	}

	/**
	 * set
	 * 
	 * @param set up result for data send
	 */
	private void setDataSendResultUI(boolean success) {
		if (success) {
			imgBadData.setVisibility(View.GONE);
			imgOKData.setVisibility(View.VISIBLE);
		} else {
			imgBadData.setVisibility(View.VISIBLE);
			imgOKData.setVisibility(View.GONE);
		}
	}

	/**
	 * Setup result for media send
	 * 
	 * @param success
	 */
	private void setMediaSendResultUI(boolean success) {
		if (success) {
			imgBadMedia.setVisibility(View.GONE);
			imgOKMedia.setVisibility(View.VISIBLE);
		} else {
			imgBadMedia.setVisibility(View.VISIBLE);
			imgOKMedia.setVisibility(View.GONE);
		}
	}
	
	/**
	 * Refresh the activity reference when changes
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = activity;
	}
	
}