/**
 * 
 */
package com.blumenthal.listey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;

public class ListInfo {
	public static final String KIND = "list";//kind in the datastore
	public static final String NAME = "name";//name in the datastore
	public static final String LAST_UPDATE = "lastUpdate";//lastUpdate in the datastore
	public static final String STATUS = "status";//status in the datastore
	public static final String ITEMS = "items";
	public static final String CATEGORIES = "categories";
	public static final String SELECTED_CATEGORIES = "selectedCategories";
	public static final String OTHER_USER_PRIVS = "otherUserPrivs";
	
	public static enum ListInfoStatus {
		ACTIVE,
		DELETED
	}
	
	public ListInfoStatus status;
	
	public String uniqueId;
	public String name;
	public Map<String, ItemInfo> items = new HashMap<String, ItemInfo>();
	public List<CategoryInfo> categories = new ArrayList<CategoryInfo>();
	public Long lastUpdate;
	//Note, selectedCategories is not stored on the server, always just mirrored back from the request
	public Set<String> selectedCategories = new HashSet<String>();
	
	public Map<String, OtherUserPrivOnList> otherUserPrivs = new HashMap<String, OtherUserPrivOnList>();
	
	/** Default constructor */
	public ListInfo(){}
	
	/**
	 * @param status
	 * @param uniqueId
	 * @param name
	 * @param items
	 * @param categories
	 * @param lastUpdate
	 * @param selectedCategories
	 */
	public ListInfo(ListInfo.ListInfoStatus status, String uniqueId, String name,
			Long lastUpdate) {
		super();
		this.status = status;
		this.uniqueId = uniqueId;
		this.name = name;
		this.lastUpdate = lastUpdate;
	}

	/** Constructor that makes a shallow copy of only the top-level fields
	 * 
	 * @param source
	 */
	public ListInfo(ListInfo source) {
		this(source.status, source.uniqueId, source.name, source.lastUpdate);
	}
	
	public ListInfo(Entity entity) {
		if (!entity.getKind().equals(KIND)){
			//check the entity type and throw if not what we're expecting
			throw new IllegalStateException("The constructor was called with an entity of the wrong kind.");
		}//if unexpected kind
		name = (String) entity.getProperty(NAME);
		uniqueId = (String) entity.getKey().getName();
		lastUpdate = (Long) entity.getProperty(LAST_UPDATE);
		status = ListInfoStatus.valueOf((String) entity.getProperty(STATUS));
	}//ListInfo(Entity)

	public Entity toEntity(Key parent) {
		Entity entity = new Entity(KIND, uniqueId, parent);
		entity.setProperty(STATUS, status.toString());
		entity.setProperty(NAME, name);
		entity.setProperty(LAST_UPDATE, lastUpdate);
		return entity;
	}//toEntity
	
	public static ListInfo compareAndUpdate(ListInfo serverList, ListInfo clientList,
			List<Entity> updateEntities, List<Entity> deleteEntities) {
		ListInfo rv = null;
		
		if (serverList == null) {
			if (clientList.status.equals(ListInfo.ListInfoStatus.ACTIVE)) {
				//New list
				
				//Copy the top-level element
				rv = new ListInfo(clientList);
				//Always put the current server time in newly changed objects
				rv.lastUpdate = System.currentTimeMillis() / 1000L;
				rv.uniqueId = ListeyDataOneUser.uniqueIdCreator.getUniqueId();
				updateEntities.add(rv.toEntity(null));//XXX fix this!!

				//copy each low-level element
			}//new list
			else {
				//deleted on client, don't add to server, nothing to do
			}
		}//serverList == null

		else if (clientList == null) {
			//XXX
		}//clientList == null
		
		else {//serverList exists
			rv = new ListInfo();
			
		}//neither list is null
		
		return rv;
	}//compareAndUpdate
}//ListInfo