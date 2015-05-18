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
package it.geosolutions.geocollect.model.config;

import it.geosolutions.geocollect.model.source.SegSchema;
import it.geosolutions.geocollect.model.source.SopSchema;
import it.geosolutions.geocollect.model.viewmodel.Form;
import it.geosolutions.geocollect.model.viewmodel.Page;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 * The base template for the collection retrieving and saving
 */
public class MissionTemplate implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Suffix for the new notices tables
	 */
	public static String NEW_NOTICE_SUFFIX = "_new";
	
	/**
	 * Identifier of this template
	 */
	public String id;
	/**
	 * name to display for this Template
	 */
	public String title;
	/**
	 * Sopralluogi Schema
	 */
	public SopSchema schema_sop;
	
	/**
	 * Segnalazioni Schema
	 */
	public SegSchema schema_seg;
	/**
	 * Page to show data in the preview
	 */
	public Page preview;
	/**
	 * name field
	 */
	public String nameField;
	/**
	 * description Field 
	 */
	public String descriptionField;
	/**
	 * The segnalazioni form to compile
	 */
	public Form seg_form; 
	/**
	 * The sopralluogi form to compile
	 */
	public Form sop_form; 
	/**
	 * Generic data to get from the server to configure objects.
	 * Can be used to bind form editing values.
	 * Example: 
	 * my_allowed_values:["value1","value2"]
	 * 
	 */
	public HashMap<String,Object> config = new HashMap<String,Object>();
	/**
	 * priority Field 
	 */
	public String priorityField;
	/**
	 * Association between {priorityField} values and a color value String
	 * to be shown on the main missions list
	 * 
	 */
	public HashMap<String,String> priorityValuesColors = new HashMap<String,String>();
	
	/**
	 * Returns an appropriate loader index
	 * @return
	 */
	public int getLoaderIndex(){
	    
	    if(this.id != null){
	        try {
	            return Integer.parseInt(this.id);
	        }catch(NumberFormatException nfe){
	            return this.id.hashCode();
	        } 
	    }
	    
	    if(this.title != null){
	        return this.title.hashCode();
	    }
	    
        return 0;
	    
	}

}
