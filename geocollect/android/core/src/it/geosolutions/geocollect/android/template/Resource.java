package it.geosolutions.geocollect.android.template;

import java.io.Serializable;

/**
 * Resource "Pojo" class according to the current server side Resource implementation
 * 
 * @author Robert Oehler
 *
 */

public class Resource implements Serializable {


	private static final long serialVersionUID = -2654634049062234108L;
	private String Attributes;
	private Category category;
	private String creation;
	private Data data;
	private String description;
	private Integer id;
	private String name;

	/**
	 * 
	 * @return
	 * The Attributes
	 */
	public String getAttributes() {
		return Attributes;
	}

	/**
	 * 
	 * @param Attributes
	 * The Attributes
	 */
	public void setAttributes(String Attributes) {
		this.Attributes = Attributes;
	}

	/**
	 * 
	 * @return
	 * The category
	 */
	public Category getCategory() {
		return category;
	}

	/**
	 * 
	 * @param category
	 * The category
	 */
	public void setCategory(Category category) {
		this.category = category;
	}

	/**
	 * 
	 * @return
	 * The creation
	 */
	public String getCreation() {
		return creation;
	}

	/**
	 * 
	 * @param creation
	 * The creation
	 */
	public void setCreation(String creation) {
		this.creation = creation;
	}

	/**
	 * 
	 * @return
	 * The data
	 */
	public Data getData() {
		return data;
	}

	/**
	 * 
	 * @param data
	 * The data
	 */
	public void setData(Data data) {
		this.data = data;
	}

	/**
	 * 
	 * @return
	 * The description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * 
	 * @param description
	 * The description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * 
	 * @return
	 * The id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * 
	 * @param id
	 * The id
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * 
	 * @return
	 * The name
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @param name
	 * The name
	 */
	public void setName(String name) {
		this.name = name;

	}
	public class Data  implements Serializable{

		private static final long serialVersionUID = -2598549865717807884L;
		private String data;

		/**
		 * 
		 * @return
		 * The data
		 */
		public String getData() {
			return data;
		}

		/**
		 * 
		 * @param data
		 * The data
		 */
		public void setData(String data) {
			this.data = data;
		}
	}
	class Category implements Serializable {

		private static final long serialVersionUID = -5819821041094977404L;
		private Integer id;
		private String name;

		/**
		 * 
		 * @return
		 * The id
		 */
		public Integer getId() {
			return id;
		}

		/**
		 * 
		 * @param id
		 * The id
		 */
		public void setId(Integer id) {
			this.id = id;
		}

		/**
		 * 
		 * @return
		 * The name
		 */
		public String getName() {
			return name;
		}

		/**
		 * 
		 * @param name
		 * The name
		 */
		public void setName(String name) {
			this.name = name;
		}
	}
}