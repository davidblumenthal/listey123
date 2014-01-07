/**
 * 
 */
package com.blumenthal.listey;

import java.util.HashMap;
import java.util.Map;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Transaction;

/**
 * @author David
 *
 */
public class DataStoreUniqueId {
	public static int numShards = 20;
	
	private Map<String, String> tempToPermanentId = new HashMap<String,String>();
	
	public String getUniqueId() {
		int shardNum = (int) ((Math.random()*numShards)) + 1;//1-numShards
		Key shardKey = KeyFactory.createKey("uniqueIdShard", Integer.toString(shardNum));
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Transaction txn = datastore.beginTransaction();
		Long shardVal = null;
		try {
			Entity shard = null;
			try {
				shard = datastore.get(shardKey);
			} catch (EntityNotFoundException e) {
				shard = new Entity(shardKey);
				shard.setProperty("value", new Long(0));
			}//EntityNotFoundException
			shardVal = ((Long)shard.getProperty("value")) + 1;
			shard.setProperty("value", shardVal);
			datastore.put(shard);
			txn.commit();
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}
	    return (shardNum + ":" + shardVal);
	}//getUniqueId
	
	/**
	 * Pass this a unique id and it will return true if this is a temporary ID (created by a client).
	 * Temporary IDs start with a colon
	 * 
	 * @param id A unique id string to check.
	 * @return Whether id is a temporary id (starts with colon).
	 */
	public static boolean isTemporaryId(String id) {
		return (id.charAt(0) == ':');
	}//isTemporaryId
	
	

	/**
	 * Returns a permanent ID to use for the given id.
	 * If id is already a permanent ID, then it is returned.
	 * Otherwise, if id is a temporary ID, then a permanent one is created and returned.
	 * Once a permanent id is created for a temporary id, the 
	 * same permanent id is always returned for the same temporary one (for the same object instance). 
	 */
	public String ensurePermanentId(String id){
		if (isTemporaryId(id)) {
			if (tempToPermanentId.containsKey(id)){
				id = tempToPermanentId.get(id);
			}
			else {
				String newId = getUniqueId();
				tempToPermanentId.put(id, newId);
				id = newId;
			}
		}//if isTemporaryId
		
		return id;
	}//ensurePermanentId
}//DataStoreUniqueId
