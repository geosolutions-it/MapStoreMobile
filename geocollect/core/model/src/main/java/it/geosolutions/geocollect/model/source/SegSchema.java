package it.geosolutions.geocollect.model.source;

import java.io.Serializable;
import java.util.HashMap;
/**
 * Represents the "segnalazioni" source
 * @author robertoehler
 *
 */
public class SegSchema implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String type = "WFS";
	/**
	 * URL of the WFS Service
	 */
	public String URL;
	/**
	 * Base parameters for the service. A CQL_FILTER can be included
	 */
	public HashMap<String,String> baseParams;
	/**
	 * name of the FeatureType
	 */
	public String typeName;
	/**
	 * Can be used to have only the list without geometries
	 * and other attributes
	 */
	public String previewParamNames;
	/**
	 * Map with "propertyName":"propertyType". Can be used to keep in the database or save later
	 */
	public HashMap<String,XDataType> fields = new HashMap<String,XDataType>();
	/**
	 * Defines the local database table name where the original mission data should be saved
	 */
	public String localSourceStore;

	/**
	 * Optional column name for ordering
	 */
	public String orderingField;

}
