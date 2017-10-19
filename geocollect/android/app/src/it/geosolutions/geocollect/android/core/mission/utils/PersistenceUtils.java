/**
 * 
 */
package it.geosolutions.geocollect.android.core.mission.utils;

import static it.geosolutions.geocollect.android.core.mission.utils.SpatialiteUtils.populateFeatureFromStmt;
import it.geosolutions.android.map.dto.MarkerDTO;
import it.geosolutions.android.map.overlay.MarkerOverlay;
import it.geosolutions.android.map.overlay.items.DescribedMarker;
import it.geosolutions.android.map.view.AdvancedMapView;
import it.geosolutions.android.map.wfs.geojson.feature.Feature;
import it.geosolutions.geocollect.android.app.BuildConfig;
import it.geosolutions.geocollect.android.app.R;
import it.geosolutions.geocollect.android.core.form.utils.FormBuilder;
import it.geosolutions.geocollect.android.core.mission.Mission;
import it.geosolutions.geocollect.android.core.mission.MissionFeature;
import it.geosolutions.geocollect.android.core.widgets.DatePicker;
import it.geosolutions.geocollect.model.config.MissionTemplate;
import it.geosolutions.geocollect.model.source.XDataType;
import it.geosolutions.geocollect.model.viewmodel.Field;
import it.geosolutions.geocollect.model.viewmodel.Form;
import it.geosolutions.geocollect.model.viewmodel.Page;
import it.geosolutions.geocollect.model.viewmodel.type.XType;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import jsqlite.Database;
import jsqlite.Exception;
import jsqlite.Stmt;

import org.mapsforge.core.model.GeoPoint;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKBReader;

import eu.geopaparazzi.spatialite.database.spatial.core.GeometryType;

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
	
	private static final String DOWNLOADED_TEMPLATES = "it.geosolutions.geocollect.downloaded_templates";
	private static final String UPLOADABLE_ENTRIES   = "it.geosolutions.geocollect.uploadable_entries";
	
	/**
	 * wrapper method which handles the complete database integration/update for a MissionTemplate
	 * @param t the template to integrate
	 * @param spatialiteDatabase the database to use
	 * @return true if the operation was successful, false otherwise
	 */
	
	public static boolean createOrUpdateTablesForTemplate(final MissionTemplate t, final Database spatialiteDatabase){
		
		boolean success = true;
		if(t != null && t.id != null){
			
			//incomplete schema check
			if(t.schema_sop == null || t.schema_seg == null || t.schema_seg.localSourceStore == null || t.schema_sop.localFormStore == null){
				Log.w(TAG, "incomplete MissionTemplate schema, cannot create/update tables");
				return false;
			}
			
			//1. "create mission" table
			
			if(!PersistenceUtils.createTableFromTemplate(spatialiteDatabase, t.schema_seg.localSourceStore+ MissionTemplate.NEW_NOTICE_SUFFIX, t.schema_seg.fields)){
				Log.e(TAG, "error creating \"create_mission\" table ");
				success = false;
			}

			//2.  "source" table

			if(!PersistenceUtils.createOrUpdateTable(spatialiteDatabase,t.schema_seg.localSourceStore, t.schema_seg.fields)){
				Log.e(TAG, "error creating "+t.schema_seg.localSourceStore+" table ");
				success = false;
			}
			
			//3. "form" -> rilevamenti table

			if(!PersistenceUtils.createOrUpdateTable(spatialiteDatabase,t.schema_sop.localFormStore, t.schema_sop.fields)){
				Log.e(TAG, "error creating "+t.schema_sop.localFormStore+" table ");
				success = false;
			}

			Log.d(TAG, "Loaded template ID: "+t.id);
		}else{
			Log.w(TAG, "MissionTemplate is not valid, skipping...");
			success = false;
		}
		return success;
	}
	
	public static boolean createOrUpdateTable(final Database spatialiteDatabase, final String pTableName,final HashMap<String, XDataType> hm){
		
        
        	// default value
        	String tableName = pTableName+"_data";
        	if(pTableName != null && !pTableName.isEmpty()){
        		tableName = pTableName;
        	}
            if(PersistenceUtils.createTableFromTemplate(spatialiteDatabase, tableName, hm)){
//            if(SpatialiteUtils.checkOrCreateTable(spatialiteDatabase, t.id+"_data")){
	            Log.v(TAG, "Table Found, checking for schema updates");
	            if(PersistenceUtils.updateTableFromTemplate(spatialiteDatabase, tableName, hm)){
	            	Log.v(TAG, "All good");
	            	return true;
	            }else{
	            	Log.w(TAG, "Something went wrong during the update, the data can be inconsistent");
	            	return false;
	            }
	            
            }else{
	            Log.w(TAG, "Table could not be created, edits will not be saved");
	            return false;
            }

	}
	
	/**
	 * Default method for storePageData
	 * Stores the view data on given database based on given Page information
	 */
	public static boolean storePageData(Page page, LinearLayout layout, Mission mission){
		if(mission == null || mission.getTemplate() == null){
			Log.w(TAG, "Mission or MissionTemplate could not be found, abort saving..");
			return false;
		}

    	// default value
    	String tableName = mission.getTemplate().id + MissionTemplate.DEFAULT_TABLE_DATA_SUFFIX;
    	if(mission.getTemplate().schema_sop != null 
    			&& mission.getTemplate().schema_sop.localFormStore != null
    			&& !mission.getTemplate().schema_sop.localFormStore.isEmpty()){
    		tableName = mission.getTemplate().schema_sop.localFormStore;
    	}
		return storePageData(page, layout, mission, tableName);
	}
	
	/**
	 * Stores the view data on given database based on given Page information
	 * @return
	 */
	public static boolean storePageData(Page page, LinearLayout layout, Mission mission, String tableName){
		
		if(tableName == null || tableName.isEmpty()){
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
			String value = null;
			Stmt st = null;
			Boolean ignoreField = false;
			for(Field f : page.fields){
				if(f == null )continue;

				View v = layout.findViewWithTag(f.fieldId);

				if(v == null){
					Log.w(TAG, "Tag not found : "+f.fieldId);
					continue;
				}
				
				ignoreField = false;
				
				if (f.xtype == null) {
					// TODO: load all the fields in one query
					value = ((TextView)v).getText().toString();
				} else {
				    try {
    					// switch witch widget create
    					switch (f.xtype) {
    					case textfield:
    						value = ((TextView)v).getText().toString();
    						if(f.type == XDataType.integer
					        || f.type == XDataType.decimal
                            || f.type == XDataType.real){
    						    if(value.isEmpty()){
    						        value = null;
    						    }
    						}
    						
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
    					case photo:
    						// skip
    						continue;
    						//break;
    					case mapViewPoint:
    						if(!(Boolean)FormBuilder.getAttributeWithDefault(f,"editable",true)){
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
				    }catch(ClassCastException cce){
				        if(BuildConfig.DEBUG){
				            Log.e(TAG, "Configuration Error, maybe you set two fields with the same fieldIs ?");
				            ignoreField = true;
				            continue;
				        }
				    }
				}
				if(!ignoreField){
    				try {	
    					
    					String originIDString = MissionUtils.getMissionGCID(mission);
    					
    					if(f.xtype == XType.mapViewPoint){
    						// a geometry must be built
    						s = "UPDATE '"+tableName+"' SET "+ f.fieldId +" = "+ value +" WHERE "+ Mission.ORIGIN_ID_STRING +" = '"+originIDString+"';";
    					}else{
    					    if(value != null){
        						// Standard values
        						value = value.replace("'", "''");
        						s = "UPDATE '"+tableName+"' SET "+ f.fieldId +" = '"+ value +"' WHERE "+ Mission.ORIGIN_ID_STRING +" = '"+originIDString+"';";
    					    }else{
    					        s = "UPDATE '"+tableName+"' SET "+ f.fieldId +" = NULL WHERE "+ Mission.ORIGIN_ID_STRING +" = '"+originIDString+"';";
    					    }
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
	public static Feature loadFeatureById(Mission mission){
		
		if(mission == null || mission.getTemplate() == null){
			Log.w(TAG, "Mission or MissionTemplate could not be found, abort loading..");
			return null;
		}

    	// default value
    	String tableName = mission.getTemplate().schema_sop.localFormStore;
    	//geometry needs ST_AsBinary(CastToXY(GEOMETRY)) AS GEOMETRY
    	HashMap<String, String> columns = SpatialiteUtils.getPropertiesFields(mission.db,tableName);

    	List<String> selectFields = new ArrayList<String>();
    	for(String columnName : columns.keySet()){
    		//Spatialite custom field point
    		if("point".equalsIgnoreCase(columns.get(columnName))){
    			selectFields.add("ST_AsBinary(CastToXY(" + columnName + ")) AS GEOMETRY");
    		}else{
    			selectFields.add(columnName);
    		}
    	}
    	
    	String originIDString = MissionUtils.getMissionGCID(mission);
	
    	String selectString = TextUtils.join(",",selectFields);
    	StringWriter queryWriter = new StringWriter();
    	queryWriter.append("SELECT ")
    		.append(selectString)
    		.append(" FROM ")
    		.append(tableName)
    		.append(" WHERE '").append( Mission.ORIGIN_ID_STRING ).append("' = '")
    		.append(originIDString)
    		.append("';");
    	
    	
    	String query = queryWriter.toString();
    	List<Feature> fl = loadFormFeature(mission, tableName, query);
    	if(fl!=null && fl.size() == 1){
    		return fl.get(0);
    	}
    	return null;
	}
	/**
	 * Create a Feature from the query
	 * @param m mission
	 * @param wkbReader
	 * @param query
	 */
	private static List<Feature> loadFormFeature(Mission m, String tableName, String query) {
		Stmt stmt;
		List<Feature> mData = new ArrayList<Feature>();
		if(Database.complete(query)){
			
		    try {
		    	if(BuildConfig.DEBUG){
		    		Log.i(TAG, "Loading from query: "+query);
		    	}
		    	stmt = m.db.prepare(query);
		    	
		    	WKBReader wkbReader = new WKBReader();
		        MissionFeature f;
		        while( stmt.step() ) {
		            f = new MissionFeature();
		        	populateFeatureFromStmt(wkbReader, stmt, f);
		        	f.typeName = tableName;
		            mData.add(f);
		        }
		        stmt.close();
		        			            
		    } catch (Exception e) {
		        Log.e(TAG, Log.getStackTraceString(e));
		    }
		    
		}else{
			if(BuildConfig.DEBUG){
	    		Log.w(TAG, "Query is not complete: "+query);
			}
		}
		return mData;
	}
	/**
	 * Default method for loadPageData
	 * @param createIfNotPresent create a entry in the table if the requested entry is not found
	 * this will cause the entry to show up as "inEditing"
	 */
	public static boolean loadPageData(Page page, LinearLayout layout, Mission mission, Context context,boolean createIfNotPresent){
		if(mission == null || mission.getTemplate() == null){
			Log.w(TAG, "Mission or MissionTemplate could not be found, abort loading..");
			return false;
		}

    	// default value
    	String tableName = mission.getTemplate().id + MissionTemplate.DEFAULT_TABLE_DATA_SUFFIX;
    	if(mission.getTemplate().schema_sop != null 
    			&& mission.getTemplate().schema_sop.localFormStore != null
    			&& !mission.getTemplate().schema_sop.localFormStore.isEmpty()){
    		tableName = mission.getTemplate().schema_sop.localFormStore;
    	}
		return loadPageData(page, layout, mission, context, tableName,createIfNotPresent);
	}
	
	public static void loadSpinnerData(Page page, LinearLayout layout, Database db, Context context, String tableName, String origin_id){
		
		for(Field f : page.fields){
			try {
				if(f.xtype == XType.spinner){
					String s = "SELECT " + f.fieldId +" FROM '" + tableName + "' WHERE "+ Mission.ORIGIN_ID_STRING +" = '" + origin_id+"';";
					Stmt st = db.prepare(s);
					if(st.step()){
						final View v = layout.findViewWithTag(f.fieldId);
						if(st.column_string(0) != null && v != null){
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
					}
				}
			} catch (Exception e) {
				Log.e(TAG, "Error setting spinner values",e);
			}
		}
		
	}
	/**
	 * Load the page data from the give database
	 * @param createIfNotPresent create a entry in the table if the requested entry is not found
	 * this will cause the entry to show up as "inEditing"
	 * @return
	 */
	public static boolean loadPageData(Page page, LinearLayout layout, Mission mission, Context context, String tableName, boolean createIfNotPresent){
		
		
		if(mission.db != null){
			
			// the database exists but is closed
			if(mission.db.dbversion().equals("unknown")){
				Log.w(TAG, "Database is already closed, aborting...");
				return false;
			}
			
			String s;
			Stmt st = null;
			ArrayList<String> addedFields = new ArrayList<String>();
			for(Field f : page.fields){
				
			    if( f == null || addedFields.contains(f.fieldId) )
				{
				    continue;
			    }
				
				try {
					// TODO: load all the fields in one query
					String originIDString = MissionUtils.getMissionGCID(mission);					
					if(f.xtype == XType.mapViewPoint){
						// a point must be retreived
						s = "SELECT Y( GEOMETRY ), X( GEOMETRY ) FROM '" + tableName + "' WHERE "+ Mission.ORIGIN_ID_STRING +" = '" + originIDString +"';";
					}else{
						s = "SELECT " + f.fieldId +" FROM '" + tableName + "' WHERE "+ Mission.ORIGIN_ID_STRING +" = '" + originIDString +"';";
					}
					if(jsqlite.Database.complete(s)){
						st = mission.db.prepare(s);
						if(st.step()){
							View v = layout.findViewWithTag(f.fieldId);

							if (f.xtype == null) {
								//textfield as default
								if(st.column_string(0) != null && v != null)
									((TextView)v).setText(st.column_string(0));
							} else {
								switch (f.xtype) {
								case textfield:
									if(st.column_string(0) != null && v != null)
										((TextView)v).setText(st.column_string(0));
									break;
								case textarea:
									if(st.column_string(0) != null && v != null)
										((TextView)v).setText(st.column_string(0));
									break;
								case datefield:
									if(st.column_string(0) != null && v != null){
										Log.v(TAG, "Setting date :"+st.column_string(0));
										((DatePicker)v).setDate(st.column_string(0));
									}
									break;
								case checkbox:
									if(st.column_string(0) != null && v != null){
										Log.v(TAG, "Setting checkbox value :"+st.column_string(0));
										((CheckBox)v).setChecked(st.column_string(0).equals("1"));
									}
									break;
								case spinner:
									if(st.column_string(0) != null && v != null){
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
									if(st.column_string(0) != null && v != null){
										((TextView)v).setText(st.column_string(0));
									}
									break;
								case separator:
									// skip
									break;
								case photo:
									// skip
									break;
								case mapViewPoint:
									if(v != null){
										Log.v(TAG, "Setting Point value :"+st.column_double(0)+" , "+st.column_double(1));
										
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
											
											GeoPoint origin_geoPoint = null;

											// only from tag is supported
											List<String> tags = MissionUtils.getTags("${origin."+mission.getOrigin().geometry_name+"}");
											
											if(tags!=null && tags.size() ==1){
												//Get geometry now geoPoint only supported)
												//TODO support for different formats
												Point geom = (Point) mission.getValueByTag(context, tags.get(0));
												if(geom !=null){
													if(!geom.isEmpty()){
														double lat = geom.getY();
														double lon = geom.getX();
														origin_geoPoint = new GeoPoint(lat, lon);
													}
												}
											}

											// Add new marker based on geopoint
											//DescribedMarker origin_marker = new MarkerDTO(origin_geoPoint.latitude, origin_geoPoint.longitude,MarkerDTO.PIN_BLUE).createMarker(context);
											//mo.getOverlayItems().add(origin_marker);
											
											if(geoPoint.latitude + geoPoint.longitude==0){
											    if(origin_geoPoint != null && origin_geoPoint.latitude+origin_geoPoint.longitude != 0){
											        Log.v(TAG, "Zero coordinates on "+f.fieldId+". " +
											        		"Setting to original values: "+origin_geoPoint.latitude+" , "+origin_geoPoint.longitude);
											        geoPoint = origin_geoPoint;
											    }else{
											        Log.v(TAG, "No coordinates found, skipping.");
											        break;
											    }
											}
											
											// Add new marker based on geopoint
											DescribedMarker marker = new MarkerDTO(geoPoint.latitude, geoPoint.longitude,MarkerDTO.PIN_RED).createMarker(context);
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
									if(st.column_string(0) != null && v != null)
										((TextView)v).setText(st.column_string(0));
								}
							}							
						}else if(createIfNotPresent){
							// no record found, creating..
							Log.v(TAG, "No record found, creating..");
							//This causes the write of ORIGIN_ID which will lead to "inediting" in pendingmissionlist
							s = "INSERT INTO '"+tableName+"' ( "+ Mission.ORIGIN_ID_STRING +" , MY_ORIG_ID ) VALUES ( '"+ originIDString +"' , '"+ originIDString +"');";
							st = mission.db.prepare(s);
							if(st.step()){
								// nothing will be returned anyway
							}
							Log.v(TAG, "Record created with query:\n"+s);

						}
						st.close();
						
						addedFields.add(f.fieldId);
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
	
	/**
	 * Lists all the fields with the associated field type contained in the give MissionTemplate
	 * or null if no MissionTemplate, no Form or can be found
	 * @param mt
	 * @return
	 */
	public static HashMap<String,XDataType> getTemplateFieldsList(Form form){

		// TODO: what if the same field name is found, but with different type?
		
		if(form == null){
			Log.v(TAG, "Form not found");
			return null;
		}
		
		HashMap<String,XDataType> fieldsList = new HashMap<String, XDataType>();
		
		if(form.pages != null && form.pages.size()>0 ){
			
			for(Page p : form.pages){
				if (p.fields != null & p.fields.size()>0){
					
					for(Field f : p.fields){
						
						// Add only valid couples "fieldId":"type"
						if(f.fieldId != null 
								&& !f.fieldId.isEmpty()
								&& f.type != null
								&& !fieldsList.containsKey(f.fieldId)){
							
							fieldsList.put(f.fieldId, f.type);
							
						}
						
					}
					
				}
			}
			
		}
		
		return fieldsList;
		
	}
	/**
	 * Creates a table with the given tableName and data types in the given db
	 * Does not convert the table if already exists
	 * with default geometry POINT_XY
	 * @param db
	 * @param string
	 * @param templateDataTypes
	 */
	public static boolean createTableFromTemplate(Database db, String tableName,
			HashMap<String, XDataType> templateDataTypes) {
		return createTableFromTemplate(db, tableName, templateDataTypes, false, GeometryType.POINT_XY);
	}
	
	/**
	 * Creates a table with the given tableName and data types in the given db
	 * Does not convert the table if already exists
	 * @param db
	 * @param string
	 * @param templateDataTypes
	 * @param geometryType the type geometry for the spatial column of the table 
	 */
	public static boolean createTableFromTemplate(Database db, String tableName,
			HashMap<String, XDataType> templateDataTypes, GeometryType geometryType) {
		return createTableFromTemplate(db, tableName, templateDataTypes, false, geometryType);
	}
	
	/**
	 * Creates a table with the given tableName and data types in the given db
	 * If the table already exists and convertIfNeeded is true, tries to edit the table to match the given template
	 * @param db
	 * @param string
	 * @param templateDataTypes
	 * @param convertIfNeeded
	 * @param geometryType the type geometry for the spatial column of the table 
	 */
	public static boolean createTableFromTemplate(Database db, String tableName,
			HashMap<String, XDataType> templateDataTypes, boolean convertIfNeeded, final GeometryType geometryType) {
		
		if(tableName == null || tableName.isEmpty()){
			Log.v(TAG, "No tableName, cannot create table");
			return false;
		}
		
		if (db != null){
			
	        String query = "SELECT name FROM sqlite_master WHERE type='table' AND name='"+tableName+"'";

	        boolean found = false;
	        try {
	            Stmt stmt = db.prepare(query);
	            if( stmt.step() ) {
	                String nomeStr = stmt.column_string(0);
	                found = true;
	                Log.v(TAG, "Found table: "+nomeStr);
	            }
	            stmt.close();
	        } catch (Exception e) {
	            Log.e(TAG, Log.getStackTraceString(e));
	            return false;
	        }

			if(found){
				if(convertIfNeeded){
					// TODO should call updateTableFromTemplate
					// table_info lists the columns of the given table
			        String table_info_query = "PRAGMA table_info('"+tableName+"');";
			        int nameColumn = -1;
			        int typeColumn = -1;
			        String columnName, typeName;
			        boolean origin_id_found = false;
			        boolean pk_uid_found = false;
			        try {
			            Stmt stmt = db.prepare(table_info_query);
			            while( stmt.step() ) {
			                found = true;
			                if(nameColumn<0 || typeColumn<0){
			                	// I have to retrieve the position of the metadata fields
			                	for(int i = 0; i<stmt.column_count(); i++){
//			                		Log.v(TAG, stmt.column_name(i));
			                		if(stmt.column_name(i).equalsIgnoreCase("name")){
			                			nameColumn = i;
			                		}
			                		if(stmt.column_name(i).equalsIgnoreCase("type")){
			                			typeColumn = i;
			                		}
			                	}
			                }
			                
			                columnName = stmt.column_string(nameColumn);
			                if(columnName != null){
			                	origin_id_found = origin_id_found || columnName.equalsIgnoreCase(Mission.ORIGIN_ID_STRING);
			                	pk_uid_found = pk_uid_found || columnName.equalsIgnoreCase(Mission.PK_UID_STRING);			                	
			                }else{
			                	// This should never happen
			                	Log.v(TAG, "Found a NULL column name, this is strange.");
			                }
			            }
			            stmt.close();
			            
			            if(!origin_id_found){
			            	
			            	stmt = db.prepare("ALTER TABLE '"+tableName+"' ADD COLUMN '"+ Mission.ORIGIN_ID_STRING +"' TEXT;");
			            	stmt.step();
			            	stmt.close();
			            	
			            	if(pk_uid_found){
				            	stmt = db.prepare("UPDATE '"+tableName+"' SET "+ Mission.ORIGIN_ID_STRING +" = PK_UID;");
				            	stmt.step();
				            	stmt.close();
			            	}
			            }
			            
			            // TODO: PersistenceUtils.updateTableFromTemplate()			            
			            
			        } catch (Exception e) {
			            Log.e(TAG, Log.getStackTraceString(e));
			            return false;
			        }

					
				}
				return true;
			}else{
				// Table not found creating
                Log.v(TAG, "Table "+tableName+" not found, creating..");
				
            	String create_stmt = "CREATE TABLE '"+tableName+"' (" +
            			"'ORIGIN_ID' TEXT" ;
            	
            	if(templateDataTypes != null && !templateDataTypes.isEmpty()){
    				Log.v(TAG, "templateDataTypes size: "+templateDataTypes.size());
            		for(Entry<String, XDataType> e : templateDataTypes.entrySet()){
            			if(e.getKey()!=null && e.getValue()!= null)
            				create_stmt = create_stmt + ", '"+e.getKey()+"' "+SpatialiteUtils.getSQLiteTypeFromString(e.getValue().toString());
            			
            		}

            	}
            	
            			//
            	create_stmt =create_stmt + ");";

                Log.v(TAG, "Create statement: "+create_stmt);
            	if(!Database.complete(create_stmt)){
    				Log.w(TAG, "Create statement is not complete:\n"+create_stmt);
    				return false;
    			}
            	
            	// TODO: manage more geometry types and srid
            	String add_geom_stmt = null;
            	switch(geometryType){
            		case POINT_XY :
            			add_geom_stmt = "SELECT AddGeometryColumn('"+tableName+"', 'GEOMETRY', 4326, 'POINT', 'XY');";
            			break;
            		case LINESTRING_XY :
            			add_geom_stmt = "SELECT AddGeometryColumn('"+tableName+"', 'GEOMETRY', 4326, 'LINESTRING', 'XY');";
            			break;
            		case MULTILINESTRING_XY :
            			add_geom_stmt = "SELECT AddGeometryColumn('"+tableName+"', 'GEOMETRY', 4326, 'MULTILINESTRING', 'XY');";
            			break;
            		case POLYGON_XY :
            			add_geom_stmt = "SELECT AddGeometryColumn('"+tableName+"', 'GEOMETRY', 4326, 'POLYGON', 'XY');";
            			break;
            		case MULTIPOINT_XY :
            			add_geom_stmt = "SELECT AddGeometryColumn('"+tableName+"', 'GEOMETRY', 4326, 'MULTIPOINT', 'XY');";
            			break;
            		case MULTIPOLYGON_XY :
            			add_geom_stmt = "SELECT AddGeometryColumn('"+tableName+"', 'GEOMETRY', 4326, 'MULTIPOLYGON', 'XY');";
            			break;
            		case GEOMETRY_XY :
            			add_geom_stmt = "SELECT AddGeometryColumn('"+tableName+"', 'GEOMETRY', 4326, 'GEOMETRY', 'XY');";
            			break;
            		default:
            			add_geom_stmt = "SELECT AddGeometryColumn('"+tableName+"', 'GEOMETRY', 4326, 'POINT', 'XY');";
            			break;
            	}
            	if(!Database.complete(add_geom_stmt)){
    				Log.w(TAG, "AddGeometryColumn statement is not complete:\n"+add_geom_stmt);
    				return false;
    			}
            	
            	String create_idx_stmt = "SELECT CreateSpatialIndex('"+tableName+"', 'GEOMETRY');";
            	if(!Database.complete(create_idx_stmt)){
    				Log.w(TAG, "CreateSpatialIndex statement is not complete:\n"+create_idx_stmt);
    				return false;
    			}
                
            	try {                	
            		Stmt stmt01 = db.prepare(create_stmt);

            		// Create statements returns empty result set, the step() call return value will always be FALSE
					stmt01.step();
					
					if(!db.prepare("PRAGMA table_info('"+tableName+"');").step()){
	    				Log.w(TAG, "Table could not be created");
	    				return false;
					}
					
					stmt01 = db.prepare(add_geom_stmt);
					if (stmt01.step()) {
					    Log.v(TAG, "Geometry Column Added "+stmt01.column_string(0));
					}
					
					stmt01 = db.prepare(create_idx_stmt);
					if (stmt01.step()) {
					    Log.v(TAG, "Index Created");
					}
					
					stmt01.close();
					
				} catch (jsqlite.Exception e) {
					Log.e(TAG, Log.getStackTraceString(e));
					return false;
				}
            	return true;
            	
			}
			
		}else{
			Log.w(TAG, "No valid database received, aborting..");
		}
		
		return false;
	}

	/**
	 * Compare a table schema with the one provided and changes it accordingly
	 * Create columns based on the given templateDataTypes
	 * @param db
	 * @param tableName
	 * @param templateDataTypes2
	 * @return
	 */
	// TODO: Add EnableDropColumn parameter
	public static boolean updateTableFromTemplate(Database db, String tableName,
			HashMap<String, XDataType> templateDataTypes2) {


		if(tableName == null || tableName.isEmpty()){
			Log.v(TAG, "No tableName, cannot create table");
			return false;
		}
		
		if(templateDataTypes2 == null){
			Log.v(TAG, "No templateDataTypes given, aborting");
			return false;
		}
		

		if (db != null){
			
			HashMap<String, XDataType> newschema = new HashMap<String, XDataType>( templateDataTypes2);
			
			// table_info lists the columns of the given table
	        String query = "PRAGMA table_info('"+tableName+"');";
	        int nameColumn = -1;
	        int typeColumn = -1;
	        String columnName, typeName;

	        boolean found = false;
	        ArrayList<String> queriesToBeRun = new ArrayList<String>();
	        ArrayList<String> old = new ArrayList<String>();
	        try {
	            Stmt stmt = db.prepare(query);
	            while( stmt.step() ) {
	                found = true;
	                if(nameColumn<0 || typeColumn<0){
	                	// I have to retrieve the position of the metadata fields
	                	for(int i = 0; i<stmt.column_count(); i++){
	                		//Log.v(TAG, stmt.column_name(i));
	                		if(stmt.column_name(i).equalsIgnoreCase("name")){
	                			nameColumn = i;
	                		}
	                		if(stmt.column_name(i).equalsIgnoreCase("type")){
	                			typeColumn = i;
	                		}
	                	}
	                }
	                
	                columnName = stmt.column_string(nameColumn);
	                if(columnName != null){
	                	old.add(columnName);
	                }else{
	                	// Well, this is strange
	                	Log.v(TAG, "Found a NULL column name, this is strange.");
	                }
	            }
	            stmt.close();
	        } catch (Exception e) {
	            Log.e(TAG, Log.getStackTraceString(e));
	            return false;
	        }

			if(found){
				//compute diff
				/*
				// Check fields to be removed
				for(String oldFieldName : old){
					if(templateDataTypes2.containsKey(oldFieldName)){
						queryListToBeRun.add("")
					}
				}
				*/
				
				//List fields to be added
				
				Log.v(TAG, "templateDataTypes size: "+newschema.size());
				for(String oldFieldName : old){
					XDataType got = newschema.remove(oldFieldName);
					if(got == null){
						// this column should be dropped
					}else{
						// Check type?
					}
				}
				
				Log.v(TAG, "Found "+newschema.size()+" fields to be added");
				boolean canAddColumn = false;
        		for(Entry<String, XDataType> e : newschema.entrySet()){
        			if(e.getKey()!=null && e.getValue()!= null){
        			    
        			    canAddColumn = true;
        			    
        			    for(String oldFieldName : old){
                            if(oldFieldName!= null && oldFieldName.equalsIgnoreCase(e.getKey())){
                                // Only the casing is changed, do not try to add the column
                                canAddColumn = false;
                            }
                        }
        			    
        			    if(canAddColumn){
        			        queriesToBeRun.add("ALTER TABLE '"+tableName+"' ADD COLUMN '"+e.getKey()+"' "+SpatialiteUtils.getSQLiteTypeFromString(e.getValue().toString())+";");
        			    }
        			}
        		}

        		// Log out
        		for(String s : queriesToBeRun){
        			Log.v(TAG, "Query : "+s);
        			if(!Database.complete(s)){
        				Log.w(TAG, "The query is not complete: "+s);
        				return false;
        			}
        		}
        		Stmt stmt;
        		// Execute queries
        		try {
        			
        			// TODO: Investigate transaction for concurrency
        			
        			for(String s : queriesToBeRun){
	                    stmt = db.prepare(s);
	                    stmt.step(); // step on an ALTER query always returns false
	                    stmt.close();
        			}
        			
                } catch (Exception e) {
                    Log.e(TAG,  Log.getStackTraceString(e));
                    return false;
                }
        		
        		
				return true;
			}else{
				// Table not found
                Log.v(TAG, "Table "+tableName+" not found.");
				return false;
			}
			
		}else{
			Log.w(TAG, "No valid database received, aborting..");
		}
		
		return false;
	}
	/**
	 * retrieves the current max id for the created missionFeature table
	 * if none is present yet 0 is returned
	 * 
	 * TODO: This ID should not be sent to the server
	 * 		 At the moment the server does not respond with an ID, so this ID must be used
	 * 
	 * @param db the database to use
	 * @param tableName the table to lookup
	 * @return max + 1 
	 */
	public static Long getIDforNewMissionFeatureEntry(Database db,String tableName) {
		
		/* 
		 * I will now completely ignore database content and generate a new ID
		 * trying to avoid collisions with other existing GCID.
		 * Generated GCIDs are in milliseconds values, so the highest collision probability 
		 * will be in the few seconds after midnight, January the 1st, of each year.
		 */
		
		long millisInYear = 31622400000L;
		long currentTime = System.currentTimeMillis();
		
		long generatedID = currentTime % millisInYear;
		
		if(BuildConfig.DEBUG){
			Log.v(TAG, "Generated ID: "+generatedID);
		}
		
		if(generatedID > 0){
			return generatedID;
		}
		
		return Long.valueOf(0);
	}
	/**
	 * creates a new row in the created mission table identified by an id
	 * @param db the database to write to
	 * @param tableName the table to insert
	 * @param id the id to refer
	 * @return if the insert was successful
	 */
	public static boolean insertCreatedMissionFeature(Database db,String tableName, Long id) {
		
		if(tableName == null || tableName.isEmpty()){
			Log.v(TAG, "No tableName, cannot create table");
			return false;
		}
		
		
		String insert = "INSERT INTO '"+tableName+"' ("+Mission.ORIGIN_ID_STRING+") VALUES ('"+id+"');";
		
		
		try {
			Stmt stmt = db.prepare(insert);
			
			if(stmt.step()){
				return true;
			}
		} catch (Exception e) {
			Log.e(TAG, "error inserting entry "+id + " into "+tableName,e);
			return false;
		}
		
		return true;
	}
	/**
	 * deletes a missionFeature from a table which can be a "sop" or a "new" table
	 * 
	 * @param db the database to delete from
	 * @param tableName the table to delete in
	 * @param the "ORIGIN-ID" of the feature to delete
	 */
	public static void deleteMissionFeature(final Database db, final String tableName, final String id){
		
		if(tableName == null || tableName.isEmpty()){
			Log.v(TAG, "No tableName, cannot create table");
			return;
		}		
		
		String delete = "DELETE FROM '"+tableName+"' WHERE "+Mission.ORIGIN_ID_STRING+" = ('"+id+"');";
		
		
		try {
			Stmt stmt = db.prepare(delete);
			stmt.step();

		} catch (Exception e) {
			Log.e(TAG, "error deleting entry "+id + " from "+tableName,e);
		}
	}
	/**
	 * gets the x and y coordinated of a geometry
	 * @param db the database to read from
	 * @param tableName the name of the spatialite table
	 * @param id the origin_id to refer to
	 * @return a double array{x,y}
	 */
	public static double[] getXYCoord(final Database db,final String tableName,final String id){
		
		double[] result = new double[2];
		
		String query = "SELECT x(GEOMETRY), y(GEOMETRY) FROM '"+tableName+"' WHERE "+Mission.ORIGIN_ID_STRING+" = ('"+id+"');";
		
		Stmt stmt;
		try {
			stmt = db.prepare(query);
			if(stmt.step()){
				if(stmt.column_name(0).equalsIgnoreCase("x(GEOMETRY)")){
					String x = stmt.column_string(0);
					if(x != null){        				
						result[0] = Double.parseDouble(x);
					}
				}
				if(stmt.column_name(1).equalsIgnoreCase("y(GEOMETRY)")){
					String y = stmt.column_string(1);
					if(y != null){        				
						result[1] = Double.parseDouble(y);
					}
				}
				stmt.close();
			}
		} catch (Exception e) {
			Log.e(TAG, "error get Max ID for newMission",e);
			return null;
		}
		
		return result;
	}
	/**
	 * updates a row in the "created missionsfeature" table
	 * @param db to update
	 * @param tableName the table to modify
	 * @param f the field to refer
	 * @param value the value to update
	 * @param id the origin_id to refer
	 */
	public static void updateCreatedMissionFeatureRow(final Database db, final String tableName, final Field f, String value, final String id){

		Stmt st = null;
		String s;
		if(f.xtype == XType.mapViewPoint){
			// a geometry must be built
			s = "UPDATE '"+tableName+"' SET "+ f.fieldId +" = "+ value +" WHERE "+Mission.ORIGIN_ID_STRING+" = '"+id+"';";
		}else{
			// Standard values
			value = value.replace("'", "''");
			s = "UPDATE '"+tableName+"' SET "+ f.fieldId +" = '"+ value +"' WHERE "+Mission.ORIGIN_ID_STRING+" = '"+id+"';";
		}
		Log.v(TAG, "Query :\n"+s);
		if(Database.complete(s)){
			try{
				st = db.prepare(s);
				if(st.step()){
					//an update will not end necessarily here
				}
			} catch (Exception e) {
				Log.e(TAG, "error updating entry "+f.fieldId + " into "+tableName,e);
			}
		}else{
			Log.w(TAG, "Skipping non complete statement:\n"+s);
		}
		if(st!=null){
			try {
				st.close();
			} catch (Exception e) {
				//Log.e(TAG, Log.getStackTraceString(e));
				// ignore
			}
		}
	}

	public static void saveDownloadedTemplates(final Context context, final ArrayList<MissionTemplate> templates){

		try {
			if(templates != null && templates.size() > 0){
			    if(BuildConfig.DEBUG){
	                int i = 0;
	                for(MissionTemplate mt : templates){
	                    Log.d(TAG, "SaveTemplates - Mission "+i+++" :"+(mt.id!=null?mt.id:"NULL") + "  **");
	                }
	            }
				FileOutputStream fo = context.openFileOutput(DOWNLOADED_TEMPLATES, Context.MODE_PRIVATE);
				ObjectOutputStream out = new ObjectOutputStream(fo);
				out.writeObject(templates);
				out.flush();
				out.close();
				fo.close();
			}
		} catch (IOException e) {
			Log.e(TAG, "Downloaded Template save failed",e);
		}

	}

	public static ArrayList<MissionTemplate> loadSavedTemplates(final Context context){

		try {

			FileInputStream fi = context.openFileInput(DOWNLOADED_TEMPLATES);
			ObjectInputStream in = new ObjectInputStream(fi);
			@SuppressWarnings("unchecked")
			ArrayList<MissionTemplate> templates = (ArrayList<MissionTemplate>) in.readObject();
			Collections.sort(templates, new MissionUtils.MissionTemplateComparator());
			
			if(BuildConfig.DEBUG){
			    int i = 0;
    			for(MissionTemplate mt : templates){
    			    Log.d(TAG, "LoadTemplates - Mission "+i+++" :"+(mt.id!=null?mt.id:"NULL"));
    			}
    			Log.d(TAG, "Templates load succeeded");
			}
			in.close();
			fi.close();

			return templates;

		} catch (FileNotFoundException e) {
			Log.d(TAG, "No templates saved yet");
		} catch (StreamCorruptedException e) {
			Log.e(TAG, "Saved Templates load failed",e);
		} catch (IOException e) {
			Log.e(TAG, "Saved Templates load failed",e);
		} catch (ClassNotFoundException e) {
			Log.e(TAG, "Saved Templates load failed",e);
		}
		return null;
	}
	
	public static void saveUploadables(final Context context, final HashMap<String,ArrayList<String>> uploadables){

		try {
			if(uploadables != null){

				FileOutputStream fo = context.openFileOutput(UPLOADABLE_ENTRIES, Context.MODE_PRIVATE);
				ObjectOutputStream out = new ObjectOutputStream(fo);
				out.writeObject(uploadables);
				out.flush();
				out.close();
				fo.close();
			}
		} catch (IOException e) {
			Log.e(TAG, "Uploadables save failed",e);
		}

	}
	
	public static HashMap<String,ArrayList<String>> loadUploadables(final Context context){
		
		try {

			FileInputStream fi = context.openFileInput(UPLOADABLE_ENTRIES);
			ObjectInputStream in = new ObjectInputStream(fi);
			@SuppressWarnings("unchecked")
			HashMap<String,ArrayList<String>> uploadables = (HashMap<String,ArrayList<String>>) in.readObject();
			in.close();
			fi.close();

			return uploadables;

		} catch (FileNotFoundException e) {
			Log.d(TAG, "No uploadables saved yet");
			return new HashMap<String,ArrayList<String>>();
		} catch (StreamCorruptedException e) {
			Log.e(TAG, "Uploadables load failed",e);
		} catch (IOException e) {
			Log.e(TAG, "Uploadables load failed",e);
		} catch (ClassNotFoundException e) {
			Log.e(TAG, "Uploadables load failed",e);
		}
		return null;
	}

    /**
     * Load file from res/raw folder or Assets folder into a String 
     * 
     * @param resources
     * @param fileName
     * @param loadFromRawFolder
     * @return
     * @throws IOException
     */
    public static String loadFile(Resources resources, String fileName, boolean loadFromRawFolder) throws IOException  
    {  
        //Create a InputStream to read the file into  
        InputStream iS;  
  
        if (loadFromRawFolder)  
        {  
            //get the resource id from the file name  
            int rID = resources.getIdentifier("it.geosolutions.geocollect.android.core:raw/"+fileName, null, null);  
            //get the file as a stream  
            iS = resources.openRawResource(rID);  
        }  
        else  
        {  
            //get the file as a stream  
            iS = resources.getAssets().open(fileName);  
        }  
  
        //create a buffer that has the same size as the InputStream  
        byte[] buffer = new byte[iS.available()];  
        //read the text file as a stream, into the buffer  
        iS.read(buffer);  
        //create a output stream to write the buffer into  
        ByteArrayOutputStream oS = new ByteArrayOutputStream();  
        //write this buffer to the output stream  
        oS.write(buffer);  
        //Close the Input and Output streams  
        oS.close();  
        iS.close();  
  
        //return the output stream as a String  
        return oS.toString();  
    }
    
    /**
     * Load file from res/raw folder or Assets folder into a String 
     * 
     * @param resources
     * @param fileName
     * @param loadFromRawFolder
     * @return
     * @throws IOException
     */
    public static String loadBaseStyleFile(Resources resources) throws IOException  
    {  
        //Create a InputStream to read the file into  
        InputStream iS = resources.openRawResource(R.raw.base);    
        
        //create a buffer that has the same size as the InputStream  
        byte[] buffer = new byte[iS.available()];  
        //read the text file as a stream, into the buffer  
        iS.read(buffer);  
        //create a output stream to write the buffer into  
        ByteArrayOutputStream oS = new ByteArrayOutputStream();  
        //write this buffer to the output stream  
        oS.write(buffer);  
        //Close the Input and Output streams  
        oS.close();  
        iS.close();  
  
        //return the output stream as a String  
        return oS.toString();  
    } 
    
    public static void sanitizePendingFeaturesList(HashMap<String, ArrayList<String>> uploadables, Database db ){
        
        if(uploadables != null
                && uploadables.size()> 0
                && db != null){
            
            Stmt stmt;
            ArrayList<String> validIDsList;
            for(String tableName : uploadables.keySet()){
                String query = "SELECT \""+Mission.ORIGIN_ID_STRING+"\" FROM \"" + tableName+"\" ; ";
                validIDsList = new ArrayList<String>();
                try {
                    stmt = db.prepare(query);
                    while(stmt.step()){
                        if(stmt.column_name(0).equalsIgnoreCase(Mission.ORIGIN_ID_STRING)){
                            String originID = stmt.column_string(0);
                            if(uploadables.get(tableName).contains(originID)){
                                Log.d(TAG, "ORIGIN_ID found : "+originID);
                                validIDsList.add(originID);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error getting ORIGIN_ID for "+ tableName,e);
                }
                
                // Update the ID list
                uploadables.put(tableName, validIDsList);
                
            }
        }
        
        
    }

}
