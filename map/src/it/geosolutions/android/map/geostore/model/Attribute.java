package it.geosolutions.android.map.geostore.model;

import java.io.Serializable;

public class Attribute implements Serializable{

		private static final long serialVersionUID = 1L;
		
		public Long id;
        public String name;
        public String textValue;
        public Double numberValue;
        public String dateValue;
        public Resource resource;
}
