/*
 * GeoSolutions map - Digital field mapping on Android based devices
 * Copyright (C) 2013 - 2017  GeoSolutions (www.geo-solutions.it)
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

import it.geosolutions.geocollect.android.core.mission.MissionFeature;
import it.geosolutions.geocollect.model.config.MissionTemplate;

import java.util.Comparator;

import android.util.Log;

/**
 * class to compare MissionFeatures by their "new" @link MissionTemplate.NEW_NOTICE_SUFFIX
 *  and "editing" flags
 * 
 * @author Robert Oehler
 *
 */


public class MissionFeatureSorter implements Comparator<MissionFeature>{
	
	private String orderingField;
	private boolean byDistance;
	private boolean reverse;
	private boolean preferEdited;
	
	public MissionFeatureSorter(final String pOrderingField, boolean pPreferEdited, boolean pByDistance, boolean pReverse){
		
		this.orderingField = pOrderingField;
		this.byDistance    = pByDistance;
		this.reverse       = pReverse;
		this.preferEdited  = pPreferEdited;
	}

	@Override
	public int compare(MissionFeature lhs, MissionFeature rhs) {
		
		if(preferEdited){
			
			//check the new flag	
			boolean bothNew = lhs.typeName != null && lhs.typeName.endsWith(MissionTemplate.NEW_NOTICE_SUFFIX) &&
							  rhs.typeName != null && rhs.typeName.endsWith(MissionTemplate.NEW_NOTICE_SUFFIX);
			
			//if both have the new flag we cannot compare by this flag continue with next criterion
			if(!bothNew){
				//otherwise check
				 if(lhs.typeName != null && lhs.typeName.endsWith(MissionTemplate.NEW_NOTICE_SUFFIX)){
					 return reverse ? 1 : -1;
				 }else if(rhs.typeName != null && rhs.typeName.endsWith(MissionTemplate.NEW_NOTICE_SUFFIX)){
					 return reverse ? -1 : 1;
				 }
			}
			//not new, editing ?
			//when both are editing we cannot use this criterion
			if(!(lhs.editing && rhs.editing)){	 
				//otherwise check which
				 if(lhs.editing){
					 return reverse ? 1 : -1;
				 }else if(rhs.editing){
					 return reverse ? -1 : 1;
				 }
			}
		}
		
		//could not decide by new or editing - use either distance or alphabetical

		if (byDistance) {
			// sort by distance
			if (lhs.properties == null
					|| !lhs.properties.containsKey(MissionFeature.DISTANCE_VALUE_ALIAS)
					|| lhs.properties.get(MissionFeature.DISTANCE_VALUE_ALIAS) == null) {
				return 1;
			}
			if (rhs.properties == null
					|| !rhs.properties
							.containsKey(MissionFeature.DISTANCE_VALUE_ALIAS)
					|| rhs.properties.get(MissionFeature.DISTANCE_VALUE_ALIAS) == null) {
				return -1;
			}

			try {
				long ldistance = Math.round(Double.parseDouble(lhs.properties.get(MissionFeature.DISTANCE_VALUE_ALIAS).toString()));
				long rdistance = Math.round(Double.parseDouble(rhs.properties.get(MissionFeature.DISTANCE_VALUE_ALIAS).toString()));
				return (int) (rdistance - ldistance);
			} catch (NumberFormatException nfe) {
				return 0;
			}

		} else {
			// sort alphabetical

			if (lhs.properties == null || !lhs.properties.containsKey(orderingField)) {
				return 1;
			}
			if (rhs.properties == null || !rhs.properties.containsKey(orderingField)) {
				return -1;
			}

			try {
				return lhs.properties
						.get(orderingField)
						.toString()
						.compareTo(rhs.properties.get(orderingField).toString());
			} catch (NullPointerException npe) {
				return 0;
			}

		}
	}

}
