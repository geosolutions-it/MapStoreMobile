package it.geosolutions.android.map.utils;

import it.geosolutions.android.map.style.AdvancedStyle;

public class StyleUtils {
	public static boolean isVisible(AdvancedStyle s,byte zoomLevel){
		return s.enabled !=0 && s.maxZoom >= zoomLevel && zoomLevel >= s.minZoom;
	}
}
