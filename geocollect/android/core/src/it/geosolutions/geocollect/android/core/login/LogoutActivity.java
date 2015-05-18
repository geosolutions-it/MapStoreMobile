package it.geosolutions.geocollect.android.core.login;

import it.geosolutions.android.map.utils.MapFilesProvider;
import it.geosolutions.android.map.utils.ZipFileManager;
import it.geosolutions.geocollect.android.core.BuildConfig;
import it.geosolutions.geocollect.android.core.GeoCollectApplication;
import it.geosolutions.geocollect.android.core.R;
import it.geosolutions.geocollect.android.core.login.utils.LoginRequestInterceptor;
import it.geosolutions.geocollect.android.core.login.utils.LoginUtil;
import it.geosolutions.geocollect.android.core.login.utils.LoginUtil.LoginStatusCallback;
import it.geosolutions.geocollect.android.core.login.utils.NetworkUtil;
import it.geosolutions.geocollect.android.core.mission.PendingMissionListActivity;
import it.geosolutions.geocollect.android.core.mission.utils.MissionUtils;
import it.geosolutions.geocollect.android.core.mission.utils.PersistenceUtils;
import it.geosolutions.geocollect.android.core.mission.utils.SpatialiteUtils;
import it.geosolutions.geocollect.android.template.TemplateDownloadTask;
import it.geosolutions.geocollect.model.config.MissionTemplate;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit.RetrofitError;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
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
		
		final ArrayList<MissionTemplate> templates = PersistenceUtils.loadSavedTemplates(getBaseContext());
	
		
		final HashMap <MissionTemplate,Boolean> downloads = new HashMap<MissionTemplate,Boolean>();
		
        if(templates != null){
            for(MissionTemplate t : templates){
                
                boolean exists = MissionUtils.checkTemplateForBackgroundData(getBaseContext(), t);
                
                downloads.put(t, exists);
                
                if(BuildConfig.DEBUG){
                    Log.i(TAG,"adding to downloads "+t.title+" , id "+t.id +" exists "+Boolean.toString(exists));
                }
            }
        }
		
		final ListView missionListview = (ListView) findViewById(R.id.mission_list);
		
		final MissionDownloadItemAdapter downloadsAdapter = new MissionDownloadItemAdapter(getBaseContext(), downloads);
		missionListview.setAdapter(downloadsAdapter);
		
		final ProgressBar progress = (ProgressBar) findViewById(R.id.update_missions_progress);
		
		final ImageView updateTemplatesButton = (ImageView) findViewById(R.id.update_missions_button);
		updateTemplatesButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				updateTemplatesButton.setVisibility(View.GONE);
				progress.setVisibility(View.VISIBLE);
				
				//update MissionTemplates
				
				  final TemplateDownloadTask task = new TemplateDownloadTask() {
		                @Override
		                public void complete(final ArrayList<MissionTemplate> downloadedTemplates) {
		                	
		                	updateTemplatesButton.setVisibility(View.VISIBLE);
		    				progress.setVisibility(View.GONE);

		                    /**
		                     * download successful, elaborate result
		                     **/
		                    // 1. update database
		                    ArrayList<MissionTemplate> validTemplates = new ArrayList<MissionTemplate>();
		                    if (downloadedTemplates != null && downloadedTemplates.size() > 0) {
		                        
		                    	jsqlite.Database spatialiteDatabase = SpatialiteUtils.openSpatialiteDB(
		                                    LogoutActivity.this, "geocollect/genova.sqlite");
		                        
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

		                    // 2. save valid templates
		                    PersistenceUtils.saveDownloadedTemplates(getBaseContext(), validTemplates);

		                    if(BuildConfig.DEBUG){
		                        Log.d(TAG, "valid templates persisted");
		                    }
		                    
		                    // 3. Update the currently selected template
		                    Editor ed = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();
		                    
		                    ArrayList<MissionTemplate> loadedTemplates = PersistenceUtils.loadSavedTemplates(getBaseContext());
		                    int templateIndex = 0;
		                    String selectedTemplateId = prefs.getString(PendingMissionListActivity.PREFS_SELECTED_TEMPLATE_ID, null);
		                    if(selectedTemplateId != null && !selectedTemplateId.isEmpty()){
    		                    for(MissionTemplate t : loadedTemplates){
    		                        if(t.id != null && t.id.equalsIgnoreCase(selectedTemplateId)){
    		                            
    		                            ((GeoCollectApplication) getApplication()).setTemplate(t);
    		                            ed.putInt(PendingMissionListActivity.PREFS_DOWNLOADED_TEMPLATE_INDEX, templateIndex);
    		                            
    		                        }
    		                        templateIndex++;
    		                    }
		                    }
		                    
		                    ed.commit();
		                }
		            };
		            
		            final String authKey = prefs.getString(LoginActivity.PREFS_AUTH_KEY, null);
		            String username = prefs.getString(LoginActivity.PREFS_USER_EMAIL, null);
		            String password = prefs.getString(LoginActivity.PREFS_PASSWORD, null);

		            String authorizationString = LoginRequestInterceptor.getB64Auth(username, password);
		            
		            task.execute(authKey, authorizationString);
				
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
	
	public class MissionDownloadItemAdapter extends BaseAdapter {

		private Context mContext;
		private HashMap<MissionTemplate, Boolean> mDownloads = new HashMap<MissionTemplate, Boolean>();
		private MissionTemplate[] mKeys;

		public MissionDownloadItemAdapter(Context context, HashMap<MissionTemplate,Boolean> items) {
			
		   mContext = context;
		   mDownloads = items;
		   mKeys = mDownloads.keySet().toArray(new MissionTemplate[mDownloads.size()]);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View v = convertView;

			if (v == null) {

				LayoutInflater vi;
				vi = LayoutInflater.from(mContext);
				v = vi.inflate(R.layout.download_mission_item, null);

			}

			final TextView titleTV = (TextView) v.findViewById(R.id.download_mission_title);
			final ImageView imageV = (ImageView) v.findViewById(R.id.download_mission_image);
			
			final MissionTemplate t = mKeys[position];
			final boolean exists = mDownloads.get(t);

			titleTV.setText(t.title);

			//remove for reused views;
			v.setOnClickListener(null);
			
			if(exists){
				imageV.setImageResource(R.drawable.ic_navigation_accept);
			}else{
				v.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {

						final String mount   = MapFilesProvider.getEnvironmentDirPath(getBaseContext());
						final HashMap<String,Integer> urls = MissionUtils.getContentUrlsAndFileAmountForTemplate(t);
						Resources res = getResources();
						for(String url : urls.keySet()){

						    String dialogMessage = res.getQuantityString(R.plurals.dialog_message_with_amount, urls.get(url), urls.get(url));
							new ZipFileManager(LogoutActivity.this, mount, MapFilesProvider.getBaseDir(), url, null, dialogMessage ) {
								@Override
								public void launchMainActivity(final boolean success) {

//									//TODO necessary ?
//									if (getApplication() instanceof GeoCollectApplication) {
//										((GeoCollectApplication) getApplication()).setupMBTilesBackgroundConfiguration();
//									}
									
									if(success){
										
										Toast.makeText(getBaseContext(), getString(R.string.download_successfull), Toast.LENGTH_SHORT).show();	
										
										mDownloads.remove(t);
										mDownloads.put(t, true);
										
										notifyDataSetChanged();
									}								
								}
							};
						}

					}
					
				});
			}

			return v;
		}
		
	    @Override
	    public int getCount() {
	        return mDownloads.size();
	    }

	    @Override
	    public Object getItem(int position) {
	        return mDownloads.get(mKeys[position]);
	    }

	    @Override
	    public long getItemId(int arg0) {
	        return arg0;
	    }
	}

}
