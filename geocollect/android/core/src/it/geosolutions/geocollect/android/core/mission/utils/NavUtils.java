/*
 * GeoSolutions - MapstoreMobile - GeoSpatial Framework on Android based devices
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
package it.geosolutions.geocollect.android.core.mission.utils;

import it.geosolutions.geocollect.android.core.R;
import it.geosolutions.geocollect.android.core.navigation.NavDrawerItem;
import it.geosolutions.geocollect.android.core.navigation.NavMenuItem;
import it.geosolutions.geocollect.android.core.navigation.NavMenuSection;
import it.geosolutions.geocollect.model.config.MissionTemplate;

import java.util.ArrayList;

import android.app.Activity;

/**
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public class NavUtils {
	
	public final static int DOWNLOADED_TEMPLATE_ID = 2000;
	/**
	 * return a list of menu entries for the navdrawer
	 * may contain downloaded templates
	 * @param app context
	 * @return
	 */
	public static NavDrawerItem[] getNavMenu(Activity app){
		
		final ArrayList<MissionTemplate> persistedTemplates = PersistenceUtils.loadSavedTemplates(app);
		
		int size = 4;
		
		if(persistedTemplates != null && persistedTemplates.size() > 0){
			size += (persistedTemplates.size() * 2);
		}
		
		
		NavDrawerItem[] items = new NavDrawerItem[size];
		
		int i = 0;
		
		// 0
		items[i++] = NavMenuSection.create( 100, app.getString(R.string.action_mission));
		
		if(persistedTemplates == null){
    		
		    // 1
            items[i++] = NavMenuItem.create(101,app.getString(R.string.reporting), "ic_collections_view_as_list", false, app);
            // 2
            items[i++] = NavMenuItem.create(1001,app.getString(R.string.my_inspections_my)+ " "+app.getString(R.string.reporting), "ic_collections_view_as_list", false, app);
		
		}else{
			
			for(int j = 0, k = 0; j < persistedTemplates.size(); i++, j++, k+=2 ){
				MissionTemplate t = persistedTemplates.get(j);
				items[i + k]     =  NavMenuItem.create(DOWNLOADED_TEMPLATE_ID + k    ,t.title, "ic_collections_view_as_list", false, app);
				items[i + k + 1] =  NavMenuItem.create(DOWNLOADED_TEMPLATE_ID + k + 1,app.getString(R.string.my_inspections_my)+ " " +t.title, "ic_collections_view_as_list", false, app);
			}
		}
		
		//items[size - 4] = NavMenuItem.create(102, app.getString(R.string.map), "ic_location_map", false, app);
		items[size - 3] = NavMenuSection.create(200,app.getString(R.string.general));
		items[size - 2] = NavMenuItem.create(203, app.getString(R.string.action_account), "ic_action_settings", false, app);
	    items[size - 1] = NavMenuItem.create(204, app.getString(R.string.action_quit), "ic_navigation_quit_light", false, app);
	    
	    return items;
		
	}
}
