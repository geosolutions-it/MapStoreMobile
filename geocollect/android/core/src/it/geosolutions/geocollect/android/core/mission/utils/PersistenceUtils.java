/**
 * 
 */
package it.geosolutions.geocollect.android.core.mission.utils;

import java.util.HashMap;
import java.util.List;

import org.mapsforge.core.model.GeoPoint;

import com.vividsolutions.jts.geom.Point;

import it.geosolutions.android.map.dto.MarkerDTO;
import it.geosolutions.android.map.overlay.MarkerOverlay;
import it.geosolutions.android.map.overlay.items.DescribedMarker;
import it.geosolutions.android.map.view.AdvancedMapView;
import it.geosolutions.geocollect.android.core.form.utils.FormBuilder;
import it.geosolutions.geocollect.android.core.mission.Mission;
import it.geosolutions.geocollect.android.core.widgets.DatePicker;
import it.geosolutions.geocollect.model.viewmodel.Field;
import it.geosolutions.geocollect.model.viewmodel.Page;
import it.geosolutions.geocollect.model.viewmodel.type.XType;
import jsqlite.Database;
import jsqlite.Exception;
import jsqlite.Stmt;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Utils class to store and retrieve data from SQLite database
 * @author Lorenzo Pini lorenzo.pini@geo-solutions.it
 *
 */
public class PersistenceUtils {

	/**
	 * Tag for logging
	 */
	public static String TAG = "PersistanceUtils";
	
	/**
	 * Default method for storePageData
	 * Stores the view data on given database based on given Page information
	 */
	public static boolean storePageData(Page page, LinearLayout layout, Mission mission){
		return storePageData(page, layout, mission, "punti_accumulo_data");
	}
	
	/**
	 * Stores the view data on given database based on given Page information
	 * @return
	 */
	public static boolean storePageData(Page page, LinearLayout layout, Mission mission, String tableName){
		
		if(tableName == null || tableName.equalsIgnoreCase("")){
			Log.w(TAG, "Empty tableName, aborting...");
			return false;
		}
		
		if(mission.db != null){
			// the database exists but is closed
			if(mission.db.dbversion().equals("unknown")){
				Log.w(TAG, "Database is already closed, aborting...");
				return false;
			}
			
			String s;
			String value;
			Stmt st = null;
			for(Field f : page.fields){
				if(f == null )continue;

				View v = layout.findViewWithTag(f.fieldId);

				if(v == null){
					Log.w(TAG, "Tag not found : "+f.fieldId);
					continue;
				}
				
				if (f.xtype == null) {
					// TODO: load all the fields in one query
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
					case mapViewPoint:
						// TODO Investigate performance of this check
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
								value = "MakePoint("+g.latitude+","+g.longitude+", 4326)";
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
				try {	
					if(f.xtype == XType.mapViewPoint){
						// a geometry must be built
						s = "UPDATE 'punti_accumulo_data' SET "+ f.fieldId +" = "+ value +" WHERE ORIGIN_ID = '"+mission.getOrigin().id+"';";
					}else{
						// Standard values
						value = value.replace("'", "''");
						s = "UPDATE 'punti_accumulo_data' SET "+ f.fieldId +" = '"+ value +"' WHERE ORIGIN_ID = '"+mission.getOrigin().id+"';";
					}
					Log.v(TAG, "Query :\n"+s);
					if(Database.complete(s)){
						st = mission.db.prepare(s);
						if(st.step()){
							//Log.v(TAG, "Updated");
						}else{
							// useless check, the step on an update returns zero rows
							//Log.v(TAG, "Update failed");
						}
					}else{
						Log.w(TAG, "Skipping non complete statement:\n"+s);
					}
					
				} catch (Exception e) {
					Log.e(TAG, Log.getStackTraceString(e));
					return false;
				}
			}
			if(st!=null){
				try {
					st.close();
				} catch (Exception e) {
					//Log.e(TAG, Log.getStackTraceString(e));
					// ignore
				}
			}
			return true;
		}else{
			Log.w(TAG, "Database not found, aborting...");
			return false;
		} // if db
		
	}
	
	/**
	 * Default method for loadPageData
	 */
	public static boolean loadPageData(Page page, LinearLayout layout, Mission mission, Context context){
		return loadPageData(page, layout, mission, context, "punti_accumulo_data");
	}
	
	
	/**
	 * Load the page data from the give database
	 * @return
	 */
	public static boolean loadPageData(Page page, LinearLayout layout, Mission mission, Context context, String tableName){
		
		
		if(mission.db != null){
			
			// the database exists but is closed
			if(mission.db.dbversion().equals("unknown")){
				Log.w(TAG, "Database is already closed, aborting...");
				return false;
			}
			
			String s;
			Stmt st = null;
			for(Field f : page.fields){
				if(f == null )continue;
				try {
					// TODO: load all the fields in one query
					if(f.xtype == XType.mapViewPoint){
						// a point must be retreived
						s = "SELECT X("+ f.fieldId +"), Y("+ f.fieldId +") FROM '"+tableName+"' WHERE ORIGIN_ID = '"+mission.getOrigin().id+"';";
					}else{
						s = "SELECT "+ f.fieldId +" FROM '"+tableName+"' WHERE ORIGIN_ID = '"+mission.getOrigin().id+"';";
					}
					if(jsqlite.Database.complete(s)){
						st = mission.db.prepare(s);
						if(st.step()){
							View v = layout.findViewWithTag(f.fieldId);

							if (f.xtype == null) {
								//textfield as default
								((TextView)v).setText(st.column_string(0));
							} else {
								switch (f.xtype) {
								case textfield:
									((TextView)v).setText(st.column_string(0));
									break;
								case textarea:
									((TextView)v).setText(st.column_string(0));
									break;
								case datefield:
									if(st.column_string(0) != null){
										Log.v(TAG, "Setting date :"+st.column_string(0));
										((DatePicker)v).setDate(st.column_string(0));
									}
									break;
								case checkbox:
									if(st.column_string(0) != null){
										Log.v(TAG, "Setting checkbox value :"+st.column_string(0));
										((CheckBox)v).setChecked(st.column_string(0).equals("1"));
									}
									break;
								case spinner:
									if(st.column_string(0) != null){
										Log.v(TAG, "Setting spinner value :"+st.column_string(0));
										String fieldValue = st.column_string(0);
										List<HashMap<String, String>> l = FormBuilder.getFieldAllowedData(f);
										int i = 0;
										for( i = 0; i<l.size(); i++){
											if(l.get(i).get("f1") != null && l.get(i).get("f1").equals(fieldValue)){
												((Spinner)v).setSelection(i);
											}
										}
									}
									break;
								case label:
									// skip
									break;
								case separator:
									// skip
									break;
								case mapViewPoint:
									if(v != null){
										Log.v(TAG, "Setting Point value :"+st.column_double(0)+" , "+st.column_double(1));
										if(st.column_double(0)+st.column_double(1)==0){
											Log.v(TAG, "Skipping zero coordinates on "+f.fieldId);
											break;
										}
										
										// do we need to show the origin point?
										boolean displayOriginalValue = false;
										if(f.getAttribute("displayOriginalValue")!=null){
											try{
												displayOriginalValue = (Boolean)f.getAttribute("displayOriginalValue");
											}catch(ClassCastException cce){
												Log.w(TAG, "Cannot cast displayOriginalValue to Boolean");
											}
										}
										
										GeoPoint geoPoint = new GeoPoint(st.column_double(0), st.column_double(1));
										if(geoPoint!=null){

											AdvancedMapView amv = ((AdvancedMapView)v);
											MarkerOverlay mo = amv.getMarkerOverlay();
											
											// Remove existing markers
											mo.getOverlayItems().removeAll(mo.getOverlayItems());
											mo.getMarkers().removeAll(mo.getMarkers());

											if(displayOriginalValue){
												// only from tag is supported
												List<String> tags = MissionUtils.getTags(f.value);
												GeoPoint origin_geoPoint = null;
												if(tags!=null && tags.size() ==1){
													//Get geometry now geoPoint only supported)
													//TODO support for different formats
													Point geom = (Point) mission.getValueByTag(tags.get(0));
													if(geom !=null){
														if(!geom.isEmpty()){
															double lat = geom.getY();
															double lon = geom.getX();
															origin_geoPoint = new GeoPoint(lat, lon);
														}
													}
												}

												// Add new marker based on geopoint
												DescribedMarker origin_marker = new MarkerDTO(origin_geoPoint.latitude, origin_geoPoint.longitude,MarkerDTO.MARKER_BLUE).createMarker(context);
												mo.getOverlayItems().add(origin_marker);
											}
											// TODO : If displayOriginalValue is true, the map should be read-only to prevent mismatch saving marker position on database
											
											// Add new marker based on geopoint
											DescribedMarker marker = new MarkerDTO(geoPoint.latitude, geoPoint.longitude,MarkerDTO.MARKER_BLUE).createMarker(context);
											mo.getOverlayItems().add(marker);
											//mc.selectMarker(marker);
											// center map on marker
											if(((AdvancedMapView)v).getMapViewPosition() != null){
												((AdvancedMapView)v).getMapViewPosition().setCenter(geoPoint);
											}
										}
									}else{
										Log.v(TAG, "Cannot find MapView "+f.fieldId);
									}
									
									break;
								default:
									//textfield as default
									((TextView)v).setText(st.column_string(0));
								}
							}							
						}else{
							// no record found, creating..
							Log.v(TAG, "No record found, creating..");
							s = "INSERT INTO '"+tableName+"' ( ORIGIN_ID ) VALUES ( '"+mission.getOrigin().id+"');";
							st = mission.db.prepare(s);
							if(st.step()){
								// nothing will be returned anyway
							}
							Log.v(TAG, "Record created");

						}
						st.close();
					}
				} catch (Exception e) {
					Log.e(TAG, Log.getStackTraceString(e));
				}
			}
			if(st!=null){
				try {
					st.close();
				} catch (Exception e) {
					//Log.e(TAG, Log.getStackTraceString(e));
					// ignore
				}
			}
			
			return true;
			
		}else{
			Log.w(TAG, "Database not found, aborting...");
			return false;
		} // if db
		
	}
	
}
