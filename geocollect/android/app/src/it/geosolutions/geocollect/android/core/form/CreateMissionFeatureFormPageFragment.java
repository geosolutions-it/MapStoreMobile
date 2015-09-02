package it.geosolutions.geocollect.android.core.form;

import it.geosolutions.android.map.fragment.MapFragment;
import it.geosolutions.android.map.view.AdvancedMapView;
import it.geosolutions.geocollect.android.app.R;
import it.geosolutions.geocollect.android.core.form.action.AndroidAction;
import it.geosolutions.geocollect.android.core.form.action.CameraAction;
import it.geosolutions.geocollect.android.core.form.action.SaveMissionFeatureAction;
import it.geosolutions.geocollect.android.core.form.action.SendMissionFeatureAction;
import it.geosolutions.geocollect.android.core.form.utils.FormUtils;
import it.geosolutions.geocollect.android.core.mission.Mission;
import it.geosolutions.geocollect.android.core.form.utils.MissionFeatureFormBuilder;
import it.geosolutions.geocollect.android.core.mission.MissionFeature;
import it.geosolutions.geocollect.android.core.mission.utils.MissionUtils;
import it.geosolutions.geocollect.android.core.mission.utils.PersistenceUtils;
import it.geosolutions.geocollect.android.core.widgets.DatePicker;
import it.geosolutions.geocollect.android.core.widgets.UILImageAdapter;
import it.geosolutions.geocollect.model.config.MissionTemplate;
import it.geosolutions.geocollect.model.viewmodel.Field;
import it.geosolutions.geocollect.model.viewmodel.FormAction;
import it.geosolutions.geocollect.model.viewmodel.FormActionType;
import it.geosolutions.geocollect.model.viewmodel.Page;
import it.geosolutions.geocollect.model.viewmodel.type.XType;

import java.util.HashMap;

import jsqlite.Database;

import org.mapsforge.core.model.GeoPoint;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
/**
 * a page fragment for a created missionfeature
 * 
 * @author robertoehler
 *
 */
public class CreateMissionFeatureFormPageFragment extends MapFragment {
	
	/**
	 * Tag for logs
	 */
	public static final String TAG = CreateMissionFeatureFormPageFragment.class.getSimpleName();
	
	/**
	 * The argument for the page
	 */
    public static final String CREATED_MISSION = "createdMission";
    private ScrollView mScrollView;
    private Page page;
	private LinearLayout mFormView;
//	private ProgressBar mProgressView;
	private boolean mDone;
	private boolean visibleToUser;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		Integer pageNumber = (Integer) getArguments().get(FormPageFragment.ARG_OBJECT);
		if(pageNumber!=null){
			MissionTemplate t = MissionUtils.getDefaultTemplate(getActivity());
			
			
			//if page number exists i suppose pages is not empty
			page = t.seg_form.pages.get(pageNumber);	
		}

		//This allow form fragments to 
		// display actions in the action bar
		setHasOptionsMenu(true);
		
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// add actions from the page configuration
		if(this.page.actions == null) return;
	   //add actions associated to the page
	   for(int i = 0;i< this.page.actions.size();i++){
		   FormAction a = this.page.actions.get(i);
		   		   
		   MenuItem item = menu.add(Menu.NONE, a.id , Menu.NONE, a.text);
		   item.setIcon(FormUtils.getDrawable(a.iconCls));
		   item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		   Log.v("ACTION","added action"+ a.name);
	   }
	   super.onCreateOptionsMenu(menu,inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
	
		if(this.page.actions !=null){
			for(FormAction action : this.page.actions){
				if(id == action.id){
					
					storePageData(this.page, mFormView);
					
					MissionFeature mFeature = ((FormEditActivity) getSherlockActivity()).mMission;
					performAction(action, mFeature);
				}
			}
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {

		if (mScrollView == null) {
			// normally inflate the view hierarchy
			mScrollView = (ScrollView) inflater.inflate(R.layout.form_page_fragment,container, false);
			mFormView = (LinearLayout) mScrollView.findViewById(R.id.formcontent);
//			mProgressView = (ProgressBar) mScrollView.findViewById(R.id.loading);
		} else {
			// mScrollView is still attached to the previous view hierarchy
			// we need to remove it and re-attach it to the current one
			ViewGroup parent = (ViewGroup) mScrollView.getParent();
			parent.removeView(mScrollView);
		}
		
		mFormView.setGravity(Gravity.TOP);
		buildForm();

		
		return mScrollView;
	}
	
    /**
     * Creates the page content cycling the page fields
     */
    private void buildForm() {
		// if the view hierarchy was already build, skip this
		if (mDone)
			return;

		MissionFeatureFormBuilder.buildForm(getActivity(), this.mFormView, page.fields, ((FormEditActivity)getSherlockActivity()).mMission);
		
		final Database db = ((FormEditActivity) getSherlockActivity()).spatialiteDatabase;
		final String tableName = ((FormEditActivity) getSherlockActivity()).mMissionTableName;
		final String id = ((FormEditActivity) getSherlockActivity()).mMission.id;
		
		PersistenceUtils.loadSpinnerData(page, mFormView, db, getActivity(),tableName, id);

		// the view hierarchy is now complete
		mDone = true;
	
		if(visibleToUser && page.attributes != null && page.attributes.containsKey("message")){
			Toast.makeText(getSherlockActivity(), (String) page.attributes.get("message"), Toast.LENGTH_LONG).show();
		}

	}
    
    @Override
    public void setMenuVisibility(boolean menuVisible) {
    	super.setMenuVisibility(menuVisible);
    	visibleToUser = menuVisible;

    }
    
    
    @Override 
    public void onSaveInstanceState(Bundle outState) {
    	outState.putSerializable(CREATED_MISSION,((FormEditActivity) getSherlockActivity()).mMission);
    	    	
    }
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
    	super.setUserVisibleHint(isVisibleToUser);
    	
    	//using this method, every page will be saved immediately after when is was swiped away
    	
    	if(!isVisibleToUser && page != null && mFormView != null){
    		storePageData(page, mFormView);
    	}
    }
    @Override
    public void onPause() {
    	super.onPause();
    	
    	if(page != null && mFormView != null){
    		storePageData(page, mFormView);
    	}
    }
    /**
     * store this fragments page data
     * @param page
     * @param layout
     */
	private void storePageData(Page page, LinearLayout layout) {
		
		String value;
		
		for(Field f : page.fields){
			if(f == null )continue;

			View v = layout.findViewWithTag(f.fieldId);

			if(v == null){
				Log.w(TAG, "Tag not found : "+f.fieldId);
				continue;
			}
			
			if (f.xtype == null) {
				
				value = ((TextView)v).getText().toString();
			} else {
				// switch witch widget create
				switch (f.xtype) {
				case textfield:
					value = ((TextView)v).getText().toString();
					break;
				case textarea:
					value = ((TextView)v).getText().toString();
					break;
				case datefield:
					value = ((DatePicker)v).getText().toString();
					break;
				case checkbox:
					value = ((CheckBox)v).isChecked() ? "1" : "0";
					break;
				case spinner:
					if(((Spinner)v).getSelectedItem() instanceof HashMap<?, ?>){
						@SuppressWarnings("unchecked")
						HashMap<String, String> h = (HashMap<String, String>) ((Spinner)v).getSelectedItem();
						if(h.get("f1") != null){
							value = h.get("f1");
							break;
						}else{
							continue;
						}
					}else{
						Log.w(TAG, "Type mismatch on Spinner :"+f.fieldId);
						continue;
					}
				case label:
					// skip
					continue;
					//break;
				case separator:
					// skip
					continue;
					//break;
				case photo:
					// skip
					continue;
					//break;
				case mapViewPoint:
					if(!(Boolean)MissionFeatureFormBuilder.getAttributeWithDefault(f,"editable",true)){
						// Field is not editable, do not save
						continue;
					}
					AdvancedMapView amv = ((AdvancedMapView)v);
					if( amv.getMarkerOverlay()==null){
						Log.v(TAG, "Missing MarkerOverlay for "+f.fieldId);
						continue;
					}
					if(amv.getMarkerOverlay().getMarkers() == null){
						Log.v(TAG, "Missing Markers for "+f.fieldId);
						continue;
					}
					if(	amv.getMarkerOverlay().getMarkers().size()<=0){
						Log.v(TAG, "Empty Markers for "+f.fieldId);
						continue;
					}
					if(amv.getMarkerOverlay().getMarkers().get(0) == null) {
						Log.v(TAG, "First Marker is NULL for "+f.fieldId);
						continue;
					}
					if(amv.getMarkerOverlay().getMarkers().get(0).getGeoPoint() != null){
						GeoPoint g = amv.getMarkerOverlay().getMarkers().get(0).getGeoPoint();
						if(g != null){
							value = "MakePoint("+g.longitude+","+g.latitude+", 4326)";
							
							// TODO: remove
							// Set the in-memory object geometry
							GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(),4326);
							((FormEditActivity) getSherlockActivity()).mMission.geometry = geometryFactory.createPoint(new Coordinate(g.longitude, g.latitude));
							
						}else{
							Log.v(TAG, "Missing Geopoint for "+f.fieldId);
							continue;
						}
					}else{
						Log.w(TAG, "Cannot list features for "+f.fieldId);
						continue;
					}
					break;
				default:
					//textfield as default
					value = ((TextView)v).getText().toString();
				}
			}
			//here
			Log.d(TAG, "saving "+value +" for "+ f.fieldId);
			
			if(!f.fieldId.equals(Mission.GEOMETRY_FIELD_STRING)){
				((FormEditActivity) getSherlockActivity()).mMission.properties.put(f.fieldId, value);
			}
			
			Database db = ((FormEditActivity) getSherlockActivity()).spatialiteDatabase;
			String tableName = ((FormEditActivity) getSherlockActivity()).mMissionTableName;
			String id = ((FormEditActivity) getSherlockActivity()).mMission.id;
			
			PersistenceUtils.updateCreatedMissionFeatureRow(db, tableName, f, value, id);
			
		}
		
	}
	
	
	/**
	 * Performs and <Action> in the current page.
	 * This function is call when the user tap an action
	 * @param a
	 */
	private void performAction(FormAction a, MissionFeature feature) {
		Log.v("ACTION","performing action "+a);
		if(a.type == FormActionType.save){
			SaveMissionFeatureAction smfa = new SaveMissionFeatureAction(a);
			smfa.performAction(this, a, feature);
		}else if(a.type == FormActionType.send){
			SendMissionFeatureAction smfa = new SendMissionFeatureAction(a);
			smfa.performAction(this, a, feature);
		}else{
			AndroidAction aa = FormUtils.getAndroidAction(a);
			if(aa!=null){
				Mission m = new Mission();
				m.setOrigin(feature);
				aa.performAction(this, a, m, page);
			}
		}
		
	}
	
	/**
	 * Handle the results of the CameraAction
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult(): "+requestCode);
	    super.onActivityResult(requestCode, resultCode, data);
	    
	    if(requestCode == CameraAction.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK){
	    	
	    	MissionFeature mFeature = ((FormEditActivity) getSherlockActivity()).mMission;
	    	String originIDString = MissionUtils.getFeatureGCID(mFeature);
	    	if( this.page.fields != null &&
	    			originIDString != null &&
	    			!originIDString.isEmpty()){
	    		
	    		
				for(Field f : page.fields){
					if(f == null || f.xtype != XType.photo)
						continue;
					
					// TODO: use a non-string tag
					GridView grid = (GridView) this.mFormView.findViewWithTag("__photo__");
					
			    	if( grid != null && ImageLoader.getInstance().isInited()){
			    		
		    		    //((UILImageAdapter)grid.getAdapter()).setImageUrls(FormUtils.getPhotoUriStrings(this.mission.getOrigin().id));
		    		    ((UILImageAdapter)grid.getAdapter()).notifyDataSetChanged();
			    	}
			    	break;
		    	}
	    		
	    	}
	    }
	    
	}
	
}
