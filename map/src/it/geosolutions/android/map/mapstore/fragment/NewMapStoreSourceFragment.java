/*
 * GeoSolutions map - Digital field mapping on Android based devices
 * Copyright (C) 2013  GeoSolutions (www.geo-solutions.it)
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
package it.geosolutions.android.map.mapstore.fragment;

import it.geosolutions.android.map.R;
import it.geosolutions.android.map.fragment.sources.LayerStoreProvider;
import it.geosolutions.android.map.geostore.utils.GeoStoreClient;
import it.geosolutions.android.map.model.stores.LayerStore;
import it.geosolutions.android.map.utils.LocalPersistence;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

/**
 * This fragment shows a form to create a new MapStore Resource
 * 
 * @author Lorenzo Natali (www.geo-solutions.it)
 */
public class NewMapStoreSourceFragment extends SherlockFragment implements
		LayerStoreProvider {
	private static final int MESSAGE_OK = 0;
	private static final int MESSAGE_ERROR = 1;
	/**
	 * Called only once
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);

		return inflater.inflate(R.layout.mapstore_new_resource, container,
				false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		Button b = (Button) view.findViewById(R.id.button_test);
		final EditText txtEdit = (EditText)view.findViewById(R.id.url);
		b.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String url = txtEdit.getText().toString();
				url = adjustGeoStoreUrl(url);
				if(url ==null){
					Toast.makeText(getActivity(), "URL not well formed", Toast.LENGTH_LONG).show();//TODO i18N
					return;
				}
				txtEdit.setText(url);
				startTest(url);
				
			}

			private String adjustGeoStoreUrl(String url) {
				if( !url.startsWith("http://")){
					url = "http://" + url;
				}
				
				URL urlObj=null;
				try {
					urlObj = new URL(url);
				} catch (MalformedURLException e) {
					return null;
				}
				
				String path  =  urlObj.getPath();
				if("/".equals(path)|| "".equals(path)||path==null){
					url = "http://"+urlObj.getHost() + "/geostore/rest/";
					return url;
				}
				if(path.endsWith("/rest/")){
					return url;
				}else{
					return null;
				}
				
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.geosolutions.android.map.fragment.sources.LayerStoreProvider#getSources
	 * ()
	 */
	@Override
	public List<LayerStore> getSources() {
		@SuppressWarnings("unchecked")
		List<LayerStore> stores = (List<LayerStore>) LocalPersistence
				.readObjectFromFile(getSherlockActivity(),
						LocalPersistence.SOURCES);
		return stores;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		inflater.inflate(R.menu.save_undo, menu);

	}
	
	//test dialog
	public ProgressDialog loadingdialog;
	  private Handler handler = new Handler() {
	          @Override
	              public void handleMessage(Message msg) {
	              loadingdialog.dismiss();
	              switch (msg.what) {
				case MESSAGE_OK:
					Toast.makeText(getActivity(), "The url is correct :D", Toast.LENGTH_LONG).show();//TODO i18N
					break;
				case MESSAGE_ERROR:
					Toast.makeText(getActivity(), "the service is not available", Toast.LENGTH_LONG).show();//TODO i18N
					break;
				default:
					break;
				}
	              ShowManager();

	          }
	      };
	  public void ShowManager()
	  {
//	      TextView mainText = (TextView) findViewById(R.id.wifiText);
//	      mainText.setText("editted");
	  }
	  
	  public void startTest(final String geoStoreURL) {
	      loadingdialog = ProgressDialog.show(this.getActivity(),"","Scanning Please Wait",true);
	      new Thread() {
	          public void run() {
	              try {
	                  GeoStoreClient c = new GeoStoreClient();
	                  c.setUrl(geoStoreURL);
	                  if(c.test()){;
	                	  handler.sendEmptyMessage(MESSAGE_OK);//OK
	                  }else{
	                	  handler.sendEmptyMessage(MESSAGE_ERROR);//ERROR
	                  }
	                  

	              } catch(Exception e) {
	                  Log.e("threadmessage",e.getMessage());
	              }
	          }
	      }.start();
	  }

	  
	
}
