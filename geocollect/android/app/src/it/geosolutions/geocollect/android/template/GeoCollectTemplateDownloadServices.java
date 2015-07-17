package it.geosolutions.geocollect.android.template;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
/**
 * Remote templates download services
 * 
 * @author Robert Oehler
 *
 */
public interface GeoCollectTemplateDownloadServices {
		
		

		/**
		 * request remote template list
		 */
		@GET("/resources")
		public void getTemplates(Callback<RemoteTemplateListResponse> cb);
	
		/**
		 * request a remote template
		 */
		@GET("/resources/resource/{id}/?full=true")
		public void getTemplate(@Path("id") Long id, Callback<RemoteSingleResourceResponse> cb);
	
}
