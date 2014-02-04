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
package it.geosolutions.android.map.activities;

import it.geosolutions.android.map.R;
import it.geosolutions.android.map.fragment.GetFeatureInfoFragment;
import it.geosolutions.android.map.fragment.featureinfo.FeatureDetailsFragment;
import it.geosolutions.android.map.fragment.featureinfo.FeatureInfoAttributeListFragment;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

/**
 * This activity manages the FeatureInfoAttributeListFragment managing tasks.
 * can return a feature from it fragment
 * 
 * @author Lorenzo Natali (www.geo-solutions.it)
 */
public class FeatureDetailsActivity extends SherlockFragmentActivity {

public static final int GET_ITEM = 0;
private static final String TAG = "FEATURE_DETAILS";
FeatureDetailsFragment details=null;

@Override
protected void onCreate(Bundle savedInstanceState) {
    overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    super.onCreate(savedInstanceState);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    if (savedInstanceState == null) {
        // During initial setup, plug in the details fragment.
        details = new FeatureDetailsFragment();
        details.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().add(android.R.id.content, details).commit();
        
        
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


}