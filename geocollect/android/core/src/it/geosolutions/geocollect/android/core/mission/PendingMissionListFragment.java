/*
 * GeoSolutions - MapstoreMobile - GeoSpatial Framework on Android based devices
 * Copyright (C) 2014  GeoSolutions (www.geo-solutions.it)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.geosolutions.geocollect.android.core.mission;

import it.geosolutions.android.map.model.query.BBoxQuery;
import it.geosolutions.android.map.model.query.BaseFeatureInfoQuery;
import it.geosolutions.android.map.utils.MapFilesProvider;
import it.geosolutions.android.map.utils.ZipFileManager;
import it.geosolutions.android.map.wfs.geojson.GeoJson;
import it.geosolutions.geocollect.android.core.BuildConfig;
import it.geosolutions.geocollect.android.core.GeoCollectApplication;
import it.geosolutions.geocollect.android.core.R;
import it.geosolutions.geocollect.android.core.form.FormEditActivity;
import it.geosolutions.geocollect.android.core.form.utils.FormUtils;
import it.geosolutions.geocollect.android.core.login.LoginActivity;
import it.geosolutions.geocollect.android.core.login.utils.LoginRequestInterceptor;
import it.geosolutions.geocollect.android.core.login.utils.NetworkUtil;
import it.geosolutions.geocollect.android.core.mission.utils.MissionUtils;
import it.geosolutions.geocollect.android.core.mission.utils.PersistenceUtils;
import it.geosolutions.geocollect.android.core.mission.utils.SQLiteCascadeFeatureLoader;
import it.geosolutions.geocollect.android.core.widgets.dialog.UploadDialog;
import it.geosolutions.geocollect.model.config.MissionTemplate;
import it.geosolutions.geocollect.model.http.CommitResponse;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jsqlite.Database;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;

/**
 * A list fragment representing a list of Pending Missions. This fragment also supports tablet devices by allowing list items to be given an
 * 'activated' state upon selection. This helps indicate which item is currently being viewed in a {@link PendingMissionDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks} interface.
 */
public class PendingMissionListFragment extends SherlockListFragment implements LoaderCallbacks<List<MissionFeature>>,
        OnScrollListener, OnRefreshListener, OnQueryTextListener {

    /**
     * The serialization (saved instance state) Bundle key representing the activated item position. Only used on tablets.
     */
    private static final String STATE_ACTIVATED_POSITION = "activated_position";

    /**
     * Fragment upload
     */
    private static final String FRAGMENT_UPLOAD_DIALOG = "FRAGMENT_UPLOAD_DIALOG";

    private static int CURRENT_LOADER_INDEX = 0;

    private static final String TAG = "MISSION_LIST";

    public static final String INFINITE_SCROLL = "INFINITE_SCROLL";

    public static int ARG_ENABLE_GPS = 43231;

    public static int RESET_MISSION_FEATURE_ID = 12345;

    /**
     * mode of this fragment
     */
    public enum FragmentMode {
        PENDING, CREATION
    }

    private FragmentMode mMode = FragmentMode.PENDING;

    /**
     * The fragment's current callback object, which is notified of list item clicks.
     */
    private Callbacks listSelectionCallbacks = sDummyCallbacks;

    /**
     * The current activated item position. Only used on tablets.
     */
    private int mActivatedPosition = ListView.INVALID_POSITION;

    /**
     * The adapter for the Feature
     */
    private FeatureAdapter adapter;

    private CreatedMissionAdapter missionAdapter;

    /**
     * Callback for the Loader
     */
    private LoaderCallbacks<List<MissionFeature>> mCallbacks;

    /**
     * SwipeRefreshLayout, use by the swipeDown gesture
     */
    ListFragmentSwipeRefreshLayout mSwipeRefreshLayout;

    /**
     * Boolean value that allow to start loading only once at time
     */
    private boolean isLoading;

    /**
     * The template that provides the form and the
     */
    private MissionTemplate missionTemplate;

    /**
     * page number for remote queries
     */
    private int page = 0;

    /**
     * page size for remote queries
     */
    private int pagesize = 250;

    private View footer;

    /**
     * Main Activity's jsqlite Database instance reference
     */
    private Database db;

    /**
     * A callback interface that all activities containing this fragment must implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callbacks {
        /**
         * Callback for when an item has been selected.
         */
        public void onItemSelected(Object object);
    }

    /**
     * A dummy implementation of the {@link Callbacks} interface that does nothing. Used only when this fragment is not attached to an activity.
     */
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(Object object) {
        }
    };

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon screen orientation changes).
     */
    public PendingMissionListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("MISSION_LIST_FRAGMENT", "onCreate()");

        setRetainInstance(true);
        // setup the listView
        missionTemplate = MissionUtils.getDefaultTemplate(getSherlockActivity());

        ((GeoCollectApplication) getActivity().getApplication()).setTemplate(missionTemplate);
        
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean usesDownloaded = prefs.getBoolean(PendingMissionListActivity.PREFS_USES_DOWNLOADED_TEMPLATE, false);
        if (usesDownloaded) {
            int index = prefs.getInt(PendingMissionListActivity.PREFS_DOWNLOADED_TEMPLATE_INDEX, 0) + 1;
            CURRENT_LOADER_INDEX = missionTemplate.getLoaderIndex();
        }

        adapter = new FeatureAdapter(getSherlockActivity(), R.layout.mission_resource_row, missionTemplate);
        setListAdapter(adapter);
        // option menu
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        footer = View.inflate(getActivity(), R.layout.loading_footer, null);

        // Get the list fragment's content view
        final View listFragmentView = inflater.inflate(R.layout.mission_resource_list, container, false);

        // Now create a SwipeRefreshLayout to wrap the fragment's content view
        mSwipeRefreshLayout = new ListFragmentSwipeRefreshLayout(getSherlockActivity());

        // Add the list fragment's content view to the SwipeRefreshLayout, making sure that it fills
        // the SwipeRefreshLayout
        mSwipeRefreshLayout.addView(listFragmentView, ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        // Make sure that the SwipeRefreshLayout will fill the fragment
        mSwipeRefreshLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        mSwipeRefreshLayout.setColorScheme(R.color.geosol_1, R.color.geosol_2, R.color.geosol_3, R.color.geosol_4);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        // Now return the SwipeRefreshLayout as this fragment's content view
        return mSwipeRefreshLayout;

        // return inflater.inflate(R.layout.mission_resource_list, container, false);
    }

    /**
     * hide loading bar and set loading task
     */
    private void stopLoadingGUI() {
        if (getSherlockActivity() != null) {
            getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
            getSherlockActivity().setSupportProgressBarVisibility(false);
            // getListView().removeFooterView(footer);
            //Log.v(TAG, "task terminated");

        }
        adapter.notifyDataSetChanged();
        isLoading = false;
        if (mSwipeRefreshLayout != null){
            mSwipeRefreshLayout.setRefreshing(false);
        }

    }

    /**
     * Sets the view to show that no data are available
     */
    private void setNoData() {
        ((TextView) getView().findViewById(R.id.empty_text)).setText(R.string.no_reporting_found);
        getView().findViewById(R.id.progress_bar).setVisibility(TextView.GONE);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getActivity().getIntent().getBooleanExtra(INFINITE_SCROLL, false)) {
            getListView().setOnScrollListener(this);
        } else {
            getListView().setOnScrollListener(new OnScrollListener() {

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    // Log.v("PMLF", "First: "+ firstVisibleItem + ", count: "+visibleItemCount+ ", total: "+totalItemCount);
                    if (firstVisibleItem == 0 || visibleItemCount == 0) {
                        mSwipeRefreshLayout.setEnabled(true);
                    } else {
                        mSwipeRefreshLayout.setEnabled(false);
                    }

                }
            });
        }

        // Restore the previously serialized activated item position.
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }

        MissionUtils.checkMapStyles(getResources(), missionTemplate);
        
        startDataLoading(missionTemplate, CURRENT_LOADER_INDEX);

        registerForContextMenu(getListView());

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Log.v("MISSION_LIST_FRAGMENT", "onAttach()");
        // Activities containing this fragment must implement its callbacks.
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }

        // If a previous instance of the database was attached, the loader must be restarted
        boolean needReload = false;
        if (db != null && db.dbversion().equals("unknown")) {
            needReload = true;
        }

        // Check for a database
        if (getSherlockActivity() instanceof PendingMissionListActivity) {
            Log.v(TAG, "Loader: Connecting to Activity database");
            db = ((PendingMissionListActivity) getSherlockActivity()).spatialiteDatabase;
            // restart the loader if needed
            if (needReload) {
                LoaderManager lm = getSherlockActivity().getSupportLoaderManager();
                if (lm.getLoader(CURRENT_LOADER_INDEX) != null) {
                    lm.restartLoader(CURRENT_LOADER_INDEX, null, this);
                }
            }
        } else {
            Log.w(TAG, "Loader: Could not connect to Activity database");
        }

        listSelectionCallbacks = (Callbacks) activity;

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

        // this context menu is only valid for "segnalazioni", for "new segnalazioni" "missionadapter"s longlicklistener is used
        // TODO unify both adapters and use either one method for long clicks

        // if this item is editable or uploadable, offer the possibility to "reset" the state -> delete its "sop" entry
        if (v.getId() == getListView().getId()) {
            ListView lv = (ListView) v;

            final MissionTemplate template = MissionUtils.getDefaultTemplate(getActivity());
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            MissionFeature feature = (MissionFeature) lv.getItemAtPosition(info.position);

            // identify edited/uploadable
            if (feature.editing) {
                // create a dialog to let the user clear this surveys data
                String title = getString(R.string.survey);
                if (feature.properties != null && feature.properties.get(template.nameField) != null) {
                    title = (String) feature.properties.get(template.nameField);
                }
                if (title != null) {
                    menu.setHeaderTitle(title);
                }
                menu.add(0, RESET_MISSION_FEATURE_ID, 0, getString(R.string.menu_clear_survey));
            }
        }
    }

    @Override
    public boolean onContextItemSelected(android.view.MenuItem item) {

        // user selected the option to reset, delete the edited item
        if (item.getItemId() == RESET_MISSION_FEATURE_ID) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

            MissionFeature feature = (MissionFeature) getListView().getItemAtPosition(info.position);
            final MissionTemplate template = MissionUtils.getDefaultTemplate(getActivity());

            String title = (String) feature.properties.get(template.nameField);

            Log.d(TAG, "missionfeature " + title + " selected to reset");

            String tableName = template.schema_sop.localFormStore;
            // delete db entry
            PersistenceUtils.deleteMissionFeature(db, tableName, MissionUtils.getFeatureGCID(feature));

            // if this entry was uploadable, remove it from the list of uploadables
            HashMap<String, ArrayList<String>> uploadables = PersistenceUtils.loadUploadables(getSherlockActivity());
            final String id = MissionUtils.getFeatureGCID(feature);
            if (uploadables.containsKey(tableName) && uploadables.get(tableName).contains(id)) {
                uploadables.get(tableName).remove(id);
                PersistenceUtils.saveUploadables(getSherlockActivity(), uploadables);
            }

            // reload list
            adapter.clear();
            getSherlockActivity().getSupportLoaderManager().getLoader(CURRENT_LOADER_INDEX).forceLoad();
            getSherlockActivity().supportInvalidateOptionsMenu();

            return true;
        }
        return super.onContextItemSelected(item);

    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset the active callbacks interface to the dummy implementation.
        listSelectionCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);

        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        listSelectionCallbacks.onItemSelected(getListAdapter().getItem(position));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != ListView.INVALID_POSITION) {
            // Serialize and persist the activated item position.
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    /**
     * Turns on activate-on-click mode. When this mode is on, list items will be given the 'activated' state when touched.
     */
    public void setActivateOnItemClick(boolean activateOnItemClick) {
        // When setting CHOICE_MODE_SINGLE, ListView will automatically
        // give items the 'activated' state when touched.
        getListView().setChoiceMode(activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE);
    }

    private void setActivatedPosition(int position) {
        if (position == ListView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    /**
     * Creates the actionBar buttons
     */
    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {

        // upload
        if (missionTemplate != null && missionTemplate.schema_sop != null
                && missionTemplate.schema_sop.localFormStore != null) {

            String tableName = mMode == FragmentMode.CREATION ? missionTemplate.schema_seg.localSourceStore + MissionTemplate.NEW_NOTICE_SUFFIX
                    : missionTemplate.schema_sop.localFormStore;
            HashMap<String, ArrayList<String>> uploadables = PersistenceUtils.loadUploadables(getSherlockActivity());
            if (uploadables.containsKey(tableName) && uploadables.get(tableName).size() > 0) {
                // there are uploadable entries, add a menu item
                inflater.inflate(R.menu.uploadable, menu);
            }
        }

        if (mMode == FragmentMode.CREATION) {
            inflater.inflate(R.menu.creating, menu);
            return;
        }

        // If SRID is set, a filter exists
        SharedPreferences sp = getSherlockActivity().getSharedPreferences(SQLiteCascadeFeatureLoader.PREF_NAME,
                Context.MODE_PRIVATE);
        if (sp.contains(SQLiteCascadeFeatureLoader.FILTER_SRID)) {
            inflater.inflate(R.menu.filterable, menu);
        }

        inflater.inflate(R.menu.searchable, menu);

        // get searchview and add querylistener

        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setQueryHint(getString(R.string.search_missions));
        searchView.setOnQueryTextListener(this);
        searchView.setOnQueryTextFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (!hasFocus) {
                    // keyboard was closed, collapse search action view
                    menu.findItem(R.id.search).collapseActionView();
                }
            }
        });

        if (missionTemplate != null) {
            if (missionTemplate.schema_seg != null) {
                inflater.inflate(R.menu.creating, menu);
            }
        }

        inflater.inflate(R.menu.map_full, menu);

        if (missionTemplate != null) {
            if (missionTemplate.schema_sop != null && missionTemplate.schema_sop.orderingField != null) {
                inflater.inflate(R.menu.orderable, menu);
                MenuItem orderButton = menu.findItem(R.id.order);
                if (orderButton != null) {
                    String stringFormat = getResources().getString(R.string.order_by);
                    String formattedTitle = String.format(stringFormat, missionTemplate.schema_sop.orderingField);
                    orderButton.setTitle(formattedTitle);
                }
            }

        }
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return query.length() > 0;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        if (adapter != null) {
            // filters the adapters entries using its overridden filter
            adapter.getFilter().filter(newText);
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.actionbarsherlock.app.SherlockFragment#onOptionsItemSelected(com.actionbarsherlock.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.create_new) {

            if (!isGPSAvailable()) {

                new AlertDialog.Builder(getActivity()).setTitle(R.string.app_name).setMessage(R.string.gps_not_enabled)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                startActivityForResult(new Intent(
                                        android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), ARG_ENABLE_GPS);
                            }
                        }).show();
            } else {

                startMissionFeatureCreation();
            }

            return true;

        } else if (id == R.id.order) {

            // Get it from the mission template
            if (getSherlockActivity().getSupportLoaderManager().getLoader(CURRENT_LOADER_INDEX) != null) {

                SharedPreferences sp = getSherlockActivity().getSharedPreferences(SQLiteCascadeFeatureLoader.PREF_NAME,
                        Context.MODE_PRIVATE);
                boolean reverse = sp.getBoolean(SQLiteCascadeFeatureLoader.REVERSE_ORDER_PREF, false);
                SharedPreferences.Editor editor = sp.edit();
                // Change the ordering
                Log.v(TAG, "Changing to " + reverse);
                editor.putBoolean(SQLiteCascadeFeatureLoader.REVERSE_ORDER_PREF, !reverse);
                editor.commit();

                adapter.clear();
                getSherlockActivity().getSupportLoaderManager().getLoader(CURRENT_LOADER_INDEX).forceLoad();
            }
            return true;

        } else if (id == R.id.filter) {

            // Clear the Spatial Filter
            if (getSherlockActivity().getSupportLoaderManager().getLoader(CURRENT_LOADER_INDEX) != null) {

                SharedPreferences sp = getSherlockActivity().getSharedPreferences(SQLiteCascadeFeatureLoader.PREF_NAME,
                        Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                // Change the ordering
                editor.remove(SQLiteCascadeFeatureLoader.FILTER_N);
                editor.remove(SQLiteCascadeFeatureLoader.FILTER_S);
                editor.remove(SQLiteCascadeFeatureLoader.FILTER_W);
                editor.remove(SQLiteCascadeFeatureLoader.FILTER_E);
                editor.remove(SQLiteCascadeFeatureLoader.FILTER_SRID);
                editor.commit();

                adapter.clear();
                getSherlockActivity().getSupportLoaderManager().getLoader(CURRENT_LOADER_INDEX).forceLoad();
                item.setVisible(false);
            }
            return true;
        } else if (id == R.id.upload) {

            if (!NetworkUtil.isOnline(getSherlockActivity())) {
                Toast.makeText(getSherlockActivity(), getString(R.string.login_not_online), Toast.LENGTH_LONG).show();
                return true;
            }
            // upload
            if (missionTemplate != null) {

                String title = null;
                String uploadList = null;
                String itemList = "";
                HashMap<String, ArrayList<String>> uploadables = PersistenceUtils
                        .loadUploadables(getSherlockActivity());
                ArrayList<MissionFeature> uploads = new ArrayList<MissionFeature>();
                final String tableName = mMode == FragmentMode.CREATION ? missionTemplate.schema_seg.localSourceStore
                        + MissionTemplate.NEW_NOTICE_SUFFIX : missionTemplate.schema_sop.localFormStore;
                List<String> uploadIDs = uploadables.get(tableName);


                ArrayList<MissionFeature> features = MissionUtils.getMissionFeatures(tableName, db);
                for (MissionFeature f : features) {
                	String missionID = mMode == FragmentMode.CREATION ? f.id : MissionUtils.getFeatureGCID(f);
                	if (uploadIDs.contains(missionID)) {
                		// this new entry is "uploadable" , add it
                		uploads.add(f);
                		if (f.properties != null && f.properties.containsKey(missionTemplate.nameField) && f.properties.get(missionTemplate.nameField) != null) {
                			itemList += (String) f.properties.get(missionTemplate.nameField) + "\n";
                		}else{//survey feature do not contain the "nameField", get it from the according mission
                			for (int i = 0; i < adapter.getCount(); i++) {
                				MissionFeature mf = adapter.getItem(i);
                				if(MissionUtils.getFeatureGCID(mf).equals(missionID)){
                					if (mf.properties != null && mf.properties.containsKey(missionTemplate.nameField)) {
                						itemList += (String) mf.properties.get(missionTemplate.nameField) + "\n";
                						break;
                					}
                				}
                			}
                		}
                	}
                }
            
                // from here we need to differentiate between "surveys" and "new entries"
                if (mMode == FragmentMode.CREATION) {
                	// ->new entries create a string "il(i) item(s) will be uploaded + list"
                	String entity = getResources().getQuantityString(R.plurals.new_entries, uploads.size());
                	uploadList = getResources().getQuantityString(R.plurals.upload_intro, uploads.size(), entity)
                			+ ":\n" + itemList;
                	title = getString(R.string.upload_title, getString(R.string.new_entry));
                } else {
                	// -> surveys create a string "il(i) survey(s) will be uploaded + list"
                	String entity = getResources().getQuantityString(R.plurals.surveys, uploads.size());
                	uploadList = getResources().getQuantityString(R.plurals.upload_intro, uploads.size(), entity)
                			+ ":\n" + itemList;
                	title = getString(R.string.upload_title, getString(R.string.survey));
                }
                final ArrayList<MissionFeature> finalUploads = uploads;
                // show the dialog to confirm the upload
                new AlertDialog.Builder(getSherlockActivity()).setTitle(title).setMessage(uploadList)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do the upload

                                // get urls
                                final String url = mMode == FragmentMode.CREATION ? missionTemplate.seg_form.url
                                        : missionTemplate.sop_form.url;
                                final String mediaUrl = mMode == FragmentMode.CREATION ? missionTemplate.seg_form.mediaurl
                                        : missionTemplate.sop_form.mediaurl;

                                // check urls
                                if (url == null || mediaUrl == null) {
                                    Log.e(UploadDialog.class.getSimpleName(),
                                            "no url or mediaurl available for upload, cannot continue");
                                    Toast.makeText(getSherlockActivity(), R.string.error_sending_data,
                                            Toast.LENGTH_LONG).show();
                                    return;
                                }

                                // as done before in "SendAction" ....
                                android.support.v4.app.FragmentManager fm = getSherlockActivity()
                                        .getSupportFragmentManager();
                                Fragment mTaskFragment = fm.findFragmentByTag(FRAGMENT_UPLOAD_DIALOG);
                                if (mTaskFragment == null) {
                                    FragmentTransaction ft = fm.beginTransaction();

                                    mTaskFragment = new UploadDialog() {
                                        @Override
                                        public void onFinish(Activity ctx, CommitResponse result) {
                                            if (result != null && result.isSuccess()) {

                                                Toast.makeText(getSherlockActivity(),
                                                        getResources().getString(R.string.data_send_success),
                                                        Toast.LENGTH_LONG).show();

                                                // update adapter and menu
                                                if (mMode == FragmentMode.CREATION) {
                                                    fillCreatedMissionFeatureAdapter();
                                                } else {
                                                    adapter.clear();
                                                    getSherlockActivity().getSupportLoaderManager()
                                                            .getLoader(CURRENT_LOADER_INDEX).forceLoad();
                                                }
                                                getSherlockActivity().supportInvalidateOptionsMenu();

                                                super.onFinish(ctx, result);
                                            } else {

                                                Toast.makeText(getSherlockActivity(), R.string.error_sending_data,
                                                        Toast.LENGTH_LONG).show();

                                                super.onFinish(ctx, result);
                                            }
                                        }
                                    };

                                    // fill up the args for the upload dialog
                                    Bundle arguments = new Bundle();
                                    arguments.putString(UploadDialog.PARAMS.DATAURL, url);
                                    arguments.putString(UploadDialog.PARAMS.MEDIAURL, mediaUrl);
                                    arguments.putString(UploadDialog.PARAMS.TABLENAME, tableName);

                                    // parse the max imagesize
                                    int defaultImageSize = 1000;
                                    try {
                                        defaultImageSize = Integer.parseInt((String) missionTemplate.config
                                                .get("maxImageSize"));
                                    } catch (NumberFormatException e) {
                                        Log.e(UploadDialog.class.getSimpleName(), e.getClass().getSimpleName(), e);
                                    } catch (NullPointerException e) {
                                        Log.e(UploadDialog.class.getSimpleName(), e.getClass().getSimpleName(), e);
                                    }

                                    HashMap<String, String> id_json_map = new HashMap<String, String>();
                                    HashMap<String, String[]> id_mediaurls_map = new HashMap<String, String[]>();

                                    // create entries <featureID, String data > for each missionfeature
                                    // create entries <featureID , String[] uploadUrls> for each missionfeature
                                    for (MissionFeature missionFeature : finalUploads) {

                                        if (mMode == FragmentMode.CREATION) {// new entry
                                            // Edit the MissionFeature for a better JSON compliance
                                            MissionUtils.alignPropertiesTypes(missionFeature,
                                                    missionTemplate.schema_seg.fields);
                                        }

                                        String featureIDString = MissionUtils.getFeatureGCID(missionFeature);

                                        // Set the "MY_ORIG_ID" to link this feature to its photos
                                        if (missionFeature.properties == null) {
                                            missionFeature.properties = new HashMap<String, Object>();
                                        }
                                        missionFeature.properties.put("MY_ORIG_ID", featureIDString);

                                        GeoJson gson = new GeoJson();
                                        String c = gson.toJson(missionFeature);
                                        String data = null;
                                        try {
                                            data = new String(c.getBytes("UTF-8"));
                                        } catch (UnsupportedEncodingException e) {
                                            Log.e(UploadDialog.class.getSimpleName(),
                                                    "error transforming missionfeature to gson", e);
                                        }
                                        id_json_map.put(featureIDString, data);

                                        // photos
                                        FormUtils.resizeFotosToMax(getActivity().getBaseContext(), featureIDString,
                                                defaultImageSize);
                                        String[] urls = FormUtils.getPhotoUriStrings(getActivity().getBaseContext(),
                                                featureIDString);
                                        id_mediaurls_map.put(featureIDString, urls);

                                    }
                                    // add the populated maps
                                    arguments.putSerializable(UploadDialog.PARAMS.MISSIONS, id_json_map);
                                    arguments.putSerializable(UploadDialog.PARAMS.MISSION_MEDIA_URLS, id_mediaurls_map);

                                    /*
                                     * Set the destination folder
                                     */
                                    arguments.putString(UploadDialog.PARAMS.MISSION_ID, missionTemplate.schema_seg.localSourceStore);

                                    SharedPreferences prefs = PreferenceManager
                                            .getDefaultSharedPreferences(getSherlockActivity());

                                    String email = prefs.getString(LoginActivity.PREFS_USER_EMAIL, null);
                                    String pass = prefs.getString(LoginActivity.PREFS_PASSWORD, null);

                                    arguments.putString(UploadDialog.PARAMS.BASIC_AUTH,
                                            LoginRequestInterceptor.getB64Auth(email, pass));

                                    mTaskFragment.setArguments(arguments);

                                    ((DialogFragment) mTaskFragment).setCancelable(false);
                                    ft.add(mTaskFragment, FRAGMENT_UPLOAD_DIALOG);
                                    ft.commit();

                                }
                            }
                        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // nothing, close dialog
                            }
                        }).show();
            }
        } else if (id == R.id.full_map) {

            // Open the Map
            if (getSherlockActivity() instanceof PendingMissionListActivity) {
                ((PendingMissionListActivity) getSherlockActivity()).launchFullMap();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * checks if location services are available
     */
    private boolean isGPSAvailable() {
        LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

    }

    /**
     * starts the missionfeature creation
     */
    private void startMissionFeatureCreation() {

        Intent i = new Intent(getSherlockActivity(), FormEditActivity.class);
        i.putExtra(PendingMissionListActivity.ARG_CREATE_MISSIONFEATURE, true);
        startActivityForResult(i, FormEditActivity.FORM_CREATE_NEW_MISSIONFEATURE);

    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.AbsListView.OnScrollListener#onScrollStateChanged(android.widget.AbsListView, int)
     */
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.AbsListView.OnScrollListener#onScroll(android.widget.AbsListView, int, int, int)
     */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        Log.v("PMLF", "First: " + firstVisibleItem + ", count: " + visibleItemCount + ", total: " + totalItemCount);
        if (firstVisibleItem == 0 || visibleItemCount == 0) {
            mSwipeRefreshLayout.setEnabled(true);
        } else {
            mSwipeRefreshLayout.setEnabled(false);
        }

        // check if applicable
        if (adapter == null) {
            return;
        }
        if (adapter.getCount() == 0) {
            return;
        }
        if (getSherlockActivity().getSupportLoaderManager().getLoader(CURRENT_LOADER_INDEX) == null) {
            return;
        }

        // if the last item is visible and can load more resources
        // load more resources
        int l = visibleItemCount + firstVisibleItem;
        if (l >= totalItemCount && !isLoading) {
            // It is time to add new data. We call the listener
            // getListView().addFooterView(footer);

            isLoading = true;
            loadMore();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onCreateLoader(int, android.os.Bundle)
     */
    @Override
    public Loader<List<MissionFeature>> onCreateLoader(int id, Bundle arg1) {
        getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
        //getSherlockActivity().getSupportActionBar();

        Log.d("MISSION_LIST", "onCreateLoader() for id " + id);

        return MissionUtils.createMissionLoader(missionTemplate, getSherlockActivity(), page, pagesize, db);
    }

    /**
	 * 
	 */
    /*
     * (non-Javadoc)
     * 
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoadFinished(android.support.v4.content.Loader, java.lang.Object)
     */
    @Override
    public void onLoadFinished(Loader<List<MissionFeature>> loader, List<MissionFeature> results) {
        if (results == null) {
            Toast.makeText(getSherlockActivity(), R.string.error_connectivity_problem, Toast.LENGTH_SHORT).show();
            setNoData();
        } else {
            Log.d(TAG, "loader returned " + results.size());
            // add loaded resources to the listView
            for (MissionFeature a : results) {
                adapter.add(a);
            }
            if (adapter.isEmpty()) {
                setNoData();
            } else {
            }
        }
        stopLoadingGUI();
        
        //if this fragment is visible to the user, check if the background data for the current template is available
        if(getUserVisibleHint()){
        	checkIfBackgroundIsAvailableForTemplate();
        }
    }

    /**
     * checks if background data for the current template is available
     * if not, the user is asked to download
     */
    private void checkIfBackgroundIsAvailableForTemplate() {
		
    	boolean exists = MissionUtils.checkTemplateForBackgroundData(getActivity(), missionTemplate);
    	
    	if(!exists){
			
			final HashMap<String,Integer> urls = MissionUtils.getContentUrlsAndFileAmountForTemplate(missionTemplate);
			
			if(BuildConfig.DEBUG){
				
				Log.i(TAG, "downloading "+ urls.toString());
			}
			Resources res = getResources();
			for(String url : urls.keySet()){
				
				final String mount  = MapFilesProvider.getEnvironmentDirPath(getActivity());
				
				String dialogMessage = res.getQuantityString(R.plurals.dialog_message_with_amount, urls.get(url), urls.get(url));
                new ZipFileManager(getActivity(), mount, MapFilesProvider.getBaseDir(), url, null, dialogMessage) {
					@Override
					public void launchMainActivity(final boolean success) {
						
						// TODO apply ? this was earlier in StartupActivity
//						if (getActivity().getApplication() instanceof GeoCollectApplication) {
//							((GeoCollectApplication) getActivity().getApplication()).setupMBTilesBackgroundConfiguration();
//						}
						//launch.putExtra(PendingMissionListFragment.INFINITE_SCROLL, false);
						if(success){
							
							Toast.makeText(getActivity(), getString(R.string.download_successfull), Toast.LENGTH_SHORT).show();						
						}
						
					}
				};
			}
			
			
		}
		
	}

	/*
     * (non-Javadoc)
     * 
     * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoaderReset(android.support.v4.content.Loader)
     */
    @Override
    public void onLoaderReset(Loader<List<MissionFeature>> arg0) {
        adapter.clear();
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(false);

    }

    /**
     * Loads more data from the Loader
     */
    protected void loadMore() {

        if (getSherlockActivity().getSupportLoaderManager().getLoader(CURRENT_LOADER_INDEX) != null) {

            page++;
            getSherlockActivity().getSupportLoaderManager().restartLoader(CURRENT_LOADER_INDEX, null, this);

        }
    }

    /**
     * Create the data loader and bind the loader to the parent callbacks
     * 
     * @param URL (not used for now)
     * @param loaderIndex a unique id for query loader
     */
    private void startDataLoading(MissionTemplate t, int loaderIndex) {

        // initialize Load Manager
        mCallbacks = this;
        // reset page
        LoaderManager lm = getSherlockActivity().getSupportLoaderManager();
        adapter.clear();
        page = 0;
        lm.initLoader(loaderIndex, null, this);
    }

    /**
     * Refresh the list
     */
    @Override
    public void onResume() {
        super.onResume();

        if (mMode == FragmentMode.CREATION) {
            if (missionAdapter != null) {
                fillCreatedMissionFeatureAdapter();
            }
        } else if (mMode == FragmentMode.PENDING) {
            // Start data loading
            if(getSherlockActivity().getSupportLoaderManager().getLoader(CURRENT_LOADER_INDEX) != null){
                getSherlockActivity().getSupportLoaderManager().getLoader(CURRENT_LOADER_INDEX).forceLoad();
            }
        }
        
        getSherlockActivity().supportInvalidateOptionsMenu();

    }

    /**
     * Handle the results
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult()");
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PendingMissionListActivity.SPATIAL_QUERY) {

            if (data != null && data.hasExtra("query")) {
                BaseFeatureInfoQuery query = data.getParcelableExtra("query");

                // Create task query
                if (query instanceof BBoxQuery) {
                    BBoxQuery bbox = (BBoxQuery) query;

                    // TODO: Refactor the Preferences Double Storage
                    // This is just a fast hack to store Doubles in Long but It should be handled by a more clean utility class
                    // Maybe extend the Preference Editor?

                    Log.v(TAG, "Received:\nN: " + bbox.getN() + "\nN: " + bbox.getS() + "\nE: " + bbox.getE() + "\nW: "
                            + bbox.getW() + "\nSRID: " + bbox.getSrid());
                    SharedPreferences sp = getSherlockActivity().getSharedPreferences(
                            SQLiteCascadeFeatureLoader.PREF_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putLong(SQLiteCascadeFeatureLoader.FILTER_N, Double.doubleToRawLongBits(bbox.getN()));
                    editor.putLong(SQLiteCascadeFeatureLoader.FILTER_S, Double.doubleToRawLongBits(bbox.getS()));
                    editor.putLong(SQLiteCascadeFeatureLoader.FILTER_W, Double.doubleToRawLongBits(bbox.getW()));
                    editor.putLong(SQLiteCascadeFeatureLoader.FILTER_E, Double.doubleToRawLongBits(bbox.getE()));
                    editor.putInt(SQLiteCascadeFeatureLoader.FILTER_SRID, Integer.parseInt(bbox.getSrid()));
                    editor.commit();

                    adapter.clear();
                    getSherlockActivity().getSupportLoaderManager().getLoader(CURRENT_LOADER_INDEX).forceLoad();

                    Toast.makeText(getActivity(), getString(R.string.selection_filtered), Toast.LENGTH_SHORT).show();
                }

                // else if(query instanceof CircleQuery){ }

            } else {

                // No result, clean the filter
                SharedPreferences sp = getSherlockActivity().getSharedPreferences(SQLiteCascadeFeatureLoader.PREF_NAME,
                        Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.remove(SQLiteCascadeFeatureLoader.FILTER_N);
                editor.remove(SQLiteCascadeFeatureLoader.FILTER_S);
                editor.remove(SQLiteCascadeFeatureLoader.FILTER_W);
                editor.remove(SQLiteCascadeFeatureLoader.FILTER_E);
                editor.remove(SQLiteCascadeFeatureLoader.FILTER_SRID);
                editor.commit();

                adapter.clear();
                getSherlockActivity().getSupportLoaderManager().getLoader(CURRENT_LOADER_INDEX).forceLoad();
                
            }

        } else if (requestCode == ARG_ENABLE_GPS) {
            Log.d(FormEditActivity.class.getSimpleName(), "back from GPS settings");

            if (!isGPSAvailable()) {
                Toast.makeText(getActivity(), R.string.gps_still_not_enabled, Toast.LENGTH_LONG).show();
            } else {
                startMissionFeatureCreation();
            }
        }else {
        
            missionTemplate = ((GeoCollectApplication) getActivity().getApplication()).getTemplate();
            CURRENT_LOADER_INDEX = missionTemplate.getLoaderIndex();
            adapter.setTemplate(missionTemplate);
            adapter.clear();
            getSherlockActivity().getSupportLoaderManager().getLoader(CURRENT_LOADER_INDEX).forceLoad();
        }
    }
    
    /**
     * Callback for the {@link SwipeRefreshLayout}
     */
    @Override
    public void onRefresh() {

        SharedPreferences sp = getSherlockActivity().getSharedPreferences(SQLiteCascadeFeatureLoader.PREF_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        // Reset the preference to force update
        editor.putLong(SQLiteCascadeFeatureLoader.LAST_UPDATE_PREF, 0);
        editor.commit();

        adapter.clear();
        getSherlockActivity().getSupportLoaderManager().getLoader(CURRENT_LOADER_INDEX).forceLoad();
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);

    }

    /**
     * Utility method to check whether a {@link ListView} can scroll up from it's current position. Handles platform version differences, providing
     * backwards compatible functionality where needed.
     */
    private static boolean canListViewScrollUp(ListView listView) {
        if (android.os.Build.VERSION.SDK_INT >= 14) {
            // For ICS and above we can call canScrollVertically() to determine this
            return ViewCompat.canScrollVertically(listView, -1);
        } else {
            // Pre-ICS we need to manually check the first visible item and the child view's top
            // value
            return listView.getChildCount() > 0
                    && (listView.getFirstVisiblePosition() > 0 || listView.getChildAt(0).getTop() < listView
                            .getPaddingTop());
        }
    }

    /**
     * Sub-class of {@link android.support.v4.widget.SwipeRefreshLayout} for use in this {@link android.support.v4.app.ListFragment}. The reason that
     * this is needed is because {@link android.support.v4.widget.SwipeRefreshLayout} only supports a single child, which it expects to be the one
     * which triggers refreshes. In our case the layout's child is the content view returned from
     * {@link android.support.v4.app.ListFragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)} which is a
     * {@link android.view.ViewGroup}.
     * 
     * <p>
     * To enable 'swipe-to-refresh' support via the {@link android.widget.ListView} we need to override the default behavior and properly signal when
     * a gesture is possible. This is done by overriding {@link #canChildScrollUp()}.
     */
    private class ListFragmentSwipeRefreshLayout extends SwipeRefreshLayout {

        public ListFragmentSwipeRefreshLayout(Context context) {
            super(context);
        }

        /**
         * As mentioned above, we need to override this method to properly signal when a 'swipe-to-refresh' is possible.
         * 
         * @return true if the {@link android.widget.ListView} is visible and can scroll up.
         */
        @Override
        public boolean canChildScrollUp() {
            final ListView listView = getListView();
            if (listView.getVisibility() == View.VISIBLE) {
                return canListViewScrollUp(listView);
            } else {
                return false;
            }
        }

    }

    /**
     * switches between pending mission mode and created mission mode
     * 
     * @param FragmentMode the new mode
     */
    public void switchAdapter(FragmentMode newMode) {

        // if(newMode == mMode && MissionUtils.getDefaultTemplate(getActivity()).id.equals(missionTemplate.id)){
        // Log.d(TAG, "returning switch adapter");
        // return;
        // }

        mMode = newMode;

        if (mMode == FragmentMode.CREATION) {

            missionAdapter = new CreatedMissionAdapter(getSherlockActivity(), R.layout.mission_resource_row,
                    missionTemplate.schema_seg.localSourceStore + MissionTemplate.NEW_NOTICE_SUFFIX, missionTemplate);

            // delete created items on long click listener
            getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                    new AlertDialog.Builder(getSherlockActivity()).setTitle(R.string.my_inspections)
                            .setMessage(R.string.created_inspection_delete)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    // nothing
                                    final MissionFeature f = (MissionFeature) getListView().getItemAtPosition(position);

                                    final Database db = ((PendingMissionListActivity) getSherlockActivity()).spatialiteDatabase;

                                    final String tableName = missionTemplate.schema_seg.localSourceStore + MissionTemplate.NEW_NOTICE_SUFFIX;
                                    // delete this new entry
                                    PersistenceUtils.deleteMissionFeature(db, tableName, f.id);

                                    // if this entry was uploadable remove it from the list of uploadables
                                    HashMap<String, ArrayList<String>> uploadables = PersistenceUtils
                                            .loadUploadables(getSherlockActivity());
                                    if (uploadables.containsKey(tableName) && uploadables.get(tableName).contains(f.id)) {
                                        uploadables.get(tableName).remove(f.id);
                                        PersistenceUtils.saveUploadables(getSherlockActivity(), uploadables);
                                    }

                                    fillCreatedMissionFeatureAdapter();
                                }
                            }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // nothing
                                    dialog.dismiss();
                                }
                            }).show();

                    return true;
                }
            });

            if (getSherlockActivity() instanceof PendingMissionListActivity) {

                fillCreatedMissionFeatureAdapter();

            }

            setListAdapter(missionAdapter);

        } else if (mMode == FragmentMode.PENDING) {

            // remove longclicklistener
            getListView().setOnItemLongClickListener(null);

            adapter.setTemplate(missionTemplate);
            setListAdapter(adapter);

        }

        // invalidate actionbar
        getSherlockActivity().supportInvalidateOptionsMenu();

    }

    /**
     * loads the locally available "created missionfeatures" from the database
     */
    private void fillCreatedMissionFeatureAdapter() {

        final MissionTemplate t = missionTemplate;

        final Database db = ((PendingMissionListActivity) getSherlockActivity()).spatialiteDatabase;

        Log.v(TAG, "Loading created missions for " + t.title);
        final ArrayList<MissionFeature> missions = MissionUtils.getMissionFeatures(t.schema_seg.localSourceStore + MissionTemplate.NEW_NOTICE_SUFFIX, db);

        final String prio = t.priorityField;
        final HashMap<String, String> colors = t.priorityValuesColors;
        if (prio != null && colors != null) {
            for (MissionFeature f : missions) {
                if (f.properties.containsKey(prio)) {
                    f.displayColor = colors.get(f.properties.get(prio));
                }
            }
        }

        missionAdapter.clear();
        if (Build.VERSION.SDK_INT > 10) {
            missionAdapter.addAll(missions);
        } else {
            for (MissionFeature f : missions) {
                missionAdapter.add(f);
            }
        }

        missionAdapter.notifyDataSetChanged();

        if (missionAdapter.isEmpty()) {
            setNoData();
        }
    }

    /**
     * returns the current mode of this fragment either PENDING or CREATING
     */
    public FragmentMode getFragmentMode() {
        return mMode;
    }

    /**
     * sets the current missionTemplate of this fragments list
     * 
     * @param pMissionTemplate the template to apply
     */
    public void setTemplate(final MissionTemplate pMissionTemplate) {

        missionTemplate = pMissionTemplate;

    }

    /**
     * 
     * @param restarts the loader with a certain index
     */
    public void restartLoader(final int index) {

        startDataLoading(missionTemplate, index);

        CURRENT_LOADER_INDEX = index;
    }

    public MissionTemplate getCurrentMissionTemplate() {
        return missionTemplate;
    }

}
