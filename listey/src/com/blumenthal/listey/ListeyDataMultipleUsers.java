package com.blumenthal.listey;

import java.util.HashMap;
import java.util.Map;

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
	public Long lastUpdate;
	public Map< String, ListeyDataOneUser> userData = new HashMap<String, ListeyDataOneUser>();
	
	/**
	 * No-argument constructor desired by GSON
	 */
	public ListeyDataMultipleUsers() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Return an object from serialized JSON string
	 */
	public static ListeyDataMultipleUsers fromJson(String jsonString) {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(ListInfo.class, new ListInfoJsonAdapter());
		gsonBuilder.registerTypeAdapter(ItemInfo.class,  new ItemInfoJsonAdapter());
		Gson gson = gsonBuilder.create();
		ListeyDataMultipleUsers rv = gson.fromJson(jsonString, ListeyDataMultipleUsers.class);
		return rv;
	}//fromJson
}//ListeyDataMultipleUsers
