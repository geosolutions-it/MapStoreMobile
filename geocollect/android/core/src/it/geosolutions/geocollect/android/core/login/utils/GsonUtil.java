/*
 * GeoSolutions - GeoCollect
 * Copyright (C) 2014 - 2015  GeoSolutions (www.geo-solutions.it)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.geosolutions.geocollect.android.core.login.utils;

import java.lang.reflect.Type;
import java.util.List;

import it.geosolutions.android.map.geostore.model.Attribute;
import it.geosolutions.android.map.geostore.model.GeoStoreAttributeTypeAdapter;
import it.geosolutions.android.map.geostore.model.GeoStoreResourceTypeAdapter;
import it.geosolutions.android.map.geostore.model.Resource;
import it.geosolutions.android.map.wfs.geojson.GeometryJsonDeserializer;
import it.geosolutions.android.map.wfs.geojson.GeometryJsonSerializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.vividsolutions.jts.geom.Geometry;
/**
 * Utility classes for serialization
 * @author Lorenzo Natali,GeoSolutions
 *
 */
public class GsonUtil {
    public static Gson createFeatureGson(){

        Type resourceListType = new TypeToken<List<Resource>>(){}.getType();
        Type attributeListType = new TypeToken<List<Attribute>>(){}.getType();
        return  new GsonBuilder()
        // .serializeNulls()
        .disableHtmlEscaping()
        // GeoStore compliant typeAdapter
        .registerTypeAdapter(resourceListType, new GeoStoreResourceTypeAdapter())
        .registerTypeAdapter(attributeListType, new GeoStoreAttributeTypeAdapter())
        // GeoJson serializer/deserializer
        .registerTypeHierarchyAdapter(Geometry.class, new GeometryJsonSerializer())
        .registerTypeHierarchyAdapter(Geometry.class, new GeometryJsonDeserializer()).create();
    } 
}
