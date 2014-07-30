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
package it.geosolutions.geocollect.model.http;

/**
 * Represents the return object of a commit
 * 
 * @author Lorenzo Natali (lorenzo.natali@geo-solutions.it)
 *
 */
public class CommitResponse {
	/**
	 * The esit of the response
	 */
	private Status status;
	/**
	 * The path to the media service to send 
	 */
	private String message;
	/**
	 * identifier of the created object
	 */
	private String id;
	
	public boolean isSuccess(){
		return Status.SUCCESS.equals(status);
	}
	//GETTERS AND SETTERS
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

}
