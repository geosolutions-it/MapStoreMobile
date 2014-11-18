/*
 *    GeoSolutions Android Map Library
 *    http://www.geo-solutions.it
 *
 *    (C) 2012-2013, GeoSolutions S.A.S
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package it.geosolutions.android.map.utils;

import it.geosolutions.android.map.dto.MarkerDTO;
import it.geosolutions.android.map.model.Feature;
import it.geosolutions.android.map.overlay.items.DescribedMarker;

import java.util.ArrayList;
import java.util.List;

import org.mapsforge.core.model.GeoPoint;
import org.mapsforge.core.model.MapPosition;

import android.content.Context;

/**
 * Utility class for marker management
 * @author Lorenzo Natali
 */
public class MarkerUtils {

/**
 * Convert an array of markerDTO to an array of DescribedMarkers
 * @param c the context, needed to retrieve the <Drawable> objects @see {@link MarkerDTO}
 * @param markerDTOs
 * @return
 */
public static ArrayList<DescribedMarker> markerDTO2DescribedMarkers(Context c,
        List<MarkerDTO> markerDTOs) {
    ArrayList<DescribedMarker> result = new ArrayList<DescribedMarker>();
    if (markerDTOs != null) {
        for (MarkerDTO mdto : markerDTOs) {
            result.add(mdto.createMarker(c));
        }
    }
    return result;

}

/**
 * Create an array of DescribedMarker from and array of <MarkerDTO>.
 * Used for serialization
 * @param dms the <DescribedMarker> array to convert 
 * @return
 */
public static ArrayList<MarkerDTO> getMarkersDTO(ArrayList<DescribedMarker> dms) {

    ArrayList<MarkerDTO> dtos = new ArrayList<MarkerDTO>();
    for (DescribedMarker d : dms) {
        dtos.add(new MarkerDTO(d));
    }
    return dtos;
}

/**
 * For markers that doesn't have a <GeoPoint> or a <Feature> assigned,
 * try to retrieve it from the DataBase
 * @param markers
 * @param idcol the name of the column used as id
 */
public static boolean assignFeaturesFromDb(ArrayList<DescribedMarker> markers,String idcol) {
    String id=null;
    boolean ok =true;
    for(DescribedMarker m : markers){
        id= m.getFeatureId();
           GeoPoint gp = m.getGeoPoint();
           Feature f = m.getFeature();
           String layer =m.getSource();
           //if source available, look directly in that table
          if(layer!=null){
              //retrieve gp if missing
              if(gp==null){
                  gp= SpatialDbUtils.getGeopointFromLayer(layer, idcol, id);
                  if(gp==null){
                      gp=SpatialDbUtils.getGeopointFromLayer(idcol, id);
                      //if gp!=null TODO assign the source if missing
                  }
              }
              //retrieve feature if missing
              if(f==null || f.size()==0){
                  f = SpatialDbUtils.getFeatureById(id, idcol,layer);
                  if(f==null || f.size()==0){
                      f=SpatialDbUtils.getFeatureById(id, idcol);
                      //if gp!=null TODO assign the source if missing
                  }
              }
              
              //retry iterating on all the tables
              
          }else{
              if(gp == null){
                  //iterate on all the tables to find the required geometry
                  gp = SpatialDbUtils.getGeopointFromLayer(idcol, id);
                  //if gp != null TODO assign the source if missing
              }
              if(f==null || f.size()==0){
                  f = SpatialDbUtils.getFeatureById(id, idcol);
              }
          }
          if(gp !=null){
              m.setGeoPoint(gp);
          }else{
            ok=false;
          }
          if(f!=null){
              m.setFeature(f);
          }else{
              ok= false;
          }
        
        //set feature
        if(m.getFeature()==null || m.getFeature().size()==0){
            if(id!=null){
               
                if( f!=null){
                    m.setFeature(f);
                }else{
                    ok=false;
                }
            }else{
                ok = false;
            }
        }
    } 
    return ok;
}

/**
 * Return the ideal <MapPosition> for markers
 * Now supports only one marker 
 * @param markers
 * @return the position
 */
public static MapPosition getMarkerCenterZoom(ArrayList<DescribedMarker> markers,MapPosition orig) {
    GeoPoint center =orig.geoPoint;
    byte zoomLevel=orig.zoomLevel;
    if(markers.size()==1){
        center = markers.get(0).getGeoPoint()!=null ? markers.get(0).getGeoPoint():center ;
        
    }else if(markers.size()>1){
        //TODO get proper center and zoom level
        center = markers.get(0).getGeoPoint()!=null ? markers.get(0).getGeoPoint():center ;
    }
    if(center!= null){
        MapPosition mp =new MapPosition(center, zoomLevel);
        return mp;
    }
    return null;
}
}