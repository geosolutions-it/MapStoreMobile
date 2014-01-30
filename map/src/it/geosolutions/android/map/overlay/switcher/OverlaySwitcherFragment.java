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

import it.geosolutions.android.map.DataListActivity;
import it.geosolutions.android.map.MapsActivity;
import it.geosolutions.android.map.R;
import it.geosolutions.android.map.geostore.activities.GeoStoreResourcesActivity;
import it.geosolutions.android.map.listeners.OverlayChangeListener;
import it.geosolutions.android.map.mapstore.activities.MapStoreLayerListActivity;
import it.geosolutions.android.map.overlay.managers.SimpleOverlayManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;

import com.actionbarsherlock.app.SherlockFragment;

/**
 * This fragment shows a view o the attributes of a single feature from a
 * feature passed as Extra. Is binded with The <SimpleOverlayManager> methods
 * 
 * @author Lorenzo Natali (www.geo-solutions.it)
 */
public class OverlaySwitcherFragment extends SherlockFragment implements OverlayChangeListener {

private CheckBox m;
private CheckBox ms;
private CheckBox d;
private ImageButton msdet;
private SimpleOverlayManager om ;

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
    return inflater.inflate(R.layout.overlay_switcher, container,
            false);
}

/*
 * (non-Javadoc)
 * @see android.support.v4.app.ListFragment#onViewCreated(android.view.View,
 * android.os.Bundle)
 */
@Override
public void onViewCreated(View view, Bundle savedInstanceState) {
    final MapsActivity ac = (MapsActivity)getActivity(); 
    om = (SimpleOverlayManager) ac.overlayManager;

    super.onViewCreated(view, savedInstanceState);
    // setup of the checkboxes
    m = (CheckBox)view.findViewById(R.id.markers);
    ms = (CheckBox)view.findViewById(R.id.mapstore);
    d = (CheckBox)view.findViewById(R.id.data);
    ms.setChecked(om.mapstoreActivated);
    m.setChecked(om.markerActivated);
    d.setChecked(om.spatialActivated);
    
    //Set the handlers of the buttons to show/hide the overlays on check/decheck
    ms.setOnCheckedChangeListener(new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			om.mapstoreActivated = isChecked;
			om.toggleOverlayVisibility(buttonView.getId(), isChecked);
			
		}
	});
	m.setOnCheckedChangeListener(new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			om.markerActivated = isChecked;
			om.toggleOverlayVisibility(buttonView.getId(), isChecked);
			
		}
	});
	d.setOnCheckedChangeListener(new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			om.spatialActivated = isChecked;
			om.toggleOverlayVisibility(buttonView.getId(), isChecked);
			
		}
	});
    
	//MapStoreLayerList launcher
	msdet = (ImageButton) view.findViewById(R.id.mapstore_detail);
	msdet.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent i  = new Intent(ac,MapStoreLayerListActivity.class);
			//TODO put MapStore config
			i.putExtra(MapsActivity.MAPSTORE_CONFIG	, om.getMapStoreConfig());
			ac.startActivityForResult(i, MapsActivity.MAPSTORE_REQUEST_CODE);
			
			
		}

	});
    if(om.getMapStoreConfig()!=null){
    			msdet.setVisibility(ImageButton.VISIBLE);
    }else{
    	
		msdet.setVisibility(ImageButton.GONE);
    }
    //the map selection activity launcher
    ImageButton msEd = (ImageButton) view.findViewById(R.id.mapstore_edit);
	msEd.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
		    Intent pref = new Intent(ac,GeoStoreResourcesActivity.class);
			pref.putExtra(GeoStoreResourcesActivity.PARAMS.GEOSTORE_URL,"http://mapstore.geo-solutions.it/geostore/rest/");
		    ac.startActivityForResult(pref, MapsActivity.MAPSTORE_REQUEST_CODE);
		    
			
		}
	});
	//local data style list
    ImageButton dataListButton = (ImageButton) view.findViewById(R.id.data_list);
    dataListButton.setOnClickListener(new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			Intent datalistIntent = new Intent(ac, DataListActivity.class);
			ac.startActivityForResult(datalistIntent, MapsActivity.DATAPROPERTIES_REQUEST_CODE);

			
		}
	});

}

/**
 * called on overlay visibility changes
 */
@Override
public void onOverlayVisibilityChange(int id, boolean visibility) {
		if(id== R.id.data && d!=null){
			d.setChecked(visibility);
		}else if(id== R.id.markers && m !=null){
			m.setChecked(visibility);
		}else if(id== R.id.mapstore && m!=null){
			if(msdet!=null && om.getMapStoreConfig() !=null){//TODO get it
				msdet.setVisibility(ImageButton.VISIBLE);
			}else{
				msdet.setVisibility(ImageButton.GONE);
			}
			ms.setChecked(visibility);
		}
}

}
