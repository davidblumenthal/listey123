/**
 * Holder for the set of lists owned by a single user.
 */
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
import com.google.gson.Gson;

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
	
	public Map<String, ListInfo> lists = new HashMap<String, ListInfo>();
	
	/** Default constructor */
	public ListeyDataOneUser(){}
	
	/** Copy constructor using json serialization */
	public ListeyDataOneUser makeCopy(ListeyDataOneUser orig) {
		Gson gson = ListeyDataMultipleUsers.getGson();
		String json = gson.toJson(this);
		ListeyDataOneUser copy = gson.fromJson(json, this.getClass());
		return copy;
	}//makeCopy
	
	
	/** Load the whole ListeyDataOneUser object from the datastore using the userEmail
	 * as the starting place.
	 */
	public static ListeyDataOneUser fromDatastore(DatastoreService datastore, String userEmail) {
		return fromDatastore(datastore, userEmail, null, null);
	}//fromDatastore
	
	
	public static Key getEntityKey(String userEmail) {
		//Find all entities for the user, or the user's list if listUniqueId is passed.
		return KeyFactory.createKey(KIND, userEmail);
	}//getEntityKey
	
	
	
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
		Key filterKey = getEntityKey(userEmail);
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
				oneUser.lists.put(listInfo.getUniqueId(), listInfo);
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
					listeyList.getCategories().add(catInfo);
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
					listeyList.getItems().put(e.getKey().getName(), itemInfo);
				}
			}//foreach entity
		}//if any items defined

		//Load all item categories for this list
		entityList = entitiesByKind.get(ItemCategoryInfo.KIND);
		if (entityList != null) {
			for (Entity e : entityList) {
				ItemCategoryInfo itemCategoryInfo = new ItemCategoryInfo(e);
				String listId = e.getParent().getParent().getName();
				String itemId = e.getParent().getName();
				
				//find the list it goes in
				ListInfo listeyList = oneUser.lists.get(listId);
				if (listeyList != null) {
					//find the item it goes in
					ItemInfo item = listeyList.getItems().get(itemId);
					if (item != null) {
						item.getCategories().put(e.getKey().getName(), itemCategoryInfo);
					}//if item found
				}//if list found
			}//foreach entity
		}//if any items defined
		
		//Load all other user priv on list for this list
		//Note, if listId was passed, then we should't load this
		//because listUniqueId is only passed when we're pulling in a different users list,
		//and we don't want to show the other users that different user has granted privs to
		if (listUniqueId == null) {
			entityList = entitiesByKind.get(OtherUserPrivOnList.KIND);
			if (entityList != null) {
				for (Entity e : entityList) {
					OtherUserPrivOnList priv = new OtherUserPrivOnList(e);
					String listId = e.getParent().getName();
					ListInfo listeyList = oneUser.lists.get(listId);
					if (listeyList != null) {
						listeyList.getOtherUserPrivs().put(e.getKey().getName(), priv);
					}
				}//foreach entity
			}//if any items defined
		}//if listUniqueId not passed
		return oneUser;
	}//constructor load from datastore
	
	
	/** 
	 * @param parent entity key
	 * @return a list of all entities for this object and all sub-objects
	 */
	public List<Entity> toEntities(DataStoreUniqueId uniqueIdCreator, String userEmail) {
		List<Entity> entities = new ArrayList<Entity>();
		Key thisKey = getEntityKey(userEmail);
		for (Map.Entry<String, ListInfo> entry : lists.entrySet()) {
			entities.addAll(entry.getValue().toEntities(uniqueIdCreator, thisKey));
		}//foreach item
		
		return entities;
	}//toEntities
	
	
	
	/**
	 * @param other
	 * @return Returns true if this object is essentially the same
	 * as other, and all sub-objects are also the same
	 */
	public boolean deepEquals(ListeyDataOneUser other) {
		if (lists.size() != other.lists.size()) {
			return false;
		}
		
		for (Map.Entry<String, ListInfo> entry : lists.entrySet()) {
			ListInfo otherList = other.lists.get(entry.getKey());
			if (!entry.getValue().deepEquals(otherList)) {
				return false;
			}
		}//foreach list
		
		//If we get to here, everything is equal
		return true;
	}//deepEquals

}//ListeyDataOneUser
