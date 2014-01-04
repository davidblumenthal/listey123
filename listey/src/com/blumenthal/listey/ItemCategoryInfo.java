/**
 * Links the item to a category (aka store)
 */
package com.blumenthal.listey;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

public class ItemCategoryInfo extends TimeStampedNode {
	public static final String KIND = "itemCategory";//kind in the datastore
	public static final String STATUS = "status";
	public static final String LAST_UPDATE = "lastUpdate";
	public static final String UNIQUE_ID = "uniqueId";
	
	private String uniqueId;
	private Status status;
	private Long lastUpdate;
	
	/** Default constructor */
	public ItemCategoryInfo(){}
	
	public ItemCategoryInfo(Entity entity) {
		if (!entity.getKind().equals(KIND)){
			//check the entity type and throw if not what we're expecting
			throw new IllegalStateException("The constructor was called with an entity of the wrong kind.");
		}//if unexpected kind
		setLastUpdate((Long) entity.getProperty(LAST_UPDATE));
		setUniqueId((String) entity.getKey().getName());
		setStatus(Status.valueOf((String) entity.getProperty(STATUS)));
	}//ItemCategoryInfo(Entity)
	
	
	/**
	 * @return the uniqueId
	 */
	@Override
	public String getUniqueId() {
		return uniqueId;
	}

	/**
	 * @param uniqueId the uniqueId to set
	 */
	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	/**
	 * @return the status
	 */
	@Override
	public Status getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(Status status) {
		this.status = status;
	}

	/**
	 * @return the lastUpdate
	 */
	@Override
	public Long getLastUpdate() {
		return lastUpdate;
	}

	/**
	 * @param lastUpdate the lastUpdate to set
	 */
	public void setLastUpdate(Long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

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
		ItemCategoryInfo other = (ItemCategoryInfo) obj;
		return (getUniqueId().equals(other.getUniqueId())
				&& getLastUpdate().equals(other.getLastUpdate())
				&& getStatus().equals(other.getStatus()));
	}//shallowEquals
	
	
	/**
	 * @param other
	 * @return Returns true if this object is essentially the same
	 * as other, and all sub-objects are also.
	 * 
	 * This has no other layers below it, so deepEquals can just call shallowEquals
	 */
	public boolean deepEquals(ItemCategoryInfo other) {
		return (shallowEquals(other));
	}//deepEquals
	
	
	
	/**
	 * @param uniqueIdCreator Need to pass this along to possibly translate a temporary id to a unique one
	 * @param parent
	 * @return an entity that represents this object
	 */
	@Override
	public Entity toEntity(DataStoreUniqueId uniqueIdCreator, Key parent) {
		//Before converting this to an entity, change the id to a permanent if it's not already
		setUniqueId(uniqueIdCreator.ensurePermanentId(getUniqueId()));
		Entity entity = new Entity(KIND, getUniqueId(), parent);
		entity.setProperty(STATUS, getStatus().toString());
		entity.setProperty(LAST_UPDATE, getLastUpdate());
		return entity;
	}//toEntity
}//ItemCategoryInfo