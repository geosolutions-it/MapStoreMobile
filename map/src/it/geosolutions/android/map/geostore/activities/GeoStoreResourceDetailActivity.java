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
package it.geosolutions.android.map.geostore.activities;

import it.geosolutions.android.map.R;
import it.geosolutions.android.map.geostore.fragment.GeoStoreResourceDetailsFragment;
import it.geosolutions.android.map.geostore.model.Resource;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

/**
 * This Activity shows the Details of a Resource
 * @author Lorenzo Natali (www.geo-solutions.it)
 * 
 */
public class GeoStoreResourceDetailActivity extends SherlockFragmentActivity {
	Resource resource;
	public class PARAMS{
		public static final String RESOURCE = "resource";
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		if (savedInstanceState == null) {
			// During initial setup, plug in the details fragment.
			GeoStoreResourceDetailsFragment detail = new GeoStoreResourceDetailsFragment();
			detail.setArguments(getIntent().getExtras());
			resource = (Resource) getIntent().getSerializableExtra(PARAMS.RESOURCE);
			getSupportFragmentManager().beginTransaction()
					.add(android.R.id.content, detail).commit();

		}

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {

			finish();
		} else if (item.getItemId() == R.id.action_use) {
			Intent data = new Intent();
			data.putExtra(PARAMS.RESOURCE, resource);
			setResult(Activity.RESULT_OK, data);
			finish();
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.geostore_detail, menu);
		return true;
	}
}
