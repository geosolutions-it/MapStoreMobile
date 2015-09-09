package it.geosolutions.android.map.geostore.utils;

import it.geosolutions.android.map.geostore.model.Attribute;
import it.geosolutions.android.map.geostore.model.Container;
import it.geosolutions.android.map.geostore.model.GeoStoreAttributeTypeAdapter;
import it.geosolutions.android.map.geostore.model.GeoStoreResourceTypeAdapter;
import it.geosolutions.android.map.geostore.model.Resource;
import it.geosolutions.android.map.geostore.model.ResourceContainer;
import it.geosolutions.android.map.geostore.model.ResourceList;
import it.geosolutions.android.map.geostore.model.SearchResult;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class GeoStoreClient {
	private String url;
	private String username;
	private String password;
	public int totalCount=0;
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<Resource> searchResources(String s,int start,int limit) {
		HttpClient httpclient = new DefaultHttpClient();
		try {
			s = URLEncoder.encode(s,"utf-8");
		} catch (UnsupportedEncodingException e1) {
			Log.e("GeoStore","unable to encode search text\n" + e1.getStackTrace());
			return new ArrayList<Resource>();
		}
		String req = url + "extjs/search/*"+s+"*?start="+start+"&limit="+limit;
		Log.v("GeoStore","request_url:"+req);
		HttpGet get = new HttpGet(req);
		if (url == null)
			return new ArrayList<Resource>();
		{
			Log.w("GeoStore", "URL Not Present. Unable to submit the request");
		}
		get.addHeader("Accept", "application/json");
		
		HttpResponse response;
		// TODO support pagination, filtering, account
		String responseText = null;

		try {
			response = httpclient.execute(get);
			HttpEntity resEntity = response.getEntity();
			if (resEntity != null) {
				
				// parse response.
				responseText = EntityUtils.toString(resEntity);
				Log.d("GeoStore", "remote service response:");
				Log.d("GeoStore", responseText);
				Gson gson = getGeoStoreGsonBuilder();
				SearchResult c = gson.fromJson(responseText, SearchResult.class);
				if(c!= null) {
					if(c.success) {
						totalCount=c.totalCount;
						return c.results;
					}else{
						return null;
					}
				}
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			Log.e("GeoStore", "HTTP Protocol error");
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e("GeoStore", "IOException during HTTP request");
			return null;
		} catch (IllegalArgumentException e){
			Log.e("GeoStore","Unable to parse the response:"+responseText);
			Log.e("GeoStore","Error:"+e.getMessage());
			return null;
		}catch (JsonSyntaxException e){
			Log.e("GeoStore","Unable to parse the response:"+responseText);
			Log.e("GeoStore","Error:"+e.getMessage());
			return null;
		}
		return new ArrayList<Resource>();
	}
	
	public List<Resource> getResources() {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet get = new HttpGet(url + "resources/");
		if (url == null)
			;
		{
			Log.w("GeoStore", "URL Not Present. Unable to submit the request");
		}
		get.addHeader("Accept", "application/json");
		HttpResponse response;
		// TODO support pagination, filtering, account
		try {
			response = httpclient.execute(get);

			HttpEntity resEntity = response.getEntity();
			String responseText;
			if (resEntity != null) {
				// parse response.
				responseText = EntityUtils.toString(resEntity);
				Log.d("GeoStore", "remote service response:");
				Log.d("GeoStore", responseText);
				Gson gson = getGeoStoreGsonBuilder();
				Container ctnrl = gson.fromJson(responseText, Container.class);
				ResourceList rl = ctnrl.resourceList;
				return rl.list;
			}

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			Log.e("GeoStore", "HTTP Protocol error");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e("GeoStore", "IOException during HTTP request");
		}
		return null;
	}
	
	public Resource getResource(int id){
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet get = new HttpGet(url + "resources/resource/"+id);
		if (url == null)
			;
		{
			Log.w("GeoStore", "URL Not Present. Unable to submit the request");
		}
		get.addHeader("Accept", "application/json");
		HttpResponse response;
		// TODO support pagination, filtering, account
		try {
			response = httpclient.execute(get);

			HttpEntity resEntity = response.getEntity();
			String responseText;
			if (resEntity != null) {
				// parse response.
				responseText = EntityUtils.toString(resEntity);
				Log.d("GeoStore", "remote service response:");
				Log.d("GeoStore", responseText);
				Gson gson = getGeoStoreGsonBuilder();
				ResourceContainer resc = gson.fromJson(responseText, ResourceContainer.class);
				
				return resc.resource;
			}

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			Log.e("GeoStore", "HTTP Protocol error");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e("GeoStore", "IOException during HTTP request");
		}
		return null;
	}

	/**
	 * Provides the Gson object that recongnize Resource and Attributes as arrays or single object.
	 * 
	 * @return
	 */
	private Gson getGeoStoreGsonBuilder() {
		Type resourceListType = new TypeToken<List<Resource>>(){}.getType();
		Type attributeListType = new TypeToken<List<Attribute>>(){}.getType();
		return new GsonBuilder()
		// .setDateFormat("yyyy-mm-ddTHH:mm:ss.zzz") //TODO
		// implement date formatsource>).class,
		.registerTypeAdapter(resourceListType,
				new GeoStoreResourceTypeAdapter())
		.registerTypeAdapter(attributeListType,
				new GeoStoreAttributeTypeAdapter()).create();
	}
	
	
	public String getData(Long id) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet get = new HttpGet(url + "data/" + id);
		get.addHeader("Accept", "application/json");

		if (url == null)
			;
		{
			Log.w("GeoStore", "URL Not Present. Unable to submit the request");
		}
		// get.addHeader("Accept","application/json");
		HttpResponse response;
		// TODO support pagination, filtering, account
		try {
			response = httpclient.execute(get);

			HttpEntity resEntity = response.getEntity();
			String responseText;
			if (resEntity != null) {
				// parse response.
				responseText = EntityUtils.toString(resEntity);
				return responseText;

			}

		} catch (ClientProtocolException e) {
			Log.e("GeoStore", "HTTP Protocol error");
		} catch (IOException e) {
			Log.e("GeoStore", "IOException during HTTP request");
		}
		return null;
	}

	/**
	 * 
	 */
	public boolean test() {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet get = new HttpGet(url);
		get.addHeader("Accept", "application/json");
		HttpResponse response;
		// TODO support pagination, filtering, account
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

}
