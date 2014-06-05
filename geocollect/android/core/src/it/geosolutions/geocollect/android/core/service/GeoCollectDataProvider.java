/*
 * GeoSolutions - MapstoreMobile - GeoSpatial Framework on Android based devices
 * Copyright (C) 2014  GeoSolutions (www.geo-solutions.it)
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
package it.geosolutions.geocollect.android.core.service;

import java.util.ArrayList;

/**
 * Generic interface for GeoCollect data provider.
 * 
 * Uses generics to define IDT (Identifier type, (generically strings) and T (Type of the content)
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public interface GeoCollectDataProvider<T,IDT> {
	public ArrayList<T> getAll(String search,Integer start,Integer limit);
	public ArrayList<T> getAll(String search);
	public void write(IDT id,T content);
	public T read(IDT id);
}
