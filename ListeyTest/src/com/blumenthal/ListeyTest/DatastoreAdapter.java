package com.blumenthal.ListeyTest;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.blumenthal.listey.CategoryInfo;
import com.blumenthal.listey.DataStoreUniqueId;
import com.blumenthal.listey.ItemCategoryInfo;
import com.blumenthal.listey.ItemInfo;
import com.blumenthal.listey.ListInfo;
import com.blumenthal.listey.ListeyDataMultipleUsers;
import com.blumenthal.listey.ListeyDataOneUser;
import com.blumenthal.listey.OtherUserPrivOnList;
import com.blumenthal.listey.TimeStampedNode;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

/**
 * 
 */

/**
 * @author David
 *
 */
public class DatastoreAdapter {
	final String FOO_EMAIL = "foo@test.com";
	final String BAR_EMAIL = "bar@test.com";
	
	private String fooList1Id = null;
	private String fooList1Item1Id = null;
	private long uniqueTime = 10000L;
	private int tempUniqueId = 1;
	
	static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	
    private final LocalServiceTestHelper helper =
    		new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());

    @Before
    public void setUp() {
    	helper.setUp();
    }

    @After
    public void tearDown() {
    	helper.tearDown();
    }
 
    
    public ListeyDataMultipleUsers createAndSaveUser() {
		DataStoreUniqueId uniqCreator = new DataStoreUniqueId();
		
		//Create and flesh out a multi-user data structure
		ListeyDataMultipleUsers multiUser = new ListeyDataMultipleUsers();
		ListeyDataOneUser fooUser = new ListeyDataOneUser();
		fooUser.setUniqueId(FOO_EMAIL);
		multiUser.userData.put(FOO_EMAIL, fooUser);
		
		//Create Foo list1
		fooList1Id = uniqCreator.getUniqueId();
		ListInfo fooList1 = new ListInfo(TimeStampedNode.Status.ACTIVE, fooList1Id, "Foo List 1", uniqueTime++);
		fooUser.lists.put(fooList1.getUniqueId(), fooList1);
		
		OtherUserPrivOnList barPrivOnFoo1 = new OtherUserPrivOnList();
		barPrivOnFoo1.userId = BAR_EMAIL;
		barPrivOnFoo1.lastUpdate = uniqueTime++;
		barPrivOnFoo1.priv = OtherUserPrivOnList.OtherUserPriv.FULL;
		fooList1.getOtherUserPrivs().put(BAR_EMAIL, barPrivOnFoo1);
		
		CategoryInfo fooList1Cat1 = new CategoryInfo();
		fooList1Cat1.setLastUpdate(uniqueTime++);
		fooList1Cat1.setName("Foo List 1 Category 1");
		fooList1Cat1.setStatus(TimeStampedNode.Status.ACTIVE);
		fooList1Cat1.setUniqueId(uniqCreator.getUniqueId());
		fooList1.getCategories().add(fooList1Cat1);
		
		ItemInfo fooList1Item1 = new ItemInfo();
		fooList1Item1Id = uniqCreator.getUniqueId();
		fooList1Item1.setUniqueId(fooList1Item1Id);
		fooList1.getItems().put(fooList1Item1.getUniqueId(), fooList1Item1);
		fooList1Item1.setLastUpdate(uniqueTime++);
		fooList1Item1.setName("Foo List 1 Item 1");
		fooList1Item1.setStatus(TimeStampedNode.Status.ACTIVE);
		fooList1Item1.setCount(2L);
		ItemCategoryInfo fooList1Item1Cat1 = new ItemCategoryInfo();
		fooList1Item1Cat1.setUniqueId(uniqCreator.getUniqueId());
		fooList1Item1.getCategories().put(fooList1Item1Cat1.getUniqueId(), fooList1Item1Cat1);
		fooList1Item1Cat1.setLastUpdate(uniqueTime++);
		fooList1Item1Cat1.setStatus(TimeStampedNode.Status.ACTIVE);
		
		//Add list for bar user
		//Create Bar list1
		ListeyDataOneUser barUser = new ListeyDataOneUser();
		multiUser.userData.put(BAR_EMAIL, barUser);
		barUser.setUniqueId(BAR_EMAIL);
		ListInfo barList1 = new ListInfo(TimeStampedNode.Status.ACTIVE, uniqCreator.getUniqueId(), "Bar List 1", uniqueTime++);
		barUser.lists.put(barList1.getUniqueId(), barList1);
				
		OtherUserPrivOnList fooPrivOnBar1 = new OtherUserPrivOnList();
		fooPrivOnBar1.userId = FOO_EMAIL;
		fooPrivOnBar1.lastUpdate = uniqueTime++;
		fooPrivOnBar1.priv = OtherUserPrivOnList.OtherUserPriv.FULL;
		barList1.getOtherUserPrivs().put(FOO_EMAIL, fooPrivOnBar1);
				
		CategoryInfo barList1Cat1 = new CategoryInfo();
		barList1Cat1.setLastUpdate(uniqueTime++);
		barList1Cat1.setName("Bar List 1 Category 1");
		barList1Cat1.setStatus(TimeStampedNode.Status.ACTIVE);
		barList1Cat1.setUniqueId(uniqCreator.getUniqueId());
		barList1.getCategories().add(barList1Cat1);

		ItemInfo barList1Item1 = new ItemInfo();
		barList1Item1.setUniqueId(uniqCreator.getUniqueId());
		barList1.getItems().put(barList1Item1.getUniqueId(), barList1Item1);
		barList1Item1.setLastUpdate(uniqueTime++);
		barList1Item1.setName("Bar List 1 Item 1");
		barList1Item1.setStatus(TimeStampedNode.Status.ACTIVE);
		barList1Item1.setCount(2L);
		ItemCategoryInfo barList1Item1Cat1 = new ItemCategoryInfo();
		barList1Item1Cat1.setUniqueId(uniqCreator.getUniqueId());
		barList1Item1.getCategories().put(barList1Item1Cat1.getUniqueId(), barList1Item1Cat1);
		barList1Item1Cat1.setLastUpdate(uniqueTime++);
		barList1Item1Cat1.setStatus(TimeStampedNode.Status.ACTIVE);
		
		//Convert multiUser to entities and write all the entities to the datastore at once
		List<Entity> entities = multiUser.toEntities(uniqCreator);
		//Also convert bar's list to entities, since foo has privs on bar's list
		Key barKey = KeyFactory.createKey(ListeyDataOneUser.KIND, BAR_EMAIL);
		entities.addAll(barList1.toEntities(uniqCreator, barKey));
		datastore.put(entities);
		
		
		//Now wipe the other user privs map from bar, since those won't be reloaded
		//when we load data for foo
		for (Map.Entry<String, ListInfo> entry : multiUser.userData.get(BAR_EMAIL).lists.entrySet()) {
			entry.getValue().getOtherUserPrivs().clear();
		}
		
		return multiUser;
    }//createAndSaveUser
    
    
	
    public ListeyDataMultipleUsers[] createAndReloadUser() {
		ListeyDataMultipleUsers clientMultiUser = createAndSaveUser();

		//Now, reload (a new copy) back from the datastore
		ListeyDataMultipleUsers serverMultiUser = new ListeyDataMultipleUsers(datastore, FOO_EMAIL);
		
		return new ListeyDataMultipleUsers[] {clientMultiUser, serverMultiUser};
    }//createAndReloadUser
    
    
	@Test
	public void testSaveAndLoadUser(){
		ListeyDataMultipleUsers[] users = createAndReloadUser();
		ListeyDataMultipleUsers clientMultiUser = users[0];
		ListeyDataMultipleUsers serverMultiUser = users[1];
		
		//Verify loaded version matches saved version
		assertEquals("before/after multi-user jsons don't match", clientMultiUser.toJson(), serverMultiUser.toJson());
		assertEquals("loadedMultiUser doesn't match original", true, clientMultiUser.deepEquals(serverMultiUser));
	}//testLoadAndSaveUser
	
	
	@Test
	public void testClientChange() {
		ListeyDataMultipleUsers[] users = createAndReloadUser();
		ListeyDataMultipleUsers clientMultiUser = users[0];
		ListeyDataMultipleUsers serverMultiUser = users[1];
		
		//Now change the client and test compareAndUpdate
		DataStoreUniqueId uniqueIdCreator = new DataStoreUniqueId();
		
		//*******************************************
		//Change list name
		ListInfo clientList1 = clientMultiUser.userData.get(FOO_EMAIL).lists.get(fooList1Id);
		clientList1.setName("Foo List 1 new name");
		clientList1.setLastUpdate(uniqueTime++);
		
		//Add new list
		ListInfo clientList2 = new ListInfo();
		clientList2.setLastUpdate(uniqueTime++);
		clientList2.setName("Foo List 2");
		clientList2.setUniqueId(":" + tempUniqueId++);
		clientMultiUser.userData.get(FOO_EMAIL).lists.put(clientList2.getUniqueId(), clientList2);
		
		//Change item name
		ItemInfo clientItem1 = clientList1.getItems().get(fooList1Item1Id);
		clientItem1.setName("Foo List 1 Item 1 new name");
		clientItem1.setLastUpdate(uniqueTime++);
		
		//Add new item
		ItemInfo clientItem2 = new ItemInfo();
		clientItem2.setUniqueId(":" + tempUniqueId++);
		clientItem2.setCount(2L);
		clientItem2.setLastUpdate(uniqueTime++);
		clientItem2.setName("Foo List 1 Item 2");
		clientList1.getItems().put(clientItem2.getUniqueId(), clientItem2);
		
		//Change category name
		CategoryInfo clientList1Cat1 = clientList1.getCategories().first();
		clientList1Cat1.setName("Foo List 1 Category 1 new name");
		clientList1Cat1.setLastUpdate(uniqueTime++);
		
		//Add new category
		CategoryInfo clientList1Cat2 = new CategoryInfo();
		clientList1Cat2.setLastUpdate(uniqueTime++);
		clientList1Cat2.setName("Foo List 1 Category 2");
		clientList1Cat2.setUniqueId(":" + tempUniqueId++);
		clientList1.getCategories().add(clientList1Cat2);
		
		//************************************************************************
		//Test full compare
		List<Entity> updateEntities = new ArrayList<Entity>();
		List<Entity> deleteEntities = new ArrayList<Entity>();
		ListeyDataMultipleUsers updatedMultiUser = ListeyDataMultipleUsers.compareAndUpdate(uniqueIdCreator, serverMultiUser, clientMultiUser, updateEntities, deleteEntities);
		assertNotNull(updatedMultiUser);
		assertEquals(0, deleteEntities.size());
		assertEquals(2, updateEntities.size());
		
		//Verify list change was noticed
		ListInfo updatedList1 = updatedMultiUser.userData.get(FOO_EMAIL).lists.get(fooList1Id);
		assertTrue(updatedList1.shallowEquals(clientList1));
		ListInfo listFromEntity = new ListInfo(updateEntities.remove(0));
		assertTrue(listFromEntity.shallowEquals(clientList1));
		
		//Verify new list was noticed
		//Iterate through the list ids until you find the one that is NOT the one we already knew
		String updatedList2UniqueId = null;
		for (String oneId : updatedMultiUser.userData.get(FOO_EMAIL).lists.keySet()) {
			if (!oneId.equals(updatedList1.getUniqueId())) {
				updatedList2UniqueId = oneId;
				break;
			}
		}//foreach oneId
		ListInfo updatedList2 = updatedMultiUser.userData.get(FOO_EMAIL).lists.get(updatedList2UniqueId);
		assertEquals(clientList2.getLastUpdate(), updatedList2.getLastUpdate());
		assertEquals(clientList2.getName(), updatedList2.getName());
		assertEquals(clientList2.getStatus(), updatedList2.getStatus());
		assertTrue(DataStoreUniqueId.isTemporaryId(clientItem2.getUniqueId()));
		assertFalse(DataStoreUniqueId.isTemporaryId(updatedList2.getUniqueId()));
		assertFalse("Expected " + clientList2.getUniqueId() + " to be different from " + updatedList2.getUniqueId(),
				clientList2.getUniqueId().equals(updatedList2.getUniqueId()));
		listFromEntity = new ListInfo(updateEntities.remove(0));
		assertTrue(listFromEntity.shallowEquals(updatedList2));
		
		//Verify item change was noticed
		ItemInfo updatedItem1 = updatedList1.getItems().get(fooList1Item1Id);
		assertTrue(updatedItem1.shallowEquals(clientItem1));
		ItemInfo itemFromEntity = new ItemInfo(updateEntities.remove(0));
		assertTrue(itemFromEntity.shallowEquals(clientItem1));
		
		//Verify new item was noticed
		//Iterate through the item ids until you find the one that is NOT the one we already knew
		String updatedItem2UniqueId = null;
		for (String oneId : updatedList1.getItems().keySet()) {
			if (!oneId.equals(updatedItem1.getUniqueId())) {
				updatedItem2UniqueId = oneId;
				break;
			}
		}//foreach oneId
		ItemInfo updatedItem2 = updatedList1.getItems().get(updatedItem2UniqueId);
		assertEquals(clientItem2.getCount(), updatedItem2.getCount());
		assertEquals(clientItem2.getLastUpdate(), updatedItem2.getLastUpdate());
		assertEquals(clientItem2.getName(), updatedItem2.getName());
		assertEquals(clientItem2.getStatus(), updatedItem2.getStatus());
		assertTrue(DataStoreUniqueId.isTemporaryId(clientItem2.getUniqueId()));
		assertFalse(DataStoreUniqueId.isTemporaryId(updatedItem2.getUniqueId()));
		assertFalse("Expected " + clientItem2.getUniqueId() + " to be different from " + updatedItem2.getUniqueId(),
				clientItem2.getUniqueId().equals(updatedItem2.getUniqueId()));
		itemFromEntity = new ItemInfo(updateEntities.remove(0));
		assertTrue(itemFromEntity.shallowEquals(updatedItem2));
		
		//Verify new category was noticed
		Iterator<CategoryInfo> catIter = updatedList1.getCategories().iterator();
		CategoryInfo updatedCat2 = catIter.next();
		assertEquals(clientList1Cat2.getName(), updatedCat2.getName());
		assertEquals(clientList1Cat2.getLastUpdate(), updatedCat2.getLastUpdate());
		assertEquals(clientList1Cat2.getStatus(), updatedCat2.getStatus());
		assertTrue(DataStoreUniqueId.isTemporaryId(clientList1Cat2.getUniqueId()));
		assertFalse(DataStoreUniqueId.isTemporaryId(updatedCat2.getUniqueId()));
		assertFalse("Expected " + clientList1Cat2.getUniqueId() + " to be different from " + updatedCat2.getUniqueId(),
				clientList1Cat2.getUniqueId().equals(updatedCat2.getUniqueId()));
		CategoryInfo categoryFromEntity = new CategoryInfo(updateEntities.remove(0));
		assertTrue(categoryFromEntity.shallowEquals(updatedCat2));
		
		//Verify category change was noticed
		CategoryInfo updatedCat1 = catIter.next();
		assertTrue(updatedCat1.shallowEquals(clientList1Cat1));
		categoryFromEntity = new CategoryInfo(updateEntities.remove(0));
		assertTrue(categoryFromEntity.shallowEquals(clientList1Cat1));
		
		
	}//testClientChange
	
}//DatastoreAdapter
