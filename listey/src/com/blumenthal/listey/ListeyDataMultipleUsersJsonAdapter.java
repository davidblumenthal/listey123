/**
 * 
 */
package com.blumenthal.listey;

import java.lang.reflect.Type;
import java.util.Map;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class ListeyDataMultipleUsersJsonAdapter implements JsonDeserializer<ListeyDataMultipleUsers> {
	@Override
	public ListeyDataMultipleUsers deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		ListeyDataMultipleUsers multiUserInfo = new ListeyDataMultipleUsers();

		JsonObject multiUserJson = json.getAsJsonObject();
		if (multiUserJson.has(ListeyDataMultipleUsers.USER_DATA)){
			JsonObject userDataJson = multiUserJson.get(ListeyDataMultipleUsers.USER_DATA).getAsJsonObject();
			for ( Map.Entry<String,JsonElement> userEntry : userDataJson.entrySet()){
				ListeyDataOneUser userInfo = context.deserialize(userEntry.getValue(), ListeyDataOneUser.class);
				userInfo.setUniqueId(userEntry.getKey());
				multiUserInfo.userData.put(userEntry.getKey(), userInfo);
			}//for categoriesJson
		}//if USER_DATA
		
		return multiUserInfo;
	}//deserialize
}//ListeyDataMultipleUsersJsonAdapter