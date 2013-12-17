package it.geosolutions.android.map.wms;

import java.util.ArrayList;
import java.util.HashMap;

import it.geosolutions.android.map.model.Source;

public class WMSSource implements Source{
	public HashMap<String,String> baseParams= new HashMap<String,String>();
	private String url;
	public WMSSource(String url){
		setDefaultParameters();
		this.url=url;
	}
	
	public String getUrl(){
		return url;
	}
	private void setDefaultParameters(){
		baseParams.put("format","image/png8");
		baseParams.put("transparent","true");
	}
	
}
