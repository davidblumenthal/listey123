/**
 * Top-level object for holding all the information for a particular login.
 * 
 * Note, since users can share lists, a ListeyDataMultipleUsers object can contain information
 * for the user who logs in AND other user's lists that were shared with him/her.
 * 
 * This and its sub-objects is intended to map pretty directly to the JSON stored on the client.
 */
package com.blumenthal.listey;

import static com.blumenthal.listey.JsonFieldNameConstants.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

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
	public static final String USER_DATA = "userData";
	private static final Logger log = Logger.getLogger(TimeStampedNode.class.getName());
	private String thisUserEmail;
	public Map< String, ListeyDataOneUser> userData = new HashMap<String, ListeyDataOneUser>();
	
	/**
	 * No-argument constructor desired by GSON
	 */
	public ListeyDataMultipleUsers() {
		super();
	}
	
	/** 
	 * 
	 * @return
	 */
	public ListeyDataMultipleUsers(DatastoreService datastore, String userEmail) {
		setThisUserEmail(userEmail);
    	//Load the current user data from the datastore by the user's email address
    	ListeyDataOneUser currentUserData = ListeyDataOneUser.fromDatastore(datastore, userEmail);
    	userData.put(userEmail, currentUserData);
    	
    	//Load other user's lists that this user should be able to access
		Query q =  new Query(OtherUserPrivOnList.KIND)
			.setFilter(new FilterPredicate(USER_ID,
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
	
	
	/** 
	 * @param parent entity key
	 * @return a list of all entities for this object and all sub-objects
	 */
	public List<Entity> toEntities(DataStoreUniqueId uniqueIdCreator) {
		List<Entity> entities = new ArrayList<Entity>();
		for (ListeyDataOneUser userInfo : userData.values()) {
			entities.addAll(userInfo.toEntities(uniqueIdCreator, null));
		}//foreach item
		
		return entities;
	}//toEntities
	
	
	
	public static ListeyDataMultipleUsers compareAndUpdate(DataStoreUniqueId uniqueIdCreator, ListeyDataMultipleUsers serverObj, ListeyDataMultipleUsers clientObj,
			List<Entity> updateEntities, List<Key> deleteKeys) {
		ListeyDataMultipleUsers rv = new ListeyDataMultipleUsers();
		
		Map<String, ListeyDataOneUser> clientUserMap = clientObj.userData;
		Map<String, ListeyDataOneUser> serverUserMap = serverObj.userData;

		//iterate only through the server set, because if a user only exists on the client and not on the server
		//that means they shouldn't have privs
		Set<String> uneditableSet = serverUserMap.keySet();
		//Need to make a new copy if I want to add to it.
		Set<String> userEmails = new HashSet<String>(uneditableSet);
		//Add this user email even if nothing set up on server
		userEmails.add(serverObj.getThisUserEmail());

		for (String userEmail : userEmails) {
			ListeyDataOneUser clientSubObj = clientUserMap.get(userEmail);
			ListeyDataOneUser serverSubObj = serverUserMap.get(userEmail);

			ListeyDataOneUser updatedObj = (ListeyDataOneUser) ListeyDataOneUser.compareAndUpdate(uniqueIdCreator, null, serverSubObj, clientSubObj, updateEntities, deleteKeys);
			rv.userData.put(updatedObj.getUniqueId(), updatedObj);
		}//for each subObj
		return rv;
	}//compareAndUpdate
	
	
	
	/**
	 * @param other
	 * @return Returns true if this object is essentially the same
	 * as other, and all sub-objects are also the same
	 */
	public boolean deepEquals(ListeyDataMultipleUsers other) {
		if (userData.size() != other.userData.size()) {
			return false;
		}
		
		for (Map.Entry<String, ListeyDataOneUser> entry : userData.entrySet()) {
			ListeyDataOneUser otherUser = other.userData.get(entry.getKey());
			if (!entry.getValue().deepEquals(otherUser)) {
				return false;
			}
		}//foreach list
		
		//If we get to here, everything is equal
		return true;
	}//deepEquals

	
	public static Gson getGson() {
		return getGson(false);
	}
	
	
	
	public static Gson getGson(boolean doAllFields) {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(ListInfo.class, new ListInfoJsonAdapter(doAllFields));
		gsonBuilder.registerTypeAdapter(ItemInfo.class, new ItemInfoJsonAdapter(doAllFields));
		gsonBuilder.registerTypeAdapter(ListeyDataMultipleUsers.class, new ListeyDataMultipleUsersJsonAdapter(doAllFields));
		gsonBuilder.registerTypeAdapter(ListeyDataOneUser.class, new ListeyDataOneUserJsonAdapter(doAllFields));
		return gsonBuilder.create();
	}//getGson
	
	
	
	/**
	 * Return an object from serialized JSON string
	 */
	public static ListeyDataMultipleUsers fromJson(String thisUserEmail, String jsonString) {
		Gson gson = getGson();
		ListeyDataMultipleUsers rv = gson.fromJson(jsonString, ListeyDataMultipleUsers.class);
		rv.setThisUserEmail(thisUserEmail);
		return rv;
	}//fromJson
	
	
	
	/**
	 * Return an object from serialized JSON string
	 */
	public String toJson() {
		Gson gson = getGson();
		String rv = gson.toJson(this);
		return rv;
	}//fromJson

	/**
	 * @return the log
	 */
	private static Logger getLog() {
		return log;
	}

	/**
	 * @return the thisUserEmail
	 */
	public String getThisUserEmail() {
		return thisUserEmail;
	}

	/**
	 * @param thisUserEmail the thisUserEmail to set
	 */
	private void setThisUserEmail(String thisUserEmail) {
		this.thisUserEmail = thisUserEmail;
	}
}//ListeyDataMultipleUsers
