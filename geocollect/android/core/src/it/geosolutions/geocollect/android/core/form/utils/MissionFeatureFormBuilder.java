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
package it.geosolutions.geocollect.android.core.form.utils;

import it.geosolutions.android.map.control.CoordinateControl;
import it.geosolutions.android.map.control.LocationControl;
import it.geosolutions.android.map.control.MarkerControl;
import it.geosolutions.android.map.dto.MarkerDTO;
import it.geosolutions.android.map.overlay.MarkerOverlay;
import it.geosolutions.android.map.overlay.items.DescribedMarker;
import it.geosolutions.android.map.overlay.managers.MultiSourceOverlayManager;
import it.geosolutions.android.map.utils.MapFilesProvider;
import it.geosolutions.android.map.view.AdvancedMapView;
import it.geosolutions.geocollect.android.core.R;
import it.geosolutions.geocollect.android.core.form.FormEditActivity;
import it.geosolutions.geocollect.android.core.form.ViewPagerAwareMarkerControl;
import it.geosolutions.geocollect.android.core.mission.MissionFeature;
import it.geosolutions.geocollect.android.core.mission.utils.LocationProvider;
import it.geosolutions.geocollect.android.core.mission.utils.LocationProvider.LocationResultCallback;
import it.geosolutions.geocollect.android.core.widgets.DatePicker;
import it.geosolutions.geocollect.model.viewmodel.Field;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.mapsforge.android.maps.MapView;
import org.mapsforge.core.model.GeoPoint;

import android.content.Context;
import android.graphics.Typeface;
import android.location.Location;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.vividsolutions.jts.geom.Point;

/**
* @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
* 
* @author Robert Oehler
* 
* Utility Class to build forms adapted for MissionFeatures
*/
public class MissionFeatureFormBuilder {

	// set a static id
	private static int sId = 0;

	private static int id() {
		return sId++;
	}

	/**
	 * Creates and add the form fields to the layout passed
	 * 
	 * @param context
	 *            the context
	 * @param mFormView
	 *            the Layout
	 * @param fields
	 * 			  list of fields
	 * @param feature
	 * 			  the according missionFeature 
	 */
	public static void buildForm(Context context, LinearLayout mFormView, List<Field> fields, MissionFeature feature) {
		if (fields == null) {
			return;
		}
		for (Field f : fields) {
			//the null field will be ignored
			if(f == null )continue;
			if (f.xtype == null) {
				//textfield as default
				addTextField(f, mFormView, context,feature);
			} else {
				// switch witch widget create
				switch (f.xtype) {
				case textfield:
					addTextField(f, mFormView, context,feature);
					break;
				case textarea:
					addTextField(f, mFormView, context,feature);
					break;
				case datefield:
					addDatePicker(f, mFormView, context,feature);
					break;
				case checkbox:
					addCheckBox(f,mFormView,context,feature);
					break;
				case spinner:
					addSpinner(f,mFormView,context,feature);
					break;
				case label:
					addLabel(f,mFormView,context,feature);
					break;
				case mapViewPoint:
					addMapViewPoint(f,mFormView,context,feature);
					break;
				default:
					//textfield as default
					addTextField(f, mFormView, context,feature);
				}
			}
		}
	}

	/**
	 * Creates a map view - when a valid geometry is available the map will be
	 * centered on it - otherwise a location acquiring will be started which needs
	 * the priorly checked activated location services - its result will update the map
	 * @param f
	 * @param mFormView
	 * @param context
	 * @param missionFeature
	 */
	private static void addMapViewPoint(final Field field, LinearLayout mFormView,
			final Context context, final MissionFeature feature) {
		TextView tvLabel = getLabelForField(field, context);
		AdvancedMapView mapView =null;
		ImageButton buttonLocation = null;
		
		if(context instanceof FormEditActivity){
			View formView = ((FormEditActivity)context).getLayoutInflater().inflate(R.layout.form_mapview, null);
			mapView = (AdvancedMapView) formView.findViewById(R.id.advancedMapView);
			
			buttonLocation = (ImageButton) formView.findViewById(R.id.ButtonLocation);
			
			mFormView.addView(tvLabel);
			mapView.setTag(field.fieldId);
			mFormView.addView(formView);
			
		}else{
			if(mapView==null){
				mapView = new AdvancedMapView(context);
				mapView.setLayoutParams(getMapLayoutParams(field));//if not in layout
				mFormView.addView(tvLabel);
				//mFormView.addView(infoButton);
				mapView.setMinimumHeight(100);//it doesn't work, try a different method
				mapView.setMinimumWidth(100);
				mapView.setTag(field.fieldId);
				mFormView.addView(mapView);
			}
		}

		//setup overlay Manager
		final MultiSourceOverlayManager o = new MultiSourceOverlayManager(mapView);
		mapView.setOverlayManger(o);
		final MarkerOverlay m = new MarkerOverlay();
		o.setMarkerOverlay(m);
		m.setProjection(mapView.getProjection());
		// only from tag is supported
		
		GeoPoint geoPoint = null;
		
		if(feature.geometry != null){

			final Point p = feature.geometry.getCentroid();
			
			//check if point was set to 0,0
			if((!isZero(p.getX(),0.0001d)) && (!isZero(p.getY(),0.0001d))){		
				//if not, use it later when centering
				geoPoint = new GeoPoint(p.getY(), p.getX());
			}else{
				//get a better position
				acquirePosition(context, mapView, m, o);
			}
		}else{
			//get a position
			acquirePosition(context, mapView, m, o);
		}

		o.setMarkerOverlay(m);
		
		//Marker Control 

		
		//check editable
		Boolean editable = (Boolean)getAttributeWithDefault(field,"editable",true);
		//check disablePan
		Boolean disablePan = (Boolean)getAttributeWithDefault(field,"disablePan",false);
		//add marker control
		MarkerControl mc;
		if(context instanceof FormEditActivity)
			mc = new ViewPagerAwareMarkerControl(mapView,editable, ((FormEditActivity)context).mViewPager);
		else
			mc = new MarkerControl(mapView,editable);

		//mc.setInfoButton(infoButton);
		mapView.addControl(mc);
		
		if(editable){
			 mc.setMode(MarkerControl.MODE_EDIT);
			 //TODO make infobutton work
			 //infoButton.setVisibility(ImageButton.VISIBLE);

		}

        File mapFile = MapFilesProvider.getBackgroundMapFile();
		if(mapFile!=null){
			mapView.setMapFile(mapFile);
		}
		//pannable
		mapView.setClickable(!disablePan);
		mapView.setBuiltInZoomControls(true);
		//set center and zoom level limits
		Integer b = (Integer)getAttributeWithDefault(field,"zoom",18);
		
		mapView.getMapViewPosition().setZoomLevel(b.byteValue());
		mapView.getMapZoomControls().setZoomLevelMax((byte) 30);
		mapView.getMapZoomControls().setZoomLevelMin((byte) 1);
		
		//setup tile cache
		mapView.getFileSystemTileCache().setPersistent(true);

		if(geoPoint != null){
			centerMapAndSetMarker(context, geoPoint, mapView, m, o);
		}
		if(context instanceof FormEditActivity){
			
			
			//add coordinates control
			 mapView.addControl(new CoordinateControl(mapView, true));
			
			 //add "location" control connected to the button
			 LocationControl lc  =new LocationControl(mapView);
			 lc.setActivationButton(buttonLocation);
			 mapView.addControl(lc);

		}
	}
	/**
	 * start the location acquisition
	 * @param context
	 * @param mapView
	 * @param m
	 * @param o
	 * @param field
	 */
	public static void acquirePosition(final Context context,final MapView mapView,final MarkerOverlay m,final MultiSourceOverlayManager o){
		
		
		LocationResultCallback locationResult = new LocationResultCallback(){
			@Override
			public void gotLocation(final Location location){
				
				if(location != null){
					centerMapAndSetMarker(context, new GeoPoint(location.getLatitude(), location.getLongitude()), mapView, m, o);
				}
			}
		};
		new LocationProvider().getLocation(context, locationResult);
	}
	/**
	 * center the mapView on the provided geopoint, add a marker and show it
	 * @param context
	 * @param geoPoint
	 * @param mapView
	 * @param m
	 * @param o
	 * @param field
	 */
	public static void centerMapAndSetMarker(Context context,final GeoPoint geoPoint,MapView mapView,MarkerOverlay m,MultiSourceOverlayManager o){
		o.setMarkerVisible();
		DescribedMarker marker = new MarkerDTO(geoPoint.latitude, geoPoint.longitude,MarkerDTO.PIN_BLUE).createMarker(context);
		
		m.getOverlayItems().add(marker);
		//mc.selectMarker(marker);
		mapView.getMapViewPosition().setCenter(geoPoint);
	}
	/**
	 * check if a double is 0 which may fail for d == 0
	 * @param value
	 * @param threshold
	 * @return
	 */
	public static boolean isZero(double value, double threshold){
	    return value >= -threshold && value <= threshold;
	}
	
	/**
	 * Create a not editable text field
	 * @param f
	 * @param mFormView
	 * @param context
	 * @param missionFeature
	 */
	private static void addLabel(Field field, LinearLayout mFormView,Context context, MissionFeature feature) {
		//set label
		TextView tvLabel = getLabelForField(field, context);
		//set text
		TextView text = new TextView(context);
		text.setLayoutParams(getTextDefaultParams(field, false));
		if(field.lines!=null){
			text.setSingleLine(false);
			text.setLines(field.lines);
			text.setMaxLines(field.lines);
			
		}
		// setting an unique id is important in order to save the state
		// (content) of this view across screen configuration changes
		//int type = getInputType(field);
		text.setId(id());
		text.setTag(field.fieldId);
		//text.setInputType(type)
		if(feature.properties.get(field.fieldId) != null){			
			text.setText((String) feature.properties.get(field.fieldId));
		}
		mFormView.addView(tvLabel);
		mFormView.addView(text);
		
		
	}

	/**
	 * Creates a <Spinner>
	 * @param field
	 * @param mFormView
	 * @param context
	 * @param mission 
	 */
	private static void addSpinner(Field field, LinearLayout mFormView,
			Context context, MissionFeature feature) {
		TextView tvLabel = getLabelForField(field, context);
		String[] allowed = getFieldAllowedFrom(field);
		
		SpinnerAdapter adapter = new SimpleAdapter(
					context,
					getFieldAllowedData(field),
				 	android.R.layout.simple_spinner_dropdown_item,
				 	allowed, 
	        		new int[]{android.R.id.text1}
   	);
		Spinner spinner = new Spinner(context);
		
		
		//set initial value
		//THIS does nothing
//		if(feature.properties.get(field.fieldId) != null){		
//			String value = (String) feature.properties.get(field.fieldId);
//			List<HashMap<String, String>> maps = getFieldAllowedData(field);
//			if(value!=null && allowed != null && maps!=null){
//				int valueIndex=-1;
//				for(int i = 0;i<maps.size();i++){
//					if(value.equals(maps.get(i).get(allowed[0])));
//					valueIndex = i;
//				}
//				if(valueIndex>=0){
//					spinner.setSelection(valueIndex);
//				}
//			}
//		}
		LayoutParams lp = getTextDefaultParams(field, false);
		spinner.setLayoutParams(lp);
		// setting an unique id is important in order to save the state
		// (content) of this view across screen configuration changes
		spinner.setId(id());
		spinner.setTag(field.fieldId);
		mFormView.addView(tvLabel);
		mFormView.addView(spinner);
		spinner.setAdapter(adapter);
	}

	private static TextView getLabelForField(Field field, Context context) {
		String label = field.label;
		
		TextView tvLabel = new TextView(context);
		tvLabel.setLayoutParams(getTextDefaultParams(field,true));
		tvLabel.setText(label);
		String labelStyle = (String)field.getAttribute("labelStyle");
		if(labelStyle !=null){
			//TODO use style attribute
		}else{
			//default style for field label
			tvLabel.setTypeface(null, Typeface.BOLD|Typeface.ITALIC);
		}
		//if label is missing set none
		if(label == null){
			tvLabel.setVisibility(TextView.GONE);
		}
		return tvLabel;
	}
	/**
	 * Provides the attribute of the map to use in the spinner
	 * @param field
	 * @return
	 */
	private static String[] getFieldAllowedFrom(Field field) {
		//TODO make it configurable
		return new String[]{"f1"};
	}

	/**
	 * Provides the Data for the spinner
	 * @param field
	 * @return
	 */
	public static List<HashMap<String,String>> getFieldAllowedData(Field field){
		ArrayList<HashMap<String,String>> data = new ArrayList<HashMap<String,String>>();
		
		if(field.options!=null){
			for(String value : field.options){
				HashMap<String,String> v1 =new  HashMap<String,String>();
				v1.put("f1", value);
				data.add(v1);
			}
		}
		return data;
	}

	/**
	 * Creates a date picker
	 * 
	 * @param f
	 * @param mFormView
	 * @param context
	 * @param missionFeature
	 */
	private static void addDatePicker(Field field, LinearLayout mFormView,
			Context context, MissionFeature feature) {
		
		int type = getInputType(field);
		TextView tvLabel = getLabelForField(field, context);

		DatePicker editView = new DatePicker(context,null);
		LayoutParams lp = getTextDefaultParams(field, false);
		editView.setLayoutParams(lp);
		// setting an unique id is important in order to save the state
		// (content) of this view across screen configuration changes
		editView.setId(id());
		editView.setTag(field.fieldId);
		editView.setInputType(type);
		mFormView.addView(tvLabel);
		mFormView.addView(editView);
		if(feature.properties.get(field.fieldId) != null){		
			String value = (String) feature.properties.get(field.fieldId);
			//get the value
			if(value !=null){
				DateFormat df = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
				try{
					Calendar c = Calendar.getInstance(); 
					c.setTime(df.parse(value));
					editView.setDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE));
				}catch(ParseException e){
					Log.e("FORM_PARSER","unable to parse date:"+ value + "with format string" + field.format);
				}
			}
		}else{
			//set today
			Calendar c = Calendar.getInstance(); 
			c.setTime(new Date());
			editView.setDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE));
		}
	}

	/**
	 * Add an <EditText> multiline
	 * 
	 * @param field
	 *            the field configuration
	 * @param mFormView
	 *            the form view
	 * @param context
	 *            the context
	 */
	public static void addTextArea(Field field, LinearLayout mFormView,
			Context context) {

		int type = getInputType(field);
		TextView tvLabel = getLabelForField(field, context);
		EditText editView = new EditText(context);
		editView.setLayoutParams(getTextDefaultParams(field, false));
		editView.setSingleLine(false);
		editView.setLines(field.lines);
		
		// setting an unique id is important in order to save the state
		// (content) of this view across screen configuration changes
		editView.setId(id());
		editView.setInputType(type);
		mFormView.addView(tvLabel);
		mFormView.addView(editView);

	}

	/**
	 * Add a <EditText> single line
	 * 
	 * @param label
	 * @param type
	 * @param context
	 * @param missionFeature
	 */
	public static void addTextField(Field field, LinearLayout mFormView,
			Context context, MissionFeature feature) {
		int type = getInputType(field);
		TextView tvLabel = getLabelForField(field, context);
		EditText editView  = null;
		//set autocomplete if options present
		if(field.options!=null){
			editView =  addAutoCompleteField(field,mFormView,context);
		}else{
			editView = new EditText(context);
		}
		editView.setLayoutParams(getTextDefaultParams(field, false));
		// setting an unique id is important in order to save the state
		// (content) of this view across screen configuration changes
		editView.setId(id());
		editView.setTag(field.fieldId);
		editView.setInputType(type);
		if(feature.properties.get(field.fieldId) != null){			
			editView.setText((String) feature.properties.get(field.fieldId) );
		}
		
		final String mandatoryTag = field.mandatory ? " ("+mFormView.getContext().getString(R.string.mandatory)+")" : "";
		tvLabel.setText(tvLabel.getText()+mandatoryTag);
		
		mFormView.addView(tvLabel);
		mFormView.addView(editView);
	}
	/**
	 * Add a <EditText> single line
	 * 
	 * @param label
	 * @param type
	 * @param context
	 */
	public static AutoCompleteTextView addAutoCompleteField(Field field, LinearLayout mFormView,
			Context context) {

		
		AutoCompleteTextView editView = (AutoCompleteTextView) new AutoCompleteTextView(context);
		// Get the string array
		if(field.options !=null){
			ArrayAdapter<String> adapter = 
					new ArrayAdapter<String>(context,
			                 android.R.layout.simple_dropdown_item_1line, field.options);
			editView.setAdapter(adapter);
			editView.setLayoutParams(getTextDefaultParams(field, false));
		}
		return editView;
	}
	/**
	 * Add a <CheckBox> single line
	 * 
	 * @param label
	 * @param type
	 * @param context
	 * @param mission 
	 */
	public static void addCheckBox(Field field, LinearLayout mFormView,
			Context context, MissionFeature feature) {
		// setting an unique id is important in order to save the state
		// (content) of this view across screen configuration changes
		CheckBox cb = new CheckBox(context);
	    cb.setText(field.label);
	    cb.setLayoutParams(getTextDefaultParams(field, false));
	    cb.setId(id());
	    cb.setTag(field.fieldId);
	    
	    String selected = (String) feature.properties.get(field.fieldId);
	    
	    if(selected != null){
	    	int selection = Integer.parseInt(selected);
	    	cb.setChecked(selection > 0);
	    }
	    
		mFormView.addView(cb);
	}
	
	
	/**
	 * Generates the Android input Type from the text field
	 * 
	 * @param field
	 * @return
	 */
	private static int getInputType(Field field) {
		if (field.type == null) {
			return InputType.TYPE_CLASS_TEXT;
		} else {
			switch (field.type) {
			case person:
				//return InputType.TYPE_TEXT_VARIATION_PERSON_NAME; // TYPE_TEXT_VARIATION_PERSON_NAME seems to not be used by the framework
				return InputType.TYPE_TEXT_FLAG_CAP_WORDS;
			case phone:
				return InputType.TYPE_CLASS_PHONE;
			case decimal:
			case integer:
				return InputType.TYPE_CLASS_NUMBER;
			case date:
				return InputType.TYPE_CLASS_DATETIME
						| InputType.TYPE_DATETIME_VARIATION_DATE;
			case datetime:
				return InputType.TYPE_CLASS_DATETIME
						| InputType.TYPE_DATETIME_VARIATION_NORMAL;
			case string:
				
				return InputType.TYPE_CLASS_TEXT;
			default:
				break;
			}
		}

		return InputType.TYPE_CLASS_TEXT;

	}

	/**
	 * Provides default Layout Parameters
	 * 
	 * @param isLabel
	 * @return
	 */
	private static LayoutParams getTextDefaultParams(Field field,
			boolean isLabel) {
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		if (isLabel) {
			params.bottomMargin = 5;
			params.topMargin = 10;
			
		}
		params.leftMargin = 10;
		params.rightMargin =10;
		return params;
	}
	
	private static LayoutParams getMapLayoutParams (Field field){
		Double h = (Double) field.getAttribute("height");
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT);
		if(h!=null){
			params = new LayoutParams(LayoutParams.MATCH_PARENT,
					h.intValue());
		}else{
			params = new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
		}
		
		params.weight = 0.4f;
		params.bottomMargin = 20;
		params.topMargin = 20;
		params.leftMargin = 10;
		params.rightMargin =10;
		return params;
	}
	
	/**
	 * get the attribute of a field if present, the default value instead
	 * @param f the field
	 * @param attributeName the name of the attribute
	 * @param defaultValue the default value
	 * @return
	 */
	public static Object getAttributeWithDefault(Field f, String attributeName,Object defaultValue){
		Object o = f.getAttribute(attributeName);
		if(o==null){
			return defaultValue;
		}
		else{
			return o;
		}
	}
}

