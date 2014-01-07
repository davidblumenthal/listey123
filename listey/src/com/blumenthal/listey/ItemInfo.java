/**
 * Stores info about a list item.
 */
package com.blumenthal.listey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

public class ItemInfo extends TimeStampedNode {
	public static final String KIND = "item";//kind in the datastore
	public static final String NAME = "name";
	public static final String STATUS = "status";
	public static final String COUNT = "count";
	public static final String LAST_UPDATE = "lastUpdate";
	public static final String UNIQUE_ID = "uniqueId";
	public static final String CATEGORIES = "categories";
	
	private String name;
	private String uniqueId;
	private Long count = 1L;
	private Status status = TimeStampedNode.Status.ACTIVE;
	private Map<String, ItemCategoryInfo> categories = new HashMap<String, ItemCategoryInfo>();
	private Long lastUpdate;
	
	/** Default constructor */
	public ItemInfo(){}
	
	
	public ItemInfo(Entity entity) {
		if (!entity.getKind().equals(KIND)){
			//check the entity type and throw if not what we're expecting
			throw new IllegalStateException("The constructor was called with an entity of the wrong kind. (" + entity.getKind() + ").");
		}//if unexpected kind
		
		setLastUpdate((Long) entity.getProperty(LAST_UPDATE));
		setName((String) entity.getProperty(NAME));
		setUniqueId((String) entity.getKey().getName());
		setCount((Long) entity.getProperty(COUNT));
		setStatus(Status.valueOf((String) entity.getProperty(STATUS)));
	}//ItemInfo(Entity)
	
	
	
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
	 * @return the count
	 */
	public Long getCount() {
		return count;
	}


	/**
	 * @param count the count to set
	 */
	public void setCount(Long count) {
		this.count = count;
	}


	/**
	 * @return the status
	 */
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
	 * @param parent
	 * @return an entity that represents this object (but not its child objects)
	 */
	@Override
	public Entity toEntity(DataStoreUniqueId uniqueIdCreator, Key parent) {
		//Before converting this to an entity, change the id to a permanent if it's not already
		setUniqueId(uniqueIdCreator.ensurePermanentId(getUniqueId()));
		Entity entity = new Entity(getEntityKey(parent));
		entity.setProperty(STATUS, getStatus().toString());
		entity.setProperty(NAME, getName());
		entity.setProperty(COUNT,  getCount());
		entity.setProperty(LAST_UPDATE, getLastUpdate());
		return entity;
	}//toEntity
	
	
	
	/** Returns a list of all entities for this object and all sub-objects
	 * 
	 * @param parent
	 * @return
	 */
	@Override
	public List<Entity> toEntities(DataStoreUniqueId uniqueIdCreator, Key parent) {
		List<Entity> entities = new ArrayList<Entity>();
		Entity thisEntity = toEntity(uniqueIdCreator, parent);
		entities.add(thisEntity);
		for (Map.Entry<String, ItemCategoryInfo> entry : getCategories().entrySet()) {
			entities.add(entry.getValue().toEntity(uniqueIdCreator, thisEntity.getKey()));
		}//foreach category
		return entities;
	}//toEntities
	
	
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
		ItemInfo other = (ItemInfo) obj;
		return (getUniqueId().equals(other.getUniqueId())
				&& getName().equals(other.getName())
				&& getLastUpdate().equals(other.getLastUpdate())
				&& getStatus().equals(other.getStatus())
				&& getCount().equals(other.getCount()));
	}//shallowEquals
	
	
	@Override
	public TimeStampedNode makeShallowCopy() {
		ItemInfo newObj = new ItemInfo();
		newObj.setUniqueId(getUniqueId());
		newObj.setName(getName());
		newObj.setStatus(getStatus());
		newObj.setLastUpdate(getLastUpdate());
		newObj.setCount(getCount());
		return newObj;
	}
	
	
	/**
	 * @param other
	 * @return Returns true if this object is essentially the same
	 * as other, and all sub-objects are also.
	 */
	public boolean deepEquals(ItemInfo other) {
		if (!shallowEquals(other)
			|| getCategories().size() != other.getCategories().size()) {
			return false;
		}
		for (Map.Entry<String, ItemCategoryInfo> entry : getCategories().entrySet()) {
			ItemCategoryInfo otherCat = other.getCategories().get(entry.getKey());
			if (!entry.getValue().deepEquals(otherCat)) {
				return false;
			}
		}//foreach category
		
		//If we get to here, everything is equal
		return true;
	}//deepEquals


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
	 * @return the categories
	 */
	public Map<String, ItemCategoryInfo> getCategories() {
		return categories;
	}


	/**
	 * @param categories the categories to set
	 */
	public void setCategories(Map<String, ItemCategoryInfo> categories) {
		this.categories = categories;
	}


	/* (non-Javadoc)
	 * @see com.blumenthal.listey.TimeStampedNode#subMaps()
	 */
	@Override
	public List<Map<String, ? extends TimeStampedNode>> subMapsToCompare() {
		List<Map<String, ? extends TimeStampedNode>> rv = new ArrayList<Map<String, ? extends TimeStampedNode>>();
		rv.add(getCategories());
		return (rv);
	}//subMapsToCompare
	


	/* (non-Javadoc)
	 * @see com.blumenthal.listey.TimeStampedNode#addSubMapEntries(java.util.List)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void addSubMapEntries(List<List<? extends TimeStampedNode>> subMapEntriesToAdd) {
		List<ItemCategoryInfo> catInfos = (List<ItemCategoryInfo>) subMapEntriesToAdd.get(0);
		for (ItemCategoryInfo catInfo : catInfos) {
			categories.put(catInfo.getUniqueId(), catInfo);
		}
	}//addSubMapEntries


	/* (non-Javadoc)
	 * @see com.blumenthal.listey.TimeStampedNode#getKind()
	 */
	@Override
	public String getKind() {
		return KIND;
	}
}//ItemInfo