package it.geosolutions.geocollect.android.core.widgets.dialog;

import it.geosolutions.geocollect.android.core.R;
import it.geosolutions.geocollect.android.core.mission.PendingMissionListActivity;
import it.geosolutions.geocollect.android.core.mission.utils.PersistenceUtils;
import it.geosolutions.geocollect.android.core.mission.utils.UploadTask;
import it.geosolutions.geocollect.model.http.CommitResponse;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;

import jsqlite.Database;

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

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
/**
 * Fragment that shows uplaod status
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 * 
 * adapted for multiple uploads in one background thread by
 * @author Robert Oehler
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
	
	
	public static class PARAMS {
		public static final String DATAURL="URL";
		public static final String MEDIAURL="MEDIAURL";
		public static final String MISSION_ID="MISSION_ID";
		public static final String AUTH_KEY="AUTH_KEY";
		public static final String BASIC_AUTH="BASIC_AUTH";
		public static final String MISSIONS= "MISSIONS";
		public static final String MISSION_MEDIA_URLS= "MISSION_MEDIA_URLS";
		public static final String TABLENAME = "TABLENAME";
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

	@SuppressWarnings("unchecked")
	@Override
	public void onActivityCreated(Bundle arg0) {
		super.onActivityCreated(arg0);
		
		if (isAdded() && (!sending)) {
			new UIUploadTask(
					getActivity(),
					(HashMap<String, String>) getArguments().getSerializable(PARAMS.MISSIONS),
					(HashMap<String, String[]>) getArguments().getSerializable(PARAMS.MISSION_MEDIA_URLS),
					(String)getArguments().getSerializable(PARAMS.DATAURL),
					(String)getArguments().getSerializable(PARAMS.MEDIAURL),
					(String)getArguments().getSerializable(PARAMS.TABLENAME),
					(String)getArguments().getSerializable(PARAMS.MISSION_ID),
					(String)getArguments().getSerializable(PARAMS.BASIC_AUTH)
					)
			.execute();
		}
	}
	public class Result{
		
		
	}
	/**
	 * extends Uploadtask giving feedback about the upload status on the UI
	 * 
	 * @author Robert Oehler
	 *
	 */
	private class UIUploadTask extends UploadTask{
		
		public UIUploadTask(Context pContext, HashMap<String, String> pUploads,
				HashMap<String, String[]> pMediaUrls, String pDataUrl,
				String pMediaUrl, String pTableName, String pMissionID,
				String pAuth) {
			super(pContext, pUploads, pMediaUrls, pDataUrl, pMediaUrl, pTableName,pMissionID, pAuth, true);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			sending = true;

			setupDataControls(true);
		}	

		@Override
		protected void onPostExecute(CommitResponse result) {
			super.onPostExecute(result);
			if(result == null || !result.isSuccess()){
				setDataSendResultUI(false);
				setMediaSendResultUI(false);
				closeDialog(false);
				Toast.makeText(activity, R.string.error_sending_data, Toast.LENGTH_LONG).show();
				Activity c = activity != null ? activity : getActivity();
				onFinish(c, result);
			}else {
				//SUCCESS
				setupDataControls(false);
				setDataSendResultUI(true);
				setupMediaControl(false);
				setMediaSendResultUI(true);
				closeDialog(true);
				Activity c = activity != null ? activity : getActivity();
				onFinish(c,result);
			}
		}
		
		@Override
		public void hideMedia() {
			//UI update -> data upload
			UploadDialog.this.getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					setupMediaControl(false);
					setupDataControls(true);
				}
			});

		}
		@Override
		public void dataDone() {
			UploadDialog.this.getActivity().runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					setupDataControls(false);
					setDataSendResultUI(true);
					setupMediaControl(true);
				}
			});
		}

		@Override
		public void done(CommitResponse response) {
			//this method is used for tests			
		}
		
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
	
	
	public interface UploadCallback{
		
		public void success();
		
		public void error();
	}
	
}