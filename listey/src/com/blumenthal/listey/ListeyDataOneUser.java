package com.blumenthal.listey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;

/** This class is a java implementation of the JSON primitive for spec version 1
 * JSON Data Format
   {
  	    "lastUpdate": "1234567890",
  		"lists" : {
    		     <listName> : {
    		         "items" :
    		               [{"name" : "<NAME1>",
    		                 "categories" : {"<CATEGORY1>" : true, ...},
    		                 "count" : <NUMBER>,
    		                 "state" : "ACTIVE"/"COMPLETED"/"DELETED"
                             "lastUpdate": "1234567890",
                            },
                            ...
                           ],
                     "lastUpdate": "1234567890",
                     "categories": [{name="<CATEGORY_NAME>", "lastUpdate"=123456789}, ...],
                     "selectedCategories" : ["<CATEGORY1>", ...]
                 }//<listName>
        }//lists
  }//top-level
 */


public class ListeyDataOneUser {
	public static final String KIND = "user";//kind in the datastore
	public static DataStoreUniqueId uniqueIdCreator = new DataStoreUniqueId();
	
	public static enum ItemStatus {
	    ACTIVE,
	    COMPLETED
	}
	
	public static enum ItemCategoryStatus {
		ACTIVE,
		DELETED
	}
	public static class ListInfo {
		public static final String KIND = "list";//kind in the datastore
		public static final String NAME = "displayName";//name in the datastore
		public static final String LAST_UPDATE = "lastUpdate";//lastUpdate in the datastore
		
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
		public ListInfo(ListInfoStatus status, String uniqueId, String name,
				Long lastUpdate) {
			super();
			this.status = status;
			this.uniqueId = uniqueId;
			this.name = name;
			this.lastUpdate = lastUpdate;
		}

		//Constructor that makes a shallow copy of only the top-level fields
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
		}//ListInfo(Entity)

		public Entity toEntity(Key parent) {
			Entity entity = new Entity(KIND, uniqueId, parent);
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
					rv.uniqueId = uniqueIdCreator.getUniqueId();
					updateEntities(rv.toEntity());

					//copy each low-level element
				}//new list
				else {
					//deleted on client, don't add to server, nothing to do
				}
			}//serverList == null

			else {//serverList exists
				serverList = new ListInfo();
				
			}
			
			return rv;
		}//compareAndUpdate
	}//ListInfo
	
	public Long lastUpdate;
	public Map<String, ListInfo> lists;
	private Map<String, String> tempToPermanentId = new HashMap<String,String>();
	
	/**
	 * Returns a permanent ID to use for the given id.
	 * If id is already a permanent ID, then it is returned.
	 * Otherwise, if id is a temporary ID, then a permanent one is created and returned.
	 * Once a permanent id is created for a temporary id, the 
	 * same permanent id is always returned for the same temporary one (for the same object instance). 
	 */
	public String ensurePermanentId(String id){
		if (uniqueIdCreator.isTemporaryId(id)) {
			if (tempToPermanentId.containsKey(id)){
				id = tempToPermanentId.get(id);
			}
			else {
				String newId = uniqueIdCreator.getUniqueId();
				tempToPermanentId.put(id, newId);
				id = newId;
			}
		}//if isTemporaryId
		
		return id;
	}//ensurePermanentId
	
	
	
	/** Default constructor */
	public ListeyDataOneUser(){}
	
	
	/** Load the whole ListeyDataOneUser object from the datastore using the userEmail
	 * as the starting place.
	 */
	public ListeyDataOneUser (DatastoreService datastore, String userEmail) {
        // We have one entity group per user
		Key userKey = KeyFactory.createKey(KIND, userEmail);
		//Get all entities for this user in one big list
		Query q =  new Query().setAncestor(userKey);
		List<Entity> results = datastore.prepare(q)
				.asList(FetchOptions.Builder.withDefaults());

		//Split the entities out into different kinds, because it's important that we load them in order by kind
		Map<String,List<Entity>> entitiesByKind = new HashMap<String,List<Entity>>(); 
		for (Entity e : results) {
			String kind = e.getKind();
			List<Entity> list = entitiesByKind.get(kind);
			if (list == null) {
				list = new ArrayList<Entity>();
				entitiesByKind.put(kind, list);
			}
			list.add(e);        			
		}//for each found entity

		//Nothing interesting actually is stored in the parent entity, so just skip 'user' kind

		//Load all the list kinds
		List<Entity> entityList = entitiesByKind.get(ListInfo.KIND);
		if (entityList != null) {
			for (Entity e : entityList) {
				ListInfo listInfo = new ListInfo(e);
				lists.put(listInfo.uniqueId, listInfo);
			}//for each list entity
		}//if any lists defined

		//Load all categories for this list
		entityList = entitiesByKind.get(CategoryInfo.KIND);
		if (entityList != null) {
			for (Entity e : entityList) {
				CategoryInfo catInfo = new CategoryInfo(e);
				String listId = e.getParent().getName();
				ListInfo listeyList = lists.get(listId);
				if (listeyList != null) {
					listeyList.categories.add(catInfo);
				}
			}//foreach list entity
		}//if any categories defined

		//Load all items for this list
		entityList = entitiesByKind.get(ItemInfo.KIND);
		if (entityList != null) {
			for (Entity e : entityList) {
				ItemInfo itemInfo = new ItemInfo(e);
				String listId = e.getParent().getName();
				ListInfo listeyList = lists.get(listId);
				if (listeyList != null) {
					listeyList.items.put(e.getKey().getName(), itemInfo);
				}
			}//foreach entity
		}//if any items defined

		//Load all item categories for this list
		entityList = entitiesByKind.get("itemCategory");
		if (entityList != null) {
			for (Entity e : entityList) {
				ItemCategoryInfo itemCategoryInfo = new ItemCategoryInfo(e);
				String listId = e.getParent().getParent().getName();
				String itemId = e.getParent().getName();
				
				//find the list it goes in
				ListInfo listeyList = lists.get(listId);
				if (listeyList != null) {
					//find the item it goes in
					ItemInfo item = listeyList.items.get(itemId);
					if (item != null) {
						item.categories.put(e.getKey().getName(), itemCategoryInfo);
					}//if item found
				}//if list found
			}//foreach entity
		}//if any items defined  		
	}//constructor
	
	
	/** Compare 2 ListeyDataOneUser objects and return lists of entities
	 *  to add+update and a new up-to-date ListeyDataOneUser
	 */
	public static ListeyDataOneUser compareAndUpdate(ListeyDataOneUser serverData, ListeyDataOneUser clientData, 
			List<Entity> updateEntities, List<Entity> deleteEntities) {
		ListeyDataOneUser rv = new ListeyDataOneUser();
		
		for (ListInfo clientList : clientData.lists.values()) {
			ListInfo serverList = serverData.lists.get(clientList.uniqueId);

			ListInfo updatedListInfo = ListInfo.compareAndUpdate(serverList, clientList, updateEntities, deleteEntities);
			//If they deleted the list, don't pass it back to the client
			if (updatedListInfo != null) {
				rv.lists.put(updatedListInfo.uniqueId, updatedListInfo);
			}//updatedListInfo != null
		}//for lists
		
		return rv;
	}//compareAndUpdate
}//ListeyDataOneUser
