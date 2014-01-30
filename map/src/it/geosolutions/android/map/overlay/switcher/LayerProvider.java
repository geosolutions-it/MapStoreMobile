package it.geosolutions.android.map.overlay.switcher;

import it.geosolutions.android.map.model.Layer;

import java.util.List;

public interface LayerProvider {
	List<Layer> getLayers();
}
