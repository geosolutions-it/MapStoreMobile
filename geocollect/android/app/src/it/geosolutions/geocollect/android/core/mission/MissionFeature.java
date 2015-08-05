package it.geosolutions.geocollect.android.core.mission;

import it.geosolutions.android.map.wfs.geojson.feature.Feature;

/**
 * This Feature type extends <it.geosolutions.android.map.wfs.geojson.feature.Feature> 
 * to hold additional informations about the related mission
 * This type it's used as DTO between Feature Loaders and Feature Adapters
 * 
 * @author Lorenzo Pini
 *
 */
public class MissionFeature extends Feature {
	
	private static final long serialVersionUID = -2001258280624797792L;
	
	public static String DISTANCE_VALUE_ALIAS = "gc_dist_ord";

	/**
	 * Used to store the source table name of this feature
	 */
	public String typeName;
	
	/**
	 * Set to true if the Feature must be shown as "in editing"
	 */
	public boolean editing = false;
	
	/**
	 * Color value as String
	 * This field should be set only with String formats complying 
	 * with the {android.graphics.Color} ones:
     * #RRGGBB
     * #AARRGGBB
     * 'red', 'blue', 'green', 'black', 'white', 'gray', 'cyan', 'magenta',
     * 'yellow', 'lightgray', 'darkgray', 'grey', 'lightgrey', 'darkgrey',
     * 'aqua', 'fuschia', 'lime', 'maroon', 'navy', 'olive', 'purple',
     * 'silver', 'teal'
     */
	public String displayColor;
}
