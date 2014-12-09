package it.geosolutions.geocollect.model.source;
import java.io.Serializable;
import java.util.HashMap;
/**
 * represents the "sopralluogi" source
 */
public class SopSchema implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * Map with "propertyName":"propertyType". Can be used to keep in the database or save later
	 */
	public HashMap<String,XDataType> fields = new HashMap<String,XDataType>();
	/**
	 * Defines the local database table name where the form data should be saved
	 */
	public String localFormStore;

	/**
	 * Optional column name for ordering
	 */
	public String orderingField;

}
