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

import com.actionbarsherlock.app.SherlockFragment;

import it.geosolutions.geocollect.android.core.mission.Mission;
import it.geosolutions.geocollect.model.viewmodel.FormAction;
import it.geosolutions.geocollect.model.viewmodel.Page;
import android.app.Activity;
import android.support.v4.app.Fragment;

/**
 * Abstraction for <Mission> action to implement the
 * effective behiviour of actions. Attributes and data model are used
 * to manage the specificity of this field
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public abstract class AndroidAction extends FormAction{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ActionResultListener actionResultListener;
	/**
	 * Copy attributes from <Action>
	 * @param a
	 */
	public AndroidAction(FormAction a){
		this.attributes = a.attributes;
		this.dataModel = a.dataModel;
		this.iconCls = a.iconCls;
		this.id = a.id;
		this.type = a.type; 
		this.text = a.text;
	}
	
	/**
	 * Set a listener for action success
	 * @param l
	 */
	public void setActionResultListener(ActionResultListener l){
		this.actionResultListener = l;
		
	}

	/**
	 * Perform an <Action> for a <MissionTemplate>
	 * @param fragment the fragment that started this action
	 * @param action the action to perform
	 * @param m the mission
	 * @param p the page
	 */
	public abstract void performAction(SherlockFragment fragment, FormAction action, Mission m, Page p);

	
	 
}
