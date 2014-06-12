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
import android.app.Activity;

/**
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public class NavUtils {
	public static NavDrawerItem[] getNavMenu(Activity app){
		return new NavDrawerItem[] {
                NavMenuSection.create( 100, app.getString(R.string.action_mission)),//TODO translate
                NavMenuItem.create(101,app.getString(R.string.reporting), "ic_collections_view_as_list", false, app),
                NavMenuItem.create(102, app.getString(R.string.map), "ic_location_map", false, app), 
                NavMenuSection.create(200,app.getString(R.string.general)),
                NavMenuItem.create(203, app.getString(R.string.action_settings), "ic_action_settings", false, app), 
                NavMenuItem.create(204, app.getString(R.string.action_quit), "ic_navigation_quit_light", false, app)};
		
	}
}
