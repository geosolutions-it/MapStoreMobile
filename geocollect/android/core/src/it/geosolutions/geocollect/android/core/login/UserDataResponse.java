package it.geosolutions.geocollect.android.core.login;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDataResponse {


	public String password;
	public String username;
	public List<Authority> authorities;
	public boolean accountNonExpired;
	public boolean accountNonLocked;
	public boolean credentialsNonExpired;
	public boolean enabled;


	public class Authority {

		private String authority;
		private Map<String, Object> additionalProperties = new HashMap<String, Object>();

		public String getAuthority() {
			return authority;
		}

		public void setAuthority(String authority) {
			this.authority = authority;
		}

		public Map<String, Object> getAdditionalProperties() {
			return this.additionalProperties;
		}

		public void setAdditionalProperty(String name, Object value) {
			this.additionalProperties.put(name, value);
		}

	}

}
