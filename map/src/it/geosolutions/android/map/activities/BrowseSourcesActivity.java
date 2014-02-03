/*
 * GeoSolutions MapStoreMobile - Digital field mapping on Android based devices
 * Copyright (C) 2014  GeoSolutions (www.geo-solutions.it)
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
package it.geosolutions.android.map.activities;

import java.util.ArrayList;
import java.util.Collections;

import it.geosolutions.android.map.MapsActivity;
import it.geosolutions.android.map.R;
import it.geosolutions.android.map.fragment.sources.SourcesFragment;
import it.geosolutions.android.map.geostore.activities.GeoStoreResourceDetailActivity;
import it.geosolutions.android.map.geostore.activities.GeoStoreResourcesActivity;
import it.geosolutions.android.map.geostore.model.Resource;
import it.geosolutions.android.map.mapstore.activities.MapStoreLayerListActivity;
import it.geosolutions.android.map.mapstore.model.MapStoreConfiguration;
import it.geosolutions.android.map.mapstore.model.MapStoreLayer;
import it.geosolutions.android.map.mapstore.utils.MapStoreConfigTask;
import it.geosolutions.android.map.mapstore.utils.MapStoreUtils;
import it.geosolutions.android.map.model.Layer;
import it.geosolutions.android.map.wms.WMSLayer;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.vividsolutions.jts.operation.valid.IsValidOp;

/**
 * This activity Allows to add layers from a source
 * 
 * 
 * @author Lorenzo Natali (www.geo-solutions.it)
 */
public class BrowseSourcesActivity extends SherlockFragmentActivity {

	public static final int GET_ITEM = 0;
	private static final int MAPSTORE_REQUEST_CODE = 1;
	private static final String MAPSTORE_CONFIG = "MAPSTORE_CONFIG";
	public static final String LAYERS_TO_ADD = "LAYERS_TO_ADD";
	SourcesFragment sources = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		if (savedInstanceState == null) {
			// During initial setup, plug in the details fragment.
			sources = new SourcesFragment();
			sources.setArguments(getIntent().getExtras());
			getSupportFragmentManager().beginTransaction()
					.add(android.R.id.content, sources).commit();

		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:

			finish();
			overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
			break;
		}
		return false;
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if( RESULT_OK == resultCode ){
			setResult(resultCode, data);
			finish();
		}
	}

	

}