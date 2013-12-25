package com.blumenthal.ListeyTest;

import static org.junit.Assert.assertEquals;

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
import com.blumenthal.listey.ListInfo.ListInfoStatus;
import com.blumenthal.listey.ListeyDataMultipleUsers;
import com.blumenthal.listey.ListeyDataOneUser;
import com.blumenthal.listey.OtherUserPrivOnList;
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
		DataStoreUniqueId uniqCreator = ListeyDataOneUser.uniqueIdCreator;
		
		//Create and flesh out a multi-user data structure
		long uniqueTime = 10000L;
		ListeyDataMultipleUsers multiUser = new ListeyDataMultipleUsers();
		ListeyDataOneUser fooUser = new ListeyDataOneUser();
		multiUser.userData.put(FOO_EMAIL, fooUser);
		
		//Create Foo list1
		ListInfo fooList1 = new ListInfo(ListInfoStatus.ACTIVE, uniqCreator.getUniqueId(), "Foo List 1", uniqueTime++);
		fooUser.lists.put(fooList1.uniqueId, fooList1);
		
		OtherUserPrivOnList barPrivOnFoo1 = new OtherUserPrivOnList();
		barPrivOnFoo1.userId = BAR_EMAIL;
		barPrivOnFoo1.lastUpdate = uniqueTime++;
		barPrivOnFoo1.priv = OtherUserPrivOnList.OtherUserPriv.FULL;
		fooList1.otherUserPrivs.put(BAR_EMAIL, barPrivOnFoo1);
		
		CategoryInfo fooList1Cat1 = new CategoryInfo();
		fooList1Cat1.lastUpdate = uniqueTime++;
		fooList1Cat1.name = "Foo List 1 Category 1";
		fooList1Cat1.status = CategoryInfo.CategoryStatus.ACTIVE;
		fooList1Cat1.uniqueId = uniqCreator.getUniqueId();
		fooList1.categories.add(fooList1Cat1);
		
		ItemInfo fooList1Item1 = new ItemInfo();
		fooList1Item1.uniqueId = uniqCreator.getUniqueId();
		fooList1.items.put(fooList1Item1.uniqueId, fooList1Item1);
		fooList1Item1.lastUpdate = uniqueTime++;
		fooList1Item1.name = "Foo List 1 Item 1";
		fooList1Item1.status = ItemInfo.ItemStatus.ACTIVE;
		fooList1Item1.count = 2L;
		ItemCategoryInfo fooList1Item1Cat1 = new ItemCategoryInfo();
		fooList1Item1Cat1.uniqueId = uniqCreator.getUniqueId();
		fooList1Item1.categories.put(fooList1Item1Cat1.uniqueId, fooList1Item1Cat1);
		fooList1Item1Cat1.lastUpdate = uniqueTime++;
		fooList1Item1Cat1.status = ItemCategoryInfo.ItemCategoryStatus.ACTIVE;
		
		//Add list for bar user
		//Create Bar list1
		ListeyDataOneUser barUser = new ListeyDataOneUser();
		multiUser.userData.put(BAR_EMAIL, barUser);
		ListInfo barList1 = new ListInfo(ListInfoStatus.ACTIVE, uniqCreator.getUniqueId(), "Bar List 1", uniqueTime++);
		barUser.lists.put(barList1.uniqueId, barList1);
				
		OtherUserPrivOnList fooPrivOnBar1 = new OtherUserPrivOnList();
		fooPrivOnBar1.userId = FOO_EMAIL;
		fooPrivOnBar1.lastUpdate = uniqueTime++;
		fooPrivOnBar1.priv = OtherUserPrivOnList.OtherUserPriv.FULL;
		barList1.otherUserPrivs.put(FOO_EMAIL, barPrivOnFoo1);
				
		CategoryInfo barList1Cat1 = new CategoryInfo();
		barList1Cat1.lastUpdate = uniqueTime++;
		barList1Cat1.name = "Bar List 1 Category 1";
		barList1Cat1.status = CategoryInfo.CategoryStatus.ACTIVE;
		barList1Cat1.uniqueId = uniqCreator.getUniqueId();
		barList1.categories.add(barList1Cat1);

		ItemInfo barList1Item1 = new ItemInfo();
		barList1Item1.uniqueId = uniqCreator.getUniqueId();
		barList1.items.put(barList1Item1.uniqueId, barList1Item1);
		barList1Item1.lastUpdate = uniqueTime++;
		barList1Item1.name = "Bar List 1 Item 1";
		barList1Item1.status = ItemInfo.ItemStatus.ACTIVE;
		barList1Item1.count = 2L;
		ItemCategoryInfo barList1Item1Cat1 = new ItemCategoryInfo();
		barList1Item1Cat1.uniqueId = uniqCreator.getUniqueId();
		barList1Item1.categories.put(barList1Item1Cat1.uniqueId, barList1Item1Cat1);
		barList1Item1Cat1.lastUpdate = uniqueTime++;
		barList1Item1Cat1.status = ItemCategoryInfo.ItemCategoryStatus.ACTIVE;
		
		
		//Convert multiUser to entities and write all the entities to the datastore at once
		List<Entity> entities = multiUser.toEntities();
		//Also convert bar's list to entities, since foo has privs on bar's list
		Key barKey = KeyFactory.createKey(ListeyDataOneUser.KIND, BAR_EMAIL);
		entities.addAll(barList1.toEntities(barKey));
		datastore.put(entities);
		
		return multiUser;
    }//createAndSaveUser
    
    
	
	@Test
	public void testSaveAndLoadUser(){
		ListeyDataMultipleUsers multiUser = createAndSaveUser();
		
		//Now wipe the other user privs map from bar, since those won't be reloaded
		//when we load data for foo
		for (Map.Entry<String, ListInfo> entry : multiUser.userData.get(BAR_EMAIL).lists.entrySet()) {
			entry.getValue().otherUserPrivs.clear();
		}
		
		//Now, reload (a new copy) back from the datastore
		ListeyDataMultipleUsers loadedMultiUser = new ListeyDataMultipleUsers(datastore, FOO_EMAIL);
		
		//Compare
		assertEquals("before/after multi-user jsons don't match", multiUser.toJson(), loadedMultiUser.toJson());
		assertEquals("loadedMultiUser doesn't match original", true, multiUser.deepEquals(loadedMultiUser));
	}//testLoadAndSaveUser

}//DatastoreAdapter
