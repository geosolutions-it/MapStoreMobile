package it.geosolutions.android.map.wms;

import java.util.ArrayList;
import java.util.HashMap;
/**
 * Utility class to create Chunks of contiguos layers for a source.
 * @author  Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public class WMSLayerChunker {
	public static ArrayList<WMSRequest> createChunkedRequests(ArrayList<WMSLayer> layers){
		HashMap<WMSSource, ArrayList<WMSLayer>> chunks = createChunkedHash(layers);
		ArrayList<WMSRequest> requests = new ArrayList<WMSRequest>();
		for(WMSSource s : chunks.keySet()){
			requests.add(new WMSRequest(s, chunks.get(s)));
		}
		return requests;
		
	}

	/**
	 * Create HashMap of layers grouped by source
	 * @param layers the list of unsorted layers
	 * @return <HashMap> of Layers grouped by source
	 */
	public static HashMap<WMSSource, ArrayList<WMSLayer>> createChunkedHash(
			ArrayList<WMSLayer> layers) {
		HashMap<WMSSource,ArrayList<WMSLayer>> sources = new HashMap<WMSSource,ArrayList<WMSLayer>>();
		for(WMSLayer l: layers){
			WMSSource s = l.getSource();
			ArrayList<WMSLayer> sameSourceLayers = sources.get(s);
			if(sameSourceLayers == null){
				sameSourceLayers = new ArrayList<WMSLayer>();
				sources.put(s,sameSourceLayers);
			}
			sameSourceLayers.add(l);
		}
		return sources;
	}
}
