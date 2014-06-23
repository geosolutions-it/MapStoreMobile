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
import it.geosolutions.geocollect.android.core.mission.Mission;
import it.geosolutions.geocollect.android.core.mission.utils.MissionUtils;
import it.geosolutions.geocollect.android.core.widgets.DatePicker;
import it.geosolutions.geocollect.android.core.widgets.UILImageAdapter;
import it.geosolutions.geocollect.model.viewmodel.Field;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.mapsforge.core.model.GeoPoint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.vividsolutions.jts.geom.Point;

/**
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 * 
 *         Utility Class to build forms
 */
public class FormBuilder {

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
	 * @param mission 
	 * @param t
	 *            the <Page> of the form
	 */
	public static void buildForm(Context context, LinearLayout mFormView, List<Field> fields, Mission mission) {
		if (fields == null) {
			return;
		}
		for (Field f : fields) {
			//the null field will be ignored
			if(f == null )continue;
			if (f.xtype == null) {
				//textfield as default
				addTextField(f, mFormView, context,mission);
			} else {
				// switch witch widget create
				switch (f.xtype) {
				case textfield:
					addTextField(f, mFormView, context,mission);
					break;
				case textarea:
					addTextField(f, mFormView, context,mission);
					break;
				case datefield:
					addDatePicker(f, mFormView, context,mission);
					break;
				case checkbox:
					addCheckBox(f,mFormView,context,mission);
					break;
				case spinner:
					addSpinner(f,mFormView,context,mission);
					break;
				case label:
					addLabel(f,mFormView,context,mission);
					break;
				case separator:
					addSeparator(f,mFormView,context,mission);
					break;
				case mapViewPoint:
					addMapViewPoint(f,mFormView,context,mission);
					break;
				case photo:
					addPhotoGrid(f, mFormView, context, mission);
					break;
				default:
					//textfield as default
					addTextField(f, mFormView, context,mission);
				}
			}
		}
		//mFormView.getParent().requestLayout();

	}

	/**
	 * Creates a map view
	 * @param f
	 * @param mFormView
	 * @param context
	 * @param mission
	 */
	private static void addMapViewPoint(Field field, LinearLayout mFormView,
			Context context, Mission mission) {
		TextView tvLabel = getLabelForField(field, context);
		AdvancedMapView mapView =null;
		//setup view
		//ImageButton infoButton = new ImageButton(context);
		//infoButton.setImageResource(R.drawable.ic_menu_info_details); //TODO move icon to main project
		//infoButton.setVisibility(ImageButton.GONE);
		//infoButton.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
		//TODO find a mapview in layout to provide a better GUI
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
		
		//setup overlay Manager
		MultiSourceOverlayManager o = new MultiSourceOverlayManager(mapView);
		mapView.setOverlayManger(o);
		MarkerOverlay m = new MarkerOverlay();
		o.setMarkerOverlay(m);
		m.setProjection(mapView.getProjection());
		// only from tag is supported
		List<String> tags = MissionUtils.getTags(field.value);
		GeoPoint geoPoint = null;
		if(tags!=null && tags.size() ==1){
			//Get geometry now geoPoint only supported)
			//TODO support for different formats
			Point geom = (Point) mission.getValueByTag(tags.get(0));
			if(geom !=null){
				if(!geom.isEmpty()){
					double lat = geom.getY();
					double lon = geom.getX();
					geoPoint = new GeoPoint(lat, lon);
				}
				
			}
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

		if(geoPoint!=null){
			o.setMarkerVisible();
			DescribedMarker marker = new MarkerDTO(geoPoint.latitude, geoPoint.longitude,MarkerDTO.MARKER_BLUE).createMarker(context);
			marker.setDescription(mission.getValueAsString(field, (String)field.getAttribute("description")));
			
			m.getOverlayItems().add(marker);
			//mc.selectMarker(marker);
			mapView.getMapViewPosition().setCenter(geoPoint);
		}
	}

	/**
	 * Create an Header with separator
	 * @param f
	 * @param mFormView
	 * @param context
	 * @param mission 
	 */
	private static void addSeparator(Field field, LinearLayout mFormView,
			Context context, Mission mission) {
		String label = mission.getValueAsString(field, field.label);
		
		TextView tvLabel = new TextView(context,null,android.R.attr.listSeparatorTextViewStyle);
		tvLabel.setLayoutParams(getTextDefaultParams(field, true));
		tvLabel.setText(label);
		mFormView.addView(tvLabel);
		
	}

	
	/**
	 * Create a not editable text field
	 * @param f
	 * @param mFormView
	 * @param context
	 * @param mission 
	 */
	private static void addLabel(Field field, LinearLayout mFormView,
			Context context, Mission mission) {
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
		//text.setInputType(type);
		text.setText( mission.getValueAsString(field));//TODO make it parameterizable
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
			Context context, Mission mission) {
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
		String value = mission.getValueAsString(field);
		if(value!=null && allowed!=null){
			int valueIndex=-1;
			for(int i = 0;i<allowed.length;i++){
				if(value.equals(allowed[i]));
				valueIndex = i;
			}
			if(valueIndex>=0){
				spinner.setSelection(valueIndex);
			}
		}
		
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
	 * @param mission 
	 */
	private static void addDatePicker(Field field, LinearLayout mFormView,
			Context context, Mission mission) {
		
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
		String value = mission.getValueAsString(field);
		//get the value
		if(value !=null){
			DateFormat df = new SimpleDateFormat(field.format, Locale.getDefault());
			try{
			Calendar c = Calendar.getInstance(); 
			c.setTime(df.parse(value));
			editView.setDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE));
			}catch(ParseException e){
				Log.e("FORM_PARSER","unable to parse date:"+ value + "with format string" + field.format);
			}
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
	 * @param mission 
	 */
	public static void addTextField(Field field, LinearLayout mFormView,
			Context context, Mission mission) {
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
		
		editView.setText( mission.getValueAsString(field));//TODO make it parameterizable
		
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
			Context context, Mission mission) {
		// setting an unique id is important in order to save the state
		// (content) of this view across screen configuration changes
		CheckBox cb = new CheckBox(context);
	    cb.setText(field.label);
	    cb.setLayoutParams(getTextDefaultParams(field, false));
	    cb.setId(id());
	    cb.setTag(field.fieldId);
		mFormView.addView(cb);
	}
	
	/**
	 * Create an Header with GridView
	 * @param f
	 * @param mFormView
	 * @param context
	 * @param mission 
	 */
	private static void addPhotoGrid(Field field, LinearLayout mFormView,
			Context context, Mission mission) {
		// TODO: enable label?
		//TextView tvLabel = getLabelForField(field, context);
		// TODO: Null check on this line
		GridView photoView = (GridView) ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.ac_image_grid, mFormView, false);;
		photoView.setLayoutParams(getTextDefaultParams(field, true));
		//photoView.setTag(field.fieldId); // TODO: useless, the photoView must be used to fetch from a folder derived from the mission id
		// TODO: use a non-string tag
		photoView.setTag("__photo__");
		
		//***************************
		// Test config
		/*
		String[] stringUrls = new String[] {
				"http://cdn.urbanislandz.com/wp-content/uploads/2011/10/MMSposter-large.jpg", // Very large image
				"http://4.bp.blogspot.com/-LEvwF87bbyU/Uicaskm-g6I/AAAAAAAAZ2c/V-WZZAvFg5I/s800/Pesto+Guacamole+500w+0268.jpg", // Image with "Mark has been invalidated" problem
				"file:///sdcard/Universal Image Loader @#&=+-_.,!()~'%20.png", // Image from SD card with encoded symbols
				"http://cdn.urbanislandz.com/wp-content/uploads/2011/10/MMSposter-large.jpg", // Very large image
				"http://4.bp.blogspot.com/-LEvwF87bbyU/Uicaskm-g6I/AAAAAAAAZ2c/V-WZZAvFg5I/s800/Pesto+Guacamole+500w+0268.jpg", // Image with "Mark has been invalidated" problem
				"file:///sdcard/Universal Image Loader @#&=+-_.,!()~'%20.png" // Image from SD card with encoded symbols
				
		};
		*/

	    String[] stringUrls = FormUtils.getPhotoUriStrings(mission.getOrigin().id);

		DisplayImageOptions options = new DisplayImageOptions.Builder()
		.showImageOnLoading(it.geosolutions.geocollect.android.core.R.drawable.ic_launcher)
		.showImageForEmptyUri(it.geosolutions.geocollect.android.core.R.drawable.ic_empty)
		.showImageOnFail(it.geosolutions.geocollect.android.core.R.drawable.ic_error)
		.resetViewBeforeLoading(false)
        
		.cacheInMemory(false)
		.cacheOnDisk(false)
		.considerExifParams(true)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.imageScaleType(ImageScaleType.EXACTLY)
		.build();
		
		//***************************
		
		photoView.setAdapter(new UILImageAdapter(context, stringUrls, options));
		// TODO: enable when the Activity exists
		/*
		photoView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				startImagePagerActivity(position);
			}
		});*/
		
		// TODO: enable label?
		//mFormView.addView(tvLabel);
		mFormView.addView(photoView);
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
