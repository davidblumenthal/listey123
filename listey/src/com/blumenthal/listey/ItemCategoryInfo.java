/**
 * 
 */
package com.blumenthal.listey;

import com.blumenthal.listey.ListeyDataOneUser.ItemCategoryStatus;
import com.google.appengine.api.datastore.Entity;

public class ItemCategoryInfo {
	public static final String KIND = "itemCategory";//kind in the datastore
	public String uniqueCategoryId;
	ItemCategoryStatus status;
	public Long lastUpdate;
	
	/** Default constructor */
	public ItemCategoryInfo(){}
	
	public ItemCategoryInfo(Entity entity) {
		if (!entity.getKind().equals(KIND)){
			//check the entity type and throw if not what we're expecting
			throw new IllegalStateException("The constructor was called with an entity of the wrong kind.");
		}//if unexpected kind
		lastUpdate = (Long) entity.getProperty("lastUpdate");
		uniqueCategoryId = (String) entity.getKey().getName();
		status = ItemCategoryStatus.valueOf((String) entity.getProperty("status"));
	}//ItemCategoryInfo(Entity)
}//ItemCategoryInfo