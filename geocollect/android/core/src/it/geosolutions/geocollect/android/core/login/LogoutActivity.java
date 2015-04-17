package it.geosolutions.geocollect.android.core.login;

import it.geosolutions.geocollect.android.core.R;
import it.geosolutions.geocollect.android.core.login.utils.LoginUtil;
import it.geosolutions.geocollect.android.core.login.utils.LoginUtil.LoginStatusCallback;
import it.geosolutions.geocollect.android.core.login.utils.NetworkUtil;
import retrofit.RetrofitError;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class LogoutActivity extends Activity {

	private static final String TAG = LogoutActivity.class.getSimpleName();
	
	private View mLogoutFormView;
	private View mLogoutStatusView;
	
	public final static int REQUEST_LOGOUT = 222;
	public final static int LOGGED_OUT = 333;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.logout_layout);

		mLogoutFormView = findViewById(R.id.logout_form);
		mLogoutStatusView = findViewById(R.id.logout_status);
		
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		final String email = prefs.getString(LoginActivity.PREFS_USER_EMAIL, null);
		final String pass  = prefs.getString(LoginActivity.PREFS_PASSWORD, null);
		final String url  = prefs.getString(LoginActivity.PREFS_LOGIN_URL, null);


		// check online
		if(!NetworkUtil.isOnline(getBaseContext())){

			mLogoutFormView.setVisibility(View.VISIBLE);
			//if not online, see if values saved and fill
			tryToFillFormWithSavedData();

		}else if(email != null && pass != null && url != null){
			
			//login user to get credential data
			showProgress(true, false);
			
			LoginUtil.session(url, email, pass,new LoginStatusCallback() {
				
				@Override
				public void notLoggedIn(RetrofitError error) {
					
					//user is not logged in or some error occured, login again
					startLoginActivity();
				}
				
				@Override
				public void loggedIn(final String authKey) {
					
					showProgress(false,true);

					setProps(email,null,null);
					
				}
			});
		}else{
			//missing data 
			startLoginActivity();
		}
		
		final Button logoutButton = (Button) findViewById(R.id.logout_button);

		logoutButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				//clear user data
				final Editor ed = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();

				ed.putString(LoginActivity.PREFS_USER_EMAIL, null);
				ed.putString(LoginActivity.PREFS_USER_FORENAME, null);
				ed.putString(LoginActivity.PREFS_USER_SURNAME, null);
				ed.putString(LoginActivity.PREFS_PASSWORD, null);
				ed.putString(LoginActivity.PREFS_AUTH_KEY, null);
				ed.putString(LoginActivity.PREFS_USER_ENTE, null);

				ed.commit();

				Toast.makeText(getBaseContext(), getString(R.string.logout_logged_out),Toast.LENGTH_LONG).show();
				
				Intent returnIntent = new Intent();
				setResult(LOGGED_OUT,returnIntent);

				finish();

			}
		});
		
	}

	private void tryToFillFormWithSavedData() {
		
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

		final String _user = prefs.getString(LoginActivity.PREFS_USER_EMAIL, null);
		final String _username_sur = prefs.getString(LoginActivity.PREFS_USER_SURNAME, null);
		final String _username_fore = prefs.getString(LoginActivity.PREFS_USER_FORENAME, null);
		final String _ente = prefs.getString(LoginActivity.PREFS_USER_ENTE, null);
		
		final String _username = _username_fore + " "+ _username_sur; 

		if(_user != null && _username != null){

			setProps(_user, _username, _ente);

		}else{

			//user is probably not logged in and offline
			//TODO is it possible to use the app offline and logged out ?
			startLoginActivity();
		}
		
	}
	
	
	public void setProps(final String user, final String userName, final String ente){


		final TextView email_TV = (TextView) findViewById(R.id.logout_email_tv);
		final TextView name_TV  = (TextView) findViewById(R.id.logout_username_tv);
		final TextView ente_TV = (TextView) findViewById(R.id.logout_ente_tv);

		if(user != null){
			email_TV.setText(user);
		}
		if(userName != null){			
			name_TV.setText(userName);
		}
		if(ente != null){			
			ente_TV.setText(ente);
		}
	}
	
	/**
	 * Shows the progress UI and hides the account details form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show, final boolean showForm) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

			mLogoutStatusView.setVisibility(View.VISIBLE);
			mLogoutStatusView.animate().setDuration(shortAnimTime)
			.alpha(show ? 1 : 0)
			.setListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					mLogoutStatusView.setVisibility(show ? View.VISIBLE	: View.GONE);
				}
			});

			if(showForm){
				mLogoutFormView.setVisibility(View.VISIBLE);
				mLogoutFormView.animate().setDuration(shortAnimTime)
				.alpha(show ? 0 : 1)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						mLogoutFormView.setVisibility(show ? View.GONE: View.VISIBLE);
					}
				});
			}else{
			    mLogoutFormView.setVisibility(View.GONE);
			}
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLogoutStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLogoutFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
	
	public void startLoginActivity(){

		showProgress(false,false);

		startActivity(new Intent(LogoutActivity.this, LoginActivity.class));

		finish();
	}

}
