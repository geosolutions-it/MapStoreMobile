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
			final String authorizationValue = encodeCredentialsForBasicAuthorization();
			requestFacade.addHeader("Authorization", authorizationValue);
		}else{
			throw new IllegalArgumentException("no password or user available to intercept");	
		}
	}

	private String encodeCredentialsForBasicAuthorization() {
		final String userAndPassword = mUser + ":" + mPass;
		return "Basic " + Base64.encodeToString(userAndPassword.getBytes(), Base64.NO_WRAP);
	}

}
