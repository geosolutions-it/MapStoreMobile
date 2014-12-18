package it.geosolutions.geocollect.android.core.login.utils;


import it.geosolutions.geocollect.android.core.BuildConfig;
import it.geosolutions.geocollect.android.core.login.GeoCollectLoginServices;
import it.geosolutions.geocollect.android.core.login.LoginActivity;
import it.geosolutions.geocollect.android.core.login.UserDataResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RestAdapter.LogLevel;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
/**
 * class which contains the logic to login the user using Retrofit
 * 
 * @author Robert Oehler
 *
 */
public class LoginUtil {
	
	private final static String TAG = LoginUtil.class.getSimpleName();
	
	/**
	 * checks the login state of the user using RETROFIT
	 * user the provided params @param pUsername and @param pPassword
	 * if the @param pCallback provided it will report the result to the calling object
	 */
	public static void session(final String pUrl, final String pUsername,final String pPassword,final LoginStatusCallback pCallback){

		
		Gson gson = GsonUtil.createFeatureGson();
		RestAdapter restAdapter = new RestAdapter.Builder()
		.setEndpoint(pUrl)
		.setRequestInterceptor(new LoginRequestInterceptor(pUsername, pPassword)) //set the interceptor who inserts user:pass 
		.setConverter(new GsonConverter(gson))
		//.setLogLevel(LogLevel.FULL)
		.build();

		GeoCollectLoginServices geoCollectService = restAdapter.create(GeoCollectLoginServices.class);
		geoCollectService.session(new Callback<Response>() {

			@Override
			public void success(final Response result, final Response response) {

				//Try to get response body
		        BufferedReader reader = null;
		        StringBuilder sb = new StringBuilder();
		        try {

		        	reader = new BufferedReader(new InputStreamReader(result.getBody().in()));

		        	String line;

		        	while ((line = reader.readLine()) != null) {
		        		sb.append(line);
		        	}

		        } catch (IOException e) {
		        	Log.e(TAG, "parse error ",e);
		        }

		        String _result = sb.toString();
		        if(BuildConfig.DEBUG){
		        	
		        	Log.d(TAG, "session returned successfully "+_result);
		        }
				
				pCallback.loggedIn(_result);
				
				return;

			}

			@Override
			public void failure(RetrofitError error) {

				if(BuildConfig.DEBUG){
					Log.e(TAG, "login error "+error.getMessage());
				}

				pCallback.notLoggedIn(error);

			}
		});
	}
	
	public static void user(final Context pContext, final String pUrl, final String pAuthKey, final UserDataStatusCallback pCallback){
		
		Gson gson = GsonUtil.createFeatureGson();
		RestAdapter restAdapter = new RestAdapter.Builder()
		.setEndpoint(pUrl)
		.setConverter(new GsonConverter(gson))
//		.setLogLevel(LogLevel.FULL)
		.build();

		GeoCollectLoginServices geoCollectService = restAdapter.create(GeoCollectLoginServices.class);
		geoCollectService.user(pAuthKey, new Callback<UserDataResponse>() {
			
			@Override
			public void success(UserDataResponse udr, Response arg1) {
				
				if(BuildConfig.DEBUG){		        	
		        	Log.d(TAG, "user returned successfully ");
		        }
				
				//example properties of the UserDataResponse
				final String password = udr.password;
				final String username = udr.username;
				
				final Editor ed = PreferenceManager.getDefaultSharedPreferences(pContext).edit();
				
				//TODO enter real values
				ed.putString(LoginActivity.PREFS_USER_SURNAME, username);
				ed.putString(LoginActivity.PREFS_USER_FORENAME, "Antonio");
				ed.putString(LoginActivity.PREFS_USER_ENTE, "Comune di Genova");
				ed.commit();
				
				pCallback.received();
				
			}
			
			@Override
			public void failure(RetrofitError error) {
				
				if(BuildConfig.DEBUG){
					Log.e(TAG, "login error "+error.getMessage());
				}
				pCallback.failed(error);
			}
		});
		
	}
	
	public interface LoginStatusCallback {

		public void loggedIn(final String authKey);

		public void notLoggedIn(final RetrofitError error);
	}
	public interface UserDataStatusCallback{
		
		public void received();
		
		public void failed(final RetrofitError error);
	}

}
