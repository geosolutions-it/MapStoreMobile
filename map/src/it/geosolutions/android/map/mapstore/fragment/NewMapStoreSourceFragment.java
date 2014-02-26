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
import it.geosolutions.android.map.model.stores.MapStoreLayerStore;
import it.geosolutions.android.map.utils.LocalPersistence;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

/**
 * This fragment shows a form to create a new MapStore Resource
 * 
 * @author Lorenzo Natali (www.geo-solutions.it)
 */
public class NewMapStoreSourceFragment extends SherlockFragment implements
		LayerStoreProvider {
	private static final int MESSAGE_OK = 0;
	private static final int MESSAGE_ERROR = 1;
	public static class PARAMS{
		public static final String STORE ="STORE";
	}
	private MapStoreLayerStore store = new MapStoreLayerStore();
	private List<LayerStore>  stores;
	/**
	 * Called only once
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		setHasOptionsMenu(true);
		MapStoreLayerStore store = (MapStoreLayerStore) getActivity().getIntent().getSerializableExtra(PARAMS.STORE);
		if(store !=null){
			this.store = store;
			getSources();
			//if a store is equal so is the same.
			//this workaround allow to edit the source
			//and add it to the list with the changes
			
			
      	  	
			
		}
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
		
		//fill fields
		if(store != null){
			View v = getView();
			EditText url = (EditText) v.findViewById(R.id.url);
			EditText name = (EditText) v.findViewById(R.id.name); 
      	  	EditText description = (EditText) v.findViewById(R.id.description);
      	  	name.setText(this.store.getName());
      	  	description.setText(this.store.getDescription());
      	  	url.setText(this.store.getUrl());
		}
		
		//Test button handler
		Button b = (Button) view.findViewById(R.id.button_test);
		final EditText txtEdit = (EditText)view.findViewById(R.id.url);

		b.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				checkAndSetUrl(txtEdit);
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
	@SuppressWarnings("unchecked")
	@Override
	public List<LayerStore> getSources() {
		if(stores ==null){
		stores = (List<LayerStore>) LocalPersistence
				.readObjectFromFile(getSherlockActivity(),
						LocalPersistence.SOURCES);
		}
		return stores;
	}

	private void saveSources(List<LayerStore> sources) {
		 LocalPersistence.writeObjectToFile(this.getActivity(), sources, LocalPersistence.SOURCES);
		
	}
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		inflater.inflate(R.menu.save, menu);

	}
	
	//test dialog
	public ProgressDialog loadingdialog;
	protected boolean isSaving;
	/**
	 * Handler to manage the 
	 */
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			loadingdialog.dismiss();
			switch (msg.what) {
			case MESSAGE_OK:
				
				if(isSaving){
					continueSaving();
				}else{
					Toast.makeText(getActivity(), R.string.mapstore_url_correct,
							Toast.LENGTH_LONG).show();
				}
				break;
			case MESSAGE_ERROR:
				
				if(isSaving){
					warnGeoStoreURLCheckMissing();
				}else{
					Toast.makeText(getActivity(), R.string.warning_geostore_url_not_verified,
							Toast.LENGTH_LONG).show();
				}
				break;
			default:
				break;
			}
		}
	};
	 
	  
	  /**
	 * 
	 */
	protected void stopSaving() {
		isSaving = false;
		
	}

	/**
	 * Warn the user the GeoStore URL is not verified.
	 * Ask if the user want to continue saving or not
	 */
	protected void warnGeoStoreURLCheckMissing() {
		new AlertDialog.Builder(this.getActivity())
        .setIcon(android.R.drawable.ic_dialog_alert)
        .setMessage(getString(R.string.warning_geostore_url_not_verified)+getString(R.string.question_do_you_want_to_continue_saving) )
        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                continueSaving();
            }

        })
        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                stopSaving();
            }

        }).show();

        
		
	}

	/**
	 * Continue the saving process.
	 * From here the GeoStore URL is verified so the fragment has to simply
	 * save the source in sources and stop the current activity
	 */
	protected void continueSaving() {
		loadingdialog = ProgressDialog.show(this.getActivity(),"",getString(R.string.saving_source),true);
		new Thread() {
	          public void run() {
	              try {
	            	  EditText url = (EditText)getView().findViewById(R.id.url);
	            	  EditText name = (EditText)getView().findViewById(R.id.name); 
	            	  EditText description = (EditText)getView().findViewById(R.id.description);
	            	  
	            	  if(store!=null){
	            		  if(stores==null){
	            			  getSources();
	            			  
	            		  }
	            		  for (LayerStore st : stores){
	          				if(st instanceof MapStoreLayerStore && ((MapStoreLayerStore)st).equals(store)){
	          					stores.remove(st);
	          				}
	          				}
	            		  store.setDescription(description.getText().toString());
	            		  store.setName(name.getText().toString());
	            		  store.setUrl(url.getText().toString());
	            		 
	            		  stores.add(store);
	            		  saveSources(stores);
	            		  stopSaving();
	            		  
	            		  getActivity().finish();
	            	  }
	              } catch(Exception e) {
	                Log.e("NEW SOURCE","error saving the new source");
	                e.printStackTrace();
	                
	              }finally{
	            	  loadingdialog.dismiss(); 
	            	  stopSaving();
	              }
	          }

			
	      }.start();
		
	}

	/**
	   * Start a test on the GeoStore URL
	   * @param geoStoreURL
	   */
	  public void startTest(final String geoStoreURL) {
	      loadingdialog = ProgressDialog.show(this.getActivity(),"",getString(R.string.verifying_mapstore_url),true);
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
	 
	  
	  
	 /* (non-Javadoc)
	 * @see com.actionbarsherlock.app.SherlockFragment#onOptionsItemSelected(com.actionbarsherlock.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		//saving start a thread to check the GeoStoreURL
		if(item.getItemId() == R.id.save){
			isSaving = true;
			EditText txtEdit = (EditText)getView().findViewById(R.id.url);
			final EditText name =(EditText)getView().findViewById(R.id.name);
			if("".equals(name.getText().toString())){
				name.setError(getString(R.string.field_required));	
				stopSaving();
			}else if("".equals(txtEdit.getText().toString())){
				txtEdit.setError(getString(R.string.field_required));
				stopSaving();
			}else{
				checkAndSetUrl(txtEdit);
			}
			
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Adjust and starts a test for the GeoStore URL
	 * @param txtEdit
	 */
	private void checkAndSetUrl(final EditText txtEdit) {
		String url = txtEdit.getText().toString();
		url = adjustGeoStoreUrl(url);
		if(url ==null){
			Toast.makeText(getActivity(), "URL not well formed", Toast.LENGTH_LONG).show();//TODO i18N
			return;
		}
		txtEdit.setText(url);
		startTest(url);
	}
	
	/**
	 * Auto correction of the geostore URL. add http:// and path to geostore rest if missing
	 * @param url the URL the user created
	 * @return
	 */
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
	  
	
}
