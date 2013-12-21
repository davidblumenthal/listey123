/**
 * Stores category (store) information for a list.
 */
package com.blumenthal.listey;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

public class CategoryInfo {
	public static final String KIND = "category";//kind in the datastore
	public static final String STATUS = "status";
	public static final String NAME = "name";
	public static final String LAST_UPDATE = "lastUpdate";
	
	public static enum CategoryStatus {
	    ACTIVE,
	    COMPLETED
	}
	
	public String name;
	public String uniqueId;
	public Long lastUpdate;
	public CategoryStatus status;
	
	/** Default constructor */
	public CategoryInfo(){}
	
	public CategoryInfo(Entity entity) {
		if (!entity.getKind().equals(KIND)){
			//check the entity type and throw if not what we're expecting
			throw new IllegalStateException("The constructor was called with an entity of the wrong kind.");
		}//if unexpected kind
		lastUpdate = (Long) entity.getProperty("lastUpdate");
		name = (String) entity.getProperty(NAME);
		uniqueId = (String) entity.getKey().getName();
		status = CategoryStatus.valueOf((String) entity.getProperty(STATUS));
	}//CategoryInfo(Entity)
	
	
	
	/**
	 * @param parent
	 * @return an entity that represents this object
	 */
	public Entity toEntity(Key parent) {
		Entity entity = new Entity(KIND, uniqueId, parent);
		entity.setProperty(STATUS, status.toString());
		entity.setProperty(NAME, name);
		entity.setProperty(LAST_UPDATE, lastUpdate);
		return entity;
	}//toEntity

}//CategoryInfo