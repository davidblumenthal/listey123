package com.blumenthal.listey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
  	    userData
  		"lists" : {
    		     "<uniqueId>" : {//uniqueId is a unique identifier generated on the server (e.g. 1:3), or temporary on the client (e.g. ':3')
    		     	"name" : "List Name"
    		         "items" :
    		               [{"uniqueId" : "<uniqueId>",
    		                 "name" : "<NAME1>",
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
	
	public Long lastUpdate;
	public Map<String, ListInfo> lists;
	
	/** Default constructor */
	public ListeyDataOneUser(){}
	
	
	/** Load the whole ListeyDataOneUser object from the datastore using the userEmail
	 * as the starting place.
	 */
	public static ListeyDataOneUser fromDatastore(DatastoreService datastore, String userEmail) {
		return fromDatastore(datastore, userEmail, null, null);
	}//fromDatastore
	
	
	/** Load the info for the user from the datastore.
	 *
	 * @param datastore
	 * @param userEmail
	 * @param listUniqueId - If not null, only load info for that list only.
	 * @param oneUser - if not null, add to that and return, otherwise instantiate a new copy.
	 * @return
	 */
	public static ListeyDataOneUser fromDatastore(DatastoreService datastore, String userEmail, String listUniqueId, ListeyDataOneUser oneUser) {
		if (oneUser == null) {
			oneUser = new ListeyDataOneUser();
		}

		//Find all entities for the user, or the user's list if listUniqueId is passed.
		Key filterKey = KeyFactory.createKey(KIND, userEmail);
		if (listUniqueId != null) {
			filterKey = KeyFactory.createKey(filterKey, ListInfo.KIND, listUniqueId);
		}
		
		//Get all entities for this user in one big list
		Query q =  new Query().setAncestor(filterKey);
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
				oneUser.lists.put(listInfo.uniqueId, listInfo);
			}//for each list entity
		}//if any lists defined

		//Load all categories for this list
		entityList = entitiesByKind.get(CategoryInfo.KIND);
		if (entityList != null) {
			for (Entity e : entityList) {
				CategoryInfo catInfo = new CategoryInfo(e);
				String listId = e.getParent().getName();
				ListInfo listeyList = oneUser.lists.get(listId);
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
				ListInfo listeyList = oneUser.lists.get(listId);
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
				ListInfo listeyList = oneUser.lists.get(listId);
				if (listeyList != null) {
					//find the item it goes in
					ItemInfo item = listeyList.items.get(itemId);
					if (item != null) {
						item.categories.put(e.getKey().getName(), itemCategoryInfo);
					}//if item found
				}//if list found
			}//foreach entity
		}//if any items defined
		return oneUser;
	}//constructor load from datastore
	
	
	
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
