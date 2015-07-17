package it.geosolutions.geocollect.android.core.login.utils;

import android.content.Context;
import android.net.ConnectivityManager;

public class NetworkUtil {
	
    /**
     * check if the device is online
     */
    public static boolean isOnline(Context c) {
    	
    	 ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
    	 boolean isOnline = false;
    	 
    	 if(cm.getActiveNetworkInfo() == null){
			 return false;		 
		 }
		 isOnline = cm.getActiveNetworkInfo().isConnected();

    	 return isOnline;
    	}
    

}
