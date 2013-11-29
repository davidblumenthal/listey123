/**
 * 
 */
package com.blumenthal.listey;

import java.util.HashMap;
import java.util.Map;

import com.blumenthal.listey.ListeyDataOneUser.ItemStatus;
import com.google.appengine.api.datastore.Entity;

public class ItemInfo {
	public static final String KIND = "item";//kind in the datastore
	
	public String name;
	public String uniqueId;
	public Integer count = 1;
	public ItemStatus status;
	public Map<String, ItemCategoryInfo> categories = new HashMap<String, ItemCategoryInfo>();
	public Long lastUpdate;
	
	/** Default constructor */
	public ItemInfo(){}
	
	
	public ItemInfo(Entity entity) {
		if (!entity.getKind().equals(KIND)){
			//check the entity type and throw if not what we're expecting
			throw new IllegalStateException("The constructor was called with an entity of the wrong kind.");
		}//if unexpected kind
		
		lastUpdate = (Long) entity.getProperty("lastUpdate");
		name = (String) entity.getProperty("displayName");
		uniqueId = (String) entity.getKey().getName();
		count = (Integer) entity.getProperty("count");
		status = ItemStatus.valueOf((String) entity.getProperty("status"));
	}//ItemInfo(Entity)
}//ItemInfo