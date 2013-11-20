package com.blumenthal.listey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** This class is a java implementation of the JSON primitive for spec version 1
 * JSON Data Format
   {
  	    "lastUpdate": "1234567890",
  		"lists" : {
    		     <listName> : {
    		         "items" :
    		               [{"name" : "<NAME1>",
    		                 "categories" : {"<CATEGORY1>" : true, ...},
    		                 "count" : <NUMBER>
                             "lastUpdate": "1234567890",
                            },
                            ...
                           ],
                     "crossedOffItems" : [... SEE "items" above]
                     "lastUpdate": "1234567890",
                     "categories": [{name="<CATEGORY_NAME>", "lastUpdate"=123456789}, ...],
                     "selectedCategories" : ["<CATEGORY1>", ...]
                 }//<listName>
        }//lists
  }//top-level
 */
public class ListeyDataOneUser {
	static class ItemInfo {
		public String name;
		public Integer count;
		public Map<String, Boolean> categories = new HashMap();
		public Long lastUpdate;
		
		public ListeyDataMultipleUsers.ItemInfo convertToNextVersion() {
			ListeyDataMultipleUsers.ItemInfo v2 = new ListeyDataMultipleUsers.ItemInfo();
			v2.name = name;
			v2.count = count;
			v2.categories = categories;
			v2.lastUpdate = lastUpdate;
			
			return v2;
		}//convertToNextVersion
	}//ItemInfo
	
	static class CategoryInfo {
		public String name;
		public Long lastUpdate;
		
		public ListeyDataMultipleUsers.CategoryInfo convertToNextVersion() {
			ListeyDataMultipleUsers.CategoryInfo v2 = new ListeyDataMultipleUsers.CategoryInfo();
			v2.name = name;
			v2.lastUpdate = lastUpdate;
			
			return v2;
		}//convertToNextVersion
	}//CategoryInfo
	
	static class ListInfo {
		public List<ItemInfo> items = new ArrayList();
		public List<ItemInfo> crossedOffItems = new ArrayList();
		public List<CategoryInfo> categories = new ArrayList();
		public Long lastUpdate;
		public Set<String> selectedCategories = new HashSet();
		
		public ListeyDataMultipleUsers.ListInfo convertToNextVersion() {
			ListeyDataMultipleUsers.ListInfo v2 = new ListeyDataMultipleUsers.ListInfo();
			v2.items = new ArrayList();
			for (ItemInfo i : items) {
				v2.items.add(i.convertToNextVersion());
			}
			v2.crossedOffItems = new ArrayList();
			for (ItemInfo i : crossedOffItems) {
				v2.crossedOffItems.add(i.convertToNextVersion());
			}
			v2.categories = new ArrayList();
			for (CategoryInfo i : categories) {
				v2.categories.add(i.convertToNextVersion());
			}
			v2.lastUpdate = lastUpdate;
			
			v2.selectedCategories = selectedCategories;
			return v2;
		}
	}//ListInfo
	
	public Long lastUpdate;
	public Map<String, ListInfo> lists;
	
	/**
	 * Take the spec version 1 data in this and copy it into a 
	 * spec version 2 object and return that.
	 * 
	 * @return A spec version 2 object
	 */
	public ListeyDataMultipleUsers convertToNextVersion() {
		//create new v2 object
		ListeyDataMultipleUsers v2 = new ListeyDataMultipleUsers();
		
		//Copy top-level fields
		v2.lastUpdate = lastUpdate;
		v2.lists = new HashMap();
		
		//Deep-copy lists map
		Map <String, ListeyDataMultipleUsers.ListInfo> v2ListMap = new HashMap();
		for(String key : lists.keySet()) {
			ListInfo v1ListInfo = lists.get(key);
			ListeyDataMultipleUsers.ListInfo v2ListInfo = v1ListInfo.convertToNextVersion();
			v2ListMap.put(key,  v2ListInfo);
		}
		v2.lists.put(ListeyData.SELF, v2ListMap);
		
		return v2;
	}//convertToNextVersion
}//ListeyDataV1
