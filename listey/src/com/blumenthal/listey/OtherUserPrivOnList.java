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
	public static final String USER_ID = "userId";
	
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
		userId = (String) entity.getProperty(USER_ID);
		lastUpdate = (Long) entity.getProperty(LAST_UPDATE);
		priv = OtherUserPriv.valueOf((String) entity.getProperty(PRIV));
	}//ItemCategoryInfo(Entity)
	
	
	
	/**
	 * @param parent
	 * @return an entity that represents this object
	 */
	public Entity toEntity(Key parent, String otherUserId) {
		Entity entity = new Entity(KIND, otherUserId, parent);
		entity.setProperty(USER_ID, otherUserId);
		entity.setProperty(PRIV, priv.toString());
		entity.setProperty(LAST_UPDATE, lastUpdate);
		return entity;
	}//toEntity
	
	
	
	/**
	 * @param other
	 * @return Returns true if all essential fields of this object
	 * are the same as other.
	 */
	public boolean shallowEquals(OtherUserPrivOnList other) {
		return (userId.equals(other.userId)
				&& lastUpdate.equals(other.lastUpdate)
				&& priv.equals(other.priv));
	}//shallowEquals
	
	
	/**
	 * @param other
	 * @return Returns true if this object is essentially the same
	 * as other, and all sub-objects are also.
	 * 
	 * This has no other layers below it, so deepEquals can just call shallowEquals
	 */
	public boolean deepEquals(OtherUserPrivOnList other) {
		return (shallowEquals(other));
	}//deepEquals

}//ItemCategoryInfo