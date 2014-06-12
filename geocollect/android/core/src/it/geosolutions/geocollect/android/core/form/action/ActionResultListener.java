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
package it.geosolutions.geocollect.android.core.form.action;

import android.content.Context;
import android.os.Bundle;

/**
 * Listener that perform actions after success or failure for and android action
 * The methods provide the current context of the application to call the current
 * activity methods
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public interface ActionResultListener {
	public abstract void onActionSuccess(Context ctx, Bundle successData);
	public abstract void onActionFailure(Context ctx, Bundle errorData);
}
