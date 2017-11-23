package it.geosolutions.geocollect.android.core.mission.utils;

import it.geosolutions.android.map.utils.MapFilesProvider;
import it.geosolutions.android.map.wfs.geojson.GeoJson;
import it.geosolutions.android.map.wfs.geojson.feature.Feature;
import it.geosolutions.android.map.wfs.geojson.feature.FeatureCollection;
import it.geosolutions.geocollect.android.app.BuildConfig;
import it.geosolutions.geocollect.android.core.mission.Mission;
import it.geosolutions.geocollect.model.config.MissionTemplate;
import it.geosolutions.geocollect.model.http.VectorLayer;
import it.geosolutions.geocollect.model.source.XDataType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import jsqlite.Database;
import jsqlite.Stmt;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import eu.geopaparazzi.spatialite.database.spatial.core.GeometryType;

/**
 * class which handles the download and persistence of vector layers
 * 
 * @author Robert Oehler
 */

public class VectorLayerLoader {

	private final static String TAG = "VectorLayerLoader";
	
	private Database db;
	
	private VectorLayerLoaderListener mListener;
	
	public VectorLayerLoader(Database db) {
		super();
		this.db = db;
	}
	
	
	/**
	 * checks if the layers defined by @param missionTemplate are locally available and up to date
	 * returns a list of vector layers which need an update
	 * or an empty list if everything is up to date
	 * or null if an error occurred / e.g. the MissionTemplate does not define vector layers
	 * @param missionTemplate 
	 * @return a list of vector layers to download or null
	 */
	public ArrayList<VectorLayer> checkIfVectorLayersAreAvailable(final MissionTemplate missionTemplate) {

		if (missionTemplate != null&& missionTemplate.config != null && missionTemplate.config.containsKey(MissionTemplate.OVERLAYS_KEY)) {

			try {
				ArrayList<Map> overlays = (ArrayList<Map>) missionTemplate.config.get(MissionTemplate.OVERLAYS_KEY);

				if (overlays != null && overlays.size() > 0) {

					ArrayList<VectorLayer> layersToDownload = new ArrayList<VectorLayer>();

					for (int i = 0; i < overlays.size(); i++) {

						final Map entry = overlays.get(i);

						final String name = (String) entry.get("name");
						final String url = (String) entry.get("url");
						final String style = (String) entry.get("style");
						final int version = Integer.parseInt((String) entry.get("version"));

						// what is the version of the local data ?
						final int localVersion = getVersionOfLocalVectorTable(name);

						// if it is not locally available or not the template's version add it
						// to the task list
						if (localVersion == -1 || localVersion != version) {

							layersToDownload.add(new VectorLayer(name, url,version, style));
						}
					}

					return layersToDownload;
				} else {
					Log.w(TAG, "template did not contain any vector overlays");
				}

			} catch (ClassCastException e) {
				Log.e(TAG, "vector layers are not a map", e);
			}

		} else {
			Log.v(TAG,"template null or config null or template does not contain overlays key");
		}
		return null;
	}

	/**
	 * downloads the layers in @param vectorLayers and persists them in the local database
	 * @param vectorLayers the layers to download
	 */
	public void loadLayers(final ArrayList<VectorLayer> vectorLayers){
		
		new AsyncTask<Void,Void,Pair<Boolean, String>>() {
			
			protected Pair<Boolean, String> doInBackground(Void... arg0) {
				
				final HttpClient httpclient = new DefaultHttpClient();
				String error = "";
				
				for (VectorLayer vectorLayer : vectorLayers) {
					
					if(vectorLayer.name == null || vectorLayer.url == null){
						Log.e(TAG, "vector layer does not define name and download url, cannot download this layer");
						continue;
					}
					
					//1. download features
					if(BuildConfig.DEBUG){
						Log.i(TAG, "Downloading features for " + vectorLayer.name);
					}

					HttpGet getFeatures = new HttpGet(vectorLayer.url);
					getFeatures.addHeader("Accept", "application/json");

					try {
						HttpResponse response = httpclient.execute(getFeatures);
						HttpEntity resEntity = response.getEntity();
						
						if (resEntity != null) {
							// parse response.
							String responseText = EntityUtils.toString(resEntity);

							FeatureCollection featureCollection = new GeoJson().fromJson(responseText, FeatureCollection.class);
							
							if(featureCollection != null && featureCollection.features != null && featureCollection.features.size() > 0){
								
								if(BuildConfig.DEBUG){
									Log.i(TAG, "Successfully downloaded " + featureCollection.features.size() +" features");
								}
								
								//write data to database
								if(insertData(vectorLayer, featureCollection.features, true)){
									
									if(BuildConfig.DEBUG){
										Log.i(TAG, "Successfully persisted " + vectorLayer.name);
									}
								}else{
									error +=  "Error persisting" + vectorLayer.name;									
								}
							
							}else{
								error +=  "Download failed or empty for " + vectorLayer.url;
							}
						}
					} catch (Exception e) {
						Log.e(TAG, "Exception loading features", e);
						error += "Exception loading features";
					}

					// 2.style file
					if (vectorLayer.styleFile != null) {
						
						File styleDir = checkStylesDirectory();
						File styleFile = new File(styleDir, vectorLayer.name+ ".style");

						if (!styleFile.exists()) {
							HttpGet getStyle = new HttpGet(vectorLayer.styleFile);
							getStyle.addHeader("Accept", "application/json");
							try {
								HttpResponse response = httpclient.execute(getStyle);
								HttpEntity resEntity = response.getEntity();

								if (resEntity != null) {
									// parse response.
									String style = EntityUtils.toString(resEntity);

									if (style != null&& !TextUtils.isEmpty(style)) {

										if (BuildConfig.DEBUG) {
											Log.i(TAG, "Downloaded style "+ style);
										}

										MissionUtils.writeStyleFile(style,styleFile);

									} else {
										error +=  "Error downloading style "+ vectorLayer.styleFile;
									}
								} else {
									error +=  "HttpEntity null for style "+ vectorLayer.styleFile;
								}

							} catch (Exception e) {
								Log.e(TAG, "Exception loading style", e);
								error += "Exception loading style " + vectorLayer.styleFile;
							}
						}
					}else{
						Log.e(TAG, "vector layer does not define a style file "+ vectorLayer.name);
					}
				}
				if(!TextUtils.isEmpty(error)){
					Log.e(TAG, error);
				}
				
				if (TextUtils.isEmpty(error)) {
					return new Pair<Boolean, String>(true, null);
				} else {
					return new Pair<Boolean, String>(false, error);
				}
				
			}		
			protected void onPostExecute(android.util.Pair<Boolean,String> result) {
				
				if(mListener != null){
					if(result != null && result.first){
						mListener.didLoadLayers();
					}else if(result != null){
						mListener.error(result.second);
					}else{
						mListener.error(null);
					}
				}
			};
		}.execute();
	}
	
	/**
	 * inserts the downloaded data into a database table
	 * 
	 * @param vectorLayer the container of this vector layer
	 * @param features the features to insert
	 * @param dropTable if to delete a priorly existing table before - otherwise it is truncated 
	 * @return if the operation was successful
	 */
	private boolean insertData(final VectorLayer vectorLayer, final ArrayList<Feature> features, final boolean dropTable){
		
		if (dropTable) {
			// drop table
			try {
				// 1.table itself
				Stmt stmt = db.prepare("DROP TABLE IF EXISTS '"+ vectorLayer.name + "';");
				stmt.step();
				stmt.close();

				// 2.row in geometry_columns
				stmt = db.prepare("DELETE FROM geometry_columns WHERE f_table_name ='"+ vectorLayer.name + "';");
				stmt.step();
				stmt.close();

			} catch (jsqlite.Exception e) {
				Log.e(TAG, "Error dropping table "+vectorLayer.name, e);
			}
		}
		
		//determine the geometry type
		GeometryType type = null;
		for(Feature feature : features){
			if(type == null){
				if(feature.geometry != null){
					type = GeometryType.values()[GeometryType.forValue(feature.geometry.getGeometryType())];
				}
			}else{
				//there is already a type, has this feature the same ?
				if(feature.geometry != null){
					GeometryType thisType = GeometryType.values()[GeometryType.forValue(feature.geometry.getGeometryType())];
					if(type != thisType){
						//has different type, use the generic geometry type
						type = GeometryType.GEOMETRY_XY;
					}
				}
			}
		}
		
		if(type == null){
			Log.e(TAG, "could not determine a geometry type for "+ vectorLayer.name +" table ");
			return false;
		}
		
		//create table if necessary
		if(!PersistenceUtils.createTableFromTemplate(db, vectorLayer.name, getDatabaseSchema(features), type)){
			Log.e(TAG, "error creating "+ vectorLayer.name +" table ");
			return false;
		}
		
		if (!dropTable) {
			// when not having dropped the table be sure to delete any prior entry for this table
			try {
				Stmt stmt = db.prepare("DELETE FROM '" + vectorLayer.name+ "';");
				stmt.step();
				stmt.close();
			} catch (jsqlite.Exception e) {
				Log.e(TAG, "Error truncating table " + vectorLayer.name);
			}
		}
		
		//now insert, this is based on SQLiteCascadeFeatureLoader's loadInBackground() "reload" part
		
		Stmt stmt;

		StringBuilder columnNames = new StringBuilder(300);
		StringBuilder columnValues = new StringBuilder(300);
        
        columnNames.append(" ( ").append(Mission.ORIGIN_ID_STRING);
        columnValues.append(" ( ").append("'");
        
        int namesToTruncate = columnNames.length();
        int valuesToTruncate = columnValues.length();
        
        HashMap<String, String> dbFieldValues = SpatialiteUtils.getPropertiesFields(db, vectorLayer.name);
        int errors = 0;
		for(Feature feature : features){

			  columnValues.append(feature.id).append( "'");
              
              for(Entry<String, String> entry : dbFieldValues.entrySet()){
            
                  if (entry.getKey().equals("GEOMETRY") && feature.geometry != null){
                  
                	  boolean supported = true;
                 
                      if(feature.geometry instanceof Point){
                    	  
                    	  columnValues.append(", MakePoint(").append(((Point)feature.geometry).getX()).append(",").append(((Point)feature.geometry).getY()).append(", 4326)");
                      }else if(feature.geometry instanceof MultiLineString){
                    	  
                    	  columnValues.append(", MultiLineStringFromText('").append(feature.geometry.toText()).append("', 4326)");
                      }else if(feature.geometry instanceof LineString){
                    	  
                    	  columnValues.append(", LineFromText('").append(feature.geometry.toText()).append("', 4326)");
                      }else if(feature.geometry instanceof Polygon){
                    	  
                    	  columnValues.append(", PolygonFromText('").append(feature.geometry.toText()).append("', 4326)");
                      }else if(feature.geometry instanceof MultiPolygon){
                    	  
                    	  columnValues.append(", MultiPolygonFromText('").append(feature.geometry.toText()).append("', 4326)");
                      }else if(feature.geometry instanceof MultiPoint){
                    	  
                    	  columnValues.append(", MultiPointFromText('").append(feature.geometry.toText()).append("', 4326)");
                      }else{
                    	  
                    	  Log.w(TAG, "unsupported geometry "+ feature.geometry.getGeometryType());
                    	  supported = false;
                      }
                      
                      if(supported){
                    	  columnNames.append( ", GEOMETRY");
                      }
                      
                  }else if(entry.getKey().equals(VectorLayer.VERSION)){
                      
                      columnNames.append( ", " ).append( entry.getKey());
                      columnValues.append( ", " ).append( vectorLayer.version) ;
                  }else if(entry.getKey().equals(VectorLayer.STYLE)){
                	  
                	    columnNames.append( ", " ).append( entry.getKey() );
                        columnValues.append( ", '" ).append( vectorLayer.styleFile).append("'");
                  }else if(entry.getKey().equals(VectorLayer.URL)){
                	  
                	  columnNames.append( ", " ).append( entry.getKey() );
                      columnValues.append( ", '" ).append( vectorLayer.url).append("'") ;
                  }else if(entry.getKey().equals(Mission.ORIGIN_ID_STRING)){
                	  
                	  //origin id was handled before as first entry
                	  continue;
                  }else if(feature.geometry == null){
                	  
                	  Log.w(TAG, "geometry null - cannot import this feature");
                  }else{
                	  
                      for(String k : feature.properties.keySet()){
                          
                          if(entry.getKey().equals(k)){
                              
                              columnNames.append( ", " ).append( entry.getKey() );
                              columnValues.append( ", '" ).append( feature.properties.get(k)).append("'") ;
                          }

                      }
                  }
              }

			// close
			columnNames.append(" )");
			columnValues.append(" )");

			final String insertQuery = "INSERT INTO '" + vectorLayer.name + "' "+ columnNames.toString() + " VALUES "+ columnValues.toString() + ";";
			
			if(BuildConfig.DEBUG){
				Log.i(TAG, insertQuery);
			}

			columnNames.setLength(namesToTruncate);
			columnValues.setLength(valuesToTruncate);

			try {
				stmt = db.prepare(insertQuery);
				stmt.step();
				stmt.close();
			} catch (Exception e) {
				Log.e(TAG, "Error inserting vector layer", e);
				errors++;
			}
		}
		
		return errors == 0;
	}
	
	/**
	 * Cycle on all features to get all properties
	 * @param features
	 * @return
	 */
	private HashMap<String, XDataType> getDatabaseSchema(ArrayList<Feature> features) {
	    
	    if(features == null){
	        return null;
	    }
	    
        HashMap<String, XDataType> map = new HashMap<String, XDataType>();
        
        for(Feature feature : features){
            
            for(String k : feature.properties.keySet()){
                map.put(k , XDataType.text);
            }
        }
        map.put(VectorLayer.URL , XDataType.text);
        map.put(VectorLayer.VERSION, XDataType.integer);
        map.put(VectorLayer.STYLE, XDataType.text);
        
        return map;
    }


    /**
	 * reads the version column of the local vector layer table and returns it
	 * @param tableName the name of the table
	 * @return the version or -1 if the table does not exist
	 */
	public int getVersionOfLocalVectorTable(final String tableName){
		
		int localVersion = -1;
		
		// 1. does table exist ?

		String query = "SELECT name FROM sqlite_master WHERE type='table' AND name='"+ tableName + "'";

		boolean found = false;
		try {
			Stmt stmt = db.prepare(query);
			if (stmt.step()) {
				String nomeStr = stmt.column_string(0);
				found = true;
				Log.v(TAG, "Found table: " + nomeStr);
			}
			stmt.close();
		} catch (Exception e) {
			Log.e(TAG, "error checking if table " + tableName+ " exists", e);
			return localVersion;
		}
		
		if (found) {
			// 2. check version
			query = "SELECT VERSION from " + tableName+ " ORDER BY ORIGIN_ID DESC LIMIT 1";

			Stmt stmt;
			try {
				stmt = db.prepare(query);
				stmt.step();
				localVersion = stmt.column_int(0);
				stmt.close();
			} catch (Exception e) {
				Log.e(TAG, "error reading version from table " + tableName, e);
			}
		}
		
		return localVersion;
	}
	
	/**
	 * checks if the style directory exists, creates it if necessary and returns it
	 * @return the style directory of the application
	 */
	private File checkStylesDirectory() {

		File styleDir = new File(MapFilesProvider.getStyleDirIn());
		if (!styleDir.exists()) {

			// Create the directory if not exists
			styleDir.mkdirs();

		} else if (!styleDir.isDirectory()) {
			if (BuildConfig.DEBUG) {
				Log.w(TAG, "Style directory is not a directory!");
			}
		}
		return styleDir;
	}
	
	
	/**
	 * sets a listener to listen to layer download events
	 * @param listener
	 */
	public void setListener(VectorLayerLoaderListener listener) {
		this.mListener = listener;
	}

	public interface VectorLayerLoaderListener
	{
		public void didLoadLayers();
		
		public void error(String errorMessage);
	}
}
