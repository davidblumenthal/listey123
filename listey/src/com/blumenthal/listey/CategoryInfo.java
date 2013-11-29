/**
 * Stores category (store) information for a list.
 */
package com.blumenthal.listey;

import com.google.appengine.api.datastore.Entity;

public class CategoryInfo {
	public static final String KIND = "category";//kind in the datastore
	
	public String name;
	public String uniqueId;
	public Long lastUpdate;
	
	/** Default constructor */
	public CategoryInfo(){}
	
	public CategoryInfo(Entity entity) {
		if (!entity.getKind().equals(KIND)){
			//check the entity type and throw if not what we're expecting
			throw new IllegalStateException("The constructor was called with an entity of the wrong kind.");
		}//if unexpected kind
		lastUpdate = (Long) entity.getProperty("lastUpdate");
		name = (String) entity.getProperty("displayName");
		uniqueId = (String) entity.getKey().getName();
	}//CategoryInfo(Entity)
}//CategoryInfo