package it.geosolutions.android.map.renderer;

import it.geosolutions.android.map.model.Layer;
import it.geosolutions.android.map.model.Source;
import it.geosolutions.android.map.utils.RendererProvider;

import java.util.ArrayList;
import java.util.Iterator;

import org.mapsforge.android.maps.Projection;
import org.mapsforge.core.model.BoundingBox;

import android.graphics.Canvas;
import android.util.Log;

/**
 * use multiple renderers to render data from different sources
 * @author Lorenzo Natali
 *
 */
public class MultiSourceRenderer implements OverlayRenderer<Layer> {
	private ArrayList<Layer> layers =new  ArrayList<Layer>();
	private ArrayList<OverlayRenderer> renderers = new ArrayList<OverlayRenderer>();
	private Projection projection;
	public void setLayers(ArrayList<Layer> layers){
		this.layers =layers;
		updateRenderers();
	}
	private void updateRenderers() {
		renderers = new ArrayList<OverlayRenderer>();
		ArrayList<Layer> layerChunk =new ArrayList<Layer>();
		Iterator<Layer> iterator = this.layers.iterator();
		//create chunks
		Layer l = null;
		Source s = null;
		if(iterator.hasNext()){
			 l = iterator.next();
			 s = l.getSource();
		}
		while (iterator.hasNext()){
			
			layerChunk.add(l);
			//get all next layers with the same source
			while(iterator.hasNext()){
				l = iterator.next();
				if(l.getSource().equals(s)){//TODO check if equals method have to be redefined
					layerChunk.add(l);
					if (!iterator.hasNext()){
						//generate last renderer and chunk
						generateRenderer(layerChunk, s);
						layerChunk =new ArrayList<Layer>();
					}
				}else{
					//here we have the layer chunk
					generateRenderer(layerChunk, s);
					//Start new Chunk
					s = l.getSource();
					layerChunk =new ArrayList<Layer>();
					//if the list is finished, the last
					//renderer have to be generated 
					if(!iterator.hasNext()){
						layerChunk.add(l);
						generateRenderer(layerChunk, s);
					}
					break;
				}
			}
			
			
		}
		Log.v("Renderer","Created renderers:"+renderers.size());
	}
	
	/**
	 * Generates a renderer for a list of layers from the same source
	 * @param layerChunk the list of layer that have to use this renderer
	 * @param s a <Source> implementation to choose the proper renderer
	 */
	private void generateRenderer(ArrayList<Layer> layerChunk, Source s) {
		OverlayRenderer r = RendererProvider.getRenderer(s);
		r.setProjection(projection);
		r.setLayers(layerChunk);
		synchronized (renderers) {
			renderers.add(r);
		}
		
	}
	
	/**
	 * Renders the layers associated to the renerer calling his internal 
	 * <OverlayRenderer> objects
	 * @throws RenderingException 
	 */
	public void render(Canvas c, BoundingBox boundingBox, byte zoomLevel) throws RenderingException {
		//TODO add a method to prepare request to start them at the same time
		//the render methods will wait
		
		//render anyway
		RenderingException lastException =null;
		synchronized (renderers) {
			for(OverlayRenderer r : renderers){
				try{
					r.render(c, boundingBox, zoomLevel);
				}catch(RenderingException e){
					lastException = e;
				}
			}
		}
		
		
		//notify only the last exception
		if(lastException !=null){
			throw lastException;
		}
		
	}
	
	/**
	 * Refresh the layers
	 * @param layer
	 */
	public void refreshLayer(Layer layer) {
		synchronized (renderers) {
			for(OverlayRenderer r:renderers){
				if(r.getLayers().contains(layer)){
					r.refresh();
					return;
				}
			}
		}
		
		
	}
	@Override
	public ArrayList<Layer> getLayers() {
		return layers;
	}
	@Override
	public void refresh() {
		synchronized (renderers) {
			for(OverlayRenderer<?> r:renderers){
				r.refresh();
			}
		}
		
	}
	@Override
	public void setProjection(Projection projection) {
		this.projection = projection;
		
	}
	
}
