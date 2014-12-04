package it.geosolutions.android.map.wfs;

import it.geosolutions.android.map.BuildConfig;
import it.geosolutions.android.map.wfs.geojson.GeoJson;
import it.geosolutions.android.map.wfs.geojson.feature.Feature;
import it.geosolutions.android.map.wfs.geojson.feature.FeatureCollection;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import android.net.Uri;
import android.net.Uri.Builder;
import android.util.Base64;
import android.util.Log;

import com.google.gson.JsonSyntaxException;
/**
 * This is a Client for a WFS Service
 * @author Lorenzo Natali (lorenzo.natali at geo-solutions.it)
 *
 */
public class WFSGeoJsonClient {
	private String url;
	private String username;
	private String password;
	public Integer totalCount;
	public String getUrl() {
		return url;
	}
	/**
	 * Set the WFS URL
	 * @param url
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	/**
	 *  (Basic authentication)
	 * @return username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Set username for Basic Authentication
	 * @param username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * get password
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * Set password for Basic Authentication
	 * @param password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Get Feature with start and limit 
	 * @param typeName the name of the FeatureType
	 * @param baseParams the params
	 * @param start displacement from the first feature (needs sorting)
	 * @param limit limit the number of features)
	 * @return
	 */
	public List<Feature> getFeature(String typeName,Map<String,String> baseParams, Integer start,Integer limit) {
		HttpClient httpclient = new DefaultHttpClient();
		
		//create the URI of the request
		URI uri;
		try {
			
			Builder ub = new Uri.Builder();
			//recreate the uri
			String[] parts = url.split("://");
			if(parts.length > 1){
				//Set schema
				ub.scheme(parts[0]);
				//separate query string
				parts = parts[1].split("\\?");
				//separate path parameters
				String[] part1 =  parts[0].split("/");
				//set authority
				ub.authority(part1[0]);
				//append path
				if(part1.length >1){
					for(int i=1; i < part1.length;i++){
						ub.appendPath(part1[i]);
					}
				}
				//append query parameters
				if(parts.length > 1){
					String queryString = parts[1];
					//separate parameters
					String[] params = queryString.split("&");
					for(int i=0; i < params.length;i++){
						//separate key and values
						String[] pel = params[i].split("=");
						//append if anything ok
						if(pel.length>1){
							ub.appendQueryParameter(pel[0], pel[1]);
						}
						
					}
					
				}
			}else{
				throw new URISyntaxException(url,"missing schema or domain");
			}
			ub.appendQueryParameter("request", "GetFeature");
			ub.appendQueryParameter("typeName", typeName);
			//TODO params
			if(baseParams == null){
				baseParams = new HashMap<String,String>();
			}
			//?service=WFS&version=1.0.0&request=GetFeature&typeName=geosolutions:cities&maxFeatures=50&outputFormat=json"
			//Add default parameters;
			baseParams.put("service", "WFS");
			baseParams.put("version", "1.0.0");
			baseParams.put("request", "GetFeature");
			baseParams.put("outputFormat", "json");
			for(String par : baseParams.keySet()){
				ub.appendQueryParameter(par, baseParams.get(par));
			}
			
			if(limit !=null){
				ub.appendQueryParameter("maxfeatures",limit.toString());
				if(start != null){//TODO, need a sort criteria!!!
					ub.appendQueryParameter("startindex",start.toString());
					
				}
				
			}
			Uri ur = ub.build();
			uri = new URI(ur.toString());
			Log.d("WFS-Geojson","URL:" +uri );
		} catch (URISyntaxException e1) {
			Log.w("WFS-Geojson", "URL not vaild for the request");
			return new ArrayList<Feature>();
		}
		Log.d("WFS","request_url:"+uri.toString());
		HttpGet get = new HttpGet(uri);
		if (url == null) {
			if(BuildConfig.DEBUG){
				Log.w("WFS-Geojson", "URL Not Present. Unable to submit the request");
			}
			return new ArrayList<Feature>();
		}
		
		get.addHeader("Accept", "application/json");
		
		if(username!= null && password!= null){
			get.addHeader(new BasicHeader("Authorization", getB64Auth(username, password)));
		}

		HttpResponse response;
		// TODO support pagination, filtering
		String responseText = null;

		try {
			response = httpclient.execute(get);
			HttpEntity resEntity = response.getEntity();
			if (resEntity != null) {
				
				// parse response.
				responseText = EntityUtils.toString(resEntity);
				Log.d("WFS-Geojson", "remote service response:");
				Log.d("WFS-Geojson", responseText);
				GeoJson gson = new GeoJson();
				FeatureCollection c = gson.fromJson(responseText, FeatureCollection.class);
				if(c!= null) {
					if(c.totalFeatures !=null){
						totalCount=c.totalFeatures; 
						
					}
					//is a count of the features
					return c.features;
					
				}
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			Log.e("WFS-Geojson", "HTTP Protocol error");
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e("WFS-Geojson", "IOException during HTTP request");
			return null;
		} catch (IllegalArgumentException e){
			Log.e("WFS-Geojson","Unable to parse the response:"+responseText);
			Log.e("WFS-Geojson","Error:"+e.getMessage());
			return null;
		}catch (JsonSyntaxException e){
			Log.e("WFS-Geojson","Unable to parse the response:"+responseText);
			Log.e("WFS-Geojson","Error:"+e.getMessage());
			return null;
		}
		return new ArrayList<Feature>();
	}
	
	public boolean test() {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet get = new HttpGet(url);
		get.addHeader("Accept", "application/json");
		HttpResponse response;
		// TODO support pagination, filtering
		try {
			response = httpclient.execute(get);

			HttpEntity resEntity = response.getEntity();
			
			String responseText;
			if (resEntity != null) {
				// parse response.
				responseText = EntityUtils.toString(resEntity);
				Log.d("GeoStore", "remote service response:");
				
				//Workaround for html page
				if(responseText.contains("RESTful")){
					return true;
				}
			}

		}catch (Exception e) {
			Log.w("GeoStore Client","the test url returned an exception");
			
		}
		return false;
		
	}
	
	/**
	 * Generates a string suitable as BasicAuth header value
	 * @param login
	 * @param pass
	 * @return
	 */
    public static String getB64Auth( String login, String pass ) {
        String source = login + ":" + pass;
        String ret = "Basic " + Base64.encodeToString(source.getBytes(), Base64.URL_SAFE | Base64.NO_WRAP);
        return ret;
    }

}
