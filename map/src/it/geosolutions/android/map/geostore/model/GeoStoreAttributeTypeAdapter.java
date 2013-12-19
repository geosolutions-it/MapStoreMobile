/*
 * GeoSolutions map - Digital field mapping on Android based devices
 * Copyright (C) 2013  GeoSolutions (www.geo-solutions.it)
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
package it.geosolutions.android.map.geostore.model;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
/**
 * Type adapter to manage arrays and single objects as they are.
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 * @param <T>
 */
public class GeoStoreAttributeTypeAdapter implements JsonDeserializer<List<Attribute>> {

	@Override
	public List<Attribute> deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
			JsonDeserializationContext ctx) throws JsonParseException {
		 List<Attribute> vals = new ArrayList<Attribute>();
	        if (json.isJsonArray()) {
	            for (JsonElement e : json.getAsJsonArray()) {
	                vals.add((Attribute) ctx.deserialize(e,Attribute.class));
	            }
	        } else if (json.isJsonObject()) {
	            vals.add((Attribute) ctx.deserialize(json, Attribute.class));
	        } else {
	            throw new RuntimeException("Unexpected JSON type: " + json.getClass());
	        }
	        return vals;
	}
}