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
package it.geosolutions.android.map.overlay.switcher;

import it.geosolutions.android.map.MapsActivity;
import it.geosolutions.android.map.R;
import it.geosolutions.android.map.activities.BrowseSourcesActivity;
import it.geosolutions.android.map.adapters.LayerSwitcherAdapter;
import it.geosolutions.android.map.fragment.sources.SourcesFragment;
import it.geosolutions.android.map.geostore.activities.GeoStoreResourcesActivity;
import it.geosolutions.android.map.listeners.LayerChangeListener;
import it.geosolutions.android.map.model.Layer;
import it.geosolutions.android.map.model.stores.LayerStore;
import it.geosolutions.android.map.overlay.managers.MultiSourceOverlayManager;
import it.geosolutions.android.map.overlay.managers.OverlayManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemLongClickListener;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

/**
 * This fragment shows a view o the attributes of a single feature from a
 * feature passed as Extra. 
 * 
 * Is binded with The <SimpleOverlayManager> methods.
 * 
 * This object is retained, but the activity itself is recreated. So it has also 
 * to provide to the loaders that initializes all the data. This is why it implements <LayerProvider>.
 * 
 * Catch events from <LayerChangeListener> to be sincronized with add/changes to the map.
 * 
 * When things change (visibility,add) the list and the map are refreshed.
 * 
 * 
 * @author Lorenzo Natali (www.geo-solutions.it)
 */
public class LayerSwitcherFragment extends SherlockListFragment implements LayerChangeListener, LoaderCallbacks<List<Layer>>, LayerProvider,ActionMode.Callback {
private int LOADER_INDEX =1290;
private LayerSwitcherAdapter adapter;
private LayerListLoader loader;
private boolean isLoading = false;
private ActionMode actionMode;
private ArrayList<Layer<?>> selected = new ArrayList<Layer<?>>();

/**
 * Called only once
 */
@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
   
}

@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    //set the adapter for the layers
    adapter = new LayerSwitcherAdapter(getSherlockActivity(),
            R.layout.layer_checklist_row,this);
    setListAdapter(adapter);
    //star loading Layers
    
    getSherlockActivity().getSupportLoaderManager().initLoader(LOADER_INDEX, null, this);
    
    //return the layout inflater
    return inflater.inflate(R.layout.layer_switcher, container, false);
}

/* (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		 getSherlockActivity().getSupportLoaderManager().initLoader(LOADER_INDEX, null, this);
	}

/*
 * (non-Javadoc)
 * @see android.support.v4.app.ListFragment#onViewCreated(android.view.View,
 * android.os.Bundle)
 */
@Override
public void onViewCreated(View view, Bundle savedInstanceState) {
    final MapsActivity ac = (MapsActivity)getActivity(); 
    
    //the map selection activity launcher
    ImageButton msEd = (ImageButton) view.findViewById(R.id.layer_add);
	msEd.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
		    Intent pref = new Intent(ac,BrowseSourcesActivity.class);
		    ac.startActivityForResult(pref, MapsActivity.LAYER_ADD);
		}
	});
	
	//force reload
	reload();
	
	//
    //Set Contextual ACTION BAR CALLBACKS
    //
    final LayerSwitcherFragment callback = this;
    ListView lv = getListView();
    lv.setLongClickable(true);
    lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE); 
    
    lv.setOnItemLongClickListener(new OnItemLongClickListener() {
    	   public boolean onItemLongClick (AdapterView<?> parent, View view, int position, long id) {
    	     System.out.println("Long click");
    	     Layer<?> sel = adapter.getItem(position);
    	     
    	     if(!selected.contains(sel)){
    	    	 getListView().setItemChecked(position, true);
    	    	 selected.add(sel);
    	    	 
    	     }else{
    	    	 getListView().setItemChecked(position, false);
    	    	 selected.remove(sel);
    	     }
    	     if(selected.size()>0){
    	    	 actionMode = getSherlockActivity().startActionMode(callback);
    	    	 //override the done button to deselect all when the button is pressed
    	    	 int doneButtonId = Resources.getSystem().getIdentifier("action_mode_close_button", "id", "android");
    	    	 View doneButton = getActivity().findViewById(doneButtonId);
    	    	 doneButton.setOnClickListener(new View.OnClickListener() {

    	    	     @Override
    	    	     public void onClick(View v) {
    	    	         getListView().clearChoices();
    	    	         selected = new ArrayList<Layer<?>>();
    	    	         actionMode.finish();
    	    	     }
    	    	 });
    	    	 
    	     }else{
    	    	 if(actionMode !=null){
    	    		 actionMode.finish();
    	    	 }
    	     }
    	     view.setSelected(true);
    	     return true;
    	   }
	});
    
    super.onViewCreated(view, savedInstanceState);
    // setup of the checkboxes
}

@Override
public Loader<List<Layer>> onCreateLoader(int arg0, Bundle arg1) {
	 this.loader = new LayerListLoader(getSherlockActivity(),this);
	return this.loader;
}

@Override
public void onLoadFinished(Loader<List<Layer>> arg0, List<Layer> layers) {
	isLoading =true;
	adapter.clear();
	ArrayList<Layer> ll = new ArrayList<Layer>();
	int size = layers.size();
	//reverse add to the layer list to 
	//have the checkbox stacked as the layers
	if(size > 0){
		for(int i = size-1 ; i >=0 ; i--){
			adapter.add( layers.get(i) );
		}
	}
	isLoading =false;
	adapter.notifyDataSetChanged();
}

@Override
public void onLoaderReset(Loader<List<Layer>> layers) {
	adapter.clear();
}

@Override
public void onSetLayers(ArrayList<Layer> layers) {
	reload();
}
private void reload() {
	if(adapter !=null){
		adapter.clear();
		//force reload
		Loader<?> l = getSherlockActivity().getSupportLoaderManager().getLoader(LOADER_INDEX);
		if(l!=null){
			l.forceLoad();
		}else{
			Log.e("LAYER_SWITCHER", "Unable to reload layers");
		}
		
	}
	
}

/**
 * privide the overlayManager binded to this layer switcher
 * @return
 */
public MultiSourceOverlayManager getOverlayManager(){
	final MapsActivity ac = (MapsActivity)getActivity(); 
    return (MultiSourceOverlayManager) ac.overlayManager;
}

@Override
public void onLayerVisibilityChange(Layer layer) {
	if(!isLoading ){
		getOverlayManager().redrawLayer(layer);
	}
}

@Override
public ArrayList<Layer> getLayers() {
	//returns the layers from the current overlayManager
	//NOTE: this is needed because the instance of the overlay manager
	// can change during time. It needs to be get from the current
	// activity.
	return getOverlayManager().getLayers();
}

//ACTION MODE CALLBACKS
public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
	ListView lv = getListView();
	Resources res = getResources();
	int number =selected.size();
	String title = res.getQuantityString(R.plurals.quantity_sources_selected,number,number );
	mode.setTitle(title);
	
	return false;
}


public void onDestroyActionMode(ActionMode mode) {
	adapter.notifyDataSetChanged();
}

public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	mode.getMenuInflater().inflate(R.menu.delete_menu, menu);
	this.actionMode =mode;
	
	return true;
}

public boolean onActionItemClicked(ActionMode mode, MenuItem menu) {
	
	if(menu.getItemId()==R.id.delete){
		ArrayList<Layer> layers = getLayers();
		layers.removeAll(selected);
		getOverlayManager().setLayers(new ArrayList<Layer>(layers));
		getOverlayManager().forceRedraw();
		

	}
	selected = new ArrayList<Layer<?>>();
	mode.finish();
	getListView().clearChoices();
	getListView().clearFocus();
	actionMode=null;
	return true;

}

}


