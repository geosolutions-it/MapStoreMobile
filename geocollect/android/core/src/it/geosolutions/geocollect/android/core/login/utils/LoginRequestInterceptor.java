package it.geosolutions.geocollect.android.core.login.utils;

import retrofit.RequestInterceptor;
import android.util.Base64;
/**
 * class to intercept the retrofit login PUT request with the user credentials
 * @author Robert Oehler
 *
 */
public class LoginRequestInterceptor implements RequestInterceptor {

	private String mUser;
	private String mPass;

	public LoginRequestInterceptor(final String pUser,final String pPass){
		this.mUser = pUser;
		this.mPass = pPass;
	}

	@Override
	public void intercept(RequestFacade requestFacade) {

		if (mUser != null && mPass != null) {
			final String authorizationValue = getB64Auth(mUser, mPass);
			requestFacade.addHeader("Authorization", authorizationValue);
		}else{
			throw new IllegalArgumentException("no password or user available to intercept");	
		}
	}

    public static String getB64Auth( String login, String pass ) {
        String source = login + ":" + pass;
        String ret = "Basic " + Base64.encodeToString(source.getBytes(), Base64.URL_SAFE | Base64.NO_WRAP);
        return ret;
    }

}
