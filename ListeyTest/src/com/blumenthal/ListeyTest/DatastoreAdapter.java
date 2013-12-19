package com.blumenthal.ListeyTest;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.blumenthal.listey.CategoryInfo;
import com.blumenthal.listey.CategoryInfo.CategoryStatus;
import com.blumenthal.listey.ItemCategoryInfo;
import com.blumenthal.listey.ItemInfo;
import com.blumenthal.listey.ItemInfo.ItemStatus;
import com.blumenthal.listey.ListInfo;
import com.blumenthal.listey.ListInfo.ListInfoStatus;
import com.blumenthal.listey.ListeyDataOneUser;
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
        
	@Test
	public void testListInfoToFromEntity() {
		//set up the object
		Key userKey = KeyFactory.createKey("user", "test@test.com");
		ListInfo listInfo = new ListInfo(ListInfoStatus.ACTIVE, "1:1", "Test Name", 12345L );
		
		//create entity from the object
		Entity listInfoEntity = listInfo.toEntity(userKey);
		
		//create new object back from the entity
		ListInfo listInfo2 = new ListInfo(listInfoEntity);
		
		//verify all the fields of the new object match the original
		assertEquals(listInfo.status, listInfo2.status);
		assertEquals(listInfo.uniqueId, listInfo2.uniqueId);
		assertEquals(listInfo.name, listInfo2.name);
	}//testListInfoToFromEntity
	
    
	@Test
	public void testItemInfoToFromEntity() {
		//set up the object
		Key userKey = KeyFactory.createKey(ListeyDataOneUser.KIND, "test@test.com");
		Key listKey = KeyFactory.createKey(userKey, ListInfo.KIND, "1:1");
		ItemInfo itemInfo = new ItemInfo();
		itemInfo.uniqueId = "2:1";
		itemInfo.name = "Test Item 1";
		itemInfo.count=2;
		itemInfo.status=ItemStatus.ACTIVE;
		itemInfo.lastUpdate = 54321L;
		
		//create entity from the object
		Entity itemInfoEntity = itemInfo.toEntity(listKey);

		//create new object back from the entity
		ItemInfo itemInfo2 = new ItemInfo(itemInfoEntity);
		
		//verify all the fields of the new object match the original
		assertEquals(itemInfo.status, itemInfo2.status);
		assertEquals(itemInfo.uniqueId, itemInfo2.uniqueId);
		assertEquals(itemInfo.name, itemInfo2.name);
		assertEquals(itemInfo.lastUpdate, itemInfo2.lastUpdate);
		assertEquals(itemInfo.count, itemInfo2.count);
	}//testItemInfoToFromEntity
	
    
	@Test
	public void testItemCatToFromEntity() {
		//set up the object
		Key userKey = KeyFactory.createKey(ListeyDataOneUser.KIND, "test@test.com");
		Key listKey = KeyFactory.createKey(userKey, ListInfo.KIND, "1:1");
		Key itemKey = KeyFactory.createKey(listKey, ItemCategoryInfo.KIND, "2:1");
		ItemCategoryInfo itemCat = new ItemCategoryInfo();
		itemCat.uniqueCategoryId = "2:1";
		itemCat.status=ItemCategoryInfo.ItemCategoryStatus.ACTIVE;
		itemCat.lastUpdate = 54321L;
		
		//create entity from the object
		Entity itemCatEntity = itemCat.toEntity(itemKey);

		//create new object back from the entity
		ItemCategoryInfo itemCat2 = new ItemCategoryInfo(itemCatEntity);
		
		//verify all the fields of the new object match the original
		assertEquals(itemCat.status, itemCat2.status);
		assertEquals(itemCat.uniqueCategoryId, itemCat2.uniqueCategoryId);
		assertEquals(itemCat.lastUpdate, itemCat2.lastUpdate);
	}//testItemCatToFromEntity
	
	
	@Test
	public void testCategoryToFromEntity() {
		//set up the object
		Key userKey = KeyFactory.createKey(ListeyDataOneUser.KIND, "test@test.com");
		Key listKey = KeyFactory.createKey(userKey, ListInfo.KIND, "1:1");
		CategoryInfo catInfo = new CategoryInfo();
		catInfo.uniqueId = "3:1";
		catInfo.name = "Test Category 1";
		catInfo.status=CategoryStatus.ACTIVE;
		catInfo.lastUpdate = 54321L;
		
		//create entity from the object
		Entity itemInfoEntity = catInfo.toEntity(listKey);

		//create new object back from the entity
		CategoryInfo catInfo2 = new CategoryInfo(itemInfoEntity);
		
		//verify all the fields of the new object match the original
		assertEquals(catInfo.status, catInfo2.status);
		assertEquals(catInfo.uniqueId, catInfo2.uniqueId);
		assertEquals(catInfo.name, catInfo2.name);
		assertEquals(catInfo.lastUpdate, catInfo2.lastUpdate);
	}//testCategoryToFromEntity

}//DatastoreAdapter
