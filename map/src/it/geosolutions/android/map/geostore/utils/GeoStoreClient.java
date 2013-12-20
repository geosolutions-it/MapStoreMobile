package it.geosolutions.android.map.geostore.utils;

import it.geosolutions.android.map.geostore.model.Attribute;
import it.geosolutions.android.map.geostore.model.Container;
import it.geosolutions.android.map.geostore.model.GeoStoreAttributeTypeAdapter;
import it.geosolutions.android.map.geostore.model.GeoStoreResourceTypeAdapter;
import it.geosolutions.android.map.geostore.model.Resource;
import it.geosolutions.android.map.geostore.model.ResourceList;
import it.geosolutions.android.map.geostore.model.SearchResult;

import java.io.IOException;
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

public class GeoStoreClient {
	private String url;
	private String username;
	private String password;

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

	public List<Resource> searchResources(String s ) {
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet get = new HttpGet(url + "extjs/search/*"+s+"*");
		if (url == null)
			return new ArrayList<Resource>();
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
				Gson gson = new GsonBuilder()
				// .setDateFormat("yyyy-mm-ddTHH:mm:ss.zzz") //TODO
				// implement date format
				.registerTypeAdapter(Resource[].class,
						new GeoStoreResourceTypeAdapter())
				.registerTypeAdapter(Attribute[].class,
						new GeoStoreAttributeTypeAdapter()).create();
				SearchResult c = gson.fromJson(responseText, SearchResult.class);
				if(c== null) {return null;}
				if(!c.success) {return null;}
				return c.results;
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			Log.e("GeoStore", "HTTP Protocol error");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e("GeoStore", "IOException during HTTP request");
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
				Gson gson = new GsonBuilder()
				// .setDateFormat("yyyy-mm-ddTHH:mm:ss.zzz") //TODO
				// implement date format
				.registerTypeAdapter(Resource[].class,
						new GeoStoreResourceTypeAdapter())
				.registerTypeAdapter(Attribute[].class,
						new GeoStoreAttributeTypeAdapter()).create();
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
				Log.d("GeoStore", "remote service response:");
				Log.d("GeoStore", responseText);
				
				return responseText;

			}

		} catch (ClientProtocolException e) {
			Log.e("GeoStore", "HTTP Protocol error");
		} catch (IOException e) {
			Log.e("GeoStore", "IOException during HTTP request");
		}
		return null;
	}

}
