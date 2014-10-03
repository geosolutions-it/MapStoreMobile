/*
 * GeoSolutions map - Digital field mapping on Android based devices
 * Copyright (C) 2013 - 2014 GeoSolutions (www.geo-solutions.it)
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
import it.geosolutions.android.map.activities.MBTilesLayerOpacitySettingActivity;
import it.geosolutions.android.map.adapters.LayerSwitcherAdapter;
import it.geosolutions.android.map.listeners.LayerChangeListener;
import it.geosolutions.android.map.mbtiles.MbTilesLayer;
import it.geosolutions.android.map.model.Layer;
import it.geosolutions.android.map.overlay.managers.MultiSourceOverlayManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
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
import android.widget.TextView;

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
 * to provide to the loaders that initializes all the data. This is why it
 * implements <LayerProvider>.
 * 
 * Catch events from <LayerChangeListener> to be synchronized with add/changes to
 * the map.
 * 
 * When things change (visibility,add) the list and the map are refreshed.
 * 
 * 
 * @author Lorenzo Natali (www.geo-solutions.it)
 */
public class LayerSwitcherFragment extends SherlockListFragment implements
		LayerChangeListener, LoaderCallbacks<List<Layer>>, LayerProvider,
		ActionMode.Callback {
	private int LOADER_INDEX = 1290;
	private LayerSwitcherAdapter adapter;
	private LayerListLoader loader;
	private boolean isLoading = false;
	private ActionMode actionMode;
	private ArrayList<Layer<?>> selected = new ArrayList<Layer<?>>();
	
	private boolean mbTilesLayerSelected = false;
	public final static int OPACITY_SETTIN_REQUEST_ID = 999;

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
		// set the adapter for the layers
    adapter = new LayerSwitcherAdapter(getSherlockActivity(),
				R.layout.layer_checklist_row, this);
    setListAdapter(adapter);
		// star loading Layers
    
		getSherlockActivity().getSupportLoaderManager().initLoader(
				LOADER_INDEX, null, this);
    
		// return the layout inflater
    return inflater.inflate(R.layout.layer_switcher, container, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();
		getSherlockActivity().getSupportLoaderManager().initLoader(
				LOADER_INDEX, null, this);
	}

	/*
 * (non-Javadoc)
	 * 
 * @see android.support.v4.app.ListFragment#onViewCreated(android.view.View,
 * android.os.Bundle)
 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		final MapsActivity ac = (MapsActivity) getActivity();
    
		// the map selection activity launcher
    ImageButton msEd = (ImageButton) view.findViewById(R.id.layer_add);
	msEd.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			closeActionMode();
				Intent pref = new Intent(ac, BrowseSourcesActivity.class);
		    ac.startActivityForResult(pref, MapsActivity.LAYER_ADD);
		}
	});
		
		// reset the ActionBar ActionMode
	closeActionMode();
		
		// force reload
	reload();
	
	//
		// Set Contextual ACTION BAR CALLBACKS
    //
	final LayerSwitcherFragment callback = this;
	ListView lv = getListView();
	lv.setLongClickable(true);
	lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE); 

	lv.setOnItemLongClickListener(new OnItemLongClickListener() {
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			Log.d(LayerSwitcherFragment.class.getSimpleName(), "layer long pressed : "+position);
			Layer<?> sel = adapter.getItem(position);
			
			if(sel instanceof MbTilesLayer){
				mbTilesLayerSelected = true;
			}else{
				mbTilesLayerSelected = false;
			}
			boolean wasDeselected = false;
			if (!selected.contains(sel)) {
				selected.add(sel);
			} else {
				wasDeselected = true;
				selected.remove(sel);
			}
			updateSelected();
			int numSelected = selected.size();
			if (numSelected > 0) {
				if (actionMode != null) {
					updateCAB(numSelected,wasDeselected);
				} else {
					actionMode = getSherlockActivity().startActionMode(
							callback);
					// override the done button to deselect all when the
					// button is pressed
					int doneButtonId = Resources.getSystem().getIdentifier(
							"action_mode_close_button", "id", "android");
					View doneButton = getActivity().findViewById(
							doneButtonId);
					doneButton
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							closeActionMode();
						}
					});
				}
			} else {
				closeActionMode();
			}

			return true;
		}

	});
    
    lv.setOnItemClickListener(new OnItemClickListener() {

		@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			updateSelected();
			
		}
	});
    
    super.onViewCreated(view, savedInstanceState);
    // setup of the checkboxes
	}

	@Override
	public Loader<List<Layer>> onCreateLoader(int arg0, Bundle arg1) {
		this.loader = new LayerListLoader(getSherlockActivity(), this);
	return this.loader;
	}

	@Override
	public void onLoadFinished(Loader<List<Layer>> arg0, List<Layer> layers) {
		isLoading = true;
	adapter.clear();
	ArrayList<Layer> ll = new ArrayList<Layer>();
	int size = layers.size();
	setLoading();
		// reverse add to the layer list to
		// have the checkbox stacked as the layers
		if (size > 0) {

			// prevents notifyDataSetChanged()
			// to be called on each add()
			adapter.setNotifyOnChange(false);

			for (int i = size - 1; i >= 0; i--) {
				adapter.add(layers.get(i));
		}
		} else {
		setNoData();
	}
		isLoading = false;
	adapter.notifyDataSetChanged();
	}

	/**
 * Set the GUI to display loading info
 */
	private void setLoading() {
		getView().findViewById(R.id.progress_bar).setVisibility(
				TextView.VISIBLE);
    ((TextView) getView().findViewById(R.id.empty_text))
    .setText(R.string.loading_layers);
	}

	/**
 * Set the GUI to show no data is present
 */
	private void setNoData() {
    getView().findViewById(R.id.progress_bar).setVisibility(TextView.GONE);
    ((TextView) getView().findViewById(R.id.empty_text))
    .setText(R.string.no_layer_loaded);
	}

	@Override
	public void onLoaderReset(Loader<List<Layer>> layers) {
	adapter.clear();
	}

	@Override
	public void onSetLayers(ArrayList<Layer> layers) {
	
	reload();
	}

	/**
	 * Clears the adapter and triggers a loader reload
	 */
	private void reload() {
		if (adapter != null) {
			adapter.clear();
			// force reload
			Loader<?> l = getSherlockActivity().getSupportLoaderManager()
					.getLoader(LOADER_INDEX);
			if (l != null) {
				l.forceLoad();
			} else {
				Log.e("LAYER_SWITCHER", "Unable to reload layers");
			}

		}

	}

	/**
	 * Provide the overlayManager binded to this layer switcher
	 * 
 * @return
 */
	public MultiSourceOverlayManager getOverlayManager() {
		final MapsActivity ac = (MapsActivity) getActivity();
    return (MultiSourceOverlayManager) ac.overlayManager;
	}

	@Override
	public void onLayerVisibilityChange(Layer layer) {
		if (!isLoading) {
		getOverlayManager().redrawLayer(layer);
	}
	}

	@Override
	public ArrayList<Layer> getLayers() {
		// returns the layers from the current overlayManager
		// NOTE: this is needed because the instance of the overlay manager
	// can change during time. It needs to be get from the current
	// activity.
	return getOverlayManager().getLayers();
	}

	// ACTION MODE CALLBACKS
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		Log.d(LayerSwitcherFragment.class.getSimpleName(), "onPrepareActionMode");
		ListView lv = getListView();
		Resources res = getResources();
		int number = selected.size();
		updateCAB(number,false);

		return false;
	}

	public void onDestroyActionMode(ActionMode mode) {
		Log.d(LayerSwitcherFragment.class.getSimpleName(), "onDestroyActionmode ");
		ListView lv = getListView();
		lv.clearFocus();
		lv.clearChoices();
		selected = new ArrayList<Layer<?>>();
		if (this.actionMode != null) {
			this.actionMode = null;
		}
		adapter.notifyDataSetChanged();
	}

	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		Log.d(LayerSwitcherFragment.class.getSimpleName(), "onCreateActionMode");
		if(mbTilesLayerSelected){
			mode.getMenuInflater().inflate(R.menu.edit_delete_up_down, menu);
			//for some reasons the icon is dark, though this is the holo_dark icon
			menu.findItem(R.id.edit).getIcon().mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
		}else{
			mode.getMenuInflater().inflate(R.menu.delete_up_down, menu);
		}
		this.actionMode = mode;
	
	return true;
	}

	public boolean onActionItemClicked(ActionMode mode, MenuItem menu) {
	int itemId = menu.getItemId();
	ArrayList<Layer> layers = getLayers();
	Log.d(LayerSwitcherFragment.class.getSimpleName(), "actionitemclicked "+itemId);
	ListView lv = getListView();
	MultiSourceOverlayManager om = getOverlayManager();
	
		if(itemId == R.id.edit){
			getActivity().startActivityForResult(new Intent(getActivity(), MBTilesLayerOpacitySettingActivity.class),OPACITY_SETTIN_REQUEST_ID);
		}else if (itemId == R.id.delete) {
		layers.removeAll(selected);
			om.setLayers(new ArrayList<Layer>(layers), true);
		om.forceRedraw();
		closeActionMode();
	
		} else if (itemId == R.id.up || itemId == R.id.down) {
			// check if already on tom or bottom
		int size = layers.size();
		List<Integer> indexes = new ArrayList<Integer>();
			for (Layer<?> l : selected) {
			indexes.add(layers.indexOf(l));
		}
		int min = Collections.min(indexes);
		int max = Collections.max(indexes);
			if (max < size - 1 && itemId == R.id.up) {

			for (Layer<?> l : selected) {
				int i = layers.indexOf(l);
				layers.remove(l);
				layers.add(Math.min(i + 1, size - 1), l);
					// keep adapter sync
				adapter.remove(l);
					adapter.insert(l, Math.max(size - 2 - i, 0));
			}
			refreshOverlays(layers, om);
			} else if (min > 0 && itemId == R.id.down) {
			// NOTE:the listView is in the reverse order
			for (Layer<?> l : selected) {
				int i = layers.indexOf(l);
				layers.remove(l);		
				layers.add(Math.max(i - 1, 0), l);
					// keep adapter sync
				adapter.remove(l);
					adapter.insert(l, Math.min(size - i, size - 1));
			}
			refreshOverlays(layers, om);
		}
		updateSelected();

	}
	return true;

	}

	/**
 * Launch a thread to update the layers
	 * 
 * @param layers
 * @param om
 */
	private void refreshOverlays(final ArrayList<Layer> layers,
		final MultiSourceOverlayManager om) {
	
		om.setLayers(new ArrayList<Layer>(layers), false);
		om.forceRedraw();
			
	}
		
	/**
 * Clear selections and close the action mode
 */
	private void closeActionMode() {
		Log.d(LayerSwitcherFragment.class.getSimpleName(), "close actionmode ");
		selected = new ArrayList<Layer<?>>();
		if (actionMode != null) {
			actionMode.finish();
			actionMode = null;
		}
	}

	/**
 * Update the selected layers in the list view
 */
	private void updateSelected() {
		ListView lv = getListView();
		lv.clearFocus();
		lv.clearChoices();
		ArrayList<Layer> layers = getLayers();
		int size = layers.size();
		for (Layer l : selected) {
			int i = layers.indexOf(l);
			lv.setItemChecked(size - 1 - i, true);

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.geosolutions.android.map.listeners.LayerChangeListener#onLayerStatusChange
	 * ()
 */
	@Override
	public void onLayerStatusChange() {
	reload();
	
	}

	/**
	 * Updates the ActionBar if it is in "ACTION_MODE"
	 * @param numSelected
	 */
	private void updateCAB(int numSelected, boolean wasDeselected) {

		if (actionMode == null){
			// nothing to update
			return;
		}
		String title = getResources().getQuantityString(
				R.plurals.quantity_layers_selected, numSelected, numSelected);
		actionMode.setTitle(title);
		Menu menu = actionMode.getMenu();
		if (numSelected > 1) {
			menu.findItem(R.id.up).setVisible(false);
			menu.findItem(R.id.down).setVisible(false);

		} else if(numSelected == 1 && wasDeselected) {
			//from multiple selections, one remains, what is it ?
			if(selected != null && selected.size() > 0){
				
				MenuItem editItem = menu.findItem(R.id.edit);
				
				if(selected.get(0) instanceof MbTilesLayer){
					
					//the remaining is an MbTilesLayer item, config and add edit item if necessary				
					menu.findItem(R.id.up).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
					menu.findItem(R.id.down).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
					menu.findItem(R.id.up).setVisible(true);
					menu.findItem(R.id.down).setVisible(true);
					
					//this was a vector entry, add the edit entry as first item
					if(editItem == null){
						Drawable d = getResources().getDrawable(R.drawable.ic_action_settings);
						d.mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
						menu.add(Menu.CATEGORY_SYSTEM, R.id.edit, Menu.FIRST, getString(R.string.edit))
				        .setIcon(d)
				        .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
					}
				}else{
					//the remaining is a raster layer, config and remove edit if necessary
					menu.findItem(R.id.up).setVisible(true);
					menu.findItem(R.id.down).setVisible(true);
					
					//this was a raster entry, remove edit
					if(editItem != null){
						menu.removeItem(R.id.edit);
					}
				}
			}
		} else { //should not be called anymore
			menu.findItem(R.id.up).setVisible(true);
			menu.findItem(R.id.down).setVisible(true);
		}
	}


}
