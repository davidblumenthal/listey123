package com.blumenthal.listey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** This class is a java implementation of the JSON primitive
 * JSON Data Format
   {
  	    "lastUpdate": "1234567890",
  		"lists" : {
    		'self' : {
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
            },
            '<OTHER_USER_EMAIL>' : {... see "self" above}
        }//lists
  }//top-level
 */
public class ListeyData {
	private static class ItemInfo {
		public String name;
		public Integer count;
		public Map<String, Boolean> categories = new HashMap();
		public Long lastUpdate;
	}
	
	private static class CategoryInfo {
		public String name;
		public Long lastUpdate;
	}
	
	private static class ListInfo {
		public List<ItemInfo> items = new ArrayList();
		public List<ItemInfo> crossedOffItems = new ArrayList();
		public List<CategoryInfo> categories = new ArrayList();
		public Long lastUpdate;
		public Set<String> selectedCategories = new HashSet();
	}
	public Long lastUpdate;
	public Map<String, ListInfo> lists;
}
