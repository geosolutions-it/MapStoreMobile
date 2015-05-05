package it.geosolutions.geocollect.android.core.login;

import it.geosolutions.android.map.geostore.model.ResourceList;
import it.geosolutions.geocollect.android.core.BuildConfig;
import it.geosolutions.geocollect.android.core.Config;
import it.geosolutions.geocollect.android.core.R;
import it.geosolutions.geocollect.android.core.login.utils.InstantAutoComplete;
import it.geosolutions.geocollect.android.core.login.utils.LoginRequestInterceptor;
import it.geosolutions.geocollect.android.core.login.utils.LoginUtil;
import it.geosolutions.geocollect.android.core.login.utils.LoginUtil.LoginStatusCallback;
import it.geosolutions.geocollect.android.core.login.utils.LoginUtil.UserDataStatusCallback;
import it.geosolutions.geocollect.android.core.login.utils.NetworkUtil;
import it.geosolutions.geocollect.android.core.login.utils.URLListPersistanceUtil;
import it.geosolutions.geocollect.android.core.mission.utils.MissionUtils;
import it.geosolutions.geocollect.android.core.mission.utils.PersistenceUtils;
import it.geosolutions.geocollect.android.core.mission.utils.SpatialiteUtils;
import it.geosolutions.geocollect.android.template.Resource;
import it.geosolutions.geocollect.android.template.TemplateDownloadTask;
import it.geosolutions.geocollect.android.template.TemplateDownloadTask.RemoteTemplatesFetchCallback;
import it.geosolutions.geocollect.android.template.TemplateDownloadTask.SingleRemoteTemplateFetchCallback;
import it.geosolutions.geocollect.model.config.MissionTemplate;

import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity {
	
	private final static String TAG = LoginActivity.class.getSimpleName();
	
	public final static String PREFS_USER_EMAIL = "it.geosolutions.geocollect.android.user_email";
	public final static String PREFS_PASSWORD = "it.geosolutions.geocollect.android.password";
	public final static String PREFS_USER_ID= "it.geosolutions.geocollect.android.user_id";
	public final static String PREFS_USER_FORENAME = "it.geosolutions.geocollect.android.user_forename";
	public final static String PREFS_USER_SURNAME = "it.geosolutions.geocollect.android.user_surname";
	public final static String PREFS_USER_ENTE = "it.geosolutions.geocollect.android.user_ente";
	public final static String PREFS_AUTH_KEY = "it.geosolutions.geocollect.android.auth_key";
	public final static String PREFS_LOGIN_URL = "it.geosolutions.geocollect.android.login_url";
	
	public final static int REQUEST_LOGIN = 111;
	
	// Values for email and password at the time of the login attempt.
	private String mEmail;
	private String mPassword;
	private String mSelectedUrl;

	// UI references.
	private InstantAutoComplete mAutoCompleteTextView;
	private EditText mEmailView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;
	
	private List<String> mUrls;
	
	private int arrived = 0;
    private ArrayList<MissionTemplate> downloads = new ArrayList<MissionTemplate>();
    /**
     * Spatialite Database for persistence
     */
    public jsqlite.Database spatialiteDatabase;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.login_layout);
	
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

    	mEmail    = prefs.getString(PREFS_USER_EMAIL, null);
    	mPassword = prefs.getString(PREFS_PASSWORD, null);
		
		
		mEmailView = (EditText) findViewById(R.id.email);
		
		if(mEmail != null && mEmailView != null){		
			mEmailView.setText(mEmail);
		}
		
		mAutoCompleteTextView = (InstantAutoComplete) findViewById(R.id.login_act);
		
		mUrls = URLListPersistanceUtil.load(getBaseContext());
		
		if(mUrls == null || mUrls.size() == 0){
			//provide initial server access point
			mUrls = new ArrayList<String>();
			mUrls.add(Config.MAIN_SERVER_BASE_URL+Config.OPENSDI_PATH);
		}
		
		// Creating adapter for spinner
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.server_spinner_item, mUrls);
		
		// Drop down layout style - list view with radio button
		adapter.setDropDownViewResource( R.layout.server_spinner_item);
		mAutoCompleteTextView.setAdapter(adapter);
		
        // Auto-set first value
        if(mUrls != null && mUrls.size() > 0){
            mAutoCompleteTextView.setText(mUrls.get(0));
        }

		mPasswordView = (EditText) findViewById(R.id.password);
		
		if(mPasswordView != null){
			
			// Enter saved password
			if(mPassword != null){
				mPasswordView.setText(mPassword);
			}
			
			mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView textView, int id,KeyEvent keyEvent) {
					if (id == R.id.login || id == EditorInfo.IME_NULL) {
						attemptLogin();
						return true;
					}
					return false;
				}
			});

			//if eMail provided but no pass set focus
			if(mEmail != null && mPassword == null){
				mPasswordView.requestFocus();
			}

		}

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.login_confirm).setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						//hide keyboard
						InputMethodManager imm = (InputMethodManager)getSystemService( Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(mPasswordView.getWindowToken(), 0);
						attemptLogin();
					}
				});
		
		findViewById(R.id.login_cancel).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//report cancel click 
				Intent returnIntent = new Intent();
				setResult(RESULT_CANCELED,returnIntent);
				finish();
			}
		});
		
		TextView forgotPasswordTextView = (TextView) findViewById(R.id.password_forgotten_tv);
		if(forgotPasswordTextView != null){
			forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
			
				@Override
				public void onClick(View v) {
					String url = getString(R.string.geocollect_retrieve_password_link);
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(url));
					startActivity(i);
					
				}
			});
		
		}
		
		TextView register_tv = (TextView) findViewById(R.id.register_tv);
		
		if(register_tv != null){
			
			register_tv.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					String url = getString(R.string.geocollect_register_link);
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(url));
					startActivity(i);
					
				}
			});
			
		}

		
	}
	
	/**
	 * Attempt Login
	 */
	private void attemptLogin() {
		
		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();
		mSelectedUrl = mAutoCompleteTextView.getText().toString();
		
		Log.d(TAG, "selected url "+ mSelectedUrl); 

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.login_error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.login_error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.login_error_field_required));
			focusView = mEmailView;
			cancel = true;
		}
		
		if(TextUtils.isEmpty(mSelectedUrl)){
			mAutoCompleteTextView.setError(getString(R.string.login_error_field_required));
			focusView = mAutoCompleteTextView;
			cancel = true;
		}
		
		//check if online
		if(!NetworkUtil.isOnline(getBaseContext())){
			
			Toast.makeText(getBaseContext(), getString(R.string.login_not_online), Toast.LENGTH_LONG).show();
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			if(focusView != null)focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true,false);
			
			LoginUtil.session(mSelectedUrl, mEmail, mPassword,new LoginStatusCallback() {
				
				@Override
				public void notLoggedIn(final RetrofitError error) {
					
					showProgress(false,true);

					if(error != null && error.isNetworkError()){
						Log.d(TAG, "Retrofit network error : "+error.getMessage());
						Toast.makeText(getBaseContext(), getString(R.string.login_error_generic) + " "+error.getMessage() , Toast.LENGTH_LONG).show();
					}else{						
						//credentials were most likely wrong
						mPasswordView.setError(getString(R.string.login_error_incorrect_password));
						mPasswordView.requestFocus();
					}					
				}
				
				@Override
				public void loggedIn(final String authKey) {
					
					//success, save credentials to prefs
					final Editor ed = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();
					ed.putString(PREFS_USER_EMAIL, mEmail);
					ed.putString(PREFS_PASSWORD, mPassword);
					ed.putString(PREFS_AUTH_KEY, authKey);
					ed.putString(PREFS_LOGIN_URL, mSelectedUrl);
					
					ed.commit();
					
					String authorizationString = LoginRequestInterceptor.getB64Auth(mEmail, mPassword);
					
					//save urls
					ArrayList<String> urls = new ArrayList<String>();
					//add the one that worked
					urls.add(mSelectedUrl);
					
					for(int i = 0; i < mUrls.size(); i++){
						//if is not yet included, add the others
						if(!mUrls.get(i).equals(mSelectedUrl)){
							urls.add(mUrls.get(i));
						}
					
					}
					URLListPersistanceUtil.save(getBaseContext(), urls);
					
					//auth key received, get userdata
					LoginUtil.user(getBaseContext(), mSelectedUrl, authKey, authorizationString, new UserDataStatusCallback() {
						
						@Override
						public void received(String authorizationString) {
						
							showProgress(false,false);
							
							Toast.makeText(getBaseContext(), getString(R.string.login_success), Toast.LENGTH_LONG).show(); 

							/////////////
						    TemplateDownloadTask.getRemoteTemplates(authorizationString, new RemoteTemplatesFetchCallback() {

						            @Override
						            public void templatesReceived(String authorizationString, ResourceList list) {

						                if(list != null && list.list.size() > 0){

						                    final int awaiting = list.list.size();
						                    //list worked using a "geostore" resource
						                    for(final it.geosolutions.android.map.geostore.model.Resource resource : list.list){

						                        TemplateDownloadTask.downloadRemoteTemplate(authorizationString, resource.id, new SingleRemoteTemplateFetchCallback (){

						                            @Override
						                            public void received(Resource res) {
						                                
						                                //to download a single template, a slightly different Resource was needed
						                                //TODO use geostore resource when server side has applied the according schema
						                                
						                                String templateString = res.getData().getData();

						                                downloads.add(MissionUtils.getTemplateFromJSON(templateString));
						                                
						                                arrived++;
						                                
						                                if(arrived == awaiting){
						                                    complete(downloads);
						                                }
						                            }

						                            @Override
						                            public void error(RetrofitError error) {

						                                Log.e(TAG, "error getting template "+resource.id+" : "+ error.getMessage());

						                                arrived++;
						                                
						                                if(arrived == awaiting){
						                                    complete(downloads);
						                                }
						                            }

						                        }); 
						                    }
						                }else{
						                    Log.e(TAG, "none or empty list received, cannot download templates");

				                            showProgress(false,false);
				                            
				                            Toast.makeText(getBaseContext(), "none or empty list received, cannot download templates", Toast.LENGTH_SHORT).show();
				                            
						                }
						            }

						            @Override
						            public void error(RetrofitError error) {

			                            showProgress(false,false);
			                            
			                            Toast.makeText(getBaseContext(), getString(R.string.login_error_generic) + " "+error.getMessage(), Toast.LENGTH_SHORT).show();
			                            
						                Log.e(TAG, "error getting template list : "+ error.getMessage());

						            }
						        });
							/////////////
							
							
						}
						
						@Override
						public void failed(final RetrofitError error) {
							
							showProgress(false,false);
							
							Toast.makeText(getBaseContext(), getString(R.string.login_error_generic) + " "+error.getMessage(), Toast.LENGTH_SHORT).show();
							
						}
					});		
				}
			});
			
		}
		
	}
	
	
	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show, final boolean showForm) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});
			if(showForm){
				mLoginFormView.setVisibility(View.VISIBLE);
				mLoginFormView.animate().setDuration(shortAnimTime)
				.alpha(show ? 0 : 1)
				.setListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						mLoginFormView.setVisibility(show ? View.GONE
								: View.VISIBLE);
					}
				});
			}else{
			    mLoginFormView.setVisibility(View.GONE);
            }
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
	
	
	public void complete(final ArrayList<MissionTemplate> downloadedTemplates) {

        /**
         * download successful, elaborate result
         **/
        // 1. update database
        ArrayList<MissionTemplate> validTemplates = new ArrayList<MissionTemplate>();
        if (downloadedTemplates != null && downloadedTemplates.size() > 0) {
            if (spatialiteDatabase == null) {

                spatialiteDatabase = SpatialiteUtils.openSpatialiteDB(getApplicationContext(), "geocollect/genova.sqlite");
            }
            for (MissionTemplate t : downloadedTemplates) {
                if (!PersistenceUtils.createOrUpdateTablesForTemplate(t,
                        spatialiteDatabase)) {
                    Log.w(TAG, "error creating/updating table");
                } else {
                    // if insert succesfull add to list of valid templates
                    validTemplates.add(t);
                }
            }
        }
        Log.d(TAG, "database updated");

        // 2. save valid templates
        PersistenceUtils.saveDownloadedTemplates(getBaseContext(), validTemplates);

        if(BuildConfig.DEBUG){
            Log.d(TAG, "valid templates persisted");
        }


        Intent returnIntent = new Intent();
        setResult(RESULT_OK,returnIntent);
                
                
        finish();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        if(mEmailView != null){
            mEmailView.requestFocus();
        }
    }
}
