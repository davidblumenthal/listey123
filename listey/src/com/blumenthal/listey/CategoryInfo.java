/**
 * Stores category (aka store) information for a list.
 */
package com.blumenthal.listey;

import static com.blumenthal.listey.JsonFieldNameConstants.*;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

public class CategoryInfo extends TimeStampedNode {
	private String name;
	private String uniqueId;
	private Long lastUpdate;
	private Status status = TimeStampedNode.Status.ACTIVE;
	
	/** Default constructor */
	public CategoryInfo(){}
	
	public CategoryInfo(Entity entity) {
		if (!entity.getKind().equals(KIND)){
			//check the entity type and throw if not what we're expecting
			throw new IllegalStateException("The constructor was called with an entity of the wrong kind (" + entity.getKind() + "): " + entity);
		}//if unexpected kind
		setLastUpdate((Long) entity.getProperty("lastUpdate"));
		setName((String) entity.getProperty(NAME));
		setUniqueId((String) entity.getKey().getName());
		setStatus(Status.valueOf((String) entity.getProperty(STATUS)));
	}//CategoryInfo(Entity)
	
	
	
	/**
	 * @param parent
	 * @return an entity that represents this object
	 */
	@Override
	public Entity toEntity(DataStoreUniqueId uniqueIdCreator, Key parent) {
		//Before converting this to an entity, change the id to a permanent if it's not already
		setUniqueId(uniqueIdCreator.ensurePermanentId(getUniqueId()));
		Entity entity = new Entity(getEntityKey(parent));
		entity.setProperty(STATUS, getStatus().toString());
		entity.setProperty(NAME, getName());
		entity.setProperty(LAST_UPDATE, getLastUpdate());
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
		CategoryInfo other = (CategoryInfo) obj;
		return (getUniqueId().equals(other.getUniqueId())
				&& getName().equals(other.getName())
				&& getLastUpdate().equals(other.getLastUpdate())
				&& getStatus().equals(other.getStatus()));
	}//shallowEquals
	
	
	@Override
	public TimeStampedNode makeShallowCopy() {
		CategoryInfo newObj = new CategoryInfo();
		newObj.setUniqueId(getUniqueId());
		newObj.setName(getName());
		newObj.setStatus(getStatus());
		newObj.setLastUpdate(getLastUpdate());
		return newObj;
	}
	
	
	/**
	 * @param other
	 * @return Returns true if this object is essentially the same
	 * as other, and all sub-objects are also.
	 * 
	 * This has no other layers below it, so deepEquals can just call shallowEquals
	 */
	public boolean deepEquals(CategoryInfo other) {
		return (shallowEquals(other));
	}//deepEquals
	
	

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

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
	
	
	
	/* (non-Javadoc)
	 * @see com.blumenthal.listey.TimeStampedNode#getKind()
	 */
	@Override
	public String getKind() {
		return KIND;
	}
}//CategoryInfo