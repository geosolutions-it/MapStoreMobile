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
package it.geosolutions.android.map.fragment.sources;

import it.geosolutions.android.map.R;
import it.geosolutions.android.map.activities.NewSourceActivity;
import it.geosolutions.android.map.adapters.LayerStoreAdapter;
import it.geosolutions.android.map.common.Constants;
import it.geosolutions.android.map.model.stores.LayerStore;
import it.geosolutions.android.map.utils.LocalPersistence;

import java.util.ArrayList;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

/**
 * This fragment shows a list of Sources from the local storage.
 * Allow to edit and add new Sources. Implements CAB for long press 
 * and item selection allow source delete.
 * 
 * 
 * @author Lorenzo Natali (www.geo-solutions.it)
 */
public class SourcesFragment extends SherlockListFragment implements LayerStoreProvider,LoaderCallbacks<List<LayerStore>>,ActionMode.Callback {

private static final int LOADER_INDEX = 50;
private static final String CONTENTS = "MSM_CONTENT";
private LayerStoreAdapter adapter;
private ActionMode actionMode = null;
private ArrayList<LayerStore> selected = new ArrayList<LayerStore>();
private List<LayerStore> stores = null;
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
    
    adapter = new LayerStoreAdapter(getSherlockActivity(),
            R.layout.sources_row,this);
    setListAdapter(adapter);
    //star loading Layers
    getSherlockActivity().getSupportLoaderManager().initLoader(LOADER_INDEX, null, this);
    return inflater.inflate(R.layout.sources_fragment, container,
            false);
}

@Override
public void onViewCreated(View view, Bundle savedInstanceState) {
	//set the listener for add button
    ImageButton add = (ImageButton) view.findViewById(R.id.sources_add);
    add.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent i = new Intent(getActivity(),NewSourceActivity.class);
			getActivity().startActivityForResult(i,Constants.requestCodes.CREATE_SOURCE);
			
		}
	});
    
    //
    //Set Contextual ACTION BAR CALLBACKS
    //
    final SourcesFragment callback = this;
    ListView lv = getListView();
    lv.setLongClickable(true);
    lv.setClickable(true);
    lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE); 
    //edit - delete
    lv.setOnItemLongClickListener(new OnItemLongClickListener() {
    	   public boolean onItemLongClick (AdapterView<?> parent, View view, int position, long id) {
    	     LayerStore sel = adapter.getItem(position);
    	     
    	     if(!selected.contains(sel)){
    	    	 getListView().setItemChecked(position, true);
    	    	 selected.add(sel);
    	    	 
    	     }else{
    	    	 getListView().setItemChecked(position, false);
    	    	 selected.remove(sel);
    	     }
    	     int numSelected = selected.size();
    	     if(numSelected>0){
    	    	 if(actionMode != null){
    	    		 updateCAB(numSelected);
    	    	 }else{
	    	    	 actionMode = getSherlockActivity().startActionMode(callback);
	    	    	 //override the done button to deselect all when the button is pressed
	    	    	 int doneButtonId = Resources.getSystem().getIdentifier("action_mode_close_button", "id", "android");
	    	    	 View doneButton = getActivity().findViewById(doneButtonId);
	    	    	 doneButton.setOnClickListener(new View.OnClickListener() {
	
	    	    	     @Override
	    	    	     public void onClick(View v) {
	    	    	         getListView().clearChoices();
	    	    	         selected = new ArrayList<LayerStore>();
	    	    	         actionMode.finish();
	    	    	     }
	    	    	 });
    	    	 }
    	     }else{
    	    	 if(actionMode !=null){
    	    		 actionMode.finish();
    	    	 }
    	     }
    	     view.setSelected(true);
    	     return true;
    	   }
	});
    //browse
    lv.setOnItemClickListener(new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			LayerStore s  = (LayerStore)adapter.getItem(position);
			s.openDetails(getSherlockActivity());
		}
	});

    super.onViewCreated(view, savedInstanceState);
}

/* (non-Javadoc)
 * @see it.geosolutions.android.map.fragment.sources.LayerStoreProvider#getSources()
 */
@Override
public List<LayerStore> getSources() {
	stores = (List<LayerStore>)LocalPersistence.readObjectFromFile(getSherlockActivity(), LocalPersistence.SOURCES);
	return stores;
}

/* (non-Javadoc)
 * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onCreateLoader(int, android.os.Bundle)
 */
@Override
public Loader<List<LayerStore>> onCreateLoader(int arg0, Bundle arg1) {
	Loader<List<LayerStore>>  l = new LayerStoreLoader(getSherlockActivity(),this);
	l.forceLoad();
	return l;
}

/* (non-Javadoc)
 * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoadFinished(android.support.v4.content.Loader, java.lang.Object)
 */
@Override
public void onLoadFinished(Loader<List<LayerStore>> loader, List<LayerStore> result) {
	
	adapter.clear();
	ArrayList<LayerStore> ll = new ArrayList<LayerStore>();
	if(result != null){
		
		int size = result.size();
		
		Log.v("SOURCES","Loaded sources:"+size);
		if(size > 0){
			for(LayerStore ls : result){
				adapter.add(ls );
			}
		}
	}

	adapter.notifyDataSetChanged();
	
}

/* (non-Javadoc)
 * @see android.support.v4.app.LoaderManager.LoaderCallbacks#onLoaderReset(android.support.v4.content.Loader)
 */
@Override
public void onLoaderReset(Loader<List<LayerStore>> arg0) {
	adapter.clear();
	
}

/**
 * reloadStores and clean Contextual Action Bar if present
 */
public void reloadStores(){
	Log.v("SOURCES","reloading sources");
	Loader l = getSherlockActivity().getSupportLoaderManager().getLoader(LOADER_INDEX);
	if(l!=null){
		adapter.clear();
		l.forceLoad();
	}
	if(actionMode!=null){
		actionMode.finish();
		selected = new ArrayList<LayerStore>();
		getListView().clearChoices();
		getListView().clearFocus();
	}
}

// ACTION MODE CALLBACKS
public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
	int number =selected.size();
	updateCAB(number);
	return false;
}


public void onDestroyActionMode(ActionMode mode) {
	selected = new ArrayList<LayerStore>();
	getListView().clearChoices();
	getListView().clearFocus();
	actionMode = null;
	adapter.notifyDataSetChanged();
}

public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	mode.getMenuInflater().inflate(R.menu.delete_menu, menu);
	this.actionMode =mode;
	
	return true;
}

public boolean onActionItemClicked(ActionMode mode, MenuItem menu) {
	
	if(menu.getItemId()==R.id.delete){
		stores.removeAll(selected);
		saveSources(stores);
	}else if(menu.getItemId() == R.id.edit){
		LayerStore ls = selected.get(0);
		if(ls!=null){
			ls.openEdit(getSherlockActivity());
		}
	}
	selected = new ArrayList<LayerStore>();
	getListView().clearChoices();
	getListView().clearFocus();
	mode.finish();
	reloadStores();
	actionMode=null;
	return true;

}

	private void saveSources(List<LayerStore> sources) {
	 LocalPersistence.writeObjectToFile(this.getActivity(), sources, LocalPersistence.SOURCES);
	
	}

/**
 * Update the contextual action bar for the number of item selected
 * @param numSelected
 */
private void updateCAB(int numSelected) {
	if(actionMode == null) return ;

	Menu menu = actionMode.getMenu();
	if(numSelected == 1){
		if(selected.get(0).canEdit()){
			menu.findItem(R.id.edit).setVisible(true);
		}else{
			menu.findItem(R.id.edit).setVisible(false);
		}
	}else{
		menu.findItem(R.id.edit).setVisible(false);
	}
	String title = getResources().getQuantityString(R.plurals.quantity_sources_selected,numSelected,numSelected );
	actionMode.setTitle(title);
}
}
