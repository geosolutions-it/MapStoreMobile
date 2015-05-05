package it.geosolutions.geocollect.android.template;

import it.geosolutions.android.map.geostore.model.ResourceList;
import it.geosolutions.geocollect.android.core.BuildConfig;
import it.geosolutions.geocollect.android.core.Config;
import it.geosolutions.geocollect.android.core.login.utils.GsonUtil;
import it.geosolutions.geocollect.android.core.mission.utils.MissionUtils;
import it.geosolutions.geocollect.model.config.MissionTemplate;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RestAdapter.LogLevel;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
/**
 * Class which extends Asynctask to download remote MissionTemplates
 * 
 * it makes 2 steps
 * 
 * 1.downloads a (simple) list of available templates - does not need authentification
 * 
 * 2.downloads the detailed templates, a login (authkey) is necessary
 * 
 * if successful the callback will return with the list of downloaded templates
 * 
 * otherwise error messages are logged
 * 
 * 
 * @author Robert Oehler
 *
 */
public abstract class TemplateDownloadTask  extends AsyncTask<String, Void, Void>{

	final static String TAG = TemplateDownloadTask.class.getSimpleName();

	private final static String endPoint = Config.MAIN_SERVER_BASE_URL + Config.GEOSTORE_PATH;
	
	private int arrived = 0;
	
	private ArrayList<MissionTemplate> downloads = new ArrayList<MissionTemplate>();
	
	public abstract void complete(ArrayList<MissionTemplate> downloaded);

	@Override
	protected Void doInBackground(final String... params) {

	    String authorizationString = "";
	    if(params.length > 1){
	        authorizationString = params[1];
	    }
	    
		getRemoteTemplates(authorizationString, new RemoteTemplatesFetchCallback() {

			@Override
			public void templatesReceived(String authorizationString, ResourceList list) {

				if(list != null && list.list.size() > 0){

					final int awaiting = list.list.size();
					//list worked using a "geostore" resource
					for(final it.geosolutions.android.map.geostore.model.Resource resource : list.list){

						downloadRemoteTemplate(authorizationString, resource.id, new SingleRemoteTemplateFetchCallback (){

							@Override
							public void received(Resource res) {
								
								//to download a single template, a slightly different Resource was needed
								//TODO use geostore resource when server side has applied the according schema
								
								String templateString = res.getData().getData();

								downloads.add(MissionUtils.getTemplateFromJSON(templateString));
								
                                if(BuildConfig.DEBUG){
                                    Log.v(TAG, "Received resource with id: "+res.getId());
                                }
								
								arrived++;
								
								if(arrived == awaiting){
									complete(downloads);
								}
							}

							@Override
							public void error(RetrofitError error) {

                                if(BuildConfig.DEBUG){
                                    Log.e(TAG, "error getting template "+resource.id+" : "+ error.getMessage());
                                }

								arrived++;
								
								if(arrived == awaiting){
									complete(downloads);
								}
							}

						}); 
					}
				}else{
					Log.e(TAG, "none or empty list received, cannot download templates");
				}
			}

			@Override
			public void error(RetrofitError error) {

				Log.e(TAG, "error getting template list : "+ error.getMessage());

			}
		});
		return null;
	}
	
	/**
	 * downloads the list of available remote templates
	 * @param callback giving feedback of the result of the operation
	 */
	public static void getRemoteTemplates(final String authorizationString, final RemoteTemplatesFetchCallback callback){

		Gson gson = GsonUtil.createFeatureGson();
		RestAdapter restAdapter = new RestAdapter.Builder()
		.setEndpoint(endPoint)
		.setConverter(new GsonConverter(gson))
		.setRequestInterceptor(new RequestInterceptor() {
			@Override
			public void intercept(RequestFacade request) {
				request.addHeader("Accept", "application/json;");
				request.addHeader("Authorization", authorizationString);
			}
		})
		//.setLogLevel(LogLevel.FULL)
		.build();

		GeoCollectTemplateDownloadServices services = restAdapter.create(GeoCollectTemplateDownloadServices.class);
		services.getTemplates(new Callback<RemoteTemplateListResponse>() {

			@Override
			public void success(RemoteTemplateListResponse rl, Response response) {

				callback.templatesReceived(authorizationString, rl.ResourceList);	
			}

			@Override
			public void failure(RetrofitError error) {

				callback.error(error);
			}
		});



	}

	/**
	 * downloads a remote template
	 * @param authorizationString the key to authorize the operation
	 * @param id the id to identify the remote
	 * @param callback giving feedback of the result of the operation
	 */
	public static void downloadRemoteTemplate(final String authorizationString, final Long id, final SingleRemoteTemplateFetchCallback callback){


		RestAdapter restAdapter = new RestAdapter.Builder()
		.setEndpoint(endPoint)
		.setConverter(new GsonConverter(new Gson()))
		.setRequestInterceptor(new RequestInterceptor() {
			@Override
			public void intercept(RequestFacade request) {
				request.addHeader("Accept", "application/json;");
				request.addHeader("Authorization", authorizationString);

			}
		})
		.setLogLevel(LogLevel.FULL)
		.build();

		GeoCollectTemplateDownloadServices services = restAdapter.create(GeoCollectTemplateDownloadServices.class);
		services.getTemplate(id, new Callback<RemoteSingleResourceResponse>() {

			@Override
			public void success(RemoteSingleResourceResponse res, Response arg1) {
									
				callback.received(res.Resource);

			}

			@Override
			public void failure(RetrofitError error) {

				callback.error(error);
			}
		});
	}

	public interface RemoteTemplatesFetchCallback
	{
		public void templatesReceived(String authorizationString, ResourceList list);

		public void error(RetrofitError error);
	}

	public interface SingleRemoteTemplateFetchCallback
	{
		public void received(Resource res);

		public void error(RetrofitError error);
	}

}
