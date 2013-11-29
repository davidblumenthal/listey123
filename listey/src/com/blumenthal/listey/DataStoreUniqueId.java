/**
 * 
 */
package com.blumenthal.listey;

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
	int numShards = 20;
	
	public DataStoreUniqueId(){
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key numShardsKey = KeyFactory.createKey("numShards", "singleton");
		Entity numShardsEntity;
		try {
			numShardsEntity = datastore.get(numShardsKey);
			numShards = (Integer) numShardsEntity.getProperty("numShards");
		} catch (EntityNotFoundException e) {
			// If it's not found, we'll just use the default
		}
	}//constructor
	
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
	boolean isTemporaryId(String id) {
		return (id.charAt(0) == ':');
	}//isTemporaryId
}//DataStoreUniqueId
