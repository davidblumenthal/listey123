/**
 * 
 */
package com.blumenthal.ListeyTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.blumenthal.listey.CategoryInfo;
import com.blumenthal.listey.ItemCategoryInfo;
import com.blumenthal.listey.ItemCategoryInfo.ItemCategoryStatus;
import com.blumenthal.listey.ItemInfo;
import com.blumenthal.listey.ItemInfoJsonAdapter;
import com.blumenthal.listey.ListInfo;
import com.blumenthal.listey.ListInfo.ListInfoStatus;
import com.blumenthal.listey.ListeyDataMultipleUsers;
import com.blumenthal.listey.ListeyDataOneUser;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
/**
 * @author David
 *
 */
public class TestJsonParse {
	private static final Logger log = Logger.getLogger(TestJsonParse.class.getName());
	
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
	public void testEmptyHash() {
		String jsonString = "{}";
		ListeyDataMultipleUsers parsed = ListeyDataMultipleUsers.fromJson(jsonString);
		assertNotNull("Something should have been parsed", parsed);
		assertNull("lastUpdate should be null", parsed.lastUpdate);
		assertEquals("userData should be empty", parsed.userData.size(), 0);
	}//testEmptyHash
	
	@Test
	public void testParseEnum() {
		ItemInfo.ItemStatus itemStatus = ItemInfo.ItemStatus.valueOf("ACTIVE");
		assertEquals("Should have parsed ACTIVE", ItemInfo.ItemStatus.ACTIVE, itemStatus);
		
		itemStatus = ItemInfo.ItemStatus.valueOf("COMPLETED");
		assertEquals("Should have parsed COMPLETED", ItemInfo.ItemStatus.COMPLETED, itemStatus);
		
		itemStatus = null;
		try {
			itemStatus = ItemInfo.ItemStatus.valueOf("FOO");
		} catch (IllegalArgumentException e) {
			//expected
		}
		assertNull("Should not have parsed FOO", itemStatus);
	}//testParseEnum
	
	public static final String ITEM1_JSON = 
			"{'uniqueId':'2:1'"+
			",'state':'ACTIVE'"+
			",'count':'2'"+
			",'lastUpdate':'1234567890'" +
			",'name': 'Item 1 Name'"+
			",'categories': {'3:1':{'lastUpdate':'2234567890', 'status':'ACTIVE'}}"+
			"}";
	
	/** Make sure item is exactly what we'd expect to be parsed from ITEM1_JSON
	 * 
	 * @param item Item to be compared
	 */
	public void testItem1(ItemInfo item) {
		assertEquals("2:1", item.uniqueId);
		assertEquals(new Integer(2), item.count);
		assertEquals(new Long(1234567890L), item.lastUpdate);
		assertEquals("Item 1 Name", item.name);
		assertEquals(1, item.categories.size());
		assertEquals("Should have parsed ACTIVE", ItemInfo.ItemStatus.ACTIVE, item.status);
		assertNotNull("Categories shouldn't be null", item.categories);
		assertEquals(item.categories.size(), 1);
		ItemCategoryInfo parsedCatInfo = item.categories.get("3:1");
		assertNotNull("ItemCategory 3:1 should not be null", parsedCatInfo);
		assertEquals(new Long(2234567890L), parsedCatInfo.lastUpdate);
		assertEquals(ItemCategoryStatus.ACTIVE, parsedCatInfo.status);	
	}//testItem1
	
	
	
	@Test
	public void testItem(){
		Gson gson = new GsonBuilder().registerTypeAdapter(ItemInfo.class, new ItemInfoJsonAdapter()).create();
		ItemInfo item = gson.fromJson(ITEM1_JSON, ItemInfo.class);
		testItem1(item);
	}//testItem
	
	
	public static final String USER1_JSON =
			"{'lastUpdate': '1111'," +
			"'userData' : {" +
				"'test@test.com' : {" +
					"'lastUpdate':'2222'," +
					"'lists' : {" +
						"'1:1':{" +
							"'name':'Test List'"+
							",'lastUpdate':'1234567890'"+
							",'status' : 'ACTIVE'" +
							",'items': [" +
							ITEM1_JSON +
							"]" + //items
							",'categories' : [" +
							    "{'name':'Test Category', 'uniqueId':'4:1', 'lastUpdate':'312345'}" +
							"]" + //categories
							",'selectedCategories': ['4:1']" +
						"}" + //test list
					"}" + //lists
				"}" + //test@test.com
			"}" + //userData
		"}";//top hash
	
	@Test
	public void testOneUser() {
		ListeyDataMultipleUsers parsed = ListeyDataMultipleUsers.fromJson(USER1_JSON);
		assertNotNull("Something should have been parsed", parsed);
		assertEquals("top hash lastUpdate wasn't parsed right", new Long(1111L), parsed.lastUpdate);
		assertEquals("userData should have 1 item", 1, parsed.userData.size());
		
		ListeyDataOneUser oneUserData = parsed.userData.get("test@test.com");
		assertNotNull("oneUserData for test@test.com should not be null", oneUserData);
		assertEquals("test@test.com lastUpdate wasn't parsed right", new Long(2222L), oneUserData.lastUpdate);
		assertNotNull("lists should not be null", oneUserData.lists);
		
		ListInfo listInfo = oneUserData.lists.get("1:1");
		assertNotNull("testList should not be null", listInfo);
		assertEquals("Test List", listInfo.name);
		assertEquals(ListInfoStatus.ACTIVE, listInfo.status);
		assertEquals(new Long(1234567890L), listInfo.lastUpdate);
		
		assertEquals(1, listInfo.items.size());
		ItemInfo item = listInfo.items.get("2:1");
		testItem1(item);
		
		assertEquals(1, listInfo.categories.size());
		CategoryInfo cat = listInfo.categories.get(0);
		assertEquals("Test Category", cat.name);
		assertEquals("4:1", cat.uniqueId);
		assertEquals(new Long(312345L), cat.lastUpdate);
		
		assertEquals(1, listInfo.selectedCategories.size());
		assertEquals("4:1", listInfo.selectedCategories.toArray()[0]);
	}//testOneUser
	
	@Test
	public void testUniqueId(){
		//parse json, but first replace everything that looks like a permanent ID with something
		//that looks like a temporary id
		ListeyDataMultipleUsers parsed = ListeyDataMultipleUsers.fromJson(USER1_JSON.replaceAll("\\d+:(\\d+)", ":$1"));
		
		//Now, replace all the temporary ids with permanent ones
		
	}//testUniqueId

}//TestJsonParse
