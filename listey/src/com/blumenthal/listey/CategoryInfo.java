/**
 * Stores category (store) information for a list.
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
	
	public String name;
	public String uniqueId;
	public Long lastUpdate;
	public CategoryStatus status;
	
	/** Default constructor */
	public CategoryInfo(){}
	
	public CategoryInfo(Entity entity) {
		if (!entity.getKind().equals(KIND)){
			//check the entity type and throw if not what we're expecting
			throw new IllegalStateException("The constructor was called with an entity of the wrong kind.");
		}//if unexpected kind
		lastUpdate = (Long) entity.getProperty("lastUpdate");
		name = (String) entity.getProperty(NAME);
		uniqueId = (String) entity.getKey().getName();
		status = CategoryStatus.valueOf((String) entity.getProperty(STATUS));
	}//CategoryInfo(Entity)
	
	
	
	/**
	 * @param parent
	 * @return an entity that represents this object
	 */
	public Entity toEntity(Key parent) {
		Entity entity = new Entity(KIND, uniqueId, parent);
		entity.setProperty(STATUS, status.toString());
		entity.setProperty(NAME, name);
		entity.setProperty(LAST_UPDATE, lastUpdate);
		return entity;
	}//toEntity

	
	/**
	 * @param other
	 * @return Returns true if all essential fields of this object
	 * are the same as other.
	 */
	public boolean shallowEquals(CategoryInfo other) {
		return (uniqueId.equals(other.uniqueId)
				&& name.equals(other.name)
				&& lastUpdate.equals(other.lastUpdate)
				&& status.equals(other.status));
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
		return name.compareTo(o.name);
	}
}//CategoryInfo