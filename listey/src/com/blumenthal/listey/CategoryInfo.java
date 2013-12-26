/**
 * Stores category (aka store) information for a list.
 */
package com.blumenthal.listey;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

public class CategoryInfo implements Comparable<CategoryInfo> {
	public static final String KIND = "category";//kind in the datastore
	public static final String STATUS = "status";
	public static final String NAME = "name";
	public static final String LAST_UPDATE = "lastUpdate";
	
	public static enum CategoryStatus {
	    ACTIVE,
	    DELETED
	}
	
	private String name;
	private String uniqueId;
	private Long lastUpdate;
	private CategoryStatus status;
	
	/** Default constructor */
	public CategoryInfo(){}
	
	public CategoryInfo(Entity entity) {
		if (!entity.getKind().equals(KIND)){
			//check the entity type and throw if not what we're expecting
			throw new IllegalStateException("The constructor was called with an entity of the wrong kind.");
		}//if unexpected kind
		setLastUpdate((Long) entity.getProperty("lastUpdate"));
		setName((String) entity.getProperty(NAME));
		setUniqueId((String) entity.getKey().getName());
		setStatus(CategoryStatus.valueOf((String) entity.getProperty(STATUS)));
	}//CategoryInfo(Entity)
	
	
	
	/**
	 * @param parent
	 * @return an entity that represents this object
	 */
	public Entity toEntity(Key parent) {
		Entity entity = new Entity(KIND, getUniqueId(), parent);
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
	public boolean shallowEquals(CategoryInfo other) {
		return (getUniqueId().equals(other.getUniqueId())
				&& getName().equals(other.getName())
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
	public boolean deepEquals(CategoryInfo other) {
		return (shallowEquals(other));
	}//deepEquals
	
	

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(CategoryInfo o) {
		return getName().compareTo(o.getName());
	}

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
	public CategoryStatus getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(CategoryStatus status) {
		this.status = status;
	}
}//CategoryInfo