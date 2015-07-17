package it.geosolutions.geocollect.android.core.login;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.PUT;
import retrofit.http.Path;

public interface GeoCollectLoginServices {
	
	
	/**
	 * request the current user state
	 * @param the callback
	 */
	@PUT("/session/")
	public void session(Callback<Response> cb);

	/**
	 * request user details
	 */
	@GET("/session/user/{auth_key}")
	public void user(@Path("auth_key") String auth_key, Callback<UserDataResponse> cb);
}
