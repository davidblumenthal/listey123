/**
 * 
 */
package com.blumenthal.listey;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

public class ItemCategoryInfo {
	public static enum ItemCategoryStatus {
		ACTIVE,
		DELETED
	}
	
	public static final String KIND = "itemCategory";//kind in the datastore
	public static final String STATUS = "status";
	public static final String LAST_UPDATE = "lastUpdate";
	public static final String UNIQUE_ID = "uniqueId";
	
	public String uniqueId;
	public ItemCategoryStatus status;
	public Long lastUpdate;
	
	/** Default constructor */
	public ItemCategoryInfo(){}
	
	public ItemCategoryInfo(Entity entity) {
		if (!entity.getKind().equals(KIND)){
			//check the entity type and throw if not what we're expecting
			throw new IllegalStateException("The constructor was called with an entity of the wrong kind.");
		}//if unexpected kind
		lastUpdate = (Long) entity.getProperty(LAST_UPDATE);
		uniqueId = (String) entity.getKey().getName();
		status = ItemCategoryStatus.valueOf((String) entity.getProperty(STATUS));
	}//ItemCategoryInfo(Entity)
	
	
	
	/**
	 * @param parent
	 * @return an entity that represents this object
	 */
	public Entity toEntity(Key parent) {
		Entity entity = new Entity(KIND, uniqueId, parent);
		entity.setProperty(STATUS, status.toString());
		entity.setProperty(LAST_UPDATE, lastUpdate);
		return entity;
	}//toEntity
}//ItemCategoryInfo