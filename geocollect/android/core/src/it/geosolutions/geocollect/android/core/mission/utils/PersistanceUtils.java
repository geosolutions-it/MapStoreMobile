/**
 * 
 */
package it.geosolutions.geocollect.android.core.mission.utils;

import java.util.HashMap;
import java.util.List;

import it.geosolutions.geocollect.android.core.form.utils.FormBuilder;
import it.geosolutions.geocollect.android.core.mission.Mission;
import it.geosolutions.geocollect.android.core.widgets.DatePicker;
import it.geosolutions.geocollect.model.viewmodel.Field;
import it.geosolutions.geocollect.model.viewmodel.Page;
import jsqlite.Database;
import jsqlite.Exception;
import jsqlite.Stmt;
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
public class PersistanceUtils {

	/**
	 * Tag for logging
	 */
	public static String TAG = "PersistanceUtils";
	
	/**
	 * Default method for storePageData
	 * Stores the view data on given database based on given Page information
	 */
	public static boolean storePageData(Page page, LinearLayout layout, Mission mission, Database db){
		return storePageData(page, layout, mission, db, "punti_accumulo_data");
	}
	
	/**
	 * Stores the view data on given database based on given Page information
	 * @return
	 */
	public static boolean storePageData(Page page, LinearLayout layout, Mission mission, Database db, String tableName){
		
		if(tableName == null || tableName.equalsIgnoreCase("")){
			Log.w(TAG, "Empty tableName, aborting...");
			return false;
		}
		
		if(db != null){
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
						// TODO
						continue;
						//addMapViewPoint(f,mFormView,context,mission);
						//break;
					default:
						//textfield as default
						value = ((TextView)v).getText().toString();
					}
				}
				try {	
					value = value.replace("'", "''");
					s = "UPDATE 'punti_accumulo_data' SET "+ f.fieldId +" = '"+ value +"' WHERE ORIGIN_ID = '"+mission.getOrigin().id+"';";
					Log.v(TAG, "Query :\n"+s);
					if(Database.complete(s)){
						st = db.prepare(s);
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
	public static boolean loadPageData(Page page, LinearLayout layout, Mission mission, Database db){
		return loadPageData(page, layout, mission, db, "punti_accumulo_data");
	}
	
	
	/**
	 * Load the page data from the give database
	 * @return
	 */
	public static boolean loadPageData(Page page, LinearLayout layout, Mission mission, Database db, String tableName){
		
		if(db != null){
			String s;
			Stmt st = null;
			for(Field f : page.fields){
				if(f == null )continue;
				try {
					// TODO: load all the fields in one query
					s = "SELECT "+ f.fieldId +" FROM '"+tableName+"' WHERE ORIGIN_ID = '"+mission.getOrigin().id+"';";
					if(jsqlite.Database.complete(s)){
						st = db.prepare(s);
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
									// TODO
									//addMapViewPoint(f,mFormView,context,mission);
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
							st = db.prepare(s);
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
		}else{
			Log.w(TAG, "Database not found, aborting...");
			return false;
		} // if db
		
		return false;
		
	}
	
}
