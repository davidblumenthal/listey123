package com.blumenthal.listey;

import java.util.HashMap;
import java.util.Map;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/** This class is a java implementation of the JSON primitive for spec version 2
 * JSON Data Format
   {
  	    "lastUpdate": "1234567890",
  		"userData" : {
    		'<CURRENT_USER_EMAIL' : {
				<SEE ListyDataOneUser>
            },
            '<OTHER_USER_EMAIL>' : {... see <CURRENT_USER_EMAIL> above}
        }//lists
  }//top-level
 */
public class ListeyDataMultipleUsers {
	public Long lastUpdate;
	public Map< String, ListeyDataOneUser> userData = new HashMap<String, ListeyDataOneUser>();
	
	/**
	 * No-argument constructor desired by GSON
	 */
	public ListeyDataMultipleUsers() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	/** 
	 * 
	 * @return
	 */
	public ListeyDataMultipleUsers(DatastoreService datastore, String userEmail) {
    	//Load the current user data from the datastore by the user's email address
    	ListeyDataOneUser currentUserData = ListeyDataOneUser.fromDatastore(datastore, userEmail);
    	userData.put(userEmail, currentUserData);
    	
    	//Load other user's lists that this user should be able to access
		Query q =  new Query()
			.setFilter(new FilterPredicate(Entity.KEY_RESERVED_PROPERTY,
                Query.FilterOperator.EQUAL,
                userEmail));
		PreparedQuery pq = datastore.prepare(q);
    	
    	//Look up and insert info for each other user's list individually
		for (Entity privEntity : pq.asIterable()) {
			Key listKey = privEntity.getKey().getParent();
			Key userKey = listKey.getParent();
			String otherUserEmail = userKey.getName();
			String listUniqueId = listKey.getName();
			
			//Append the other user info for this list to existing other user info, or create the other user info if it doesn't exist
			ListeyDataOneUser otherUserData = ListeyDataOneUser.fromDatastore(datastore, otherUserEmail, listUniqueId, userData.get(otherUserEmail));
			userData.put(otherUserEmail, otherUserData);
		}//foreach privEntity
	}//Constructor
	
	
	
	public static Gson getGson() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(ListInfo.class, new ListInfoJsonAdapter());
		gsonBuilder.registerTypeAdapter(ItemInfo.class,  new ItemInfoJsonAdapter());
		return gsonBuilder.create();
	}//getGson
	
	
	
	/**
	 * Return an object from serialized JSON string
	 */
	public static ListeyDataMultipleUsers fromJson(String jsonString) {
		Gson gson = getGson();
		ListeyDataMultipleUsers rv = gson.fromJson(jsonString, ListeyDataMultipleUsers.class);
		return rv;
	}//fromJson
}//ListeyDataMultipleUsers
