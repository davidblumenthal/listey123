/**
 * 
 */
package com.blumenthal.ListeyTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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
import com.blumenthal.listey.OtherUserPrivOnList;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
/**
 * @author David
 *
 */
public class TestJsonParse {
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
		"{\"count\":2," +
		"\"lastUpdate\":1234567890," +
		"\"name\":\"Item 1 Name\"," +
		"\"status\":\"ACTIVE\"," +
		"\"uniqueId\":\"2:1\"," +
		"\"categories\":{" +
			"\"3:1\":{" +
				"\"status\":\"ACTIVE\"," +
				"\"lastUpdate\":2234567890}}}";
	
	/** Make sure item is exactly what we'd expect to be parsed from ITEM1_JSON
	 * 
	 * @param item Item to be compared
	 */
	public void testItem1(ItemInfo item) {
		assertEquals("2:1", item.getUniqueId());
		assertEquals(new Long(2), item.getCount());
		assertEquals(new Long(1234567890L), item.getLastUpdate());
		assertEquals("Item 1 Name", item.getName());
		assertEquals(1, item.getCategories().size());
		assertEquals("Should have parsed ACTIVE", ItemInfo.ItemStatus.ACTIVE, item.getStatus());
		assertNotNull("Categories shouldn't be null", item.getCategories());
		assertEquals(item.getCategories().size(), 1);
		ItemCategoryInfo parsedCatInfo = item.getCategories().get("3:1");
		assertNotNull("ItemCategory 3:1 should not be null", parsedCatInfo);
		assertEquals(new Long(2234567890L), parsedCatInfo.getLastUpdate());
		assertEquals(ItemCategoryStatus.ACTIVE, parsedCatInfo.getStatus());	
	}//testItem1
	
	
	
	@Test
	public void testItem(){
		Gson gson = new GsonBuilder().registerTypeAdapter(ItemInfo.class, new ItemInfoJsonAdapter()).create();
		
		//deserialize ITEM1_JSON into object
		ItemInfo item = gson.fromJson(ITEM1_JSON, ItemInfo.class);
		
		//validate the object looks like we expect
		testItem1(item);
		
		//serialize the item back into a JSON string
		String newItem1Json = gson.toJson(item);
		
		assertEquals(ITEM1_JSON, newItem1Json);
	}//testItem
	
	
	public static final String USER1_JSON =
			"{\"userData\":{" +
				"\"test@test.com\":{" +
					"\"lists\":{" +
						"\"1:1\":{" +
							"\"lastUpdate\":1234567890"+
							",\"name\":\"Test List\""+
							",\"status\":\"ACTIVE\"" +
							",\"items\":[" +
							ITEM1_JSON +
							"]" + //items
							",\"categories\":[" +
							    "{\"name\":\"Test Category\",\"uniqueId\":\"4:1\",\"lastUpdate\":312345}" +
							"]" + //categories
							",\"selectedCategories\":[\"4:1\"]" +
							",\"otherUserPrivs\":{\"foo@bar.com\":{\"priv\":\"FULL\",\"lastUpdate\":44321}}" +
						"}" + //test list
					"}" + //lists
				"}" + //test@test.com
			"}" + //userData
		"}";//top hash
	
	@Test
	public void testOneUser() {
		ListeyDataMultipleUsers parsed = ListeyDataMultipleUsers.fromJson(USER1_JSON);
		assertNotNull("Something should have been parsed", parsed);
		assertEquals("userData should have 1 item", 1, parsed.userData.size());
		
		ListeyDataOneUser oneUserData = parsed.userData.get("test@test.com");
		assertNotNull("oneUserData for test@test.com should not be null", oneUserData);
		assertNotNull("lists should not be null", oneUserData.lists);
		
		ListInfo listInfo = oneUserData.lists.get("1:1");
		assertNotNull("testList should not be null", listInfo);
		assertEquals("Test List", listInfo.getName());
		assertEquals(ListInfoStatus.ACTIVE, listInfo.getStatus());
		assertEquals(new Long(1234567890L), listInfo.getLastUpdate());
		
		assertEquals(1, listInfo.getItems().size());
		ItemInfo item = listInfo.getItems().get("2:1");
		testItem1(item);
		
		assertEquals(1, listInfo.getCategories().size());
		CategoryInfo cat = listInfo.getCategories().first();
		assertEquals("Test Category", cat.getName());
		assertEquals("4:1", cat.getUniqueId());
		assertEquals(new Long(312345L), cat.getLastUpdate());
		
		assertEquals(1, listInfo.getSelectedCategories().size());
		assertEquals("4:1", listInfo.getSelectedCategories().toArray()[0]);
		
		assertEquals(1, listInfo.getOtherUserPrivs().size());
		OtherUserPrivOnList privObj = listInfo.getOtherUserPrivs().get("foo@bar.com");
		assertEquals(OtherUserPrivOnList.OtherUserPriv.FULL, privObj.priv);
		assertEquals(new Long(44321L), privObj.lastUpdate);		
		
		//serialize the user back into a JSON string
		String newJson = ListeyDataMultipleUsers.getGson().toJson(parsed);
		
		assertEquals(USER1_JSON, newJson);
	}//testOneUser

}//TestJsonParse
