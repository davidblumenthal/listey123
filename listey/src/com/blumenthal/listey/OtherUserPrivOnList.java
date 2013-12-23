/**
 * 
 */
package com.blumenthal.listey;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

public class OtherUserPrivOnList {
	public static enum OtherUserPriv {
		FULL,
		VIEW_ONLY
	}
	
	public static final String KIND = "otherUserPrivOnList";//kind in the datastore
	public static final String LAST_UPDATE = "lastUpdate";
	public static final String PRIV = "priv";
	public static final String USER_ID = "userId";//not actually used in the json
	
	public OtherUserPriv priv;
	public Long lastUpdate;
	public String userId;
	
	/** Default constructor */
	public OtherUserPrivOnList(){}
	
	public OtherUserPrivOnList(Entity entity) {
		if (!entity.getKind().equals(KIND)){
			//check the entity type and throw if not what we're expecting
			throw new IllegalStateException("The constructor was called with an entity of the wrong kind.");
		}//if unexpected kind
		lastUpdate = (Long) entity.getProperty(LAST_UPDATE);
		priv = OtherUserPriv.valueOf((String) entity.getProperty(PRIV));
	}//ItemCategoryInfo(Entity)
	
	
	
	/**
	 * @param parent
	 * @return an entity that represents this object
	 */
	public Entity toEntity(Key parent, String otherUserId) {
		Entity entity = new Entity(KIND, otherUserId, parent);
		entity.setProperty(PRIV, priv.toString());
		entity.setProperty(LAST_UPDATE, lastUpdate);
		return entity;
	}//toEntity
}//ItemCategoryInfo