/**
 * An object storing the privs that other people have been granted on this list.  
 * E.g. if me@test.com grants you@test.com rights to view a particular list,
 * then my copy of the list will have an OtherUserPrivOnList element with
 * the priv level and you@test.com userId.  
 */
package com.blumenthal.listey;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

public class OtherUserPrivOnList extends TimeStampedNode{
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
	public String userId;//This is the other user's user id
	
	/** Default constructor */
	public OtherUserPrivOnList(){}
	
	public OtherUserPrivOnList(Entity entity) {
		if (!entity.getKind().equals(KIND)){
			//check the entity type and throw if not what we're expecting
			throw new IllegalStateException("The constructor was called with an entity of the wrong kind. (" + entity.getKind() + "): " + entity);
		}//if unexpected kind
		userId = (String) entity.getProperty(USER_ID);
		lastUpdate = (Long) entity.getProperty(LAST_UPDATE);
		priv = OtherUserPriv.valueOf((String) entity.getProperty(PRIV));
	}//ItemCategoryInfo(Entity)
	
	
	
	/**
	 * Note, this has no autogenerated uniqueId, so it doesn't need uniqueIdCreator,
	 * but it has it for compatibility with the base class.
	 * 
	 * @param parent
	 * @return an entity that represents this object
	 */
	@Override
	public Entity toEntity(DataStoreUniqueId uniqueIdCreator, Key parent) {
		Entity entity = new Entity(getEntityKey(parent));
		entity.setProperty(USER_ID, userId);
		entity.setProperty(PRIV, priv.toString());
		entity.setProperty(LAST_UPDATE, lastUpdate);
		return entity;
	}//toEntity
	
	
	/**
	 * @param other
	 * @return Returns true if all essential fields of this object
	 * are the same as other.
	 */
	@Override
	public boolean shallowEquals(TimeStampedNode obj) {
		if (obj == null) return false;
		if (getClass() != obj.getClass())
			return false;
		OtherUserPrivOnList other = (OtherUserPrivOnList) obj;
		return (userId.equals(other.userId)
				&& lastUpdate.equals(other.lastUpdate)
				&& priv.equals(other.priv));
	}//shallowEquals
	
	
	@Override
	public TimeStampedNode makeShallowCopy() {
		OtherUserPrivOnList newObj = new OtherUserPrivOnList();
		newObj.priv = priv;
		newObj.userId = userId;
		newObj.lastUpdate = lastUpdate;
		return newObj;
	}
	
	
	
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

	
	
	/* (non-Javadoc)
	 * @see com.blumenthal.listey.TimeStampedNode#getLastUpdate()
	 */
	@Override
	public Long getLastUpdate() {
		return lastUpdate;
	}

	
	
	/* (non-Javadoc)
	 * @see com.blumenthal.listey.TimeStampedNode#getLastUpdate()
	 */
	public void setLastUpdate(Long newLastUpdate) {
		lastUpdate = newLastUpdate;
	}
	
	

	/* (non-Javadoc)
	 * @see com.blumenthal.listey.TimeStampedNode#getUniqueId()
	 * This actually returns userId, which is a unique ID, but not generated by DataStoreUniqueId
	 */
	@Override
	public String getUniqueId() {
		return userId;
	}

	
	
	/* (non-Javadoc)
	 * @see com.blumenthal.listey.TimeStampedNode#getStatus()
	 * Hard-coded to return ACTIVE, since if this exists at all, it is active
	 */
	@Override
	public Status getStatus() {
		return TimeStampedNode.Status.ACTIVE;
	}


	/* (non-Javadoc)
	 * @see com.blumenthal.listey.TimeStampedNode#getKind()
	 */
	@Override
	public String getKind() {
		return KIND;
	}

}//ItemCategoryInfo